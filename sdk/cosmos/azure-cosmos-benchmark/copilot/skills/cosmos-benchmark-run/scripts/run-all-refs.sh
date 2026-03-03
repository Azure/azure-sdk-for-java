#!/bin/bash
# run-all-refs.sh — Orchestrate benchmark execution across multiple git refs
#
# Usage:
#   ./run-all-refs.sh --config-dir <path> --refs "main,fix/leak" [--scenario SIMPLE]
#   ./run-all-refs.sh --config-dir <path> --refs "main" --force-copy-scripts
#
# Copies scripts to the VM via SCP, then for each ref executes
# vm-prepare-and-run.sh remotely (checkout → build → verify → run).

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

CONFIG_DIR=""
SCENARIO="SIMPLE"
REFS_CSV=""
TENANTS_FILE="~/tenants.json"
EXTRA_FLAGS=""
FORCE_COPY_SCRIPTS=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)    CONFIG_DIR="$2"; shift 2 ;;
    --scenario)      SCENARIO="$2"; shift 2 ;;
    --refs)          REFS_CSV="$2"; shift 2 ;;
    --tenants-file)  TENANTS_FILE="$2"; shift 2 ;;
    --extra-flags)   EXTRA_FLAGS="$2"; shift 2 ;;
    --force-copy-scripts) FORCE_COPY_SCRIPTS=true; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" || -z "$REFS_CSV" ]]; then
  echo "Usage: $0 --config-dir <path> --refs \"ref1,ref2,...\" [--scenario SIMPLE]" >&2
  exit 1
fi

VM_IP=$(cat "$CONFIG_DIR/vm-ip")
VM_USER=$(cat "$CONFIG_DIR/vm-user")
VM_KEY=$(cat "$CONFIG_DIR/vm-key")
SSH_CMD="ssh -i $VM_KEY -o StrictHostKeyChecking=no $VM_USER@$VM_IP"
SCP_CMD="scp -i $VM_KEY -o StrictHostKeyChecking=no"

# Copy scripts to VM.
# Default: only the bootstrapper (vm-prepare-and-run.sh) is copied; after
# checkout, remaining scripts are resolved from the cloned repo.
# --force-copy-scripts: copies ALL scripts and tells the bootstrapper to
# prefer ~/benchmark-scripts/ over repo versions (for testing local changes).
VM_SCRIPTS_DIR="~/benchmark-scripts"
$SSH_CMD "mkdir -p $VM_SCRIPTS_DIR"
if [[ "$FORCE_COPY_SCRIPTS" == "true" ]]; then
  for SCRIPT_FILE in vm-prepare-and-run.sh run-benchmark.sh monitor.sh capture-diagnostics.sh; do
    if [[ -f "$SCRIPT_DIR/$SCRIPT_FILE" ]]; then
      $SCP_CMD "$SCRIPT_DIR/$SCRIPT_FILE" "$VM_USER@$VM_IP:$VM_SCRIPTS_DIR/$SCRIPT_FILE"
    fi
  done
  $SSH_CMD "chmod +x $VM_SCRIPTS_DIR/*.sh"
  echo "All scripts copied to VM:$VM_SCRIPTS_DIR (force mode)"
else
  $SCP_CMD "$SCRIPT_DIR/vm-prepare-and-run.sh" "$VM_USER@$VM_IP:$VM_SCRIPTS_DIR/vm-prepare-and-run.sh"
  $SSH_CMD "chmod +x $VM_SCRIPTS_DIR/*.sh"
  echo "Bootstrapper copied to VM:$VM_SCRIPTS_DIR/vm-prepare-and-run.sh"
fi
echo ""

IFS=',' read -ra REFS <<< "$REFS_CSV"
TOTAL=${#REFS[@]}
DATE=$(date +%Y%m%d)

echo "============================================="
echo "  Benchmark Run: $TOTAL ref(s), scenario=$SCENARIO"
echo "============================================="
for i in "${!REFS[@]}"; do
  echo "  [$((i+1))/$TOTAL] $(echo "${REFS[$i]}" | xargs)"
done
echo "============================================="
echo ""

SUCCEEDED=0
FAILED=0
RESULTS=()

for i in "${!REFS[@]}"; do
  REF=$(echo "${REFS[$i]}" | xargs)
  REF_LABEL=$(echo "$REF" | tr '/' '-' | tr '#' '-')
  RUN_NAME="${DATE}-${SCENARIO}-${REF_LABEL}"
  SEQ="[$((i+1))/$TOTAL]"

  echo ""
  echo "$SEQ Starting: $REF -> $RUN_NAME"
  echo "   (tmux session: checkout -> build -> verify -> run)"

  FORCE_FLAG=""
  [[ "$FORCE_COPY_SCRIPTS" == "true" ]] && FORCE_FLAG="--force-scripts"
  BENCH_DIR_VM="\$HOME/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark"

  # End any previous tmux session gracefully
  $SSH_CMD 'tmux send-keys -t bench C-c 2>/dev/null; sleep 1; tmux send-keys -t bench exit Enter 2>/dev/null; sleep 1' 2>/dev/null || true

  # Write a small launcher script on the VM to avoid nested quoting issues
  $SSH_CMD "cat > /tmp/bench-launch.sh << 'LAUNCHER'
#!/bin/bash
bash ~/benchmark-scripts/vm-prepare-and-run.sh "\$@"
EXIT_CODE=\$?
echo \$EXIT_CODE > ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/\$4/.exit-code
exit \$EXIT_CODE
LAUNCHER
chmod +x /tmp/bench-launch.sh"

  # Start entire pipeline in tmux (all steps survive SSH disconnection)
  $SSH_CMD "mkdir -p $BENCH_DIR_VM/results/$RUN_NAME && tmux new-session -d -s bench 'bash /tmp/bench-launch.sh $REF $SCENARIO $TENANTS_FILE $RUN_NAME $FORCE_FLAG $EXTRA_FLAGS'"
  echo "$SEQ tmux session started on VM"

  # Poll until tmux session ends
  case "$SCENARIO" in
    SIMPLE)  POLL_INTERVAL=120 ;;
    EXPAND)  POLL_INTERVAL=300 ;;
    CHURN)   POLL_INTERVAL=300 ;;
    *)       POLL_INTERVAL=120 ;;
  esac
  echo "$SEQ Polling every ${POLL_INTERVAL}s..."
  while $SSH_CMD "tmux has-session -t bench 2>/dev/null" 2>/dev/null; do
    sleep $POLL_INTERVAL
  done

  # Read exit code from the VM
  RUN_EXIT=$($SSH_CMD "cat $BENCH_DIR_VM/results/$RUN_NAME/.exit-code 2>/dev/null || echo 1" 2>/dev/null)
  RUN_EXIT=$(echo "$RUN_EXIT" | tr -d '[:space:]')

  if [[ $RUN_EXIT -eq 0 ]]; then
    echo "$SEQ ✅ Completed: $REF → results/$RUN_NAME"
    SUCCEEDED=$((SUCCEEDED + 1))
    RESULTS+=("✅ $RUN_NAME")
  else
    echo "$SEQ ❌ Failed: $REF (exit=$RUN_EXIT)"
    FAILED=$((FAILED + 1))
    RESULTS+=("❌ $RUN_NAME")
  fi
done

echo ""
echo "============================================="
echo "  Summary: $SUCCEEDED/$TOTAL succeeded, $FAILED/$TOTAL failed"
echo "============================================="
for R in "${RESULTS[@]}"; do
  echo "  $R"
done
echo ""

if [[ $TOTAL -gt 1 ]]; then
  echo "💡 Download and compare results:"
  echo "   bash scripts/download-results.sh --config-dir $CONFIG_DIR --all"
fi

[[ $FAILED -gt 0 ]] && exit 1
exit 0
