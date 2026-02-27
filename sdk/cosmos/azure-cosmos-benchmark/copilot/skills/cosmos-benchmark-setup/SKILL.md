---
name: cosmos-benchmark-setup
description: Set up the benchmark execution environment — install tools on VM, clone repo at a specific branch/PR/commit/tag, generate tenants.json, copy files, build the benchmark JAR, and verify readiness. Auto-detects VM from provision output. Triggers on "setup benchmark", "install JDK", "create tenants.json", "create config", "copy files to VM", "verify build", "clone repo on VM".
---

# Benchmark Environment Setup

Prepare the execution environment after infrastructure is provisioned.

## VM Connection

Auto-detect VM connection info from provision output:

```bash
VM_IP=$(cat benchmark-config/vm-ip)
VM_USER=$(cat benchmark-config/vm-user)
VM_KEY=$(cat benchmark-config/vm-key)
SSH_CMD="ssh -i $VM_KEY $VM_USER@$VM_IP"
```

If `benchmark-config/vm-ip` doesn't exist, ask the user for VM IP and SSH credentials.

All remote commands below use `$SSH_CMD` as shorthand.

## 1. Install Dependencies on VM

Run the setup script:

```bash
$SSH_CMD 'bash -s' < copilot/skills/cosmos-benchmark-setup/scripts/setup-benchmark-vm.sh
```

### What gets installed

| Component | Version | Location |
|-----------|---------|----------|
| OpenJDK | 21 | System package |
| Maven | 3.9.12 | `/opt/apache-maven-3.9.12` |
| async-profiler | 3.0 | `/opt/async-profiler-3.0-linux-x64` |
| git, net-tools, sysstat, tmux | latest | System packages |

### Verify

```bash
$SSH_CMD 'java -version 2>&1 | head -1; /opt/apache-maven-3.9.12/bin/mvn --version 2>&1 | head -1; tmux -V; df -h / | tail -1'
```

## 2. Clone/Update Repo on VM

### From a branch

```bash
$SSH_CMD "git clone --depth 1 -b <branch> <repo-url> ~/azure-sdk-for-java"
```

If already cloned:
```bash
$SSH_CMD "cd ~/azure-sdk-for-java && git fetch --depth 1 origin <branch> && git checkout <branch> && git pull origin <branch>"
```

### From a PR number

```bash
$SSH_CMD "cd ~/azure-sdk-for-java && git fetch origin pull/<pr-number>/head:pr-<pr-number> && git checkout pr-<pr-number>"
```

### From a commit SHA

```bash
$SSH_CMD "cd ~/azure-sdk-for-java && git fetch origin && git checkout <commit-sha>"
```

### From a tag

```bash
$SSH_CMD "cd ~/azure-sdk-for-java && git fetch --tags origin && git checkout tags/<tag-name>"
```

Ask the user which ref type they want. Default to branch if unspecified.

## 3. Generate Benchmark Configuration

### Multi-tenant mode (tenants.json)

If `clientHostAndKey.txt` exists (created by provision skill), generate `tenants.json`:

#### Sample tenants.json template

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
    "applicationName": "cosmos-bench"
  },
  "tenants": []
}
```

#### Generation steps

1. Read `clientHostAndKey.txt` — each line: `<name>,<endpoint>,<key>`
2. For each line, create a tenant entry:
   ```json
   { "id": "tenant-<index>", "serviceEndpoint": "<endpoint>", "masterKey": "<key>", "databaseId": "benchdb", "containerId": "benchcol" }
   ```
3. Ask user if they want to customize `globalDefaults` (operation type, concurrency, connection mode, etc.) or use the template defaults.
4. Write to `sdk/cosmos/azure-cosmos-benchmark/tenants.json`
5. Verify: parse output, confirm tenant count, first/last endpoints.

### Single-tenant mode

No config file needed. The **run** skill will construct CLI flags directly:
```
-serviceEndpoint <endpoint> -masterKey <key> -databaseId benchdb -containerId benchcol
```

### Gitignore

Ensure `clientHostAndKey.txt` and `tenants.json` are in `.gitignore` — they contain secrets.

## 4. Copy Config Files to VM

```bash
scp -i $VM_KEY -o StrictHostKeyChecking=no tenants.json $VM_USER@$VM_IP:~/tenants.json
```

## 5. Build the Benchmark JAR

All builds run in a tmux session to survive SSH disconnection.

Common Maven flags: `MAVEN_FLAGS="-e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true"`

```bash
$SSH_CMD "tmux new-session -d -s build 'export PATH=/opt/apache-maven-3.9.12/bin:\$PATH && \
  cd ~/azure-sdk-for-java/sdk/cosmos && \
  mvn $MAVEN_FLAGS -pl ,azure-cosmos -am clean install && \
  mvn $MAVEN_FLAGS -pl ,azure-cosmos-test clean install && \
  mvn $MAVEN_FLAGS -pl ,azure-cosmos-encryption clean install && \
  mvn $MAVEN_FLAGS -pl ,azure-cosmos-benchmark clean package -P package-assembly && \
  echo BUILD_COMPLETE'"
```

Monitor build progress:
```bash
$SSH_CMD "tmux capture-pane -t build -p | tail -20"
```

## 6. Verify Readiness

```bash
$SSH_CMD "echo '=== VM Check ==='; \
  java -version 2>&1 | head -1; \
  /opt/apache-maven-3.9.12/bin/mvn --version 2>&1 | head -1; \
  df -h / | tail -1; \
  ls ~/tenants.json 2>/dev/null && echo 'Config: ✅' || echo 'Config: ❌ MISSING'; \
  ls ~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/target/*jar-with-dependencies.jar 2>/dev/null \
    && echo 'JAR: ✅' || echo 'JAR: ❌ MISSING'; \
  cd ~/azure-sdk-for-java && echo \"Branch: \$(git rev-parse --abbrev-ref HEAD)  Commit: \$(git rev-parse --short HEAD)\""
```

Checklist:
- ✅ JDK 21 installed
- ✅ Maven 3.8.1+ installed
- ✅ Repo cloned (correct branch/PR/commit)
- ✅ Benchmark JAR built
- ✅ Config file present (tenants.json or single-tenant credentials)
- ✅ Disk space >10 GB free

## After Setup

Suggest: "Ready to run. Use the **cosmos-benchmark-run** skill to start a benchmark."
