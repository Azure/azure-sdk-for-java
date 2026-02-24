---
name: multi-tenancy-benchmark-status
description: Show the current state of the Cosmos DB benchmark environment — recent runs, VM status, build status, and tenants config. Use when the user asks "what runs do I have", "benchmark status", "is the VM up", "show recent results", or wants an overview before starting a benchmark session.
---

# Benchmark Environment Status

Check and report the current state of the benchmark environment.

## Checks

1. **Recent results**: List directories under `sdk/cosmos/azure-cosmos-benchmark/results/` and `./results/`.
   - For each: check if `monitor.csv` exists (📊 = complete, ❌ = incomplete)
   - Read `git-info.json` for branch/commit if present
   - Show most recent 5–10 runs

2. **Benchmark VM**: Check for `.vm-ip` file in workspace root.
   - If found, SSH to verify: `ssh -o ConnectTimeout=5 benchuser@<ip> "echo OK; uptime; java -version 2>&1 | head -1; ls ~/azure-sdk-for-java/results/ 2>/dev/null | tail -5"`
   - Report: reachable/unreachable, uptime, JDK version, runs on VM

3. **Build status**: Check for `sdk/cosmos/azure-cosmos-benchmark/target/azure-cosmos-benchmark-*-jar-with-dependencies.jar`.
   - Report: found (with file timestamp) or "Not built"

4. **Tenants config**: Check for `tenants.json` in workspace root and `sdk/cosmos/azure-cosmos-benchmark/`.
   - Report: found/not found, count of tenants if parseable

## Output Format

```
📊 Recent Runs:
  📊 20260218T143022-CHURN/    branch: fix-a1  commit: abc1234
  📊 20260218T120000-SCALING/  branch: main    commit: def5678
  ❌ 20260219T090000-CHURN/    (incomplete)

🖥️ Benchmark VM: ✅ 4.154.169.45 (up 3 days, JDK 21.0.10)
🔨 Build: ✅ JAR found (2026-02-18 14:30)
📋 Tenants: ✅ tenants.json (3 accounts)
```
