---
name: multi-tenancy-benchmark-compare
description: Compare two Cosmos DB multi-tenancy benchmark runs side by side. Use when the user wants to compare before/after results, evaluate a fix's impact, compute deltas between runs, or determine if a change regressed performance. Triggers on "compare runs", "before vs after", "did the fix help", "regression check", or two result directory paths.
---

# Compare Two Benchmark Runs

Read two result directories, compute deltas, and render a comparison table with verdict.

## Workflow

1. Read `monitor.csv` from both directories.
2. For each, extract baseline (first row), peak (max heap), final (last row).
3. Read `git-info.json` from each to identify branch/commit.
4. Compute deltas for each metric.
5. Render the table and explain which fix was validated using `references/fix-scenario-map.md`.

## Output Format

```
📊 Comparing:
   Before: <dir1> (branch: <branch1>, commit: <commit1>)
   After:  <dir2> (branch: <branch2>, commit: <commit2>)

| Metric                    | Before | After  | Delta   | Status |
|---------------------------|--------|--------|---------|--------|
| Threads after close       |    218 |     19 |    -199 | ✅     |
| Thread delta (from base)  |    200 |      1 |    -199 | ✅     |
| Heap after close (MB)     |    342 |    134 |    -208 | ✅     |
| Heap ratio                |   2.67 |   1.05 |  -1.62  | ✅     |
| Peak direct mem (MB)      |    512 |    498 |     -14 | ✅     |
| Peak FDs                  |   4428 |   4312 |    -116 | ✅     |
| GC count                  |    847 |    812 |     -35 | ✅     |

Overall: ✅ Fix validated — safe to merge.
```

## Status Indicators

- ✅ = improved (delta ≤ 0 for resource metrics)
- 🟡 = marginal (<10% change)
- 🔴 = regressed (>10% worse)

## References

- **Fix-to-scenario mapping**: `references/fix-scenario-map.md` — identify which fix the comparison validates
- **Root causes**: `references/root-causes.md` — explain anomalies in either run
