# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

- Added `getOpenAIClient` methods to obtain an instance of the Stainless OpenAI client
- Added documentation on how to get an `AgentsClient`

### Breaking Changes

- Updated service version from `2025-11-15-preview` to `v1`
- Renamed `AgenticIdentityCredentials` to `AgenticIdentityPreviewCredentials`
- Renamed `AgentClusterInsightsRequest` to `AgentClusterInsightRequest`
- `ConnectionType.REMOTE_TOOL` value changed to `RemoteTool_Preview`
- `CredentialType.AGENTIC_IDENTITY` renamed to `AGENTIC_IDENTITY_PREVIEW`

### Bugs Fixed

### Other Changes

- Updated version of `openai` client library to `4.14.0`
- Generated from latest v2 API spec

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
