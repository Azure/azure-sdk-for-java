#!/bin/bash
# validate-capacity.sh — Check whether a region has capacity for benchmark resources
#
# Usage:
#   ./validate-capacity.sh --region <region> [--vm-size Standard_D16s_v5] [--vm-count 1] \
#                          [--cosmos-count 1] [--app-insights-count 1]
#
# Exit codes:
#   0 — all checks passed
#   1 — one or more checks failed
#
# Output: JSON summary to stdout with per-check results

set -uo pipefail

REGION="westus2"
VM_SIZE="Standard_D16s_v5"
VM_COUNT=1
COSMOS_COUNT=1
APP_INSIGHTS_COUNT=1

while [[ $# -gt 0 ]]; do
  case $1 in
    --region)             REGION="$2"; shift 2 ;;
    --vm-size)            VM_SIZE="$2"; shift 2 ;;
    --vm-count)           VM_COUNT="$2"; shift 2 ;;
    --cosmos-count)       COSMOS_COUNT="$2"; shift 2 ;;
    --app-insights-count) APP_INSIGHTS_COUNT="$2"; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

PASS=true
VM_SKU_OK=false
VM_QUOTA_OK=false
COSMOS_OK=false
APP_INSIGHTS_OK=false
VM_SKU_MSG=""
VM_QUOTA_MSG=""
COSMOS_MSG=""
APP_INSIGHTS_MSG=""
AVAILABLE_VCPUS=0
COSMOS_AVAILABLE=0
APP_INSIGHTS_AVAILABLE=0
SUGGESTED_SKU=""

# --- 1. Resource providers ---
for NS in Microsoft.Compute Microsoft.DocumentDB Microsoft.Insights; do
  STATE=$(az provider show --namespace "$NS" --query "registrationState" -o tsv 2>/dev/null || echo "Unknown")
  if [[ "$STATE" != "Registered" ]]; then
    echo "Registering $NS..." >&2
    az provider register --namespace "$NS" 2>/dev/null
  fi
done

# --- 2. VM SKU availability ---
# Check if the exact SKU is available (no restrictions)
RESTRICTED=$(az vm list-skus --location "$REGION" --size "$VM_SIZE" \
  --query "[?restrictions[?reasonCode=='NotAvailableForSubscription']] | length(@)" -o tsv 2>/dev/null || echo "0")
TOTAL_SKUS=$(az vm list-skus --location "$REGION" --size "$VM_SIZE" \
  --query "length(@)" -o tsv 2>/dev/null || echo "0")

if [[ "$TOTAL_SKUS" -gt 0 && "$RESTRICTED" -eq 0 ]]; then
  VM_SKU_OK=true
  VM_SKU_MSG="$VM_SIZE available"
else
  VM_SKU_MSG="$VM_SIZE not available"
  PASS=false
  # Try to find a similar SKU in the same family
  # Extract family prefix (e.g., Standard_D16s_v5 -> standardDSv5Family, also try v4)
  FAMILY_BASE=$(echo "$VM_SIZE" | sed -E 's/Standard_D([0-9]+)s_v[0-9]+/standardDSv/')
  VCPUS_NEEDED=$(echo "$VM_SIZE" | sed -E 's/Standard_D([0-9]+)s_v[0-9]+/\1/')
  for VER in 5 4 3; do
    FAMILY="${FAMILY_BASE}${VER}Family"
    ALT=$(az vm list-skus --location "$REGION" \
      --query "[?family=='$FAMILY' && restrictions[0]==null].name" -o tsv 2>/dev/null | head -5)
    if [[ -n "$ALT" ]]; then
      # Find a SKU with matching vCPU count
      for SKU in $ALT; do
        SKU_VCPUS=$(az vm list-skus --location "$REGION" --size "$SKU" \
          --query "[0].capabilities[?name=='vCPUs'].value | [0]" -o tsv 2>/dev/null || echo "0")
        if [[ "$SKU_VCPUS" == "$VCPUS_NEEDED" ]]; then
          SUGGESTED_SKU="$SKU"
          break 2
        fi
      done
    fi
  done
  if [[ -n "$SUGGESTED_SKU" ]]; then
    VM_SKU_MSG="$VM_SIZE not available; similar SKU: $SUGGESTED_SKU"
  fi
fi

# --- 3. VM vCPU quota ---
# Parse "Standard DSv5 Family vCPUs" usage line
VCPUS_PER_VM=$(echo "$VM_SIZE" | sed -E 's/Standard_D([0-9]+)s_v[0-9]+/\1/')
VCPUS_NEEDED=$((VCPUS_PER_VM * VM_COUNT))

USAGE_LINE=$(az vm list-usage --location "$REGION" -o tsv 2>/dev/null | grep -i "Standard DSv5 Family" || echo "")
if [[ -n "$USAGE_LINE" ]]; then
  CURRENT_USAGE=$(echo "$USAGE_LINE" | awk '{print $1}')
  LIMIT=$(echo "$USAGE_LINE" | awk '{print $2}')
  AVAILABLE_VCPUS=$((LIMIT - CURRENT_USAGE))
  if [[ $AVAILABLE_VCPUS -ge $VCPUS_NEEDED ]]; then
    VM_QUOTA_OK=true
    VM_QUOTA_MSG="$AVAILABLE_VCPUS vCPUs available (need $VCPUS_NEEDED)"
  else
    VM_QUOTA_MSG="Only $AVAILABLE_VCPUS vCPUs available (need $VCPUS_NEEDED)"
    PASS=false
  fi
else
  # No usage line found — could mean zero usage or family not available
  VM_QUOTA_OK=true
  VM_QUOTA_MSG="No existing usage found; quota assumed available"
  AVAILABLE_VCPUS=999
fi

# --- 4. Cosmos DB account quota ---
CURRENT_COSMOS=$(az cosmosdb list --query "length(@)" -o tsv 2>/dev/null || echo "0")
COSMOS_LIMIT=50
COSMOS_AVAILABLE=$((COSMOS_LIMIT - CURRENT_COSMOS))
if [[ $COSMOS_AVAILABLE -ge $COSMOS_COUNT ]]; then
  COSMOS_OK=true
  COSMOS_MSG="$COSMOS_AVAILABLE slots available (need $COSMOS_COUNT)"
else
  COSMOS_MSG="Only $COSMOS_AVAILABLE slots available (need $COSMOS_COUNT, limit $COSMOS_LIMIT)"
  PASS=false
fi

# --- 5. Application Insights quota ---
CURRENT_AI=$(az monitor app-insights component list \
  --query "[?location=='$REGION'] | length(@)" -o tsv 2>/dev/null || echo "0")
AI_LIMIT=200
AI_AVAILABLE=$((AI_LIMIT - CURRENT_AI))
if [[ $AI_AVAILABLE -ge $APP_INSIGHTS_COUNT ]]; then
  APP_INSIGHTS_OK=true
  APP_INSIGHTS_MSG="$AI_AVAILABLE slots available (need $APP_INSIGHTS_COUNT)"
else
  APP_INSIGHTS_MSG="Only $AI_AVAILABLE slots available (need $APP_INSIGHTS_COUNT, limit $AI_LIMIT)"
  PASS=false
fi

# --- Output JSON summary ---
cat <<EOF
{
  "region": "$REGION",
  "passed": $PASS,
  "vm_sku": {
    "passed": $VM_SKU_OK,
    "requested": "$VM_SIZE",
    "suggested_alternative": "$SUGGESTED_SKU",
    "message": "$VM_SKU_MSG"
  },
  "vm_quota": {
    "passed": $VM_QUOTA_OK,
    "available_vcpus": $AVAILABLE_VCPUS,
    "needed_vcpus": $VCPUS_NEEDED,
    "message": "$VM_QUOTA_MSG"
  },
  "cosmos_db": {
    "passed": $COSMOS_OK,
    "current_accounts": $CURRENT_COSMOS,
    "available_slots": $COSMOS_AVAILABLE,
    "needed": $COSMOS_COUNT,
    "message": "$COSMOS_MSG"
  },
  "app_insights": {
    "passed": $APP_INSIGHTS_OK,
    "available_slots": $AI_AVAILABLE,
    "needed": $APP_INSIGHTS_COUNT,
    "message": "$APP_INSIGHTS_MSG"
  }
}
EOF

if [[ "$PASS" == "true" ]]; then
  exit 0
else
  exit 1
fi
