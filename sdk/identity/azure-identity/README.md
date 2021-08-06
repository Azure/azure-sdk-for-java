# Azure Identity client library for Java
The Azure Identity library provides [Azure Active Directory (AAD)](https://docs.microsoft.com/azure/active-directory/fundamentals/active-directory-whatis) token authentication through a set of convenient [TokenCredential](https://docs.microsoft.com/en-us/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable) implementations. It enables Azure SDK clients to authenticate with AAD, while also allowing other Java apps to authenticate with AAD work and school accounts, Microsoft personal accounts (MSA), and other Identity providers through the [AAD B2C](https://docs.microsoft.com/azure/active-directory-b2c/overview) service.
  [Source code][source] | [API reference documentation][javadoc] | [Azure Active Directory documentation][aad_doc]

## Getting started
### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/boms/azure-sdk-bom/README.md).

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
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.


Maven dependency for Azure Secret Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-identity;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.3.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
* A [Java Development Kit (JDK)][jdk_link], version 8 or later.
* An [Azure subscription][azure_sub].
* The Azure CLI can also be useful for authenticating in a development environment, creating accounts, and managing account roles.

### Authenticate the client

When debugging and executing code locally it is typical for a developer to use their own account for authenticating calls to Azure services. There are several developer tools which can be used to perform this authentication in your development environment:

- [Azure Toolkit for IntelliJ](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-toolkit-for-intellij-for-intellijcredential)
- [Visual Studio Code Azure Account Extension](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-visual-studio-code-azure-account-extension-for-visualstudiocodecredential)
- [Azure CLI](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-cli-for-azureclicredential)
- [Visual Studio 2019 (Shared token cache)](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-applications-for-shared-token-cache-credential)

Click on each item above to learn about how to configure them for Azure Identity authentication.

## Key concepts
### Credentials

A credential is a class which contains or can obtain the data needed for a service client to authenticate requests. Service clients across Azure SDK accept credentials when they are constructed, and service clients use those credentials to authenticate requests to the service. 

The Azure Identity library focuses on OAuth authentication with Azure Active directory, and it offers a variety of credential classes capable of acquiring an AAD token to authenticate service requests. All of the credential classes in this library are implementations of the `TokenCredential` abstract class in [azure-core][azure_core_library], and any of them can be used by to construct service clients capable of authenticating with a `TokenCredential`. 

See [Credential Classes](#credential-classes) for a complete list of available credential classes.

### DefaultAzureCredential
The `DefaultAzureCredential` is appropriate for most scenarios where the application is intended to ultimately be run in the Azure Cloud. This is because the `DefaultAzureCredential` combines credentials commonly used to authenticate when deployed, with credentials used to authenticate in a development environment.

> Note: `DefaultAzureCredential` is intended to simplify getting started with the SDK by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't served by the default settings should use other credential types.

 The `DefaultAzureCredential` will attempt to authenticate via the following mechanisms in order.
 
![DefaultAzureCredential authentication flow](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/identity/azure-identity/images/defaultazurecredential.png)

 - Environment - The `DefaultAzureCredential` will read account information specified via [environment variables](#environment-variables) and use it to authenticate.
 - Managed Identity - If the application is deployed to an Azure host with Managed Identity enabled, the `DefaultAzureCredential` will authenticate with that account.
 - IntelliJ - If the developer has authenticated via Azure Toolkit for IntelliJ, the `DefaultAzureCredential` will authenticate with that account.
 - Visual Studio Code - If the developer has authenticated via the Visual Studio Code Azure Account plugin, the `DefaultAzureCredential` will authenticate with that account.
 - Azure CLI - If the developer has authenticated an account via the Azure CLI `az login` command, the `DefaultAzureCredential` will authenticate with that account.

## Examples
You can find more examples of using various credentials in [Azure Identity Examples Wiki page](https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples). 

### Authenticating with `DefaultAzureCredential`
This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`. There's also [a compilable sample](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java) to create a Key Vault secret client you can copy-paste.

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L40-L52 -->
```java
/**
 * The default credential first checks environment variables for configuration.
 * If environment configuration is incomplete, it will try managed identity.
 */
public void createDefaultAzureCredential() {
    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultCredential)
        .buildClient();
}
```

See more how to configure the `DefaultAzureCredential` on your workstation or Azure in [Configure DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#configure-defaultazurecredential).

### Authenticating a user assigned managed identity with `DefaultAzureCredential`
This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`, deployed to an Azure resource with a user assigned managed identity configured.

See more about how to configure a user assigned managed identity for an Azure resource in [Enable managed identity for Azure resources](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-managed-identity-for-azure-resources).

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L242-L255 -->
```java
/**
 * The default credential will use the user assigned managed identity with the specified client ID.
 */
public void createDefaultAzureCredentialForUserAssignedManagedIdentity() {
    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
        .managedIdentityClientId("<MANAGED_IDENTITY_CLIENT_ID>")
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultCredential)
        .buildClient();
}
```

In addition to configuring the `managedIdentityClientId` via code, it can also be set using the `AZURE_CLIENT_ID` environment variable. These two approaches are equivalent when using the `DefaultAzureCredential`.

### Authenticating a user in Azure Toolkit for IntelliJ with `DefaultAzureCredential`
This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`, on a workstation with IntelliJ IDEA installed, and the user has signed in with an Azure account to the Azure Toolkit for IntelliJ.

See more about how to configure your IntelliJ IDEA in [Sign in Azure Toolkit for IntelliJ for IntelliJCredential](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-toolkit-for-intellij-for-intellijcredential).

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L257-L271 -->
```java
/**
 * The default credential will use the KeePass database path to find the user account in IntelliJ on Windows.
 */
public void createDefaultAzureCredentialForIntelliJ() {
    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
        // KeePass configuration required only for Windows. No configuration needed for Linux / Mac
        .intelliJKeePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultCredential)
        .buildClient();
}
```

## Managed Identity Support
The [Managed identity authentication](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview) is supported via either the `DefaultAzureCredential` or the `ManagedIdentityCredential` directly for the following Azure Services:
* [Azure Virtual Machines](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token)
* [Azure App Service](https://docs.microsoft.com/azure/app-service/overview-managed-identity?tabs=dotnet)
* [Azure Kubernetes Service](https://docs.microsoft.com/azure/aks/use-managed-identity)
* [Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/msi-authorization)
* [Azure Arc](https://docs.microsoft.com/azure/azure-arc/servers/managed-identity-authentication)
* [Azure Service Fabric](https://docs.microsoft.com/azure/service-fabric/concepts-managed-identity)

### Examples
####  Authenticating in Azure with Managed Identity
This examples demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `ManagedIdentityCredential` in a virtual machine, app service, function app, cloud shell, or AKS environment on Azure, with system assigned, or user assigned managed identity enabled.

see more about how to configure your Azure resource for managed identity in [Enable managed identity for Azure resources](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-managed-identity-for-azure-resources)

```java
/**
 * Authenticate with a User Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .clientId("<USER ASSIGNED MANAGED IDENTITY CLIENT ID>") // only required for user assigned
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(managedIdentityCredential)
        .buildClient();
}
```

```java
/**
 * Authenticate with a System Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(managedIdentityCredential)
        .buildClient();
}
```

## Credential classes

### Authenticating Azure Hosted Applications
        
<table border="1" width="100%">
  <thead>
    <tr>
      <th>credential class</th>
      <th>usage</th>
      <th>configuration</th>
      <th>example</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>DefaultAzureCredential</code></td>
      <td>provides a simplified authentication experience to quickly start developing applications run in the Azure cloud</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#configure-defaultazurecredential">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-with-defaultazurecredential">example</a></td>
    </tr>
    <tr>
      <td><code>ChainedTokenCredential</code></td>
      <td>allows users to define custom authentication flows composing multiple credentials</td>
      <td></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#chaining-credentials">example</a></td>
    </tr>
    <tr>
      <td><code>EnvironmentCredential</code></td>
      <td>authenticates a service principal or user via credential information specified in environment variables</td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td><code>ManagedIdentityCredential</code></td>
      <td>authenticates the managed identity of an azure resource</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-managed-identity-for-azure-resources">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-in-azure-with-managed-identity">example</a></td>
    </tr>
  </tbody>
</table>
    
### Authenticating Service Principals

<table border="1" width="100%">
  <thead>
    <tr>
      <th>credential class</th>
      <th>usage</th>
      <th>configuration</th>
      <th>example</th>
      <th>reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>ClientSecretCredential</code></td>
      <td>authenticates a service principal using a secret</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#creating-a-service-principal-with-the-azure-cli">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-secret">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/app-objects-and-service-principals">Service principal authentication</a></td>
    </tr>
    <tr>
      <td><code>ClientCertificateCredential</code></td>
      <td>authenticates a service principal using a certificate</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#creating-a-service-principal-with-the-azure-cli">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-certificate">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/app-objects-and-service-principals">Service principal authentication</a></td>
    </tr>
  </tbody>
</table>

### Authenticating Users

<table border="1" width="100%">
  <thead>
    <tr>
      <th>credential class</th>
      <th>usage</th>
      <th>configuration</th>
      <th>example</th>
      <th>reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>DeviceCodeCredential</code></td>
      <td>interactively authenticates a user on devices with limited UI</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-applications-for-device-code-flow">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-device-code-flow">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-device-code">Device code authentication</a></td>
    </tr>
    <tr>
      <td><code>InteractiveBrowserCredential</code></td>
      <td>interactively authenticates a user with the default system browser</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-applications-for-interactive-browser-oauth-2-flow">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-interactively-in-the-browser">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow">OAuth2 authentication code</a></td>
    </tr>
    <tr>
      <td><code>UsernamePasswordCredential</code></td>
      <td>authenticates a user with a username and password without multi-factored auth</td>
      <td></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-username-and-password">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth-ropc">Username + password authentication</a></td>
    </tr>
    <tr>
      <td><code>AuthorizationCodeCredential</code></td>
      <td>authenticate a user with a previously obtained authorization code as part of an Oauth 2 flow</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-applications-for-oauth-2-auth-code-flow">configuration</a></td>
      <td></td>
      <td><a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow">OAuth2 authentication code</a></td>
    </tr>
  </tbody>
</table>

### Authenticating via Development Tools

<table border="1" width="100%">
  <thead>
    <tr>
      <th>credential class</th>
      <th>usage</th>
      <th>configuration</th>
      <th>example</th>
      <th>reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AzureCliCredential</code></td>
      <td>authenticate in a development environment with the enabled user or service principal in Azure CLI</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-cli-for-azureclicredential">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-cli">example</a></td>
      <td><a href="https://docs.microsoft.com/cli/azure/authenticate-azure-cli">Azure CLI authentication</a></td>
    </tr>
    <tr>
      <td><code>IntelliJCredential</code></td>
      <td>authenticate in a development environment with the account in Azure Toolkit for IntelliJ</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-toolkit-for-intellij-for-intellijcredential">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-intellij-idea">example</a></td>
      <td><a href="https://docs.microsoft.com/azure/developer/java/toolkit-for-intellij/sign-in-instructions">IntelliJ authentication</a></td>
    </tr>
    <tr>
      <td><code>VisualStudioCodeCredential</code></td>
      <td>authenticate in a development environment with the account in Visual Studio Azure Account extension</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-visual-studio-code-azure-account-extension-for-visualstudiocodecredential">configuration</a></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-visual-studio-code">example</a></td>
      <td><a href="https://code.visualstudio.com/docs/azure/extensions">VS Code Azure extension</a></td>
    </tr>
  </tbody>
</table>

> __Note:__ All credential implementations in the Azure Identity library are threadsafe, and a single credential instance can be used to create multiple service clients.

Credentials can be chained together to be tried in turn until one succeeds using the `ChainedTokenCredential`; see [chaining credentials](#chaining-credentials) for details.

## Environment Variables
`DefaultAzureCredential` and `EnvironmentCredential` can be configured with environment variables. Each type of authentication requires values for specific variables:

#### Service principal with secret
<table border="1" width="100%">
  <thead>
    <tr>
      <th>variable name</th>
      <th>value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>id of an Azure Active Directory application</td>
    </tr>
    <tr>
      <td><code>AZURE_TENANT_ID</code></td>
      <td>id of the application's Azure Active Directory tenant</td>
    </tr>
    <tr>
      <td><code>AZURE_CLIENT_SECRET</code></td>
      <td>one of the application's client secrets</td>
    </tr>
  </tbody>
</table>

#### Service principal with certificate
<table border="1" width="100%">
  <thead>
    <tr>
      <th>variable name</th>
      <th>value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>id of an Azure Active Directory application</td>
    </tr>
    <tr>
      <td><code>AZURE_TENANT_ID</code></td>
      <td>id of the application's Azure Active Directory tenant</td>
    </tr>
    <tr>
      <td><code>AZURE_CLIENT_CERTIFICATE_PATH</code></td>
      <td>path to a PEM-encoded certificate file including private key (without password protection)</td>
    </tr>
  </tbody>
</table>

#### Username and password
<table border="1" width="100%">
  <thead>
    <tr>
      <th>variable name</th>
      <th>value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>id of an Azure Active Directory application</td>
    </tr>
    <tr>
      <td><code>AZURE_USERNAME</code></td>
      <td>a username (usually an email address)</td>
    </tr>
    <tr>
      <td><code>AZURE_PASSWORD</code></td>
      <td>that user's password</td>
    </tr>
  </tbody>
</table>

Configuration is attempted in the above order. For example, if values for a client secret and certificate are both present, the client secret will be used.

## Troubleshooting
Credentials raise exceptions either when they fail to authenticate or cannot execute authentication.
When credentials fail to authenticate, the`ClientAuthenticationException` is raised and it has a `message` attribute which
describes why authentication failed. When this exception is raised by `ChainedTokenCredential`, the chained execution of underlying list of credentials is stopped.

When credentials cannot execute authentication due to one of the underlying resources required by the credential being unavailable on the machine, the`CredentialUnavailableException` is raised and it has a `message` attribute which
describes why the credential is unavailable for authentication execution . When this exception is raised by `ChainedTokenCredential`, the message collects error messages from each credential in the chain.

### Enable client logging

Azure SDK for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

The java client libraries listed [here](https://azure.github.io/azure-sdk/releases/latest/java.html) support authenticating with `TokenCredential` and the Azure Identity library.  You can learn more about their use, and find additional documentation on use of these client libraries along samples with can be found in the links mentioned [here](https://azure.github.io/azure-sdk/releases/latest/java.html).

The [microsoft-graph-sdk][https://github.com/microsoftgraph/msgraph-sdk-java] also supports authenticating with `TokenCredential` and the Azure Identity library.

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[aad_doc]: https://docs.microsoft.com/azure/active-directory/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[keys_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-keys
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[eventhubs_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[javadoc]: https://azure.github.io/azure-sdk-for-java
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fidentity%2Fazure-identity%2FREADME.png)
