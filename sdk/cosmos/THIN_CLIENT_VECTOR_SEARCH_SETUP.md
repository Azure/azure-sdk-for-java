
Cosmos DB Test Account Setup — Thin Client + Vector Search


OVERVIEW

Test accounts require these steps in this order:
  (0) Create the Cosmos DB account(s) in region(s) where thin proxy is available
  (1) Thin Client Proxy support — via Compute federation migration
  (2) NoSQL Vector Search — via account capability enablement
  (3) Update Azure Key Vault secrets so CI pipelines use the new accounts
  (4) Update local test config (cosmos-v4.properties) if running tests locally


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 0: CREATE COSMOS DB ACCOUNTS IN SUPPORTED REGIONS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Before setting up thin client proxy, you need Cosmos DB accounts deployed in regions where the thin proxy service is available. Not all regions/federations have thin client enabled.

Step 1 — Identify regions with thin client support

Run this Kusto query to find which federations (and thus regions) have thin proxy enabled:

  ConfigurationTrace
  | where Key contains "IsThinClientEnabled"
  | where TIMESTAMP > ago(1d)
  | where Value == "True"
  | distinct FederationId

Use the best available regions from the first 3 deployment batches, as they get the latest builds/fixes.

Step 2 — Create the accounts

Create accounts in the Azure Portal or via CLI, deployed to regions where thin proxy is available.

For testing, you'll typically need two types of accounts:
  • Multi-region account (single-writer, multi-region reads) — e.g., East US 2 + Central US
  • Multi-writer account (multi-region writes enabled) — e.g., East US 2 + Central US

Example via CLI:

  az cosmosdb create --name "<account-name>" --resource-group "<resource-group>" --locations regionName="East US 2" failoverPriority=0 --locations regionName="Central US" failoverPriority=1 --default-consistency-level "Session"

For multi-writer:

  az cosmosdb create --name "<account-name>" --resource-group "<resource-group>" --locations regionName="East US 2" failoverPriority=0 --locations regionName="Central US" failoverPriority=1 --default-consistency-level "Session" --enable-multiple-write-locations true

Important: Choose regions that have thin-client-enabled federations (confirmed via Kusto query above). For example, East US 2 has cdb-ms-prod-eastus2-fe50 with thin client enabled.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 1: THIN CLIENT PROXY SETUP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Prerequisites:
  • Access to ACIS portal — https://portal.microsoftgeneva.com
  • Access to Kusto (cdbsupport) — https://cdbsupport.kusto.windows.net/Support
  • Contacts for federation issues: Gary Fang, Anya Robinson, Sumant Bhardvaj



Step 1 — Find a thin-client-enabled federation

Run this query in Kusto (cdbsupport):

  ConfigurationTrace
  | where Key contains "IsThinClientEnabled"
  | where TIMESTAMP > ago(1d)
  | where Value == "True"
  | distinct FederationId

To check a specific region (e.g., East US 2):

  ConfigurationTrace
  | where Key contains "IsThinClientEnabled"
  | where TIMESTAMP > ago(1d)
  | where FederationId contains "eastus2"
  | distinct FederationId, Key, Value, TIMESTAMP
  | take 10

Known working federation: cdb-ms-prod-eastus2-fe50 (East US 2)


Step 2 — Migrate the account to Compute

  • Open ACIS and run: "Migrate SQL account to Compute and Swap Endpoints" (Ref: SQL on Compute tenant migration | Azure Cosmos DB Team Docs)
  • Set the target federation (e.g., cdb-ms-prod-eastus2-fe50) (Ref: Migrating an account between Compute federations | Azure Cosmos DB Team Docs)
  • Repeat for EACH region the account is deployed in (e.g., East US 2 + Central US)
  • No Zonal/Regional checkboxes should be needed if the federation audience is already configured


Step 3 — If migration fails with audience/capabilities error

You may see this error:

  "The account has capabilities EnableSql. Please check the audiences,
   policies, and capabilities of the destination federation."

Action: Ask the Thin Client team (Anya Robinson / Sumant Bhardvaj / Chukang) to add your subscription to the destination federation's audience. This must be done for each region where the account is deployed.


Step 4 — Verify thin client works

After migration, the account should reach the thin proxy endpoint. The config IsThinClientEnabled = true is set at the federation level — no per-account config change is needed for public endpoint access.

Reference configs (set at federation level, NOT per-account):
  • IsThinClientEnabled = true  →  Required
  • enableThinClientPublicEndpoint = true (default)  →  For direct proxy access

Important: The Compute-rerouting configs (thinClientRouteViaProxy, thinClientEnableNonReadRequestRouting, etc.) are for Gateway-team internal testing only. SDK CI tests hit the thin proxy endpoint directly and do NOT need these.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 2: ENABLE VECTOR SEARCH (needed to pass our thin-client live tests)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Do this AFTER thin client proxy is working.


Step 1 — Login to the ephemeral tenant

  az login --tenant "<tenant-id>" 

  az account set --subscription "<subscription-id>"

If the subscription isn't visible, make sure you're logging into the correct tenant using --tenant.


Step 2 — Enable the Vector Search capability

Option A (requires subscription set in CLI context):

  az cosmosdb update --resource-group "<resource-group>" --name "<cosmos-account-name>" --capabilities EnableNoSQLVectorSearch

Option B (full resource ID, no subscription context needed):

  az cosmosdb update --ids "/subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.DocumentDB/databaseAccounts/<cosmos-account-name>" --capabilities EnableNoSQLVectorSearch


Step 3 — Wait 5–10 minutes for propagation


Step 4 — Verify

  az cosmosdb show --resource-group "<resource-group>" --name "<cosmos-account-name>" --query "capabilities" --output table

You should see EnableNoSQLVectorSearch in the output.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 3: UPDATE AZURE KEY VAULT SECRETS + PIPELINE MAPPING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

The CI pipelines pull account credentials from Azure Key Vault. When you create or replace test accounts, you must update the corresponding Key Vault secrets. The pipeline variable group is linked here:
  https://dev.azure.com/azure-sdk/internal/_library?itemType=VariableGroups

How it works:
  • Azure Key Vault is the SOURCE of truth for secrets
  • Pipeline variable groups READ from Key Vault automatically
  • You do NOT need to edit YAML to change which account is used — just update the secret value in Key Vault
  • For new secret names, you'll need Wes' help to add the Key Vault → pipeline variable mapping
  • Reusing/updating existing secret names is preferred ("secret hijacking") to avoid mapping changes

Current pipeline secrets and their usage (from tests.yml):

  Pipeline Test                              Pipeline Variables (from Key Vault)
  ─────────────────────────────────────────  ──────────────────────────────────────────────────
  Cosmos_Live_Test_ThinClient                $(thinclient-test-endpoint)
                                             $(thinclient-test-key)

  Cosmos_Live_Test_ThinClient_MultiRegion    $(thin-client-canary-multi-region-session-endpoint)
                                             $(thin-client-canary-multi-region-session-key)

  Cosmos_Live_Test_ThinClient_MultiMaster    $(thin-client-canary-multi-writer-session-endpoint)
                                             $(thin-client-canary-multi-writer-session-key)

Current account → secret mapping:

  Account: thin-client-multi-region
    → Update Key Vault secret: thin-client-canary-multi-region-session-endpoint  (set to account endpoint)
    → Update Key Vault secret: thin-client-canary-multi-region-session-key       (set to account primary key)

  Account: thin-client-multi-writer
    → Update Key Vault secret: thin-client-canary-multi-writer-session-endpoint  (set to account endpoint)
    → Update Key Vault secret: thin-client-canary-multi-writer-session-key       (set to account primary key)

  For thinclient-test-endpoint / thinclient-test-key:
    → Can reuse one of the above accounts (e.g., thin-client-multi-writer)

Steps to update:
  1. Get the endpoint and primary key for the new account (Azure Portal or CLI)
  2. Go to the Azure Key Vault that backs the pipeline variable group
  3. Update the secret VALUES for the names listed above
  4. No YAML changes needed if reusing existing secret names
  5. Pipeline will pick up new values on next run
  6. Pipeline will fail until Parts 0–2 are complete (account created, Compute migrated, vector search enabled)

Note: If you need entirely NEW secret names (not reusing existing ones), contact Wes to update the Key Vault → pipeline variable group mapping.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 4: CONFIGURE LOCAL TESTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Update cosmos-v4.properties with the account endpoint and key:

  ACCOUNT_HOST: https://<cosmos-account-name>.documents-test.windows-int.net:443/
  ACCOUNT_KEY: <primary-key>

Note: If deploying a NEW account via the ARM template (test-resources.json), EnableNoSQLVectorSearch is already declared in the template. However, account creation in supported regions (Part 0) and thin client proxy setup via Compute migration (Part 1) must still be done manually.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CURRENT TEST ACCOUNTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  • thin-client-multi-region  →  East US 2 + Central US  →  Federation: eastus2-fe50  →  Sub: bf8b935b-5f34-...
  • thin-client-multi-writer  →  East US 2 + Central US  →  Federation: eastus2-fe50  →  Sub: bf8b935b-5f34-...


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
TROUBLESHOOTING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  • ACIS migration fails with audience error
    → Ask Anya/Sumant/Chukang to add subscription to federation audience

  • Subscription not found in az login
    → Login with --tenant "<tenant-id>"

  • Vector search error after enabling capability
    → Wait 10+ min, then re-run tests

  • Thin client not working post-migration
    → Verify federation has IsThinClientEnabled = True via Kusto query

  • Need thin client in a new region
    → Check Kusto for enabled federations in that region first

  • Account created in a region without thin proxy support
    → Either migrate the account to a supported region or create a new account in a supported region (see Part 0)

  • Pipeline fails with auth errors after account swap
    → Verify Key Vault secrets were updated with new endpoint + key (see Part 3)
    → Ensure Parts 0–2 are complete before expecting pipeline to pass

  • Need new Key Vault → pipeline variable mapping
    → Contact Wes; prefer reusing existing secret names to avoid this


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CONTACTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  • Thin Client / Federation migrations  →  Gary Fang, Anya Robinson, Sumant Bhardvaj
  • Federation audience changes  →  Anya Robinson, Chukang
