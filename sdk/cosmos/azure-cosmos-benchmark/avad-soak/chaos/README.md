# Cosmos DB Soak Test — Chaos Library

Reusable chaos injection scenarios for AKS-hosted Cosmos DB
consumers. Works with any workload deployed via the soak
infra Helm chart.

## Scenarios

| Scenario | Script | What It Tests |
|----------|--------|---------------|
| Pod Kill | `pod-kill.sh` | Lease rebalancing after random pod loss |
| Partition Split | `partition-split.sh` | Continuation token validity across splits |

## Usage

### Manual — run one scenario

```bash
export NAMESPACE=cosmos-soak
export COSMOS_ACCOUNT=<your-account>
export COSMOS_RG=<your-rg>

# Kill a random AVAD CFP pod
bash chaos/scenarios/pod-kill.sh

# Trigger partition split (2x throughput)
SCALE_FACTOR=2 bash chaos/scenarios/partition-split.sh
```

### Automated — via soak orchestrator

The `run-soak.sh` orchestrator reads `chaos-schedule.yaml`
and fires scenarios on a phase-based schedule:

```
Warm-up → Steady → Chaos → Recovery → repeat
```

See `chaos-schedule.yaml` for interval/parameter config.

## Adding a New Scenario

1. Create `chaos/scenarios/my-scenario.sh`
2. Use env vars for all parameters (no hardcoded values)
3. Add an entry to `chaos-schedule.yaml`
4. The soak orchestrator will pick it up automatically
