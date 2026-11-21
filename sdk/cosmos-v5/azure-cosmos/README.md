# Azure Cosmos DB SDK for Java - v5

> **Not a functional SDK. Do not publish or use this build in an application.**
> This is the first migration slice on `feature/cosmos/vnext`, not a release.
> It contains no service clients, operations, native loader, or native binaries.

The v5 foundation uses `com.azure.v2:azure-cosmos` and the `com.azure.v2.cosmos`
Java namespace and module name, aligned with the new Azure Core family.
The `v2` prefix identifies that SDK family, not the Cosmos release version:
this module remains version `5.0.0-beta.1` and requires Java 17 or newer.
The existing `sdk/cosmos` v4 implementation, reactor, CI, version registration,
connectors, and tests are unchanged. The two versions occupy separate source
directories and have distinct Maven and Java identities:

| Identity | v4 | v5 |
| --- | --- | --- |
| Maven coordinates | `com.azure:azure-cosmos:4.x` | `com.azure.v2:azure-cosmos:5.x` |
| Java package/module | `com.azure.cosmos` | `com.azure.v2.cosmos` |

This enables side-by-side migration without dependency mediation selecting one
Cosmos version over the other. Foundation tests load the released v4 SDK alongside
the v5 metadata on the classpath and in a JPMS module layer. The JUnit harness
runs on the classpath so its module-layer test can resolve both SDKs without
inheriting another v5 module in the boot layer; inherited JaCoCo hooks remain.
This is not yet proof of service-operation or native-runtime interoperability;
those capabilities do not exist in this slice.

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
mvn -B -ntp "-Dmaven.repo.local=$COSMOS_MAVEN_REPO" "-Dgpg.skip" -f sdk/cosmos-v5/pom.xml -pl com.azure.v2:azure-core -am clean install "-Dmaven.test.skip=true" "-Dcheckstyle.skip" "-Dmaven.javadoc.skip" "-Drevapi.skip" "-Dspotbugs.skip" "-Djacoco.skip" "-Dspotless.skip"
```

After that command succeeds, build the v5 module with its tests and quality checks:

```text
mvn -B -ntp "-Dmaven.repo.local=$COSMOS_MAVEN_REPO" "-Dgpg.skip" -f sdk/cosmos-v5/pom.xml -pl com.azure.v2:azure-cosmos clean verify "-Dspotless.apply.skip=true"
```

The second command runs six JUnit 5 metadata/coexistence tests, Checkstyle, SpotBugs,
formatting verification, and artifact generation without disabling those checks
for Cosmos. Outputs are under `sdk/cosmos-v5/azure-cosmos/target`. Reuse this
Maven repository for iterative builds; rerun the bootstrap after changing the
same-checkout Core dependencies. Neither command deploys packages.

### Additional cold-build validation

`verify-build.py` is a **validation harness, not a build system or code generator**.
It invokes the same Maven commands above, then checks invariants that a successful
`mvn verify` alone does not establish: shared version-map consistency,
unambiguous v4/v5 discovery, effective compiler-execution configuration, Java 17
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

### Standard Maven and version configuration

`sdk/cosmos-v5/pom.xml` is a dedicated reactor for this foundation and its
same-checkout Core dependencies. It is not included in the root reactor, whose
Cosmos selection remains v4. The SDK uses a normal
`sdk/cosmos-v5/azure-cosmos/pom.xml`, which can also be built directly with `mvn -f`
after bootstrapping Core. An IDE can import these POMs as ordinary Maven projects.

Distinct Maven groups let shared tooling discover and version both SDKs without
overwriting one with the other. `eng/versioning/version_client.txt` now has a
separate `com.azure.v2:azure-cosmos` entry, while the `com.azure:azure-cosmos` entry
is unchanged. Build plugin versions use `eng/versioning/external_dependencies.txt`.
There are no Cosmos-local version maps or alternate Maven descriptors.

The validator reuses the shared updater on a temporary copy, detecting marker
drift without rewriting source files. Configuration tests exercise actual shared
aggregate discovery of both coordinates, the shared unused-entry checker, and
the separate root/v5 reactor selections. Run them independently with:

```bash
python3 -B -m unittest discover -s sdk/cosmos-v5/azure-cosmos -p test_build_configuration.py
```

On Windows, replace `python3` with `py -3`.

Use the repository's [versioning tools](../../../docs/contributor/versioning.md)
with the full `com.azure.v2:azure-cosmos` identity when changing v5 versions.
Do not change the v4 entry as part of a v5 update.

The v5 CI `Artifacts` and `AdditionalModules` lists are empty intentionally, so
shared release-package discovery still registers only v4. Maven/version discovery
is not release enablement: CI uploads nonproduction validation outputs, not
releasable SDK packages. Keep the empty release registration, literal Maven
deployment prohibition, and dedicated validation-only pipeline until publishing
is separately approved.

SDK metadata lives at `com.azure.v2.cosmos.properties` so classpath
resource lookup cannot shadow v4's root-level `azure-cosmos.properties`.

## Validation and release boundaries

The separate `sdk/cosmos-v5/ci.yml` pipeline defines build validation on JDK 17,
21, and 25. It does not import release stages, deploy Maven packages, sign
packages, or publish to a dev feed. The module also disables Maven deployment.
Pipeline registration and required-check configuration must be arranged with
the repository maintainers; adding YAML does not create branch protection.

The durable entrypoints are `verify-build.py` and the `cosmos-v5-validation.yml`
pipeline template. Their current-slice assertions still forbid implementation
classes, native payloads, and module exports/opens. Future native/API changes
must deliberately evolve those assertions; the naming change does not relax
them or the Java baseline, artifact, isolation, or deployment checks.

Checkstyle now inherits the Core v2 parent's unmodified `vnext` ruleset:
the `com.azure.v2.cosmos` namespace no longer needs a track2 override.
No rules are modified or suppressed. SpotBugs includes the build metadata and
coexistence tests because production still contains only module/package metadata.

The default Cosmos reactor and both shared version-manifest columns remain on
v4. The v5 foundation is not a replacement dependency for any existing consumer.
The dedicated validation reactor does not build v4 SDK sources or its consumers.
It resolves the released v4 SDK as a test-only dependency for coexistence checks;
v4 is not a runtime dependency of the v5 artifact.

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
