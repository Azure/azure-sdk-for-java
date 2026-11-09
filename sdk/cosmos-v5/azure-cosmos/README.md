# Azure Cosmos DB SDK for Java - v5 foundation

> **Not a functional SDK. Do not publish or use this build in an application.**
> This is the first migration slice on `feature/cosmos/vnext`, not a release.
> It contains no service clients, operations, native loader, or native binaries.

The isolated v5 foundation retains `com.azure:azure-cosmos` and the `com.azure.cosmos`
Java namespace and module name. It requires Java 17 or newer. It is not designed
to coexist with v4 in the same application's dependency graph.
The existing `sdk/cosmos` v4 implementation, reactor, CI, version registration,
connectors, and tests are unchanged. The two versions occupy separate source
directories but must not be selected in the same Maven reactor or application.

## Getting started

This foundation cannot connect to Azure Cosmos DB. There is deliberately no
installation snippet or client sample until a usable client exists.
Core v2 and Client Core remain ordinary external Maven dependencies; they are
not shaded into the Cosmos JAR. The final SDK is intended to contain its qualified
native driver and JNI binaries in one SDK JAR, but native packaging is not part
of this foundation.

## Building and testing

Use a checkout of this feature branch, Maven, Python 3.9 or newer, and JDK 17 or newer.
Set `JAVA_HOME` explicitly: the JDK used by Maven may differ from `java` on `PATH`.
From the repository root:

```bash
python3 sdk/cosmos-v5/azure-cosmos/verify-foundation.py --maven-repository /absolute/path/to/empty/maven-repository
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

### Opt-in build and version configuration

`sdk/cosmos-v5/reactor.xml` is a dedicated reactor for this foundation and its
same-checkout Core dependencies. It is not included in the repository's root
reactor. The SDK's Maven build descriptor is `foundation.xml`, not `pom.xml`;
invoke it explicitly with `mvn -f sdk/cosmos-v5/azure-cosmos/foundation.xml`
after bootstrapping the dependencies. The validation script performs both steps.

These alternate filenames intentionally exclude v5 from the shared `pom*.xml`
discovery used by aggregate builds, version updates, and version scanning.
`sdk/cosmos-v5/versions.txt` supplies the isolated Cosmos version; it is **not**
an additional registration in `eng/versioning/version_client.txt`. The validation
script uses the existing version-update implementation on a temporary copy to
check the alternate descriptor against this local override and the shared
dependency/plugin versions, failing on drift without rewriting source files.
Configuration regression tests also exercise actual shared aggregate discovery
and check that the default and opt-in reactors each select only their own Cosmos
version. Run them independently with:

```bash
python3 -B -m unittest discover -s sdk/cosmos-v5/azure-cosmos -p test_foundation_configuration.py
```

When changing the v5 version, update both the local manifest and `foundation.xml`.
Do not run the shared version updater's `--target-file` option on this descriptor:
without the local override it would apply the v4 version.

The v5 CI `Artifacts` and `AdditionalModules` lists are empty intentionally, so
shared package/release discovery still registers only v4. CI uploads validation
outputs, not releasable SDK packages. Do not rename these descriptors to
`pom*.xml`, register this SDK in shared release metadata, or add this reactor
to the root build while the same-GA v4 source remains selected there.

## Validation and release boundaries

The separate `sdk/cosmos-v5/ci.yml` pipeline runs only foundation validation on JDK 17,
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

The default Cosmos reactor and both shared version-manifest columns remain on
v4. The v5 foundation is not a replacement dependency for any existing consumer.
The dedicated validation reactor never includes the v4 SDK or its consumers.

## Key concepts

This slice establishes build and module boundaries only. No public packages are
exported. Native acquisition, ABI validation, loader behavior, and emulator
testing belong to the next migration slice, followed by service functionality.

## Examples

The pure-Java foundation tests exercise dependency loading and build metadata,
not Cosmos operations. No v4 samples or documentation are copied into v5.

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
