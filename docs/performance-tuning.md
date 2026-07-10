# Performance Tuning

---

## SSL Tuning

By default, Azure SDKs for Java use the **Tomcat-Native Boring SSL** (BoringSSL) library for SSL operations. It is packaged as an uber-JAR containing native libraries for Linux, macOS, and Windows. Benchmark data shows Boring SSL is approximately **30% faster** than JDK SSL.

### Reduce Dependency Size

The default uber-JAR includes native libraries for all platforms. To include only the library for the current OS:

```xml
<dependencies>
  <dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative-boringssl-static</artifactId>
    <version>2.0.61.Final</version>
    <classifier>${os.detected.classifier}</classifier>
  </dependency>
</dependencies>

<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.7.1</version>
    </extension>
  </extensions>
</build>
```

### Switch to JDK SSL

To use JDK built-in SSL (e.g. to reduce JAR size further or avoid native dependency complexity), exclude Boring SSL:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.x.x</version>
  <exclusions>
    <exclusion>
      <groupId>io.netty</groupId>
      <artifactId>netty-tcnative-boringssl-static</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

> **Note:** JDK SSL is about 30% slower than Boring SSL in benchmarks.

---

## HTTP Client Connection Pooling

Proper connection pool sizing is critical for throughput. See [Configuration — Connection Pool](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/configuration.md#connection-pool-connectionprovider) for `NettyAsyncHttpClientBuilder` tuning.

Key defaults:
- Default `ConnectionProvider`: 500 max connections, 1000 pending connections
- Custom `ConnectionProvider` without `maxConnections`: defaults to `max(16, 2 × processors)`

---

## Async vs. Sync Clients

For throughput-sensitive workloads, prefer the **async client** (`*AsyncClient`). It uses non-blocking I/O via Project Reactor and does not tie up threads while waiting for responses.

```java
// Async — non-blocking, suitable for high-throughput scenarios
BlobAsyncClient blobAsync = new BlobClientBuilder()
    .endpoint(url)
    .credential(credential)
    .buildAsyncClient();

// Sync — simpler for low-concurrency scenarios
BlobClient blob = new BlobClientBuilder()
    .endpoint(url)
    .credential(credential)
    .buildClient();
```

---

## Parallelism

When processing many items, use `Flux.flatMap` with a concurrency limit:

```java
Flux.fromIterable(blobNames)
    .flatMap(name -> containerAsyncClient.getBlobAsyncClient(name).download(), 64 /* concurrency */)
    .blockLast();
```

---

## Logging Level

Verbose logging has a non-trivial cost. Set the log level to `WARN` or `ERROR` in production:

```bash
export AZURE_LOG_LEVEL=3   # WARN
```

Or configure SLF4J / Logback to restrict `com.azure` logging.

---

## Measuring Performance

Use the SDK's built-in performance test framework. See [Writing Performance Tests](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-tests.md) for the full perf-test setup.

Quick benchmark:

```bash
mvn clean package -f sdk/<service>/azure-<service>-perf/pom.xml
java -jar target/azure-<service>-perf-*-jar-with-dependencies.jar \
  <TestName> --duration 30 --parallel 8
```

---

## See Also

- [Configuration — HTTP Clients](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/configuration.md#configure-http-clients)
- [Writing Performance Tests](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-tests.md)
- [FAQ](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/faq.md)
