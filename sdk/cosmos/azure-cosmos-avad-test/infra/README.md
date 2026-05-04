# Cosmos DB Soak Test — Infrastructure

Reusable Helm chart and setup scripts for running Cosmos DB
change feed processor soak tests on AKS.

## Quick Start

```bash
# 1. Create AKS cluster
./scripts/setup-aks.sh

# 2. Create Cosmos containers
./scripts/setup-cosmos.sh

# 3. Build + push image to ACR
./scripts/setup-acr.sh

# 4. Deploy (from repo root)
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

Default configuration targets:
- Subscription: `b31b6408-0fb5-4688-9a3c-33ffb3983297`
- Resource Group: `abhm-rg`
- AKS: `abhm-avad-soak-aks` (3x D4s_v5 nodes)
- ACR: `abhmavadsoakacr`
- Cosmos: `abhm-cfp-region-test`

Override via environment variables in each script.
