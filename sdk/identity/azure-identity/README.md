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
    <version>1.16.3</version>
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
- [Azure CLI](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#azure-cli-credential)

Select each item above to learn about how to configure them for Azure Identity authentication.

## Key concepts

### Credentials

A credential is a class that contains or can obtain the data needed for a service client to authenticate requests. Service clients across the Azure SDK accept credentials when they're constructed. The service clients use those credentials to authenticate requests to the service.

The Azure Identity library focuses on OAuth authentication with Microsoft Entra ID, and it offers various credential classes capable of acquiring a Microsoft Entra token to authenticate service requests. All of the credential classes in this library are implementations of the `TokenCredential` abstract class in [azure-core][azure_core_library], and any of them can be used by to construct service clients capable of authenticating with a `TokenCredential`.

See [Credential classes](#credential-classes) for a complete list of available credential classes.

### DefaultAzureCredential

`DefaultAzureCredential` simplifies authentication while developing apps that deploy to Azure by combining credentials used in Azure hosting environments with credentials used in local development. For more information, see [DefaultAzureCredential overview][dac_overview].

#### Continuation policy

As of v1.10.0, `DefaultAzureCredential` attempts to authenticate with all developer credentials until one succeeds, regardless of any errors previous developer credentials experienced. For example, a developer credential may attempt to get a token and fail, so `DefaultAzureCredential` continues to the next credential in the flow. Deployed service credentials stop the flow with a thrown exception if they're able to attempt token retrieval, but don't receive one.

This allows for trying all of the developer credentials on your machine while having predictable deployed behavior.

## Examples

You can find more examples of using various credentials in [Azure Identity Examples Wiki page](https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples).

### Authenticate with `DefaultAzureCredential`

This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using `DefaultAzureCredential`:

```java
/**
 * DefaultAzureCredential first checks environment variables for configuration.
 * If environment configuration is incomplete, it tries managed identity.
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

### Authenticate a user-assigned managed identity with `DefaultAzureCredential`

To authenticate using user-assigned managed identity, ensure that configuration instructions for your supported Azure resource [here](#managed-identity-support) have been successfully completed.

The below example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using `DefaultAzureCredential`, deployed to an Azure resource with a user-assigned managed identity configured.

See more about how to configure a user-assigned managed identity for an Azure resource in [Enable managed identity for Azure resources](https://learn.microsoft.com/azure/developer/java/sdk/identity-azure-hosted-auth#managed-identity-credential).

```java
/**
 * DefaultAzureCredential uses the user-assigned managed identity with the specified client ID.
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

In addition to configuring the `managedIdentityClientId` via code, it can also be set using the `AZURE_CLIENT_ID` environment variable. These two approaches are equivalent when using `DefaultAzureCredential`.

### Authenticate a user in Azure Toolkit for IntelliJ with `DefaultAzureCredential`

To authenticate using IntelliJ, ensure that configuration instructions [here](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#sign-in-azure-toolkit-for-intellij-for-intellijcredential) have been successfully completed.

The below example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using `DefaultAzureCredential`, on a workstation with IntelliJ IDEA installed, and the user has signed in with an Azure account to the Azure Toolkit for IntelliJ.

See more about how to configure your IntelliJ IDEA in [Sign in Azure Toolkit for IntelliJ for IntelliJCredential](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#intellij-credential).

```java
/**
 * DefaultAzureCredential uses the signed-in user from Azure Toolkit for Java.
 */
public void createDefaultAzureCredentialForIntelliJ() {
    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultCredential)
        .buildClient();
}
```

### Authenticate Using Visual Studio Code with `DefaultAzureCredential`

To authenticate using Visual Studio Code, ensure you have signed in through the **Azure Resources** extension. The signed-in user is then picked up automatically by `DefaultAzureCredential` in the Azure SDK for Java.

#### Prerequisites

- [Azure Resources Extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azureresourcegroups) is installed in Visual Studio Code.
- You are signed in using the `Azure: Sign In` command in VS Code.
- Your project includes the [`azure-identity-broker`](https://search.maven.org/artifact/com.azure/azure-identity-broker) package.

#### Example: Use `DefaultAzureCredential` with Key Vault

The following example demonstrates authenticating the `SecretClient` from the [`azure-security-keyvault-secrets`](https://learn.microsoft.com/java/api/overview/azure/security-keyvault-secrets-readme?view=azure-java-stable) client library using `DefaultAzureCredential`:

```java
/**
 * DefaultAzureCredential uses the signed-in user from Visual Studio Code
 * via the Azure Resources extension.
 */
public void createDefaultAzureCredentialForVSCode() {
    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultCredential)
        .buildClient();
    }
```

## Managed Identity support

The [Managed identity authentication](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/overview) is supported indirectly via `DefaultAzureCredential` or directly via `ManagedIdentityCredential` for the following Azure Services:

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

This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `ManagedIdentityCredential` in a Virtual Machine, App Service, Functions app, Cloud Shell, or AKS environment on Azure, with system-assigned or user-assigned managed identity enabled.

See more about how to configure your Azure resource for managed identity in [Enable managed identity for Azure resources](https://learn.microsoft.com/azure/developer/java/sdk/identity-azure-hosted-auth#managed-identity-credential)

```java
/**
 * Authenticate with a user-assigned managed identity.
 */
public void createManagedIdentityCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .clientId("<USER-ASSIGNED MANAGED IDENTITY CLIENT ID>") // only required for user-assigned
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(managedIdentityCredential)
        .buildClient();
}
```

```java
public void createUserAssignedManagedIdentityCredentialWithResourceId() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .resourceId("/subscriptions/<subscriptionID>/resourcegroups/<resource group>/providers/Microsoft.ManagedIdentity/userAssignedIdentities/<MI name>") // only required for user-assigned
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(managedIdentityCredential)
        .buildClient();
}
```

```java
public void createUserAssignedManagedIdentityCredentialWithObjectId() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .objectId("<USER-ASSIGNED MANAGED IDENTITY OBJECT ID>") // only required for user-assigned
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
 * Authenticate with a system-assigned managed identity.
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

### Define a custom authentication flow with `ChainedTokenCredential`

While `DefaultAzureCredential` is generally the quickest way to authenticate apps for Azure, you can create a customized chain of credentials to be considered. `ChainedTokenCredential` enables users to combine multiple credential instances to define a customized chain of credentials. For more information, see [ChainedTokenCredential overview][ctc_overview].

## Cloud / Sovereign configuration

By default, credentials authenticate to the Microsoft Entra endpoint for Azure Public Cloud. To access resources in other clouds, such as Azure US Government or a private cloud, use one of the following solutions:

1. Configure credentials with the `authorityHost` method. For example:

```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
    .authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT)
    .build();
```

[AzureAuthorityHosts](https://learn.microsoft.com/java/api/com.azure.identity.azureauthorityhosts?view=azure-java-stable) defines authorities for well-known clouds.

2. Set the `AZURE_AUTHORITY_HOST` environment variable to the appropriate authority host URL. For example, `https://login.microsoftonline.us/`. Note that this setting affects all credentials in the environment. Use the previous solution to set the authority host on a specific credential.

Not all credentials honor this configuration. Credentials that authenticate through a development tool, such as `AzureCliCredential`, use that tool's configuration.

## Credential classes

### Credential chains

|Credential|Usage|Example|
|-|-|-|
|[DefaultAzureCredential][cred_dac]|Provides a simplified authentication experience to quickly start developing apps run in Azure|[example][cred_dac_example]|
|[ChainedTokenCredential][cred_ctc]|Allows users to define custom authentication flows composing multiple credentials|[example][cred_ctc_example]|

### Authenticate Azure-hosted applications

|Credential|Usage|Example|Reference|
|-|-|-|-|
|[EnvironmentCredential][cred_ec]|Authenticates a service principal or user via credential information specified in environment variables|||
|[ManagedIdentityCredential][cred_mic]|Authenticates the managed identity of an Azure resource|[example][cred_mic_example]||
|[WorkloadIdentityCredential][cred_wic]|Supports Microsoft Entra Workload ID on Kubernetes|[example][cred_wic_example]|[Microsoft Entra Workload ID][cred_wic_ref]|

### Authenticate service principals

|Credential|Usage|Example|Reference|
|-|-|-|-|
|[AzurePipelinesCredential][cred_apc]|Authenticates with a service connection in Azure Pipelines.||[Manage service connections][cred_apc_ref]|
|[ClientAssertionCredential][cred_cac]|Authenticates a service principal using a signed client assertion|||
|[ClientCertificateCredential][cred_ccc]|Authenticates a service principal using a certificate|[example][cred_ccc_example]|[Service principal authentication][sp]|
|[ClientSecretCredential][cred_csc]|Authenticates a service principal using a secret|[example][cred_csc_example]|[Service principal authentication][sp]|

### Authenticate users

|Credential| Usage                                                                                                     |Example|Reference|
|-|-----------------------------------------------------------------------------------------------------------|-|-|
|[AuthorizationCodeCredential][cred_acc]| Authenticates a user with a previously obtained authorization code as part of an OAuth 2.0 auth code flow ||[OAuth 2.0 auth code][cred_acc_ref]|
|[DeviceCodeCredential][cred_dcc]| Interactively authenticates a user on devices with limited UI                                             |[example][cred_dcc_example]|[device code authentication][cred_dcc_ref]|
|[InteractiveBrowserCredential][cred_ibc]| Interactively authenticates a user with the default system browser                                        |[example][cred_ibc_example]|[OAuth 2.0 auth code][cred_acc_ref]|
|[OnBehalfOfCredential][cred_obo]| Propagates the delegated user identity and permissions through the request chain                          ||[On-behalf-of authentication][cred_obo_ref]|

### Authenticate via development tools

|Credential|Usage|Example|Reference|
|-|-|-|-|
|[AzureCliCredential][cred_az]|Authenticates in a development environment with the enabled user or service principal in Azure CLI|[example][cred_az_example]|[Azure CLI authentication][cred_az_ref]|
|[AzureDeveloperCliCredential][cred_azd]|Authenticates in a development environment with the enabled user or service principal in Azure Developer CLI||[Azure Developer CLI authentication][cred_azd_ref]|
|[AzurePowerShellCredential][cred_azpwsh]|Authenticates in a development environment with the enabled user or service principal in Azure PowerShell|[example][cred_azpwsh_example]|[Azure PowerShell authentication][cred_azpwsh_ref]|
|[IntelliJCredential][cred_ij]|Authenticates in a development environment with the account in Azure Toolkit for IntelliJ|[example][cred_ij_example]|[IntelliJ authentication][cred_ij_ref]|
|[VisualStudioCodeCredential][cred_vsc]|Authenticates in a development environment with the account in Visual Studio Code|||

> __Note:__ All credential implementations in the Azure Identity library are threadsafe, and a single credential instance can be used to create multiple service clients.

Credentials can be chained together to be tried in turn until one succeeds using `ChainedTokenCredential`. For more information, see [chaining credentials](#define-a-custom-authentication-flow-with-the-chainedtokencredential).

## Environment variables

`DefaultAzureCredential` and `EnvironmentCredential` can be configured with environment variables. Each type of authentication requires values for specific variables.

### Service principal with secret

|Variable name|Value|
|-|-|
|`AZURE_CLIENT_ID`|ID of a Microsoft Entra application|
|`AZURE_TENANT_ID`|ID of the application's Microsoft Entra tenant|
|`AZURE_CLIENT_SECRET`|one of the application's client secrets|

### Service principal with certificate

|Variable name|Value|
|-|-|
|`AZURE_CLIENT_ID`|ID of a Microsoft Entra application|
|`AZURE_TENANT_ID`|ID of the application's Microsoft Entra tenant|
|`AZURE_CLIENT_CERTIFICATE_PATH`|path to a PFX or PEM-encoded certificate file including private key|
|`AZURE_CLIENT_CERTIFICATE_PASSWORD`|(optional) password for certificate. The certificate can't be password-protected unless this value is specified.|

### Managed identity (`DefaultAzureCredential`)

|Variable name|Value|
|-|-|
|`AZURE_CLIENT_ID`|The client ID for the user-assigned managed identity.|

Configuration is attempted in the preceding order. For example, if values for a client secret and certificate are both present, the client secret is used.

## Continuous Access Evaluation

As of v1.10.0, accessing resources protected by [Continuous Access Evaluation](https://learn.microsoft.com/entra/identity/conditional-access/concept-continuous-access-evaluation) (CAE) is possible on a per-request basis. This can be enabled using the [`TokenRequestContext.setCaeEnabled(boolean)` API](https://learn.microsoft.com/java/api/com.azure.core.credential.tokenrequestcontext?view=azure-java-stable#com-azure-core-credential-tokenrequestcontext-setcaeenabled(boolean)). CAE isn't supported for developer credentials.

## Token caching

Token caching is a feature provided by the Azure Identity library that allows apps to:

- Cache tokens in memory (default) or on disk (opt-in).
- Improve resilience and performance.
- Reduce the number of requests made to Microsoft Entra ID to obtain access tokens.

The Azure Identity library offers both in-memory and persistent disk caching. For more information, see the [token caching documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TOKEN_CACHING.md).

## Brokered authentication

An authentication broker is an application that runs on a user’s machine and manages the authentication handshakes and token maintenance for connected accounts. Currently, only the Windows Web Account Manager (WAM) is supported. To enable support, use the [`azure-identity-broker`][azure_identity_broker] package. For details on authenticating using WAM, see the [broker plugin documentation][azure_identity_broker_readme].


## Troubleshooting

Credentials raise exceptions when they fail to authenticate or can't execute authentication. When credentials fail to authenticate, the`ClientAuthenticationException` is raised. The exception has a `message` attribute, which describes why authentication failed. When `ChainedTokenCredential` raises this exception, the chained execution of underlying list of credentials is stopped.

When credentials can't execute authentication due to one of the underlying resources required by the credential being unavailable on the machine, the `CredentialUnavailableException` is raised. The exception has a `message` attribute that describes why the credential is unavailable for authentication execution. When `ChainedTokenCredential` raises this exception, the message collects error messages from each credential in the chain.

See the [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

## Next steps

The Java client libraries listed [here](https://azure.github.io/azure-sdk/releases/latest/java.html) support authenticating with `TokenCredential` and the Azure Identity library. You can learn more about their use, and find additional documentation on use of these client libraries along samples with can be found in the links mentioned [here](https://azure.github.io/azure-sdk/releases/latest/java.html).

The [microsoft-graph-sdk](https://github.com/microsoftgraph/msgraph-sdk-java) also supports authenticating with `TokenCredential` and the Azure Identity library.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information, see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core
[azure_identity_broker]: https://central.sonatype.com/artifact/com.azure/azure-identity-broker
[azure_identity_broker_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity-broker/README.md
[azure_sub]: https://azure.microsoft.com/free/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[cred_acc]: https://learn.microsoft.com/java/api/com.azure.identity.authorizationcodecredential?view=azure-java-stable
[cred_acc_ref]: https://learn.microsoft.com/entra/identity-platform/v2-oauth2-auth-code-flow
[cred_apc]: https://learn.microsoft.com/java/api/com.azure.identity.azurepipelinescredential?view=azure-java-stable
[cred_apc_ref]: https://learn.microsoft.com/azure/devops/pipelines/library/service-endpoints?view=azure-devops&tabs=yaml
[cred_az]: https://learn.microsoft.com/java/api/com.azure.identity.azureclicredential?view=azure-java-stable
[cred_az_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-cli
[cred_az_ref]: https://learn.microsoft.com/cli/azure/authenticate-azure-cli
[cred_azd]: https://learn.microsoft.com/java/api/com.azure.identity.azuredeveloperclicredential?view=azure-java-stable
[cred_azd_ref]: https://learn.microsoft.com/azure/developer/azure-developer-cli/reference#azd-auth
[cred_azpwsh]: https://learn.microsoft.com/java/api/com.azure.identity.azurepowershellcredential?view=azure-java-stable
[cred_azpwsh_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-powershell
[cred_azpwsh_ref]: https://learn.microsoft.com/powershell/azure/authenticate-azureps
[cred_cac]: https://learn.microsoft.com/java/api/com.azure.identity.clientassertioncredential?view=azure-java-stable
[cred_ccc]: https://learn.microsoft.com/java/api/com.azure.identity.clientcertificatecredential?view=azure-java-stable
[cred_ccc_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-certificate
[cred_csc]: https://learn.microsoft.com/java/api/com.azure.identity.clientsecretcredential?view=azure-java-stable
[cred_csc_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-secret
[cred_ctc]: https://learn.microsoft.com/java/api/com.azure.identity.chainedtokencredential?view=azure-java-stable
[cred_ctc_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#chaining-credentials
[cred_dac]: https://learn.microsoft.com/java/api/com.azure.identity.defaultazurecredential?view=azure-java-stable
[cred_dac_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-with-defaultazurecredential
[cred_dcc]: https://learn.microsoft.com/java/api/com.azure.identity.devicecodecredential?view=azure-java-stable
[cred_dcc_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-device-code-flow
[cred_dcc_ref]: https://learn.microsoft.com/entra/identity-platform/v2-oauth2-device-code
[cred_ec]: https://learn.microsoft.com/java/api/com.azure.identity.environmentcredential?view=azure-java-stable
[cred_ibc]: https://learn.microsoft.com/java/api/com.azure.identity.interactivebrowsercredential?view=azure-java-stable
[cred_ibc_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-interactively-in-the-browser
[cred_ij]: https://learn.microsoft.com/java/api/com.azure.identity.intellijcredential?view=azure-java-stable
[cred_ij_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-intellij-idea
[cred_ij_ref]: https://learn.microsoft.com/azure/developer/java/toolkit-for-intellij/sign-in-instructions
[cred_mic]: https://learn.microsoft.com/java/api/com.azure.identity.managedidentitycredential?view=azure-java-stable
[cred_mic_example]: https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-in-azure-with-managed-identity
[cred_obo]: https://learn.microsoft.com/java/api/com.azure.identity.onbehalfofcredential?view=azure-java-stable
[cred_obo_ref]: https://learn.microsoft.com/entra/identity-platform/v2-oauth2-on-behalf-of-flow
[cred_vsc]: https://learn.microsoft.com/java/api/com.azure.identity.visualstudiocodecredential?view=azure-java-stable
[cred_wic]: https://learn.microsoft.com/java/api/com.azure.identity.workloadidentitycredential?view=azure-java-stable
[cred_wic_example]: https://learn.microsoft.com/azure/aks/workload-identity-overview?tabs=java#azure-identity-client-libraries
[cred_wic_ref]: https://learn.microsoft.com/azure/aks/workload-identity-overview
[ctc_overview]: https://aka.ms/azsdk/java/identity/credential-chains#chainedtokencredential-overview
[dac_overview]: https://aka.ms/azsdk/java/identity/credential-chains#defaultazurecredential-overview
[entraid_doc]: https://learn.microsoft.com/entra/identity/
[javadoc]: https://learn.microsoft.com/java/api/com.azure.identity?view=azure-java-stable
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[secrets_client_library]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[sp]: https://learn.microsoft.com/entra/identity-platform/app-objects-and-service-principals
