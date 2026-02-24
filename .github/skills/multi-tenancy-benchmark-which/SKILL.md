---
name: multi-tenancy-benchmark-which
description: Look up which benchmark scenario, run ID, and metrics to use for validating a specific Cosmos DB SDK fix (A1-A22). Use when the user asks "which test for A1", "what scenario validates the telemetry fix", "how to test fix A7", or mentions a specific action item number from the multi-tenancy analysis.
---

# Which Test for a Fix

Look up the fix ID in `references/fix-scenario-map.md` and respond with full context.

## Output Format

```
Fix <ID> — <Description>
  File:       <source file path>
  Scenario:   <SCENARIO> (S<N>)
  Run ID:     <B-number>
  Key metric: <what to measure>
  Pass:       <criteria>

  Run command:
    bash sdk/cosmos/azure-cosmos-benchmark/scripts/run-benchmark.sh <SCENARIO> tenants.json ./results/B<N>-<id>

  Or build from branch:
    bash sdk/cosmos/azure-cosmos-benchmark/scripts/trigger-benchmark.sh --branch fix-<id> --scenario <SCENARIO> --tenants tenants.json
```

For fixes not in the mapping table (A4–A6, A8–A10, A12–A14, A16–A21), read `sdk/cosmos/multi-tenancy-analysis.md` and `sdk/cosmos/azure-cosmos-benchmark/IMPLEMENTATION_GUIDE.md`.

## Reference

- **Fix-to-scenario mapping**: `references/fix-scenario-map.md`
