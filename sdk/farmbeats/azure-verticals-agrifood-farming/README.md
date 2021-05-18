# Azure FarmBeats client library for Java

Azure FarmBeats is a business-to-business offering available in Azure Marketplace. It enables aggregation of agriculture data sets across providers. Azure FarmBeats enables you to build artificial intelligence (AI) or machine learning (ML) models based on fused data sets. By using Azure FarmBeats, agriculture businesses can focus on core value-adds instead of the undifferentiated heavy lifting of data engineering.

**Please rely heavily on the javadocs and our [Low-Level client docs][low_level_client] to use this library**

[Source code][source_code] | [Package (Maven)][package] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- A running instance of Azure FarmBeats.

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-verticals-agrifood-farming;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-verticals-agrifood-farming</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

#### Using Azure Active Directory

In order to interact with the Azure FarmBeats service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create FarmersBaseClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.2.5</version>
</dependency>
```

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

##### Example
<!-- embedme ./src/samples/java/com/azure/verticals/agrifood/farming/ReadmeSamples.java#L20-L23 -->
```java
FarmersBaseClient client = new FarmBeatsClientBuilder()
        .endpoint("https://<farmbeats resource name>.farmbeats-dogfood.azure.net")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildFarmersBaseClient();
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
[samples]: src/samples/java/com/azure/verticals/agrifood/farming
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/farmbeats/azure-verticals-agrifood-farming/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/farmbeats/azure-verticals-agrifood-farming/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[product_documentation]: https://docs.microsoft.com/azure/industry/agriculture/overview-azure-farmbeats
[ledger_base_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/farmbeats/azure-verticals-agrifood-farming/src/main/java/com/azure/verticals/agrifood/farming/LedgerBaseClient.java
[azure_portal]: https://portal.azure.com
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://mvnrepository.com/artifact/com.azure/azure-verticals-agrifood-farming
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/farmbeats/azure-verticals-agrifood-farming/src/samples/README.md


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ffarmbeats%2Fazure-verticals-agrifood-farming%2FREADME.png)
