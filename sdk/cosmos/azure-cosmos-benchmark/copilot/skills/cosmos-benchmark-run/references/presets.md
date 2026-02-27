# Benchmark Scenario Presets

CLI flag recipes for benchmark scenarios.
The benchmark harness uses smart defaults: when cycles > 1, it
automatically sets settleTimeMs=90000, suppressCleanup=true,
and gcBetweenCycles=true unless explicitly overridden.

## CHURN (Leak Detection)

Tests client lifecycle resource leaks (threads, connections, memory).

```
-cycles 5 -numberOfOperations 500
```

The harness auto-applies:
- settleTimeMs=90000 (> 60s BoundedElastic evictor TTL)
- suppressCleanup=true (keep DB/container across cycles)
- gcBetweenCycles=true (separate real leaks from GC-eligible garbage)

To override any default: `-cycles 5 -settleTimeMs 120000`

## Adding New Presets

New presets can be added here as needed. Each preset should define:
1. A name and description of what it tests
2. The minimal CLI flags needed
3. Which defaults the harness auto-applies
4. When to use this preset
