# Duplicate Class Analysis: azure-ai-agents vs openai-java

**Date:** 2026-03-10
**openai-java version:** 4.14.0 (`openai-java-core`)
**Generated models scanned:** 155
**openai-java models available:** 787

## Summary

| Verdict | Count |
|---------|-------|
| 🔴 Actionable duplicate (suppressed, use openai-java) | 4 models + 4 enums |
| ⬜ Structural equivalent (same JSON, not actionable) | 10+ |
| ⚪ No match | ~140 |

---

## 🔴 Actionable Duplicates — Suppressed

These standalone models produce identical JSON to their openai-java counterpart and are **not** part of any SDK type hierarchy. They have been suppressed to `implementation.models` via `@@access(internal)` in TypeSpec and users should use the openai-java equivalents directly.

### 1. `ComparisonFilter` + `ComparisonFilterType` → `com.openai.models.ComparisonFilter`

| Field | JSON key | Agents type | openai-java type | Match |
|-------|----------|------------|-------------------|-------|
| `type` | `"type"` | `ComparisonFilterType` (enum) | `ComparisonFilter.Type` (string enum) | ✅ |
| `key` | `"key"` | `String` | `String` | ✅ |
| `value` | `"value"` | `BinaryData` | `ComparisonFilter.Value` (union) | ✅ |

Enum values identical: `eq`, `ne`, `gt`, `gte`, `lt`, `lte`.

### 2. `CompoundFilter` + `CompoundFilterType` → `com.openai.models.CompoundFilter`

| Field | JSON key | Agents type | openai-java type | Match |
|-------|----------|------------|-------------------|-------|
| `type` | `"type"` | `CompoundFilterType` (enum) | `CompoundFilter.Type` (string enum) | ✅ |
| `filters` | `"filters"` | `List<BinaryData>` | `List<CompoundFilter.Filter>` (union) | ✅ |

Enum values identical: `and`, `or`.

### 3. `Reasoning` + `ReasoningEffort` + `ReasoningSummary` + `ReasoningGenerateSummary` → `com.openai.models.Reasoning`

| Field | JSON key | Agents type | openai-java type | Match |
|-------|----------|------------|-------------------|-------|
| `effort` | `"effort"` | `ReasoningEffort` (enum) | `ReasoningEffort` (string enum) | ✅ |
| `summary` | `"summary"` | `ReasoningSummary` (enum) | `Reasoning.Summary` (string enum) | ✅ |
| `generateSummary` | `"generate_summary"` | `ReasoningGenerateSummary` (enum) | `Reasoning.GenerateSummary` (string enum) | ✅ |

All enum values identical across all three fields.

### What was done

- **TypeSpec**: `@@access(OpenAI.ComparisonFilter, Access.internal, "java")` (and same for CompoundFilter, Reasoning, ReasoningEffort) in `client.tsp`. `@@changePropertyType(PromptAgentDefinition.reasoning, string)` to decouple the public field from the internal type.
- **PromptAgentDefinition**: `reasoning` field changed to `BinaryData`. Getter returns `com.openai.models.Reasoning`, setter accepts it. Serialization uses `BinaryData.writeTo(jsonWriter)`.
- **FileSearchTool**: Added typed `setFilters(ComparisonFilter)` and `setFilters(CompoundFilter)` overloads.

---

## ⬜ Structural Equivalents — Not Actionable

These classes produce identical JSON to their openai-java counterpart but **cannot be suppressed** because they participate in the `Tool` or `TextResponseFormatConfiguration` discriminator hierarchies. The SDK's polymorphic serialization (`Tool.fromJson()` dispatches to `FunctionTool.fromJson()`, etc.) requires them to exist as concrete subtypes. They are not duplicates in a meaningful sense — they serve a structural role.

| Agents class | openai-java class | Why not actionable |
|---|---|---|
| `FunctionTool` | `responses.FunctionTool` | extends `Tool`, used in `Tool.fromJson()` discriminator |
| `FileSearchTool` | `responses.FileSearchTool` | extends `Tool`, used in `Tool.fromJson()` discriminator |
| `WebSearchPreviewTool` | `responses.WebSearchPreviewTool` | extends `Tool`, used in `Tool.fromJson()` discriminator |
| `ComputerUsePreviewTool` | `responses.ComputerTool` | extends `Tool`, used in `Tool.fromJson()` discriminator |
| `TextResponseFormatJsonSchema` | `responses.ResponseFormatTextJsonSchemaConfig` | extends `TextResponseFormatConfiguration` |
| `TextResponseFormatConfigurationResponseFormatText` | `ResponseFormatText` | extends `TextResponseFormatConfiguration` |
| `TextResponseFormatConfigurationResponseFormatJsonObject` | `ResponseFormatJsonObject` | extends `TextResponseFormatConfiguration` |

Sub-types of the above (`RankingOptions`, `HybridSearchOptions`, `ApproximateLocation`, `SearchContextSize`, `ComputerEnvironment`, `RankerVersionType`, `ResponseFormatJsonSchemaInner`) are also structural — they exist because their parent class references them.

---

## 🟡 Partial Matches (Azure superset)

| Agents class | openai-java class | Extra field |
|---|---|---|
| `CodeInterpreterTool` | `beta.assistants.CodeInterpreterTool` | `container` (Azure-only) |
| `WebSearchTool` | `responses.WebSearchTool` | `custom_search_configuration` (Azure-only) |

---

## Methodology

1. All 155 agents model classes compared by name against 787 openai-java-core classes.
2. Name-matched candidates had fields extracted and compared by JSON key, type compatibility, and enum values.
3. Sub-type comparison performed recursively for nested objects.
4. Classes were categorized as *actionable* (standalone, suppressible) vs *structural* (hierarchy members, required by SDK type system) based on whether they could be removed without breaking polymorphic serialization.
