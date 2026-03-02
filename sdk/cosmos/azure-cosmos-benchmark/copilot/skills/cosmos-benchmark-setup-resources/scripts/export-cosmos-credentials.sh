#!/bin/bash
# export-cosmos-credentials.sh — Export Cosmos DB account credentials to clientHostAndKey.txt
#
# Usage:
#   ./export-cosmos-credentials.sh --rg <resource-group> --prefix <name-prefix> --count <N> --config-dir <path>
#   ./export-cosmos-credentials.sh --rg <resource-group> --discover --config-dir <path>
#
# Modes:
#   --prefix + --count: Export credentials for accounts named <prefix>0, <prefix>1, ...
#   --discover:         List all accounts in the resource group and export all

set -euo pipefail

RG=""
PREFIX=""
COUNT=0
CONFIG_DIR=""
DISCOVER=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --rg)         RG="$2"; shift 2 ;;
    --prefix)     PREFIX="$2"; shift 2 ;;
    --count)      COUNT="$2"; shift 2 ;;
    --config-dir) CONFIG_DIR="$2"; shift 2 ;;
    --discover)   DISCOVER=true; shift ;;
    *) echo "Unknown flag: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$RG" || -z "$CONFIG_DIR" ]]; then
  echo "Usage: $0 --rg <resource-group> --config-dir <path> (--prefix <prefix> --count <N> | --discover)" >&2
  exit 1
fi

mkdir -p "$CONFIG_DIR"
OUTPUT_FILE="$CONFIG_DIR/clientHostAndKey.txt"

if [[ "$DISCOVER" == "true" ]]; then
  echo "Discovering Cosmos DB accounts in resource group: $RG"
  ACCOUNTS=$(az cosmosdb list -g "$RG" --query "[].name" -o tsv 2>/dev/null)
  if [[ -z "$ACCOUNTS" ]]; then
    echo "No Cosmos DB accounts found in $RG" >&2
    exit 1
  fi
  EXPORTED=0
  > "$OUTPUT_FILE"
  for ACCT in $ACCOUNTS; do
    ENDPOINT=$(az cosmosdb show -g "$RG" -n "$ACCT" --query documentEndpoint -o tsv)
    KEY=$(az cosmosdb keys list -g "$RG" -n "$ACCT" --query primaryMasterKey -o tsv)
    echo "${ACCT},${ENDPOINT},${KEY}" >> "$OUTPUT_FILE"
    EXPORTED=$((EXPORTED + 1))
    echo "  Exported: $ACCT ($EXPORTED)"
  done
  echo "Exported $EXPORTED accounts to $OUTPUT_FILE"

elif [[ -n "$PREFIX" && "$COUNT" -gt 0 ]]; then
  echo "Exporting credentials for $COUNT accounts with prefix: $PREFIX"
  EXPORTED=0
  FAILED=0
  > "$OUTPUT_FILE"
  for i in $(seq 0 $((COUNT - 1))); do
    NAME="${PREFIX}${i}"
    ENDPOINT=$(az cosmosdb show -g "$RG" -n "$NAME" --query documentEndpoint -o tsv 2>/dev/null)
    if [[ -z "$ENDPOINT" ]]; then
      echo "  ❌ $NAME not found — skipping" >&2
      FAILED=$((FAILED + 1))
      continue
    fi
    KEY=$(az cosmosdb keys list -g "$RG" -n "$NAME" --query primaryMasterKey -o tsv)
    echo "${NAME},${ENDPOINT},${KEY}" >> "$OUTPUT_FILE"
    EXPORTED=$((EXPORTED + 1))
    echo "  ✅ $NAME ($EXPORTED/$COUNT)"
  done
  echo ""
  echo "Exported $EXPORTED/$COUNT accounts to $OUTPUT_FILE"
  [[ $FAILED -gt 0 ]] && echo "⚠️  $FAILED accounts not found" >&2

else
  echo "Provide --prefix + --count or --discover" >&2
  exit 1
fi
