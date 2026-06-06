---
name: codegen-survival-rules
description: 'Rules for making manual edits survive TypeSpec Java codegen re-generation. Covers @Generated removal, marker comment placement, and javadoc preservation. WHEN: edit generated code; survive codegen; @Generated annotation; marker comment placement; manual edits to generated files.'
---

# Codegen Survival Rules

The TypeSpec Java codegen (`tsp-client update` / `tsp-client generate`) re-generates files on every run. Methods **without** `@Generated` are preserved (body intact), but everything **above** the method signature (javadoc, comments) is regenerated. Follow these rules so your manual edits survive.

## Rules

1. **Remove `@Generated`** from any method you modify. The codegen will not overwrite the method body.
2. **Place marker comments inside the method body**, not above the signature. The codegen rewrites the javadoc block above the signature but does not touch the body.
3. **Place javadoc above the method** normally. Since the method lacks `@Generated`, the codegen preserves the javadoc you wrote.
4. **For field declarations**, place marker comments on the same line (trailing), not on the line above. The codegen regenerates the comment block above the field.

## Examples

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

## When to apply

These rules apply whenever you hand-edit a generated model class, including:
- De-duplicating against openai-java types (see `dedup-openai` skill)
- Overriding TypeSpec types with Java-native types (see `tsp-type-override` skill)
- Adding typed union wrappers over `BinaryData` properties (see `union-type-wrappers` skill)
- Any other manual customization of generated `toJson`/`fromJson` methods
