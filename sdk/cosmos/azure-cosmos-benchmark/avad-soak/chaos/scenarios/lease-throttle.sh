#!/bin/bash
# Lease Throttle — scale lease container RU to simulate throttling
set -euo pipefail

COSMOS_ACCOUNT="${COSMOS_ACCOUNT:?Set COSMOS_ACCOUNT}"
COSMOS_RG="${COSMOS_RG:?Set COSMOS_RG}"
COSMOS_DB="${COSMOS_DB:-graph_db}"
LEASE_CONTAINER="${LEASE_CONTAINER:-avad-test-leases}"
TARGET_RU="${TARGET_RU:-400}"
THROTTLE_DURATION="${THROTTLE_DURATION:-300}"  # 5 minutes

echo "[$(date '+%H:%M:%S')] Chaos: lease-throttle"

# Save current throughput
CURRENT_RU=$(az cosmosdb sql container throughput show \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "$LEASE_CONTAINER" \
    --query "resource.throughput" -o tsv 2>/dev/null || echo "autoscale")

echo "  Current lease RU: $CURRENT_RU"
echo "  Scaling to: $TARGET_RU RU for ${THROTTLE_DURATION}s"

# Scale down
az cosmosdb sql container throughput update \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "$LEASE_CONTAINER" \
    --throughput "$TARGET_RU" \
    --output none

echo "  Lease container throttled to $TARGET_RU RU"
sleep "$THROTTLE_DURATION"

# Restore
if [ "$CURRENT_RU" != "autoscale" ]; then
    echo "  Restoring lease RU to $CURRENT_RU"
    az cosmosdb sql container throughput update \
        --account-name "$COSMOS_ACCOUNT" \
        --resource-group "$COSMOS_RG" \
        --database-name "$COSMOS_DB" \
        --name "$LEASE_CONTAINER" \
        --throughput "$CURRENT_RU" \
        --output none
fi

echo "  Lease throttle complete"
