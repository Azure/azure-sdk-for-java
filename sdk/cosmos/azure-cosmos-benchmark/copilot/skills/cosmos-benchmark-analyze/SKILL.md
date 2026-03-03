---
name: cosmos-benchmark-analyze
description: Analyze Cosmos DB benchmark results — download from VM, generate markdown reports with time-series charts and comparison tables, apply pass/fail thresholds. Triggers on "analyze results", "compare runs", "leak check", "did it pass", "generate report", "regression check", or result directories.
---

# Analyze Benchmark Results

Download results, generate a markdown report with metrics analysis, time-series charts, and multi-run comparison.

## Step 1 — Download Results

Download results from the VM to the local config directory:

```bash
# List available runs on VM
bash scripts/download-results.sh --config-dir "$CONFIG_DIR" --list

# Download a specific run
bash scripts/download-results.sh --config-dir "$CONFIG_DIR" --run-name <run-name>

# Download all runs
bash scripts/download-results.sh --config-dir "$CONFIG_DIR" --all
```

Results are saved to `$CONFIG_DIR/results/<run-name>/`. Each run directory contains:

```
<run-name>/
├── monitor.csv            # JVM metrics (threads, heap, FDs, GC, RSS, CPU)
├── metrics/               # Codahale CSV metrics (throughput, latency per operation)
│   ├── #Successful Operations.csv
│   ├── #Unsuccessful Operations.csv
│   └── ...                # Per-tenant and per-operation variants
├── git-info.json          # branch, commit SHA
├── gc.log                 # G1GC log
└── benchmark.log          # Benchmark stdout/stderr
```

## Step 2 — Generate Report

Generate a markdown report from the downloaded results:

```bash
python3 scripts/generate-report.py \
  --results-dir "$CONFIG_DIR/results" \
  --output "$CONFIG_DIR/results/report.md"
```

To analyze specific runs only:

```bash
python3 scripts/generate-report.py \
  --results-dir "$CONFIG_DIR/results" \
  --runs "20260302-SIMPLE-main,20260302-SIMPLE-fix-leak"
```

### What the report contains

#### Per-run summary

For each run, the report includes:
- **Git info**: branch, commit
- **JVM metrics table**: baseline, peak, and final values for threads, heap, RSS, FDs, GC
- **Throughput table**: Codahale metrics (ops/sec mean, 1m, 5m rates) from `metrics/*.csv`
- **Time-series SVG charts**: inline sparklines for threads, heap, FDs, RSS, GC count, CPU over time

#### Multi-run comparison table (when ≥2 runs)

If multiple runs are present, the report includes:
- **Side-by-side metrics comparison**: threads, heap, heap ratio, thread delta, FDs, GC, RSS for each run
- **Throughput comparison**: ops/sec for each operation across runs

### Metrics analyzed

From **`monitor.csv`** (JVM-level, sampled every 60s):
| Metric | Description |
|---|---|
| threads | Live thread count |
| heap_used_kb | Used heap (S1U+EU+OU from jstat) |
| heap_max_kb | Max heap capacity |
| rss_kb | Resident set size |
| fds | Open file descriptors |
| cpu_pct | CPU usage percentage |
| gc_count | Cumulative GC count |
| gc_time_ms | Cumulative GC time |

From **`metrics/*.csv`** (Codahale, per-operation):
| File | Metrics |
|---|---|
| `#Successful Operations.csv` | count, mean_rate, m1_rate, m5_rate |
| `#Unsuccessful Operations.csv` | count, mean_rate, m1_rate, m5_rate |
| Per-tenant/operation variants | Same columns per operation type |

### Metrics reference

See `references/thresholds.md` for monitor.csv column definitions and metric descriptions.

## Step 3 — Thread Dump Analysis (optional)

If thread dumps were captured during the run (via `capture-diagnostics.sh`):

Look for:
- **Thread count growth**: compare total counts across dumps
- **Stuck threads**: same thread in same stack across dumps
- **Leaked pools**: threads that should have been shut down after client close

Key Cosmos SDK thread name patterns:
- `cosmos-parallel-*` — SDK parallel scheduler
- `reactor-http-*` — Reactor Netty event loop
- `boundedElastic-*` — Reactor bounded elastic pool
- `globalEndpointManager-*` — Cosmos endpoint refresh

## Scripts Reference

| Script | Purpose |
|---|---|
| `scripts/download-results.sh` | Download results from VM to `$CONFIG_DIR/results/`. |
| `scripts/generate-report.py` | Generate markdown report with metrics, charts, and comparison tables. |
| `references/thresholds.md` | Pass/fail thresholds and monitor.csv column definitions. |
