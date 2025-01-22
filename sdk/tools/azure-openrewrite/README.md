# Azure Code Migration with OpenRewrite

This repository showcases the integration of OpenRewrite with Maven for code migration purposes.
The migration recipe is defined to transition from `azure-core` to `azure-core-v2` libraries.

## Setup

### Prerequisites
The following tools are required to build and execute this project:
- Java (version 8 or higher)
- Maven

### Recipe Configuration

The migration recipe is defined in the `rewrite-java-core` module as detailed below:

```yaml
### Recipe Configuration for OpenRewrite
type: specs.openrewrite.org/v1beta/recipe
name: com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2
displayName: Migrate azure-core samples to azure-core-v2
description: This recipe migrates the samples in azure-core to azure-core-v2
recipeList:
  ...
```
You can find the full recipe configuration in the `rewrite.yml` file [here](https://github.com/Azure/azsdk-java-rewrite-recipes/blob/main/rewrite-java-core/src/main/resources/META-INF/rewrite/rewrite.yml).


## Usage
### Maven Plugin Configuration
The OpenRewrite Maven plugin is configured in the `rewrite-java-core` module to run the migration recipe on the sample project
as follows:
```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.7.1</version>
    <configuration>
        <activeRecipes>
            <recipe>com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>rewrite-java-core</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```
The plugin configuration is defined in the `pom.xml` file [here](https://github.com/Azure/azsdk-java-rewrite-recipes/blob/main/rewrite-sample/pom.xml).

## Execution
The `rewrite-sample` module is configured to use the `openrewrite-maven-plugin` to run the OpenRewrite recipe on the sample project.
The `rewrite-sample` module contains the modules `azure-ai-translation-text-v1` and `azure-ai-translation-text-v2`
to demonstrate the migration of code from `azure-core` to `azure-core-v2`.

**Note:** To execute the below commands, ensure that you are within the `rewrite-sample` directory.

### Dry Run
To run the OpenRewrite recipe in dry-run mode, execute the following command:
```shell
mvn rewrite:dryRun
```
If the above command is not recognised, execute the full version of the command:

```shell
mvn org.openrewrite.maven:rewrite-maven-plugin:dryRun
```

This will generate a file `rewrite.patch` in `rewrite-sample/target/rewrite` directory.

### Run (apply changes)
To actually apply the changes to the sample project, execute the following command:
```shell
mvn rewrite:run
```
If the above command is not recognised, execute the full version of the command:

```shell
mvn org.openrewrite.maven:rewrite-maven-plugin:run
```

## Testing
To run the unit tests for the OpenRewrite recipe, execute the following command:
```shell
mvn:test
```


## Openrewrite Reference
- [Rewrite Recipe Starter](https://github.com/moderneinc/rewrite-recipe-starter):  Template for building your own recipe JARs
- [Best practices for writing recipes](https://docs.openrewrite.org/recipes/recipes/openrewritebestpractices)
- [Recipe Testing](https://docs.openrewrite.org/authoring-recipes/recipe-testing): Information on testing the recipe with unit tests.
- [Collaboration Proposal](https://github.com/openrewrite/collaboration-proposals/issues/new/choose): collaboration with OpenRewrite






