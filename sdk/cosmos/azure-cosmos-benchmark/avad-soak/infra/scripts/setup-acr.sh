#!/bin/bash
# =============================================================================
# ACR Setup + Image Build/Push
# =============================================================================
# Builds the benchmark module locally (fat jar), then pushes a minimal
# runtime image to ACR. No multi-stage Docker build needed — avoids
# dependency resolution issues with internal SDK modules.
#
# Usage:
#   ./setup-acr.sh
#   IMAGE_TAG=v2 ./setup-acr.sh
# =============================================================================

set -euo pipefail

SUBSCRIPTION="${SUBSCRIPTION:-b31b6408-0fb5-4688-9a3c-33ffb3983297}"
RG="${RG:-abhm-rg}"
ACR_NAME="${ACR_NAME:-abhmavadsoakacr}"
IMAGE_NAME="${IMAGE_NAME:-cosmos-avad-test}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/../../.."

az account set --subscription "$SUBSCRIPTION"

echo "=== ACR Setup ==="

# Create ACR (if not exists)
az acr create \
    --resource-group "$RG" \
    --name "$ACR_NAME" \
    --sku Basic \
    --output none 2>/dev/null || echo "ACR already exists"

# Attach ACR to AKS (if AKS exists)
AKS_CLUSTER="${AKS_CLUSTER:-abhm-avad-soak-aks}"
az aks update \
    --resource-group "$RG" \
    --name "$AKS_CLUSTER" \
    --attach-acr "$ACR_NAME" \
    --output none 2>/dev/null || echo "AKS-ACR attachment skipped"

echo "=== Building module locally ==="

cd "$PROJECT_DIR"
mvn package -DskipTests -DskipCheckstyle -Dspotbugs.skip=true -Drevapi.skip=true -B -q

# Verify the fat jar exists
FAT_JAR=$(ls target/azure-cosmos-benchmark-*-jar-with-dependencies.jar 2>/dev/null | head -1)
if [ -z "$FAT_JAR" ]; then
    echo "ERROR: Fat jar not found. Check maven-shade/assembly plugin config."
    exit 1
fi
echo "Fat jar: $FAT_JAR"

echo "=== Pushing image to ACR ==="

# Build and push using ACR Tasks — context is the module root (has target/ + Dockerfile path)
az acr build \
    --registry "$ACR_NAME" \
    --image "${IMAGE_NAME}:${IMAGE_TAG}" \
    --file "$PROJECT_DIR/avad-soak/Dockerfile" \
    "$PROJECT_DIR"

echo "Image pushed: ${ACR_NAME}.azurecr.io/${IMAGE_NAME}:${IMAGE_TAG}"
