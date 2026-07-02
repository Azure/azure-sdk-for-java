#!/bin/bash
# =============================================================================
# AVAD Soak Test — Local Mode
# =============================================================================
# Runs ingestor + avad-reader + lv-reader as local JVM processes.
# No AKS/Helm required. For dev-box validation before deploying to AKS.
#
# Usage:
#   ./run-local.sh --config config.json
#   ./run-local.sh --config config.json --duration 1800
#   COSMOS_KEY=xxx ./run-local.sh --config config.json
#
# Prerequisites:
#   - JDK 17+
#   - Maven (for first build)
#   - COSMOS_KEY env var set (or in config.json)
#   - Cosmos DB containers created (see infra/scripts/setup-cosmos.sh)
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$SCRIPT_DIR/.."
CONFIG_FILE=""
DURATION_OVERRIDE=""
OPS_OVERRIDE=""

# ── Parse args ────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
    case "$1" in
        --config)   CONFIG_FILE="$2"; shift 2 ;;
        --duration) DURATION_OVERRIDE="$2"; shift 2 ;;
        --ops)      OPS_OVERRIDE="$2"; shift 2 ;;
        -h|--help)
            echo "Usage: $0 --config <config.json> [--duration <seconds>] [--ops <ops/sec>]"
            exit 0 ;;
        *) echo "Unknown arg: $1"; exit 1 ;;
    esac
done

if [ -z "$CONFIG_FILE" ]; then
    echo "ERROR: --config <path> is required"
    exit 1
fi

# ── Apply overrides via env vars ──────────────────────────────────────────
[ -n "$DURATION_OVERRIDE" ] && export DURATION_SECONDS="$DURATION_OVERRIDE"
[ -n "$OPS_OVERRIDE" ] && export OPS_PER_SEC="$OPS_OVERRIDE"

# ── Build if needed ───────────────────────────────────────────────────────
JAR="$MODULE_DIR/target/azure-cosmos-benchmark-4.0.1-beta.1.jar"
CP_FILE="$MODULE_DIR/target/cp.txt"

if [ ! -f "$JAR" ]; then
    echo "=== Building module (first run) ==="
    cd "$MODULE_DIR"
    mvn package -DskipTests -DskipCheckstyle -Dspotbugs.skip=true -Drevapi.skip=true -B -q
    mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt -B -q
fi

if [ ! -f "$CP_FILE" ]; then
    cd "$MODULE_DIR"
    mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt -B -q
fi

# Build classpath with logback config, excluding log4j-slf4j-impl
CP_RAW=$(cat "$CP_FILE")
CP_FILTERED=$(echo "$CP_RAW" | tr ';' '\n' | tr ':' '\n' | grep -v 'log4j-slf4j-impl' | tr '\n' ':')
CLASSPATH="$SCRIPT_DIR:$JAR:$CP_FILTERED"

JAVA_CMD="java -cp $CLASSPATH"
MAIN_CLASS="com.azure.cosmos.avadtest.Main"

# ── Output directory ──────────────────────────────────────────────────────
RUN_ID="soak-$(date +%Y%m%d-%H%M%S)"
export RUN_ID
OUTPUT_DIR="$SCRIPT_DIR/local-run-$RUN_ID"
mkdir -p "$OUTPUT_DIR"

log() { echo "[$(date '+%H:%M:%S')] $*" | tee -a "$OUTPUT_DIR/run.log"; }

# ── PIDs for cleanup ─────────────────────────────────────────────────────
INGESTOR_PID=""
AVAD_PID=""
LV_PID=""

cleanup() {
    log "=== Stopping all processes ==="
    [ -n "$INGESTOR_PID" ] && kill "$INGESTOR_PID" 2>/dev/null && wait "$INGESTOR_PID" 2>/dev/null || true
    [ -n "$AVAD_PID" ]     && kill "$AVAD_PID" 2>/dev/null     && wait "$AVAD_PID" 2>/dev/null     || true
    [ -n "$LV_PID" ]       && kill "$LV_PID" 2>/dev/null       && wait "$LV_PID" 2>/dev/null       || true
    log "All processes stopped"
    log "Run ID: $RUN_ID"
    log "Logs: $OUTPUT_DIR"
    log "Reconcile: RUN_ID=$RUN_ID java -cp ... Main --mode reconcile --full --config $CONFIG_FILE"
}
trap cleanup EXIT INT TERM

# ── Launch processes ──────────────────────────────────────────────────────

log "=== AVAD Local Soak Test ==="
log "Config: $CONFIG_FILE"
log "Run ID: $RUN_ID"
log "Output: $OUTPUT_DIR"

# 1. Ingestor
log "Starting ingestor (port 8080)..."
$JAVA_CMD $MAIN_CLASS --mode ingestor --config "$CONFIG_FILE" --health-port 8080 \
    > "$OUTPUT_DIR/ingestor.log" 2>&1 &
INGESTOR_PID=$!
log "  Ingestor PID: $INGESTOR_PID"

# Wait for ingestor to start producing
sleep 10

# 2. AVAD reader
log "Starting avad-reader (port 8081)..."
CONSUMED_LOG="$OUTPUT_DIR/consumed-avad.log" \
$JAVA_CMD $MAIN_CLASS --mode avad-reader --config "$CONFIG_FILE" --health-port 8081 \
    > "$OUTPUT_DIR/avad-reader.log" 2>&1 &
AVAD_PID=$!
log "  AVAD reader PID: $AVAD_PID"

# 3. LV reader
log "Starting lv-reader (port 8082)..."
CONSUMED_LOG="$OUTPUT_DIR/consumed-lv.log" \
$JAVA_CMD $MAIN_CLASS --mode lv-reader --config "$CONFIG_FILE" --health-port 8082 \
    > "$OUTPUT_DIR/lv-reader.log" 2>&1 &
LV_PID=$!
log "  LV reader PID: $LV_PID"

log "All 3 processes running"
log "  tail -f $OUTPUT_DIR/ingestor.log"
log "  tail -f $OUTPUT_DIR/avad-reader.log"
log "  tail -f $OUTPUT_DIR/lv-reader.log"

# ── Monitor loop ──────────────────────────────────────────────────────────

check_alive() {
    local name="$1" pid="$2"
    if ! kill -0 "$pid" 2>/dev/null; then
        log "❌ $name (PID $pid) has exited!"
        return 1
    fi
    return 0
}

log "Monitoring processes (Ctrl+C to stop)..."
while true; do
    sleep 30

    alive=true
    check_alive "ingestor"    "$INGESTOR_PID" || alive=false
    check_alive "avad-reader" "$AVAD_PID"     || alive=false
    check_alive "lv-reader"   "$LV_PID"       || alive=false

    if [ "$alive" = false ]; then
        log "One or more processes died — check logs in $OUTPUT_DIR"
        exit 1
    fi

    # Print last progress line from ingestor
    tail -1 "$OUTPUT_DIR/ingestor.log" 2>/dev/null | grep -o 'Progress:.*' || true
done
