# Azure AI Search tests

Integration tests extend [SearchTestBase](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/test/java/com/azure/search/documents/SearchTestBase.java).
`RECORD` calls Azure and records HTTP traffic; `LIVE` calls Azure without recording; `PLAYBACK` uses existing
recordings and needs no Azure resources. Mock-based tests do not need resources or recordings.

## Local environment

From the repository root, copy the example once, then edit your private copy:

```powershell
Copy-Item ./sdk/search/azure-search-documents/Set-TestEnvironment.example.ps1 `
  ./sdk/search/azure-search-documents/Set-TestEnvironment.local.ps1
```

The [example](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/Set-TestEnvironment.example.ps1) contains only `$env:` assignments and works with Windows
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

### Standard file extraction

Set `SEARCH_AI_SERVICES_ENDPOINT` explicitly to the Foundry resource's AI Services endpoint
(`https://<resource>.cognitiveservices.azure.com/`), not its `services.ai.azure.com` endpoint or a project URL.
Use a Foundry resource of kind `AIServices` with Content Understanding configured in a supported region.
The Search service's managed identity needs `Cognitive Services User` access to that resource.
See the [file knowledge source prerequisites](https://learn.microsoft.com/azure/search/agentic-knowledge-source-how-to-file#prerequisites).

After editing the local environment script, run it again in the same terminal as Maven; an existing process
does not pick up changes to the script automatically. Standard extraction setup failures can return HTTP 502.
The tests already retry transient failures, so inspect the resource configuration before increasing retries.

### Model activity metadata

`KnowledgeBaseTests.retrievalActivityIncludesModelNameSync` and its async counterpart require a model-backed
retrieval: they send `messages` with medium reasoning effort and explicitly request `answerSynthesis` output.
Supplying `intents` bypasses model query planning, so an extractive retrieval can succeed without returning any
model activity or model name. Configuring a model on the knowledge base alone does not force it to be used.

The mock-based `SearchGaTests.retrievalDeserializesActivityModelMetadata` test independently verifies that both
clients preserve `model.modelName` and `model.deploymentId` in query-planning, answer-synthesis, and
web-summarization activity records without calling Azure.

## Authentication and permissions

[TestHelpers](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/test/java/com/azure/search/documents/TestHelpers.java) uses the federated pipeline identity when
`AZURESUBSCRIPTION_SERVICE_CONNECTION_ID` is configured, and your `az login` identity otherwise, for `RECORD` and `LIVE`.
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
The [streaming example](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseRetrievalStreamExample.java)
uses `DefaultAzureCredential` for Search and the Search service's system-assigned identity for Azure OpenAI.
Run `az login` locally and enable RBAC authentication on Search. Set `SEARCH_SERVICE_ENDPOINT`, `SEARCH_OPENAI_ENDPOINT`,
`SEARCH_OPENAI_DEPLOYMENT_NAME`, and `SEARCH_OPENAI_MODEL_NAME`; API keys are not required.
The unchanged [provisioning template](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/test-resources.bicep) uses user-assigned identity and does not configure
the system-assigned workflow described here.

**Use an isolated test service:** integration tests create, update, and delete resources, and some cleanup enumerates
knowledge resources on the service. Never use production resources or commit private configuration.

`KnowledgeBaseTests` and `KnowledgeSourceTests` share an exclusive JUnit resource lock because their cleanup
enumerates knowledge resources across the service. `SAME_THREAD` alone only serializes tests within each class;
it does not protect one class's shared knowledge source from the other class's cleanup. The lock does not
coordinate separate Maven processes, so do not run these classes in separate processes against the same service.

Analyzer tests create indexes concurrently. An HTTP 429 reporting the service's index quota is a resource-capacity
failure, not a serialization error. Retry with available capacity and bounded test parallelism. In playback,
such a response is replayed from the recording and is not a fresh Azure request.

`SearchIndexerClientBuilderTests.asyncListingHelpersPreserveContinuationPages` uses mock HTTP responses to
verify that the async data source, indexer, and skillset listing helpers preserve typed items and continuation
pages after regeneration or merge changes. It does not require Azure resources or recordings.

## Playback and recording assets

The indexer prefix-pagination tests use the recorded container name `test` in playback. Live and record runs
continue to use `SEARCH_STORAGE_CONTAINER_NAME`; no storage-container environment override is needed for playback.

Run all tests without Azure calls:

```powershell
$env:AZURE_TEST_MODE = 'PLAYBACK'
mvn -f sdk/search/azure-search-documents/pom.xml --batch-mode test
```

Recordings use [assets.json](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/assets.json) and an external, git-excluded assets checkout.
The test framework normally starts the test proxy automatically. To inspect and publish recordings with the
[test-proxy CLI](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/test-proxy-migration.md#test-proxy-cli-commands):

```powershell
test-proxy config locate -a sdk/search/azure-search-documents/assets.json
# Inspect recordings and confirm secrets and private data are sanitized before pushing.
test-proxy push -a sdk/search/azure-search-documents/assets.json
```

Pushing requires assets-repository write access and updates the recording tag in `assets.json`.
Include that tag update in your PR, not private scripts or raw recordings.
