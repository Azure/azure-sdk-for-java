<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->

## Overview

The Java APIView processor generates API reviews from one or more Maven `-sources.jar` files.

Includes the processor fixes from [Azure/azure-sdk-tools#17586](https://github.com/Azure/azure-sdk-tools/pull/17586).

## Building

From the repository root, build the executable JAR and run tests:

```shell
mvn -f sdk/tools/apiview-java-processor/pom.xml clean verify
```

Artifacts are written to `sdk/tools/apiview-java-processor/target`.
No processor publication is required.
Verification runs tests, Spotless formatting checks, and shared SDK Checkstyle rules, including license headers.

## Generate an API review bundle

Use the [API review Maven plugin](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tools/api-review-maven-plugin/README.md)
for `api.md` and `api.metadata.yml`, or its [ARH wrappers](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tools/api-review-maven-plugin/README.md#standalone-execution-and-api-review-hub)
to build and stage the executable tool once for both baseline and target generation.

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
