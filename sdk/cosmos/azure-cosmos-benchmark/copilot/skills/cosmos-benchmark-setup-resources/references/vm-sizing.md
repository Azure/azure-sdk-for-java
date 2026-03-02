# VM Sizing Reference

## Default VM size

Use **Standard_D16s_v5** (16 vCPUs, 64 GB RAM) for all benchmark workloads.

## Cost considerations

- Use **spot instances** for non-critical test runs (~60-80% cheaper)
- **Deallocate** when not in use: `az vm deallocate -g <rg> -n <vm>`
- **Start** when needed: `az vm start -g <rg> -n <vm>`
- Consider **auto-shutdown** for overnight runs

## Region selection

Choose a region **co-located** with your Cosmos DB accounts to minimize network latency.
If accounts are in multiple regions, pick the region with the most accounts.
