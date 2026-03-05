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
3. **List candidate classes**:
   - Generated classes: scan the source root for `class` and `record` declarations.
   - openai-java classes: `jar tf <jar> | rg '\.class$'` (filter by package hints if provided).
4. **Extract field signatures** (names may differ; compare shape):
   - **Generated source**:
     - For `record`, use the component list in the `record` declaration.
     - For `class`, extract non-static field declarations (type + count) and note any `@JsonProperty` names.
   - **openai-java JAR**:
     - Use `javap -classpath <jar> -p <FQCN>` to list fields (ignore `static`).
     - If you need annotations, use `javap -classpath <jar> -p -verbose <FQCN>` and look for `RuntimeVisibleAnnotations`.
5. **Compare shapes**:
   - Compare **field count** and **field types** (order-independent).
   - If `@JsonProperty` names exist in generated sources, compare those names with the openai-java field names.
6. **Report duplicates**:
   - Provide a table: generated class → openai-java class, with matching field types and any name mismatches.
   - Flag candidates with high similarity (same count and same types) even if names differ.

## Useful commands

### List generated class names
```bash
rg -n "^(public\s+)?(final\s+)?(class|record)\s+" <generated_root>
```

### Extract field lines from source (classes)
```bash
rg -n "^(\s*)(public|protected|private)\s+(static\s+)?(final\s+)?[A-Za-z0-9_<>,\[\].]+\s+[A-Za-z0-9_]+;" <generated_root>
```

### Extract record components
```bash
rg -n "record\s+[A-Za-z0-9_]+\s*\(([^)]*)\)" <generated_root>
```

### Inspect fields in a JAR class
```bash
javap -classpath <jar> -p <fully.qualified.ClassName>
```

## Notes
- Use `search-m2` if you need help locating the dependency version or JAR path.
- If the user provides only a vague class hint, narrow candidates by package or field count first.
