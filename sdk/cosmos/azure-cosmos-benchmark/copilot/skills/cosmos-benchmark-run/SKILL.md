---
name: cosmos-benchmark-run
description: Build and run Cosmos DB benchmarks — clone repo at a branch/PR/commit, generate tenants.json, build the benchmark JAR, and execute scenarios on remote VMs. Supports multiple refs for comparison. Triggers on "run benchmark", "execute test", "start benchmark", "build benchmark", "create tenants.json", "DR drill".
---

# Run Benchmark

Clone, build, and execute a benchmark on a provisioned VM. All operations are implemented as scripts. Each ref uses a single SSH session for checkout → build → verify → run. The benchmark execution runs inside a **tmux session** on the VM for resilience against SSH disconnections.

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
   - Fork branches: `xinlian12/wireConnectionSharingInBenchmark` (auto-detects remote)

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
4. Execute the benchmark (inside a tmux session for resilience)

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

### Async execution (non-blocking)

**Always run the orchestrator in async mode** so the user can continue working while benchmarks run. Use the `bash` tool with `mode="async"`:

```bash
# Launches in background, returns a shellId for monitoring
bash scripts/run-all-refs.sh \
  --config-dir "$CONFIG_DIR" \
  --refs "main, fix/telemetry-leak" \
  --scenario SIMPLE
```

After launching, the user can:
- **Continue working** on other tasks in the main context
- **Check status** at any time (see Monitor progress below)
- **Get notified** when the orchestrator reports completion via `read_bash`

The benchmark itself runs in a **tmux session** (`bench`) on the VM, so it survives SSH disconnections. Even if the local orchestrator process is interrupted, the benchmark continues on the VM.

### Monitor progress

**Local orchestrator output** (shows which ref is running, build progress):

Use `read_bash` with the shellId from the async launch to check the latest output.

**VM-side benchmark output** (real-time metrics, live logs):

```bash
SSH_CMD="ssh -i $(cat $CONFIG_DIR/vm-key) $(cat $CONFIG_DIR/vm-user)@$(cat $CONFIG_DIR/vm-ip)"

# Peek at live benchmark output in the tmux session
$SSH_CMD "tmux capture-pane -t bench -p | tail -30"

# Check if tmux session is still running
$SSH_CMD "tmux has-session -t bench 2>/dev/null && echo 'Running' || echo 'Finished'"

# Check monitor.csv row count (grows every 60s)
$SSH_CMD "wc -l ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/<run-name>/monitor.csv"

# Attach to live session (interactive — for debugging only)
$SSH_CMD -t "tmux attach -t bench"
```

### Early exit detection and troubleshooting

After launching the orchestrator in async mode, **proactively verify** that the run is progressing. If the async shell exits within a few minutes (expected runtime is 30–90+ min), the run likely failed early.

**Detection**: When checking status via `read_bash`, if the shell has already exited or accepts new commands, investigate immediately — do not assume success.

**Diagnosis steps** (run on the VM via SSH):

```bash
SSH_CMD="ssh -i $(cat $CONFIG_DIR/vm-key) $(cat $CONFIG_DIR/vm-user)@$(cat $CONFIG_DIR/vm-ip)"

# 1. Check if any new results directories were created today
$SSH_CMD "ls -lt ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/ | head -5"

# 2. Check git state (last checkout, remotes available)
$SSH_CMD "cd ~/azure-sdk-for-java && git log --oneline -1 && git remote -v"

# 3. Check if the benchmark JAR exists
$SSH_CMD "ls ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/target/*jar-with-dependencies.jar 2>/dev/null || echo 'JAR not found — build may have failed'"

# 4. Check tmux session
$SSH_CMD "tmux has-session -t bench 2>/dev/null && echo 'Running' || echo 'No session'"
```

**Common failures and fixes**:

| Symptom | Likely cause | Fix |
|---|---|---|
| No new results directory | Checkout failed | Check if ref exists; for fork branches use `remote/branch` format (e.g., `xinlian12/branchName`) — the script auto-detects remotes configured on the VM |
| JAR not found | Build failed | SSH in and check Maven output; common issues: disk space, dependency download failures |
| tmux session exited immediately | Benchmark startup error | Check `results/<run-name>/benchmark.log` for errors (e.g., invalid tenants.json, connection failures) |
| Orchestrator exits but tmux still running | SSH timeout during poll wait | Benchmark is fine — tmux session survives. Check results via SSH directly |

**If diagnosis reveals an issue**, fix it and confirm with the user before relaunching. Do not silently retry.

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
