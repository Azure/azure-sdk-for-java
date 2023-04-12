This document currently details Maven commands for building Azure Cosmos SDK.

### Fork and clone the repository
To build and develop locally, it is strongly recommended to fork and clone the repository: https://github.com/Azure/azure-sdk-for-java

<u>**NOTE:**</u> If running on Windows please ensure that you have enabled LFS-support in Git - see the installation instructions [here](https://git-lfs.com/).

**<u>NOTE:</u>** All of the below commands need to be run from home directory of azure-sdk-for-java repository.

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

Tips: if you're using powershell on windows, you may run into this error`[ERROR] Unknown lifecycle phase ".skip". You must specify a valid lifecycle phase or a goal in the format`, this can be fixed by telling powershell to stop parsing this command with [stop-parsing parameter "--%"](https://technet.microsoft.com/library/hh847892.aspx) 

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
![Screenshot 2022-12-19 at 1 51 17 PM](https://user-images.githubusercontent.com/14034156/208549832-9edf00d6-613a-4efd-a410-eaeb7abe86cd.png)


### Defining Project Structure

Open project structure through project settings for azure-cosmos project and set the SDK and Language Level to JDK 11 under Project tab:
![Screenshot 2022-12-19 at 1 54 08 PM](https://user-images.githubusercontent.com/14034156/208549843-4824a467-9d21-4ffa-bc56-7f14da9d573c.png)

Open Modules tab in the same settings and set the Language level to match project default, which should be JDK 11:
![Screenshot 2022-12-19 at 1 58 27 PM](https://user-images.githubusercontent.com/14034156/208549863-d541c174-c7a3-48d6-b186-e19b78153cff.png)


Set target bytecode version for the project azure-cosmos in IntelliJ Preferences for Java Compiler as JDK 11: 
![Screenshot 2022-12-19 at 2 23 22 PM](https://user-images.githubusercontent.com/14034156/208549894-39804c35-9f4c-4b74-b076-aeaf24edd847.png)

### Installing the Cosmos DB emulator

Setup Azure Cosmos DB Emulator by following [this instruction](https://docs.microsoft.com/azure/cosmos-db/local-emulator). Then please export the emulator's SSL certificates and install them in the JVM trust stores on your development machine following [this instruction](https://learn.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates).

For running the SDK unit tests use follogwing start options for the emulator:
PS C:\Program Files\Azure Cosmos DB Emulator> .\CosmosDB.Emulator.exe /enablepreview /EnableSqlComputeEndpoint /disableratelimiting /partitioncount=50 /consistency=Strong

For installing the keys on windows following power shell script (running as administrator) can be used:
```
Push-Location -Path $env:JAVA_HOME

gci -recurse cert:\LocalMachine\My | ? FriendlyName -eq DocumentDbEmulatorCertificate | Export-Certificate -Type cer -FilePath cosmos_emulator.cer
keytool -cacerts -delete -alias cosmos_emulator -storepass changeit
keytool -cacerts -importcert -alias cosmos_emulator -storepass changeit -file cosmos_emulator.cer
del cosmos_emulator.cer

Pop-Location
```

### Running Unit Tests

Unit tests are tests with group "unit" and can be run from IntelliJ directly without needing any Azure Cosmos DB Account or Emulator support. To run them, right click on any unit test class and run them. To test this, run `ClientConfigDiagnosticsTest` from IntelliJ IDE directly. 

Note: When running the Azure Cosmos DB Emulator in a virtual machine you may receive timeouts. If that happens increase the number of cores and improve the I/O performance of the VM.

### Running Integration Tests

Azure Cosmos Java SDK has different Integration tests which can be run with Azure Cosmos Emulator or Azure Cosmos DB production Account. 

Emulator Integration tests are with test group `emulator`, labeled in the code as `groups = { "emulator" }` and can be run from IntelliJ after starting Azure Cosmos DB Emulator on the local development machine. For example, `DocumentCrudTest` is of group emulator.

Latest version of Azure Cosmos DB Emulator can be downloaded and installed from [here](https://learn.microsoft.com/azure/cosmos-db/local-emulator)
Our CI pipelines start Azure Cosmos DB Emulator with these parameters. It is highly recommended to use these for local development and testing.
```shell
/enablepreview /EnableSqlComputeEndpoint /disableratelimiting /partitioncount=50 /consistency=Strong
```

Other test groups are meant to be tested against Azure Cosmos DB production account, but can also be tested against Emulator. There are multiple different test groups like `groups = {"simple", "long", "direct", "multi-region", "multi-master"}`. For example, `CosmosItemTest` is a simple group test which can be run against Azure Cosmos DB production account, as well as against emulator.

To run any test against Azure Cosmos DB production account, it is required to update `TestConfigurations.java` class with account host and key. 
NOTE: When creating a PR, make sure to remove any account key and account host information from the PR. To avoid security breaches, never commit and push any keys and host information related to Azure Cosmos DB Account.
