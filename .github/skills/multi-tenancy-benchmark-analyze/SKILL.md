---
name: multi-tenancy-benchmark-analyze
description: Analyze Cosmos DB multi-tenancy benchmark results. Use when the user wants to analyze benchmark output, check for leaks, interpret monitor.csv, or evaluate whether a fix passed. Triggers on mentions of "analyze results", "check benchmark", "leak check", "did it pass", monitor.csv, or benchmark result directories.
---

# Analyze Benchmark Results

Read a benchmark result directory, apply pass/fail thresholds, and explain anomalies.

## Workflow

1. Find `monitor.csv` in the given directory. If not found, search `sdk/cosmos/azure-cosmos-benchmark/results/` for recent runs.
2. Parse the CSV. Column definitions in `references/thresholds.md`.
3. Extract key snapshots:
   - **Baseline** = first row (`PRE_CREATE`)
   - **Peak** = row with highest `usedHeapBytes`
   - **Final** = last row (`POST_CLOSE`)
4. Compute:
   - `thread_delta = final.liveThreadCount - baseline.liveThreadCount`
   - `heap_ratio = final.usedHeapBytes / baseline.usedHeapBytes`
5. Apply thresholds from `references/thresholds.md`.
6. If any metric fails, explain why using `references/root-causes.md`.
7. Read `git-info.json` and `test_config.json` if present for branch/commit/scenario.

## Output Format

```
📊 Benchmark Results: <directory>
Branch: <branch>  Commit: <commit>  Scenario: <scenario>

HEAP:    Baseline=<X>MB  Peak=<Y>MB  After close=<Z>MB
THREADS: Baseline=<X>  Peak=<Y>  After close=<Z>
DIRECT:  Peak=<X>MB
FDs:     Peak=<X>
GC:      Count=<X>  Time=<Y>ms

✅/🔴 Thread leak: delta=<N> (threshold: ≤2)
✅/🔴 Memory leak: ratio=<N> (threshold: ≤1.1)

Overall: ✅ PASSED / 🔴 FAILED
```

## References

- **Pass/fail thresholds and CSV columns**: `references/thresholds.md`
- **Anomaly explanations**: `references/root-causes.md`
