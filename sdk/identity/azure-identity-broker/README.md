# Azure Identity Brokered Authentication client library for Java

The Azure Identity Brokered Authentication library extends the Azure Identity library to provide brokered authentication support. This options class can be used to create an `InteractiveBrowserCredential` capable of using the system authentication broker in lieu of the web browser when available.

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
    <version>1.1.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- An [Azure subscription][azure_sub].
- The Azure CLI can also be useful for authenticating in a development environment, creating accounts, and managing account roles.


## Key concepts

This package enables broker support via `InteractiveBrowserCredential`, from the [Azure Identity library][azure_identity_library]. The credential is created with `InteractiveBrowserBrokerCredentialBuilder` from the `azure-identity-broker` library, as shown below.

### Parent window handles

When authenticating interactively via `InteractiveBrowserCredential`, a parent window handle is required to ensure that the authentication dialog is shown correctly over the requesting window. In the context of graphical user interfaces on devices, a window handle is a unique identifier that the operating system assigns to each window. For the Windows operating system, this handle is an integer value that serves as a reference to a specific window.

## Microsoft account (MSA) passthrough

Microsoft accounts (MSA) are personal accounts created by users to access Microsoft services. MSA passthrough is a legacy configuration which enables users to get tokens to resources which normally don't accept MSA logins. This feature is only available to first-party applications. Users authenticating with an application that is configured to use MSA passthrough can `InteractiveBrowserBrokerCredentialBuilder.enableLegacyMsaPassthrough()` to allow these personal accounts to be listed by WAM.

## Redirect URIs

Microsoft Entra applications rely on redirect URIs to determine where to send the authentication response after a user has logged in. To enable brokered authentication through WAM, a redirect URI matching the following pattern should be registered to the application:

```
ms-appx-web://Microsoft.AAD.BrokerPlugin/{client_id}
```

### Examples

#### Configure the `InteractiveBrowserCredential` to use the system authentication broker for Windows

```java com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.windows
long windowHandle = getWindowHandle(); // Samples below
InteractiveBrowserCredential cred = new InteractiveBrowserBrokerCredentialBuilder()
    .setWindowHandle(windowHandle)
    .build();
```

#### Use the default account for sign-in

When this option is enabled, the credential will attempt to silently use the default broker account. If using the default account fails, the credential will fall back to interactive authentication.

```java com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.defaultaccount
long windowHandle = getWindowHandle(); // Samples below
InteractiveBrowserCredential cred = new InteractiveBrowserBrokerCredentialBuilder()
    .setWindowHandle(windowHandle)
    .useDefaultBrokerAccount()
    .build();
```

#### Obtain a window handle

##### JavaFX

This code will obtain the window handle for a JavaFX application for use with `InteractiveBrowserCredentialBuilder`:

```java com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.getwindowhandle.javafx
public long getWindowHandle(Stage stage) {
    try {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, stage.getTitle());
        return Pointer.nativeValue(hwnd.getPointer());
    } catch (Exception e) {
        // Handle exceptions in an appropriate manner for your application.
        // Not being able to retrieve a window handle for Windows is a fatal error.
        throw e;
    }
}
```

## Troubleshooting

Credentials raise exceptions when they fail to authenticate or can't execute authentication. When credentials fail to authenticate a `ClientAuthenticationException` is raised. The exception has a `message` attribute, which describes why authentication failed. When this exception is raised by `ChainedTokenCredential`, the chained execution of underlying list of credentials is stopped.

When credentials can't execute authentication due to one of the underlying resources required by the credential being unavailable on the machine a `CredentialUnavailableException` is raised. The exception has a `message` attribute that describes why the credential is unavailable for authentication execution. When this exception is raised by `ChainedTokenCredential`, the message collects error messages from each credential in the chain.

See the [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

## Next steps

[These Java client libraries](https://learn.microsoft.com/azure/developer/java/sdk/azure-sdk-library-package-index#libraries-using-azure-core) support authenticating with `TokenCredential` and the Azure Identity library. To learn more about using these client libraries, see the aforementioned link.

The [microsoft-graph-sdk](https://github.com/microsoftgraph/msgraph-sdk-java) also supports authenticating with `TokenCredential` and the Azure Identity library.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.


<!-- LINKS -->
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[azure_identity_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity
[azure_sub]: https://azure.microsoft.com/free/java
[entra_id_doc]: https://learn.microsoft.com/entra/identity/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[javadoc]: https://learn.microsoft.com/java/api/com.azure.identity.broker?view=azure-java-stable
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity-broker
