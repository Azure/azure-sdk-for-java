# Azure V2 — Logging Best Practices and HTTP Clients


Azure V2 refers to the next-generation `io.clientcore` packages. This page covers guidance specifically for code that uses `clientcore` rather than `com.azure`.

---

## Logging Best Practices

These guidelines extend the general [Azure SDK exception policy](https://azure.github.io/azure-sdk/java_introduction.html).

### 1. Always Log When Creating New Exceptions

```java
// ✅ Good
throw logger.throwableAtError()
    .log("This is a test exception", CoreException::from);

// ❌ Bad — exception is created without logging
throw new RuntimeException("This is a test exception");
```

### 2. Trivial Input Validation Does Not Need Logging

```java
public MyClientOptions setName(String name) {
    Objects.requireNonNull(name, "'name' cannot be null.");  // implicit NullPointerException is fine
    if (name.isEmpty()) {
        throw new IllegalArgumentException("'name' cannot be empty");  // no logger needed
    }
    return this;
}
```

DO log exceptions in non-trivial cases (e.g. validating network responses).

### 3. Add Context to Exceptions with Key-Value Pairs

```java
// ✅ Good — structured context helps log analysis
try {
    uploadBlob(containerId, blobId, data);
} catch (RuntimeException ex) {
    throw logger.throwableAtError()
        .addKeyValue("blobId", blobId)
        .addKeyValue("containerId", containerId)
        .addKeyValue("endpoint", endpoint)
        .log("Error when uploading blob", ex, CoreException::from);
}
```

This produces a structured exception message:

```
CoreException: Error when uploading blob; {"endpoint":"www.xyz.com",
  "blobId":"foo","containerId":"bar","cause.type":"java.net.UnknownHostException",
  "cause.message":"Unable to resolve host www.xyz.com"}
```

And a matching structured log record:

```json
{"message":"Error when uploading blob","endpoint":"www.xyz.com",
  "blobId":"foo","containerId":"bar","cause.type":"java.net.UnknownHostException"}
```

### 4. Don't Log Exceptions Twice as They Bubble Up

```java
// ✅ Good — log only at the point of origin
try {
    return createIfNotExist(resourceId);
} catch (HttpResponseException ex) {
    if (ex.getResponse().getStatusCode() == 409) {
        return ex.getResponse();    // expected case — no need to rethrow
    }
    throw ex;                       // rethrow without re-logging
}
```

### 5. Don't Create Exceptions You Don't Throw

```java
// ✅ Good
throw logger.throwableAtError().log("File does not exist", CoreException::from);

// ❌ Bad — creates exception but doesn't throw it
logger.throwableAtError().log("File does not exist", CoreException::from);

// ❌ Bad — fake cause
throw logger.throwableAtError()
    .log("File does not exist", new FileNotFoundException(), CoreException::from);
```

### Logging Without Exceptions

```java
logger.atError()
    .addKeyValue("providerType", providerType)
    .addKeyValue("supportedTypes", KNOWN_TYPES)
    .log("Unknown provider type.");
```

---

## HTTP Clients (Azure V2 / `clientcore`)

### Default HTTP Client

`clientcore` includes a default HTTP client based on Java's built-in `HttpClient` (Java 11+). No additional dependencies are required.

### OkHttp Client

To use OkHttp instead:

```xml
<dependency>
  <groupId>io.clientcore</groupId>
  <artifactId>http-okhttp3</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```

```java
HttpClient okHttpClient = new OkHttpHttpClientBuilder().build();
```

---

## See Also

- [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md#configure-http-clients) — for `com.azure` / `azure-core-http-netty` configuration
- [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/performance-tuning.md)
- General [Azure SDK exception policy](https://azure.github.io/azure-sdk/java_introduction.html)
