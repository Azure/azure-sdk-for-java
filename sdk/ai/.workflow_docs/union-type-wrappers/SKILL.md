---
name: union-type-wrappers
description: Add typed getters and setters over BinaryData properties that represent TypeSpec union types in generated Java models. Use when generated classes expose BinaryData for union-typed fields and you need ergonomic, type-safe accessors instead.
---

# Union Type Wrappers for Generated Java Models

When the Java codegen encounters a TypeSpec union type (e.g. `string | SomeModel`), it emits the property as `BinaryData`. This skill replaces public `BinaryData` accessors with typed setters and getters for each union variant, following the same pattern established for `PromptAgentDefinition.toolChoice`.

## Preconditions
- You must be in a Java SDK module directory containing `tsp-location.yaml` and `pom.xml`.
- TypeSpec sources must be available in `TempTypeSpecFiles/`. If missing, run `tsp-client sync` first.
- The project must compile before starting.

## Important: BinaryData.writeTo(JsonWriter) semantics

Understanding how `BinaryData` writes to JSON is critical for choosing the correct factory method:

| Factory method | Content type | `writeTo(JsonWriter)` calls | JSON output |
|---|---|---|---|
| `BinaryData.fromString("auto")` | `StringContent` | `jsonWriter.writeString("auto")` | `"auto"` (quoted) |
| `BinaryData.fromObject("auto")` | `SerializableContent` | `jsonWriter.writeRawValue(...)` | `"auto"` (quoted via Jackson) |
| `BinaryData.fromObject(42.0)` | `SerializableContent` | `jsonWriter.writeRawValue(...)` | `42.0` (raw) |
| `BinaryData.fromObject(true)` | `SerializableContent` | `jsonWriter.writeRawValue(...)` | `true` (raw) |
| `BinaryData.fromObject(jsonSerializable)` | `SerializableContent` | `jsonWriter.writeRawValue(...)` | `{...}` (JSON object via JacksonAdapter) |

Key: `JacksonAdapter` has special handling for `JsonSerializable` types — `BinaryData.fromObject()` and `BinaryData.toObject()` both work correctly with Azure `JsonSerializable` models.

### Setter factory method rules

| Union variant | Factory method | Reason |
|---|---|---|
| String value (ID, enum token) | `BinaryData.fromString(value)` | Writes as JSON string via `writeString()` |
| Numeric / boolean primitive | `BinaryData.fromObject(value)` | Writes as raw JSON value |
| Azure `JsonSerializable` model | `BinaryData.fromObject(value)` | JacksonAdapter handles serialization |
| Stainless (openai-java) type | `BinaryData.fromObject(value)` | Jackson handles serialization |

### Getter deserialization rules

| Union variant | Deserialization | Notes |
|---|---|---|
| String | `this.field.toObject(String.class)` | Consistent regardless of how BinaryData was created (`fromString` vs `fromObject` during deserialization) |
| Primitive (Number, Boolean) | `this.field.toObject(Double.class)` etc. | Jackson deserializes raw JSON values |
| Azure `JsonSerializable` model | `this.field.toObject(ModelClass.class)` | JacksonAdapter calls `fromJson()` |
| Stainless type | `this.field.toObject(StainlessType.class)` | Jackson deserializes natively |

## Workflow

### 1. Scan for BinaryData properties

Find all `BinaryData` fields in generated model classes:

```bash
grep -rn "private\s\+\(final\s\+\)\?BinaryData\s\+" src/main/java/ --include="*.java"
```

Exclude `List<BinaryData>` and `Map<..., BinaryData>` from this pass — those are collection-of-union patterns that require separate handling.

### 2. Cross-reference against TypeSpec

For each `BinaryData` property found, determine whether it comes from a **union type** or an **`unknown` type**:

```bash
# Search for the property name (use the wire name, e.g. tool_choice, not toolChoice)
grep -rn "<wire_name>" TempTypeSpecFiles/ --include="*.tsp"
```

- **Union type** (`type_a | type_b`): Proceed with this skill.
- **`unknown` type**: Leave as `BinaryData` — this is the correct representation.

Also check `client.tsp` for any `@@changePropertyType` overrides that may have already flattened the union to `string` (like `tool_choice`).

### 3. Identify the union variants

For each union type, determine what types the property can hold. Sources:

1. **Local TSP files** — look at the type definition in `TempTypeSpecFiles/`.
2. **Stainless SDK JAR** — if the union comes from `OpenAI.*`, inspect the Stainless types:
   ```bash
   jar tf ~/.m2/repository/com/openai/openai-java-core/<version>/openai-java-core-<version>.jar | grep "<TypeName>"
   javap -cp <jar> "com.openai.models.responses.<OuterClass>\$<InnerUnion>"
   ```
3. **Generated Azure models** — check if the Azure SDK already generates the variant model classes (e.g. `AutoCodeInterpreterToolParam`, `McpToolFilter`).

### 4. Apply changes to the model class

For each union-typed `BinaryData` property, apply the following pattern:

#### 4a. Leave the property field as-is

Do **not** modify the property declaration. Keep `@Generated`, the block comment, and the visibility exactly as the codegen produced them. The field is already `private` — there is nothing to change.

```java
/*
 * Original generated block comment.
 */
@Generated
private BinaryData myField;
```

#### 4b. Make the existing getter and setter package-private

- Remove `@Generated`.
- Change visibility to **package-private** (no access modifier). This keeps them hidden from SDK consumers but accessible to unit tests in the same package.
- **Keep the original method name** — do NOT rename to `*Internal`.
- **Keep the original javadoc intact.**
- Add `// AI Tooling: union type` as the **first line inside the method body**.

```java
/**
 * Get the myField property: original description.
 *
 * @return the myField value.
 */
BinaryData getMyField() {
    // AI Tooling: union type
    return this.myField;
}

/**
 * Set the myField property: original description.
 *
 * @param myField the myField value to set.
 * @return the MyClass object itself.
 */
MyClass setMyField(BinaryData myField) {
    // AI Tooling: union type
    this.myField = myField;
    return this;
}
```

**For `@Immutable` classes (value is a constructor param):**

- Change the `BinaryData` constructor visibility to package-private (keep for `fromJson` deserialization).
- Add public constructor overloads for each variant.
- Make the `BinaryData` getter private.

```java
/**
 * Creates an instance of MyFilter class.
 *
 * @param type the type value to set.
 * @param key the key value to set.
 * @param value the value value to set.
 */
MyFilter(MyFilterType type, String key, BinaryData value) {
    // AI Tooling: union type
    this.type = type;
    this.key = key;
    this.value = value;
}

public MyFilter(MyFilterType type, String key, String value) {
    this.type = type;
    this.key = key;
    this.value = BinaryData.fromObject(value);
}
```

#### 4c. Add typed setters (one per union variant)

Naming convention: **`set<PropertyName>(<VariantType> value)`** — use method overloading.

Copy the javadoc from the original generated setter, adapting the `@param` description to the specific variant type. Add `// AI Tooling: union type` as the first line inside the method body.

```java
/**
 * Set the myField property: original description.
 *
 * @param myField the string value to set.
 * @return the MyClass object itself.
 */
public MyClass setMyField(String myField) {
    // AI Tooling: union type
    this.myField = BinaryData.fromString(myField);
    return this;
}

/**
 * Set the myField property: original description.
 *
 * @param myField the SomeModel value to set.
 * @return the MyClass object itself.
 */
public MyClass setMyField(SomeModel myField) {
    // AI Tooling: union type
    this.myField = BinaryData.fromObject(myField);
    return this;
}
```

When overloading isn't possible (e.g. two different `String` meanings), disambiguate with parameter names and javadoc.

For `List<String>` variants:
```java
/**
 * Set the allowedTools property: original description.
 *
 * @param allowedTools the list of tool name strings to set.
 * @return the McpTool object itself.
 */
public McpTool setAllowedTools(List<String> allowedTools) {
    // AI Tooling: union type
    this.allowedTools = BinaryData.fromObject(allowedTools);
    return this;
}
```

#### 4d. Add typed getters (one per union variant)

Naming convention: **`get<PropertyName>As<TypeName>()`**

Copy the javadoc from the original generated getter, adapting the `@return` description. Add `// AI Tooling: union type` as the first line inside the method body.

```java
/**
 * Get the myField property as a String: original description.
 *
 * @return the myField value as a String.
 */
public String getMyFieldAsString() {
    // AI Tooling: union type
    if (this.myField == null) {
        return null;
    }
    return this.myField.toObject(String.class);
}

/**
 * Get the myField property as a {@link SomeModel}: original description.
 *
 * @return the myField value as a SomeModel.
 */
public SomeModel getMyFieldAsSomeModel() {
    // AI Tooling: union type
    if (this.myField == null) {
        return null;
    }
    return this.myField.toObject(SomeModel.class);
}
```

For `List<String>` variants:
```java
/**
 * Get the allowedTools property as a list of tool name strings: original description.
 *
 * @return the allowedTools value as a list of Strings.
 */
@SuppressWarnings("unchecked")
public List<String> getAllowedToolsAsStringList() {
    // AI Tooling: union type
    if (this.allowedTools == null) {
        return null;
    }
    return this.allowedTools.toObject(List.class);
}
```

### 5. Update callers

Search for existing code that uses the old `BinaryData` API:

```bash
grep -rn "\.setMyField(BinaryData\|\.getMyField()" src/ --include="*.java"
```

Update samples, tests, and internal code to use the new typed API. Remove unused `BinaryData` imports where applicable.

### 6. Write unit tests

Create a test class per model under `src/test/java/.../models/<ModelName>SerializationTests.java`.

Each test class must include:

1. **Serialization tests** — one per union variant. Construct the model with the typed setter, serialize to JSON, assert the JSON contains the expected field and value.
2. **Deserialization tests** — one per union variant. Parse a JSON string, assert the typed getter returns the correct value.
3. **Null/absent tests** — verify getters return `null` when the field is not set or absent from JSON.
4. **Round-trip tests** — serialize → deserialize → assert values match.

Use these helpers:

```java
private String serializeToJson(MyModel model) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
        model.toJson(jsonWriter);
    }
    return outputStream.toString("UTF-8");
}

private MyModel deserializeFromJson(String json) throws IOException {
    try (JsonReader jsonReader = JsonProviders.createReader(json)) {
        return MyModel.fromJson(jsonReader);
    }
}
```

### 7. Compile and run tests

```bash
mvn compile -Dbuildhelper.addtestsource.skip=true -Dbuildhelper.addtestresource.skip=true \
    -Dcodesnippet.skip=true -Dcheckstyle.skip=true -Dspotless.check.skip=true

mvn "-Dtest=*SerializationTests" test \
    -Dcodesnippet.skip=true -Dcheckstyle.skip=true -Dspotless.check.skip=true
```

All tests must pass before finishing.

## Checklist

Before reporting completion, verify:

- [ ] Every `BinaryData` property was classified as **union** or **unknown**
- [ ] Property fields left exactly as generated (no modifications)
- [ ] Original `BinaryData` getter/setter made package-private, name kept, `@Generated` removed, javadoc preserved
- [ ] `// AI Tooling: union type` placed inside the body of every modified or added getter/setter
- [ ] Typed setters added for each union variant with javadoc copied from original
- [ ] Typed getters added for each union variant (`get*As*()`) with javadoc copied from original
- [ ] All callers (samples, tests, internal code) updated to use new API
- [ ] Unused `BinaryData` imports removed from callers
- [ ] Unit tests written and passing for serialization, deserialization, null, and round-trip
- [ ] Full compilation succeeds (main + test + samples)

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `BinaryData.fromObject(jsonSerializable)` produces wrong JSON | JacksonAdapter not on classpath | Verify `azure-core` dependency includes `JacksonAdapter` |
| `toObject(AzureModel.class)` fails | JacksonAdapter doesn't find `fromJson` | Use `BinaryData.toObject()` which delegates to `JacksonAdapter.deserialize()` — confirm azure-core ≥ 1.51 |
| Setter creates `StringContent` but expects raw JSON | Wrong factory method | Use `fromString()` only for string tokens; use `fromObject()` for primitives and objects |
| Test fails on deserialized value comparison | Asymmetry between `fromString`/`fromObject` for string values | Deserialization always uses `fromObject(readUntyped())`, producing `SerializableContent`. Use `toObject(String.class)` in string getters — never `toString()` — to normalize both paths. |
| Compilation error: cannot find `List` | Missing import after adding `List<String>` setter | Add `import java.util.List;` |
| `@SuppressWarnings` needed | Unchecked cast on `toObject(List.class)` | Add `@SuppressWarnings("unchecked")` to the method |
