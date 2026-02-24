# Benchmark Scenario Presets

These are CLI flag recipes for common benchmark scenarios.
The benchmark harness uses smart defaults: when cycles > 1, it
automatically sets settleTimeMs=90000, suppressCleanup=true,
and gcBetweenCycles=true unless explicitly overridden.

## CHURN (Leak Detection)

Tests: A1/A2/A3 -- client lifecycle resource leaks (threads, connections, memory).

`
-cycles 5 -numberOfOperations 500
`

That's it. The harness auto-applies:
- settleTimeMs=90000 (> 60s BoundedElastic evictor TTL)
- suppressCleanup=true (keep DB/container across cycles)
- gcBetweenCycles=true (separate real leaks from GC-eligible garbage)

To override any default: -cycles 5 -settleTimeMs 120000

## SCALING (Resource Footprint)

Tests: A4/A5/A11 -- per-client resource cost at scale.

`
-numberOfOperations 1000
`

Single cycle (default cycles=1), N tenants. Measure steady-state threads, memory, connections.

## SOAK (Long-Running Stability)

Tests: A7/A8/A9 -- unbounded cache growth, slow leaks over hours.

`
-numberOfOperations 1000000
`

Single cycle with high operation count. Monitor heap and thread trends over time.
Run with monitor.sh for continuous JVM sampling.

## Quick Smoke Test

`
-numberOfOperations 100
`

Verify a fix compiles and runs. No leak detection.
