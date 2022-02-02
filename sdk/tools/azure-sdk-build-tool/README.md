# Azure SDK Maven Build Tool

The Azure SDK for Java project ships a Maven build tool that developers can choose to include in their projects. This tool runs locally and does not transmit any data to Microsoft. It can be configured to generate a report or fail the build when certain conditions are met, which is useful to ensure compliance with numerous best practices. These include:

- Validating the correct use of the azure-sdk-for-java BOM, including using the latest version and relying on it to 
define dependency versions on Azure SDK for Java client libraries.
- Validating that historical Azure client libraries are not being used when newer and improved versions exist.
- Providing insight into usage of beta APIs.

The build tool can be configured in a project Maven POM file as such:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.azure.tools</groupId>
            <artifactId>azure-sdk-build-tool</artifactId>
            <version>{latest_version}</version>
            <configuration>
            ...
            </configuration>
        </plugin>
    </plugins>
</build>
```
Within the configuration section, it is possible to configure the settings in the table below if desired, but by default they are configured with the recommended settings. Because of this, it is ok to not have any configuration specified at all.


| Property                                 | Default Value | Description                                                                                                                                                                                                                                      |
|------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| validateAzureSdkBomUsed                  | true          | Ensures that the build has the azure-sdk-for-java BOM referenced appropriately, so that Azure SDK for Java client library dependencies may take their versions from the BOM.                                                                     |
| validateBomVersionsAreUsed               | true          | Ensures that where a dependency is available from the azure-sdk-for-java BOM the version is not being manually overridden.                                                                                                                       |
| validateNoDeprecatedMicrosoftLibraryUsed | true          | Ensures that the project does not make use of previous-generation Azure libraries. Using the new and previous-generation libraries in a single project is unlikely to cause any issue, but is will result in a sub-optimal developer experience. |
| validateNoBetaLibraryUsed                | false         | Some Azure SDK for Java client libraries have beta releases, with version strings in the form x.y.z-beta.n. Enabling this feature will ensure that no beta libraries are being used.                                                             |
| validateNoBetaAPIUsed                    | true          | Azure SDK for Java client libraries sometimes do GA releases with methods annotated with @Beta. This check looks to see if any such methods are being used.                                                                                      |
| validateLatestBomVersionUsed             | true          | Ensures that dependencies are kept up to date by reporting back (or failing the build) if a newer azure-sdk-for-java BOM exists.                                                                                                                 |
| reportFile                               | ""            | (Optional) Specifies the location to write the build report out to, in JSON format. If not specified, no report will be written (and a summary of the build, or the appropriate build failures), will be shown in the terminal.                  |
After adding the build tool into a Maven project, the tool can be run by calling `mvn compile azure:run`. Depending on 
the configuration provided, you can expect to see build failures or report files generated that can inform you about potential issues before they become more serious.

As the build tool evolves, new releases will be published, and it is recommended that developers frequently check for new releases and update as appropriate.
