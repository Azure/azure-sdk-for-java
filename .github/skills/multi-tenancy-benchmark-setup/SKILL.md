---
name: multi-tenancy-benchmark-setup
description: Set up prerequisites for running Cosmos DB multi-tenancy benchmarks - generate tenants.json from account credentials, copy files to VM, verify build, and prepare the environment. Use when the user needs to create tenants.json, copy files to VM, check prerequisites, clone/update repo, or verify build. For VM creation/provisioning, use multi-tenancy-benchmark-vm-provision instead. Triggers on "setup benchmark", "create tenants.json", "copy files to VM", "verify build", "clone repo on VM".
---

# Benchmark Setup & Prerequisites

Prepare everything needed before running a benchmark. This skill handles environment setup tasks that must be done before using `multi-tenancy-benchmark-run`.

## Capabilities

1. **Generate tenants.json** from account credentials file
2. **Copy files to VM** (tenants.json, config files)
3. **Verify build** (JAR exists or needs building)
4. **Clone/update repo on VM**
5. **Verify VM readiness** (JDK, Maven, disk space)

## 1. Generate tenants.json

Parse a credentials file and produce a properly formatted `tenants.json`.

### Input format

The credentials file (`clientHostAndKey.txt`) has one account per line, CSV format:

```
<account-name>,<endpoint>,<master-key>
```

Example:

```
cosmosdbmulti12101,https://cosmosdbmulti12101.documents.azure.com:443/,uEXbSX...==
cosmosdbmulti12102,https://cosmosdbmulti12102.documents.azure.com:443/,qXGctv...==
```

### Output format

Generate `tenants.json` in `sdk/cosmos/azure-cosmos-benchmark/tenants.json`:

```json
{
  "globalDefaults": {
    "connectionMode": "GATEWAY",
    "consistencyLevel": "SESSION",
    "concurrency": "20",
    "numberOfOperations": "100000",
    "operation": "ReadThroughput",
    "numberOfPreCreatedDocuments": "1000",
    "connectionSharingAcrossClientsEnabled": "false",
    "maxConnectionPoolSize": "1000",
    "applicationName": "mt-bench"
  },
  "tenants": [
    {
      "id": "tenant-0",
      "serviceEndpoint": "<column-2>",
      "masterKey": "<column-3>",
      "databaseId": "benchdb",
      "containerId": "benchcol"
    }
  ]
}
```

### Steps

1. Read the credentials file. Expect 3 comma-separated columns per line.
2. For each non-empty line: column 2 = `serviceEndpoint`, column 3 = `masterKey`.
3. Generate a tenant entry with `id` = `tenant-<index>`, `databaseId` = `benchdb`, `containerId` = `benchcol`.
4. Include ALL accounts from the file - do not subset unless the user asks.
5. Write to `sdk/cosmos/azure-cosmos-benchmark/tenants.json`.
6. Verify: parse the output and confirm tenant count, first/last endpoints, and that all keys are populated.

### Gitignore

Ensure both `clientHostAndKey.txt` and `tenants.json` are in `sdk/cosmos/azure-cosmos-benchmark/.gitignore`. These files contain secrets and must never be committed.

## 2. Copy Files to VM

Copy `tenants.json` and optionally `clientHostAndKey.txt` to the benchmark VM.

```bash
scp -o StrictHostKeyChecking=no <local-path>/tenants.json benchuser@<vm-ip>:~/tenants.json
```

Check for VM IP in `.vm-ip` file or ask the user.

## 3. Verify Build

Check if the benchmark JAR exists:

```bash
ls sdk/cosmos/azure-cosmos-benchmark/target/azure-cosmos-benchmark-*-jar-with-dependencies.jar
```

If missing, build. See Build Commands below.

On remote VM:

```bash
ssh benchuser@<vm-ip> "ls ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/target/*jar-with-dependencies.jar 2>/dev/null && echo 'JAR found' || echo 'JAR missing - build needed'"
```

### Build Commands

All build commands must run from `sdk/cosmos/` directory. Maven 3.8.1+ is required (Ubuntu 22.04 apt provides 3.6.3 which is too old).

Common flags: `-e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true`

Build in this order:

```bash
cd sdk/cosmos
mvn <flags> -pl ,azure-cosmos -am clean install
mvn <flags> -pl ,azure-cosmos-test clean install
mvn <flags> -pl ,azure-cosmos-encryption clean install
mvn <flags> -pl ,azure-cosmos-benchmark clean package -P package-assembly
```

On remote VM (Maven installed at `/opt/apache-maven-3.9.12`):

```bash
ssh benchuser@<vm-ip> "export PATH=/opt/apache-maven-3.9.12/bin:/usr/bin:/bin:\$PATH && cd ~/azure-sdk-for-java/sdk/cosmos && mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos -am clean install && mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-test clean install && mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-encryption clean install && mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-benchmark clean package -P package-assembly"
```

## 4. Clone/Update Repo on VM

Clone from the user's fork (ask for the repo URL if not known):

```bash
ssh benchuser@<vm-ip> "git clone --depth 1 -b <branch> <repo-url> ~/azure-sdk-for-java"
```

If already cloned, fetch and checkout:

```bash
ssh benchuser@<vm-ip> "cd ~/azure-sdk-for-java && git fetch --depth 1 origin <branch> && git checkout <branch>"
```

## 5. Verify VM Readiness

Check all prerequisites on the VM:

```bash
ssh benchuser@<vm-ip> "echo '=== VM Check ==='; java -version 2>&1 | head -1; mvn --version 2>&1 | head -1; df -h / | tail -1; ls ~/tenants.json 2>/dev/null && echo 'tenants.json: OK' || echo 'tenants.json: MISSING'; ls ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/target/*jar-with-dependencies.jar 2>/dev/null && echo 'JAR: OK' || echo 'JAR: MISSING'"
```

Report status for each:
- JDK installed
- Maven 3.8.1+ installed (not the apt version 3.6.3)
- Repo cloned (correct branch)
- Benchmark JAR built
- tenants.json present
- Disk space sufficient (>10 GB free)

## After Setup

Once all checks pass, suggest: "Ready to run. Use the multi-tenancy-benchmark-run skill to start a benchmark."
