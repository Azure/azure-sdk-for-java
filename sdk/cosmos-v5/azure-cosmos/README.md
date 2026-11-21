# Azure Cosmos DB SDK for Java - v5

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

### Developer setup

Ordinary Java development uses **JDK and Maven only**. Python is needed for the
additional cold-build/isolation validation below, not to compile or run the Java
tests. Use a checkout of `feature/cosmos/vnext` or a PR branch based on it, and
run all commands from the repository root.

Install a [JDK](https://learn.microsoft.com/java/openjdk/download) (17, 21, or 25)
and extract the [Maven 3.9 binary distribution](https://maven.apache.org/download.cgi).
Choose the JDK download matching your OS and CPU architecture. Set the paths
below to your installation directories; `JAVA_HOME` must name the JDK, not its
`bin` directory. These settings apply to the current terminal.

**macOS (zsh or Bash):**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export MAVEN_HOME="/absolute/path/to/apache-maven"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
export COSMOS_MAVEN_REPO="$HOME/.m2/cosmos-v5"
```

For an unpacked JDK not registered with `java_home`, set `JAVA_HOME` to its
`Contents/Home` directory instead.

**Linux (Bash):**

```bash
export JAVA_HOME="/absolute/path/to/jdk-17"
export MAVEN_HOME="/absolute/path/to/apache-maven"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
export COSMOS_MAVEN_REPO="$HOME/.m2/cosmos-v5"
```

**Windows (PowerShell):**

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17"
$env:MAVEN_HOME = "C:\path\to\apache-maven"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"
$COSMOS_MAVEN_REPO = Join-Path $HOME ".m2/cosmos-v5"
```

Use the Windows [long-path setup](../../../docs/contributor/getting-started.md#3-git--long-path-support-windows)
if checkout reports paths that are too long. On each OS, run `java -version` and
`mvn -version`; Maven must report the selected JDK 17 or newer. See the repository's
[build guide](../../../docs/contributor/building.md) for shared tooling and
dependency-feed authentication guidance.

### Direct Maven build

The following commands work in both the Unix shells and PowerShell configured
above. Keep the quotes around Maven properties, especially on Windows.

First bootstrap the unchanged Core dependencies into the separate local Maven
repository. This selects Core and its parents/dependencies, **not Cosmos**, and
uses the repository's quick-build options for those dependencies:

```text
mvn -B -ntp "-Dmaven.repo.local=$COSMOS_MAVEN_REPO" "-Dgpg.skip" -f sdk/cosmos-v5/reactor.xml -pl com.azure.v2:azure-core -am clean install "-Dmaven.test.skip=true" "-Dcheckstyle.skip" "-Dmaven.javadoc.skip" "-Drevapi.skip" "-Dspotbugs.skip" "-Djacoco.skip" "-Dspotless.skip"
```

After that command succeeds, build the v5 module with its tests and quality checks:

```text
mvn -B -ntp "-Dmaven.repo.local=$COSMOS_MAVEN_REPO" "-Dgpg.skip" -f sdk/cosmos-v5/reactor.xml -pl com.azure:azure-cosmos clean verify "-Dspotless.apply.skip=true"
```

The second command runs the four JUnit 5 metadata tests, Checkstyle, SpotBugs,
formatting verification, and artifact generation without disabling those checks
for Cosmos. Outputs are under `sdk/cosmos-v5/azure-cosmos/target`. Reuse this
Maven repository for iterative builds; rerun the bootstrap after changing the
same-checkout Core dependencies. Neither command deploys packages.

### Additional cold-build validation

`verify-build.py` is a **validation harness, not a build system or code generator**.
It invokes the same Maven commands above, then checks invariants that a successful
`mvn verify` alone does not establish: local/shared version-map consistency,
v4/v5 discovery isolation, effective compiler-execution configuration, Java 17
class-file versions, expected artifact contents, and the deployment guard.
It also rejects implementation classes and native payloads for this initial
migration slice. Those last assertions must evolve deliberately in later PRs.
Keep this validation when changing build, version, or packaging configuration.

For this additional validation, install
[Python 3.9 or newer](https://www.python.org/downloads/). No `pip` packages are
needed. Verify `python3 --version` on macOS/Linux, or `py -3 --version` on Windows
with the Python launcher installed. Use a new empty/absent absolute cache path,
**not** the populated `$COSMOS_MAVEN_REPO` from the developer build:

```bash
python3 sdk/cosmos-v5/azure-cosmos/verify-build.py --maven-repository /absolute/path/to/empty/maven-repository
```

On Windows, invoke the same script from PowerShell:

```powershell
py -3 sdk/cosmos-v5/azure-cosmos/verify-build.py --maven-repository C:\path\to\empty\maven-repository
```

The repository path must be empty or absent. The validation builds Core and
Client Core from the same checkout into this isolated local Maven repository,
then verifies the Cosmos foundation and its SDK, sources, Javadoc, and POM
artifacts. No native toolchain, Cosmos account, service credentials, or
preinstalled private Core JAR is needed. Artifact-feed authentication may still
be required by your Maven configuration.

The Core versions used by this branch are not yet available from Maven Central.
A successful cold reactor build is not a claim that a standalone published
consumer can resolve the future SDK today.

### Opt-in build and version configuration

This is **Cosmos-specific coexistence scaffolding, not a Core v2 convention**.
Core v2 uses ordinary `pom.xml` files. Here, `sdk-build.xml` and `reactor.xml` are
also ordinary Maven POMs; Maven's standard `-f` option selects them. Python does
not translate them or supply Maven's version values. Maven reads the literal
versions in the descriptors; the local text maps are consistency-check inputs.

The nonstandard filenames keep the approved same-coordinate v5 source from
colliding with v4 in shared repository discovery. Renaming to `pom.xml` alone
would expose both `com.azure:azure-cosmos` modules to that tooling and allow the
shared v4 version map to rewrite the v5 descriptor. Returning to conventional
filenames therefore requires a coordinated discovery/versioning change or an
SDK identity decision; it is not required by the choice of Core dependency.

`sdk/cosmos-v5/reactor.xml` is a dedicated reactor for this foundation and its
same-checkout Core dependencies. It is not included in the repository's root
reactor. The SDK's Maven build descriptor is `sdk-build.xml`, not `pom.xml`;
invoke it explicitly with `mvn -f sdk/cosmos-v5/azure-cosmos/sdk-build.xml`
after bootstrapping the dependencies. The validation script performs both steps.

These alternate filenames intentionally exclude v5 from the shared `pom*.xml`
discovery used by aggregate builds, version updates, and version scanning.
`sdk/cosmos-v5/versions.txt` supplies the isolated Cosmos version; it is **not**
an additional registration in `eng/versioning/version_client.txt`.
`sdk/cosmos-v5/external_dependencies.txt` pins build plugins used only by the
alternate descriptor. Such plugins must not be added to the shared external
dependency manifest: its unused-entry checker scans only `pom*.xml` references.
The validation script loads the local plugin pins into the shared external
dependency map using the existing loader, rejecting duplicates within the local
file or across both maps. It then uses the existing version-update implementation
on a temporary descriptor copy to check local and shared versions, failing on
drift without rewriting source files.
Configuration regression tests also exercise actual shared aggregate discovery
and the shared unused-entry checker, reject local plugin version drift and
duplicate entries, and check that the default and opt-in reactors each select
only their own Cosmos version. Run them independently with:

```bash
python3 -B -m unittest discover -s sdk/cosmos-v5/azure-cosmos -p test_build_configuration.py
```

On Windows, replace `python3` with `py -3`.

When changing the v5 version, update both the local manifest and `sdk-build.xml`.
Do not run the shared version updater's `--target-file` option on this descriptor:
without the local override it would apply the v4 version.
When changing a v5-only plugin version, update its local external dependency
entry and the corresponding version marker in `sdk-build.xml` together.

The v5 CI `Artifacts` and `AdditionalModules` lists are empty intentionally, so
shared package/release discovery still registers only v4. CI uploads validation
outputs, not releasable SDK packages. Do not rename these descriptors to
`pom*.xml`, register this SDK in shared release metadata, or add this reactor
to the root build while the same-GA v4 source remains selected there.

## Validation and release boundaries

The separate `sdk/cosmos-v5/ci.yml` pipeline defines build validation on JDK 17,
21, and 25. It does not import release stages, deploy Maven packages, sign
packages, or publish to a dev feed. The module also disables Maven deployment.
Pipeline registration and required-check configuration must be arranged with
the repository maintainers; adding YAML does not create branch protection.

The durable entrypoints are `verify-build.py` and the `cosmos-v5-validation.yml`
pipeline template. Their current-slice assertions still forbid implementation
classes, native payloads, and module exports/opens. Future native/API changes
must deliberately evolve those assertions; this naming cleanup does not relax
them or the Java baseline, artifact, isolation, or deployment checks.

Checkstyle uses the existing `track2` ruleset because the v2 parent's `vnext`
ruleset hardcodes `com.azure.v2` and has no configurable namespace property.
This is not equivalent to the vnext ruleset: track2 permits Reactor types in
public APIs, expects the older Core logger and throw conventions, and includes
`StepVerifierCheck`; vnext permits Client Core types and instead checks
formatted exception messages, raw throws, and created-but-unthrown exceptions.
No rules are modified or suppressed. With no public API or implementation in
this slice, track2 validates the retained namespace and ordinary code style.
Before functional APIs or logging are introduced, maintainers must establish
namespace-compatible Core v2 linting. SpotBugs includes the build metadata tests
because the production artifact contains only module/package metadata.

The default Cosmos reactor and both shared version-manifest columns remain on
v4. The v5 foundation is not a replacement dependency for any existing consumer.
The dedicated validation reactor never includes the v4 SDK or its consumers.

## Key concepts

This slice establishes build and module boundaries only. No public packages are
exported. Native acquisition, ABI validation, loader behavior, and emulator
testing belong to the next migration slice, followed by service functionality.

## Examples

The pure-Java build metadata tests exercise dependency loading and build metadata,
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
