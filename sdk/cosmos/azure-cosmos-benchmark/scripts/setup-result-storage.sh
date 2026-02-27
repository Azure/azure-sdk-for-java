#!/bin/bash
# setup-result-storage.sh — Create Cosmos DB containers for benchmark results
# See §9.2.3 of the test plan.

set -euo pipefail

RESOURCE_GROUP="${1:-rg-cosmos-benchmark}"
ACCOUNT_NAME="${2:-cosmos-bench-results}"
LOCATION="${3:-eastus}"

echo "=== Creating result storage ==="
echo "  RG:       $RESOURCE_GROUP"
echo "  Account:  $ACCOUNT_NAME"
echo "  Location: $LOCATION"

az cosmosdb create --name "$ACCOUNT_NAME" --resource-group "$RESOURCE_GROUP" \
  --default-consistency-level Session --locations regionName="$LOCATION" failoverPriority=0

az cosmosdb sql database create --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" --name benchresults

az cosmosdb sql container create --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" --database-name benchresults \
  --name runs --partition-key-path /scenario --throughput 400

az cosmosdb sql container create --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" --database-name benchresults \
  --name snapshots --partition-key-path /testRunId --throughput 400 --default-ttl 2592000

echo ""
echo "=== Result storage ready ==="
echo "  export RESULT_COSMOS_ENDPOINT=$(az cosmosdb show -n $ACCOUNT_NAME -g $RESOURCE_GROUP --query documentEndpoint -o tsv)"
echo "  export RESULT_COSMOS_KEY=$(az cosmosdb keys list -n $ACCOUNT_NAME -g $RESOURCE_GROUP --query primaryMasterKey -o tsv)"
