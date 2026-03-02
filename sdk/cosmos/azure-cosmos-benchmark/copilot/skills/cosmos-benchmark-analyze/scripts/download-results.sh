#!/bin/bash
# download-results.sh — Download benchmark results from VM to local machine
#
# Usage:
#   ./download-results.sh --config-dir <path> --run-name <name> [--output-dir ./results]
#   ./download-results.sh --config-dir <path> --all [--output-dir ./results]
#   ./download-results.sh --config-dir <path> --list
#
# Modes:
#   --run-name <name>  Download a specific run directory
#   --all              Download all runs from the VM
#   --list             List available runs on the VM (no download)

set -euo pipefail

CONFIG_DIR=""
RUN_NAME=""
OUTPUT_DIR=""
ALL=false
LIST=false
REMOTE_RESULTS="~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results"

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir) CONFIG_DIR="$2"; shift 2 ;;
    --run-name)   RUN_NAME="$2"; shift 2 ;;
    --output-dir) OUTPUT_DIR="$2"; shift 2 ;;
    --all)        ALL=true; shift ;;
    --list)       LIST=true; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" ]]; then
  echo "Usage: $0 --config-dir <path> (--run-name <name> | --all | --list)" >&2
  exit 1
fi

# Default output to $CONFIG_DIR/results
[[ -z "$OUTPUT_DIR" ]] && OUTPUT_DIR="$CONFIG_DIR/results"

VM_IP=$(cat "$CONFIG_DIR/vm-ip")
VM_USER=$(cat "$CONFIG_DIR/vm-user")
VM_KEY=$(cat "$CONFIG_DIR/vm-key")
SCP_CMD="scp -i $VM_KEY -o StrictHostKeyChecking=no -r"
SSH_CMD="ssh -i $VM_KEY -o StrictHostKeyChecking=no $VM_USER@$VM_IP"

if [[ "$LIST" == "true" ]]; then
  echo "Available runs on VM ($VM_IP):"
  $SSH_CMD "ls -1d $REMOTE_RESULTS/*/ 2>/dev/null | while read d; do
    NAME=\$(basename \$d)
    HAS_MONITOR=\$(test -f \$d/monitor.csv && echo '📊' || echo '❌')
    GIT_INFO=''
    if [[ -f \$d/git-info.json ]]; then
      GIT_INFO=\$(python3 -c \"import json; d=json.load(open('\$d/git-info.json')); print(f\\\"branch={d.get('branch','?')} commit={d.get('commit','?')}\\\")\" 2>/dev/null || echo '')
    fi
    echo \"  \$HAS_MONITOR \$NAME  \$GIT_INFO\"
  done" 2>/dev/null || echo "  (no runs found)"
  exit 0
fi

mkdir -p "$OUTPUT_DIR"

if [[ -n "$RUN_NAME" ]]; then
  echo "Downloading: $RUN_NAME"
  $SCP_CMD "$VM_USER@$VM_IP:$REMOTE_RESULTS/$RUN_NAME" "$OUTPUT_DIR/"
  echo "✅ Downloaded to $OUTPUT_DIR/$RUN_NAME"

elif [[ "$ALL" == "true" ]]; then
  echo "Downloading all runs from VM..."
  RUNS=$($SSH_CMD "ls -1d $REMOTE_RESULTS/*/ 2>/dev/null | xargs -I{} basename {}" || echo "")
  if [[ -z "$RUNS" ]]; then
    echo "No runs found on VM"
    exit 0
  fi
  COUNT=0
  for RUN in $RUNS; do
    echo "  Downloading: $RUN"
    $SCP_CMD "$VM_USER@$VM_IP:$REMOTE_RESULTS/$RUN" "$OUTPUT_DIR/"
    COUNT=$((COUNT + 1))
  done
  echo "✅ Downloaded $COUNT run(s) to $OUTPUT_DIR/"

else
  echo "Provide --run-name, --all, or --list" >&2
  exit 1
fi
