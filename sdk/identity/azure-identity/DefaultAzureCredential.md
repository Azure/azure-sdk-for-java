## Default Azure Credential

The DefaultAzureCredential is appropriate for most scenarios where the application ultimately runs in the Azure Cloud.
DefaultAzureCredential combines credentials that are commonly used to authenticate when deployed, with credentials that
are used to authenticate in a development environment. The DefaultAzureCredential will attempt to authenticate via the
following mechanisms in order.

![DefaultAzureCredential authentication flow](https://github.com/Azure/azure-sdk-for-java/raw/main/sdk/identity/azure-identity/images/defaultazurecredential.png)


* Environment - The DefaultAzureCredential will read account information specified via environment variables and use it to authenticate.
* Managed Identity - If the application deploys to an Azure host with Managed Identity enabled, the DefaultAzureCredential will authenticate with that account.
* IntelliJ - If you've authenticated via Azure Toolkit for IntelliJ, the DefaultAzureCredential will authenticate with that account.
* Visual Studio Code - If you've authenticated via the Visual Studio Code Azure Account plugin, the DefaultAzureCredential will authenticate with that account.
* Azure CLI - If you've authenticated an account via the Azure CLI az login command, the DefaultAzureCredential will authenticate with that account.

## Environment Configuration Based Authentication with Default Azure Credential

### Configuration

You can configure DefaultAzureCredential with environment variables. The `DefaultAzureCredential` will utilize the environment variables through `EnvionmentCredential` in the chain of credentials and attempt to authenticate.

Each type of authentication requires values for specific environment variables as described below:

**SERVICE PRINCIPAL WITH SECRET**

Variable name | Value |
--- | --- |
AZURE_CLIENT_ID |	ID of an Azure Active Directory application.
AZURE_TENANT_ID	| ID of the application's Azure Active Directory tenant.
AZURE_CLIENT_SECRET |	One of the application's client secrets.

**SERVICE PRINCIPAL WITH CERTIFICATE**
Variable name | Value |
--- | --- |
AZURE_CLIENT_ID | ID of an Azure Active Directory application.
AZURE_TENANT_ID |	ID of the application's Azure Active Directory tenant.
AZURE_CLIENT_CERTIFICATE_PATH	| Path to a PEM-encoded certificate file including private key (without password protection).

**USERNAME AND PASSWORD**
Variable name | Value |
--- | --- |
AZURE_CLIENT_ID |	ID of an Azure Active Directory application.
AZURE_USERNAME | A username (usually an email address).
AZURE_PASSWORD | The associated password for the given username.

### Code Sample
The following example demonstrates authenticating the SecretClient from the `azure-security-keyvault-secrets` client library using the DefaultAzureCredential. The `DefaultAzureCredential` will scan for the configured environment variables and if they are found then authentication will be attempted using `EnvironmentCredential` from the chain of credentials.

```java
// Azure SDK client builders accept the credential as a parameter.
SecretClient client = new SecretClientBuilder()
  .vaultUrl("https://<your Key Vault name>.vault.azure.net")
  .credential(new DefaultAzureCredentialBuilder().build())
  .buildClient();
```



## Managed Identity Authentication with Default Azure Credential

### Configuration
The Managed Identity authenticates the managed identity (system or user assigned) of an Azure resource. So, if the application is running inside an Azure resource that supports Managed Identity through `IDENTITY/MSI, IMDS` endpoints, or both, then this credential will get your application authenticated, and offers a great secretless authentication experience.

Currently Azure Identity SDK supports [Managed Identity Authentication]((https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview)) the following Azure Services:
Azure Service | Managed Identity Configuration
--- | --- |
[Azure Virtual Machines](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm)
[Azure App Service](https://docs.microsoft.com/azure/app-service/overview-managed-identity?tabs=java) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/app-service/overview-managed-identity?tabs=java)
[Azure Kubernetes Service](https://docs.microsoft.com/azure/aks/use-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/azure/aks/use-managed-identity)
[Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/msi-authorization) |  |
[Azure Arc](https://docs.microsoft.com/azure/azure-arc/servers/managed-identity-authentication) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/azure-arc/servers/security-overview#using-a-managed-identity-with-arc-enabled-servers)
[Azure Service Fabric](https://docs.microsoft.com/azure/service-fabric/concepts-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/service-fabric/configure-existing-cluster-enable-managed-identity-token-service)


### Code Sample
This examples demonstrates authenticating the `SecretClient` from the [azure-security-keyvault-secrets][secrets_client_library] client library using the `DefaultAzureCredential`. Ensure that you have enabled Managed Identity on your Azure resource as per Instructions above.

```java
/**
 * Authenticate with a User Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
        .managedIdentityClientId("<USER ASSIGNED MANAGED IDENTITY CLIENT ID>") // only required for user assigned
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultAzureCredential)
        .buildClient();
}
```

```java
/**
 * Authenticate with a System Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(defaultAzureCredential)
        .buildClient();
}
```


## IntelliJ Authentication with Default Azure Credential

The `DefaultAzureCredential` utilizes `IntelliJCredential` from the credential chain and authenticates in a development environment with the account in Azure Toolkit for IntelliJ. It uses the logged in user information on the IntelliJ IDE and uses it to authenticate the application against Azure Active Directory.

### Configuration

Sign in Azure Toolkit for IntelliJ through the steps outlined below:

* In your IntelliJ window, open File > Settings > Plugins.
* Search for “Azure Toolkit for IntelliJ” in the marketplace. Install and restart IDE.
* Find the new menu item Tools > Azure > Azure Sign In…
* Device Login will help you log in as a user account. Follow the instructions to log in on the login.microsoftonline.com website with the device code. IntelliJ will prompt you to select your subscriptions. Select the subscription with the resources that you want to access.

On Windows environment, you'll also need the KeePass database path to read IntelliJ credentials. You can find the path in IntelliJ settings under File > Settings > Appearance & Behavior > System Settings > Passwords. Note down the location of the KeePassDatabase path.



### Code Sample
The following example demonstrates authenticating the SecretClient from the [azure-security-keyvault-secrets]() client library using the `DefaultAzureCredential` on a workstation where IntelliJ IDEA is installed, and the user has signed in with an Azure account.
```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
  // KeePass configuration isrequired only for Windows. No configuration needed for Linux / Mac.
  .intelliJkeePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
  .build();

// Azure SDK client builders accept the credential as a parameter
SecretClient client = new SecretClientBuilder()
  .vaultUrl("https://<your Key Vault name>.vault.azure.net")
  .credential(defaultAzureCredential)
  .buildClient();
```

## VS Code Authentication with Default Azure Credential

The Visual Studio Code credential enables authentication in development environments where VS Code is installed with the [VS Code Azure Account extension](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth#intellij-credential). It uses the logged-in user information in the VS Code IDE and uses it to authenticate the application against Azure Active Directory.

### Configuration

Sign in Visual Studio Code Azure Account Extension

The Visual Studio Code authentication is handled by an integration with the [Azure Account extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode.azure-account). To use this form of authentication, install the Azure Account extension, then use View > Command Palette to execute the Azure: Sign In command. This command opens a browser window and displays a page that allows you to sign in to Azure. After you've completed the login process, you can close the browser as directed. Running your application (either in the debugger or anywhere on the development machine) will use the credential from your sign-in.


### Code Sample
The following example demonstrates authenticating the SecretClient from the azure-security-keyvault-secrets client library using the `DefaultAzureCredential` on a workstation where Visual Studio Code is installed, and the user has signed in with an Azure account.

```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Azure SDK client builders accept the credential as a parameter.
SecretClient client = new SecretClientBuilder()
  .vaultUrl("https://<your Key Vault name>.vault.azure.net")
  .credential(defaultAzureCredential)
  .buildClient();
```
