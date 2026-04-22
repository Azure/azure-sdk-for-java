# azure-search-documents -- Customization Guide

All post-generation modifications live in `customizations/src/main/java/SearchCustomizations.java`. This file extends `Customization` and uses JavaParser AST manipulation at code-generation time.

**Use Java customizations when TypeSpec cannot express the desired behavior, or when the behavior is Java-specific.** For TypeSpec-level customizations (preferred when possible), see [TypeSpec Client Customizations Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/knowledge/customizing-client-tsp.md).

Note: The `azsdk_customized_code_update` MCP tool can apply many customization fixes automatically (TypeSpec decorators first, then code patches). The documentation below covers what that tool does NOT handle -- search-specific patterns that require manual understanding.

## Customization Methods

### 1. hideGeneratedSearchApis

**What**: Strips public modifiers from `searchWithResponse`, `autocompleteWithResponse`, `suggestWithResponse` on SearchClient/SearchAsyncClient (makes them package-private).

**Why**: The generator must expose these to infer SearchOptions, but they shouldn't be public API.

**When to update**: If the generator changes how SearchOptions is inferred or renames these operations.

### 2. addSearchAudienceScopeHandling

**What**: Adds a mutable `private String[] scopes = DEFAULT_SCOPES` field to all builder classes, replaces `DEFAULT_SCOPES` with `scopes` in `createHttpPipeline()` method body.

**Applied to**: SearchClientBuilder, SearchIndexClientBuilder, SearchIndexerClientBuilder, KnowledgeBaseRetrievalClientBuilder.

**When to update**: When [typespec#9458](https://github.com/microsoft/typespec/issues/9458) is resolved. Also if `DEFAULT_SCOPES` field or `createHttpPipeline` method is renamed/restructured.

### 3. includeOldApiVersions

**What**: Prepends older version constants to the SearchServiceVersion enum.

**Current list**: See `includeOldApiVersions()` in `SearchCustomizations.java` for the authoritative version list. When a new API version is released, add the previous latest to this list -- but only if the generator does not already produce it.

**When to update**: When a new API version is released -- add the previous latest to this list. **Critical**: check if the generator now produces a version that's in this list. If so, remove it to avoid duplicate enum constants.

### 4. removeGetApis

**What**: Removes all methods with prefixes `searchGet`, `suggestGet`, `autocompleteGet` from SearchClient/SearchAsyncClient.

**Why**: The spec defines GET and POST variants; Java SDK only exposes POST.

**When to update**: If the spec changes operation names or adds new GET variants.

### 5. hideWithResponseBinaryDataApis (MOST FRAGILE)

**What**: For each public `@Generated` method using BinaryData in return type or parameters:
1. Renames to `hiddenGenerated<OriginalName>` and strips modifiers (package-private)
2. Updates convenience methods to call the renamed version

**Applied to**: All 8 client classes (sync + async for search, index, indexer, knowledge base).

**Why it's fragile**: It does string replacement across method bodies, which can create cross-package import mismatches when operations are added/removed (type numbering changes).

**When to update**: When the Java TypeSpec generator natively supports typed `WithResponse<T>` APIs, or when operations are added/removed in the spec.

## Post-Regeneration Checklist

```
[ ] Check if any customization methods reference names that no longer exist
    (silently does nothing -- no error, no warning)
[ ] If new API version:
    [ ] Check includeOldApiVersions() list for duplicates with generator
    [ ] Verify getLatest() returns the new version
[ ] If new client classes generated:
    [ ] Add hideWithResponseBinaryDataApis() call
    [ ] Add addSearchAudienceScopeHandling() call for builder
[ ] If new public methods should be hidden:
    [ ] Add AST manipulation in SearchCustomizations.java
```

> For adding new customizations or applying fixes, use the `azsdk_customized_code_update` MCP tool from the `generate-sdk-locally` shared skill. It handles the classify-apply-regenerate-build cycle automatically.

## Troubleshooting

**Build fails after customization**: The AST manipulation produced invalid Java. Check `StaticJavaParser.parseBlock()` calls for syntax errors.

**Customization silently does nothing**: A method/class name changed upstream. The `getMethodsByName()` call returns empty. Verify string literals match current generated code.

**Cross-package import errors after hideWithResponseBinaryDataApis**: The method body rewiring created imports referencing types in the wrong package. Check if operations were added/removed in the spec.
