# Azure JSON Migration


`com.azure:azure-json` replaces Jackson Databind as the serialization layer for Azure SDK models. This guide explains why and how to migrate.

---

## Why Migrate?

1. **No external dependencies** — `azure-json` has zero dependencies, eliminating Jackson version conflicts
2. **Easier to debug** — stream serialization makes the read/write flow explicit and traceable
3. **Less reflection** — fewer `--add-opens` requirements; works cleanly with Java module system
4. **Pluggable** — the backing implementation can be swapped (GSON, Jackson, or the default)
5. **Potential JAR size reduction and performance gains**

---

## Migration Steps

### 1. Update `pom.xml`

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-json</artifactId>
  <version>1.3.0</version> <!-- {x-version-update;com.azure:azure-json;dependency} -->
</dependency>
```

### 2. Update `module-info.java`

```java
requires com.azure.json;
```

Remove any `opens ... to com.fasterxml.jackson.databind` — replace with `opens ... to com.azure.core`:

```java
// Before
opens com.azure.sdk.models to com.fasterxml.jackson.databind;
opens com.azure.sdk.implementation to com.fasterxml.jackson.databind, com.azure.core;

// After
opens com.azure.sdk.models to com.azure.core;
opens com.azure.sdk.implementation to com.azure.core;
```

Also remove any `--add-exports`/`--add-opens`/`--add-reads` for `com.fasterxml.jackson.databind` in `javaModulesSurefireArgLine`.

---

## 3. Update Code Generation

### Swagger / AutoRest

Add `stream-style-serialization: true` to your AutoRest configuration:

```yaml
stream-style-serialization: true
use: '@autorest/java@4.1.x'  # optional: pin version
```

### TypeSpec

Stream-style serialization is enabled by default in recent versions of TypeSpec-Java. If your spec has `stream-style-serialization: false`, remove that line and update `tsp-location.yaml` to point to the updated commit.

---

## 4. Update Handwritten Code

### Before (Jackson annotations)

```java
public final class MyModel {
    @JsonProperty(value = "DefaultEncryptionScope")
    private String defaultEncryptionScope;

    @JsonProperty(value = "EncryptionScopeOverridePrevented")
    private Boolean encryptionScopeOverridePrevented;
}
```

### After (azure-json `JsonSerializable`)

```java
public final class MyModel implements JsonSerializable<MyModel> {
    private String defaultEncryptionScope;
    private Boolean encryptionScopeOverridePrevented;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("DefaultEncryptionScope", defaultEncryptionScope);
        jsonWriter.writeBooleanField("EncryptionScopeOverridePrevented", encryptionScopeOverridePrevented);
        return jsonWriter.writeEndObject();
    }

    public static MyModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MyModel model = new MyModel();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("DefaultEncryptionScope".equals(fieldName)) {
                    model.defaultEncryptionScope = reader.getString();
                } else if ("EncryptionScopeOverridePrevented".equals(fieldName)) {
                    model.encryptionScopeOverridePrevented = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }
            return model;
        });
    }
}
```

> `toJson` does not need to call `JsonWriter.flush()` — the caller handles that.

**Finding all Jackson usages:**

```bash
grep -r "com.fasterxml.jackson" src/main/java/
```

---

## 5. Update Build Configuration

### Test Coverage

Stream serialization is more verbose (lines of code vs annotations), so coverage may drop. Either add tests or lower minimum thresholds for beta:

```xml
<properties>
  <jacoco.min.linecoverage>0.30</jacoco.min.linecoverage>
  <jacoco.min.branchcoverage>0.25</jacoco.min.branchcoverage>
</properties>
```

### RevApi Suppressions

Removing Jackson annotations is a breaking change per RevApi. Add suppressions to `eng/code-quality-reports/src/main/resources/revapi/revapi.json`:

```json
{
  "regex": true,
  "code": "java\\.annotation\\.removed",
  "old": ".*? com\\.azure\\.myservice\\.models.*",
  "justification": "Removing Jackson annotations in transition to azure-json stream-style."
}
```

---

## 6. Finalize

Run the full test suite in both playback and live modes:

```bash
mvn verify -f sdk/<service>/<module>/pom.xml
```

---

## See Also

- [Serialization](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/serialization.md) — architecture of `azure-json` vs `JacksonAdapter`
- [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md) — RevApi suppressions
- [Working with AutoRest](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/autorest.md)
