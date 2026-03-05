---
name: tsp-type-override
description: Override TypeSpec types with Java-native types (e.g. OffsetDateTime, DayOfWeek) using @@alternateType in a client.java.tsp file. Use when a TypeSpec model field has an incorrect or too-generic type that should map to a specific Java type.
---

# TypeSpec Type Override for Java

Override types in generated Java code by adding `@@alternateType` decorators to the `client.java.tsp` file.

## Preconditions
- You must be in the directory that contains `tsp-location.yaml`.
- The TypeSpec must already be synced locally into `TempTypeSpecFiles/`. If not, run `tsp-client sync` first.
- Identify the `client.java.tsp` file inside `TempTypeSpecFiles/`. This is the **only** file you should edit.

## Important: TempTypeSpecFiles is volatile

`TempTypeSpecFiles/` is a **transient working directory** managed by `tsp-client`. It is regenerated on every `tsp-client sync` or `tsp-client update` and is typically gitignored. **Any changes made only in `TempTypeSpecFiles/` will be lost** on the next sync/update cycle.

If the user provides a **local checkout of `Azure/azure-rest-api-specs`**, always apply the same `client.java.tsp` edits there so the changes are preserved and can be committed to a PR. The path to the spec file inside that repo can be derived from `tsp-location.yaml`:
- `directory` field gives the relative path (e.g. `specification/ai-foundry/data-plane/Foundry`)
- The file to edit is `client.java.tsp` inside that directory

## Workflow

### 1. Locate the TypeSpec model and field

Search the `.tsp` files under `TempTypeSpecFiles/` for the model and field the user wants to override:

```bash
grep -rn "<ModelName>\|<fieldName>" TempTypeSpecFiles/ --include="*.tsp"
```

Read the model definition to confirm the current type of the target field (e.g. `string`, `int32`, a union, etc.).

### 2. Determine the correct decorator form

There are **two forms** of `@@alternateType`. Choose based on the target type:

#### Form A — TypeSpec built-in type (preferred when possible)

Use when there is a TypeSpec scalar that the Java emitter already maps to the desired Java type.

| Desired Java type     | TypeSpec alternate     |
|-----------------------|------------------------|
| `OffsetDateTime`      | `utcDateTime`          |
| `Duration`            | `duration`             |
| `byte[]`              | `bytes`                |
| `long` / `Long`       | `int64`                |
| `double` / `Double`   | `float64`              |

Syntax (applied to a **model property**, scoped to Java):

```tsp
@@alternateType(ModelName.fieldName, utcDateTime, "java");
```

This form is **fully supported** on model properties.

#### Form B — External Java type via identity

Use when no TypeSpec scalar maps to the desired Java type (e.g. `java.time.DayOfWeek`).

Syntax (applied to the **type definition itself**, not a property):

```tsp
@@alternateType(TypeName, { identity: "fully.qualified.ClassName" }, "java");
```

> **Important constraints for external types:**
> - External types (`{ identity: ... }`) **cannot** be applied to model properties — they must target the type definition (Model, Enum, Union, Scalar).
> - A `scope` parameter (e.g. `"java"`) is **required** for external types.
> - **Known limitation (as of typespec-java 0.39.x):** The Java emitter does not fully support external types on Enum/Union definitions. It will still generate the class instead of referencing the JDK type. This is tracked as a bug. Only use Form B for Model types until the emitter is fixed.

### 3. Apply the override

Edit the `client.java.tsp` file inside `TempTypeSpecFiles/`. Add the decorator(s) under the type-replacement section (usually at the bottom of the file):

```tsp
// EvaluatorVersion datetime fields are typed as string in the spec
@@alternateType(EvaluatorVersion.created_at, utcDateTime, "java");
```

### 4. Generate and verify

Always generate with `--save-inputs` so the edited TypeSpec files are preserved:

```bash
tsp-client generate --save-inputs
```

After generation, verify the Java source uses the expected type:

```bash
grep -n "OffsetDateTime\|DayOfWeek\|<ExpectedType>" src/main/java/com/azure/ai/projects/models/<ModelName>.java
```

Check that:
- The field type changed (e.g. `private OffsetDateTime createdAt;`)
- The getter return type changed (e.g. `public OffsetDateTime getCreatedAt()`)
- The JSON deserialization uses the correct parser (e.g. `CoreUtils.parseBestOffsetDateTime`)
- No spurious files were generated (e.g. under `src/main/java/java/`)

### 5. Write unit tests for serialization/deserialization

After generation, **always** write a unit test that verifies the generated model serializes and deserializes the overridden type to the **same wire-format values** defined in the original TypeSpec. This is critical because the emitter may generate serialization code that does not match the API wire format (e.g. `java.time.DayOfWeek.name()` produces `"MONDAY"` but the TypeSpec union defined `"Monday"`).

Place the test class under `src/test/java/` in the model's package (e.g. `com.azure.ai.projects.models`).

The test must cover three scenarios:

1. **Serialization** — Construct the model with the Java type, serialize to JSON, and assert the JSON string values match the TSP-defined wire format (e.g. PascalCase `"Monday"`, not UPPER_CASE `"MONDAY"`).
2. **Deserialization** — Parse a JSON string using the TSP-defined wire-format values and assert the Java type is correctly populated.
3. **Round-trip** — Serialize → deserialize and assert the original values are preserved.

Example test skeleton:

```java
@Test
void serializationProducesWireFormatValues() throws IOException {
    // Build model with the Java type
    var schedule = new WeeklyRecurrenceSchedule(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    String json = toJsonString(schedule);
    // Assert the wire values match the TSP union/enum values, NOT the Java enum constant names
    String expected = "{\"daysOfWeek\":[\"Monday\",\"Friday\"],\"type\":\"Weekly\"}";
    assertEquals(expected, json);
}

@Test
void deserializationParsesWireFormatValues() throws IOException {
    // Use TSP-defined wire-format values
    String json = "{\"daysOfWeek\":[\"Monday\",\"Wednesday\"],\"type\":\"Weekly\"}";
    WeeklyRecurrenceSchedule schedule;
    try (JsonReader reader = JsonProviders.createReader(json)) {
        schedule = WeeklyRecurrenceSchedule.fromJson(reader);
    }
    assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), schedule.getDaysOfWeek());
}

@Test
void roundTripPreservesValues() throws IOException {
    var original = new WeeklyRecurrenceSchedule(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY));
    String json = toJsonString(original);
    WeeklyRecurrenceSchedule deserialized;
    try (JsonReader reader = JsonProviders.createReader(json)) {
        deserialized = WeeklyRecurrenceSchedule.fromJson(reader);
    }
    assertEquals(original.getDaysOfWeek(), deserialized.getDaysOfWeek());
}
```

#### If the tests fail: customize toJson/fromJson

When the emitter generates incorrect serialization (e.g. `element.name()` instead of PascalCase), you must manually fix the `toJson` and `fromJson` methods in the generated model class:

1. **Remove the `@Generated` annotation** from `toJson` and `fromJson`. This ensures your customizations survive future `tsp-client generate` / `tsp-client update` runs — the codegen will not overwrite methods that lack `@Generated`.
2. Fix the serialization logic to convert between the Java type and the TSP wire format. For example, for `java.time.DayOfWeek`:
   - **`toJson`**: convert `DayOfWeek.MONDAY` → `"Monday"` (PascalCase) using a helper like:
     ```java
     private static String toPascalCase(DayOfWeek day) {
         String name = day.name();
         return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
     }
     ```
   - **`fromJson`**: convert `"Monday"` → `DayOfWeek.MONDAY` by uppercasing before `valueOf()`:
     ```java
     DayOfWeek.valueOf(reader.getString().toUpperCase(Locale.ROOT))
     ```
3. Re-run the unit tests and confirm all three scenarios pass.

### 6. Apply changes to the local spec repo (if provided)

If the user supplied a local checkout path for `Azure/azure-rest-api-specs`, apply the **same edits** to the `client.java.tsp` there. Derive the file path from `tsp-location.yaml`:

```
<local_spec_repo>/<directory>/client.java.tsp
```

For example, if `directory: specification/ai-foundry/data-plane/Foundry` and the local repo is at `~/code/azure-rest-api-specs`:

```
~/code/azure-rest-api-specs/specification/ai-foundry/data-plane/Foundry/client.java.tsp
```

Verify the file exists before editing. If it doesn't, warn the user and print the expected path.

### 7. Remind the user about the spec PR

After confirming the generated code is correct, remind the user:

> The `@@alternateType` changes in `client.java.tsp` are local overrides in `TempTypeSpecFiles/`.
> For these to persist across future code generations, the same changes must be contributed to the
> **Azure/azure-rest-api-specs** repository via a pull request targeting the corresponding
> `client.java.tsp` file under the `specification/` directory.
>
> Build the PR URL from `tsp-location.yaml`:
> - Repo: `repo` field (e.g. `Azure/azure-rest-api-specs`)
> - Directory: `directory` field (e.g. `specification/ai-foundry/data-plane/Foundry`)
> - File: `client.java.tsp` in that directory

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Generated class still uses `String` | Decorator not picked up | Verify the model/field names match exactly (case-sensitive, use the TypeSpec name, not the Java name) |
| File generated under `src/main/java/java/time/...` | External type identity used on an Enum/Union | Remove the decorator — this is the known emitter bug for Enum/Union external types |
| Compiler error on `@@alternateType` | Wrong target kind | External types must target type definitions, not properties. TypeSpec built-ins can target properties. |
| Warning: `external-type-on-model-property` | External type `{ identity: ... }` applied to a property | Move the decorator to the type definition instead |
