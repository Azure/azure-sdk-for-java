# Cosmos DB Soak Test — Infrastructure

Reusable Helm chart and setup scripts for running Cosmos DB
change feed processor soak tests on AKS.

## Prerequisites

Before deploying, create the required Kubernetes secrets:

```bash
# Cosmos DB key secret (referenced by Helm chart)
kubectl create secret generic <release-name>-secrets \
  --namespace cosmos-soak \
  --from-literal=cosmos-key="<your-cosmos-key>"

# ACR pull secret (if not using AKS-managed ACR attachment)
kubectl create secret docker-registry acr-secret \
  --namespace cosmos-soak \
  --docker-server=<acr-name>.azurecr.io \
  --docker-username=<sp-id> \
  --docker-password=<sp-password>
```

If using AKS with `--attach-acr`, the `acr-secret` is not needed
and can be removed from the chart templates.

## Quick Start

```bash
# 1. Create AKS cluster
./scripts/setup-aks.sh

# 2. Create Cosmos containers
./scripts/setup-cosmos.sh

# 3. Build + push image to ACR
./scripts/setup-acr.sh

# 4. Create secrets (see Prerequisites above)

# 5. Deploy (from repo root)
cd ../..
./run-soak.sh
```

## What This Provides

| Component | Description |
|-----------|-------------|
| `chart/` | Helm chart with templated Deployments, StatefulSets, ConfigMaps, probes |
| `scripts/setup-aks.sh` | AKS cluster provisioning |
| `scripts/setup-cosmos.sh` | Cosmos containers (feed, lease, reconciliation, health) |
| `scripts/setup-acr.sh` | ACR creation + image build/push |

## Reusing for Your Own Workload

1. Build a container image with your workload logic
2. Implement HTTP endpoints: `/health` (liveness), `/ready`
   (readiness), `/metrics` (optional)
3. Create a `values-myworkload.yaml` overriding:
   - `image.repository` / `image.tag`
   - `cosmos.*` (endpoint, containers, etc.)
   - `avadConsumer.replicas` / `lvConsumer.replicas`
4. Deploy: `helm upgrade --install my-soak ./infra/chart -f values-myworkload.yaml`

## Azure Resources

Override default resource names via environment variables
in each script:

```bash
export SUBSCRIPTION="<your-subscription-id>"
export RG="<your-resource-group>"
export AKS_CLUSTER="<your-aks-name>"
export ACR_NAME="<your-acr-name>"
```
