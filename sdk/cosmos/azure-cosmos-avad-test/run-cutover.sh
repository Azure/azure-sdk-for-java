#!/bin/bash
# =============================================================================
# AVAD Cut-Over Test Orchestrator
# =============================================================================
# Runs from your dev box. Coordinates across 2 VMs via SSH.
#
# Architecture:
#   Dev box (this script) ──SSH──► EUS VM (ingestor)
#                          ──SSH──► WCUS VM (lv-reader, avad-reader)
#
# Sequence:
#   1. Start ingestor on EUS VM
#   2. Wait for warm-up (configurable)
#   3. Start LV CFP reader on WCUS VM
#   4. Wait for LV warm-up (verify events flowing)
#   5. Start AVAD CFP reader on WCUS VM (staggered start)
#   6. Run all 3 concurrently for PARALLEL_DURATION
#   7. Stop LV reader → verify AVAD continues
#   8. Run AVAD-only for AVAD_ONLY_DURATION
#   9. Stop all
#  10. Collect logs from both VMs
#  11. Run reconciler locally
# =============================================================================

set -euo pipefail

# ── Cleanup trap — stops all remote processes on error/exit ────────────────
INGESTOR_PID=""
LV_PID=""
AVAD_PID=""
SPARK_LV_RUN_ID=""
SPARK_AVAD_RUN_ID=""

cleanup() {
    log "=== Cleanup triggered ==="
    [ -n "$AVAD_PID" ] && stop_remote "$READER_VM" "$AVAD_PID" "avad-reader" 10 2>/dev/null || true
    [ -n "$LV_PID" ] && stop_remote "$READER_VM" "$LV_PID" "lv-reader" 10 2>/dev/null || true
    [ -n "$INGESTOR_PID" ] && stop_remote "$INGESTOR_VM" "$INGESTOR_PID" "ingestor" 10 2>/dev/null || true
    [ -n "$SPARK_LV_RUN_ID" ] && stop_spark_job "$SPARK_LV_RUN_ID" "LV Spark" 2>/dev/null || true
    [ -n "$SPARK_AVAD_RUN_ID" ] && stop_spark_job "$SPARK_AVAD_RUN_ID" "AVAD Spark" 2>/dev/null || true
    log "=== Cleanup complete ==="
}
trap cleanup EXIT ERR INT TERM

# ── Configuration ──────────────────────────────────────────────────────────
INGESTOR_VM="azureuser@<INGESTOR_EUS_IP>"
READER_VM="azureuser@<READER_WCUS_IP>"

# Databricks workspace
DATABRICKS_HOST="${DATABRICKS_HOST:?Set DATABRICKS_HOST (e.g. https://adb-xxx.azuredatabricks.net)}"
DATABRICKS_TOKEN="${DATABRICKS_TOKEN:?Set DATABRICKS_TOKEN}"
SPARK_LV_JOB_ID="${SPARK_LV_JOB_ID:?Set SPARK_LV_JOB_ID (Databricks job ID for LV Spark reader)}"
SPARK_AVAD_JOB_ID="${SPARK_AVAD_JOB_ID:?Set SPARK_AVAD_JOB_ID (Databricks job ID for AVAD Spark reader)}"

JAR="cosmos-avad-test-1.0-SNAPSHOT.jar"
JAR_PATH="/home/azureuser/$JAR"

# Cosmos config (set these or export before running)
export COSMOS_ENDPOINT="${COSMOS_ENDPOINT:?Set COSMOS_ENDPOINT}"
export COSMOS_KEY="${COSMOS_KEY:?Set COSMOS_KEY}"
export COSMOS_DATABASE="${COSMOS_DATABASE:-graph_db}"
export COSMOS_FEED_CONTAINER="${COSMOS_FEED_CONTAINER:-avad-test}"
export COSMOS_LEASE_CONTAINER="${COSMOS_LEASE_CONTAINER:-avad-test-leases}"
export COSMOS_PREFERRED_REGION="${COSMOS_PREFERRED_REGION:-West Central US}"
export OPS_PER_SEC="${OPS_PER_SEC:-10}"

# Timing
INGESTOR_WARMUP_SEC=60          # Let ingestor run before starting readers
LV_WARMUP_SEC=120               # Let LV reader run before starting AVAD
PARALLEL_DURATION_SEC=1800      # All workloads running before split #1 (30 min)
SPLIT1_SETTLE_SEC=300           # Wait for split #1 to complete (5 min)
SPLIT2_SETTLE_SEC=300           # Wait for split #2 to complete (5 min)
POST_SPLIT_DURATION_SEC=1800    # Run after splits to verify no gaps (30 min)
AVAD_ONLY_DURATION_SEC=1800     # AVAD-only after LV shutdown (30 min)
SPARK_AVAD_DELAY_SEC=120        # Delay before starting AVAD Spark after LV Spark

# Throughput levels for 2-level split test
# Start at 10K (~1 PP) → 50K (~5 PP, split #1) → 100K (~10 PP, split #2)
INITIAL_THROUGHPUT=10000
SPLIT1_THROUGHPUT=50000
SPLIT2_THROUGHPUT=100000

# Cosmos container config (for throughput scaling)
COSMOS_ACCOUNT="${COSMOS_ACCOUNT:-abhm-cfp-region-test}"
COSMOS_RG="${COSMOS_RG:-abhm-rg}"

# Local output directory
OUTPUT_DIR="./cutover-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUTPUT_DIR"

# ── Helper functions ───────────────────────────────────────────────────────

log() { echo "[$(date '+%H:%M:%S')] $*"; }

ssh_cmd() {
    local vm="$1"; shift
    ssh -o StrictHostKeyChecking=no "$vm" "$@"
}

# Start a process on a remote VM, returns the remote PID
start_remote() {
    local vm="$1"
    local mode="$2"
    local log_file="$3"
    local extra_env="${4:-}"

    log "Starting --mode $mode on $vm"
    local pid
    pid=$(ssh_cmd "$vm" "
        export COSMOS_ENDPOINT='$COSMOS_ENDPOINT'
        export COSMOS_KEY='$COSMOS_KEY'
        export COSMOS_DATABASE='$COSMOS_DATABASE'
        export COSMOS_FEED_CONTAINER='$COSMOS_FEED_CONTAINER'
        export COSMOS_LEASE_CONTAINER='$COSMOS_LEASE_CONTAINER'
        export COSMOS_PREFERRED_REGION='$COSMOS_PREFERRED_REGION'
        export OPS_PER_SEC='$OPS_PER_SEC'
        $extra_env
        nohup java -jar $JAR_PATH --mode $mode > $log_file 2>&1 &
        echo \$!
    ")
    log "  PID: $pid"
    echo "$pid"
}

# Stop a process on a remote VM by PID with graceful timeout
stop_remote() {
    local vm="$1"
    local pid="$2"
    local label="$3"
    local timeout="${4:-30}" # default 30s graceful shutdown

    log "Stopping $label (PID $pid) on $vm, timeout=${timeout}s"
    ssh_cmd "$vm" "kill $pid 2>/dev/null || true"

    # Wait for graceful exit with timeout loop
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if ! ssh_cmd "$vm" "kill -0 $pid 2>/dev/null"; then
            log "  $label exited gracefully after ${elapsed}s"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done

    # Force kill only if still running
    log "  $label still running after ${timeout}s, sending SIGKILL"
    ssh_cmd "$vm" "kill -9 $pid 2>/dev/null || true"
    sleep 2
    log "  $label force-killed"
}

# Collect a file from a remote VM
collect_log() {
    local vm="$1"
    local remote_path="$2"
    local local_name="$3"

    log "Collecting $remote_path from $vm"
    scp -o StrictHostKeyChecking=no "$vm:$remote_path" "$OUTPUT_DIR/$local_name" 2>/dev/null || \
        log "  WARNING: Could not collect $remote_path"
}

# ── Databricks helpers ─────────────────────────────────────────────────────

dbx_api() {
    local method="$1"
    local endpoint="$2"
    shift 2
    curl -s -X "$method" \
        "$DATABRICKS_HOST/api/2.1/jobs$endpoint" \
        -H "Authorization: Bearer $DATABRICKS_TOKEN" \
        -H "Content-Type: application/json" \
        "$@"
}

# Start a Databricks job, returns run_id
start_spark_job() {
    local job_id="$1"
    local label="$2"

    log "Starting Spark $label (job_id=$job_id)"
    local response
    response=$(dbx_api POST "/run-now" -d "{\"job_id\": $job_id}")
    local run_id
    run_id=$(echo "$response" | python3 -c "import sys,json; print(json.load(sys.stdin).get('run_id',''))" 2>/dev/null)

    if [ -z "$run_id" ]; then
        log "  ERROR: Failed to start Spark $label: $response"
        echo ""
    else
        log "  Spark $label run_id: $run_id"
        echo "$run_id"
    fi
}

# Cancel a Databricks run
stop_spark_job() {
    local run_id="$1"
    local label="$2"

    if [ -n "$run_id" ]; then
        log "Cancelling Spark $label (run_id=$run_id)"
        dbx_api POST "/runs/cancel" -d "{\"run_id\": $run_id}" > /dev/null
        log "  Spark $label cancelled"
    fi
}

# Get Spark run status
spark_status() {
    local run_id="$1"
    dbx_api GET "/runs/get?run_id=$run_id" | \
        python3 -c "import sys,json; r=json.load(sys.stdin); print(r.get('state',{}).get('life_cycle_state','UNKNOWN'))" 2>/dev/null
}

# ── Pre-flight checks ─────────────────────────────────────────────────────

log "=== AVAD Cut-Over Test ==="
log "Ingestor VM:     $INGESTOR_VM"
log "Reader VM:       $READER_VM"
log "Databricks:      $DATABRICKS_HOST"
log "Spark LV job:    $SPARK_LV_JOB_ID"
log "Spark AVAD job:  $SPARK_AVAD_JOB_ID"
log "Output dir:      $OUTPUT_DIR"
log ""

log "Checking JAR exists on both VMs..."
ssh_cmd "$INGESTOR_VM" "test -f $JAR_PATH" || { log "ERROR: JAR not found on ingestor VM"; exit 1; }
ssh_cmd "$READER_VM"   "test -f $JAR_PATH" || { log "ERROR: JAR not found on reader VM"; exit 1; }
log "  ✅ JAR found on both VMs"

log "Checking Java on both VMs..."
ssh_cmd "$INGESTOR_VM" "java -version" 2>&1 | head -1
ssh_cmd "$READER_VM"   "java -version" 2>&1 | head -1
log ""

# ── Phase 0: Scale container to initial throughput ─────────────────────────

log "=== Phase 0: Scaling container to ${INITIAL_THROUGHPUT} RU/s (~1 PP) ==="
az cosmosdb sql container throughput update \
    --account-name "$COSMOS_ACCOUNT" --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DATABASE" --name "$COSMOS_FEED_CONTAINER" \
    --throughput "$INITIAL_THROUGHPUT" -o none 2>&1 || log "  WARNING: throughput update failed"
log "  Container at ${INITIAL_THROUGHPUT} RU/s. Waiting 60s for propagation..."
sleep 60

# ── Phase 1: Start ingestor ───────────────────────────────────────────────

log "=== Phase 1: Ingestor warm-up ($INGESTOR_WARMUP_SEC sec) ==="
INGESTOR_PID=$(start_remote "$INGESTOR_VM" "ingestor" "/home/azureuser/ingestor.log" \
    "export PRODUCED_LOG=/home/azureuser/produced.log")
sleep "$INGESTOR_WARMUP_SEC"
log "  Ingestor warm-up complete"

# ── Phase 2: Start LV reader + LV Spark ───────────────────────────────────

log "=== Phase 2: LV Reader + LV Spark warm-up ($LV_WARMUP_SEC sec) ==="
LV_PID=$(start_remote "$READER_VM" "lv-reader" "/home/azureuser/lv-reader.log" \
    "export CONSUMED_LOG=/home/azureuser/consumed-lv.log")
SPARK_LV_RUN_ID=$(start_spark_job "$SPARK_LV_JOB_ID" "LV reader")
sleep "$LV_WARMUP_SEC"
log "  LV reader + LV Spark warm-up complete"

# ── Phase 3: Start AVAD reader + AVAD Spark (staggered) ────────────────────

log "=== Phase 3: AVAD Reader + AVAD Spark starting (staggered after LV) ==="
AVAD_PID=$(start_remote "$READER_VM" "avad-reader" "/home/azureuser/avad-reader.log" \
    "export CONSUMED_LOG=/home/azureuser/consumed-avad.log")
log "  AVAD CFP started, waiting ${SPARK_AVAD_DELAY_SEC}s before starting AVAD Spark..."
sleep "$SPARK_AVAD_DELAY_SEC"
SPARK_AVAD_RUN_ID=$(start_spark_job "$SPARK_AVAD_JOB_ID" "AVAD reader")
log "  AVAD reader + AVAD Spark started"

# ── Phase 4: Parallel run before splits ────────────────────────────────────

log "=== Phase 4: Parallel run — all 5 workloads at ${INITIAL_THROUGHPUT} RU/s ($PARALLEL_DURATION_SEC sec) ==="
log "  All 5 workloads running on ~1 physical partition. Waiting..."
sleep "$PARALLEL_DURATION_SEC"

# ── Phase 4a: Split #1 — scale 10K → 50K (~1 PP → ~5 PP) ──────────────────

log "=== Phase 4a: Split #1 — scaling ${INITIAL_THROUGHPUT} → ${SPLIT1_THROUGHPUT} RU/s ==="
az cosmosdb sql container throughput update \
    --account-name "$COSMOS_ACCOUNT" --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DATABASE" --name "$COSMOS_FEED_CONTAINER" \
    --throughput "$SPLIT1_THROUGHPUT" -o none 2>&1 || log "  WARNING: throughput update failed"
log "  Throughput update submitted. Waiting ${SPLIT1_SETTLE_SEC}s for split to complete..."
sleep "$SPLIT1_SETTLE_SEC"
log "  Split #1 settle complete"

# ── Phase 4b: Split #2 — scale 50K → 100K (~5 PP → ~10 PP) ────────────────

log "=== Phase 4b: Split #2 — scaling ${SPLIT1_THROUGHPUT} → ${SPLIT2_THROUGHPUT} RU/s ==="
az cosmosdb sql container throughput update \
    --account-name "$COSMOS_ACCOUNT" --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DATABASE" --name "$COSMOS_FEED_CONTAINER" \
    --throughput "$SPLIT2_THROUGHPUT" -o none 2>&1 || log "  WARNING: throughput update failed"
log "  Throughput update submitted. Waiting ${SPLIT2_SETTLE_SEC}s for split to complete..."
sleep "$SPLIT2_SETTLE_SEC"
log "  Split #2 settle complete"

# ── Phase 4c: Post-split run — verify no events missed ─────────────────────

log "=== Phase 4c: Post-split run — all workloads on ~10 PPs ($POST_SPLIT_DURATION_SEC sec) ==="
log "  Verifying CFP handled both splits. Waiting..."
sleep "$POST_SPLIT_DURATION_SEC"

# ── Phase 5: Stop LV reader + LV Spark ────────────────────────────────────

log "=== Phase 5: Stopping LV Reader + LV Spark (AVAD continues) ==="
stop_remote "$READER_VM" "$LV_PID" "lv-reader"
stop_spark_job "$SPARK_LV_RUN_ID" "LV reader"

# ── Phase 6: AVAD-only run ─────────────────────────────────────────────────

log "=== Phase 6: AVAD-only run ($AVAD_ONLY_DURATION_SEC sec) ==="
log "  Ingestor + AVAD CFP + AVAD Spark running. LV stopped. Waiting..."
sleep "$AVAD_ONLY_DURATION_SEC"

# ── Phase 7: Stop all ──────────────────────────────────────────────────────

log "=== Phase 7: Stopping all workloads ==="
stop_remote "$READER_VM" "$AVAD_PID" "avad-reader"
stop_spark_job "$SPARK_AVAD_RUN_ID" "AVAD reader"
stop_remote "$INGESTOR_VM" "$INGESTOR_PID" "ingestor"

# ── Phase 8: Collect logs ──────────────────────────────────────────────────

log "=== Phase 8: Collecting logs ==="
collect_log "$INGESTOR_VM" "/home/azureuser/produced.log"    "produced.log"
collect_log "$INGESTOR_VM" "/home/azureuser/ingestor.log"    "ingestor.log"
collect_log "$READER_VM"   "/home/azureuser/consumed-lv.log" "consumed-lv.log"
collect_log "$READER_VM"   "/home/azureuser/consumed-avad.log" "consumed-avad.log"
collect_log "$READER_VM"   "/home/azureuser/lv-reader.log"   "lv-reader.log"
collect_log "$READER_VM"   "/home/azureuser/avad-reader.log" "avad-reader.log"

# Collect Spark run details
log "Collecting Spark run status..."
if [ -n "$SPARK_LV_RUN_ID" ]; then
    dbx_api GET "/runs/get?run_id=$SPARK_LV_RUN_ID" > "$OUTPUT_DIR/spark-lv-run.json"
    log "  Spark LV final status: $(spark_status "$SPARK_LV_RUN_ID")"
fi
if [ -n "$SPARK_AVAD_RUN_ID" ]; then
    dbx_api GET "/runs/get?run_id=$SPARK_AVAD_RUN_ID" > "$OUTPUT_DIR/spark-avad-run.json"
    log "  Spark AVAD final status: $(spark_status "$SPARK_AVAD_RUN_ID")"
fi

# ── Phase 9: Reconciliation ───────────────────────────────────────────────

log "=== Phase 9: Running reconciliation ==="

log "Gap detection: produced vs AVAD consumed"
java -jar "target/$JAR" --mode reconcile \
    --produced "$OUTPUT_DIR/produced.log" \
    --consumed "$OUTPUT_DIR/consumed-avad.log" \
    | tee "$OUTPUT_DIR/reconcile-avad.txt"
AVAD_EXIT=$?

log "Gap detection: produced vs LV consumed"
java -jar "target/$JAR" --mode reconcile \
    --produced "$OUTPUT_DIR/produced.log" \
    --consumed "$OUTPUT_DIR/consumed-lv.log" \
    | tee "$OUTPUT_DIR/reconcile-lv.txt"
LV_EXIT=$?

log "Parity check: LV vs AVAD"
java -jar "target/$JAR" --mode reconcile \
    --lv "$OUTPUT_DIR/consumed-lv.log" \
    --avad "$OUTPUT_DIR/consumed-avad.log" \
    | tee "$OUTPUT_DIR/parity.txt"
PARITY_EXIT=$?

# ── Summary ────────────────────────────────────────────────────────────────

log "=== RESULTS ==="
log "  AVAD gap check:  $([ $AVAD_EXIT -eq 0 ] && echo '✅ PASS' || echo '❌ FAIL')"
log "  LV gap check:    $([ $LV_EXIT -eq 0 ] && echo '✅ PASS' || echo '❌ FAIL')"
log "  LV↔AVAD parity:  $([ $PARITY_EXIT -eq 0 ] && echo '✅ PASS' || echo '❌ FAIL')"
log "  Results in: $OUTPUT_DIR"
log "=== DONE ==="

exit $(( AVAD_EXIT + LV_EXIT + PARITY_EXIT ))
