---
name: dedup-openai
description: Suppress generated Java classes that duplicate openai-java models, using @@alternateType in TypeSpec and manual serialization bridges. Use after dup-classes has identified actionable duplicates.
---

# De-duplicate Generated Classes Against openai-java

Use this skill **after** the `dup-classes` skill has identified actionable duplicates. This skill suppresses the generated classes and bridges to the openai-java equivalents.

## Preconditions
- A `tsp-location.yaml` must exist in the current directory.
- TypeSpec must be synced (`tsp-client sync`) so `TempTypeSpecFiles/` exists.
- The `openai-java` dependency must be in the project's `pom.xml`.
- You must know which classes to suppress (use `dup-classes` skill first).

## Key concepts

### What can be suppressed
Only **standalone models** that don't participate in a discriminator hierarchy. A model is standalone if:
- It does NOT extend `Tool`, `TextResponseFormatConfiguration`, or another base class with a `fromJson` discriminator
- It is NOT a subtype dispatched by a parent's `fromJson` method

### What cannot be suppressed
**Structural equivalents** — classes that extend a base type in a discriminator hierarchy (e.g., `FunctionTool extends Tool`). The SDK's polymorphic serialization requires these. They produce identical JSON but are not actionable. Do NOT attempt to suppress them.

### Two suppression mechanisms

| Mechanism | When to use | Effect |
|-----------|-------------|--------|
| `@@alternateType(OpenAI.X, { identity: "com.openai.models.X" }, "java")` | The model is referenced as a field type or union member in other models | Codegen replaces the type with the openai-java class. The generated class is NOT emitted at all. |
| `@@access(OpenAI.X, Access.internal, "java")` | The model is NOT referenced by any public model | Codegen moves the class to `implementation.models`. |

**Prefer `@@alternateType`** — it fully prevents emission and is the cleanest approach. Use `@@access(internal)` only as a supplement when a model isn't reachable through the type graph but is still emitted.

### Why `@@access(internal)` alone may not work
If a model is referenced by a union or property in a public model, `@@access(internal)` will NOT move it. The codegen keeps it public because removing it would break the type graph. Example: `ComparisonFilter` is a member of the `Filters` union used by `FileSearchTool.filters` — `@@access(internal)` has no effect, but `@@alternateType` prevents emission entirely.

## Steps

### 1. Edit the TypeSpec client file

Locate the `client.tsp` (or `client.java.tsp`) in `TempTypeSpecFiles/sdk-*/`:

```bash
find TempTypeSpecFiles -name "client*.tsp" -path "*/sdk-*"
```

Add `@@alternateType` directives for each actionable duplicate:

```tsp
// De-dup: map to openai-java equivalents
@@alternateType(OpenAI.ComparisonFilter, { identity: "com.openai.models.ComparisonFilter" }, "java");
@@alternateType(OpenAI.Reasoning, { identity: "com.openai.models.Reasoning" }, "java");
```

**Finding the correct model name:** The TypeSpec models are in the `OpenAI` namespace. Search the openai-typespec package:

```bash
grep -rn "^model <ClassName>" TempTypeSpecFiles/node_modules/@azure-tools/openai-typespec/src/ --include="*.tsp"
```

### 2. Regenerate and verify

```bash
tsp-client generate
```

After generation, verify the suppressed classes are gone:

```bash
# Should NOT exist:
ls src/main/java/com/azure/ai/agents/models/<SuppressedClass>.java
# Should NOT exist in implementation either (with @@alternateType):
ls src/main/java/com/azure/ai/agents/implementation/models/<SuppressedClass>.java
```

### 3. Fix serialization in parent models

When a model's property type changes from a generated `JsonSerializable` class to an openai-java class, the `toJson`/`fromJson` methods in parent models will break because the openai-java type doesn't implement `JsonSerializable`.

**Pattern for `toJson`** — use `OpenAIJsonHelper.toBinaryData()`:
```java
// Before (generated, won't compile):
jsonWriter.writeJsonField("reasoning", this.reasoning);

// After:
if (this.reasoning != null) {
    jsonWriter.writeFieldName("reasoning");
    OpenAIJsonHelper.toBinaryData(this.reasoning).writeTo(jsonWriter);
}
```

**Pattern for `fromJson`** — read as BinaryData, convert with `OpenAIJsonHelper.fromBinaryData()`:
```java
// Before (generated, won't compile):
reasoning = Reasoning.fromJson(reader);

// After:
BinaryData reasoningData
    = reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()));
reasoning = OpenAIJsonHelper.fromBinaryData(reasoningData, com.openai.models.Reasoning.class);
```

**Pattern for getter/setter** — use the openai-java type directly, with javadoc above and marker comment inside the body:
```java
// Field stores the openai-java type directly (no BinaryData indirection)
private com.openai.models.Reasoning reasoning; // AI Tooling: openai-java de-dup

/**
 * Gets the reasoning configuration.
 * @return the reasoning, or null if not set.
 */
public com.openai.models.Reasoning getReasoning() {
    // AI Tooling: openai-java de-dup
    return this.reasoning;
}

/**
 * Sets the reasoning configuration.
 * @param reasoning the reasoning to set.
 * @return this object.
 */
public PromptAgentDefinition setReasoning(com.openai.models.Reasoning reasoning) {
    // AI Tooling: openai-java de-dup
    this.reasoning = reasoning;
    return this;
}
```

Remove `@Generated` from any method you modify so the codegen preserves your changes on re-generation. See [Codegen survival rules](#codegen-survival-rules) for comment/javadoc placement.

### 4. Add typed convenience setters (for BinaryData fields)

When a property is already `BinaryData` (e.g., because it's a union type), add **distinctly named** setter methods for the openai-java types. Do NOT overload `setX` with different parameter types — this causes null-ambiguity. Use descriptive names instead:

```java
/**
 * Sets the filters using an openai-java ComparisonFilter.
 * @param filter the filter to apply, or null to clear.
 * @return this object.
 */
public FileSearchTool setComparisonFilter(com.openai.models.ComparisonFilter filter) {
    // AI Tooling: openai-java de-dup
    this.filters = OpenAIJsonHelper.toBinaryData(filter);
    return this;
}

/**
 * Sets the filters using an openai-java CompoundFilter.
 * @param filter the filter to apply, or null to clear.
 * @return this object.
 */
public FileSearchTool setCompoundFilter(com.openai.models.CompoundFilter filter) {
    // AI Tooling: openai-java de-dup
    this.filters = OpenAIJsonHelper.toBinaryData(filter);
    return this;
}
```

### 5. Add `OpenAIJsonHelper` methods if needed

The `OpenAIJsonHelper` class in `com.azure.ai.agents.implementation` may need two bridge methods:

```java
// Serialize openai-java object → BinaryData (writes as JSON object, not quoted string)
public static BinaryData toBinaryData(Object openAIObject)

// Deserialize BinaryData → openai-java type
public static <T> T fromBinaryData(BinaryData data, Class<T> type)
```

These use the openai-java `ObjectMappers.jsonMapper()` (which handles Kotlin internals correctly). Do NOT use `BinaryData.fromObject()` or `BinaryData.toObject()` with openai-java types — the default Jackson ObjectMapper cannot serialize Kotlin `SynchronizedLazyImpl` fields.

### 6. Write serialization tests

Write round-trip tests verifying the JSON shape is preserved. Test pattern:

```java
@Test
public void testRoundTrip() throws IOException {
    // Build with openai-java type
    Reasoning reasoning = Reasoning.builder().effort(ReasoningEffort.HIGH).build();
    PromptAgentDefinition original = new PromptAgentDefinition("gpt-4o").setReasoning(reasoning);

    // Serialize
    String json = serialize(original);
    assertTrue(json.contains("\"effort\":\"high\""));

    // Deserialize
    PromptAgentDefinition deserialized = deserialize(json);
    assertEquals(ReasoningEffort.HIGH, deserialized.getReasoning().effort().get());

    // Re-serialize and compare
    assertEquals(json, serialize(deserialized));
}
```

Cover: all enum values, null/absent fields, combined with other fields, polymorphic deserialization via parent `fromJson`.

### 7. Apply changes to the spec repo

If a local checkout of `Azure/azure-rest-api-specs` is available, apply the same `client.tsp` edits there. Derive the path from `tsp-location.yaml`:

```
<spec_repo>/<directory>/client.tsp
```

## Codegen survival rules

The TypeSpec Java codegen (`tsp-client update` / `tsp-client generate`) will re-generate files on every run. Methods **without** `@Generated` are preserved (body intact), but everything **above** the method signature (javadoc, comments) is regenerated. Follow these rules so your manual edits survive:

1. **Remove `@Generated`** from any method you modify. The codegen will not overwrite the method body.
2. **Place marker comments inside the method body**, not above the signature. The codegen rewrites the javadoc block above the signature but does not touch the body.
3. **Place javadoc above the method** normally. Since the method lacks `@Generated`, the codegen preserves the javadoc you wrote.
4. **For field declarations**, place marker comments on the same line (trailing), not on the line above. The codegen regenerates the comment block above the field.

```java
// ✅ SURVIVES codegen: javadoc above, marker inside body
/**
 * Gets the reasoning configuration.
 * @return the reasoning, or null if not set.
 */
public com.openai.models.Reasoning getReasoning() {
    // AI Tooling: openai-java de-dup  ← inside body, survives
    return this.reasoning;
}

// ❌ WIPED by codegen: marker above signature
// AI Tooling: openai-java de-dup  ← above signature, gets wiped
public com.openai.models.Reasoning getReasoning() {
    return this.reasoning;
}

// ✅ SURVIVES codegen: field marker on same line
private com.openai.models.Reasoning reasoning; // AI Tooling: openai-java de-dup

// ❌ WIPED by codegen: field marker on line above
// AI Tooling: openai-java de-dup
private com.openai.models.Reasoning reasoning;
```

## Common pitfalls

| Problem | Cause | Fix |
|---------|-------|-----|
| Class stays public despite `@@access(internal)` | Referenced by a union or property in a public model | Use `@@alternateType` instead |
| `BinaryData.fromObject(openAIObj)` throws `SynchronizedLazyImpl` error | Default Jackson can't serialize Kotlin internals | Use `OpenAIJsonHelper.toBinaryData()` which uses `ObjectMappers.jsonMapper()` |
| `BinaryData.fromString(json).writeTo(writer)` writes quoted string | `fromString` creates text content, not JSON | Use `BinaryData.fromObject(reader.readUntyped())` to store as a JSON object |
| Getter/setter bridge through BinaryData on every call | Unnecessary indirection | Store the openai-java type directly in the field; bridge only in `toJson`/`fromJson` |
| Tried to suppress a `Tool` subclass | Structural equivalent, not an actionable duplicate | Don't suppress — it's needed for polymorphic deserialization |
| Javadoc/comments above method wiped after codegen | Codegen rewrites everything above non-`@Generated` method signatures | Place marker comments inside the method body; javadoc survives if `@Generated` is removed (see [Codegen survival rules](#codegen-survival-rules)) |
| Overloaded setters cause null ambiguity | `setFilters(null)` matches `BinaryData`, `ComparisonFilter`, and `CompoundFilter` | Use distinct method names: `setComparisonFilter()`, `setCompoundFilter()` |
