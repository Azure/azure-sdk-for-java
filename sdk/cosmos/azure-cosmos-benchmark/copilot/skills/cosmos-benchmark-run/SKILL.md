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

### Launch and verify (required two-step)

**Step A — Launch the orchestrator** using `mode="sync"` with `initial_wait: 60`. This gives enough time to see script copy + tmux launch output while keeping the shell attached for polling:

```bash
bash scripts/run-all-refs.sh \
  --config-dir "$CONFIG_DIR" \
  --refs "main, fix/telemetry-leak" \
  --scenario SIMPLE
```

**Step B — Verify the run is progressing** (MANDATORY, do not skip). Within 90 seconds of launch, run `check-status.sh` to confirm the tmux session is alive and a new results directory exists:

```bash
bash scripts/check-status.sh --config-dir "$CONFIG_DIR"
```

**What to check**:
- ✅ Tmux session is "Running"
- ✅ A new results directory matching `<date>-<scenario>-<ref-label>` exists
- ✅ Status is "⏳ Starting or build-only" or "🔄 Running" (not "❌ Failed")

**If any check fails**, investigate immediately — read `benchmark.log`, diagnose the issue, and report to the user before retrying. Do NOT tell the user "it's running" without verifying.

The benchmark itself runs in a **tmux session** (`bench`) on the VM, so it survives SSH disconnections. Even if the local orchestrator process is interrupted, the benchmark continues on the VM.

After verification, the user can continue working. The agent MUST:
- **Proactively poll** for completion using `read_bash` on the orchestrator shell
- When the orchestrator exits, **immediately notify** the user with the result (✅/❌ per ref)
- **Suggest analysis** if all refs succeeded

Do NOT wait for the user to ask "peek" to discover the run has completed. If the user is idle and the shell completes, report the result immediately.

### Monitor progress

**Quick status check** — use the `check-status.sh` script:

```bash
bash scripts/check-status.sh --config-dir "$CONFIG_DIR"

# With run-specific details
bash scripts/check-status.sh --config-dir "$CONFIG_DIR" --run-name 20260303-SIMPLE-main

# With system resource info
bash scripts/check-status.sh --config-dir "$CONFIG_DIR" --verbose
```

The script checks in a single call:
- Tmux session status (running / completed) with latest output
- Results directories with per-run status (in progress / completed / failed)
- Git state (branch, commit)
- Build status (JAR present)
- Run-specific details when `--run-name` is provided (monitor samples, metrics, disk)
- System resources when `--verbose` is set (disk, memory, load)

**Local orchestrator output** (shows which ref is currently running):

Use `read_bash` with the shellId from the async launch.

### Early exit detection and troubleshooting

The mandatory verify step (Step B above) catches most early failures. Additionally, if the orchestrator shell exits unexpectedly during polling (expected runtime is 30–90+ min), investigate immediately.

**Detection**: When checking status via `read_bash`, if the shell has already exited or accepts new commands, investigate immediately — do not assume success.

**Diagnosis**: Run `check-status.sh` which checks all of the above in one call:

```bash
bash scripts/check-status.sh --config-dir "$CONFIG_DIR" --verbose
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
| `scripts/check-status.sh` | **Status checker.** Tmux, results, git, build, system resources — all in one call. |
| `references/tenants-sample.json` | Template for tenants.json structure. |
| `references/presets.md` | Preset flag recipes (SIMPLE, EXPAND, CHURN). |
| `references/scenarios.md` | Full operation catalog (20+ types). |
