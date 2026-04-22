# Configuration


Azure Core provides the `Configuration` class as the standard way to load, store, and share environment and runtime configuration across SDK components.

---

## Configuration API

### Get

Checks in-memory cache first, then falls back to `System.getProperty` and `System.getenv` (in that order), accepting the first non-null value.

```java
// Get as String
String proxy = configuration.get("HTTP_PROXY");

// Get with default value
String proxy = configuration.get("HTTP_PROXY", "localhost:8888");

// Get and transform to a typed value
URL proxy = configuration.get("HTTP_PROXY", endpoint -> {
    try {
        return new URL(endpoint);
    } catch (MalformedURLException ex) {
        return null;
    }
});
```

### Check Existence

```java
if (configuration.contains("HTTP_PROXY")) {
    String proxy = configuration.get("HTTP_PROXY");
} else {
    configuration.put("HTTP_PROXY", "<default proxy>");
}
```

### Put

Insert a runtime value (bypasses environment lookup):

```java
configuration.put("HTTP_PROXY", "localhost:8888");
```

### Remove

Remove from in-memory cache (causes next `get` to re-read from the environment):

```java
String removed = configuration.remove("HTTP_PROXY");
```

---

## Configuration Scoping

### Global Configuration

```java
// Shared across the entire application
Configuration global = Configuration.getGlobalConfiguration();
global.put("AZURE_LOG_LEVEL", "2"); // affects all SDK components
```

### Scoped Configuration

```java
// Applies only where this instance is passed explicitly
Configuration scopedConfig = new Configuration();
scopedConfig.put("AZURE_CLIENT_CERTIFICATE_PATH", "/my/cert.pem");

BlobServiceClient client = new BlobServiceClientBuilder()
    .configuration(scopedConfig)
    .buildClient();
```

### No-op / Empty Configuration

Use `Configuration.NONE` to prevent configuration from affecting behavior:

```java
HttpClient httpClient = new NettyAsyncHttpClientBuilder()
    .configuration(Configuration.NONE)
    .build();
```

---

## Common Environment Variables

| Variable | Description |
|----------|-------------|
| `AZURE_CLIENT_ID` | Service principal / managed identity client ID |
| `AZURE_CLIENT_SECRET` | Service principal client secret |
| `AZURE_CLIENT_CERTIFICATE_PATH` | Path to PEM/PFX certificate |
| `AZURE_TENANT_ID` | Azure AD tenant ID |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID |
| `HTTP_PROXY` / `HTTPS_PROXY` | Proxy URL (e.g. `http://proxy:8080`) |
| `NO_PROXY` | Comma-separated list of hosts to bypass proxy |
| `AZURE_LOG_LEVEL` | Logging level: 1=verbose, 2=info, 3=warn, 4=error |
| `AZURE_TELEMETRY_DISABLED` | Set to `true` to disable telemetry |
| `AZURE_TEST_MODE` | `PLAYBACK`, `RECORD`, or `LIVE` for tests |

---

## Configure HTTP Clients

### NettyAsyncHttpClient (default)

The default HTTP client uses Reactor Netty. Customize via `NettyAsyncHttpClientBuilder`:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.15.0</version>
</dependency>
```

#### Connection Pool (`ConnectionProvider`)

```java
ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
    .maxConnections(500)           // default: 500
    .maxIdleTime(Duration.ofSeconds(60))
    .maxLifeTime(Duration.ofMinutes(10))
    .pendingAcquireMaxCount(1000)  // default: 1000
    .pendingAcquireTimeout(Duration.ofSeconds(45))
    .build();

HttpClient nettyClient = new NettyAsyncHttpClientBuilder()
    .connectionProvider(connectionProvider)
    .build();
```

> When specifying `maxConnections` without a `ConnectionProvider`, it defaults to `max(16, 2 × available processors)` and `pendingAcquireMaxCount` defaults to `2 × maxConnections`.

#### Proxy

```java
ProxyOptions proxy = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("proxy", 8080))
    .setCredentials("username", "password");

HttpClient nettyClient = new NettyAsyncHttpClientBuilder()
    .proxy(proxy)
    .build();
```

### OkHttp Client

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.12.0</version>
</dependency>
```

```java
HttpClient okHttpClient = new OkHttpAsyncHttpClientBuilder().build();

BlobServiceClient client = new BlobServiceClientBuilder()
    .httpClient(okHttpClient)
    .buildClient();
```

### Use the Client in a Service Builder

```java
BlobServiceClient client = new BlobServiceClientBuilder()
    .httpClient(nettyClient)
    .buildClient();
```

---

## Retry Configuration

Retries are handled automatically by the retry policy in the HTTP pipeline. To customize:

```java
RetryOptions retryOptions = new RetryOptions(new ExponentialBackoffOptions()
    .setMaxRetries(3)
    .setBaseDelay(Duration.ofMillis(800))
    .setMaxDelay(Duration.ofSeconds(8)));

BlobServiceClient client = new BlobServiceClientBuilder()
    .retryOptions(retryOptions)
    .buildClient();
```

---

## See Also

- [Azure Identity Examples](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/identity-examples.md)
- [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/performance-tuning.md)
- [FAQ](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/faq.md)
