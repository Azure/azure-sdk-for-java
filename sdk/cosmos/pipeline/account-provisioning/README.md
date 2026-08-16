# Cosmos live-test account provisioning

`New-CosmosLiveTestAccounts.ps1` provisions the fixed Cosmos DB accounts used by the
**azure-sdk-for-java** Cosmos live tests and outputs the single JSON document
stored in `sub-config-cosmos-azure-cloud-test-resources`, which the pipelines read.

- **`New-CosmosLiveTestAccounts.ps1`** — creates the resource group (if missing) + the
  accounts, and **outputs** the accounts JSON (endpoints + keys). It does not touch Key
  Vault; publish the JSON to the secret manually (see below).

## Why

The Java Cosmos live tests are moving off the central EngSys on-the-fly provisioner to
**fixed, self-owned accounts** (no service-connection / tenant dependency for the
key-based tests). Because the ephemeral tenant is deleted and recreated roughly every
**90 days**, this script is re-run after each rotation to recreate the accounts and
regenerate the fresh endpoints/keys.

## Files

| File | Purpose |
| --- | --- |
| `New-CosmosLiveTestAccounts.ps1` | Creates `sdk-ci` RG (if missing) + accounts; outputs accounts JSON. |
| `cosmos-live-test-accounts.definition.json` | Desired accounts (logical selector + config). |

An account entry may set `"regions": [...]` to pin its own regions when `regionDefaults` does not
suit it. `gsi-single-session` uses this to sit in **East US 2**, because
`live-gsi-platform-matrix.json` runs it single-region with `PREFERRED_LOCATIONS=["East US 2"]`, and a
preferred region the account does not have leaves the client with nothing to prefer.

The emitted JSON conforms to the schema at
`../live-test-accounts.schema.json`, which the pipeline pre-step
`../resolve-cosmos-test-account.sh` parses.

## Prerequisites

- PowerShell 7+
- Az modules: `Az.Accounts`, `Az.Resources`, `Az.CosmosDB`
- Contributor on the subscription that hosts `sdk-ci`
- Signed in: `Connect-AzAccount -Tenant <id> -Subscription <sub>`

## Usage

```powershell
# Create/refresh accounts and write the JSON to a file
# (contains keys - treat as secret, delete after publishing)
./New-CosmosLiveTestAccounts.ps1 -SubscriptionId <sub> -OutputPath ./accounts.json

# Dry run: create nothing, print the assembled JSON with keys stubbed
./New-CosmosLiveTestAccounts.ps1 -SubscriptionId <sub> -WhatIf
```

Idempotent: existing accounts are left in place and missing capabilities are added; the
JSON is regenerated with current endpoints/keys. A single run is enough for a fresh tenant -
capabilities are applied by ARM PATCH after the account exists, and verified, rather than being
passed to `New-AzCosmosDBAccount` (some Az.CosmosDB versions ignore `-Capabilities` silently,
which previously produced accounts with no capabilities and required a second run).

The multi-master accounts are separated by contention domain:

| Selector | Workload |
| --- | --- |
| `multimaster-multiregion-session` | General query/direct and multi-master tests |
| `multimaster-session-control` | Control-plane-heavy `fast` tests |
| `multimaster-session-http2` | HTTP/2 fast/query/circuit-breaker tests |
| `multimaster-session-circuit` | Flaky and circuit-breaker tests |
| `multimaster-multiregion-session-fi` | Fault-injection tests |
| `multimaster-multiregion-session-split` | Multi-master partition split tests |
| `single-session-cfp-split` | Single-region Session change-feed split tests |
| `single-strong-cfp-split` | Single-region Strong change-feed split tests |
| `multimaster-session-cfp-split` | Multi-master change-feed split tests |

Then **update the Key Vault secret / ADO variable manually** with the contents of
`accounts.json` (paste the JSON into the `sub-config-cosmos-azure-cloud-test-resources` secret).

## Rotation runbook

1. Ephemeral tenant is recreated (~every 90 days).
2. `Connect-AzAccount -Tenant <new-tenant-id> -Subscription <sub>`.
3. Run the account script (above) to recreate any missing `sdk-ci` accounts and
   regenerate `accounts.json`.
4. Manually update the `sub-config-cosmos-azure-cloud-test-resources` secret with the new JSON.
5. Java pipelines pick up the refreshed values on their next run — no YAML edits.

## Incident-response key rotation

If either key set may have been disclosed, do not wait for the next tenant rotation:

1. Cancel or pause active Cosmos live-test runs so no job continues using a key while it
   is being revoked.
2. Connect to the subscription and regenerate both master keys for every fixed account:

   ```powershell
   $subscriptionId = '<sub>'
   $resourceGroupName = 'sdk-ci'
   $accountNamePrefix = 'sdkci'
   $definitionPath = './cosmos-live-test-accounts.definition.json'

   Connect-AzAccount -Tenant <tenant-id> -Subscription $subscriptionId
   $definition = Get-Content -Raw $definitionPath | ConvertFrom-Json
   foreach ($account in $definition.accounts) {
       $accountName = ('{0}-{1}' -f $accountNamePrefix, $account.name).ToLowerInvariant()
       foreach ($keyKind in @('primary', 'secondary')) {
           New-AzCosmosDBAccountKey `
               -ResourceGroupName $resourceGroupName `
               -Name $accountName `
               -KeyKind $keyKind `
               -Confirm:$false | Out-Null
       }
   }
   ```

3. Regenerate the complete JSON without printing it to the terminal:

   ```powershell
   $accountsPath = Join-Path ([System.IO.Path]::GetTempPath()) 'cosmos-live-test-accounts.json'
   ./New-CosmosLiveTestAccounts.ps1 `
       -SubscriptionId $subscriptionId `
       -ResourceGroupName $resourceGroupName `
       -AccountNamePrefix $accountNamePrefix `
       -DefinitionPath $definitionPath `
       -OutputPath $accountsPath > $null
   ```

4. Immediately replace the `sub-config-cosmos-azure-cloud-test-resources` Key Vault
   secret / ADO variable with the contents of `$accountsPath`.
5. Delete the temporary file even if publication or validation fails:

   ```powershell
   Remove-Item -LiteralPath $accountsPath -Force
   ```

   Do not paste the JSON into terminals, chats, build logs, or incident notes. Remove any
   other copies created by shell transcripts, redirected output, or local backups.
6. Queue a fresh Cosmos live-test run, verify account resolution and key-dependent tests,
   and record the rotation in the incident timeline.
