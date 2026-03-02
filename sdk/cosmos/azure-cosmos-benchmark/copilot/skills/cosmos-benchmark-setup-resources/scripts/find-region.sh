#!/bin/bash
# find-region.sh — Find a region with capacity for all benchmark resources
#
# Usage:
#   ./find-region.sh [--preferred westus2] [--vm-size Standard_D16s_v5] [--vm-count 1] \
#                    [--cosmos-count 1] [--app-insights-count 1]
#
# Checks the preferred region first. If it fails, tries candidate regions.
# Prints the first valid region name to stdout. Exits 1 if none found.
#
# Output (stdout):
#   Line 1: region name (or "NONE" if no region found)
#   Line 2+: JSON capacity report for the selected region (or summary of failures)

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PREFERRED="westus2"
VM_SIZE="Standard_D16s_v5"
VM_COUNT=1
COSMOS_COUNT=1
APP_INSIGHTS_COUNT=1

while [[ $# -gt 0 ]]; do
  case $1 in
    --preferred)          PREFERRED="$2"; shift 2 ;;
    --vm-size)            VM_SIZE="$2"; shift 2 ;;
    --vm-count)           VM_COUNT="$2"; shift 2 ;;
    --cosmos-count)       COSMOS_COUNT="$2"; shift 2 ;;
    --app-insights-count) APP_INSIGHTS_COUNT="$2"; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

CANDIDATES=("westus2" "eastus" "eastus2" "westus3" "centralus" "northeurope" "westeurope" "southeastasia" "australiaeast")

validate_region() {
  local region="$1"
  bash "$SCRIPT_DIR/validate-capacity.sh" \
    --region "$region" \
    --vm-size "$VM_SIZE" \
    --vm-count "$VM_COUNT" \
    --cosmos-count "$COSMOS_COUNT" \
    --app-insights-count "$APP_INSIGHTS_COUNT" 2>/dev/null
}

# Try preferred region first
echo "Checking preferred region: $PREFERRED..." >&2
RESULT=$(validate_region "$PREFERRED")
if [[ $? -eq 0 ]]; then
  echo "$PREFERRED"
  echo "$RESULT"
  exit 0
fi

echo "Preferred region $PREFERRED failed capacity checks." >&2
echo "$RESULT" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  for k in ['vm_sku', 'vm_quota', 'cosmos_db', 'app_insights']:
    if not d[k]['passed']:
      print(f\"  ❌ {k}: {d[k]['message']}\", file=sys.stderr)
except: pass
" 2>&2 || true

echo "" >&2
echo "Searching candidate regions..." >&2

FAILURES=""
for REGION in "${CANDIDATES[@]}"; do
  [[ "$REGION" == "$PREFERRED" ]] && continue
  echo "  Checking $REGION..." >&2
  RESULT=$(validate_region "$REGION")
  if [[ $? -eq 0 ]]; then
    echo "$REGION"
    echo "$RESULT"
    exit 0
  fi
  FAILURES="$FAILURES\n$REGION: $(echo "$RESULT" | python3 -c "
import sys, json
try:
  d = json.load(sys.stdin)
  fails = [f\"{k}: {d[k]['message']}\" for k in ['vm_sku','vm_quota','cosmos_db','app_insights'] if not d[k]['passed']]
  print('; '.join(fails))
except: print('parse error')
" 2>/dev/null || echo "check error")"
done

echo "" >&2
echo "No candidate region has capacity for all resources." >&2
echo -e "Failures:$FAILURES" >&2
echo "NONE"
exit 1
