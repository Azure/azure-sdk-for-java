# Azure SDK for Java – Documentation Hub

This directory is the **canonical documentation hub** for the Azure SDK for Java repository.
It is structured so that both humans and LLM/agent tooling can navigate directly to any topic.

> **Not sure where to start?**
> - **Using** an Azure SDK library → see [User Guides](#user-guides)
> - **Contributing code** to this repo → see [Contributor Guides](#contributor-guides)
> - **Understanding repo layout** → see [Repository Structure](STRUCTURE.md)

---

## Repository at a Glance

| Item | Location |
|------|----------|
| Root README (overview, packages, need help) | [`/README.md`](../README.md) |
| Contributing rules & PR process | [`/CONTRIBUTING.md`](../CONTRIBUTING.md) |
| Security policy | [`/SECURITY.md`](../SECURITY.md) |
| Support channels | [`/SUPPORT.md`](../SUPPORT.md) |
| Code of Conduct | [`/CODE_OF_CONDUCT.md`](../CODE_OF_CONDUCT.md) |
| Per-library documentation | `sdk/<service>/<library>/README.md` |
| Per-library changelog | `sdk/<service>/<library>/CHANGELOG.md` |
| Engineering tooling & pipelines | [`/eng/`](../eng/README.md) |
| GitHub Copilot agent instructions | [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) |
| Legacy developer docs | [`/doc/`](../doc/README.md) |

---

## User Guides

For **consumers** of the Azure SDK for Java libraries:

| Guide | Description |
|-------|-------------|
| [User Guide Index](user/README.md) | Index of all user-facing guides |
| [Frequently Asked Questions](user/faq.md) | Async gotchas, dependency conflicts, Security Manager |
| [Azure Identity Examples](user/identity-examples.md) | All credential types with code samples |
| [Configuration](user/configuration.md) | Environment variables, HTTP client tuning, retries |
| [Performance Tuning](user/performance-tuning.md) | SSL, connection pooling, async vs. sync |
| [Test Proxy Migration](user/test-proxy-migration.md) | Migrate test recordings to the external assets repo |
| [Azure JSON Migration](user/azure-json-migration.md) | Replace Jackson with `azure-json` stream serialization |
| [Serialization](user/serialization.md) | `JacksonAdapter`, `JsonSerializer`, default config |
| [Protocol Methods](user/protocol-methods.md) | Direct low-level HTTP access via `RequestOptions` |
| [Management Libraries](user/management.md) | Auth, sync/async calls, LROs for ARM libraries |
| [Azure V2 — Logging & HTTP](user/azure-v2.md) | `clientcore` logging best practices, OkHttp |
| [Custom HTTP Clients](https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients) | Plug in OkHttp, Vert.x, etc. (wiki) |
| [Custom JSON Serializer](https://github.com/Azure/azure-sdk-for-java/wiki/Custom-JSON-serializer) | Provide your own `JsonSerializer` (wiki) |
| [Android Support](https://github.com/Azure/azure-sdk-for-java/wiki/Android-Support) | Android compatibility notes (wiki) |
| [Spring Cloud Azure](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Cloud-Azure) | Spring Boot / Spring Cloud integration (wiki) |

---

## Contributor Guides

For **developers building or maintaining** SDK libraries:

| Guide | Description |
|-------|-------------|
| [Contributor Guide Index](contributor/README.md) | Index of all contributor guides |
| [Getting Started](contributor/getting-started.md) | Set up your dev environment (Java, Maven, Git, IDE) |
| [Building](contributor/building.md) | Build commands, skipping analysis, HTML reports |
| [Unit Testing](contributor/unit-testing.md) | Mocking, test parallelization, remote debugging |
| [Live Testing](contributor/live-testing.md) | Deploy test resources and run live tests |
| [Code Quality](contributor/code-quality.md) | CheckStyle, SpotBugs, Revapi, JaCoCo |
| [Versioning](contributor/versioning.md) | Version files, dependency tags, incrementing |
| [Adding a Module](contributor/adding-a-module.md) | Create a new SDK module in the repo |
| [TypeSpec Quickstart](contributor/typespec-quickstart.md) | End-to-end workflow: generate, build, test, release |
| [Working with AutoRest](contributor/autorest.md) | OpenAPI 2.0 / Swagger code generation |
| [Writing Performance Tests](contributor/performance-tests.md) | `perf-test-core` benchmarking framework |
| [JavaDoc & Code Snippets](contributor/javadocs.md) | Javadoc standards + codesnippet plugin |
| [Access Helpers](contributor/access-helpers.md) | Cross-package internal access without public APIs |
| [Deprecation Process](contributor/deprecation.md) | How to mark a library deprecated and release it |
| [BOM Guidelines](contributor/bom-guidelines.md) | Azure SDK BOM (bill of materials) guidelines |
| [Release Checklist](contributor/release-checklist.md) | Pre-release checklist from Beta 1 through GA |
| [Credential Scan](contributor/credential-scan.md) | Monitor and fix CredScan warnings |
| [SDK Generation Pipeline Troubleshooting](../doc/dev/sdk-generation-pipeline-troubleshooting.md) | Diagnose SDK auto-generation pipeline failures |

---

## Documentation Placement Decisions

See [STRUCTURE.md](STRUCTURE.md) for the full rationale on what lives here versus in `eng/`, `doc/`, or alongside individual SDK libraries.

---

## Wiki

The GitHub Wiki at <https://github.com/Azure/azure-sdk-for-java/wiki> contains additional
reference material and migration guides. Where key wiki topics have in-repo summaries, this
directory is the authoritative source; the wiki remains the long-form reference.

> **Wiki clone URL** (for offline access):  
> `https://github.com/Azure/azure-sdk-for-java.wiki.git`
