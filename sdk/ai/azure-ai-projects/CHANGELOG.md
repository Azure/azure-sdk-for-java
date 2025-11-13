# Release History

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
