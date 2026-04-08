# Commit `d59cf77c97c` — Change Analysis
**Message:** regen w new SHA + customizations
**Author:** Isaiah Dickerson
**Date:** Thu Mar 19 14:30:20 2026

---

## Verdict: Two manual changes — both are cleanup-only deletions, no new tests needed

The vast majority of this commit (185 files, ~23k line deletions) is a TypeSpec regeneration driven
by a new spec SHA and a GA version bump from `2025-11-01-preview` to `2026-04-01`. Two manually
maintained files (`FieldBuilder.java` and `SearchUtils.java`) required follow-up cleanup to remove
references to types and fields that TypeSpec deleted. Neither change introduces new behavior.

---

## Manual Changes

### ✅ `FieldBuilder.java` — Removed two field mappings (cleanup, no new tests needed)

`FieldBuilder` is a hand-written class that maps Java annotations to `SearchField`. Two mappings
were removed because their corresponding types were deleted from TypeSpec:

```java
// Removed:
.setPermissionFilter(nullOrT(basicField.permissionFilter(), PermissionFilter::fromString))
.setSensitivityLabel(toBoolean(basicField.isSensitivityLabel()))
```

Also removed the `import` for `PermissionFilter`.

**Why no test needed:** This is a pure removal — no new code path was added. Existing
`FieldBuilderTests` covered the removed behavior; those tests were deleted/updated alongside the
type removal. Nothing new to assert.

---

### ✅ `SearchUtils.java` — Removed field mappings + header logic (cleanup, no new tests needed)

`SearchUtils` is a hand-written utility. Two categories of code were removed:

**1. Field mappings in `toSearchRequest()` for removed TypeSpec fields:**
```java
// Removed:
.setQueryLanguage(options.getQueryLanguage())
.setQuerySpeller(options.getQuerySpeller())
.setQueryRewrites(options.getQueryRewrites())
.setSemanticFields(options.getSemanticFields())
.setHybridSearch(options.getHybridSearch())
```

**2. Header injection logic in `addSearchHeaders()` for removed permission features:**
```java
// Removed:
private static final HttpHeaderName X_MS_QUERY_SOURCE_AUTHORIZATION = ...;
private static final HttpHeaderName X_MS_ENABLE_ELEVATED_READ = ...;
// ... and the full body of addSearchHeaders() that set those headers
```

`addSearchHeaders()` now simply returns `requestOptions` as-is.

**Why no test needed:** Again, pure removal. `QueryLanguage`, `QuerySpeller`, `QueryRewrites`,
`SemanticFields`, `HybridSearch`, `querySourceAuthorization`, and `enableElevatedRead` were all
deleted from the TypeSpec source. The method `addSearchHeaders()` is still called by `SearchClient`
and `SearchAsyncClient` but now does nothing — any tests relying on the removed header behavior
were removed with the feature.

---

## Generated Changes (everything else)

### 🔴 Version bump

| File | Change |
|---|---|
| `SearchServiceVersion.java` | `V2025_11_01_PREVIEW("2025-11-01-preview")` → `V2026_04_01("2026-04-01")` and `getLatest()` updated |
| `azure-search-documents_metadata.json` | Version metadata updated |
| `tsp-location.yaml` | New spec SHA |

### 🔴 Entire class deletions (removed from TypeSpec)

`AIServicesVisionParameters`, `AIServicesVisionVectorizer`, `AzureMachineLearningSkill`,
`AzureOpenAITokenizerParameters`, `ChatCompletionSkill`, `IndexStatisticsSummary`,
`IndexedSharePointContainerName`, `IndexedSharePointKnowledgeSource`,
`IndexedSharePointKnowledgeSourceParameters`, `IndexerCurrentState`, `IndexerExecutionStatusDetail`,
`IndexerPermissionOption`, `IndexerRuntime`, `IndexingMode`, `KnowledgeBase`, `PermissionFilter`,
`RemoteSharePointKnowledgeSource`, `RemoteSharePointKnowledgeSourceParameters`, `SearchField` fields,
`SearchIndexPermissionFilterOption`, `SearchIndexerCache`, `SearchIndexerKnowledgeStore`,
`SearchIndexerKnowledgeStoreParameters`, `SemanticConfiguration` fields, `ServiceIndexersRuntime`,
`SplitSkill`, `SplitSkillEncoderModelName`, `SplitSkillUnit`, `VisionVectorizeSkill`,
`DocumentDebugInfo` fields, `FacetResult`, `HybridCountAndFacetMode`, `HybridSearch`,
`QueryLanguage`, `QueryResultDocumentInnerHit`, `QueryResultDocumentRerankerInput`,
`QueryResultDocumentSemanticField`, `QueryRewritesDebugInfo`, `QueryRewritesType`,
`QueryRewritesValuesDebugInfo`, `QuerySpellerType`, `SearchScoreThreshold`, `SemanticDebugInfo`,
`SemanticFieldState`, `SemanticQueryRewritesResultType`, `VectorQuery`, `VectorSimilarityThreshold`,
`VectorThreshold`, `VectorThresholdKind`, `VectorizableImageBinaryQuery`,
`VectorizableImageUrlQuery`, `VectorizableTextQuery`, `VectorizedQuery`, and knowledge-base types.

### 🔴 New generated classes

Dozens of `CountRequestAccept*.java` and `CreateOrUpdateRequestAccept*.java` enums generated to
represent typed `Accept` header values — all `@Generated`, all in `implementation/models/` or
`models/`.

### 🔴 Client / impl file regeneration

`SearchClient.java`, `SearchAsyncClient.java`, `SearchIndexClient.java`,
`SearchIndexAsyncClient.java`, `SearchIndexerClient.java`, `SearchIndexerAsyncClient.java`,
`KnowledgeBaseRetrievalClient.java`, `KnowledgeBaseRetrievalAsyncClient.java`,
`SearchClientImpl.java`, `SearchIndexClientImpl.java`, `SearchIndexerClientImpl.java`,
`KnowledgeBaseRetrievalClientImpl.java` — all regenerated to match new spec SHA. Changes are
Javadoc schema removals, new `Accept`-header overloads (all `@Generated`), and removal of the
`getDocument(key, querySourceAuthorization, enableElevatedRead, selectedFields)` overload.

### 🔴 New/updated documentation

`.github/skills/Azure.Search.Documents/SKILL.md`, `.github/skills/references/architecture.md`,
`.github/skills/references/customizations.md` — new reference docs added alongside the regen.

---

## Conclusion

No new tests are required for this commit. The two manual file edits (`FieldBuilder.java` and
`SearchUtils.java`) are deletions that follow TypeSpec removing the underlying features — there is
no new behavior to assert.
