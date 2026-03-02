---
name: cosmos-benchmark-setup-resources
description: Set up Azure resources for Cosmos DB benchmarks — create or reuse Cosmos DB accounts, Application Insights, and Azure VMs. Installs JDK/Maven on VMs. Validates region capacity before creating resources. Triggers on "provision", "create accounts", "create VM", "setup resources", "setup infrastructure", "DR drill setup", "reuse existing".
---

# Setup Resources

Create or reuse Azure resources needed for a benchmark or DR drill, and prepare the VM with build tools. This is the first step of the benchmark workflow.

All deterministic operations are implemented as scripts in `scripts/`. The agent's role is to gather user input, run the appropriate scripts, and report results.

## Step 1 — Config Directory

Ask the user where to persist resource configuration files (VM connection info, Cosmos DB credentials, App Insights connection strings). All downstream skills read from this directory.

```bash
CONFIG_DIR="<user-specified-path>"
mkdir -p "$CONFIG_DIR"
```

**⚠️ If the chosen path is inside the git repository**, warn the user:
> "This directory is inside the git repo. Files here contain credentials (keys, connection strings) and risk being committed. Consider using a path outside the repo, or ensure it is added to `.gitignore`."

If the user confirms a repo-internal path, add it to `.gitignore`:
```bash
echo "$CONFIG_DIR/" >> .gitignore
```

### Reuse existing config

Check if the config directory already has all expected files:

```bash
ls "$CONFIG_DIR"/vm-ip "$CONFIG_DIR"/vm-user "$CONFIG_DIR"/vm-key \
   "$CONFIG_DIR"/clientHostAndKey.txt \
   "$CONFIG_DIR"/app-insights-connection-string.txt 2>/dev/null
```

If all files are present → skip to the **setup benchmark** skill. If some are missing → create only the missing resources.

## Step 2 — Confirm Tenant and Subscription

```bash
az account show --query "{tenant:tenantId, subscription:name, subscriptionId:id}" -o table
```

Ask the user to confirm or switch:
- **Switch tenant**: `az login --tenant <tenant-id>`
- **Switch subscription**: `az account set --subscription <subscription-id>`
- **List subscriptions**: `az account list --query "[].{name:name, id:id, tenantId:tenantId}" -o table`

## Step 3 — Gather Resource Requirements

Ask the user what resources are needed. Defaults:

| Resource | Default | Customizable |
|---|---|---|
| Azure VM | 1 × Standard_D16s_v5 | Count, size, disk |
| Cosmos DB accounts | 1 | Count (N), naming prefix, consistency level |
| Application Insights | 1 | Name |
| Resource group | `rg-cosmos-benchmark-YYYYMMDD` (today's date) | Name |
| Region | `westus2` | Any Azure region |

For each resource type, ask: **Create new** or **Reuse existing**.

## Step 4 — Validate Capacity and Select Region

Run the validation script against the preferred region (default: `westus2`):

```bash
bash scripts/validate-capacity.sh \
  --region <preferred-region> \
  --vm-size <vm-size> \
  --vm-count <vm-count> \
  --cosmos-count <cosmos-count> \
  --app-insights-count 1 \
  --find-alternatives true
```

The script checks (with timestamped progress logging to stderr):
- Resource provider registration (auto-registers if needed)
- VM SKU availability (detects all restriction types: Zone, NotAvailableForSubscription)
- Alternative D-series SKUs with matching vCPU count (single API call, if `--find-alternatives true`)
- vCPU quota (dynamically resolves the quota family from the effective SKU)
- Cosmos DB account quota (subscription-wide limit of ~50)
- Application Insights quota (per-region limit of ~200)

**Output**: JSON summary with per-check pass/fail, `restriction_reason`, `suggested_alternative`, and messages.

### If preferred region fails

Run the region finder to automatically scan candidate regions:

```bash
bash scripts/find-region.sh \
  --preferred <preferred-region> \
  --vm-size <vm-size> \
  --vm-count <vm-count> \
  --cosmos-count <cosmos-count> \
  --fallback-regions "eastus,centralus,westeurope" \
  --stop-on-first true
```

The script uses a 4-phase search strategy:
1. Preferred region with exact SKU
2. Preferred region with similar SKUs (finds alternatives before giving up on the region)
3. Fallback regions with exact SKU
4. Fallback regions with similar SKUs

Progress is reported to stderr as each region is checked:
```
[1/9] westus2: ❌ Standard_D16s_v5 restricted (similar: Standard_D16ads_v7 available)
[2/9] eastus: ❌ not listed
[3/9] centralus: ✅ Standard_D16s_v5 available
```

**Output** (stdout): Line 1 = region, Line 2 = VM size (may differ if alternative), Line 3+ = JSON.

| Flag | Default | Description |
|---|---|---|
| `--fallback-regions` | (built-in list) | Comma-separated regions to try instead of defaults |
| `--stop-on-first` | `true` | Stop at first exact match; `false` checks all regions |

**Present the result to the user for confirmation** before proceeding.

## Step 5 — Create Resources

Once region and requirements are confirmed, run the orchestrator to create all resources in parallel:

```bash
bash scripts/provision-all.sh \
  --config-dir "$CONFIG_DIR" \
  --region <region> \
  --rg $RG \
  --cosmos-prefix <prefix> \
  --cosmos-count <N> \
  --app-insights-name <app-insights-name> \
  --vm-name vm-benchmark-01 \
  --vm-size Standard_D16s_v5 \
  --create-key
```

The orchestrator:
1. **Pre-flight capacity gate** — runs `validate-capacity.sh` to verify all resources are available in the region. **Blocks creation unless all checks pass** (override with `--skip-capacity-check`)
2. Creates the resource group
3. Launches **in parallel**: Cosmos DB accounts, App Insights, and VM creation
4. Waits for all three to complete (with elapsed time logging)
5. Exports Cosmos DB credentials to `$CONFIG_DIR/clientHostAndKey.txt`
6. Runs `verify-resources.sh` to confirm everything is ready

Each sub-task logs to `$CONFIG_DIR/logs/` for debugging if anything fails.

**Do not proceed to the next step until `provision-all.sh` exits with code 0.**

### Reuse existing resources

If the user wants to reuse existing resources instead of creating new ones, handle each resource type individually:

#### Cosmos DB — reuse

Point to an existing `clientHostAndKey.txt`, or discover accounts from a resource group:

```bash
bash scripts/export-cosmos-credentials.sh \
  --rg $RG --discover --config-dir "$CONFIG_DIR"
```

#### App Insights — reuse

Discover and save the connection string:

```bash
az monitor app-insights component list -g $RG \
  --query "[].{name:name, connectionString:connectionString}" -o table

# Save the chosen connection string
echo "<connection-string>" > "$CONFIG_DIR/app-insights-connection-string.txt"
```

#### VM — reuse

```bash
bash scripts/provision-benchmark-vm.sh \
  --existing --ip <VM_IP> --user benchuser --key ~/.ssh/id_rsa \
  --config-dir "$CONFIG_DIR"
```

### After reuse: verify

Always run the verification gate after reusing resources:

```bash
bash scripts/verify-resources.sh --config-dir "$CONFIG_DIR"
```

## Step 6 — Verification Gate

**This step is mandatory before proceeding.** If `provision-all.sh` was used, verification already ran. If resources were reused or created manually, run explicitly:

```bash
bash scripts/verify-resources.sh --config-dir "$CONFIG_DIR" [--cosmos-count <N>]
```

The script checks:
- ✅ Config directory exists with expected files
- ✅ `clientHostAndKey.txt` has the expected number of entries
- ✅ `app-insights-connection-string.txt` is non-empty
- ✅ VM is SSH-reachable
- ✅ JDK and Maven are installed on the VM

**Exit code 0 = proceed. Exit code 1 = fix issues first.**

## Resource Group Cleanup

When done with all benchmarks:
```bash
az group delete --name $RG --yes --no-wait
```

## After Setup Resources

Proceed to the **cosmos-benchmark-run** skill to:
- Clone the repo (at a specific branch/PR/commit) on the VM
- Generate `tenants.json` from `$CONFIG_DIR/clientHostAndKey.txt`
- Build the benchmark JAR
- Execute the benchmark

## Scripts Reference

| Script | Purpose |
|---|---|
| `scripts/provision-all.sh` | **Orchestrator.** Pre-flight capacity gate → RG → parallel Cosmos/AppInsights/VM → credentials → verify. `--skip-capacity-check` overrides gate. |
| `scripts/validate-capacity.sh` | Check region capacity (VM SKU, quotas, restrictions). Logs progress. Finds alternative SKUs. JSON output. |
| `scripts/find-region.sh` | 4-phase region search: exact→similar in preferred, then fallbacks. Supports `--fallback-regions`, `--stop-on-first`. |
| `scripts/create-cosmos-accounts.sh` | Create N Cosmos DB accounts with progress logging. |
| `scripts/export-cosmos-credentials.sh` | Export Cosmos DB credentials to `clientHostAndKey.txt`. |
| `scripts/provision-benchmark-vm.sh` | Create/connect VM, install tools (JDK/Maven/tmux), save config. |
| `scripts/verify-resources.sh` | **Gate.** Verify all resources are ready before proceeding. |
| `references/vm-sizing.md` | VM sizing recommendations by workload. |
