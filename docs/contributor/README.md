# Contributor Guides

This directory contains guides for **developers building or maintaining** Azure SDK for Java libraries.

If you are a **consumer** of the SDK looking for usage guidance, start at the [User Guide Index](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/README.md) instead.

---

## Quick Start

1. **Set up your environment** → [Getting Started](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/getting-started.md)
2. **Build the repo** → [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md)
3. **Run tests** → [Unit Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/unit-testing.md) | [Live Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md)
4. **Check code quality** → [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md)
5. **Submit a PR** → [CONTRIBUTING.md](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md)

---

## All Guides

| Guide | Description |
|-------|-------------|
| [Getting Started](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/getting-started.md) | Install Java, Maven, configure Git; IDE recommendations |
| [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md) | `mvn` commands to build, test, and generate reports |
| [Unit Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/unit-testing.md) | Mocking best practices, test parallelization, remote debugging |
| [Live Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md) | Deploy Azure test resources and run integration tests |
| [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md) | CheckStyle, SpotBugs, Revapi, JaCoCo |
| [Versioning](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/versioning.md) | `version_client.txt`, dependency tags, incrementing versions |
| [Adding a Module](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/adding-a-module.md) | Create a new SDK module: dir structure, POM, versioning, CODEOWNERS |
| [TypeSpec Quickstart](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/typespec-quickstart.md) | End-to-end workflow: generate → build → test → release |
| [Working with AutoRest](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/autorest.md) | OpenAPI 2.0 / Swagger code generation options |
| [Writing Performance Tests](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-tests.md) | Set up and run `perf-test-core` benchmarks |
| [JavaDoc & Code Snippets](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md) | Javadoc standards and codesnippet-maven-plugin workflow |
| [Access Helpers](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/access-helpers.md) | Cross-package internal access without public APIs |
| [BOM Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/bom-guidelines.md) | How the Azure SDK BOM is structured, released, and validated |
| [Deprecation Process](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/deprecation.md) | Steps to mark a library deprecated and publish a final release |
| [Release Checklist](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/release-checklist.md) | What to do before Beta 1, Beta N, and GA |
| [Credential Scan](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/credential-scan.md) | Monitor and suppress CredScan warnings |
| [SDK Generation Troubleshooting](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/sdk-generation-pipeline-troubleshooting.md) | Diagnose auto-generation pipeline failures |
| [TypeSpec Client Customizations](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/knowledge/customizing-client-tsp.md) | TypeSpec `client.tsp` reference |

---

## External References

- [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html)
- [CONTRIBUTING.md](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) — PR rules, merge conventions, versioning policy
