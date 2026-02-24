---
name: multi-tenancy-benchmark-vm-provision
description: Provision and configure an Azure VM for running Cosmos DB multi-tenancy benchmarks. Use when the user needs to create a new benchmark VM, connect to an existing one, or install dependencies (JDK, Maven, async-profiler) on a VM. Triggers on "provision VM", "create VM", "setup VM", "connect to VM", "install JDK on VM", "benchmark VM".
---

# VM Provision

Create or connect to an Azure VM for benchmark execution, and install required dependencies.

## Capabilities

1. **Create a new Azure VM** with recommended specs
2. **Connect to an existing VM** and verify reachability
3. **Install dependencies** (JDK 21, Maven 3.9+, async-profiler, tmux)

## 1. Create a New VM

### Prerequisites

- Azure CLI (`az`) installed and logged in
- An SSH key pair (or use `--create-key` to generate one)

### Recommended VM Specs

| Setting | Value | Reason |
|---------|-------|--------|
| Size | Standard_D16s_v5 | 16 vCPUs, 64 GB RAM, good for 50+ tenants |
| OS | Ubuntu 22.04 LTS | Stable, well-supported |
| Disk | 256 GB Premium SSD | Room for heap dumps, logs |
| Accelerated Networking | Yes | Lower latency to Cosmos DB |
| Region | Same as Cosmos DB accounts | Minimize network latency |

### Script

Use the provision script at `sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh`:

```bash
# Create a new VM (generates SSH key if needed):
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --new --location eastus --create-key

# Create with custom settings:
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --new --location westus2 \
  --rg rg-cosmos-benchmark \
  --vm-name vm-benchmark-01 \
  --size Standard_D16s_v5 \
  --ssh-key ~/.ssh/id_rsa.pub
```

After creation, the script saves connection info to:
- `.vm-ip` / `.vm-user` / `.vm-key` (legacy locations)
- `test-setup/vm-config.env` (organized location)

### Manual creation via Azure CLI

```bash
az group create --name rg-cosmos-benchmark --location eastus
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

## 2. Connect to an Existing VM

### Via provision script

```bash
bash sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh \
  --existing --ip <VM_IP> --user benchuser --key ~/.ssh/id_rsa
```

### Manual SSH

```bash
ssh -i <key-path> benchuser@<VM_IP>
```

### Verify connectivity

```bash
ssh -i <key-path> benchuser@<VM_IP> 'echo "VM reachable"; uname -a'
```

### Find VM IP

If the VM was provisioned via Azure CLI:
```bash
az vm show -g rg-cosmos-benchmark -n vm-benchmark-01 -d --query publicIps -o tsv
```

Or check saved connection info:
```bash
cat test-setup/vm-config.env   # or .vm-ip
```

## 3. Install Dependencies

Run the setup script on the VM. This installs JDK 21, Maven 3.9+, async-profiler, and tmux.

### Via SSH

```bash
ssh -i <key-path> benchuser@<VM_IP> 'bash -s' < \
  sdk/cosmos/azure-cosmos-benchmark/scripts/setup-benchmark-vm.sh
```

### What gets installed

| Component | Version | Location |
|-----------|---------|----------|
| OpenJDK | 21 | System package |
| Maven | 3.9.12 | `/opt/apache-maven-3.9.12` |
| async-profiler | 3.0 | `/opt/async-profiler-3.0-linux-x64` |
| git, net-tools, sysstat, tmux | latest | System packages |

### Verify installation

```bash
ssh -i <key-path> benchuser@<VM_IP> \
  'java -version 2>&1 | head -1; mvn --version 2>&1 | head -1; tmux -V; df -h / | tail -1'
```

Expected output:
```
openjdk version "21.x.x"
Apache Maven 3.9.12
tmux 3.2a
/dev/sda1       246G   xxG   xxxG  xx% /
```

## After Provisioning

Once the VM is ready, use the `multi-tenancy-benchmark-setup` skill to:
- Generate and copy `tenants.json` to the VM
- Clone the SDK repo and build the benchmark JAR
- Verify all prerequisites

## Reference Scripts

- **Provision**: `sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh`
- **Setup (on-VM)**: `sdk/cosmos/azure-cosmos-benchmark/scripts/setup-benchmark-vm.sh`
