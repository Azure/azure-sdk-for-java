# VM Sizing Reference

## Recommended configurations by workload

| Tenants | Scenario | VM Size | vCPUs | RAM | Notes |
|---------|----------|---------|-------|-----|-------|
| 1-10 | Quick test | Standard_D4s_v5 | 4 | 16 GB | Minimal |
| 10-50 | CHURN, SCALING | Standard_D16s_v5 | 16 | 64 GB | Default recommendation |
| 50-100 | POOL_PRESSURE | Standard_D32s_v5 | 32 | 128 GB | High concurrency |
| 100+ | SOAK (24h) | Standard_D16s_v5 | 16 | 64 GB | Long-running, moderate load |

## Cost considerations

- Use **spot instances** for non-critical test runs (~60-80% cheaper)
- **Deallocate** when not in use: `az vm deallocate -g <rg> -n <vm>`
- **Start** when needed: `az vm start -g <rg> -n <vm>`
- Consider **auto-shutdown** for overnight runs

## Region selection

Choose a region **co-located** with your Cosmos DB accounts to minimize network latency.
If accounts are in multiple regions, pick the region with the most accounts.
