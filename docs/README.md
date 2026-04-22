# Azure SDK for Java – Documentation Hub

This directory is the **canonical documentation hub** for the Azure SDK for Java repository.
It is structured so that both humans and LLM/agent tooling can navigate directly to any topic.

> **Not sure where to start?**
> - **Using** an Azure SDK library → see [User Guides](#user-guides)
> - **Contributing code** to this repo → see [Contributor Guides](#contributor-guides)
> - **Understanding repo layout** → see [Repository Structure](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/STRUCTURE.md)

---

## Repository at a Glance

| Item | Location |
|------|----------|
| Root README (overview, packages, need help) | [`/README.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/README.md) |
| Contributing rules & PR process | [`/CONTRIBUTING.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/CONTRIBUTING.md) |
| Security policy | [`/SECURITY.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/SECURITY.md) |
| Support channels | [`/SUPPORT.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/SUPPORT.md) |
| Code of Conduct | [`/CODE_OF_CONDUCT.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/CODE_OF_CONDUCT.md) |
| Per-library documentation | `sdk/<service>/<library>/README.md` |
| Per-library changelog | `sdk/<service>/<library>/CHANGELOG.md` |
| Engineering tooling & pipelines | [`/eng/`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/eng/README.md) |
| GitHub Copilot agent instructions | [`.github/copilot-instructions.md`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/.github/copilot-instructions.md) |

---

## Getting Started

The fastest way to start is to find your service in the [list of available packages](https://azure.github.io/azure-sdk/releases/latest/all/java.html)
and follow that library's `README.md`.

All client libraries:
- Baseline on **Java 8** and are tested up to the latest Java LTS release.
- Follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).
- Share common infrastructure (retry, logging, authentication, tracing) from `azure-core`.

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

For **consumers** of the Azure SDK for Java libraries:

| Guide | Description |
|-------|-------------|
| [Frequently Asked Questions](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/faq.md) | Async gotchas, dependency conflicts, Security Manager |
| [Azure Identity Examples](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/identity-examples.md) | All credential types with code samples |
| [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md) | Environment variables, HTTP client tuning, retries |
| [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/performance-tuning.md) | SSL, connection pooling, async vs. sync |
| [Test Proxy Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/test-proxy-migration.md) | Migrate test recordings to the external assets repo |
| [Azure JSON Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/azure-json-migration.md) | Replace Jackson with `azure-json` stream serialization |
| [Serialization](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/serialization.md) | `JacksonAdapter`, `JsonSerializer`, default config |
| [Protocol Methods](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/protocol-methods.md) | Direct low-level HTTP access via `RequestOptions` |
| [Management Libraries](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/management.md) | Auth, sync/async calls, LROs for ARM libraries |
| [Azure V2 — Logging & HTTP](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/azure-v2.md) | `clientcore` logging best practices, OkHttp |
| [Using the SDK with AI tools](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/sdk_and_ai.md) | Copilot, MCP server, Azure SDK skills, AI coding tools |


---

## Contributor Guides

For **developers building or maintaining** SDK libraries:

| Guide | Description |
|-------|-------------|
| [Contributor Guide Index](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/README.md) | Index of all contributor guides |
| [Getting Started](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/getting-started.md) | Set up your dev environment (Java, Maven, Git, IDE) |
| [Building](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/building.md) | Build commands, skipping analysis, HTML reports |
| [Unit Testing](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/unit-testing.md) | Mocking, test parallelization, remote debugging |
| [Live Testing](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/live-testing.md) | Deploy test resources and run live tests |
| [Code Quality](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/code-quality.md) | CheckStyle, SpotBugs, Revapi, JaCoCo |
| [Versioning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/versioning.md) | Version files, dependency tags, incrementing |
| [Adding a Module](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/adding-a-module.md) | Create a new SDK module in the repo |
| [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md) | End-to-end workflow: generate, build, test, release |
| [Working with AutoRest](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/autorest.md) | OpenAPI 2.0 / Swagger code generation |
| [Writing Performance Tests](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/performance-tests.md) | `perf-test-core` benchmarking framework |
| [JavaDoc & Code Snippets](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/javadocs.md) | Javadoc standards + codesnippet plugin |
| [Access Helpers](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/access-helpers.md) | Cross-package internal access without public APIs |
| [Deprecation Process](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/deprecation.md) | How to mark a library deprecated and release it |
| [BOM Guidelines](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/bom-guidelines.md) | Azure SDK BOM (bill of materials) guidelines |
| [Release Checklist](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/release-checklist.md) | Pre-release checklist from Beta 1 through GA |
| [Credential Scan](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/credential-scan.md) | Monitor and fix CredScan warnings |
| [SDK Generation Pipeline Troubleshooting](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/sdk-generation-pipeline-troubleshooting.md) | Diagnose SDK auto-generation pipeline failures |

---

## Documentation Placement Decisions

See [STRUCTURE.md](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/STRUCTURE.md) for the full rationale on what lives here versus in `eng/` or alongside individual SDK libraries.

---

## Need Help?

- Reference documentation: [aka.ms/java-docs](https://aka.ms/java-docs)
- Tutorials and quick starts: [Azure for Java Developers](https://docs.microsoft.com/java/azure/)
- File a bug or feature request: [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
- Community Q&A: [Stack Overflow `azure-java-sdk` tag](https://stackoverflow.com/questions/tagged/azure-java-sdk)
- Commercial support: [Azure Support](https://azure.microsoft.com/support/options/)

---

## Documentation

The `docs/` directory is the authoritative source for contributor and user documentation.
Key topics previously on the GitHub Wiki have been migrated here.
