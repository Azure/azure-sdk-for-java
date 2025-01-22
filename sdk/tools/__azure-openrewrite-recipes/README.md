# Migrate to Azure Core v2 with OpenRewrite

TBD

## Setup

### Prerequisites
The following tools are required to build and execute this project:
- Java (version 8 or higher)
- Maven

### Recipe Configuration

TBD

## Usage
### Maven Plugin Configuration

TBD

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



