# Azure Identity Brokered Authentication library for Java

The Azure Identity Brokered Authentication library extends the Azure Identity library to provide brokdered authentication support. This options class can be used to create an `InteractiveBrowserCredential` capable of using the system authentication broker in lieu of the web browser when available.

[Source code][source] | [API reference documentation][javadoc] | [Microsoft Entra ID documentation][entraid_doc]

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
<!-- LINKS -->
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[azure_sub]: https://azure.microsoft.com/free/
[entraid_doc]: https://learn.microsoft.com/azure/active-directory/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[javadoc]: https://learn.microsoft.com/java/api/com.azure.identity?view=azure-java-stable
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity-broker
