#!/bin/bash
# find-region.sh — Find a region with capacity for all benchmark resources
#
# Usage:
#   ./find-region.sh [--preferred westus2] [--vm-size Standard_D16s_v5] [--vm-count 1] \
#                    [--cosmos-count 1] [--app-insights-count 1] \
#                    [--fallback-regions "eastus,centralus,westeurope"] \
#                    [--stop-on-first true|false]
#
# Search order:
#   1. Preferred region with exact SKU
#   2. Preferred region with similar SKUs (--find-alternatives)
#   3. Fallback regions with exact SKU
#   4. Fallback regions with similar SKUs
#
# Output (stdout):
#   Line 1: region name (or "NONE" if no region found)
#   Line 2: VM size (may differ from requested if using an alternative SKU)
#   Line 3+: JSON capacity report for the selected region
#
# stderr: progress/status messages with running tally

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PREFERRED="westus2"
VM_SIZE="Standard_D16s_v5"
VM_COUNT=1
COSMOS_COUNT=1
APP_INSIGHTS_COUNT=1
FALLBACK_REGIONS=""
STOP_ON_FIRST=true

while [[ $# -gt 0 ]]; do
  case $1 in
    --preferred)          PREFERRED="$2"; shift 2 ;;
    --vm-size)            VM_SIZE="$2"; shift 2 ;;
    --vm-count)           VM_COUNT="$2"; shift 2 ;;
    --cosmos-count)       COSMOS_COUNT="$2"; shift 2 ;;
    --app-insights-count) APP_INSIGHTS_COUNT="$2"; shift 2 ;;
    --fallback-regions)   FALLBACK_REGIONS="$2"; shift 2 ;;
    --stop-on-first)      STOP_ON_FIRST="$2"; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

# Build candidate list: user-supplied fallback regions replace the defaults
DEFAULT_CANDIDATES=("westus2" "eastus" "eastus2" "westus3" "centralus" "northeurope" "westeurope" "southeastasia" "australiaeast")
if [[ -n "$FALLBACK_REGIONS" ]]; then
  IFS=',' read -ra CANDIDATES <<< "$FALLBACK_REGIONS"
else
  CANDIDATES=("${DEFAULT_CANDIDATES[@]}")
fi

# Remove preferred region from candidates (it's checked first separately)
FALLBACK=()
for r in "${CANDIDATES[@]}"; do
  [[ "$r" == "$PREFERRED" ]] || FALLBACK+=("$r")
done

# Total regions to check for progress tally (preferred + fallbacks)
TOTAL=$(( 1 + ${#FALLBACK[@]} ))
CHECK_NUM=0

# Emit a progress line to stderr, flushed immediately
progress() {
  CHECK_NUM=$(( CHECK_NUM + 1 ))
  printf "[%d/%d] %s\n" "$CHECK_NUM" "$TOTAL" "$1" >&2
}

validate_region() {
  local region="$1"
  local find_alt="${2:-false}"
  local extra_args=()
  if [[ "$find_alt" == "true" ]]; then
    extra_args+=("--find-alternatives" "true")
  fi
  bash "$SCRIPT_DIR/validate-capacity.sh" \
    --region "$region" \
    --vm-size "$VM_SIZE" \
    --vm-count "$VM_COUNT" \
    --cosmos-count "$COSMOS_COUNT" \
    --app-insights-count "$APP_INSIGHTS_COUNT" \
    "${extra_args[@]}" 2>/dev/null
}

# Extract a short status description from validate-capacity JSON output
status_summary() {
  local json="$1"
  local find_alt="$2"
  echo "$json" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  sku = d.get('vm_sku', {})
  if sku.get('passed'):
    print('available')
  else:
    msg = sku.get('message', 'failed')
    alt_name = d.get('vm_sku', {}).get('suggested_alternative', '')
    if alt_name:
      print(f\"{msg} (similar: {alt_name} available)\")
    else:
      print(msg)
except:
  print('check error')
" 2>/dev/null || echo "check error"
}

# Extract alternative SKU name from JSON if present
extract_alternative_sku() {
  local json="$1"
  echo "$json" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  print(d.get('vm_sku', {}).get('suggested_alternative', ''))
except:
  print('')
" 2>/dev/null || echo ""
}

# Track the best result found so far
BEST_REGION=""
BEST_VM_SIZE=""
BEST_RESULT=""
BEST_TYPE=""  # "exact" or "similar"

emit_result() {
  echo "$BEST_REGION"
  echo "$BEST_VM_SIZE"
  echo "$BEST_RESULT"
}

# ---------------------------------------------------------------------------
# Phase 1: Preferred region — exact SKU
# ---------------------------------------------------------------------------
RESULT=$(validate_region "$PREFERRED" false)
RC=$?
SUMMARY=$(status_summary "$RESULT" false)
if [[ $RC -eq 0 ]]; then
  progress "$PREFERRED: ✅ $VM_SIZE $SUMMARY"
  BEST_REGION="$PREFERRED"
  BEST_VM_SIZE="$VM_SIZE"
  BEST_RESULT="$RESULT"
  BEST_TYPE="exact"
  if [[ "$STOP_ON_FIRST" == "true" ]]; then
    echo "Found exact SKU match in preferred region." >&2
    emit_result
    exit 0
  fi
else
  # Phase 2: Preferred region — similar SKUs
  ALT_RESULT=$(validate_region "$PREFERRED" true)
  ALT_SUMMARY=$(status_summary "$ALT_RESULT" true)
  ALT_SKU=$(extract_alternative_sku "$ALT_RESULT")
  OTHER_CHECKS_OK=$(echo "$ALT_RESULT" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  cosmos_ok = d.get('cosmos_db', {}).get('passed', False)
  ai_ok = d.get('app_insights', {}).get('passed', False)
  quota_ok = d.get('vm_quota', {}).get('passed', False)
  print('true' if (cosmos_ok and ai_ok and quota_ok) else 'false')
except: print('false')
" 2>/dev/null || echo "false")
  if [[ -n "$ALT_SKU" && "$OTHER_CHECKS_OK" == "true" ]]; then
    progress "$PREFERRED: ❌ $VM_SIZE $SUMMARY (similar: $ALT_SKU available)"
    echo "Preferred region has alternative SKU $ALT_SKU; checking fallbacks for exact match..." >&2
    BEST_REGION="$PREFERRED"
    BEST_VM_SIZE="$ALT_SKU"
    BEST_RESULT="$ALT_RESULT"
    BEST_TYPE="similar"
  else
    progress "$PREFERRED: ❌ $VM_SIZE $SUMMARY"
  fi
fi

# ---------------------------------------------------------------------------
# Phase 3: Fallback regions — exact SKU
# ---------------------------------------------------------------------------
for REGION in "${FALLBACK[@]}"; do
  RESULT=$(validate_region "$REGION" false)
  RC=$?
  SUMMARY=$(status_summary "$RESULT" false)
  if [[ $RC -eq 0 ]]; then
    progress "$REGION: ✅ $VM_SIZE $SUMMARY"
    # Exact match in a fallback region — always preferred over a similar SKU
    if [[ "$BEST_TYPE" != "exact" ]]; then
      BEST_REGION="$REGION"
      BEST_VM_SIZE="$VM_SIZE"
      BEST_RESULT="$RESULT"
      BEST_TYPE="exact"
    fi
    if [[ "$STOP_ON_FIRST" == "true" ]]; then
      echo "Found exact SKU match in $REGION." >&2
      emit_result
      exit 0
    fi
  else
    progress "$REGION: ❌ $VM_SIZE $SUMMARY"
  fi
done

# If we already have an exact match (stop-on-first=false mode), return it
if [[ "$BEST_TYPE" == "exact" ]]; then
  echo "Best option: exact SKU in $BEST_REGION." >&2
  emit_result
  exit 0
fi

# ---------------------------------------------------------------------------
# Phase 4: Fallback regions — similar SKUs
# ---------------------------------------------------------------------------
echo "" >&2
echo "No exact SKU match found. Searching fallback regions for similar SKUs..." >&2
for REGION in "${FALLBACK[@]}"; do
  ALT_RESULT=$(validate_region "$REGION" true)
  ALT_SKU=$(extract_alternative_sku "$ALT_RESULT")
  OTHER_CHECKS_OK=$(echo "$ALT_RESULT" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  cosmos_ok = d.get('cosmos_db', {}).get('passed', False)
  ai_ok = d.get('app_insights', {}).get('passed', False)
  quota_ok = d.get('vm_quota', {}).get('passed', False)
  print('true' if (cosmos_ok and ai_ok and quota_ok) else 'false')
except: print('false')
" 2>/dev/null || echo "false")
  if [[ -n "$ALT_SKU" && "$OTHER_CHECKS_OK" == "true" ]]; then
    echo "  $REGION: similar SKU $ALT_SKU available" >&2
    # Keep the first similar-SKU fallback found, unless preferred already has one
    if [[ -z "$BEST_REGION" ]]; then
      BEST_REGION="$REGION"
      BEST_VM_SIZE="$ALT_SKU"
      BEST_RESULT="$ALT_RESULT"
      BEST_TYPE="similar"
    fi
    if [[ "$STOP_ON_FIRST" == "true" ]]; then
      break
    fi
  else
    echo "  $REGION: no similar SKU found" >&2
  fi
done

# ---------------------------------------------------------------------------
# Emit final result
# ---------------------------------------------------------------------------
if [[ -n "$BEST_REGION" ]]; then
  if [[ "$BEST_VM_SIZE" != "$VM_SIZE" ]]; then
    echo "Selected $BEST_REGION with alternative SKU $BEST_VM_SIZE (requested $VM_SIZE)." >&2
  else
    echo "Selected $BEST_REGION with $BEST_VM_SIZE." >&2
  fi
  emit_result
  exit 0
fi

echo "" >&2
echo "No region found with capacity for requested or similar SKUs." >&2
echo "NONE"
echo "$VM_SIZE"
exit 1
