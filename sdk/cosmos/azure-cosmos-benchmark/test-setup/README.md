# Test Setup Files

This directory contains configuration files needed to run multi-tenancy benchmarks.

## Files

| File | Purpose | Checked in? |
|------|---------|-------------|
| `tenants-sample.json` | Template for tenant configuration | Yes (reference) |
| `tenants.json` | Actual tenant config with endpoints and keys | No (gitignored) |
| `clientHostAndKey.txt` | Raw credentials CSV used to generate tenants.json | No (gitignored) |
| `vm-config.env` | VM connection info (IP, user, key path) | No (gitignored) |

## Setup Steps

1. Copy `tenants-sample.json` to `tenants.json`
2. Fill in your Cosmos DB account endpoints and master keys
3. Optionally create `clientHostAndKey.txt` in CSV format:
   ```
   <account-name>,<endpoint>,<master-key>
   ```
   Then use the benchmark setup skill to auto-generate `tenants.json` from it.

## VM Connection

After provisioning a benchmark VM, save connection info:
```bash
# Created by provision-benchmark-vm.sh
VM_IP=<ip>
VM_USER=benchuser
VM_KEY_PATH=~/.ssh/id_rsa
```

## Deploying to VM

Copy setup files to the VM:
```bash
scp -i $VM_KEY_PATH test-setup/tenants.json $VM_USER@$VM_IP:~/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/tenants.json
```
