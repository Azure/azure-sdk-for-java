#!/bin/bash
# verify-resources.sh — Verify all benchmark resources are provisioned and accessible
#
# Usage:
#   ./verify-resources.sh --config-dir <path> [--cosmos-count <N>]
#
# Checks:
#   1. Config directory exists with expected files
#   2. clientHostAndKey.txt has the expected number of entries
#   3. App Insights connection string is non-empty
#   4. VM is SSH-reachable and has JDK + Maven installed
#
# Exit codes:
#   0 — all checks passed, ready to proceed
#   1 — one or more checks failed

set -uo pipefail

CONFIG_DIR=""
COSMOS_COUNT=0  # 0 means skip count validation, just check file exists

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)   CONFIG_DIR="$2"; shift 2 ;;
    --cosmos-count) COSMOS_COUNT="$2"; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" ]]; then
  echo "Usage: $0 --config-dir <path> [--cosmos-count N]" >&2
  exit 1
fi

PASS=true
CHECKS=""

check() {
  local name="$1" result="$2" msg="$3"
  if [[ "$result" == "true" ]]; then
    CHECKS="$CHECKS\n  ✅ $name: $msg"
  else
    CHECKS="$CHECKS\n  ❌ $name: $msg"
    PASS=false
  fi
}

# --- 1. Config directory ---
if [[ -d "$CONFIG_DIR" ]]; then
  check "Config directory" "true" "$CONFIG_DIR exists"
else
  check "Config directory" "false" "$CONFIG_DIR not found"
  echo -e "$CHECKS"
  exit 1
fi

# --- 2. Cosmos DB credentials ---
if [[ -f "$CONFIG_DIR/clientHostAndKey.txt" ]]; then
  LINE_COUNT=$(wc -l < "$CONFIG_DIR/clientHostAndKey.txt" | tr -d ' ')
  if [[ "$COSMOS_COUNT" -gt 0 && "$LINE_COUNT" -ne "$COSMOS_COUNT" ]]; then
    check "Cosmos DB credentials" "false" "Expected $COSMOS_COUNT entries, found $LINE_COUNT"
  else
    check "Cosmos DB credentials" "true" "$LINE_COUNT account(s) in clientHostAndKey.txt"
  fi
else
  check "Cosmos DB credentials" "false" "clientHostAndKey.txt not found"
fi

# --- 3. App Insights connection string ---
if [[ -f "$CONFIG_DIR/app-insights-connection-string.txt" ]]; then
  AI_CONN=$(cat "$CONFIG_DIR/app-insights-connection-string.txt" | tr -d '[:space:]')
  if [[ -n "$AI_CONN" ]]; then
    check "App Insights" "true" "Connection string present"
  else
    check "App Insights" "false" "Connection string file is empty"
  fi
else
  check "App Insights" "false" "app-insights-connection-string.txt not found"
fi

# --- 4. VM connection files ---
VM_FILES_OK=true
for F in vm-ip vm-user vm-key; do
  if [[ ! -f "$CONFIG_DIR/$F" ]]; then
    VM_FILES_OK=false
  fi
done

if [[ "$VM_FILES_OK" == "true" ]]; then
  check "VM config files" "true" "vm-ip, vm-user, vm-key present"

  # --- 5. VM SSH connectivity + tools ---
  VM_IP=$(cat "$CONFIG_DIR/vm-ip")
  VM_USER=$(cat "$CONFIG_DIR/vm-user")
  VM_KEY=$(cat "$CONFIG_DIR/vm-key")

  SSH_OUTPUT=$(ssh -i "$VM_KEY" -o StrictHostKeyChecking=no -o ConnectTimeout=10 \
    "$VM_USER@$VM_IP" \
    'echo "SSH_OK"; java -version 2>&1 | head -1; /opt/apache-maven-3.9.12/bin/mvn --version 2>&1 | head -1' 2>/dev/null || echo "SSH_FAILED")

  if echo "$SSH_OUTPUT" | grep -q "SSH_OK"; then
    check "VM SSH" "true" "Reachable at $VM_IP"

    if echo "$SSH_OUTPUT" | grep -qi "openjdk"; then
      JDK_VER=$(echo "$SSH_OUTPUT" | grep -i "openjdk" | head -1)
      check "VM JDK" "true" "$JDK_VER"
    else
      check "VM JDK" "false" "JDK not found on VM"
    fi

    if echo "$SSH_OUTPUT" | grep -qi "maven"; then
      MVN_VER=$(echo "$SSH_OUTPUT" | grep -i "maven" | head -1)
      check "VM Maven" "true" "$MVN_VER"
    else
      check "VM Maven" "false" "Maven not found on VM"
    fi
  else
    check "VM SSH" "false" "Cannot reach $VM_IP"
  fi
else
  check "VM config files" "false" "Missing one or more of: vm-ip, vm-user, vm-key"
fi

# --- Output ---
echo ""
echo "=== Resource Verification ==="
echo "  Config: $CONFIG_DIR"
echo -e "$CHECKS"
echo ""

if [[ "$PASS" == "true" ]]; then
  echo "✅ All checks passed — ready to proceed to benchmark setup."
  exit 0
else
  echo "❌ Some checks failed — fix issues before proceeding."
  exit 1
fi
