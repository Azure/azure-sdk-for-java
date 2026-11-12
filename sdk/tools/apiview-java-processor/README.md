## Overview

The Java APIView processor generates API reviews from one or more Maven `-sources.jar` files.

Imported from [Azure/azure-sdk-tools#16809](https://github.com/Azure/azure-sdk-tools/pull/16809)
at commit `cb515d9def63095c233f47745f8fbd8068b7df2c`, including the Markdown renderer.

## Building

From the repository root, build the executable JAR and run tests:

```shell
mvn -f sdk/tools/apiview-java-processor/pom.xml clean verify
```

Artifacts are written to `sdk/tools/apiview-java-processor/target`.
The shared `sdk/tools/ci.yml` builds and tests the tool using the SDK pipeline. No processor publication is required.

## Generate a package's api.md

SDK modules using `azure-client-sdk-parent` generate `api.md` and `api.metadata.yml` during Maven's `package` phase.
This requires PowerShell 7+, Maven, and a JDK. Parallel reactor builds share one processor build per Maven invocation.
Use `--define apiMarkdown.skip=true` to skip API Markdown and metadata generation for a Maven build.

From a library module directory, using PowerShell 7+, Maven, and a JDK:

```powershell
pwsh ../../../eng/scripts/Generate-ApiMarkdown.ps1
```

Or specify the package from the repository root:

```powershell
pwsh eng/scripts/Generate-ApiMarkdown.ps1 -PackagePath sdk/serialization/azure-json
```

This builds the package and the in-repo processor, then writes `api.md` and `api.metadata.yml` in the package directory.
Metadata contains the SHA-256 hash of the normalized UTF-8/LF `api.md`, the package POM version, and the repository-relative package path.
To reuse a built sources JAR, add `-SourcesJar <path-to-sources.jar>`.
SDK generation automation collects both files from the Maven build; it does not run generation again.

## Usage

```text
java -jar apiview-java-processor-<version>.jar [--renderer=json|markdown] <comma-separated sources.jar files> <output-directory>
```

JSON is the default renderer:

```shell
java -jar apiview-java-processor-<version>.jar application-1.0.0-sources.jar output
```

Use Markdown for a fenced Java API snapshot without Javadoc:

```shell
java -jar apiview-java-processor-<version>.jar --renderer=markdown application-1.0.0-sources.jar output
```

Separate multiple input JAR paths with commas.
