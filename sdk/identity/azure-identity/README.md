# Azure Identity client library for Java
Azure Identity simplifies authentication across the Azure SDK.
It supports token authentication using an Azure Active Directory
[service principal](https://docs.microsoft.com/en-us/cli/azure/create-an-azure-service-principal-azure-cli)
or
[managed identity](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview).

# Getting started
### Adding the package to your project

Maven dependency for Azure Secret Client library. Add it to your project's pom file.
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.0-preview.1</version>
</dependency>
```

## Prerequisites
- an Azure subscription
  - if you don't have one, you can sign up for a
  [free account](https://azure.microsoft.com/free/)
- Java Development Kit (JDK) with version 8 or above


# Key concepts
## Credentials
Azure Identity offers a variety of credential classes in the `credential`
package. These are accepted by Azure SDK data plane clients. Each client
library documents its Azure Identity integration in its README and samples.
Azure SDK resource management libraries (which have `mgmt` in their names)
do not accept these credentials.

Credentials differ mostly in configuration:

|credential class|identity|configuration
|-|-|-
|`DefaultAzureCredential`|service principal or managed identity|none for managed identity; [environment variables](#environment-variables) for service principal
|`ManagedIdentityCredential`|managed identity|none
|`EnvironmentCredential`|service principal|[environment variables](#environment-variables)
|`ClientSecretCredential`|service principal|constructor parameters
|`ClientCertificateCredential`|service principal|constructor parameters

Credentials can be chained and tried in turn until one succeeds; see
[chaining credentials](#chaining-credentials) for details.

## DefaultAzureCredential
`DefaultAzureCredential` is appropriate for most scenarios. It supports
authenticating as a service principal or managed identity. To authenticate
as a service principal, provide configuration in environment variables as
described in the next section.

Authenticating as a managed identity requires no configuration, but does
require platform support. See the
[managed identity documentation](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/services-support-managed-identities)
for more information.

## Environment variables

`DefaultAzureCredential` and `EnvironmentCredential` are configured for service
principal authentication with these environment variables:

|variable name|value
|-|-
|`AZURE_CLIENT_ID`|service principal's app id
|`AZURE_TENANT_ID`|id of the principal's Azure Active Directory tenant
|`AZURE_CLIENT_SECRET`|one of the service principal's client secrets

# Examples

## `DefaultAzureCredential`
```java
# The default credential first checks environment variables for configuration as described above.
# If environment configuration is incomplete, it will try managed identity.
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;

DefaultAzureCredential defaultCredential = new DefaultAzureCredential();

// Azure SDK client builders accept the credential as a parameter

SecretClient client = SecretClient.builder()
    .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
    .credential(credential)
    .build();
```

## Authenticating as a service principal:
```java
# using a client secret
import com.azure.identity.credential.ClientSecretCredential;
import com.azure.identity.credential.EnvironmentCredential;
import com.azure.security.keyvault.secrets.SecretClient;


// authenticate with client secret,
ClientSecretCredential clientSecretCredential = new ClientSecretCredential()
	    .clientId("<YOUR_CLIENT_ID>")
	    .clientSecret("<YOUR_CLIENT_SECRET>")
	    .tenantId("<YOUR_TENANT_ID>");

// using environment variables
// (see "Environment variables" above for variable names and expected values)
EnvironmentCredential credential = new EnvironmentCredential();

SecretClient client = SecretClient.builder()
    .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
    .credential(credential)
    .build();
```

## Chaining credentials:
```java
import com.azure.identity.credential.ClientSecretCredential;
import com.azure.security.keyvault.secrets.SecretClient;

ClientSecretcredential firstServicePrincipal = new ClientSecretCredential()
	    .clientId("<YOUR_CLIENT_ID>")
	    .clientSecret("<YOUR_CLIENT_SECRET>")
	    .tenantId("<YOUR_TENANT_ID>");

ClientSecretcredential secondServicePrincipal = new ClientSecretCredential()
	    .clientId("<YOUR_CLIENT_ID>")
	    .clientSecret("<YOUR_CLIENT_SECRET>")
	    .tenantId("<YOUR_TENANT_ID>");

// when an access token is requested, the chain will try each
// credential in order, stopping when one provides a token

ChainedTokenCredential credentialChain = new ChainedTokenCredential()
		.addLast(firstServicePrincipal)
		.addLast(secondServicePrincipal);

// the chain can be used anywhere a credential is required
SecretClient client = SecretClient.builder()
    .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
    .credential(credentialChain)
    .build();
```

# Troubleshooting
## General
Credentials raise exceptions when they fail to authenticate. `ClientAuthenticationException` has a `message` attribute which
describes why authentication failed. When raised by `ChainedTokenCredential`, the message collects error messages from each credential in the chain.


# Next steps
## Provide Feedback
If you encounter bugs or have suggestions, please
[open an issue](https://github.com/Azure/azure-sdk-for-java/issues).


## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

