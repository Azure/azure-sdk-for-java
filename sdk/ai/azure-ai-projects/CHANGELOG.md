# Release History

## 2.0.0 (Unreleased)

### Features Added

- Added `getDefaultConnection(ConnectionType, boolean)` to `ConnectionsClient` and `ConnectionsAsyncClient` for retrieving the default connection of a given type.
- Added `connectionName` parameter overloads to `createDatasetWithFile` and `createDatasetWithFolder` in `DatasetsClient` and `DatasetsAsyncClient`, allowing users to specify which Azure Storage Account connection to use for uploads.

### Breaking Changes

- Methods across sub-clients were renamed to include the resource name for disambiguation (continuing the pattern from `2.0.0-beta.1`):
  - `DatasetsClient`: `listLatestVersion()` → `listLatestDatasetVersions()`, `listVersions()` → `listDatasetVersions()`, `deleteVersion()` → `deleteDatasetVersion()`, `createOrUpdateVersion()` → `createOrUpdateDatasetVersion()`
  - `IndexesClient`: `listLatest()` → `listLatestIndexVersions()`, `listVersions()` → `listIndexVersions()`, `getVersion()` → `getIndexVersion()`, `createOrUpdateVersion()` → `createOrUpdateIndexVersion()`, `deleteVersion()` → `deleteIndexVersion()`
  - `EvaluatorsClient`: `createVersion()` → `createEvaluatorVersion()`, `getVersion()` → `getEvaluatorVersion()`, `updateVersion()` → `updateEvaluatorVersion()`, `deleteVersion()` → `deleteEvaluatorVersion()`, `listVersions()` → `listEvaluatorVersions()`, `listLatestVersions()` → `listLatestEvaluatorVersions()`
  - Same renames apply to the corresponding async clients.
- `DatasetVersion.getDataUri()` / `setDataUri()` renamed to `getDataUrl()` / `setDataUrl()` (also on `FileDatasetVersion` and `FolderDatasetVersion`).
- `DatasetsClient.createDatasetWithFolder()` no longer throws checked `IOException`; it now throws `UncheckedIOException` instead.

### Bugs Fixed

- Fixed `createDatasetWithFolder` producing an invalid `dataUri` that caused a 400 error when registering the dataset.
- Fixed `createDatasetWithFile` using the dataset name as the blob name instead of the actual file name.

### Other Changes

## 2.0.0-beta.3 (2026-03-19)

### Features Added

- Added `generateInsight(Insight, FoundryFeaturesOptInKeys)` convenience method to `InsightsClient` and `InsightsAsyncClient`.

### Breaking Changes

- `FoundryFeaturesOptInKeys` changed from an `ExpandableStringEnum`-based class to a standard Java `enum` type. The `values()` method now returns an array instead of a `Collection`, and the deprecated no-arg constructor is removed.
- The `timeZone` property in `RecurrenceTrigger` changed from `String` to `java.util.TimeZone`.
- Removed `EvaluationsClient` and `EvaluationsAsyncClient`. Use `builder.buildOpenAIClient().evals()` (returns `EvalService`) and `builder.buildOpenAIAsyncClient().evals()` (returns `EvalServiceAsync`) from the Stainless OpenAI SDK directly. The corresponding `buildEvaluationsClient()` and `buildEvaluationsAsyncClient()` methods on `AIProjectClientBuilder` have also been removed.
- `InsightsClient` and `InsightsAsyncClient` no longer auto-set the `Foundry-Features: Insights=V1Preview` header. The `FoundryFeaturesOptInKeys` parameter must now be passed explicitly to `generateInsight()`, `getInsight()`, and `listInsights()` overloads that require it.
- `getInsight(String, Boolean)` overload removed; replaced by `getInsight(String)` and `getInsight(String, FoundryFeaturesOptInKeys, Boolean)`.
- `listInsights(InsightType, String, String, String, Boolean)` signature changed to `listInsights(FoundryFeaturesOptInKeys, InsightType, String, String, String, Boolean)`.

## 2.0.0-beta.2 (2026-03-04)

### Breaking Changes

- Renamed `Index` model to `AIProjectIndex` across the SDK; all `IndexesClient` and `IndexesAsyncClient` methods now use `AIProjectIndex` instead of `Index`
- Replaced `DayOfWeek` custom enum (`com.azure.ai.projects.models.DayOfWeek`) with the JDK standard `java.time.DayOfWeek` in `WeeklyRecurrenceSchedule`
- Removed `CONTAINER_AGENTS_V1_PREVIEW`, `HOSTED_AGENTS_V1_PREVIEW`, and `WORKFLOW_AGENTS_V1_PREVIEW` constants from `FoundryFeaturesOptInKeys` (agent-specific feature flags moved to `azure-ai-agents`)

### Other Changes

- Generated from latest API spec

## 2.0.0-beta.1 (2026-02-25)

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
