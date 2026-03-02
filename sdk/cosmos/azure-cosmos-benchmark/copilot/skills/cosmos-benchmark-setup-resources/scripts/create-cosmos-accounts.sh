#!/bin/bash
# create-cosmos-accounts.sh — Create Cosmos DB accounts sequentially with status tracking
#
# Usage:
#   ./create-cosmos-accounts.sh --rg <resource-group> --prefix <name-prefix> --count <N> --region <region>
#
# Accounts are created one at a time. Progress is logged to /tmp/cosmos-account-creation.log.
# Run in background with: nohup ./create-cosmos-accounts.sh [args] &

set -uo pipefail

RG=""
PREFIX=""
COUNT=1
REGION="westus2"
CONSISTENCY="Session"
LOG="/tmp/cosmos-account-creation.log"

while [[ $# -gt 0 ]]; do
  case $1 in
    --rg) RG="$2"; shift 2 ;;
    --prefix) PREFIX="$2"; shift 2 ;;
    --count) COUNT="$2"; shift 2 ;;
    --region) REGION="$2"; shift 2 ;;
    --consistency) CONSISTENCY="$2"; shift 2 ;;
    --log) LOG="$2"; shift 2 ;;
    *) echo "Unknown flag: $1"; exit 1 ;;
  esac
done

if [[ -z "$RG" || -z "$PREFIX" ]]; then
  echo "Usage: $0 --rg <resource-group> --prefix <name-prefix> [--count N] [--region region]"
  exit 1
fi

echo "$(date): Starting creation of $COUNT Cosmos DB accounts" | tee "$LOG"
echo "  Resource group: $RG" | tee -a "$LOG"
echo "  Prefix: $PREFIX" | tee -a "$LOG"
echo "  Region: $REGION" | tee -a "$LOG"
echo "  Consistency: $CONSISTENCY" | tee -a "$LOG"
echo "" | tee -a "$LOG"

SUCCESS=0
FAILED=0

for i in $(seq 0 $((COUNT - 1))); do
  NAME="${PREFIX}${i}"
  echo "$(date): [$((i+1))/$COUNT] Creating ${NAME}..." | tee -a "$LOG"
  if az cosmosdb create \
    --resource-group "$RG" \
    --name "$NAME" \
    --locations regionName="$REGION" failoverPriority=0 \
    --default-consistency-level "$CONSISTENCY" \
    --kind GlobalDocumentDB 2>>"$LOG" 1>/dev/null; then
    echo "$(date): ✅ ${NAME} created" | tee -a "$LOG"
    SUCCESS=$((SUCCESS + 1))
  else
    echo "$(date): ❌ ${NAME} FAILED" | tee -a "$LOG"
    FAILED=$((FAILED + 1))
  fi
done

echo "" | tee -a "$LOG"
echo "$(date): Complete. Succeeded: $SUCCESS / $COUNT, Failed: $FAILED / $COUNT" | tee -a "$LOG"

if [[ $FAILED -gt 0 ]]; then
  exit 1
fi
