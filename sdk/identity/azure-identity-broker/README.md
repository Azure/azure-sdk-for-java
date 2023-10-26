# Azure Identity Brokered Authentication client library for Java

The Azure Identity Brokered Authentication library extends the Azure Identity library to provide brokdered authentication support. This options class can be used to create an `InteractiveBrowserCredential` capable of using the system authentication broker in lieu of the web browser when available.

[Source code][source] | [API reference documentation][javadoc] | [Microsoft Entra ID documentation][entra_id_doc]

## Getting started

### Include the package

#### Include the BOM file

Include the `azure-sdk-bom` in your project to take a dependency on the stable version of the library. In the following snippet, replace the `{bom_version_to_target}` placeholder with the version number. To learn more about the BOM, see the [Azure SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then include the direct dependency in the `dependencies` section without the version tag:

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity-broker</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

To take dependency on a particular version of the library that isn't present in the BOM, add the direct dependency to your project as follows:

[//]: # ({x-version-update-start;com.azure:azure-identity-broker;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity-broker</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- An [Azure subscription][azure_sub].
- The Azure CLI can also be useful for authenticating in a development environment, creating accounts, and managing account roles.


## Key concepts

### Examples

#### Configuring the `InteractiveBrowserCredential` to use the system authentication broker

```java
long windowHandle = GetWindowHandle() // Samples below
InteractiveBrowserCredential cred = new InteractiveBrowserCredentialBuilder()
    .useInteractiveBrowserBroker(windowHandle)
    .build();
```

#### Obtaining a window handle

##### JavaFX

This code will obtain the window handle for a JavaFX application for use with `InteractiveBrowserCredentialBuilder`

```java

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public static long getWindowHandle(Stage stage) {
    try {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, stage.getTitle());
        return Pointer.nativeValue(hwnd.getPointer());
    } catch (Exception e) {
        e.printStackTrace();
        return 0;
    }
}
```

## Troubleshooting

Credentials raise exceptions when they fail to authenticate or can't execute authentication. When credentials fail to authenticate, the`ClientAuthenticationException` is raised. The exception has a `message` attribute, which describes why authentication failed. When this exception is raised by `ChainedTokenCredential`, the chained execution of underlying list of credentials is stopped.

When credentials can't execute authentication due to one of the underlying resources required by the credential being unavailable on the machine, the`CredentialUnavailableException` is raised. The exception has a `message` attribute that describes why the credential is unavailable for authentication execution. When this exception is raised by `ChainedTokenCredential`, the message collects error messages from each credential in the chain.

See the [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

## Next steps

The Java client libraries listed [here](https://azure.github.io/azure-sdk/releases/latest/java.html) support authenticating with `TokenCredential` and the Azure Identity library. You can learn more about their use, and find additional documentation on use of these client libraries along samples with can be found in the links mentioned [here](https://azure.github.io/azure-sdk/releases/latest/java.html).

The [microsoft-graph-sdk](https://github.com/microsoftgraph/msgraph-sdk-java) also supports authenticating with `TokenCredential` and the Azure Identity library.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.


```
<!-- LINKS -->
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[azure_sub]: https://azure.microsoft.com/free/
[entra_id_doc]: https://learn.microsoft.com/azure/active-directory/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[javadoc]: https://learn.microsoft.com/java/api/com.azure.identity?view=azure-java-stable
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity-broker
