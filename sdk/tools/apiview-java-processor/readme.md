## Overview

This application tokenises a Java project into a format useful for Java API reviews. It takes in one or more Maven-generated `sources.jar` files, and reads all files within it.

Imported from [Azure/azure-sdk-tools#16809](https://github.com/Azure/azure-sdk-tools/pull/16809)
at commit `cb515d9def63095c233f47745f8fbd8068b7df2c`, including the Markdown renderer.

## Building

From the repository root, build the executable JAR and run tests:

```shell
mvn -f sdk/tools/apiview-java-processor/pom.xml clean verify
```

The JAR is written to `sdk/tools/apiview-java-processor/target`.
The dedicated `ci.yml` builds and tests this standalone tool on Linux; publishing is not yet configured.

## How To Use

Compile your source code using Maven `mvn clean package`. This will create a `target` directory containing
the built jar files, one of which will take the form `<library-name>-sources.jar`.

The application is run using the following structure:
</br>
`java -jar apiview-java-processor-<version>.jar [--renderer=json|markdown] <comma-separated list of jar files> <outputDirectory>`

For example:</br>

* **One Jar File:** `java -jar apiview-java-processor-<version>.jar application-sources.jar temp`
* **Multiple Jar Files:** `java -jar apiview-java-processor-<version>.jar application-sources.jar,test-library-sources.jar,other-library-sources.jar temp`
* **Markdown without JavaDoc:** `java -jar apiview-java-processor-<version>.jar --renderer=markdown application-sources.jar temp`

The default renderer is `json`. The `markdown` renderer writes a fenced Java API snapshot directly and omits
JavaDoc so that the result contains only the public API surface.
