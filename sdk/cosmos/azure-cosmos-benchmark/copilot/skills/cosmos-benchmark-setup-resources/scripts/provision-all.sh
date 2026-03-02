#!/bin/bash
# provision-all.sh — Orchestrate parallel creation of all benchmark resources
#
# Usage:
#   ./provision-all.sh --config-dir <path> --region <region> --rg <resource-group> \
#     [--cosmos-prefix <prefix>] [--cosmos-count <N>] [--cosmos-consistency Session] \
#     [--app-insights-name <name>] \
#     [--vm-name <name>] [--vm-size Standard_D16s_v5] [--vm-disk-size 256] \
#     [--create-key] [--skip-vm-setup]
#
# Creates resource group, then launches Cosmos DB, App Insights, and VM creation
# in parallel. Waits for all to complete. Exports credentials. Runs verify-resources.sh.
#
# Exit codes:
#   0 — all resources provisioned and verified
#   1 — one or more resources failed

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

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

# --- Step 1: Create resource group (must complete before parallel creation) ---
echo "[1/4] Creating resource group: $RG in $REGION"
az group create --name "$RG" --location "$REGION" -o none 2>&1 | tee "$LOG_DIR/rg.log"
echo ""

# --- Step 2: Launch parallel resource creation ---
echo "[2/4] Creating resources in parallel..."
echo ""

COSMOS_PID=""
AI_PID=""
VM_PID=""

# Cosmos DB accounts (background)
echo "  Starting: Cosmos DB ($COSMOS_COUNT accounts)..."
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
echo "  Starting: Application Insights ($APP_INSIGHTS_NAME)..."
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
echo "  Starting: VM ($VM_NAME)..."
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

echo ""
echo "  PIDs: Cosmos=$COSMOS_PID, AppInsights=$AI_PID, VM=$VM_PID"
echo ""

# --- Step 3: Wait for all to complete ---
echo "[3/4] Waiting for all resources to complete..."
echo ""

OVERALL_OK=true

wait $COSMOS_PID
COSMOS_EXIT=$?
COSMOS_STATUS=$(cat "$LOG_DIR/cosmos-status" 2>/dev/null || echo "COSMOS_UNKNOWN")
if [[ "$COSMOS_STATUS" == "COSMOS_SUCCESS" ]]; then
  echo "  ✅ Cosmos DB: $COSMOS_COUNT account(s) created"
else
  echo "  ❌ Cosmos DB: creation failed (exit=$COSMOS_EXIT). See $LOG_DIR/cosmos-accounts.log"
  OVERALL_OK=false
fi

wait $AI_PID
AI_EXIT=$?
AI_STATUS=$(cat "$LOG_DIR/ai-status" 2>/dev/null || echo "AI_UNKNOWN")
if [[ "$AI_STATUS" == "AI_SUCCESS" ]]; then
  echo "  ✅ App Insights: $APP_INSIGHTS_NAME created"
else
  echo "  ❌ App Insights: creation failed (exit=$AI_EXIT). See $LOG_DIR/app-insights.log"
  OVERALL_OK=false
fi

wait $VM_PID
VM_EXIT=$?
VM_STATUS=$(cat "$LOG_DIR/vm-status" 2>/dev/null || echo "VM_UNKNOWN")
if [[ "$VM_STATUS" == "VM_SUCCESS" ]]; then
  echo "  ✅ VM: $VM_NAME created"
else
  echo "  ❌ VM: creation failed (exit=$VM_EXIT). See $LOG_DIR/vm.log"
  OVERALL_OK=false
fi

echo ""

if [[ "$OVERALL_OK" == "false" ]]; then
  echo "❌ One or more resources failed to create. Check logs in $LOG_DIR/"
  exit 1
fi

# --- Step 3b: Export Cosmos DB credentials (must happen after accounts are created) ---
echo "  Exporting Cosmos DB credentials..."
bash "$SCRIPT_DIR/export-cosmos-credentials.sh" \
  --rg "$RG" --prefix "$COSMOS_PREFIX" --count "$COSMOS_COUNT" \
  --config-dir "$CONFIG_DIR" 2>&1 | tee "$LOG_DIR/export-credentials.log"

if [[ ! -s "$CONFIG_DIR/clientHostAndKey.txt" ]]; then
  echo "  ❌ Credential export failed. See $LOG_DIR/export-credentials.log"
  exit 1
fi
echo ""

# --- Step 4: Verify all resources ---
echo "[4/4] Verifying all resources..."
echo ""
bash "$SCRIPT_DIR/verify-resources.sh" \
  --config-dir "$CONFIG_DIR" \
  --cosmos-count "$COSMOS_COUNT"

exit $?
