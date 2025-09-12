# Azure Purview Administration client library for Java

Azure Purview data plane administration. It supports data plane operations. It can manage account, collections, keys, resource set rule, metadata policy, metadata roles.

**Please rely heavily on the [service's documentation][product_documentation] and [data-plane documentation][protocol_method] to use this library**

[Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing Azure Purview administration.

For more information about creating the account see [here][create_azure_purview_account].

Some API in administration requires permissions for the user or the service principal authenticated with the client.
For more information about permissions, see [here][azure_purview_permissions].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-analytics-purview-administration;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-analytics-purview-administration</artifactId>
  <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Azure Purview service, your client must present an Azure Active Directory bearer token to the service.

After setup, you can choose which type of [credential][azure_identity_credential_type] from `azure-identity` to use.
We recommend using [DefaultAzureCredential][identity_dac], configured through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

You can find more ways to authenticate with [azure-identity][azure_identity].

#### Create AccountsClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.15.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

##### Example

```java readme-sample-createAccountsClient
AccountsClient client = new AccountsClientBuilder()
    .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts

## Examples

```java readme-sample-getAccountProperties
AccountsClient client = new AccountsClientBuilder()
    .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
BinaryData response = client.getAccountPropertiesWithResponse(null).getValue();
```

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java
[product_documentation]: https://azure.microsoft.com/services/purview/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-analytics-purview-administration
[protocol_method]: https://github.com/Azure/azure-sdk-for-java/wiki/Protocol-Methods
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_azure_purview_account]: https://learn.microsoft.com/azure/purview/create-catalog-portal
[azure_purview_permissions]: https://learn.microsoft.com/azure/purview/catalog-permissions
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[identity_dac]: https://aka.ms/azsdk/java/identity/credential-chains#defaultazurecredential-overview
