# Logging in the Azure SDK for Java

The Azure SDK for Java uses [SLF4J](https://www.slf4j.org/) as its logging facade
(`com.azure:azure-core` ships `azure-core-slf4j-stub` as a no-op default).
To see log output you need a concrete SLF4J binding on the classpath and then
control the log level either via the `AZURE_LOG_LEVEL` environment variable or
your logging framework's own configuration.

---

## Log Levels

| `AZURE_LOG_LEVEL` value | Level | When to use |
|------------------------|-------|-------------|
| `1` | `VERBOSE` | Full request/response bodies, diagnostic detail |
| `2` | `INFORMATIONAL` | Notable lifecycle events |
| `3` | `WARNING` | Retries, transient errors, degraded behaviour |
| `4` | `ERROR` | Failures that require intervention |

Set via environment variable:

```bash
export AZURE_LOG_LEVEL=1   # VERBOSE — all SDK log output
export AZURE_LOG_LEVEL=3   # WARN — recommended for production
```

Or at runtime before the first client is constructed:

```java
System.setProperty("AZURE_LOG_LEVEL", "1");
```

---

## Adding an SLF4J Binding

### Logback (recommended)

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.4.14</version>
</dependency>
```

Minimal `src/main/resources/logback.xml`:

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- Restrict Azure SDK to WARN in production; set to DEBUG for diagnostics -->
  <logger name="com.azure" level="WARN"/>

  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

### Log4j 2

```xml
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-slf4j2-impl</artifactId>
  <version>2.23.1</version>
</dependency>
```

Minimal `src/main/resources/log4j2.xml`:

```xml
<Configuration>
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{HH:mm:ss} %-5level %logger{36} - %msg%n"/>
    </Console>
  </Appenders>
  <Loggers>
    <Logger name="com.azure" level="warn"/>
    <Root level="info"><AppenderRef ref="Console"/></Root>
  </Loggers>
</Configuration>
```

---

## HTTP Request/Response Logging

Enable HTTP payload logging via `HttpLogOptions` on any client builder:

```java
SecretClient client = new SecretClientBuilder()
    .vaultUrl("https://my-vault.vault.azure.net")
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions()
        .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildClient();
```

Available `HttpLogDetailLevel` values:

| Value | What is logged |
|-------|---------------|
| `NONE` | Nothing (default) |
| `BASIC` | Method, URL, status code, latency |
| `HEADERS` | `BASIC` + request/response headers |
| `BODY` | `BASIC` + request/response bodies |
| `BODY_AND_HEADERS` | Everything |

> **Warning**: `BODY_AND_HEADERS` can log secrets and PII. Never enable it in
> production without sanitization in place.

### Sanitizing Headers and Query Parameters

By default the SDK redacts `Authorization`, `x-ms-encryption-key`, and a handful
of other sensitive headers. Add custom headers or query parameters to redact:

```java
HttpLogOptions logOptions = new HttpLogOptions()
    .setLogLevel(HttpLogDetailLevel.HEADERS)
    .addAllowedHeaderName("x-custom-request-id")         // allow-list to log
    .addAllowedQueryParamName("api-version");             // allow-list to log
```

Any header or query parameter **not** in the allow-list is redacted as `REDACTED`.

---

## Logging for Management Libraries

Management-plane clients (`azure-resourcemanager-*`) configure HTTP logging
via the fluent builder:

```java
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

---

## Filtering by Package

To see only logs from a specific service, scope the logger name in your
SLF4J configuration:

```xml
<!-- Logback: only Key Vault logs at DEBUG, everything else at WARN -->
<logger name="com.azure.security.keyvault" level="DEBUG"/>
<logger name="com.azure" level="WARN"/>
```

Common logger namespaces:

| Namespace | Covers |
|-----------|--------|
| `com.azure` | All Azure SDK Track 2 libraries |
| `com.azure.core` | HTTP pipeline, retry, auth |
| `com.azure.identity` | Authentication / `DefaultAzureCredential` |
| `com.azure.security.keyvault` | Key Vault clients |
| `com.azure.storage` | Storage Blob, Queue, File, Data Lake |
| `com.azure.messaging` | Event Hubs, Service Bus |
| `com.azure.resourcemanager` | Management-plane clients |

---

## Performance Impact

Verbose logging has a measurable throughput cost. For production workloads:

- Set `AZURE_LOG_LEVEL=3` (WARN) or `4` (ERROR).
- Avoid `HttpLogDetailLevel.BODY_AND_HEADERS` — body serialization is expensive.
- If using Logback, enable `asyncAppender` to move I/O off the request thread.

See [Performance Tuning](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/performance-tuning.md#logging-level) for benchmarking guidance.

---

## See Also

- [Configuration](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/configuration.md) — `AZURE_LOG_LEVEL` and other environment variables
- [Azure Identity Examples](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/identity-examples.md) — credential diagnostics
- [FAQ](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/faq.md) — common logging gotchas
- [Azure V2 — Logging Best Practices](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/azure-v2.md#logging-best-practices) — `ClientLogger` usage for SDK contributors
