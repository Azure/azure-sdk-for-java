#!/bin/bash
# run-benchmark.sh  Run a multi-tenancy benchmark with external resource monitoring
#
# Usage:
#   ./run-benchmark.sh <scenario> <tenants-file> [output-dir] [extra-args...]

set -euo pipefail

SCENARIO=${1:-SCALING}
TENANTS_FILE=${2:-tenants.json}
if [[ ! -f "$TENANTS_FILE" && -f "sdk/cosmos/azure-cosmos-benchmark/benchmark-config/tenants.json" ]]; then
    TENANTS_FILE="sdk/cosmos/azure-cosmos-benchmark/benchmark-config/tenants.json"
fi

OUTPUT_DIR=${3:-./results/$(date +%Y%m%dT%H%M%S)-${SCENARIO}}
shift 3 2>/dev/null || true
EXTRA_ARGS="$*"

mkdir -p "$OUTPUT_DIR"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# MODULE_DIR: use the script's parent if it contains a target/ dir (repo layout),
# otherwise use the current working directory (caller is expected to cd to the
# benchmark module before invoking this script).
MODULE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
if [[ ! -d "$MODULE_DIR/target" && -d "$PWD/target" ]]; then
    MODULE_DIR="$PWD"
fi

# Git metadata
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
COMMIT_ID=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
cat > "${OUTPUT_DIR}/git-info.json" <<EOF
{
    "branch": "$BRANCH",
    "commitId": "$COMMIT_ID",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "=== Multi-Tenancy Benchmark ==="
echo "  Scenario:  $SCENARIO"
echo "  Tenants:   $TENANTS_FILE"
echo "  Branch:    $BRANCH"
echo "  Commit:    $COMMIT_ID"
echo "  Output:    $OUTPUT_DIR"

# Find benchmark JAR
BENCHMARK_JAR=$(find "$MODULE_DIR/target" -name "azure-cosmos-benchmark-*-jar-with-dependencies.jar" 2>/dev/null | head -1 || true)
if [[ -z "$BENCHMARK_JAR" ]]; then
    echo "ERROR: Benchmark JAR not found. Build first."
    exit 1
fi

JVM_OPTS="-Xmx8g -Xms8g -XX:+UseG1GC -XX:MaxDirectMemorySize=2g \
  -Xlog:gc*:file=${OUTPUT_DIR}/gc.log:time,uptime,level \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${OUTPUT_DIR}/"

# Start benchmark process (not piped, so we get the real PID)
java $JVM_OPTS \
  -cp "$BENCHMARK_JAR" \
  com.azure.cosmos.benchmark.Main \
  -tenantsFile "$TENANTS_FILE" \
  -reportingDirectory "${OUTPUT_DIR}/metrics" \
  $EXTRA_ARGS \
  > >(tee "${OUTPUT_DIR}/benchmark.log") 2>&1 &
JAVA_PID=$!

echo "  Java PID:  $JAVA_PID"

# Start monitor alongside (auto-stops when Java PID exits)
MONITOR_PID=""
if [[ -f "$SCRIPT_DIR/monitor.sh" ]]; then
    echo "  Monitor:   ${OUTPUT_DIR}/monitor.csv"
    bash "$SCRIPT_DIR/monitor.sh" "$JAVA_PID" 60 "$OUTPUT_DIR" &
    MONITOR_PID=$!
else
    echo "  Monitor:   skipped (monitor.sh not found)"
fi

# Cleanup function: stop monitor when benchmark exits
cleanup() {
    if [[ -n "$MONITOR_PID" ]] && kill -0 "$MONITOR_PID" 2>/dev/null; then
        kill "$MONITOR_PID" 2>/dev/null || true
        wait "$MONITOR_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Wait for benchmark to finish
wait "$JAVA_PID"
BENCH_EXIT=$?

# Collect final system snapshot
ss -s > "${OUTPUT_DIR}/ss-summary.txt" 2>/dev/null || true

echo "Results in: $OUTPUT_DIR"
exit $BENCH_EXIT
