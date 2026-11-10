# Azure AI Search tests

Integration tests extend
[SearchTestBase](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/test/java/com/azure/search/documents/SearchTestBase.java).

| Mode | Behavior |
| --- | --- |
| `PLAYBACK` (default) | Replays published recordings through the local test proxy; no Azure credentials or resources are required. |
| `RECORD` | Calls real Azure resources and records HTTP traffic. |
| `LIVE` | Calls real Azure resources without recording. |

Mock-based tests use fake HTTP responses and need no Azure resources.
Disabled tests remain skipped in every mode; live-only tests are skipped in playback.

## Run existing tests in playback

From the repository root, with a working JDK/Maven:

```powershell
$env:AZURE_TEST_MODE = 'PLAYBACK'
mvn -B -ntp -f sdk\search\azure-search-documents\pom.xml test
```

The test framework restores recording assets using
[assets.json](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/assets.json).
You do not need to run RECORD first, supply client secrets, or copy recordings into the source tree.
Initial test-proxy setup and asset restoration can require network access, but playback does not call Azure services.

To run focused offline regressions:

```powershell
mvn -B -ntp -f sdk\search\azure-search-documents\pom.xml test `
  '-Dtest=SearchAuthenticationTests,SearchGaTests,SearchGaSamplesTests,PreviewApiTests,PreviewFeatureContractTests,FileIngestionPollingTests,GetDocumentOptionsTests'
```

## Real-service testing requires an isolated service

**Never point integration tests at production or shared bug-bash resources.**
RECORD and LIVE use the service configured in `SEARCH_SERVICE_ENDPOINT`; they do not create an isolated Search service.
Tests create, update and delete resources. In particular, `KnowledgeBaseTests` and `KnowledgeSourceTests` cleanup
enumerates knowledge bases and knowledge sources across that service and can delete resources the test did not create.
Do not run separate test processes against the same service.

Before an authorized real-service run, configure an isolated test environment and its permissions.
The [Search provisioning template](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/test-resources.bicep)
and [live-testing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md)
describe provisioning. Do not deploy, modify or clean up shared resources to satisfy a test prerequisite.

### Caller authentication

For both RECORD and LIVE,
[TestHelpers](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/test/java/com/azure/search/documents/TestHelpers.java)
uses `AzurePipelinesCredential` when `AZURESUBSCRIPTION_SERVICE_CONNECTION_ID` is configured, and
`AzureCliCredential` otherwise. Pipeline authentication also requires `AZURESUBSCRIPTION_CLIENT_ID`,
`AZURESUBSCRIPTION_TENANT_ID`, `SYSTEM_ACCESSTOKEN`, and the Azure Pipelines OIDC request configuration.
For local runs, authenticate with `az login` using the intended tenant/subscription.
Unrelated `AZURE_CLIENT_*` settings do not replace the CLI identity. No client secret is needed for this path.

The caller needs Search Service Contributor and Search Index Data Contributor on the isolated Search service.
Search separately authenticates to storage, OpenAI and AI Services; configure the identity and roles required by
the selected test fixture. Some file-ingestion fixtures explicitly use `SEARCH_USER_ASSIGNED_IDENTITY`.

### Fixture settings

Configure only the settings needed by the selected tests, in the same terminal that starts Maven.

| Fixture | Variables |
| --- | --- |
| Search | `SEARCH_SERVICE_ENDPOINT` |
| Azure OpenAI retrieval | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_DEPLOYMENT_NAME`, `SEARCH_OPENAI_MODEL_NAME` |
| Embeddings | `SEARCH_OPENAI_ENDPOINT`, `SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME`, `SEARCH_OPENAI_EMBEDDING_MODEL_NAME` |
| Blob storage | `SEARCH_STORAGE_ACCOUNT_NAME`, `SEARCH_STORAGE_CONTAINER_NAME`, `SEARCH_SUBSCRIPTION_ID`, `SEARCH_RESOURCE_GROUP` |
| AI Services / Content Understanding | `SEARCH_AI_SERVICES_ENDPOINT` |
| User-assigned fixture identity | `SEARCH_USER_ASSIGNED_IDENTITY` |

Deployments must already exist, networking must permit access, and each identity must have the appropriate roles.
For standard file extraction, use the AI Services endpoint (`https://<resource>.cognitiveservices.azure.com/`),
not a Foundry project URL. Search needs access to a resource configured for Content Understanding.

Model-activity tests send `messages` and request answer synthesis to exercise model-backed retrieval.
An intent-only request can bypass model planning, so a successful retrieval need not include model activity.

## Recording assets and validation limits

Record only against an authorized isolated environment, with `AZURE_TEST_MODE=RECORD` in the Maven process.
Use the repository's
[test-proxy asset workflow](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/testproxy/onboarding/README.md)
to review, sanitize and publish recordings, then update `assets.json`. Do not re-record to hide an SDK or test failure.

Passing playback verifies the SDK against recorded responses, not current backend behavior or every preview feature.
See the
[preview feature coverage matrix](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/preview-feature-coverage.md)
for the remaining service-validation gaps.
