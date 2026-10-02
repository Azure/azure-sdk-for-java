# Azure SDK Maven Archetype

The Azure SDK Maven archetype can accelerate the bootstrapping of a new project. The Azure SDK for Java Maven archetype 
creates a new application, with files and a directory structure that follows best practices. In particular, the 
Azure SDK for Java Maven archetype creates a new Maven project with the following features:

* A dependency on the latest `azure-sdk-bom` BOM release, to ensure that all Azure SDK for Java dependencies are aligned and give you the best developer experience possible.
* Built-in support for GraalVM native image compilation.
* Support for generating a new project with a specified set of Azure SDK for Java client libraries.
* Integration with the Azure SDK for Java build tooling that will give build-time analysis of your project to ensure as many best practices are followed.

> **Note**
> All other Microsoft maintained Azure maven archetypes can be found at [https://github.com/microsoft/azure-maven-archetypes](https://github.com/microsoft/azure-maven-archetypes).

As the Azure SDK for Java Maven archetype is published to Maven Central, we can bootstrap a new application by using 
the archetype directly.

```shell
mvn archetype:generate                        \
  -DarchetypeGroupId=com.azure.tools          \
  -DarchetypeArtifactId=azure-sdk-archetype
```

After entering this command, a series of prompts will ask for details about your project so that the archetype can 
generate the right output for you.


| Name           | Description  |
|----------------|--------------|
| groupId        | (Required) Specifies the Maven groupId to use in the POM file created for the generated project.  |
| artifactId     | (Required) Specifies the Maven artifactId to use in the POM file created for the generated project.  |
| package        | (Optional) Specifies the package name to put the generated code into. If not specified, it is inferred from the groupId. |
| azureLibraries | (Optional) A comma-separated list of Azure SDK for Java libraries, using their Maven artifact IDs. A list of such artifact IDs can be found [here](https://azure.github.io/azure-sdk/releases/latest/java.html). |
| enableGraalVM  | (Optional) By default GraalVM support will be enabled, but if `enableGraalVM` is set to false, the generated Maven POM file will not include support for compiling your application to a native image using GraalVM. |
| javaVersion    | (Optional) Specifies the minimum version of the JDK to target when building the generated project. By default it is Java 17, with valid ranges from Java 8 up. The value should just be the required Java version, for example, '8', '11', '17', etc. |
| junitVersion   | (Optional) The version of JUnit to include as a dependency. By default JUnit 5 will be used, but valid values are '4', '5', and '6'. JUnit 6 requires JDK 17 or later to build and run tests, regardless of the `javaVersion` target. |

If you would rather provide these values at the time of calling the archetype command above (for example, for 
automation purposes), you can specify them as parameters using the standard Maven syntax of appending `-D` to the 
parameter name, for example, `-DjavaVersion=17`.

To use JUnit 6, run Maven with JDK 17 or later and add `-DjunitVersion=6`. The generated project includes
JUnit Jupiter and a compatible Maven Surefire plugin, so its tests can be run with `mvn test`.

## Testing the archetype

Run `mvn verify` from this directory with JDK 17 or later to generate projects using JUnit 4, 5, and 6 and run
their sample tests. The JUnit 5 project also verifies that JUnit 5 remains the default when `junitVersion` is omitted.
