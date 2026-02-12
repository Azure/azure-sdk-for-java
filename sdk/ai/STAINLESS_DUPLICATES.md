# OpenAI Java duplicate model check (full repo)

## Scope
- Repository: `azure-sdk-for-java` (full scan)
- `com.openai:openai-java` dependency found in:
  - `sdk/ai/azure-ai-agents`
  - `sdk/ai/azure-ai-projects`
  - `sdk/openai/azure-ai-openai-stainless`
- OpenAI model shapes sourced from **openai-java-core 4.14.0** (the runtime classes behind the `openai-java` BOM).
- Model name overlap scan performed across `src/main/java/**/models/**` in each module, followed by field-by-field checks for overlaps.

## Likely duplicates (shape match)
> Found only in `sdk/ai/azure-ai-agents` based on full-repo scan.

| Azure model | OpenAI model | Matching fields / notes |
| --- | --- | --- |
| `com.azure.ai.agents.models.ComparisonFilter` | `com.openai.models.ComparisonFilter` | Fields: `type`, `key`, `value` |
| `com.azure.ai.agents.models.CompoundFilter` | `com.openai.models.CompoundFilter` | Fields: `type`, `filters` |
| `com.azure.ai.agents.models.FunctionTool` | `com.openai.models.responses.FunctionTool` | Fields: `name`, `parameters`, `strict`, `type`, `description` |
| `com.azure.ai.agents.models.FileSearchTool` | `com.openai.models.responses.FileSearchTool` | Fields: `type`, `vectorStoreIds`, `filters`, `maxNumResults` (Azure: `maxResults`), `rankingOptions` |
| `com.azure.ai.agents.models.Reasoning` | `com.openai.models.Reasoning` | Fields: `effort`, `summary`, `generateSummary` |
| `com.azure.ai.agents.models.ReasoningEffort` | `com.openai.models.ReasoningEffort` | Enum values: `none`, `minimal`, `low`, `medium`, `high`, `xhigh` |
| `com.azure.ai.agents.models.WebSearchPreviewTool` | `com.openai.models.responses.WebSearchPreviewTool` | Fields: `type`, `userLocation`, `searchContextSize` |

## Notes on other modules
- **`sdk/ai/azure-ai-projects`**: no model name overlaps with OpenAI Java models found in the scan; no likely duplicates identified.
- **`sdk/openai/azure-ai-openai-stainless`**: no `models` Java classes under `src/main/java`, so no duplicates identified.
