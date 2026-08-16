# Release Checklist

> **See also**: [Deprecation](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/deprecation.md) · [Versioning](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/versioning.md) · [aka.ms/azsdk/release-checklist](https://aka.ms/azsdk/release-checklist)

---

## Before Beta 1

- [ ] Validate the API design against the [Java Design Guidelines](https://azure.github.io/azure-sdk/java_introduction.html).
- [ ] Verify the `User-Agent` header format per [telemetry policy spec](https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy).
- [ ] Verify the `x-ms-azsdk-telemetry` header is present and correctly formed on all service client calls.
- [ ] Confirm that the package name follows the approved group ID and service-name conventions.
- [ ] Validate `pom.xml`:
  - Maven group ID and artifact ID are correct and approved.
  - Version string follows `x.y.z-beta.1`.
  - All dependencies are architect-approved.
- [ ] Confirm `module-info.java` exists and exports only the public API packages.
- [ ] All non-public API is in an `implementation` sub-package.
- [ ] All synchronous service client methods have `withResponse` overloads that accept a `Context`.
- [ ] All asynchronous service client methods translate `reactor.util.context.Context` to `com.azure.core.util.Context`.
- [ ] Distributed tracing is correctly implemented per [tracing guidelines](https://azure.github.io/azure-sdk/java_implementation.html#distributed-tracing).
- [ ] All service client builder classes have the full set of [expected builder APIs](https://azure.github.io/azure-sdk/java_introduction.html#service-client-creation).

---

## After Beta 1

- [ ] Validate that `User-Agent` telemetry data appears correctly in the telemetry dashboard.
- [ ] Validate that `x-ms-azsdk-telemetry` data appears correctly.

---

## Before Beta 2

- [ ] Apply all [required annotations](https://azure.github.io/azure-sdk/java_introduction.html#service-client):
  `@ServiceClient`, `@ServiceMethod`, `@Fluent`, `@ServiceClientBuilder`, etc.
- [ ] All public APIs log at the correct level and log sufficient context per the logging guidelines.

---

## Before Beta 3

- [ ] A complete set of [performance benchmarks](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/performance-tests.md) is committed to the repo and integrated into the CI pipeline.

---

## Before GA

- [ ] Generate HTML JavaDoc and review all documentation; every builder and service method must have a sample.
- [ ] All CheckStyle, SpotBugs, Revapi checks pass with only allowed suppressions. All suppressions are documented and reviewed.
- [ ] Re-validate `pom.xml` (same criteria as Before Beta 1).
- [ ] Test coverage is sufficiently high; any overrides that suppress coverage failure must be communicated in the PR.
- [ ] No beta-scoped production dependencies (test-scoped beta dependencies are acceptable).
- [ ] Run the BOM dependency checker if the library will be included in the BOM (see [BOM Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/bom-guidelines.md)).

---

## Release Pipeline

Trigger the release pipeline as documented at:

<https://aka.ms/azsdk/release-checklist>

For troubleshooting SDK generation pipeline failures see:
[SDK Generation Pipeline Troubleshooting](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/sdk-generation-pipeline-troubleshooting.md)
