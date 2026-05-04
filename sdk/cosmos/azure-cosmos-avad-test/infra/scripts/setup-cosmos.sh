#!/bin/bash
# =============================================================================
# Cosmos DB Container Setup for Soak Testing
# =============================================================================
# Creates required containers in an existing Cosmos account.
# Idempotent — safe to run multiple times.
# =============================================================================

set -euo pipefail

COSMOS_ACCOUNT="${COSMOS_ACCOUNT:-abhm-cfp-region-test}"
COSMOS_RG="${COSMOS_RG:-abhm-rg}"
COSMOS_DB="${COSMOS_DB:-graph_db}"
SUBSCRIPTION="${SUBSCRIPTION:-b31b6408-0fb5-4688-9a3c-33ffb3983297}"

az account set --subscription "$SUBSCRIPTION"

echo "=== Setting up Cosmos DB containers ==="

# Feed container (AVAD-enabled, /tenantId PK)
echo "Creating feed container: avad-test"
az cosmosdb sql container create \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "avad-test" \
    --partition-key-path "/tenantId" \
    --throughput 10000 \
    --output none 2>/dev/null || echo "  already exists"

# Lease container (/id PK)
echo "Creating lease container: avad-test-leases"
az cosmosdb sql container create \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "avad-test-leases" \
    --partition-key-path "/id" \
    --throughput 1000 \
    --output none 2>/dev/null || echo "  already exists"

# Reconciliation container (/correlationId PK, TTL 24h)
echo "Creating reconciliation container"
az cosmosdb sql container create \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "reconciliation" \
    --partition-key-path "/correlationId" \
    --throughput 5000 \
    --ttl 86400 \
    --output none 2>/dev/null || echo "  already exists"

# Soak health container (/runId PK, TTL 30 days)
echo "Creating soak-health container"
az cosmosdb sql container create \
    --account-name "$COSMOS_ACCOUNT" \
    --resource-group "$COSMOS_RG" \
    --database-name "$COSMOS_DB" \
    --name "soak-health" \
    --partition-key-path "/runId" \
    --throughput 400 \
    --ttl 2592000 \
    --output none 2>/dev/null || echo "  already exists"

echo "=== All containers ready ==="
