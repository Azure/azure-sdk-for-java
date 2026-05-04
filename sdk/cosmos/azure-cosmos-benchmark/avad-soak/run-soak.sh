#!/bin/bash
# =============================================================================
# AVAD Soak Test Orchestrator
# =============================================================================
# Single script that manages the full soak test lifecycle:
#   1. Deploy workloads via Helm
#   2. Run phase-based chaos (warm-up → steady → chaos → recovery → repeat)
#   3. Continuous reconciliation (no missed changes, AVAD ⊇ LV)
#   4. Collect results on exit
#
# Prerequisites:
#   - AKS cluster configured (kubectl context set)
#   - Helm 3 installed
#   - ACR image pushed (see infra/scripts/setup-acr.sh)
#   - Cosmos containers created (see infra/scripts/setup-cosmos.sh)
#   - az CLI logged in (for partition split + lease throttle)
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Configuration ─────────────────────────────────────────────────────────
NAMESPACE="${NAMESPACE:-cosmos-soak}"
RELEASE="${RELEASE:-cosmos-soak}"
VALUES_FILE="${VALUES_FILE:-$SCRIPT_DIR/infra/chart/values.yaml}"
VALUES_OVERRIDE="${VALUES_OVERRIDE:-}"
CHAOS_SCHEDULE="${CHAOS_SCHEDULE:-$SCRIPT_DIR/chaos/chaos-schedule.yaml}"

# Timing (all in seconds)
SOAK_DURATION_HOURS="${SOAK_DURATION_HOURS:-24}"
WARMUP_SEC="${WARMUP_SEC:-1800}"              # 30 min
STEADY_SEC="${STEADY_SEC:-3600}"              # 60 min
RECOVERY_SEC="${RECOVERY_SEC:-1800}"          # 30 min default
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-300}"  # 5 min

# Behavior
CHAOS_ENABLED="${CHAOS_ENABLED:-true}"
ABORT_ON_GAP="${ABORT_ON_GAP:-false}"

# Cosmos (for chaos scripts)
export COSMOS_ACCOUNT="${COSMOS_ACCOUNT:-abhm-cfp-region-test}"
export COSMOS_RG="${COSMOS_RG:-abhm-rg}"
export COSMOS_DB="${COSMOS_DB:-graph_db}"
export FEED_CONTAINER="${FEED_CONTAINER:-avad-test}"
export LEASE_CONTAINER="${LEASE_CONTAINER:-avad-test-leases}"

# Output
OUTPUT_DIR="$SCRIPT_DIR/soak-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUTPUT_DIR"

SOAK_DURATION_SEC=$((SOAK_DURATION_HOURS * 3600))
START_TIME=$(date +%s)
CHAOS_PID=""

# ── Helper functions ──────────────────────────────────────────────────────

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$OUTPUT_DIR/soak.log"; }

elapsed() {
    local now=$(date +%s)
    echo $(( now - START_TIME ))
}

is_expired() {
    [ "$SOAK_DURATION_SEC" -gt 0 ] && [ "$(elapsed)" -ge "$SOAK_DURATION_SEC" ]
}

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

    log "Collecting health metrics..."
    kubectl get pods -n "$NAMESPACE" -o wide \
        > "$OUTPUT_DIR/pods-final.txt" 2>/dev/null || true

    log "Results saved to: $OUTPUT_DIR"
    log "=== Soak test ended (elapsed: $(elapsed)s) ==="
}
trap cleanup EXIT ERR INT TERM

# ── Health check ──────────────────────────────────────────────────────────

check_health() {
    log "Running health check..."
    local healthy=true

    # Check all pods are running
    local not_ready=$(kubectl get pods -n "$NAMESPACE" \
        --field-selector=status.phase!=Running \
        -o name 2>/dev/null | wc -l)

    if [ "$not_ready" -gt 0 ]; then
        log "  ⚠️ $not_ready pods not in Running state"
    fi

    # Check metrics from ingestor pods
    for pod in $(kubectl get pods -n "$NAMESPACE" \
        -l "app.kubernetes.io/component=ingestor" \
        -o jsonpath='{.items[*].metadata.name}'); do
        local metrics=$(kubectl exec -n "$NAMESPACE" "$pod" -- \
            curl -s http://localhost:8080/metrics 2>/dev/null || echo "unreachable")
        echo "$metrics" >> "$OUTPUT_DIR/metrics-$(date +%H%M%S).log"

        # Check for missing previousImage
        local missing=$(echo "$metrics" | grep "previous_image_missing" | awk '{print $2}')
        if [ -n "$missing" ] && [ "$missing" != "0" ]; then
            log "  ❌ Missing previousImage count: $missing"
            healthy=false
        fi
    done

    if [ "$healthy" = true ]; then
        log "  ✅ Health check passed"
    else
        log "  ❌ Health check FAILED"
        if [ "$ABORT_ON_GAP" = "true" ]; then
            log "ABORT_ON_GAP is set — stopping soak test"
            exit 1
        fi
    fi
}

# ── Chaos runner (background) ────────────────────────────────────────────

run_chaos_loop() {
    log "Chaos loop starting (schedule: $CHAOS_SCHEDULE)"

    # Simple time-based chaos: iterate through scenarios
    local iteration=0
    while true; do
        iteration=$((iteration + 1))
        log "=== Chaos iteration $iteration ==="

        # Pod kill (every 2 hours → check every loop)
        if [ $((iteration % 1)) -eq 0 ]; then
            log "Firing: pod-kill"
            bash "$SCRIPT_DIR/chaos/scenarios/pod-kill.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
            sleep "$RECOVERY_SEC"
            check_health
        fi

        # Lease throttle (every other iteration)
        if [ $((iteration % 2)) -eq 0 ]; then
            log "Firing: lease-throttle"
            bash "$SCRIPT_DIR/chaos/scenarios/lease-throttle.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
            sleep "$RECOVERY_SEC"
            check_health
        fi

        # Restart storm (every 4th iteration)
        if [ $((iteration % 4)) -eq 0 ]; then
            log "Firing: restart-storm"
            bash "$SCRIPT_DIR/chaos/scenarios/restart-storm.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
            sleep "$RECOVERY_SEC"
            check_health
        fi

        # Partition split (every 6th iteration)
        if [ $((iteration % 6)) -eq 0 ]; then
            log "Firing: partition-split"
            bash "$SCRIPT_DIR/chaos/scenarios/partition-split.sh" 2>&1 | tee -a "$OUTPUT_DIR/chaos.log"
            sleep "$RECOVERY_SEC"
            check_health
        fi

        # Steady state between chaos events
        log "Steady state for ${STEADY_SEC}s..."
        sleep "$STEADY_SEC"

        if is_expired; then
            log "Soak duration expired, stopping chaos loop"
            break
        fi
    done
}

# ── Main ──────────────────────────────────────────────────────────────────

log "=== AVAD Soak Test Starting ==="
log "Duration: ${SOAK_DURATION_HOURS}h (${SOAK_DURATION_SEC}s)"
log "Chaos: $CHAOS_ENABLED"
log "Output: $OUTPUT_DIR"

# 1. Create namespace
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# 2. Deploy via Helm
log "Deploying workloads via Helm..."
HELM_ARGS=(
    upgrade --install "$RELEASE"
    "$SCRIPT_DIR/infra/chart"
    --namespace "$NAMESPACE"
    --values "$VALUES_FILE"
)
[ -n "$VALUES_OVERRIDE" ] && HELM_ARGS+=(--values "$VALUES_OVERRIDE")

helm "${HELM_ARGS[@]}"
log "Helm deploy complete"

# 3. Wait for warm-up
log "Warm-up phase (${WARMUP_SEC}s)..."
sleep 30  # let pods start scheduling

# Wait for all pods to be ready
kubectl wait --for=condition=ready pods \
    --all -n "$NAMESPACE" \
    --timeout="${WARMUP_SEC}s" || {
    log "⚠️ Not all pods ready after warm-up, continuing anyway"
}

log "Warm-up complete"
check_health

# 4. Start chaos loop in background (if enabled)
if [ "$CHAOS_ENABLED" = "true" ]; then
    run_chaos_loop &
    CHAOS_PID=$!
    log "Chaos loop started (PID: $CHAOS_PID)"
fi

# 5. Main monitoring loop
log "Entering main monitoring loop..."
while ! is_expired; do
    sleep "$HEALTH_CHECK_INTERVAL"
    check_health
done

log "=== Soak duration reached (${SOAK_DURATION_HOURS}h) ==="
log "Final health check..."
check_health

log "=== Soak Test Complete ==="
