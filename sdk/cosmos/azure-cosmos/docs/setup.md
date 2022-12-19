This document currently details Maven commands for building Azure Cosmos SDK.

### Fork and clone the repository
To build and develop locally, it is strongly recommended to fork and clone the repository: https://github.com/Azure/azure-sdk-for-java
NOTE: All of the below commands need to be run from home directory of azure-sdk-for-java repository.

### JDK 8 vs JDK 11
The build system is configured to support JDK, as well as the current long-term support version of the JDK (currently JDK 11). The commands presented below will work on both JDKs.

### Installing the build tools
Building azure-cosmos SDK locally depends on the availability of the build tooling. This can be installed by running the following:

```shell
mvn install -f eng/code-quality-reports/pom.xml 
```

### Building and Testing

To build azure-cosmos library using maven command line, run the following command

```shell
mvn -e -Dgpg.skip -DskipTests -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl com.azure:azure-cosmos -am clean verify
```

Tips: if you're using powershell on windows, you may run into this error`[ERROR] Unknown lifecycle phase ".skip". You must specify a valid lifecycle phase or a goal in the format`, this can be fixed by telling powershell to stop parsing this command with [stop-parsing parameter "--%"](https://technet.microsoft.com/en-us/library/hh847892.aspx) 

```shell
mvn --% -e -Dgpg.skip -DskipTests -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl com.azure:azure-cosmos -am clean verify
```

To run unit tests, simply remove `-DskipTests` option from above commands.

### Testing for SpotBugs, CheckStyle, and JavaDoc issues
SpotBugs, CheckStyle, and JavaDoc plugins are configured to break the build if there are any issues discovered by them. It is therefore strongly recommended to run the following maven options locally before submitting a pull request:

```shell
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true
```

### Testing for breaking API changes
The build is configured with [Revapi](https://revapi.org) plugin to check builds against the latest GA release in Maven. Build will fail when a breaking change is detected. To check for breaking changes, run the following maven options locally before submitting a pull request:

```shell
-Drevapi.skip=true
```

### Skipping analysis for local build

The default mvn build/install command executes the code quality tools that run analysis for check-style violations, bugs and breaking changes. The build takes more time to complete with these analyses enabled. For intermediate build/install during local development, they can be skipped by adding the following mvn options:

```
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true -Dcodesnippet.skip=true
```

Note: It is strongly recommended to run the analysis locally before submitting a pull request.

### Setting IntelliJ IDE for Java SDK

Open azure-sdk-for-java/sdk/cosmos/azure-cosmos/pom.xml in IntelliJ IDE as project to import it as a maven project:
image.png 

### Defining Project Structure

Open project structure through project settings for azure-cosmos project and set the SDK and Language Level to JDK 11 under Project tab:
image.png

Open Modules tab in the same settings and set the Language level to match project default, which should be JDK 11:
image.png

Set target bytecode version for the project azure-cosmos in IntelliJ Preferences for Java Compiler as JDK 11: 
image.png

### Running Unit Tests

Unit tests are tests with group "unit" and can be run from IntelliJ directly without needing any Azure Cosmos DB Account or Emulator support. To run them, right click on any unit test class and run them. To test this, run `ClientConfigDiagnosticsTest` from IntelliJ IDE directly. 

### Running Integration Tests

Azure Cosmos Java SDK has different Integration tests which can be run with Azure Cosmos Emulator or Azure Cosmos DB production Account. 

Emulator Integration tests are with test group `emulator`, labeled in the code as `groups = { "emulator" }` and can be run from IntelliJ after starting Azure Cosmos DB Emulator on the local development machine. For example, `DocumentCrudTest` is of group emulator.

Other test groups are meant to be tested against Azure Cosmos DB production account, but can also be tested against Emulator. There are multiple different test groups like `groups = {"simple", "long", "direct", "multi-region", "multi-master"}`. For example, `CosmosItemTest` is a simple group test which can be run against Azure Cosmos DB production account, as well as against emulator.

To run any test against Azure Cosmos DB production account, it is required to update `TestConfigurations.java` class with account host and key. 
NOTE: When creating a PR, make sure to remove any account key and account host information from the PR. To avoid security breaches, never commit and push any keys and host information related to Azure Cosmos DB Account.