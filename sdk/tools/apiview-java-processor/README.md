<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->

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
No processor publication is required.
Verification checks source license headers and formatting with Spotless.

## Generate an API review bundle

Run the standalone script with PowerShell 7+, Maven, and a JDK. It is not attached to the Maven lifecycle or SDK generation automation.

From a library module directory, using PowerShell 7+, Maven, and a JDK:

```powershell
pwsh ../../../eng/scripts/Generate-ApiMarkdown.ps1
```

Or specify the package from the repository root:

```powershell
pwsh eng/scripts/Generate-ApiMarkdown.ps1 -PackagePath sdk/serialization/azure-json -OutputDirectory artifacts/azure-json
```

This builds the package and the in-repo processor, then writes `api.md` and `api.metadata.yml` to the output directory.
Generation skips tests, documentation, formatting, snippet updates, and unrelated checks; use `verify` above to validate the processor.
Without `-OutputDirectory`, output defaults to the package's `target/api-review` directory. Generated bundles are build artifacts, not checked-in SDK files.
Metadata contains the SHA-256 hash of the normalized UTF-8/LF `api.md`, the package POM version, and the repository-relative package path.
To reuse a built sources JAR, add `-SourcesJar <path-to-sources.jar>`.
Pipeline generation, artifact publishing, and ARH integration are deferred.

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
