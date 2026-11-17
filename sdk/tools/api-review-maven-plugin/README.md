# Java API Review Maven Plugin

Generates `api.md` and `api.metadata.yml` from a built Java sources JAR. The same artifact works as a Maven plugin or a standalone executable JAR.

## Standalone execution and API Review Hub

Use the main plugin JAR, not the sources or Javadoc JAR. It includes its runtime dependencies and needs only Java 8+:

```shell
java -jar api-review-maven-plugin-1.0.0-beta.1.jar --sources-jar azure-json-1.6.0-beta.1-sources.jar --package-relative-path sdk/serialization/azure-json --output-directory api-review
```

All three options are required. The sources JAR must use its `<artifact>-<version>-sources.jar` filename; rename a copy if the build uses a custom final name. Its Maven metadata supplies the package version; no external POM, Maven installation, repository cache, or plugin downloads are needed. Use `--help` for usage.

For API Review Hub, the build-tools wrapper builds the main JAR from the pinned tooling checkout and stages it into `$(BinOutputDirectory)`. The generation wrapper builds the requested package's sources JAR, then runs the staged tool from `$(BinDirectory)` with output directed to `$(ArtifactOutputDirectory)`.

Normal SDK package builds may still restore their own dependencies; standalone review generation does not. The two language-owned ARH wrappers remain required.

To build the tool and generate locally with prebuilt sources, run from the repository root using Maven and JDK 11+:

```powershell
pwsh eng\scripts\Stage-ApiReviewTools.ps1 -SourceDirectory . -BinOutputDirectory artifacts\api-review-tools
pwsh eng\scripts\Generate-ApiReviewArtifact.ps1 -RepositoryRoot . -PackageName azure-json -ToolJar artifacts\api-review-tools\api-review-maven-plugin.jar -SourcesJar sdk\serialization\azure-json\target\azure-json-1.6.0-beta.1-sources.jar -OutputDirectory artifacts\api-review
```

## Maven execution

The Maven goal reads the module's existing `pom.xml`; no plugin declaration or new SDK-repository scripts are needed.

From a library module, run its normal build followed by the plugin:

```shell
mvn package com.azure.tools:api-review-maven-plugin:1.0.0-beta.1:generate
```

The package build must produce a `-sources.jar`. Custom `build.finalName` values are supported using the project's embedded Maven metadata. To reuse a previously built JAR:

```shell
mvn com.azure.tools:api-review-maven-plugin:1.0.0-beta.1:generate -Dapi-review.sourcesJar=/path/to/library-1.0.0-sources.jar
```

Resolve released versions through the Azure SDK Java dev feed using the SDK repository's existing Maven settings.

| Property | Default |
| --- | --- |
| `api-review.sourcesJar` | Attached sources artifact, otherwise `target/${project.build.finalName}-sources.jar` |
| `api-review.outputDirectory` | `${project.build.directory}/api-review` |
| `api-review.packageRelativePath` | Module path relative to the nearest ancestor containing `.git` |
| `api-review.repositoryRoot` | Optional root override for exported sources without Git metadata |
| `api-review.skip` | `false`; aggregator POMs are skipped automatically |

For an explicit module path, including sparse or exported checkouts:

```shell
mvn com.azure.tools:api-review-maven-plugin:1.0.0-beta.1:generate -Dapi-review.packageRelativePath=sdk/serialization/azure-json
```

Both modes record the literal package version and forward-slash repository-relative path. `apiHash` is lowercase SHA-256 of the complete UTF-8, no-BOM, LF-normalized Markdown with one trailing newline. Existing bundles are preserved if generation or replacement fails.

## Develop

Use Maven 3.6.3+ and JDK 11+ to build and verify the Java 8-compatible tool:

```shell
mvn -f sdk/tools/api-review-maven-plugin/pom.xml clean verify
```

The plugin includes the shared processor sources and resources from `sdk/tools/apiview-java-processor`; there is no separately published processor dependency. `verify` checks Maven invocation and standalone execution of a copied JAR without a POM or Maven cache, comparing both bundles byte-for-byte. CI also verifies the standalone processor.

Verification also runs SDK Checkstyle, Spotless formatting, and Codesnippet checks. Checkstyle covers plugin sources and tests; the processor module checks its own sources. Narrow exceptions allow Maven-injected fields and tool-specific exception handling.

## Release

Run `release.yml` manually from `main` in the internal project, opting into `publishDevFeed`. It builds and tests on Linux, then ESRP-signs all three JARs and GPG-signs/publishes the plugin JAR, POM, sources, and Javadoc to the Java dev feed on Windows. Existing versions cannot be overwritten.
The pipeline uses the EngSys-approved Java signing and publishing configuration. Register `eng/pipelines/api-review-java.yml` as `tools - api-review-maven-plugin - ci` and this module's `release.yml` as an internal release pipeline.
