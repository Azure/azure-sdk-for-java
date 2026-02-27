---
name: cosmos-benchmark-analyze
description: Analyze Cosmos DB benchmark results — download from VM, parse CSV metrics, compare runs, analyze heap/thread dumps, generate markdown reports, export to Kusto, and query Application Insights. Triggers on "analyze results", "compare runs", "leak check", "did it pass", "heap dump", "thread dump", "generate report", "export to kusto", "regression check", monitor.csv, or result directories.
---

# Analyze Benchmark Results

Comprehensive post-run analysis: download results, CSV metrics, run comparison, heap/thread dumps, reports, and Kusto export.

## 1. Download Results from VM

Auto-detect VM connection:
```bash
VM_IP=$(cat benchmark-config/vm-ip); VM_USER=$(cat benchmark-config/vm-user); VM_KEY=$(cat benchmark-config/vm-key)
```

Download a run's results:
```bash
scp -i $VM_KEY -r $VM_USER@$VM_IP:~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/<run-name> \
  ./results/<run-name>
```

Each run lives in its own directory. Never overwrite previous results — this enables baseline tracking.

## 2. CSV Metrics Analysis

### Workflow

1. Find `monitor.csv` in the result directory.
2. Parse CSV columns (see `references/thresholds.md` for column definitions).
3. Cross-reference with lifecycle events from the benchmark log file (pattern: `[LIFECYCLE] <event> timestamp=<ISO>`).
4. Extract snapshots:
   - **Baseline** = first row after `PRE_CREATE`
   - **Peak** = row with highest `heap_used_kb`
   - **Final** = last row (after `POST_CLOSE` + settle)
5. Compute: `thread_delta`, `heap_ratio`
6. Apply thresholds from `references/thresholds.md`.

### Output Format

```
📊 Benchmark Results: <directory>
Branch: <branch>  Commit: <commit>  Scenario: <scenario>

HEAP:    Baseline=<X>MB  Peak=<Y>MB  After close=<Z>MB
THREADS: Baseline=<X>  Peak=<Y>  After close=<Z>
FDs:     Peak=<X>
GC:      Count=<X>  Time=<Y>ms

✅/🔴 Thread leak: delta=<N> (threshold: ≤2)
✅/🔴 Memory leak: ratio=<N> (threshold: ≤1.1)

Overall: ✅ PASSED / 🔴 FAILED
```

## 3. Compare Two Runs

1. Read `monitor.csv` from both directories.
2. Extract baseline, peak, final for each.
3. Read `git-info.json` from each for branch/commit.
4. Compute deltas.

### Output

```
📊 Comparing:
   Before: <dir1> (branch: <branch1>, commit: <commit1>)
   After:  <dir2> (branch: <branch2>, commit: <commit2>)

| Metric                    | Before | After  | Delta   | Status |
|---------------------------|--------|--------|---------|--------|
| Threads after close       |    218 |     19 |    -199 | ✅     |
| Heap after close (MB)     |    342 |    134 |    -208 | ✅     |
| Heap ratio                |   2.67 |   1.05 |  -1.62  | ✅     |
| Peak FDs                  |   4428 |   4312 |    -116 | ✅     |
| GC count                  |    847 |    812 |     -35 | ✅     |

Overall: ✅ Fix validated / 🔴 Regression detected
```

Status: ✅ improved, 🟡 marginal (<10%), 🔴 regressed (>10% worse)

## 4. Heap Dump Analysis

### Locate heap dumps

```
results/<run-name>/heap-dumps/heap-PRE_CLOSE-*.hprof
results/<run-name>/heap-dumps/heap-POST_CLOSE-*.hprof
```

### Option A: HeapDumpAnalyzer (built into benchmark JAR)

```bash
java -cp azure-cosmos-benchmark-*-jar-with-dependencies.jar \
  com.azure.cosmos.benchmark.HeapDumpAnalyzer <pre.hprof> <post.hprof>
```

### Option B: Python hprof parser (lightweight, no deps)

```bash
python3 references/parse_hprof.py <pre.hprof> --top 30
python3 references/parse_hprof.py --diff <pre.hprof> <post.hprof> --top 20
```

### Option C: YourKit (detailed, requires license)

If YourKit is installed on the VM:

```bash
# Open snapshot in YourKit CLI
<yourkit-dir>/bin/profiler.sh -export -snapshot=<file.hprof> -csv -outdir=<output>
```

Or analyze interactively via YourKit GUI by downloading the .hprof files locally.

### Interpret results

Look for classes with more instances/bytes after close — these indicate objects not released during `CosmosAsyncClient.close()`. Common suspects: Reactor schedulers, Netty connection pools, background threads, unbounded caches.

## 5. Thread Dump Analysis

### Capture thread dump during benchmark

```bash
# On the VM, while benchmark is running:
jcmd <pid> Thread.print > results/<run-name>/thread-dump-$(date +%s).txt

# Or using jstack:
jstack <pid> > results/<run-name>/thread-dump-$(date +%s).txt
```

Capture multiple dumps at intervals to identify stuck or leaked threads:
```bash
for i in 1 2 3; do jcmd <pid> Thread.print > results/<run-name>/thread-dump-$i.txt; sleep 30; done
```

### Analyze thread dumps

Look for:
- **Thread count growth**: Compare total thread counts across dumps
- **Stuck threads**: Same thread in same stack across multiple dumps
- **Leaked pools**: Thread pool threads that should have been shut down after client close
- **Daemon vs non-daemon**: Non-daemon threads prevent JVM exit

Key thread name patterns in Cosmos SDK:
- `cosmos-parallel-*` — SDK parallel scheduler
- `reactor-http-*` — Reactor Netty event loop
- `boundedElastic-*` — Reactor bounded elastic pool
- `globalEndpointManager-*` — Cosmos endpoint refresh

## 6. Generate Markdown Report

Produce a markdown report with embedded charts (as inline base64 images or Plotly HTML):

### Using generate-dashboard.py

```bash
python3 copilot/skills/cosmos-benchmark-analyze/scripts/generate-dashboard.py \
  <results-dir>/metrics \
  <results-dir>/benchmark.log \
  <results-dir>/report.html \
  <results-dir>/monitor.csv
```

Arguments: `<metrics-dir> <log-file> <output-html> [monitor.csv]`

Then reference the HTML dashboard in the markdown report or generate a standalone markdown file with metrics tables and verdicts.

## 7. Export to Kusto

See `references/kusto-schema.md` for:
- Table schema (`BenchmarkResults`, `BenchmarkSummary`)
- CSV enrichment commands (add run metadata to monitor.csv)
- `.ingest` commands for Azure Data Explorer
- Sample queries (latest runs, compare runs, trend over time)

## 8. Application Insights Queries

If the benchmark was run with `APPLICATIONINSIGHTS_CONNECTION_STRING`, query metrics:

```bash
az monitor app-insights query \
  --app <app-insights-name> \
  --resource-group rg-cosmos-benchmark \
  --analytics-query "<KQL-query>"
```

Placeholder: user will provide specific KQL queries for latency percentiles, error rates, and throughput over time.

## References

- **Pass/fail thresholds and CSV columns**: `references/thresholds.md`
- **Python hprof parser**: `references/parse_hprof.py`
- **Kusto table schema & ingestion**: `references/kusto-schema.md`
- **Dashboard generator**: `scripts/generate-dashboard.py`
