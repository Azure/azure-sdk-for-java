#!/bin/bash
# provision-all.sh — Orchestrate parallel creation of all benchmark resources
#
# Usage:
#   ./provision-all.sh --config-dir <path> --region <region> --rg <resource-group> \
#     [--cosmos-prefix <prefix>] [--cosmos-count <N>] [--cosmos-consistency Session] \
#     [--app-insights-name <name>] \
#     [--vm-name <name>] [--vm-size Standard_D16s_v5] [--vm-disk-size 256] \
#     [--create-key] [--skip-vm-setup] [--skip-capacity-check]
#
# Creates resource group, then launches Cosmos DB, App Insights, and VM creation
# in parallel. Waits for all to complete. Exports credentials. Runs verify-resources.sh.
#
# Exit codes:
#   0 — all resources provisioned and verified
#   1 — one or more resources failed

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

log() {
  printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

# Required
CONFIG_DIR=""
REGION="westus2"
RG="rg-cosmos-benchmark-$(date +%Y%m%d)"

# Cosmos DB
COSMOS_PREFIX="cosmos-bench-"
COSMOS_COUNT=1
COSMOS_CONSISTENCY="Session"

# App Insights
APP_INSIGHTS_NAME="cosmos-bench-ai"

# VM
VM_NAME="vm-benchmark-01"
VM_SIZE="Standard_D16s_v5"
VM_DISK_SIZE=256
CREATE_KEY=true
SKIP_VM_SETUP=false
SKIP_CAPACITY_CHECK=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)          CONFIG_DIR="$2"; shift 2 ;;
    --region)              REGION="$2"; shift 2 ;;
    --rg)                  RG="$2"; shift 2 ;;
    --cosmos-prefix)       COSMOS_PREFIX="$2"; shift 2 ;;
    --cosmos-count)        COSMOS_COUNT="$2"; shift 2 ;;
    --cosmos-consistency)  COSMOS_CONSISTENCY="$2"; shift 2 ;;
    --app-insights-name)   APP_INSIGHTS_NAME="$2"; shift 2 ;;
    --vm-name)             VM_NAME="$2"; shift 2 ;;
    --vm-size)             VM_SIZE="$2"; shift 2 ;;
    --vm-disk-size)        VM_DISK_SIZE="$2"; shift 2 ;;
    --create-key)          CREATE_KEY=true; shift ;;
    --skip-vm-setup)       SKIP_VM_SETUP=true; shift ;;
    --skip-capacity-check) SKIP_CAPACITY_CHECK=true; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" ]]; then
  echo "ERROR: --config-dir <path> is required" >&2
  exit 1
fi

mkdir -p "$CONFIG_DIR"
LOG_DIR="$CONFIG_DIR/logs"
mkdir -p "$LOG_DIR"

PROVISION_START=$(date +%s)

echo "============================================="
echo "  Provisioning Benchmark Resources"
echo "============================================="
echo "  Config dir:     $CONFIG_DIR"
echo "  Region:         $REGION"
echo "  Resource group: $RG"
echo "  Cosmos DB:      $COSMOS_COUNT account(s), prefix=$COSMOS_PREFIX"
echo "  App Insights:   $APP_INSIGHTS_NAME"
echo "  VM:             $VM_NAME ($VM_SIZE)"
echo "============================================="
echo ""

# --- Step 1: Pre-flight capacity validation ---
log "[1/5] Pre-flight capacity check: validating $REGION for $VM_SIZE..."
bash "$SCRIPT_DIR/validate-capacity.sh" \
  --region "$REGION" --vm-size "$VM_SIZE" --vm-count 1 \
  --cosmos-count "$COSMOS_COUNT" --app-insights-count 1 \
  > "$LOG_DIR/capacity-check.json" 2>>"$LOG_DIR/capacity-check.log"

CAPACITY_ALL_PASSED=$(python3 -c "
import json
data = json.load(open('$LOG_DIR/capacity-check.json'))
print('true' if data.get('passed', False) else 'false')
" 2>/dev/null || echo "false")

if [[ "$CAPACITY_ALL_PASSED" == "true" ]]; then
  log "  ✅ All capacity checks passed for $REGION"
else
  log "  ⚠️  Capacity check failures detected:"
  python3 -c "
import json
data = json.load(open('$LOG_DIR/capacity-check.json'))
for key in ['vm_sku', 'vm_quota', 'cosmos_db', 'app_insights']:
    section = data.get(key, {})
    if not section.get('passed', False):
        print(f'    - [{key}] {section.get("message", "no details")}')
" 2>/dev/null || log "    - (could not parse capacity-check.json)"
  if [[ "$SKIP_CAPACITY_CHECK" == "true" ]]; then
    log "  ⚠️  --skip-capacity-check set, continuing despite failures."
  else
    log "❌ Capacity validation failed. Resources will NOT be created."
    log "   Use --skip-capacity-check to override this gate."
    exit 1
  fi
fi
log ""

# --- Step 2: Create resource group (must complete before parallel creation) ---
log "[2/5] Creating resource group: $RG in $REGION"
az group create --name "$RG" --location "$REGION" -o none 2>&1 | tee "$LOG_DIR/rg.log"
log ""

# --- Step 3: Launch parallel resource creation ---
log "[3/5] Creating resources in parallel..."
log ""

COSMOS_PID=""
AI_PID=""
VM_PID=""

# Cosmos DB accounts (background)
log "  Starting: Cosmos DB ($COSMOS_COUNT accounts)..."
(
  bash "$SCRIPT_DIR/create-cosmos-accounts.sh" \
    --rg "$RG" --prefix "$COSMOS_PREFIX" --count "$COSMOS_COUNT" \
    --region "$REGION" --consistency "$COSMOS_CONSISTENCY" \
    --log "$LOG_DIR/cosmos-accounts.log" \
  && echo "COSMOS_SUCCESS" > "$LOG_DIR/cosmos-status" \
  || echo "COSMOS_FAILED" > "$LOG_DIR/cosmos-status"
) &
COSMOS_PID=$!

# App Insights (background)
log "  Starting: Application Insights ($APP_INSIGHTS_NAME)..."
(
  az monitor app-insights component create \
    --app "$APP_INSIGHTS_NAME" \
    --location "$REGION" \
    --resource-group "$RG" \
    --kind web --application-type web \
    -o none 2>&1 | tee "$LOG_DIR/app-insights.log"

  az monitor app-insights component show \
    --app "$APP_INSIGHTS_NAME" \
    --resource-group "$RG" \
    --query connectionString -o tsv > "$CONFIG_DIR/app-insights-connection-string.txt" 2>>"$LOG_DIR/app-insights.log"

  if [[ -s "$CONFIG_DIR/app-insights-connection-string.txt" ]]; then
    echo "AI_SUCCESS" > "$LOG_DIR/ai-status"
  else
    echo "AI_FAILED" > "$LOG_DIR/ai-status"
  fi
) &
AI_PID=$!

# VM (background)
log "  Starting: VM ($VM_NAME)..."
VM_EXTRA_FLAGS=""
[[ "$CREATE_KEY" == "true" ]] && VM_EXTRA_FLAGS="$VM_EXTRA_FLAGS --create-key"
[[ "$SKIP_VM_SETUP" == "true" ]] && VM_EXTRA_FLAGS="$VM_EXTRA_FLAGS --skip-setup"
(
  bash "$SCRIPT_DIR/provision-benchmark-vm.sh" \
    --new --location "$REGION" \
    --rg "$RG" --vm-name "$VM_NAME" \
    --size "$VM_SIZE" --disk-size "$VM_DISK_SIZE" \
    --config-dir "$CONFIG_DIR" \
    $VM_EXTRA_FLAGS \
    2>&1 | tee "$LOG_DIR/vm.log"

  if [[ -f "$CONFIG_DIR/vm-ip" ]]; then
    echo "VM_SUCCESS" > "$LOG_DIR/vm-status"
  else
    echo "VM_FAILED" > "$LOG_DIR/vm-status"
  fi
) &
VM_PID=$!

log ""
log "  PIDs: Cosmos=$COSMOS_PID, AppInsights=$AI_PID, VM=$VM_PID"
log ""

# --- Step 4: Wait for all to complete ---
log "[4/5] Waiting for all resources to complete..."
log ""

START_WAIT=$(date +%s)
OVERALL_OK=true

wait $COSMOS_PID
COSMOS_EXIT=$?
COSMOS_STATUS=$(cat "$LOG_DIR/cosmos-status" 2>/dev/null || echo "COSMOS_UNKNOWN")
if [[ "$COSMOS_STATUS" == "COSMOS_SUCCESS" ]]; then
  log "  ✅ Cosmos DB: $COSMOS_COUNT account(s) created ($(( $(date +%s) - START_WAIT ))s elapsed)"
else
  log "  ❌ Cosmos DB: creation failed (exit=$COSMOS_EXIT). See $LOG_DIR/cosmos-accounts.log"
  OVERALL_OK=false
fi

wait $AI_PID
AI_EXIT=$?
AI_STATUS=$(cat "$LOG_DIR/ai-status" 2>/dev/null || echo "AI_UNKNOWN")
if [[ "$AI_STATUS" == "AI_SUCCESS" ]]; then
  log "  ✅ App Insights: $APP_INSIGHTS_NAME created ($(( $(date +%s) - START_WAIT ))s elapsed)"
else
  log "  ❌ App Insights: creation failed (exit=$AI_EXIT). See $LOG_DIR/app-insights.log"
  OVERALL_OK=false
fi

wait $VM_PID
VM_EXIT=$?
VM_STATUS=$(cat "$LOG_DIR/vm-status" 2>/dev/null || echo "VM_UNKNOWN")
if [[ "$VM_STATUS" == "VM_SUCCESS" ]]; then
  log "  ✅ VM: $VM_NAME created ($(( $(date +%s) - START_WAIT ))s elapsed)"
else
  log "  ❌ VM: creation failed (exit=$VM_EXIT). See $LOG_DIR/vm.log"
  OVERALL_OK=false
fi

log ""

if [[ "$OVERALL_OK" == "false" ]]; then
  log "❌ One or more resources failed to create. Check logs in $LOG_DIR/"
  exit 1
fi

# --- Step 4b: Export Cosmos DB credentials (must happen after accounts are created) ---
log "  Exporting Cosmos DB credentials..."
bash "$SCRIPT_DIR/export-cosmos-credentials.sh" \
  --rg "$RG" --prefix "$COSMOS_PREFIX" --count "$COSMOS_COUNT" \
  --config-dir "$CONFIG_DIR" 2>&1 | tee "$LOG_DIR/export-credentials.log"

if [[ ! -s "$CONFIG_DIR/clientHostAndKey.txt" ]]; then
  log "  ❌ Credential export failed. See $LOG_DIR/export-credentials.log"
  exit 1
fi
log ""

# --- Step 5: Verify all resources ---
log "[5/5] Verifying all resources..."
log ""
bash "$SCRIPT_DIR/verify-resources.sh" \
  --config-dir "$CONFIG_DIR" \
  --cosmos-count "$COSMOS_COUNT"

VERIFY_EXIT=$?
log "Total provisioning time: $(( $(date +%s) - PROVISION_START ))s"
exit $VERIFY_EXIT
