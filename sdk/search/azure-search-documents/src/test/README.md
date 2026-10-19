# Azure AI Search tests

Integration tests extend [SearchTestBase](java/com/azure/search/documents/SearchTestBase.java), which uses the Azure SDK
test proxy. Recording makes real Azure calls; playback uses assets referenced by [assets.json](../../assets.json).
Mock-based tests such as `SearchGaTests`, `SearchGaSamplesTests`, and `SearchListingTests` do not need Azure resources
or recordings. Samples have separate environment-variable names; use the test settings below when running Maven tests.

## Configure your PowerShell environment

Requires [PowerShell 7+](https://learn.microsoft.com/powershell/scripting/install/installing-powershell).
From the repository root, copy the blank template once and edit your private copy:

```powershell
Copy-Item ./sdk/search/azure-search-documents/.env.example ./sdk/search/azure-search-documents/.env
```

Do not overwrite an existing configuration. The package `.env` is already git-ignored. Never commit secrets or paste
them into issues, logs, or chat. Other filenames are not necessarily git-ignored.

Fill in only the resource settings used by the tests you plan to run, then load the file:

```powershell
& ./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode RECORD
```

Alternatively, set variables directly in your current PowerShell terminal:

```powershell
az login
az account set --subscription 'your-subscription-id'
$env:SEARCH_SERVICE_ENDPOINT = 'https://your-isolated-test-service.search.windows.net'

& ./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode RECORD
```

Existing process variables take precedence over dotenv entries. Use `-OverwriteExisting` to intentionally load file
values over the current environment, or `-EnvFile /path/to/private.env` to select another file. An explicitly selected
file must exist; if the default package `.env` is absent, your existing `$env:` values are left in place.
`-Mode` overrides `AZURE_TEST_MODE` from either source; without it, that variable is used, defaulting to `PLAYBACK`.

The helper accepts blank lines, whole-line `#` comments, optional `export NAME=VALUE` prefixes, and single-line
`NAME=VALUE` assignments with optional matching single or double quotes. Values are literal: `$env:`, `${...}`,
command substitutions, backslash escapes, and inline comments are not expanded or interpreted. Duplicate names,
malformed lines and invalid test modes are errors. Parsing happens before any
environment variables are changed, and values are never printed.

**Run the helper and Maven in the same PowerShell process.** Calling `pwsh -File Set-TestEnvironment.ps1` from another
shell cannot change the parent shell's environment. The helper does not provision resources, log in, call Azure,
select test methods, or run Maven.

## Resource settings

Set `SEARCH_SERVICE_ENDPOINT` for your real service when running `RECORD` or `LIVE`.
`PLAYBACK` does not require resource settings.

| Tests using | Variables |
| --- | --- |
| `Search` | `SEARCH_SERVICE_ENDPOINT`: HTTPS endpoint of a dedicated Search test service. No admin API key is read by the shared test builders. |
| `OpenAI` | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_DEPLOYMENT_NAME`, `SEARCH_OPENAI_MODEL_NAME`: the model resource, existing deployment, and its model name. |
| `Embeddings` | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME`, `SEARCH_OPENAI_EMBEDDING_MODEL_NAME`: the existing embedding deployment and matching model. |
| `Storage` | `SEARCH_STORAGE_ACCOUNT_NAME`, `SEARCH_STORAGE_CONTAINER_NAME`, `SEARCH_SUBSCRIPTION_ID`, `SEARCH_RESOURCE_GROUP`: the storage account/container and their subscription/resource group, used to build a resource-ID connection string. |
| `AiServices` | `SEARCH_AI_SERVICES_ENDPOINT`: the AI Services/Foundry endpoint used by standard knowledge source extraction and Content Understanding skillset tests. No API key is used. |

The helper only imports values; it does not validate resource settings or restrict which variables you can load.
Make sure the settings used by your chosen tests identify real resources, not the test classes' playback defaults.

### Authentication and permissions

There are two independent authentication paths:

1. **Your test-runner login:** run `az login` on your computer and select the appropriate subscription.
   Both `RECORD` and `LIVE` use `AzureCliCredential`, so Maven uses your Azure CLI login and its RBAC permissions.
   Your computer does not need a managed identity. Client-secret, certificate, and workload-identity environment
   variables are not used by this credential and do not replace your CLI login.
2. **Search service identity:** enable system-assigned identity on the Search service. Model/vectorizer and Blob
   configurations omit a user-assigned identity selector; AI Services configurations omit API keys. Content Understanding
   skillsets use `AIServicesAccountIdentity` with no identity selector.

A system-assigned identity **is** a managed identity, but only the Search service needs one for the outbound
connections in these tests. Laptops and Codespaces can run live/record tests using `az login`, provided the signed-in
principal has the necessary roles and can reach the Search endpoint. On an Azure-hosted runner you can also use
`az login --identity` before running Maven; the tests still acquire tokens through Azure CLI.

According to [TestHelpers.getTestTokenCredential](java/com/azure/search/documents/TestHelpers.java):

- `RECORD` and `LIVE` use `AzureCliCredential` and fail if the CLI login is unavailable or lacks RBAC.
- `PLAYBACK` uses a mock credential and needs no Azure login.

Configure the following RBAC assignments before recording:

| Principal | Resource | Role |
| --- | --- | --- |
| Your Azure CLI login identity | Search service | Search Service Contributor and Search Index Data Contributor |
| Your Azure CLI login identity | Test storage account | Storage Blob Data Contributor when tests manage source content |
| Search system-assigned identity | Source storage account | Storage Blob Data Reader |
| Search system-assigned identity | Azure OpenAI account | Cognitive Services OpenAI User |
| Search system-assigned identity | AI Services/Foundry account | Cognitive Services User |

For local testing against existing resources, configure the Search service's system-assigned identity and roles
above in Azure. No Bicep deployment is required to import environment variables or run Maven.
Model and embedding deployments must already be available as configured; provisioning may require model quota.
RBAC propagation and private-network reachability must also be satisfied. See
[resource provisioning instructions](../../../../../eng/common/TestResources/README.md).
The existing [resource template](../../../test-resources.bicep) is unchanged and provisions user-assigned identity.
It does not set up the system-assigned configuration described here; use your existing resources with the roles
above. The shared provisioning scripts can create a service principal by default, but no generated application
secret is needed by these CLI-authenticated tests.
Additional specialized tests may need tenant-specific or private-network setup, and disabled tests remain disabled.
Offline fake-key and user-assigned-identity model tests remain to cover the SDK's public APIs; they do not use real keys
or authenticate against Azure.

**Use an isolated test service.** Integration tests create, update, and delete resources. Some cleanup enumerates
knowledge resources on the service. Do not run against a shared or production service.

## Record selected tests

Start with a Search-only test from the repository root:

```powershell
& ./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode RECORD
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode `
  '-Dtest=IndexManagementTests#createAndGetIndexReturnsCorrectDefinitionSync' test
```

For model-backed knowledge tests, fill in the model, embedding, storage, and AI Services settings actually
used by the selected methods. For example, to validate the combined configuration before running source tests:

```powershell
& ./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode RECORD
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode '-Dtest=KnowledgeSourceTests' test
```

You can narrow a class to a method with Maven's `-Dtest=ClassName#methodName` selector.
The test proxy normally downloads and starts automatically for local runs.

## Verify playback and publish recordings

Switch modes in the same terminal and run the same selector:

```powershell
& ./sdk/search/azure-search-documents/Set-TestEnvironment.ps1 -Mode PLAYBACK
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode '-Dtest=KnowledgeSourceTests' test
```

Recordings are stored in the git-excluded assets checkout, not copied into this SDK's test resources.
With the [test-proxy CLI](../../../../../docs/test-proxy-migration.md#test-proxy-cli-commands) available:

```powershell
test-proxy config locate -a sdk/search/azure-search-documents/assets.json
# Inspect the changed recordings and verify secrets and private data are sanitized before pushing.
test-proxy push -a sdk/search/azure-search-documents/assets.json
```

Pushing requires assets-repository write access and updates the recording tag in `assets.json`; include that update
in the SDK PR, not the private `.env` file or raw recordings.

## Validate the environment helper offline

```powershell
pwsh -NoProfile -File ./sdk/search/azure-search-documents/src/test/scripts/Set-TestEnvironment.Tests.ps1
```

These tests use temporary dummy configuration and restore process environment variables afterward. They do not
authenticate or make Azure calls.
