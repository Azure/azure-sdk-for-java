#!/bin/bash
# =============================================================================
# AKS Cluster Setup for Cosmos DB Soak Testing
# =============================================================================
# Creates an AKS cluster in the abhm-rg resource group.
# Reusable: change variables for your own resource group/subscription.
# =============================================================================

set -euo pipefail

SUBSCRIPTION="${SUBSCRIPTION:-b31b6408-0fb5-4688-9a3c-33ffb3983297}"
RG="${RG:-abhm-rg}"
LOCATION="${LOCATION:-eastus}"
CLUSTER_NAME="${CLUSTER_NAME:-abhm-avad-soak-aks}"
NODE_COUNT="${NODE_COUNT:-3}"
NODE_VM_SIZE="${NODE_VM_SIZE:-Standard_D4s_v5}"
K8S_VERSION="${K8S_VERSION:-1.29}"

echo "=== Setting up AKS cluster ==="
echo "  Subscription: $SUBSCRIPTION"
echo "  Resource Group: $RG"
echo "  Cluster: $CLUSTER_NAME"
echo "  Nodes: $NODE_COUNT x $NODE_VM_SIZE"

az account set --subscription "$SUBSCRIPTION"

# Create cluster
az aks create \
    --resource-group "$RG" \
    --name "$CLUSTER_NAME" \
    --location "$LOCATION" \
    --node-count "$NODE_COUNT" \
    --node-vm-size "$NODE_VM_SIZE" \
    --kubernetes-version "$K8S_VERSION" \
    --enable-managed-identity \
    --enable-addons monitoring \
    --generate-ssh-keys \
    --output none

echo "AKS cluster created"

# Get credentials
az aks get-credentials \
    --resource-group "$RG" \
    --name "$CLUSTER_NAME" \
    --overwrite-existing

echo "kubectl context set to $CLUSTER_NAME"
kubectl get nodes
