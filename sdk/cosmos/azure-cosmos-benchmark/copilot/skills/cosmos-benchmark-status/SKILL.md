---
name: cosmos-benchmark-status
description: Show the current state of the Cosmos DB benchmark environment — Azure resources, recent runs, VM status, build status, config, and App Insights health. Use when the user asks "what runs do I have", "benchmark status", "is the VM up", "show recent results", "list accounts", or wants an overview before starting a benchmark session.
---

# Benchmark Environment Status

Check and report the current state of the entire benchmark environment.

## Checks

1. **Azure Resources** (requires `az` CLI):
   - Cosmos DB accounts: `az cosmosdb list -g rg-cosmos-benchmark --query "[].{name:name, endpoint:documentEndpoint}" -o table`
   - Application Insights: `az monitor app-insights component list -g rg-cosmos-benchmark --query "[].{name:name, connectionString:connectionString}" -o table`
   - VMs: `az vm list -g rg-cosmos-benchmark -d --query "[].{name:name, ip:publicIps, state:powerState}" -o table`

2. **App Insights Health**: Verify metrics are being received:
   ```bash
   az monitor app-insights query \
     --app <app-insights-name> \
     --resource-group rg-cosmos-benchmark \
     --analytics-query "customMetrics | where timestamp > ago(1h) | summarize count() by bin(timestamp, 5m) | order by timestamp desc | take 5"
   ```
   If count > 0, metrics are flowing. If empty, App Insights may not be configured or the benchmark hasn't reported yet.

3. **Recent results**: List directories under `sdk/cosmos/azure-cosmos-benchmark/results/` and `./results/`.
   - For each: check if `monitor.csv` exists (📊 = complete, ❌ = incomplete)
   - Read `git-info.json` for branch/commit if present
   - Show most recent 5–10 runs

4. **Benchmark VM**: Check for `benchmark-config/vm-ip` file in workspace root.
   - If found, SSH to verify: `ssh -i $(cat benchmark-config/vm-key) $(cat benchmark-config/vm-user)@$(cat benchmark-config/vm-ip) "echo OK; uptime; java -version 2>&1 | head -1; ls ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/results/ 2>/dev/null | tail -5"`
   - Report: reachable/unreachable, uptime, JDK version, runs on VM

5. **Build status**: Check for `sdk/cosmos/azure-cosmos-benchmark/target/azure-cosmos-benchmark-*-jar-with-dependencies.jar`.
   - Report: found (with file timestamp) or "Not built"

6. **Config**: Check for `tenants.json` in workspace root and `sdk/cosmos/azure-cosmos-benchmark/`.
   - Report: found/not found, count of tenants if parseable

## Output Format

```
☁️ Azure Resources:
  Cosmos DB: 3 accounts (cosmosdb-bench-0, -1, -2)
  App Insights: ✅ cosmos-bench-ai (receiving metrics: 142 events/5min)
  VMs: ✅ vm-benchmark-01 (running, 4.154.169.45)

📊 Recent Runs (local):
  📊 20260226-CHURN-fix-leak/     branch: fix-leak  commit: abc1234
  📊 20260225-CHURN-main-base/    branch: main      commit: def5678
  ❌ 20260226-CHURN-experiment/   (incomplete)

📊 Recent Runs (VM):
  📊 20260226-CHURN-fix-leak/
  📊 20260225-CHURN-main-base/

🖥️ Benchmark VM: ✅ 4.154.169.45 (up 3 days, JDK 21.0.10)
🔨 Build: ✅ JAR found (2026-02-26 14:30)
📋 Config: ✅ tenants.json (3 tenants)
```
