---
name: cosmos-benchmark-provision
description: Provision Azure infrastructure for Cosmos DB benchmarks — create or reuse Cosmos DB accounts, Application Insights, and Azure VMs. Verifies region capacity before creating resources. Use when the user needs to create benchmark resources, reuse existing ones, provision infrastructure, or set up a DR drill. Triggers on "provision", "create accounts", "create VM", "create app insights", "setup infrastructure", "DR drill setup", "reuse existing".
---

# Provision Benchmark Infrastructure

Create or reuse Azure resources needed for a benchmark or DR drill. All resources must be in the **same region**.

## Before You Start

### 1. Choose region

Ask the user for the target Azure region. All resources will be co-located.

### 2. Verify capacity

**Check quotas before creating anything.** If any check fails, prompt the user to choose a different region.

```bash
# VM vCPU quota
az vm list-usage --location <region> -o table | grep -i "Standard DSv5"

# Cosmos DB account count (limit is typically 50 per subscription)
az cosmosdb list --query "length(@)"

# App Insights limit check (200 per subscription per region)
az monitor app-insights component list --query "[?location=='<region>'] | length(@)"
```

### 3. Choose create vs. reuse

For each resource type, ask the user:
- **Create new** — provision fresh resources
- **Reuse existing** — point to config files or discover from Azure

## 1. Cosmos DB Accounts

### Create new

Ask user for: count (N), naming prefix, consistency level (default: Session), throughput (default: 10000 RU/s).

```bash
# Create resource group (shared by all benchmark resources)
az group create --name rg-cosmos-benchmark --location <region>

# Create N accounts in parallel
for i in $(seq 0 $((N-1))); do
  az cosmosdb create \
    --resource-group rg-cosmos-benchmark \
    --name "${PREFIX}${i}" \
    --locations regionName=<region> failoverPriority=0 \
    --default-consistency-level Session \
    --kind GlobalDocumentDB &
done
wait

# Create database + container in each
for i in $(seq 0 $((N-1))); do
  az cosmosdb sql database create \
    --resource-group rg-cosmos-benchmark \
    --account-name "${PREFIX}${i}" \
    --name benchdb

  az cosmosdb sql container create \
    --resource-group rg-cosmos-benchmark \
    --account-name "${PREFIX}${i}" \
    --database-name benchdb \
    --name benchcol \
    --partition-key-path /id \
    --throughput 10000
done
```

### Export credentials

Generate `clientHostAndKey.txt` for the **setup** skill:

```bash
for i in $(seq 0 $((N-1))); do
  ENDPOINT=$(az cosmosdb show -g rg-cosmos-benchmark -n "${PREFIX}${i}" --query documentEndpoint -o tsv)
  KEY=$(az cosmosdb keys list -g rg-cosmos-benchmark -n "${PREFIX}${i}" --query primaryMasterKey -o tsv)
  echo "${PREFIX}${i},${ENDPOINT},${KEY}"
done > clientHostAndKey.txt
```

### Reuse existing

Ask user how to discover existing accounts:
- **Option A**: Point to an existing `clientHostAndKey.txt` file
- **Option B**: Discover from resource group:
  ```bash
  az cosmosdb list -g <rg> --query "[].{name:name, endpoint:documentEndpoint}" -o table
  ```
  Then export credentials using the loop above with the discovered account names.

## 2. Application Insights

### Create new

```bash
az monitor app-insights component create \
  --app <app-insights-name> \
  --location <region> \
  --resource-group rg-cosmos-benchmark \
  --kind web \
  --application-type web
```

### Get connection string

```bash
AI_CONN_STR=$(az monitor app-insights component show \
  --app <app-insights-name> \
  --resource-group rg-cosmos-benchmark \
  --query connectionString -o tsv)
echo "$AI_CONN_STR" > test-setup/app-insights-connection-string.txt
```

The **run** skill uses this via environment variable `APPLICATIONINSIGHTS_CONNECTION_STRING`.

### Reuse existing

Ask user how to discover:
- **Option A**: Provide the connection string directly
- **Option B**: Discover from resource group:
  ```bash
  az monitor app-insights component list -g <rg> --query "[].{name:name, connectionString:connectionString}" -o table
  ```

Save to `test-setup/app-insights-connection-string.txt`.

## 3. Azure VMs

### Create new (via provision script)

```bash
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --new --location <region> --create-key \
  [--size Standard_D16s_v5] \
  [--disk-size 256] \
  [--rg rg-cosmos-benchmark] \
  [--vm-name vm-benchmark-01] \
  [--skip-setup]
```

Script flags:
| Flag | Default | Description |
|---|---|---|
| `--new` | — | Create a new VM |
| `--location` | eastus | Azure region |
| `--create-key [path]` | — | Generate SSH key pair (optional path) |
| `--ssh-key <pub>` | — | Use existing public key |
| `--size` | Standard_D16s_v5 | VM SKU |
| `--disk-size` | 256 | OS disk in GB |
| `--rg` | rg-cosmos-benchmark | Resource group |
| `--vm-name` | vm-benchmark-01 | VM name |
| `--skip-setup` | false | Skip auto-running setup-benchmark-vm.sh |

### Create new (manual Azure CLI)

```bash
az vm create \
  --resource-group rg-cosmos-benchmark \
  --name vm-benchmark-01 \
  --image Ubuntu2204 \
  --size Standard_D16s_v5 \
  --accelerated-networking true \
  --admin-username benchuser \
  --generate-ssh-keys \
  --os-disk-size-gb 256 \
  --storage-sku Premium_LRS
az vm open-port --resource-group rg-cosmos-benchmark --name vm-benchmark-01 --port 22
```

### Reuse existing VM

```bash
# Option A: provide IP directly
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --existing --ip <VM_IP> --user benchuser --key ~/.ssh/id_rsa

# Option B: discover from resource group + VM name
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --existing --rg <rg> --vm-name <name> --key ~/.ssh/id_rsa
```

### Connection info saved

The provision script saves:
- `.vm-ip` — VM public IP
- `.vm-user` — SSH username
- `.vm-key` — path to SSH private key
- `test-setup/vm-config.env` — all three in `KEY=VALUE` format

Read `references/vm-sizing.md` for workload-specific VM sizing.

## 4. Resource Group Cleanup

When done with all benchmarks:
```bash
az group delete --name rg-cosmos-benchmark --yes --no-wait
```

## After Provisioning

Use the **cosmos-benchmark-setup** skill to:
- Install JDK/Maven/tools on the VM
- Generate `tenants.json` from `clientHostAndKey.txt`
- Clone repo and build the benchmark JAR

## References

- **VM sizing by workload**: `references/vm-sizing.md`
- **Provision script**: `sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh`
