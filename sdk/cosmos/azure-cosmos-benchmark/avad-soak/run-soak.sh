#!/bin/bash
# =============================================================================
# AVAD Soak Test — AKS Mode
# =============================================================================
# Deploys ingestor + avad-reader + lv-reader to AKS via Helm, monitors health,
# and runs chaos scenarios on a schedule.
#
# Usage:
#   ./run-soak.sh
#   SOAK_DURATION_HOURS=12 CHAOS_ENABLED=false ./run-soak.sh
#   VALUES_OVERRIDE=values-prod.yaml ./run-soak.sh
#
# Prerequisites:
#   - AKS cluster configured (kubectl context set)
#   - Helm 3 installed
#   - ACR image pushed (see infra/scripts/setup-acr.sh)
#   - Cosmos containers created (see infra/scripts/setup-cosmos.sh)
#   - K8s secrets created (see infra/README.md)
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Configuration ─────────────────────────────────────────────────────────
NAMESPACE="${NAMESPACE:-cosmos-soak}"
RELEASE="${RELEASE:-cosmos-soak}"
VALUES_FILE="${VALUES_FILE:-$SCRIPT_DIR/infra/chart/values.yaml}"
VALUES_OVERRIDE="${VALUES_OVERRIDE:-}"

SOAK_DURATION_HOURS="${SOAK_DURATION_HOURS:-24}"
WARMUP_SEC="${WARMUP_SEC:-1800}"
STEADY_SEC="${STEADY_SEC:-3600}"
RECOVERY_SEC="${RECOVERY_SEC:-1800}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-300}"

CHAOS_ENABLED="${CHAOS_ENABLED:-true}"
ABORT_ON_GAP="${ABORT_ON_GAP:-false}"

export COSMOS_ACCOUNT="${COSMOS_ACCOUNT:-abhm-cfp-region-test}"
export COSMOS_RG="${COSMOS_RG:-abhm-rg}"

RUN_ID="${RUN_ID:-soak-$(date +%Y%m%d-%H%M%S)}"
export RUN_ID

OUTPUT_DIR="$SCRIPT_DIR/soak-results-$RUN_ID"
mkdir -p "$OUTPUT_DIR"

SOAK_DURATION_SEC=$((SOAK_DURATION_HOURS * 3600))
START_TIME=$(date +%s)
CHAOS_PID=""

# ── Helpers ───────────────────────────────────────────────────────────────

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$OUTPUT_DIR/soak.log"; }
elapsed() { echo $(( $(date +%s) - START_TIME )); }
is_expired() { [ "$SOAK_DURATION_SEC" -gt 0 ] && [ "$(elapsed)" -ge "$SOAK_DURATION_SEC" ]; }

# ── Cleanup ───────────────────────────────────────────────────────────────

cleanup() {
    log "=== Cleanup triggered ==="
    [ -n "$CHAOS_PID" ] && kill "$CHAOS_PID" 2>/dev/null || true

    log "Collecting pod logs..."
    for pod in $(kubectl get pods -n "$NAMESPACE" -o name 2>/dev/null); do
        local name=$(basename "$pod")
        kubectl logs "$pod" -n "$NAMESPACE" --tail=5000 \
            > "$OUTPUT_DIR/${name}.log" 2>/dev/null || true
    done

    kubectl get pods -n "$NAMESPACE" -o wide \
        > "$OUTPUT_DIR/pods-final.txt" 2>/dev/null || true

    log "Results: $OUTPUT_DIR"
    log "=== Soak test ended (elapsed: $(elapsed)s) ==="
}
trap cleanup EXIT ERR INT TERM

# ── Health check ──────────────────────────────────────────────────────────

check_health() {
    log "Running health check..."
    local healthy=true

    local not_ready=$(kubectl get pods -n "$NAMESPACE" \
        --field-selector=status.phase!=Running \
        -o name 2>/dev/null | wc -l)

    if [ "$not_ready" -gt 0 ]; then
        log "  ⚠️ $not_ready pods not in Running state"
        healthy=false
    fi

    # Check pod readiness
    local total_pods=$(kubectl get pods -n "$NAMESPACE" --no-headers 2>/dev/null | wc -l)
    local ready_pods=$(kubectl get pods -n "$NAMESPACE" --no-headers 2>/dev/null \
        | awk '$2 ~ /1\/1/' | wc -l)
    log "  Pods: $ready_pods/$total_pods ready"

    if [ "$healthy" = true ]; then
        log "  ✅ Health check passed"
    else
        log "  ❌ Health check FAILED"
        if [ "$ABORT_ON_GAP" = "true" ]; then
            log "ABORT_ON_GAP set — stopping"
            exit 1
        fi
    fi
}

# ── Chaos runner ──────────────────────────────────────────────────────────

run_chaos_loop() {
    log "Chaos loop starting"
    local iteration=0

    while ! is_expired; do
        iteration=$((iteration + 1))
        log "=== Chaos iteration $iteration ==="

        # Pod kill (every iteration)
        log "Firing: pod-kill"
        bash "$SCRIPT_DIR/chaos/scenarios/pod-kill.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
        sleep "$RECOVERY_SEC"
        check_health

        # Partition split (every 3rd iteration)
        if [ $((iteration % 3)) -eq 0 ]; then
            log "Firing: partition-split"
            bash "$SCRIPT_DIR/chaos/scenarios/partition-split.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
            sleep "$RECOVERY_SEC"
            check_health
        fi

        log "Steady state for ${STEADY_SEC}s..."
        sleep "$STEADY_SEC"
    done
}

# ── Main ──────────────────────────────────────────────────────────────────

log "=== AVAD AKS Soak Test ==="
log "Duration: ${SOAK_DURATION_HOURS}h | Chaos: $CHAOS_ENABLED"
log "Run ID: $RUN_ID"
log "Output: $OUTPUT_DIR"

# 1. Namespace
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# 2. Helm deploy
log "Deploying via Helm..."
HELM_ARGS=(
    upgrade --install "$RELEASE"
    "$SCRIPT_DIR/infra/chart"
    --namespace "$NAMESPACE"
    --values "$VALUES_FILE"
    --set "global.runId=$RUN_ID"
)
[ -n "$VALUES_OVERRIDE" ] && HELM_ARGS+=(--values "$VALUES_OVERRIDE")
helm "${HELM_ARGS[@]}"
log "Helm deploy complete"

# 3. Wait for pods
log "Warm-up (${WARMUP_SEC}s)..."
sleep 30
kubectl wait --for=condition=ready pods \
    --all -n "$NAMESPACE" \
    --timeout="${WARMUP_SEC}s" || {
    log "⚠️ Not all pods ready after warm-up"
}
check_health

# 4. Chaos (background)
if [ "$CHAOS_ENABLED" = "true" ]; then
    run_chaos_loop &
    CHAOS_PID=$!
    log "Chaos loop started (PID: $CHAOS_PID)"
fi

# 5. Monitor
log "Monitoring (Ctrl+C to stop)..."
while ! is_expired; do
    sleep "$HEALTH_CHECK_INTERVAL"
    check_health
done

log "=== Soak duration reached (${SOAK_DURATION_HOURS}h) ==="
check_health
log "=== Soak Test Complete ==="
