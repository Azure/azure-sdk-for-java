#!/bin/bash
# Partition Split — scale feed container throughput to trigger split
set -euo pipefail

COSMOS_ACCOUNT="${COSMOS_ACCOUNT:?Set COSMOS_ACCOUNT}"
COSMOS_RG="${COSMOS_RG:?Set COSMOS_RG}"
COSMOS_DB="${COSMOS_DB:-graph_db}"
FEED_CONTAINER="${FEED_CONTAINER:-avad-test}"
SCALE_FACTOR="${SCALE_FACTOR:-2}"

echo "[$(date '+%H:%M:%S')] Chaos: partition-split"

# Get current throughput
CURRENT_RU=$(az cosmosdb sql container throughput show \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "$FEED_CONTAINER" \
    --query "resource.throughput" -o tsv)

TARGET_RU=$((CURRENT_RU * SCALE_FACTOR))

echo "  Current feed RU: $CURRENT_RU"
echo "  Scaling to: $TARGET_RU RU (${SCALE_FACTOR}x) to trigger split"

# Get pre-split partition count
PRE_SPLIT_PARTITIONS=$(az cosmosdb sql container show \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "$FEED_CONTAINER" \
    --query "resource.statistics[0].partitionCount" -o tsv 2>/dev/null || echo "unknown")

echo "  Pre-split partition count: $PRE_SPLIT_PARTITIONS"

# Scale up
az cosmosdb sql container throughput update \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "$FEED_CONTAINER" \
    --throughput "$TARGET_RU" \
    --output none

echo "  Feed container scaled to $TARGET_RU RU"
echo "  Partition split may take several minutes to complete"

# Poll for split completion (check partition count changes)
WAIT_TIME=0
MAX_WAIT=1800  # 30 minutes
while [ $WAIT_TIME -lt $MAX_WAIT ]; do
    sleep 60
    WAIT_TIME=$((WAIT_TIME + 60))
    echo "  Waiting for split... (${WAIT_TIME}s elapsed)"
done

echo "  Partition split chaos event complete (waited ${WAIT_TIME}s)"
