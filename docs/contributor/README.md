# Contributor Guides

This directory contains guides for **developers building or maintaining** Azure SDK for Java libraries.

If you are a **consumer** of the SDK looking for usage guidance, start at the [User Guide Index](../user/README.md) instead.

---

## Quick Start

1. **Set up your environment** → [Getting Started](getting-started.md)
2. **Build the repo** → [Building](building.md)
3. **Run tests** → [Unit Testing](unit-testing.md) | [Live Testing](live-testing.md)
4. **Check code quality** → [Code Quality](code-quality.md)
5. **Submit a PR** → [CONTRIBUTING.md](../../CONTRIBUTING.md)

---

## All Guides

| Guide | Description |
|-------|-------------|
| [Getting Started](getting-started.md) | Install Java, Maven, configure Git; IDE recommendations |
| [Building](building.md) | `mvn` commands to build, test, and generate reports |
| [Unit Testing](unit-testing.md) | Mocking best practices, test parallelization, remote debugging |
| [Live Testing](live-testing.md) | Deploy Azure test resources and run integration tests |
| [Code Quality](code-quality.md) | CheckStyle, SpotBugs, Revapi, JaCoCo |
| [Versioning](versioning.md) | `version_client.txt`, dependency tags, incrementing versions |
| [Adding a Module](adding-a-module.md) | Create a new SDK module: dir structure, POM, versioning, CODEOWNERS |
| [TypeSpec Quickstart](typespec-quickstart.md) | End-to-end workflow: generate → build → test → release |
| [Working with AutoRest](autorest.md) | OpenAPI 2.0 / Swagger code generation options |
| [Writing Performance Tests](performance-tests.md) | Set up and run `perf-test-core` benchmarks |
| [JavaDoc & Code Snippets](javadocs.md) | Javadoc standards and codesnippet-maven-plugin workflow |
| [Access Helpers](access-helpers.md) | Cross-package internal access without public APIs |
| [BOM Guidelines](bom-guidelines.md) | How the Azure SDK BOM is structured, released, and validated |
| [Deprecation Process](deprecation.md) | Steps to mark a library deprecated and publish a final release |
| [Release Checklist](release-checklist.md) | What to do before Beta 1, Beta N, and GA |
| [Credential Scan](credential-scan.md) | Monitor and suppress CredScan warnings |
| [SDK Generation Troubleshooting](../../doc/dev/sdk-generation-pipeline-troubleshooting.md) | Diagnose auto-generation pipeline failures |
| [TypeSpec Client Customizations](../../eng/common/knowledge/customizing-client-tsp.md) | TypeSpec `client.tsp` reference |

---

## External References

- [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html)
- [Azure SDK for Java Wiki](https://github.com/Azure/azure-sdk-for-java/wiki) — complete wiki index
- [CONTRIBUTING.md](../../CONTRIBUTING.md) — PR rules, merge conventions, versioning policy
