#!/bin/bash
# validate-capacity.sh — Check whether a region has capacity for benchmark resources
#
# Usage:
#   ./validate-capacity.sh --region <region> [--vm-size Standard_D16s_v5] [--vm-count 1] \
#                          [--cosmos-count 1] [--app-insights-count 1] [--find-alternatives true|false]
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
FIND_ALTERNATIVES=true

log() {
  printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*" >&2
}

while [[ $# -gt 0 ]]; do
  case $1 in
    --region)             REGION="$2"; shift 2 ;;
    --vm-size)            VM_SIZE="$2"; shift 2 ;;
    --vm-count)           VM_COUNT="$2"; shift 2 ;;
    --cosmos-count)       COSMOS_COUNT="$2"; shift 2 ;;
    --app-insights-count) APP_INSIGHTS_COUNT="$2"; shift 2 ;;
    --find-alternatives)  FIND_ALTERNATIVES="$2"; shift 2 ;;
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
RESTRICTION_REASON=""

# --- 1. Resource providers ---
log "Checking resource provider registrations..."
for NS in Microsoft.Compute Microsoft.DocumentDB Microsoft.Insights; do
  STATE=$(az provider show --namespace "$NS" --query "registrationState" -o tsv 2>/dev/null || echo "Unknown")
  if [[ "$STATE" != "Registered" ]]; then
    log "Registering $NS..."
    az provider register --namespace "$NS" 2>/dev/null
  fi
done
log "Resource provider check complete."

# --- 2. VM SKU availability ---
log "Checking VM SKU availability in $REGION..."

# Fetch the requested SKU with its full restrictions array
SKU_JSON=$(az vm list-skus --location "$REGION" --size "$VM_SIZE" --resource-type virtualMachines \
  --query "[0].{name:name, restrictions:restrictions, family:family, caps:capabilities[?name=='vCPUs'].value | [0]}" \
  -o json 2>/dev/null || echo "null")

# A SKU is available only if it exists AND has no restrictions at all
SKU_EXISTS=false
SKU_UNRESTRICTED=false
if [[ "$SKU_JSON" != "null" && "$SKU_JSON" != "" ]]; then
  SKU_EXISTS=true
  RESTRICTION_COUNT=$(echo "$SKU_JSON" | python3 -c "
import sys, json
data = json.load(sys.stdin)
r = data.get('restrictions') or []
print(len(r))
" 2>/dev/null || echo "0")
  if [[ "$RESTRICTION_COUNT" -eq 0 ]]; then
    SKU_UNRESTRICTED=true
  else
    # Describe the restriction reasons
    RESTRICTION_REASON=$(echo "$SKU_JSON" | python3 -c "
import sys, json
data = json.load(sys.stdin)
reasons = []
for r in (data.get('restrictions') or []):
    rtype = r.get('type', 'Unknown')
    code = r.get('reasonCode', 'Unknown')
    reasons.append(f'{rtype}/{code}')
print('; '.join(reasons))
" 2>/dev/null || echo "Unknown")
  fi
fi

# Derive vCPU count from the SKU name (e.g., Standard_D16s_v5 -> 16)
VCPUS_PER_VM=$(echo "$VM_SIZE" | sed -E 's/Standard_D([0-9]+)[a-z]*_v[0-9]+/\1/')

if [[ "$SKU_EXISTS" == "true" && "$SKU_UNRESTRICTED" == "true" ]]; then
  VM_SKU_OK=true
  VM_SKU_MSG="$VM_SIZE available"
else
  if [[ "$SKU_EXISTS" == "false" ]]; then
    VM_SKU_MSG="$VM_SIZE not found in $REGION"
    RESTRICTION_REASON="NotFound"
  else
    VM_SKU_MSG="$VM_SIZE restricted ($RESTRICTION_REASON)"
  fi
  PASS=false

  # Try to find a similar D-series SKU with the same vCPU count
  if [[ "$FIND_ALTERNATIVES" == "true" ]]; then
    log "Searching for alternative D-series SKUs with $VCPUS_PER_VM vCPUs..."

    # Single API call: fetch all D-series SKUs with their vCPU count and restrictions
    ALL_D_SKUS=$(az vm list-skus --location "$REGION" --resource-type virtualMachines \
      --query "[?starts_with(name, 'Standard_D')].{name:name, vcpus:capabilities[?name=='vCPUs'].value | [0], family:family, restrictions:restrictions}" \
      -o json 2>/dev/null || echo "[]")

    # Use python3 to filter unrestricted SKUs with matching vCPUs and pick the best match
    SUGGESTED_SKU=$(echo "$ALL_D_SKUS" | python3 -c "
import sys, json

data = json.load(sys.stdin)
requested = '$VM_SIZE'
vcpus_needed = '$VCPUS_PER_VM'

# Extract generation from SKU name (e.g., Standard_D16s_v5 -> 5)
def get_generation(name):
    import re
    m = re.search(r'_v(\d+)$', name)
    return int(m.group(1)) if m else 0

req_gen = get_generation(requested)
candidates = []
for sku in data:
    name = sku.get('name', '')
    if name == requested:
        continue
    vcpus = sku.get('vcpus', '0')
    if str(vcpus) != str(vcpus_needed):
        continue
    restrictions = sku.get('restrictions') or []
    if len(restrictions) > 0:
        continue
    gen = get_generation(name)
    # Score: prefer same generation, then newer generations, then older
    if gen == req_gen:
        score = 0
    elif gen > req_gen:
        score = gen - req_gen
    else:
        score = 100 + (req_gen - gen)
    candidates.append((score, name))

candidates.sort()
if candidates:
    print(candidates[0][1])
else:
    print('')
" 2>/dev/null || echo "")

    if [[ -n "$SUGGESTED_SKU" ]]; then
      VM_SKU_MSG="$VM_SKU_MSG; similar SKU: $SUGGESTED_SKU"
      log "Found alternative: $SUGGESTED_SKU"
    else
      log "No alternative D-series SKU found with $VCPUS_PER_VM vCPUs."
    fi
  fi
fi
log "VM SKU check complete."

# --- 3. VM vCPU quota ---
# Determine which SKU to check quota for (use suggested alternative if primary failed)
EFFECTIVE_SKU="$VM_SIZE"
if [[ "$VM_SKU_OK" == "false" && -n "$SUGGESTED_SKU" ]]; then
  EFFECTIVE_SKU="$SUGGESTED_SKU"
fi

# Derive the quota family display name from the SKU
# e.g., Standard_D16s_v5 -> "Standard DSv5 Family", Standard_D16ds_v5 -> "Standard DDSv5 Family"
QUOTA_FAMILY=$(echo "$EFFECTIVE_SKU" | python3 -c "
import sys, re
sku = sys.stdin.read().strip()
m = re.match(r'Standard_D(\d+)([a-z]*)_v(\d+)', sku)
if m:
    suffix = m.group(2).upper()  # e.g., 's' -> 'S', 'ds' -> 'DS'
    ver = m.group(3)
    print(f'Standard D{suffix}v{ver} Family')
else:
    print('Standard DSv5 Family')
" 2>/dev/null || echo "Standard DSv5 Family")

VCPUS_NEEDED=$((VCPUS_PER_VM * VM_COUNT))

log "Checking vCPU quota for $QUOTA_FAMILY in $REGION..."
USAGE_LINE=$(az vm list-usage --location "$REGION" -o tsv 2>/dev/null | grep -i "$QUOTA_FAMILY" || echo "")
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
log "vCPU quota check complete."

# --- 4. Cosmos DB account quota ---
log "Checking Cosmos DB account quota..."
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
log "Cosmos DB check complete."

# --- 5. Application Insights quota ---
log "Checking Application Insights quota in $REGION..."
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
log "Application Insights check complete."

# --- Output JSON summary ---
cat <<EOF
{
  "region": "$REGION",
  "passed": $PASS,
  "vm_sku": {
    "passed": $VM_SKU_OK,
    "requested": "$VM_SIZE",
    "suggested_alternative": "$SUGGESTED_SKU",
    "restriction_reason": "$RESTRICTION_REASON",
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
