---
name: cosmos-benchmark-run
description: Build and run Cosmos DB benchmarks — clone repo at a branch/PR/commit, generate tenants.json, build the benchmark JAR, and execute scenarios on remote VMs. Supports multiple refs for comparison. Triggers on "run benchmark", "execute test", "start benchmark", "build benchmark", "create tenants.json", "DR drill".
---

# Run Benchmark

Clone, build, and execute a benchmark on a provisioned VM. All operations are implemented as scripts. Each ref uses a single SSH session for checkout → build → verify → run.

## VM Connection

Read VM connection info from the config directory provided during resource setup:

```bash
CONFIG_DIR="<path-from-setup-resources-step>"
```

## Step 1 — Collect Inputs

Ask the user for:

1. **Refs to benchmark** — one or more branches, commits, PRs, or tags:
   - Single: `main`
   - Multiple for comparison: `main, fix/telemetry-leak`
   - PR + baseline: `PR#12345, main`
   - Commit SHAs: `abc1234, def5678`

2. **Scenario preset** (default: `SIMPLE`):

   | Preset | Operations | Duration | Use case |
   |---|---|---|---|
   | **SIMPLE** | ReadThroughput | ~30 min | Quick validation, single-op benchmark |
   | **EXPAND** | ReadThroughput → WriteThroughput → QueryOrderby | ~90 min | Full performance profile |
   | **CHURN** | Configurable (cycles) | Varies | Leak detection, resource cleanup validation |

   See `references/scenarios.md` for custom operation flags.

3. **tenants.json customization** — use defaults or adjust operation, concurrency, connection mode.

## Step 2 — Generate Config

Generate `tenants.json` from the credentials exported during resource setup:

```bash
bash scripts/generate-tenants.sh \
  --config-dir "$CONFIG_DIR" \
  --output tenants.json \
  --copy-to-vm
```

Options:
| Flag | Default | Description |
|---|---|---|
| `--config-dir` | — | **Required.** Path to config directory |
| `--output` | `tenants.json` | Output file path |
| `--operation` | `ReadThroughput` | Default operation |
| `--connection-mode` | `GATEWAY` | `GATEWAY` or `DIRECT` |
| `--concurrency` | `20` | Concurrent operations |
| `--copy-to-vm` | false | SCP the file to `~/tenants.json` on VM |

Ensure `tenants.json` and `clientHostAndKey.txt` are in `.gitignore` — they contain secrets.

## Step 3 — Run Benchmarks

### Single or multiple refs

```bash
bash scripts/run-all-refs.sh \
  --config-dir "$CONFIG_DIR" \
  --refs "main, fix/telemetry-leak" \
  --scenario SIMPLE
```

The orchestrator, for each ref, uses **a single SSH session** to:
1. Checkout the ref (auto-detects branch/PR/commit/tag)
2. Build linting-extensions + cosmos benchmark JAR
3. Verify readiness (JDK, JAR, config, disk)
4. Execute the benchmark

Results are saved to `results/<date>-<scenario>-<ref-label>/` on the VM.

If any ref fails, it's skipped and the next ref proceeds. A summary is printed at the end.

### Multiple VMs (parallel)

Run `run-all-refs.sh` with different `--config-dir` paths pointing to different VMs:

```bash
bash scripts/run-all-refs.sh --config-dir "$CONFIG_DIR_VM1" --refs "main" --scenario SIMPLE &
bash scripts/run-all-refs.sh --config-dir "$CONFIG_DIR_VM2" --refs "fix/leak" --scenario SIMPLE &
wait
```

### Run naming convention

Runs are named `<date>-<scenario>-<ref-label>`, e.g.:
```
20260302-SIMPLE-main
20260302-EXPAND-fix-telemetry-leak
20260302-CHURN-PR-12345
```

### Monitor progress

```bash
SSH_CMD="ssh -i $(cat $CONFIG_DIR/vm-key) $(cat $CONFIG_DIR/vm-user)@$(cat $CONFIG_DIR/vm-ip)"

# Peek at output
$SSH_CMD "tmux capture-pane -t bench -p | tail -30"

# Check monitor.csv
$SSH_CMD "wc -l ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/<run-name>/monitor.csv"
```

## Output Directory Structure

```
results/<run-name>/
├── git-info.json          # branch, commit SHA
├── monitor.csv            # external JVM metrics (threads, heap, FDs, GC)
├── metrics/               # Codahale CSV metrics (latency histograms, throughput)
├── gc.log                 # G1GC log
└── heap-dumps/            # if OOM or manually triggered
```

## After Run

Suggest using the **cosmos-benchmark-analyze** skill to download and analyze results.

## Scripts Reference

| Script | Purpose |
|---|---|
| `scripts/run-all-refs.sh` | **Orchestrator.** For each ref: sends `vm-prepare-and-run.sh` to VM via single SSH. |
| `scripts/vm-prepare-and-run.sh` | **Runs ON VM.** Checkout → build → verify → run for one ref. |
| `scripts/generate-tenants.sh` | Generate tenants.json from clientHostAndKey.txt. |
| `scripts/run-benchmark.sh` | Execute benchmark with monitoring (git metadata, GC log, monitor.csv). |
| `scripts/monitor.sh` | External JVM monitoring (spawned by run-benchmark.sh). |
| `scripts/capture-diagnostics.sh` | Capture thread/heap dumps and JFR recordings during a live run. |
| `references/tenants-sample.json` | Template for tenants.json structure. |
| `references/presets.md` | Preset flag recipes (SIMPLE, EXPAND, CHURN). |
| `references/scenarios.md` | Full operation catalog (20+ types). |
