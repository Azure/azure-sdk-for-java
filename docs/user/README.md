# User Guides

This directory contains guides for **consumers** of Azure SDK for Java libraries.

If you are a **developer contributing to or building** SDK libraries, start at the [Contributor Guide Index](../contributor/README.md) instead.

---

## Getting Started with the SDK

The fastest way to start is to find your service in the [list of available packages](https://azure.github.io/azure-sdk/releases/latest/all/java.html)
and follow that library's `README.md`.

All client libraries:
- Baseline on **Java 8** and are tested up to the latest Java LTS release.
- Follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).
- Share common infrastructure (retry, logging, authentication, tracing) from `azure-core`.

### Maven Dependency

Add whatever library you need. Example for Azure Blob Storage:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob</artifactId>
  <version>12.x.x</version>
</dependency>
```

Or use the **Azure SDK BOM** to manage versions automatically:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-sdk-bom</artifactId>
      <version>1.2.x</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

See [BOM Guidelines](../contributor/bom-guidelines.md) for details.

---

## User Guides

The following guides are available both as in-repo documents (full detail, offline-readable) and on the wiki.

### In-Repo Guides

| Topic | Description |
|-------|-------------|
| [Frequently Asked Questions](faq.md) | Async gotchas, `NoSuchMethodError`, dependency conflicts, Security Manager |
| [Azure Identity Examples](identity-examples.md) | All credential types: DefaultAzureCredential, service principal, managed identity, device code, CLI, chained |
| [Configuration](configuration.md) | Environment variables, `Configuration` API, HTTP client connection pool, proxy, retry |
| [Performance Tuning](performance-tuning.md) | SSL tuning, BoringSSL vs JDK SSL, connection pooling, async clients |
| [Test Proxy Migration](test-proxy-migration.md) | Migrate test recordings to the external `azure-sdk-assets` repo |
| [Azure JSON Migration](azure-json-migration.md) | Replace Jackson with `azure-json` stream-style serialization |
| [Serialization](serialization.md) | `JacksonAdapter`, custom `JsonSerializer`, default config table for JSON/XML |
| [Protocol Methods](protocol-methods.md) | Direct low-level HTTP access: `RequestOptions`, `BinaryData`, response handling |
| [Management Libraries](management.md) | ARM auth, sync/async calls, LROs, client config for `azure-resourcemanager` |
| [Azure V2 — Logging & HTTP](azure-v2.md) | `clientcore` logging best practices, OkHttp dependency for Azure V2 |

### Wiki-Only Guides

| Topic | Link |
|-------|------|
| Android support limitations | [wiki/Android-Support](https://github.com/Azure/azure-sdk-for-java/wiki/Android-Support) |
| Custom HTTP clients (plug in OkHttp, Vert.x) | [wiki/Custom-HTTP-Clients](https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients) |
| Custom JSON serializer (SPI) | [wiki/Custom-JSON-serializer](https://github.com/Azure/azure-sdk-for-java/wiki/Custom-JSON-serializer) |
| New Checkstyle and SpotBugs pattern migration | [wiki/New-Checkstyle-and-Spotbugs-pattern-migration](https://github.com/Azure/azure-sdk-for-java/wiki/New-Checkstyle-and-Spotbugs-pattern-migration) |
| Parameterized test for SDK live test | [wiki/Parameterized-test-for-SDK-live-test](https://github.com/Azure/azure-sdk-for-java/wiki/Parameterized-test-for-SDK-live-test) |
| Protocol Methods - Evolving | [wiki/Protocol-Methods---Evolving](https://github.com/Azure/azure-sdk-for-java/wiki/Protocol-Methods---Evolving) |
| Protocol Methods - Quickstart with AutoRest | [wiki/Protocol-Methods-Quickstart-with-AutoRest](https://github.com/Azure/azure-sdk-for-java/wiki/Protocol-Methods-Quickstart-with-AutoRest) |
| Spring Cloud Azure | [wiki/Spring-Cloud-Azure](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Cloud-Azure) |
| Generate code from TypeSpec (management) | [wiki/Generate-code-from-TypeSpec](https://github.com/Azure/azure-sdk-for-java/wiki/Generate-code-from-TypeSpec) |

---

## Need Help?

- Reference documentation: [aka.ms/java-docs](https://aka.ms/java-docs)
- Tutorials and quick starts: [Azure for Java Developers](https://docs.microsoft.com/java/azure/)
- File a bug or feature request: [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
- Community Q&A: [Stack Overflow `azure-java-sdk` tag](https://stackoverflow.com/questions/tagged/azure-java-sdk)
- Commercial support: [Azure Support](https://azure.microsoft.com/support/options/)
