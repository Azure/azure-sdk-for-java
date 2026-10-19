# Azure AI Search tests

Integration tests extend [SearchTestBase](java/com/azure/search/documents/SearchTestBase.java).
`RECORD` calls Azure and records HTTP traffic; `LIVE` calls Azure without recording; `PLAYBACK` uses existing
recordings and needs no Azure resources. Mock-based tests do not need resources or recordings.

## Local environment

From the repository root, copy the example once, then edit your private copy:

```powershell
Copy-Item ./sdk/search/azure-search-documents/Set-TestEnvironment.example.ps1 `
  ./sdk/search/azure-search-documents/Set-TestEnvironment.local.ps1
```

The [example](../../Set-TestEnvironment.example.ps1) contains only `$env:` assignments and works with Windows
PowerShell 5.1 or PowerShell 7. There is no dotenv loader, resource validation, or provisioning.
The local copy is git-ignored; do not overwrite an existing local configuration.
Uncomment and fill in only the settings needed by the tests you choose.

Run the local script and Maven in the **same PowerShell terminal** so Maven inherits the variables:

```powershell
az login
az account set --subscription '<subscription-id>'
& ./sdk/search/azure-search-documents/Set-TestEnvironment.local.ps1

# Run all package tests and record service calls.
$env:AZURE_TEST_MODE = 'RECORD'
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode test
```

The example defaults to `PLAYBACK`. Change the mode in your local script or override it after running the script.
Invoking the setup through a separate `powershell.exe` or `pwsh` process cannot change your current terminal's
environment. No API keys or client secrets are needed.

To run one test instead of the entire package:

```powershell
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode `
  '-Dtest=IndexManagementTests#createAndGetIndexReturnsCorrectDefinitionSync' test
```

## Resource settings

| Tests using | Variables |
| --- | --- |
| Search | `SEARCH_SERVICE_ENDPOINT` |
| Azure OpenAI retrieval | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_DEPLOYMENT_NAME`, `SEARCH_OPENAI_MODEL_NAME` |
| Embeddings | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME`, `SEARCH_OPENAI_EMBEDDING_MODEL_NAME` |
| Blob storage | `SEARCH_STORAGE_ACCOUNT_NAME`, `SEARCH_STORAGE_CONTAINER_NAME`, `SEARCH_SUBSCRIPTION_ID`, `SEARCH_RESOURCE_GROUP` |
| AI Services / Content Understanding | `SEARCH_AI_SERVICES_ENDPOINT` |

These are test variables, not necessarily the names used by runnable samples.
Deployments and resources must already exist, and networking must allow access.
Disabled tests remain skipped even in `RECORD` or `LIVE`.

## Authentication and permissions

[TestHelpers](java/com/azure/search/documents/TestHelpers.java) uses your `az login` identity for `RECORD` and `LIVE`.
Your computer does not need a managed identity. Search separately uses its own system-assigned managed identity
to access storage, OpenAI, and AI Services without keys.

| Principal | Resource | Role |
| --- | --- | --- |
| Your Azure CLI login | Search | Search Service Contributor and Search Index Data Contributor |
| Your Azure CLI login | Test storage, when tests manage source content | Storage Blob Data Contributor |
| Search system-assigned identity | Source storage | Storage Blob Data Reader |
| Search system-assigned identity | Azure OpenAI | Cognitive Services OpenAI User |
| Search system-assigned identity | AI Services | Cognitive Services User |

Configure these identities and roles on your existing resources. No Bicep deployment is needed for local setup.
The unchanged [provisioning template](../../../test-resources.bicep) uses user-assigned identity and does not configure
the system-assigned workflow described here.

**Use an isolated test service:** integration tests create, update, and delete resources, and some cleanup enumerates
knowledge resources on the service. Never use production resources or commit private configuration.

## Playback and recording assets

Run all tests without Azure calls:

```powershell
$env:AZURE_TEST_MODE = 'PLAYBACK'
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode test
```

Recordings use [assets.json](../../assets.json) and an external, git-excluded assets checkout.
The test framework normally starts the test proxy automatically. To inspect and publish recordings with the
[test-proxy CLI](../../../../../docs/test-proxy-migration.md#test-proxy-cli-commands):

```powershell
test-proxy config locate -a sdk/search/azure-search-documents/assets.json
# Inspect recordings and confirm secrets and private data are sanitized before pushing.
test-proxy push -a sdk/search/azure-search-documents/assets.json
```

Pushing requires assets-repository write access and updates the recording tag in `assets.json`.
Include that tag update in your PR, not private scripts or raw recordings.
