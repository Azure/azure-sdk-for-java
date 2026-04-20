# User Guides

This directory contains guides for **consumers** of Azure SDK for Java libraries.

If you are a **developer contributing to or building** SDK libraries, start at the [Contributor Guide Index](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/README.md) instead.

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

See [BOM Guidelines](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/bom-guidelines.md) for details.

---

## User Guides

The following guides are available both as in-repo documents (full detail, offline-readable) and on the wiki.

### In-Repo Guides

| Topic | Description |
|-------|-------------|
| [Frequently Asked Questions](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/faq.md) | Async gotchas, `NoSuchMethodError`, dependency conflicts, Security Manager |
| [Azure Identity Examples](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/identity-examples.md) | All credential types: DefaultAzureCredential, service principal, managed identity, device code, CLI, chained |
| [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/configuration.md) | Environment variables, `Configuration` API, HTTP client connection pool, proxy, retry |
| [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/performance-tuning.md) | SSL tuning, BoringSSL vs JDK SSL, connection pooling, async clients |
| [Test Proxy Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/test-proxy-migration.md) | Migrate test recordings to the external `azure-sdk-assets` repo |
| [Azure JSON Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/azure-json-migration.md) | Replace Jackson with `azure-json` stream-style serialization |
| [Serialization](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/serialization.md) | `JacksonAdapter`, custom `JsonSerializer`, default config table for JSON/XML |
| [Protocol Methods](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/protocol-methods.md) | Direct low-level HTTP access: `RequestOptions`, `BinaryData`, response handling |
| [Management Libraries](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/management.md) | ARM auth, sync/async calls, LROs, client config for `azure-resourcemanager` |
| [Azure V2 — Logging & HTTP](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/user/azure-v2.md) | `clientcore` logging best practices, OkHttp dependency for Azure V2 |

---

## Need Help?

- Reference documentation: [aka.ms/java-docs](https://aka.ms/java-docs)
- Tutorials and quick starts: [Azure for Java Developers](https://docs.microsoft.com/java/azure/)
- File a bug or feature request: [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
- Community Q&A: [Stack Overflow `azure-java-sdk` tag](https://stackoverflow.com/questions/tagged/azure-java-sdk)
- Commercial support: [Azure Support](https://azure.microsoft.com/support/options/)
