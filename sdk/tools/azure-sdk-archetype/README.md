# Azure SDK Maven Archetype

The Azure SDK archetype can accelerate the bootstrapping of a new project that uses Azure SDK by 
creating an application structure that follows many of the recommended best practices for using the Azure SDK for Java. In particular, the Azure SDK for Java Maven archetype creates a new Maven project with the following features:

* A dependency on the latest `azure-sdk-bom` BOM release, to ensure that all Azure SDK for Java dependencies are aligned and give you the best developer experience possible.
* Built-in support for GraalVM native image compilation.
* Support for generating a new project with a specified set of Azure SDK for Java client libraries.
* Integration with the Azure SDK for Java build tooling that will give build-time analysis of your project to ensure as many best practices are followed.

As the Azure SDK for Java Maven archetype is published to Maven Central, to use it to create a new project you should enter the following into your terminal:

```sh
 mvn archetype:generate                        \
   -DarchetypeGroupId=com.azure.tools          \
   -DarchetypeArtifactId=azure-sdk-archetype \
```

Once you press the enter key, you will be prompted to provide details about your project so that the archetype can generate the right output for you. The questions you'll be asked are outlined in the table below.

| Name           | Description  |
|----------------|--------------|
| groupId        | (Required) Specifies the Maven groupId to use in the POM file created for the generated project.  |
| artifactId     | (Required) Specifies the Maven artifactId to use in the POM file created for the generated project.  |
| package        | (Optional) Specifies the package name to put the generated code into. If not specified, it is inferred from the groupId. |
| azureLibraries | (Optional) This is a comma-separated list of Azure SDK for Java libraries, using their Maven artifact IDs. A list of such artifact IDs can be found [here](https://azure.github.io/azure-sdk/releases/latest/java.html). |
| enableGraalVM  | (Optional) By default this is true, but can be overridden by setting this to false if you do not want to enable support for compiling your application to a native image using GraalVM. |
| javaVersion    | (Optional) Specifies the minimum version of the JDK to target when building the generated project. By default it is the latest LTS release (currently Java 17), with valid ranges from Java 8 up. The value should just be the required Java version, e.g. '8', '11', '17', etc. |
| junitVersion   | (Optional) The version of JUnit to include as a dependency. By default this is JUnit 5, but valid values are '4' and '5'. |

If you would rather provide these values at the time of calling the archetype commmand above (e.g. for automation purposes), you can specify them as parameters using the standard Maven syntax of appending `-D` to the parameter name, e.g. `-DjavaVersion=17`.
