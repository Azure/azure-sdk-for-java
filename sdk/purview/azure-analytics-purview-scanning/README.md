# Azure Purview Scanning client library for Java

Azure Purview Scanning is a fully managed cloud service whose users can scan your data into your data estate (also known as your **catalog**). Scanning is a process by which the catalog connects directly to a data source on a user-specified schedule.

- Scan your data into your catalog
- Examine your data
- Extract schemas from your data

**Please rely heavily on the [service's documentation][product_documentation] and our [data-plane client docs][protocol_method] to use this library**

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing Azure Purview account.

For more information about creating the account see [here][create_azure_purview_account].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-analytics-purview-scanning;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-analytics-purview-scanning</artifactId>
  <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})


### Authenticate the client
In order to interact with the Azure Purview service, your client must present an Azure Active Directory bearer token to the service.

After setup, you can choose which type of [credential][azure_identity_credential_type] from `azure-identity` to use.
We recommend using [DefaultAzureCredential][identity_dac], configured through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

You can find more ways to authenticate with [azure-identity][azure_identity].


#### Create SystemScanRulesetsClient with Azure Active Directory Credential

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
```java readme-sample-createSystemScanRulesetsClient
SystemScanRulesetsClient client = new PurviewScanningClientBuilder()
    .endpoint(System.getenv("SCANNING_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildSystemScanRulesetsClient();
```

## Key concepts

## Examples
More examples can be found in [samples][samples_code].

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
[samples]: src/samples/java/com/azure/analytics/purview/scanning
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-scanning/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-scanning/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java
[product_documentation]: https://azure.microsoft.com/services/purview/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-analytics-purview-scanning
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/purview/azure-analytics-purview-scanning/src/samples/README.md
[protocol_method]: https://github.com/Azure/azure-sdk-for-java/wiki/Protocol-Methods
[create_azure_purview_account]: https://learn.microsoft.com/azure/purview/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[cla]: https://cla.opensource.microsoft.com/
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[identity_dac]: https://aka.ms/azsdk/java/identity/credential-chains#defaultazurecredential-overview

