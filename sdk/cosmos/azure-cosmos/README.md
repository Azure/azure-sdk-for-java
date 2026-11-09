# Azure Cosmos DB SDK for Java - v5 foundation

> **Not a functional SDK. Do not publish or use this build in an application.**
> This is the first migration slice on `feature/cosmos/vnext`, not a release.
> It contains no service clients, operations, native loader, or native binaries.

The v5 successor retains `com.azure:azure-cosmos` and the `com.azure.cosmos`
Java namespace and module name. It requires Java 17 or newer. It is not designed
to coexist with v4 in the same application's dependency graph.
The [v4 implementation on main](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos)
remains available; existing v4 connectors and tests are not migrated by this change.

## Getting started

This foundation cannot connect to Azure Cosmos DB. There is deliberately no
installation snippet or client sample until a usable client exists.
Core v2 and Client Core remain ordinary external Maven dependencies; they are
not shaded into the Cosmos JAR. The final SDK is intended to contain its qualified
native driver and JNI binaries in one SDK JAR, but native packaging is not part
of this foundation.

## Building and testing

Use a checkout of this feature branch, Maven, Python 3, and JDK 17 or newer.
Set `JAVA_HOME` explicitly: the JDK used by Maven may differ from `java` on `PATH`.
From the repository root:

```bash
python3 sdk/cosmos/azure-cosmos/verify-foundation.py --maven-repository /absolute/path/to/empty/maven-repository
```

The repository path must be empty or absent. The validation builds Core and
Client Core from the same checkout into this isolated local Maven repository,
then verifies the Cosmos foundation with JUnit 5 and the inherited quality tools.
It checks the effective compiler executions, Java 17 bytecode, module boundaries,
and the SDK, sources, Javadoc, and POM artifacts. No native toolchain, credentials,
Cosmos account, or preinstalled private Core JAR is needed.

The Core versions used by this branch are not yet available from Maven Central.
A successful cold reactor build is not a claim that a standalone published
consumer can resolve the future SDK today.

## Validation and release boundaries

The feature-branch Cosmos pipeline runs only foundation validation on JDK 17,
21, and 25. It does not import release stages, deploy Maven packages, sign
packages, or publish to a dev feed. The module also disables Maven deployment.
Pipeline registration and required-check configuration must be arranged with
the repository maintainers; adding YAML does not create branch protection.

Checkstyle uses the existing `track2` ruleset because the v2 parent's `vnext`
ruleset hardcodes `com.azure.v2` and has no configurable namespace property.
This is not equivalent to the vnext ruleset: track2 permits Reactor types in
public APIs, expects the older Core logger and throw conventions, and includes
`StepVerifierCheck`; vnext permits Client Core types and instead checks
formatted exception messages, raw throws, and created-but-unthrown exceptions.
No rules are modified or suppressed. With no public API or implementation in
this slice, track2 validates the retained namespace and ordinary code style.
Before functional APIs or logging are introduced, maintainers must establish
namespace-compatible Core v2 linting. SpotBugs includes the foundation tests
because the production artifact contains only module/package metadata.

The default Cosmos reactor excludes legacy v4-dependent modules. Their sources
remain in the repository for later decisions, but the v5 foundation is not a
replacement dependency for them. The dependency-version manifest column remains
on the published v4 version; only the in-development current-version is v5.

## Key concepts

This slice establishes build and module boundaries only. No public packages are
exported. Native acquisition, ABI validation, loader behavior, and emulator
testing belong to the next migration slice, followed by service functionality.

## Examples

The pure-Java foundation tests exercise dependency loading and build metadata,
not Cosmos operations. The existing `docs/` material is historical v4 reference,
not documentation of supported v5 functionality.

## Troubleshooting

A JDK below 17 is rejected during Maven validation. Dependency download failures
must be resolved rather than bypassed with a developer's existing Maven cache.
Native or service-operation failures cannot be diagnosed using this foundation.

## Next steps

Native integration and a first functional client operation will be introduced
in independently reviewable changes. Release enablement requires separate
platform qualification, API review, and publishing approval.

## Contributing

See the [contribution guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).
Do not add deployment steps or expose placeholder client APIs to this foundation.
