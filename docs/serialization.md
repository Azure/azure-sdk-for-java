# Serialization


Azure Core provides two serialization abstractions:

| Abstraction | Purpose |
|-------------|---------|
| `SerializerAdapter` / `JacksonAdapter` | Default (JSON + XML) for SDK internals; not intended for replacement |
| `JsonSerializer` / `ObjectSerializer` | Pluggable interface; users and SDKs can supply custom implementations |

---

## Customizable Serialization (`JsonSerializer`)

### SPI Pattern

`JsonSerializerProvider` is a SPI loaded via `ServiceLoader`. Include one of the following on the classpath to override the default:

| Package | Implementation | Default? |
|---------|----------------|----------|
| `azure-core-serializer-json-jackson` | `JacksonJsonSerializer` | Yes (wraps JacksonAdapter) |
| `azure-core-serializer-json-gson` | `GsonJsonSerializer` | No |
| `azure-core-serializer-avro-apache` | `ApacheAvroSerializer` (experimental) | No |

### Getting an Instance

```java
// Use the SPI-discovered implementation; falls back to default if none found
JsonSerializer serializer = JsonSerializerProviders.createInstance(true);

// Disable fallback (throws if no provider on classpath)
JsonSerializer strictSerializer = JsonSerializerProviders.createInstance(false);
```

### Example: Supporting a User-Provided Serializer

```java
public final class User {
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    @JsonProperty public String firstName;
    @JsonProperty public String lastName;

    public static User fromString(String str) {
        return SERIALIZER.deserializeFromBytes(
            str.getBytes(StandardCharsets.UTF_8),
            TypeReference.createInstance(User.class));
    }

    public String toString() {
        return new String(SERIALIZER.serializeToBytes(this), StandardCharsets.UTF_8);
    }
}
```

### Example: Using `JacksonJsonSerializerBuilder` (Customized Jackson)

```java
JsonSerializer serializer = new JacksonJsonSerializerBuilder()
    .serializer(new ObjectMapper()
        .registerModule(new SimpleModule()
            .addSerializer(User.class, new UserSerializer())
            .addDeserializer(User.class, new UserDeserializer())))
    .build();
```

---

## Default Serialization (`JacksonAdapter`)

Used internally by SDK implementation code (not swappable by users):

```java
// Singleton (preferred for SDK internals)
SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

// New instance (allows customization without affecting the singleton)
SerializerAdapter adapter = new JacksonAdapter();
// Optionally customize: adapter.serializer().configure(...)
```

> ⚠️ Do not modify configuration on the `createDefaultSerializerAdapter()` singleton — it is shared across the SDK.

---

## Default Configuration Behavior

These defaults apply to both `JacksonAdapter` and `JacksonJsonSerializer` (unless customized).

### Null / Empty Properties

| Scenario | JSON | XML |
|----------|------|-----|
| Null field serialization | `JacksonAdapter`: excluded; `JacksonJsonSerializer`: written as `null` | Excluded |
| null deserialization | As null | As null |
| Empty string serialization | `""` | `<a></a>` |

### Case Sensitivity

- JSON: **case-sensitive**
- XML: **case-insensitive**

### Arrays / Collections

- Null arrays: not serialized; deserialized as `null`
- Empty arrays: serialized as `[]` (JSON) or self-closing tag `<a/>` (XML)
- Byte arrays: base64-encoded strings

### Dates and Times

| Type | Serialization format |
|------|---------------------|
| `OffsetDateTime` | UTC ISO-8601: `"2021-07-06T20:09:01.465Z"` |
| `Duration` | ISO-8601 with days: `"P1DT10H17M36.789S"` |
| `Instant` | Instant UTC ISO: `"2021-07-06T19:47:12.728Z"` |
| `UnixTime` | Epoch seconds: `1625602953.0` |
| `DateTimeRfc1123` | RFC-1123: `"Tue, 06 Jul 2021 20:31:19 GMT"` |

### `@JsonFlatten`

Allows writing nested JSON from flat model properties:

```java
@JsonFlatten
class Model {
    @JsonProperty("property.name") private String name = "foo";    // → {"property": {"name": "foo"}}
    @JsonProperty("property\\.escaped") private String esc = "baz"; // → {"property.escaped": "baz"}
}
```

### `additionalProperties`

Magic field name: a `Map<String, Object>` field named `additionalProperties` is serialized as top-level JSON properties. Prefer `@JsonAnyGetter` / `@JsonAnySetter` in performance-sensitive code (10× less overhead).

### Error Tolerance

- Unknown properties: **ignored** (no error)
- Empty bean: serialized as `{}`
- Exceptions: `JacksonJsonSerializer` logs and re-throws as `UncheckedIOException`; `JacksonAdapter` does not catch/log

---

## Serializable Models in `azure-core`

These models use the pluggable `JsonSerializerProvider`:

- `CloudEvent`
- `BinaryData`
- `RequestContent`
- `JsonPatchDocument`

Custom serializers can be passed to these models via their constructors or builders.

---

## Migration to `azure-json`

For new code and libraries migrating away from Jackson, see [Azure JSON Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/azure-json-migration.md).

---

## See Also

- [Azure JSON Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/azure-json-migration.md)
- [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md)
