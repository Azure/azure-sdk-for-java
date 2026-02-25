# Release History

## 2.0.0-beta.1 (Unreleased)

### Features Added

- Added `getOpenAIClient` methods to obtain an instance of the Stainless OpenAI client
- Added documentation on how to get an `AgentsClient`
- Added `buildOpenAIClient()` and `buildOpenAIAsyncClient()` methods to `AIProjectClientBuilder` for directly obtaining an OpenAI client instance
- Added `FoundryFeaturesOptInKeys` enum for preview feature opt-in flags (e.g., `EVALUATIONS_V1_PREVIEW`, `SCHEDULES_V1_PREVIEW`, `RED_TEAMS_V1_PREVIEW`, `INSIGHTS_V1_PREVIEW`, `MEMORY_STORES_V1_PREVIEW`)
- Added `ModelSamplingParams` and `AzureAIModelTarget` models

### Breaking Changes

- Updated service version from `2025-11-15-preview` to `v1`
- Renamed `AgenticIdentityCredentials` to `AgenticIdentityPreviewCredentials`
- Renamed `AgentClusterInsightsRequest` to `AgentClusterInsightRequest`
- `ConnectionType.REMOTE_TOOL` value changed to `RemoteTool_Preview`
- `CredentialType.AGENTIC_IDENTITY` renamed to `AGENTIC_IDENTITY_PREVIEW`
- `ConnectionType.APIKEY` renamed to `API_KEY`
- `EvaluationsClient.getOpenAIClient()` renamed to `getEvalService()`
- `BlobReference.getBlobUri()` renamed to `getBlobUrl()`
- `HumanEvaluationRuleAction` renamed to `HumanEvaluationPreviewRuleAction`
- `EvaluationComparisonRequest` renamed to `EvaluationComparisonInsightRequest`; `EvaluationCompareReport` renamed to `EvaluationComparisonInsightResult`
- `EvaluationRunClusterInsightsRequest` renamed to `EvaluationRunClusterInsightRequest`
- Credential model classes dropped the plural suffix (e.g., `ApiKeyCredentials` → `ApiKeyCredential`, `EntraIdCredentials` → `EntraIdCredential`, `SasCredentials` → `SasCredential`, `BaseCredentials` → `BaseCredential`, `NoAuthenticationCredentials` → `NoAuthenticationCredential`)
- Methods across sub-clients were renamed to include the resource name for disambiguation:
  - `DeploymentsClient`: `get()` → `getDeployment()`, `list()` → `listDeployments()`
  - `InsightsClient`: `generate()` → `generateInsight()`, `get()` → `getInsight()`, `list()` → `listInsights()`
  - `RedTeamsClient`: `get()` → `getRedTeam()`, `list()` → `listRedTeams()`, `create()` → `createRedTeamRun()`
  - `SchedulesClient`: `delete()` → `deleteSchedule()`, `createOrUpdate()` → `createOrUpdateSchedule()`, `getRun()` → `getScheduleRun()`, `listRuns()` → `listScheduleRuns()`
  - `EvaluationRulesClient`: `get()` → `getEvaluationRule()`, `list()` → `listEvaluationRules()`, `delete()` → `deleteEvaluationRule()`, `createOrUpdate()` → `createOrUpdateEvaluationRule()`
  - `EvaluationTaxonomiesClient`: `get()` → `getEvaluationTaxonomy()`, `list()` → `listEvaluationTaxonomies()`, `create()` → `createEvaluationTaxonomy()`, `update()` → `updateEvaluationTaxonomy()`, `delete()` → `deleteEvaluationTaxonomy()`
  - `IndexesClient`: `createOrUpdate()` → `createOrUpdateVersion()`
  - `DatasetsClient`: `listLatest()` → `listLatestVersion()`

### Bugs Fixed

- Fixed base URL construction in `AIProjectClientBuilder` to append `/openai/v1` directly, removing dependency on `AzureOpenAIServiceVersion` and `AzureUrlPathMode` for URL path resolution

### Other Changes

- Updated version of `openai` client library to `4.14.0`
- Generated from latest v2 API spec
- `openai-java-client-okhttp` and `openai-java-core` modules are now `transitive` dependencies in `module-info.java`
- `com.azure.ai.agents` is now a required module in `module-info.java`

## 1.0.0-beta.3 (2025-11-12)

### Features Added

- Added OpenAI's official library for `Evaluations` subclients
- New sub-clients: `EvaluationRules`, `EvaluationTaxonomies`, `Evaluators`, `Insights`, `Schedules`

### Breaking Changes

- Agents subclients where split into their own package `azure-ai-agents`
- `Telemetry` and `Inference` subclient was removed.

### Bugs Fixed

- Projects Client: Get Azure OpenAI should use the parent resource unless caller provides a connection name

## 1.0.0-beta.2 (2025-06-17)

### Features Added

- Added `InferenceClient` with `getOpenAIClient` making use of stainless openai-java.
- Added support for building `PersistentAgentsClient` from `AIProjectClientBuilder`

## 1.0.0-beta.1 (2025-05-15)

- Initial release of Azure AI Projects client library for Java.

### Features Added

- Added support for Azure AI Projects client library for Java.
