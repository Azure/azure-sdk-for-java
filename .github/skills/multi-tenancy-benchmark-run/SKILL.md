---
name: multi-tenancy-benchmark-run
description: Trigger a Cosmos DB multi-tenancy benchmark scenario. Use when the user wants to run a benchmark, execute a test scenario, build and run the benchmark, or test a fix on a branch.
---

# Run a Benchmark

Determine scenario, check prerequisites, and execute.

## Scenario Selection

Read 
eferences/presets.md for flag recipes. Map user intent to preset:

| Keyword in user request | Preset | What it tests |
|---|---|---|
| "leak", "telemetry", "close", "churn" | CHURN | Client create/close leak detection (A1/A2/A3) |
| "idle", "scaling", "footprint", "clients" | SCALING | Resource cost per client |
| "soak", "stability", "long" | SOAK | Long-running stability |
| "smoke", "quick", "verify" | Quick Smoke Test | Compilation + basic run check |

Default: CHURN if unclear.

## Prerequisites

Check before running -- fix if missing:

1. **Benchmark JAR**: sdk/cosmos/azure-cosmos-benchmark/target/azure-cosmos-benchmark-*-jar-with-dependencies.jar
   - Build if missing (run from sdk/cosmos/):
     `
     mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos -am clean install
     mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-test clean install
     mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-encryption clean install
     mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-benchmark clean package -P package-assembly
     `
2. **tenants.json**: Must exist at path passed via -tenantsFile

## Execute

Construct the java command directly using flat CLI flags from the preset.
Example CHURN command:

`ash
java -jar target/azure-cosmos-benchmark-*-jar-with-dependencies.jar \
  -tenantsFile tenants.json \
  -outputDir ./results/churn-run \
  -cycles 5 -settleTimeMs 90000 -suppressCleanup true -gcBetweenCycles true \
  -numberOfOperations 500 -operation ReadThroughput -connectionMode GATEWAY
`

**Remote VM** (if user provides IP or .vm-ip exists):
`ash
ssh benchuser@<ip> "cd ~/azure-sdk-for-java && java -jar <jar> <flags>"
`

After completion, suggest using the **multi-tenancy-benchmark-analyze** skill.
