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
The shared `sdk/tools/ci.yml` builds and tests the tool using the SDK pipeline. Publishing remains disabled.

## Usage

```text
java -jar apiview-java-processor-<version>.jar [--renderer=json|markdown] <comma-separated sources.jar files> <output-directory>
```

JSON is the default renderer:

```shell
java -jar apiview-java-processor-<version>.jar application-sources.jar output
```

Use Markdown for a fenced Java API snapshot without Javadoc:

```shell
java -jar apiview-java-processor-<version>.jar --renderer=markdown application-sources.jar output
```

Separate multiple input JAR paths with commas.
