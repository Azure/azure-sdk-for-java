---
name: dup-classes
description: Verify whether generated Java classes duplicate openai-java models by comparing fields/types (names may differ). Use when checking for duplicate model coverage.
---

# Duplicate Class Verification (Generated vs openai-java)

Use this skill to compare generated Java models against the `openai-java` dependency. The goal is **field-by-field** comparison of model shapes, even when class or field names differ.

## Inputs to confirm
- Generated source root (e.g., `src/main/java/...`)
- The relevant `pom.xml` (module) to resolve the `openai-java` dependency version
- Optional: package or class name hints to narrow the search

## Steps
1. **Locate the pom.xml** in the current directory tree (`find . -name pom.xml`). If multiple, ask which module to use.
2. **Resolve openai-java**:
   - Search the chosen pom for `openai-java` (or an explicit group/artifact provided by the user).
   - Resolve the version (including properties) and locate the JAR in `~/.m2/repository`.
   - The model classes live in `openai-java-core`, not the top-level `openai-java` artifact.
3. **List candidate classes**:
   - Generated classes: scan the source root for `class` and `record` declarations.
   - openai-java classes: `jar tf <jar> | grep '\.class$'` (filter by package hints if provided).
4. **Extract field signatures** (names may differ; compare shape):
   - **Generated source**:
     - For `record`, use the component list in the `record` declaration.
     - For `class`, extract non-static field declarations (type + count) and note any `@JsonProperty` names.
     - Check `toJson`/`fromJson` methods for the actual JSON keys used in serialization.
   - **openai-java JAR**:
     - Use `javap -classpath <jar> -p <FQCN>` to list fields (ignore `static`, `validated`, `hashCode$delegate`, `additionalProperties`).
     - Extract `@JsonProperty` keys from the sources JAR (`*-sources.jar`) for JSON key comparison.
5. **Compare shapes**:
   - Compare **field count** and **field types** (order-independent).
   - Compare **JSON keys** from `@JsonProperty` (openai-java) vs `toJson`/`fromJson` string literals (generated).
   - Compare **enum/union values** when field types are enums or string unions.
   - Follow type hierarchy: `BinaryData` ↔ `JsonValue` (both represent untyped JSON), `Map<String,BinaryData>` ↔ `Map<String,JsonValue>`, Java enum ↔ Kotlin string enum.
6. **Categorize results** (do NOT treat all matches the same):

   **Actionable duplicates** — standalone models not in any type hierarchy. These can potentially be suppressed and replaced with the openai-java equivalent. Examples: `ComparisonFilter`, `Reasoning`.

   **Structural equivalents** — classes that produce identical JSON but participate in a discriminator hierarchy (e.g., `extends Tool`, `extends TextResponseFormatConfiguration`). The SDK's polymorphic serialization (`Tool.fromJson()` dispatches to `FunctionTool.fromJson()`, etc.) requires these to exist. They are NOT actionable duplicates. Examples: `FunctionTool`, `FileSearchTool`, `ComputerUsePreviewTool`.

   **Partial matches** — classes with most fields matching but extra Azure-specific fields. Note the extra fields. Examples: `CodeInterpreterTool` (extra `container`), `WebSearchTool` (extra `custom_search_configuration`).

7. **Report**:
   - Provide a table with: generated class → openai-java class, field count, matching fields, category.
   - Clearly separate actionable duplicates from structural equivalents.
   - For actionable duplicates, note whether `@@alternateType` or `@@access(internal)` would be the right suppression mechanism (see `dedup-openai` skill).

## Useful commands

### List generated class names
```bash
rg -n "^(public\s+)?(final\s+)?(class|record)\s+" <generated_root>
```

### Extract field lines from source (classes)
```bash
grep -E '^\s+private\s+' <file> | grep -v 'static\s'
```

### Extract JSON keys from generated toJson/fromJson
```bash
grep -E 'jsonWriter\.write|"[a-z_]+"' <file> | grep -v '//'
```

### Inspect fields in a JAR class
```bash
javap -classpath <jar> -p <fully.qualified.ClassName>
```

### Extract @JsonProperty from openai-java sources JAR
```bash
jar xf <sources-jar> main/com/openai/models/<Class>.kt
grep '@JsonProperty' main/com/openai/models/<Class>.kt
```

### Check if a class participates in a hierarchy
```bash
grep 'extends\s' <file>  # If it extends Tool, TextResponseFormatConfiguration, etc. → structural
```

## Notes
- Use `search-m2` if you need help locating the dependency version or JAR path.
- If the user provides only a vague class hint, narrow candidates by package or field count first.
- The openai-java classes are Kotlin and use Jackson; generated classes use azure-json (`JsonSerializable`). Compare at the JSON wire level, not at the Java API level.
