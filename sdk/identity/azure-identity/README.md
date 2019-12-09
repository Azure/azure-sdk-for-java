# Azure Identity client library for Java
The Azure Identity library provides Azure Active Directory token authentication support across the Azure SDK. It provides a set of TokenCredential implementations which can be used to construct Azure SDK clients which support AAD token authentication.

 This library currently supports:
  - [Service principal authentication](https://docs.microsoft.com/azure/active-directory/develop/app-objects-and-service-principals)
  - [Managed identity authentication](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview)
  - [Device code authentication](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-device-code)
  - Interactive browser authentication, based on [OAuth2 authentication code](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow)
  - [Username + password authentication](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth-ropc)
  - Shared Token Cache credential, which shares login information with Visual Studio, Azure CLI, and more

  [Source code][source] | [API reference documentation][javadoc] | [Azure Active Directory documentation][aad_doc]

## Table of contents
- [Getting started](#getting-started)
  - [Adding the package to your project](#adding-the-package-to-your-project)
  - [Prerequisites](#prerequisites)
    - [Creating a Service Principal with the Azure CLI](#creating-a-service-principal-with-the-azure-cli)
    - [Enable applications for device code flow](#enable-applications-for-device-code-flow)
    - [Enable applications for interactive browser oauth 2 flow](#enable-applications-for-interactive-browser-oauth-2-flow)
    - [Enable applications for oauth 2 auth code flow](#enable-applications-for-oauth-2-auth-code-flow)
    - [Enable applications for shared token cache credential](#enable-applications-for-shared-token-cache-credential)
  - [Key concepts](#key-concepts)
    - [Credentials](#credentials)
  - [DefaultAzureCredential](#defaultazurecredential)
  - [Environment variables](#environment-variables)
- [Examples](#examples)
  - [Authenticating with `DefaultAzureCredential`](#authenticating-with-defaultazurecredential)
  - [Authenticating a service principal with a client secret](#authenticating-a-service-principal-with-a-client-secret)
  - [Authenticating a user account with device code flow](#authenticating-a-user-account-with-device-code-flow)
  - [Authenticating a user account with username and password](#authenticating-a-user-account-with-username-and-password)
  - [Authenticating a user account with auth code flow](#authenticating-a-user-account-with-auth-code-flow)
  - [Chaining credentials](#chaining-credentials)
- [Troubleshooting](#troubleshooting)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started
### Adding the package to your project

Maven dependency for Azure Secret Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-identity;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
* An [Azure subscription][azure_sub].
* An existing Azure Active Directory service principal. If you need to create a service principal, you can use the Azure Portal or [Azure CLI][azure_cli].

#### Creating a Service Principal with the Azure CLI
Use the [Azure CLI][azure_cli] snippet below to create/get client secret credentials.

 * Create a service principal and configure its access to Azure resources:

    ```bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```

    Output:

    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "dummy-app-name",
        "name": "http://dummy-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```

* Use the returned credentials above to set  **AZURE_CLIENT_ID**(appId), **AZURE_CLIENT_SECRET**(password) and **AZURE_TENANT_ID**(tenant) [environment variables](#environment-variables).

#### Enable applications for device code flow
In order to authenticate a user through device code flow, you need to go to Azure Active Directory on Azure Portal and find you app registration and enable the following 2 configurations:

![device code enable](./images/devicecode-enable.png)

This will let the application authenticate, but the application still doesn't have permission to log you into Active Directory, or access resources on your behalf. Open API Permissions, and enable Microsoft Graph, and the resources you want to access, e.g., Azure Service Management, Key Vault, etc:

![device code permissions](./images/devicecode-permissions.png)

Note that you also need to be the admin of your tenant to grant consent to your application when you login for the first time. Also note after 2018 your Active Directory may require your application to be multi-tenant. Select "Accounts in any organizational directory" under Authentication panel (where you enabled Device Code) to make your application a multi-tenant app.

#### Enable applications for interactive browser oauth 2 flow
You need to register an application in Azure Active Directory with permissions to login on behalf of a user to use InteractiveBrowserCredential. Follow all the steps above for device code flow to register your application to support logging you into Active Directory and access certain resources. Note the same limitations apply that an admin of your tenant must grant consent to your application before any user account can login.

You may notice in `InteractiveBrowserCredentialBuilder`, a port number is required, and you need to add the redirect URL on this page too:

![interactive redirect uri](./images/interactive-redirecturi.png)

In this case, the port number is 8765.

#### Enable applications for oauth 2 auth code flow
You need the same application registered as in [Enable applications for interactive browser oauth 2 flow](#enable-applications-for-interactive-browser-oauth-2-flow), except that the redirect URL must be an API endpoint on your web application where the auth code must be handled as a query parameter.

#### Enable applications for shared token cache credential
You will need to have Visual Studio 2019 installed. Login to Visual Studio with your org ID or live ID and you are ready to use shared token cache credential.

If you see an error "MSAL V3 Deserialization failed", try clearing the cache in `C:\Users\{username}\AppData\.IdentityService`.

## Key concepts
### Credentials

A credential is a class which contains or can obtain the data needed for a service client to authenticate requests. Service clients across Azure SDK accept credentials when they are constructed and use those credentials to authenticate requests to the service.Azure Identity offers a variety of credential classes in the `azure-identity` package capable of acquiring an AAD token. All of these credential classes are implementations of the `TokenCredential` abstract class in [Azure Core][azure_core_library], and can be used by any service client which can be constructed with a `TokenCredential`.


The credential types in Azure Identity differ in the types of AAD identities they can authenticate and how they are configured:

|credential class|identity|configuration
|-|-|-
|`DefaultAzureCredential`|service principal or managed identity|none for managed identity; [environment variables](#environment-variables) for service principal
|`ManagedIdentityCredential`|managed identity|`ManagedIdentityCredentialBuilder`
|`EnvironmentCredential`|service principal|[environment variables](#environment-variables)
|`ClientSecretCredential`|service principal|`ClientSecretCredentialBuilder`
|`ClientCertificateCredential`|service principal|`ClientCertificateCredentialBuilder`
|`DeviceCodeCredential`|user account|`DeviceCodeCredentialBuilder`
|`InteractiveBrowserCredential`|user account|`InteractiveBrowserCredentialBuilder`
|`UsernamePasswordCredential`|user account|`UsernamePasswordCredentialBuilder`

Credentials can be chained together to be tried in turn until one succeeds using the `ChainedTokenCredential`; see [chaining credentials](#chaining-credentials) for details.

## DefaultAzureCredential
`DefaultAzureCredential` is appropriate for most scenarios where the application is intended to run in the Azure Cloud. This is because the `DefaultAzureCredential` determines the appropriate credential type based of the environment it is executing in. It supports authenticating both as a service principal or managed identity, and can be configured so that it will work both in a local development environment or when deployed to the cloud.

The `DefaultAzureCredential` will first attempt to authenticate using credentials provided in the environment. In a development environment you can authenticate as a service principal with the `DefaultAzureCredential` by providing configuration in environment variables as described in the next section.

If the environment configuration is not present or incomplete, the `DefaultAzureCredential` will then determine if a managed identity is available in the current environment.  Authenticating as a managed identity requires no configuration, but does
require platform support. See the
[managed identity documentation](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/services-support-managed-identities) for more details on this.

## Environment variables

`DefaultAzureCredential` and `EnvironmentCredential` are configured for service
principal authentication with these environment variables:

|variable name|value
|-|-
|`AZURE_CLIENT_ID`|service principal's app id
|`AZURE_TENANT_ID`|id of the principal's Azure Active Directory tenant
|`AZURE_CLIENT_SECRET`|one of the service principal's client secrets

## Examples

### Authenticating with `DefaultAzureCredential`
This example demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`. There's also [a compilable sample](../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentitySamples.java) to create a Key Vault secret client you can copy-paste.

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L30-L42 -->
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

When executing this in a development machine you need to first [configure the environment](#environment-variables) setting the variables `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` and `AZURE_CLIENT_SECRET` to the appropriate values for your service principal.

### Authenticating a service principal with a client secret
This example demonstrates authenticating the `KeyClient` from the [azure-security-keyvault-keys][keys_client_library] client library using the `ClientSecretCredential`. There's also [a compilable sample](../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentitySamples.java) to create a Key Vault secret client you can copy-paste.

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L44-L59 -->
```java
/**
 *  Authenticate with client secret.
 */
public void createClientSecretCredential() {
    ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
        .clientId("<YOUR_CLIENT_ID>")
        .clientSecret("<YOUR_CLIENT_SECRET>")
        .tenantId("<YOUR_TENANT_ID>")
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(clientSecretCredential)
        .buildClient();
}
```

### Authenticating a user account with device code flow
This example demonstrates authenticating the `KeyClient` from the [azure-security-keyvault-keys][keys_client_library] client library using the `DeviceCodeCredential` on an IoT device. There's also [a compilable sample](../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentitySamples.java) to create a Key Vault secret client you can copy-paste. 

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L61-L77 -->
```java
/**
 * Authenticate with device code credential.
 */
public void createDeviceCodeCredential() {
    DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
        .challengeConsumer(challenge -> {
            // lets user know of the challenge
            System.out.println(challenge.getMessage());
        })
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(deviceCodeCredential)
        .buildClient();
}
```

### Authenticating a user account with username and password
This example demonstrates authenticating the `KeyClient` from the [azure-security-keyvault-keys][keys_client_library] client library using the `UsernamePasswordCredential`. The user must **not** have Multi-factor auth turned on. There's also [a compilable sample](../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentitySamples.java) to create a Key Vault secret client you can copy-paste. 

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L79-L94 -->
```java
/**
 * Authenticate with username, password.
 */
public void createUserNamePasswordCredential() {
    UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
        .clientId("<YOUR_CLIENT_ID>")
        .username("<YOUR_USERNAME>")
        .password("<YOUR_PASSWORD>")
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(usernamePasswordCredential)
        .buildClient();
}
```

### Authenticating a user account with auth code flow
This example demonstrates authenticating the `KeyClient` from the [azure-security-keyvault-keys][keys_client_library] client library using the `AuthorizationCodeCredential` on a web application.

First, prompt the user to login at the URL documented at [Microsoft identity platform and OAuth 2.0 authorization code flow](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-authorization-code). You will need the client id, tenant id, redirect URL, and the scopes your application plans to access.

Then create an API at the redirect URL with the following code to access the Key Vault service.

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L96-L110 -->
```java
/**
 * Authenticate with authorization code.
 */
public void createAuthCodeCredential() {
    AuthorizationCodeCredential authCodeCredential = new AuthorizationCodeCredentialBuilder()
        .clientId("<YOUR CLIENT ID>")
        .authorizationCode("<AUTH CODE FROM QUERY PARAMETERS")
        .redirectUrl("<THE REDIRECT URL>")
        .build();
    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(authCodeCredential)
        .buildClient();
}
```

### Chaining credentials
The `ChainedTokenCredential` class provides the ability to link together multiple credential instances to be tried sequentially when authenticating. The following example demonstrates creating a credential which will attempt to authenticate using managed identity, and fall back to certificate authentication if a managed identity is unavailable in the current environment. This example authenticates an `EventHubClient` from the [azure-eventhubs][eventhubs_client_library] client library using the `ChainedTokenCredential`. There's also [a compilable sample](../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentitySamples.java) to create a Key Vault secret client you can copy-paste. 

<!-- embedme ../../keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/IdentityReadmeSamples.java#L112-L138 -->
```java
/**
 * Authenticate with chained credentials.
 */
public void createChainedCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .clientId("<YOUR_CLIENT_ID>")
        .build();

    ClientSecretCredential secondServicePrincipal = new ClientSecretCredentialBuilder()
        .clientId("<YOUR_CLIENT_ID>")
        .clientSecret("<YOUR_CLIENT_SECRET>")
        .tenantId("<YOUR_TENANT_ID>")
        .build();

    // when an access token is requested, the chain will try each
    // credential in order, stopping when one provides a token
    ChainedTokenCredential credentialChain = new ChainedTokenCredentialBuilder()
        .addLast(managedIdentityCredential)
        .addLast(secondServicePrincipal)
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(credentialChain)
        .buildClient();
}
```

## Troubleshooting
Credentials raise exceptions when they fail to authenticate. `ClientAuthenticationException` has a `message` attribute which
describes why authentication failed. When raised by `ChainedTokenCredential`, the message collects error messages from each credential in the chain.

## Next steps
Currently the following client libraries support authenticating with `TokenCredential` and the Azure Identity library.  You can learn more about their use, and find additional documentation on use of these client libraries along samples with can be found in the links below.

- [azure-eventhubs][eventhubs_client_library]
- [azure-security-keyvault-keys][keys_client_library]
- [azure-security-keyvault-secrets][secrets_client_library]

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[source]: ./
[aad_doc]: https://docs.microsoft.com/azure/active-directory/
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[keys_client_library]: ../../keyvault/azure-security-keyvault-keys
[secrets_client_library]: ../../keyvault/azure-security-keyvault-secrets
[eventhubs_client_library]: ../../eventhubs/azure-messaging-eventhubs
[azure_core_library]: ../../core
[javadoc]: http://azure.github.io/azure-sdk-for-java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fidentity%2Fazure-identity%2FREADME.png)
