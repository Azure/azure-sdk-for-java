---
name: cosmos-benchmark-run
description: Run a Cosmos DB benchmark scenario. Supports branch/PR/commit checkout, CHURN preset and custom scenarios, auto-configures App Insights monitoring from provision output, runs in tmux on remote VMs, and supports multi-VM parallel execution. Triggers on "run benchmark", "execute test", "test fix on branch", "run from PR", "start benchmark", "DR drill".
---

# Run a Benchmark

Execute a benchmark on one or more VMs. Always uses `run-benchmark.sh` wrapper (includes `monitor.sh` + git metadata capture).

## VM Connection

Auto-detect from provision output:

```bash
VM_IP=$(cat .vm-ip)
VM_USER=$(cat .vm-user)
VM_KEY=$(cat .vm-key)
SSH_CMD="ssh -i $VM_KEY $VM_USER@$VM_IP"
```

## 1. Configure Monitoring

### Application Insights (auto-configure from provision output)

If `test-setup/app-insights-connection-string.txt` exists:

```bash
AI_CONN_STR=$(cat test-setup/app-insights-connection-string.txt)
$SSH_CMD "echo 'export APPLICATIONINSIGHTS_CONNECTION_STRING=\"$AI_CONN_STR\"' >> ~/.bashrc"
```

If not found, ask the user whether to:
- Provide an App Insights connection string
- Skip App Insights (local CSV metrics only)

### Graphite (optional)

```bash
$SSH_CMD "echo 'export GRAPHITE_SERVICE_ADDRESS=\"<host>:<port>\"' >> ~/.bashrc"
```

## 2. Scenario Selection

Read `references/presets.md` for preset flag recipes.

### CHURN preset (default — leak detection)

Tests client create/close resource leaks (threads, connections, memory).

```
-cycles 5 -numberOfOperations 500
```

The harness auto-applies when cycles > 1:
- `settleTimeMs=90000`
- `suppressCleanup=true`
- `gcBetweenCycles=true`

### Custom scenarios

Read `references/scenarios.md` for the full operation catalog (20 types) and tuning parameters.
Users can pass any combination of CLI flags for custom workloads.

## 3. Execute on Single VM

Always run inside tmux so the benchmark survives SSH disconnection.

```bash
$SSH_CMD "tmux new-session -d -s bench 'cd ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark && \
  bash scripts/run-benchmark.sh CHURN ~/tenants.json ./results/<run-name> [extra-flags]'"
```

### Monitor progress

```bash
# Attach to tmux session
$SSH_CMD -t "tmux attach -t bench"

# Or peek at output without attaching
$SSH_CMD "tmux capture-pane -t bench -p | tail -30"

# Check if monitor.csv is growing
$SSH_CMD "wc -l ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/<run-name>/monitor.csv"
```

### Run naming convention

Use descriptive names: `<date>-<scenario>-<branch>`, e.g.:
```
20260226-CHURN-fix-telemetry-leak
20260226-CHURN-main-baseline
```

## 4. Execute on Multiple VMs (parallel)

For comparing versions or running different scenarios simultaneously:

```bash
# VM 1: baseline (main branch)
ssh -i $VM_KEY $VM_USER@<VM1_IP> "tmux new-session -d -s bench \
  'cd ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark && \
   bash scripts/run-benchmark.sh CHURN ~/tenants.json ./results/baseline-main'"

# VM 2: fix branch
ssh -i $VM_KEY $VM_USER@<VM2_IP> "tmux new-session -d -s bench \
  'cd ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark && \
   bash scripts/run-benchmark.sh CHURN ~/tenants.json ./results/fix-branch'"
```

To test different SDK versions on different VMs, use the **setup** skill on each VM with different branch/PR/commit targets before running.

## 5. What run-benchmark.sh Does

The wrapper script (`scripts/run-benchmark.sh`) handles:
1. Captures git metadata (branch, commit) → `git-info.json`
2. Launches JVM with `-Xmx8g -XX:+UseG1GC` + GC logging
3. Spawns `monitor.sh` in parallel for external JVM monitoring → `monitor.csv`
4. Cleans up monitoring on exit

### Output directory structure

```
results/<run-name>/
├── git-info.json          # branch, commit SHA
├── monitor.csv            # external JVM metrics (threads, heap, FDs, GC)
├── metrics/               # Codahale CSV metrics (latency histograms, throughput)
├── gc.log                 # G1GC log
└── heap-dumps/            # if OOM or manually triggered
```

## After Run

Suggest using the **cosmos-benchmark-analyze** skill to analyze results.

## References

- **Preset flag recipes**: `references/presets.md`
- **Full operation catalog & custom scenarios**: `references/scenarios.md`
- **Run script**: `sdk/cosmos/azure-cosmos-benchmark/scripts/run-benchmark.sh`
- **Trigger script**: `sdk/cosmos/azure-cosmos-benchmark/scripts/trigger-benchmark.sh`
