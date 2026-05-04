# Cosmos DB Soak Test — Chaos Library

Reusable chaos injection scenarios for AKS-hosted Cosmos DB
consumers. Works with any workload deployed via the soak
infra Helm chart.

## Scenarios

| Scenario | Script | What It Tests |
|----------|--------|---------------|
| Pod Kill | `pod-kill.sh` | Lease rebalancing after random pod loss |
| Restart Storm | `restart-storm.sh` | Mass lease handoff on rolling restart |
| Lease Throttle | `lease-throttle.sh` | CFP behavior under lease container RU starvation |
| Network Fault | `network-fault.sh` | Retry behavior, session consistency |
| Partition Split | `partition-split.sh` | Continuation token validity across splits |
| Node Drain | `node-drain.sh` | Graceful shutdown, lease release timing |

## Usage

### Manual — run one scenario

```bash
export NAMESPACE=cosmos-soak
export COSMOS_ACCOUNT=abhm-cfp-region-test
export COSMOS_RG=abhm-rg

# Kill a random AVAD CFP pod
bash chaos/scenarios/pod-kill.sh

# Throttle lease container to 400 RU for 5 min
TARGET_RU=400 THROTTLE_DURATION=300 bash chaos/scenarios/lease-throttle.sh

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
