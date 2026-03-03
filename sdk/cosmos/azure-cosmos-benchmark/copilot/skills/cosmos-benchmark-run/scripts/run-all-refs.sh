#!/bin/bash
# run-all-refs.sh — Orchestrate benchmark execution across multiple git refs
#
# Usage:
#   ./run-all-refs.sh --config-dir <path> --refs "main,fix/leak" [--scenario SIMPLE]
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

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)    CONFIG_DIR="$2"; shift 2 ;;
    --scenario)      SCENARIO="$2"; shift 2 ;;
    --refs)          REFS_CSV="$2"; shift 2 ;;
    --tenants-file)  TENANTS_FILE="$2"; shift 2 ;;
    --extra-flags)   EXTRA_FLAGS="$2"; shift 2 ;;
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

# Copy scripts to VM (avoids stdin piping issues with heredocs)
VM_SCRIPTS_DIR="~/benchmark-scripts"
$SSH_CMD "mkdir -p $VM_SCRIPTS_DIR"
for SCRIPT_FILE in vm-prepare-and-run.sh run-benchmark.sh monitor.sh capture-diagnostics.sh; do
  if [[ -f "$SCRIPT_DIR/$SCRIPT_FILE" ]]; then
    $SCP_CMD "$SCRIPT_DIR/$SCRIPT_FILE" "$VM_USER@$VM_IP:$VM_SCRIPTS_DIR/$SCRIPT_FILE"
  fi
done
$SSH_CMD "chmod +x $VM_SCRIPTS_DIR/*.sh"
echo "Scripts copied to VM:$VM_SCRIPTS_DIR"
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
  echo "$SEQ Starting: $REF → $RUN_NAME"
  echo "   (single SSH session: checkout → build → verify → run)"

  # Execute the script already on the VM (copied via SCP at startup)
  $SSH_CMD "bash $VM_SCRIPTS_DIR/vm-prepare-and-run.sh \
    '$REF' '$SCENARIO' '$TENANTS_FILE' '$RUN_NAME' $EXTRA_FLAGS"
  RUN_EXIT=$?

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
