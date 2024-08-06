# Azure Identity client library for Java

The Azure Identity library provides [Microsoft Entra ID](https://learn.microsoft.com/entra/fundamentals/whatis) ([formerly Azure Active Directory](https://learn.microsoft.com/entra/fundamentals/new-name)) token authentication support across the Azure SDK. It provides a set of [TokenCredential](https://learn.microsoft.com/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable) implementations that can be used to construct Azure SDK clients that support Microsoft Entra token authentication.

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
    <artifactId>azure-identity</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

To take dependency on a particular version of the library that isn't present in the BOM, add the direct dependency to your project as follows:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- An [Azure subscription][azure_sub].
- The Azure CLI can also be useful for authenticating in a development environment, creating accounts, and managing account roles.

### Authenticate the client

When debugging and executing code locally, it's typical for a developer to use their own account for authenticating calls to Azure services. There are several developer tools that can be used to perform this authentication in your development environment:

- [Azure Toolkit for IntelliJ](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#intellij-credential)
- [Visual Studio Code Azure Account Extension](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#visual-studio-code-credential)
  - It's a [known issue](https://github.com/Azure/azure-sdk-for-java/issues/27364) that `VisualStudioCodeCredential` doesn't work with [Azure Account extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode.azure-account) versions newer than **0.9.11**. A long-term fix to this problem is in progress. In the meantime, consider authenticating via the Azure CLI (below).
- [Azure CLI](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#azure-cli-credential)

Select each item above to learn about how to configure them for Azure Identity authentication.

## Key concepts

### Credentials

A credential is a class that contains or can obtain the data needed for a service client to authenticate requests. Service clients across the Azure SDK accept credentials when they're constructed. The service clients use those credentials to authenticate requests to the service.

The Azure Identity library focuses on OAuth authentication with Microsoft Entra ID, and it offers various credential classes capable of acquiring a Microsoft Entra token to authenticate service requests. All of the credential classes in this library are implementations of the `TokenCredential` abstract class in [azure-core][azure_core_library], and any of them can be used by to construct service clients capable of authenticating with a `TokenCredential`.

See [Credential classes](#credential-classes) for a complete list of available credential classes.

### DefaultAzureCredential

The `DefaultAzureCredential` is appropriate for most scenarios where the application is intended to ultimately be run in Azure. This is because the `DefaultAzureCredential` combines credentials commonly used to authenticate when deployed, with credentials used to authenticate in a development environment.

> Note: `DefaultAzureCredential` is intended to simplify getting started with the SDK by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't served by the default settings should use other credential types.

The `DefaultAzureCredential` will attempt to authenticate via the following mechanisms in order.

![DefaultAzureCredential authentication flow](images/mermaidjs/DefaultAzureCredentialAuthFlow.svg)

1. **Environment** - The `DefaultAzureCredential` will read account information specified via [environment variables](#environment-variables) and use it to authenticate.
2. **Workload Identity** - If the app is deployed on Kubernetes with environment variables set by the workload identity webhook, `DefaultAzureCredential` will authenticate the configured identity.
3. **Managed Identity** - If the application is deployed to an Azure host with Managed Identity enabled, the `DefaultAzureCredential` will authenticate with that account.
4. **Azure Developer CLI** - If the developer has authenticated an account via the Azure Developer CLI `azd auth login` command, the `DefaultAzureCredential` will authenticate with that account.
5. **IntelliJ** - If the developer has authenticated via Azure Toolkit for IntelliJ, the `DefaultAzureCredential` will authenticate with that account.
6. **Azure CLI** - If the developer has authenticated an account via the Azure CLI `az login` command, the `DefaultAzureCredential` will authenticate with that account.
7. **Azure PowerShell** - If the developer has authenticated an account via the Azure PowerShell `Connect-AzAccount` command, the `DefaultAzureCredential` will authenticate with that account.

#### Continuation policy

As of v1.10.0, `DefaultAzureCredential` will attempt to authenticate with all developer credentials until one succeeds, regardless of any errors previous developer credentials experienced. For example, a developer credential may attempt to get a token and fail, so `DefaultAzureCredential` will continue to the next credential in the flow. Deployed service credentials will stop the flow with a thrown exception if they're able to attempt token retrieval, but don't receive one.

This allows for trying all of the developer credentials on your machine while having predictable deployed behavior.

#### Note about `VisualStudioCodeCredential`

Due to a [known issue](https://github.com/Azure/azure-sdk-for-java/issues/27364), `VisualStudioCodeCredential` has been removed from the `DefaultAzureCredential` token chain. When the issue is resolved in a future release, this change will be reverted.

## Examples

You can find more examples of using various credentials in [Azure Identity Examples Wiki page](https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples).

### Authenticate with `DefaultAzureCredential`

This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`.

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

### Authenticate a user-assigned managed identity with `DefaultAzureCredential`

To authenticate using user-assigned managed identity, ensure that configuration instructions for your supported Azure resource [here](#managed-identity-support) have been successfully completed.

The below example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`, deployed to an Azure resource with a user-assigned managed identity configured.

See more about how to configure a user-assigned managed identity for an Azure resource in [Enable managed identity for Azure resources](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-managed-identity-for-azure-resources).

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

### Authenticate a user in Azure Toolkit for IntelliJ with `DefaultAzureCredential`

To authenticate using IntelliJ, ensure that configuration instructions [here](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#sign-in-azure-toolkit-for-intellij-for-intellijcredential) have been successfully completed.

The below example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`, on a workstation with IntelliJ IDEA installed, and the user has signed in with an Azure account to the Azure Toolkit for IntelliJ.

See more about how to configure your IntelliJ IDEA in [Sign in Azure Toolkit for IntelliJ for IntelliJCredential](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#sign-in-azure-toolkit-for-intellij-for-intellijcredential).

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

## Managed Identity support

The [Managed identity authentication](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/overview) is supported via either the `DefaultAzureCredential` or the `ManagedIdentityCredential` directly for the following Azure Services:

- [Azure App Service and Azure Functions](https://learn.microsoft.com/azure/app-service/overview-managed-identity?tabs=dotnet)
- [Azure Arc](https://learn.microsoft.com/azure/azure-arc/servers/managed-identity-authentication)
- [Azure Cloud Shell](https://learn.microsoft.com/azure/cloud-shell/msi-authorization)
- [Azure Kubernetes Service](https://learn.microsoft.com/azure/aks/use-managed-identity)
- [Azure Service Fabric](https://learn.microsoft.com/azure/service-fabric/concepts-managed-identity)
- [Azure Virtual Machines](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/how-to-use-vm-token)
- [Azure Virtual Machines Scale Sets](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/qs-configure-powershell-windows-vmss)

**Note:** Use `azure-identity` version `1.7.0` or later to utilize [token caching](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TOKEN_CACHING.md) support for managed identity authentication.

### Examples

#### Authenticate in Azure with Managed Identity

This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `ManagedIdentityCredential` in a virtual machine, app service, function app, cloud shell, or AKS environment on Azure, with system-assigned or user-assigned managed identity enabled.

See more about how to configure your Azure resource for managed identity in [Enable managed identity for Azure resources](https://github.com/Azure/azure-sdk-for-java/wiki/Set-up-Your-Environment-for-Authentication#enable-managed-identity-for-azure-resources)

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

### Define a custom authentication flow with the `ChainedTokenCredential`

While the `DefaultAzureCredential` is generally the quickest way to get started developing applications for Azure, more advanced users may want to customize the credentials considered when authenticating. The `ChainedTokenCredential` enables users to combine multiple credential instances to define a customized chain of credentials. This example demonstrates creating a `ChainedTokenCredential`, which will:

- Attempt to authenticate using managed identity.
- Fall back to authenticating via the Azure CLI if managed identity is unavailable in the current environment.

```C# Snippet:CustomChainedTokenCredential
// Authenticate using managed identity if it is available; otherwise use the Azure CLI to authenticate.

    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder().build();
    AzureCliCredential cliCredential = new AzureCliCredentialBuilder().build();

    ChainedTokenCredential credential = new ChainedTokenCredentialBuilder().addLast(managedIdentityCredential).addLast(cliCredential).build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(credential)
        .buildClient();
```

## Cloud configuration

Credentials default to authenticating to the Microsoft Entra endpoint for Azure Public Cloud. To access resources in other clouds, such as Azure Government or a private cloud, configure credentials with the `auhtorityHost` argument. [AzureAuthorityHosts](https://learn.microsoft.com/java/api/com.azure.identity.azureauthorityhosts?view=azure-java-stable) defines authorities for well-known clouds:

```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
    .authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT)
    .build();
```

Not all credentials require this configuration. Credentials that authenticate through a development tool, such as `AzureCliCredential`, use that tool's configuration. Similarly, `VisualStudioCodeCredential` accepts an `authority` argument but defaults to the authority matching VS Code's "Azure: Cloud" setting.

## Credential classes

### Authenticate Azure-hosted applications

<table style="border: 1px; width: 100%;">
  <caption>Authenticate Azure-hosted applications</caption>
  <thead>
    <tr>
      <th>Credential class</th>
      <th>Usage</th>
      <th>Example</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.defaultazurecredential?view=azure-java-stable">DefaultAzureCredential</a></code></td>
      <td>provides a simplified authentication experience to quickly start developing applications run in Azure</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-with-defaultazurecredential">example</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.chainedtokencredential?view=azure-java-stable">ChainedTokenCredential</a></code></td>
      <td>allows users to define custom authentication flows composing multiple credentials</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#chaining-credentials">example</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.environmentcredential?view=azure-java-stable">EnvironmentCredential</a></code></td>
      <td>authenticates a service principal or user via credential information specified in environment variables</td>
      <td></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.managedidentitycredential?view=azure-java-stable">ManagedIdentityCredential</a></code></td>
      <td>authenticates the managed identity of an Azure resource</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-in-azure-with-managed-identity">example</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.workloadidentitycredential?view=azure-java-stable">WorkloadIdentityCredential</a></code></td>
      <td>supports <a href="https://learn.microsoft.com/azure/aks/workload-identity-overview">Microsoft Entra Workload ID</a> on Kubernetes</td>
      <td><a href="https://learn.microsoft.com/java/api/com.azure.identity.workloadidentitycredential?view=azure-java-stable">example</a></td>
    </tr>
  </tbody>
</table>

### Authenticate service principals

<table style="border: 1px; width: 100%;">
  <caption>Authenticate service principals</caption>
  <thead>
    <tr>
      <th>Credential class</th>
      <th>Usage</th>
      <th>Example</th>
      <th>Reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azurepipelinescredential?view=azure-java-stable">AzurePipelinesCredential</a></td>
      <td>Authenticates with a <a href="https://learn.microsoft.com/azure/devops/pipelines/library/service-endpoints?view=azure-devops&tabs=yaml">service connection in Azure Pipelines.</a></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.clientassertioncredential?view=azure-java-stable">ClientAssertionCredential</a></td>
      <td>authenticates a service principal using a signed client assertion</td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.clientcertificatecredential?view=azure-java-stable">ClientCertificateCredential</a></code></td>
      <td>authenticates a service principal using a certificate</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-certificate">example</a></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/app-objects-and-service-principals">Service principal authentication</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.clientsecretcredential?view=azure-java-stable">ClientSecretCredential</a></code></td>
      <td>authenticates a service principal using a secret</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-secret">example</a></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/app-objects-and-service-principals">Service principal authentication</a></td>
    </tr>
  </tbody>
</table>

### Authenticate users

<table style="border: 1px; width: 100%;">
  <caption>Authenticate users</caption>
  <thead>
    <tr>
      <th>Credential class</th>
      <th>Usage</th>
      <th>Example</th>
      <th>Reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.authorizationcodecredential?view=azure-java-stable">AuthorizationCodeCredential</a></code></td>
      <td>authenticate a user with a previously obtained authorization code as part of an Oauth 2 flow</td>
      <td></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/v2-oauth2-auth-code-flow">OAuth2 authentication code</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.devicecodecredential?view=azure-java-stable">DeviceCodeCredential</a></code></td>
      <td>interactively authenticates a user on devices with limited UI</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-device-code-flow">example</a></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/v2-oauth2-device-code">Device code authentication</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.interactivebrowsercredential?view=azure-java-stable">InteractiveBrowserCredential</a></code></td>
      <td>interactively authenticates a user with the default system browser</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-interactively-in-the-browser">example</a></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/v2-oauth2-auth-code-flow">OAuth2 authentication code</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.onbehalfofcredential?view=azure-java-stable">OnBehalfOfCredential</a></code></td>
      <td>propagates the delegated user identity and permissions through the request chain</td>
      <td></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/v2-oauth2-on-behalf-of-flow">On-behalf-of authentication</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.usernamepasswordcredential?view=azure-java-stable">UsernamePasswordCredential</a></code></td>
      <td>authenticates a user with a username and password without multi-factored auth</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-username-and-password">example</a></td>
      <td><a href="https://learn.microsoft.com/entra/identity-platform/v2-oauth-ropc">Username + password authentication</a></td>
    </tr>
  </tbody>
</table>

### Authenticate via development tools

<table style="border: 1px; width: 100%;">
  <caption>Authenticate via development tools</caption>
  <thead>
    <tr>
      <th>Credential class</th>
      <th>Usage</th>
      <th>Example</th>
      <th>Reference</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azureclicredential?view=azure-java-stable">AzureCliCredential</a></code></td>
      <td>Authenticate in a development environment with the enabled user or service principal in Azure CLI</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-cli">example</a></td>
      <td><a href="https://learn.microsoft.com/cli/azure/authenticate-azure-cli">Azure CLI authentication</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azuredeveloperclicredential?view=azure-java-stable">AzureDeveloperCliCredential</a></code></td>
      <td>Authenticate in a development environment with the enabled user or service principal in Azure Developer CLI</td>
      <td></td>  
      <td><a href="https://learn.microsoft.com/azure/developer/azure-developer-cli/reference">Azure Developer CLI Reference</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azurepowershellcredential?view=azure-java-stable">AzurePowerShellCredential </a></code></td>
      <td>Authenticate in a development environment with the enabled user or service principal in Azure PowerShell</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-powershell">example</a></td>
      <td><a href="https://learn.microsoft.com/powershell/azure/?view=azps-8.3.0">Azure PowerShell documentation</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.intellijcredential?view=azure-java-stable">IntelliJCredential</a></code></td>
      <td>Authenticate in a development environment with the account in Azure Toolkit for IntelliJ</td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-intellij-idea">example</a></td>
      <td><a href="https://learn.microsoft.com/azure/developer/java/toolkit-for-intellij/sign-in-instructions">IntelliJ authentication</a></td>
    </tr>
    <tr>
      <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.visualstudiocodecredential?view=azure-java-stable">VisualStudioCodeCredential</a></code></td>
      <td>Authenticate in a development environment with the account in Visual Studio Code Azure Account extension. </td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-visual-studio-code">example</a></td>
      <td><a href="https://marketplace.visualstudio.com/items?itemName=ms-vscode.azure-account">VS Code Azure Account extension</a></td>
    </tr>
  </tbody>
</table>

> __Note:__ All credential implementations in the Azure Identity library are threadsafe, and a single credential instance can be used to create multiple service clients.

Credentials can be chained together to be tried in turn until one succeeds using the `ChainedTokenCredential`; see [chaining credentials](#define-a-custom-authentication-flow-with-the-chainedtokencredential) for details.

## Environment variables

`DefaultAzureCredential` and `EnvironmentCredential` can be configured with environment variables. Each type of authentication requires values for specific variables:

### Service principal with secret

<table style="border: 1px; width: 100%;">
  <caption>Service principal with secret</caption>
  <thead>
    <tr>
      <th>Variable name</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>ID of a Microsoft Entra application</td>
    </tr>
    <tr>
      <td><code>AZURE_TENANT_ID</code></td>
      <td>ID of the application's Microsoft Entra tenant</td>
    </tr>
    <tr>
      <td><code>AZURE_CLIENT_SECRET</code></td>
      <td>one of the application's client secrets</td>
    </tr>
  </tbody>
</table>

### Service principal with certificate

<table style="border: 1px; width: 100%;">
  <caption>Service principal with certificate</caption>
  <thead>
    <tr>
      <th>Variable name</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>ID of a Microsoft Entra application</td>
    </tr>
    <tr>
      <td><code>AZURE_TENANT_ID</code></td>
      <td>ID of the application's Microsoft Entra tenant</td>
    </tr>
    <tr>
      <td><code>AZURE_CLIENT_CERTIFICATE_PATH</code></td>
      <td>path to a PFX or PEM-encoded certificate file including private key</td>
    </tr>
    <tr>
        <td><code>AZURE_CLIENT_CERTIFICATE_PASSWORD</code></td>
        <td>(optional) password for certificate. The certificate can't be password-protected unless this value is specified.</td>
    </tr>
  </tbody>
</table>

### Username and password

<table style="border: 1px; width: 100%;">
  <caption>Username and password</caption>
  <thead>
    <tr>
      <th>Variable name</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>AZURE_CLIENT_ID</code></td>
      <td>ID of a Microsoft Entra application</td>
    </tr>
    <tr>
      <td><code>AZURE_TENANT_ID</code></td>
      <td>(optional) ID of the application's Microsoft Entra tenant</td>
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

## Continuous Access Evaluation

As of v1.10.0, accessing resources protected by [Continuous Access Evaluation](https://learn.microsoft.com/entra/identity/conditional-access/concept-continuous-access-evaluation) (CAE) is possible on a per-request basis. This can be enabled using the [`TokenRequestContext.setCaeEnabled(boolean)` API](https://learn.microsoft.com/java/api/com.azure.core.credential.tokenrequestcontext?view=azure-java-stable#com-azure-core-credential-tokenrequestcontext-setcaeenabled(boolean)). CAE isn't supported for developer credentials.

## Token caching
Token caching is a feature provided by the Azure Identity library that allows apps to:
- Cache tokens in memory (default) or on disk (opt-in).
- Improve resilience and performance.
- Reduce the number of requests made to Microsoft Entra ID to obtain access tokens.

The Azure Identity library offers both in-memory and persistent disk caching. For more details, see the [token caching documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TOKEN_CACHING.md).

## Brokered authentication

An authentication broker is an application that runs on a user’s machine and manages the authentication handshakes and token maintenance for connected accounts. Currently, only the Windows Web Account Manager (WAM) is supported. To enable support, use the [`azure-identity-broker`][azure_identity_broker] package. For details on authenticating using WAM, see the [broker plugin documentation][azure_identity_broker_readme].

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

<!-- LINKS -->
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[azure_identity_broker]: https://central.sonatype.com/artifact/com.azure/azure-identity-broker
[azure_identity_broker_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity-broker/README.md
[azure_sub]: https://azure.microsoft.com/free/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[entraid_doc]: https://learn.microsoft.com/entra/identity/
[javadoc]: https://learn.microsoft.com/java/api/com.azure.identity?view=azure-java-stable
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fidentity%2Fazure-identity%2FREADME.png)
