#!/bin/bash
# check-status.sh — Check benchmark status on the VM
#
# Usage:
#   ./check-status.sh --config-dir <path> [--run-name <name>] [--verbose]

set -uo pipefail

CONFIG_DIR=""
RUN_NAME=""
VERBOSE=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)  CONFIG_DIR="$2"; shift 2 ;;
    --run-name)    RUN_NAME="$2"; shift 2 ;;
    --verbose)     VERBOSE=true; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" ]]; then
  echo "Usage: $0 --config-dir <path> [--run-name <name>] [--verbose]" >&2
  exit 1
fi

VM_IP=$(cat "$CONFIG_DIR/vm-ip")
VM_USER=$(cat "$CONFIG_DIR/vm-user")
VM_KEY=$(cat "$CONFIG_DIR/vm-key")
SSH_CMD="ssh -i $VM_KEY -o StrictHostKeyChecking=no -o ConnectTimeout=10 $VM_USER@$VM_IP"

BENCH_DIR="~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark"

echo "=== Benchmark Status ==="
echo "  VM: $VM_USER@$VM_IP"
echo ""

# 1. tmux session
echo "--- Tmux Session ---"
TMUX_STATUS=$($SSH_CMD "tmux has-session -t bench 2>/dev/null && echo 'RUNNING' || echo 'NONE'" 2>/dev/null)
if [[ "$TMUX_STATUS" == "RUNNING" ]]; then
  echo "  Status: ✅ Running"
  echo ""
  echo "  Latest output:"
  $SSH_CMD "tmux capture-pane -t bench -p | tail -15" 2>/dev/null | sed 's/^/    /'
else
  echo "  Status: ⏹️  No active session (build phase or completed)"
fi
echo ""

# 2. Results directories
echo "--- Results ---"
RESULTS=$($SSH_CMD "ls -lt $BENCH_DIR/results/ 2>/dev/null | grep '^d' | head -5" 2>/dev/null)
if [[ -n "$RESULTS" ]]; then
  echo "$RESULTS" | while read -r line; do
    DIR_NAME=$(echo "$line" | awk '{print $NF}')
    DIR_TIME=$(echo "$line" | awk '{print $6, $7, $8}')
    # Check for exit code file
    EXIT_FILE=$($SSH_CMD -n "cat $BENCH_DIR/results/$DIR_NAME/.exit-code 2>/dev/null" 2>/dev/null)
    if [[ -n "$EXIT_FILE" ]]; then
      if [[ "$EXIT_FILE" == "0" ]]; then
        STATUS="✅ Completed"
      else
        STATUS="❌ Failed (exit=$EXIT_FILE)"
      fi
    else
      # Check if monitor.csv exists and is growing
      MONITOR_LINES=$($SSH_CMD -n "wc -l < $BENCH_DIR/results/$DIR_NAME/monitor.csv 2>/dev/null" 2>/dev/null | tr -d ' ')
      if [[ -n "$MONITOR_LINES" && "$MONITOR_LINES" -gt 0 ]]; then
        STATUS="🔄 In progress (monitor: ${MONITOR_LINES} samples)"
      else
        STATUS="⏳ Starting or build-only"
      fi
    fi
    echo "  $DIR_NAME  ($DIR_TIME)  $STATUS"
  done
else
  echo "  No results directories found"
fi
echo ""

# 3. Git state
echo "--- Git ---"
$SSH_CMD "cd ~/azure-sdk-for-java && echo \"  Branch: \$(git rev-parse --abbrev-ref HEAD 2>/dev/null)\" && echo \"  Commit: \$(git log --oneline -1 2>/dev/null)\"" 2>/dev/null
echo ""

# 4. Run-specific details
if [[ -n "$RUN_NAME" ]]; then
  echo "--- Run: $RUN_NAME ---"
  RUN_DIR="$BENCH_DIR/results/$RUN_NAME"

  # git-info.json
  GIT_INFO=$($SSH_CMD "cat $RUN_DIR/git-info.json 2>/dev/null" 2>/dev/null)
  if [[ -n "$GIT_INFO" ]]; then
    echo "  Git info: $GIT_INFO"
  fi

  # Monitor CSV
  MONITOR_LINES=$($SSH_CMD "wc -l < $RUN_DIR/monitor.csv 2>/dev/null" 2>/dev/null | tr -d ' ')
  if [[ -n "$MONITOR_LINES" && "$MONITOR_LINES" -gt 0 ]]; then
    echo "  Monitor samples: $MONITOR_LINES"
    if [[ "$VERBOSE" == "true" ]]; then
      echo "  Last 3 monitor entries:"
      $SSH_CMD "tail -3 $RUN_DIR/monitor.csv 2>/dev/null" 2>/dev/null | sed 's/^/    /'
    fi
  fi

  # Metrics files
  METRIC_COUNT=$($SSH_CMD "ls $RUN_DIR/metrics/*.csv 2>/dev/null | wc -l" 2>/dev/null | tr -d ' ')
  if [[ -n "$METRIC_COUNT" && "$METRIC_COUNT" -gt 0 ]]; then
    echo "  Metric files: $METRIC_COUNT"
  fi

  # Disk usage
  DISK=$($SSH_CMD "du -sh $RUN_DIR 2>/dev/null" 2>/dev/null | awk '{print $1}')
  if [[ -n "$DISK" ]]; then
    echo "  Disk usage: $DISK"
  fi
  echo ""
fi

# 5. JAR status
echo "--- Build ---"
JAR=$($SSH_CMD "ls $BENCH_DIR/target/*jar-with-dependencies.jar 2>/dev/null | head -1" 2>/dev/null)
if [[ -n "$JAR" ]]; then
  echo "  JAR: ✅ $(basename $JAR)"
else
  echo "  JAR: ❌ Not found (build may be in progress or failed)"
fi
echo ""

# 6. System resources (verbose only)
if [[ "$VERBOSE" == "true" ]]; then
  echo "--- System ---"
  $SSH_CMD "echo \"  Disk: \$(df -h / | tail -1 | awk '{print \$4}') available\" && echo \"  Memory: \$(free -h 2>/dev/null | awk '/Mem:/{print \$3\"/\"\$2}' || echo 'N/A')\" && echo \"  Load: \$(uptime | sed 's/.*load average/load average/')\"" 2>/dev/null
  echo ""
fi
