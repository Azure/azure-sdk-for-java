# Azure FarmBeats client library for Java

FarmBeats is a B2B PaaS offering from Microsoft that makes it easy for AgriFood companies to build intelligent digital agriculture solutions on Azure. FarmBeats allows users to acquire, aggregate, and process agricultural data from various sources (farm equipment, weather, satellite) without the need to invest in deep data engineering resources.  Customers can build SaaS solutions on top of FarmBeats and leverage first class support for model building to generate insights at scale.

Use FarmBeats client library for Python to do the following. 

- Create & update farmers, farms, fields, seasonal fields and boundaries.
- Ingest satellite and weather data for areas of interest.
- Ingest farm operations data covering tilling, planting, harvesting and application of farm inputs.

**Please rely heavily on the javadocs and our [Low-Level client docs][low_level_client] to use this library**

[Source code][source_code] | [Package (Maven)][package] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- AgriFood (FarmBeats) resource - [Install FarmBeats][install_farmbeats]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-verticals-agrifood-farming;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-verticals-agrifood-farming</artifactId>
  <version>1.0.0-beta.2</version>
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

Basic understanding of below terms will help to get started with FarmBeats client library.

### [Farm Hierarchy][farm_hierarchy]
Farm hierarchy is a collection of below entities.
- Farmer - is the custodian of all the agronomic data.
- Farm - is a logical collection of fields and/or seasonal fields. They do not have any area associated with them.
- Field - is a multi-polygon area. This is expected to be stable across seasons.
- Seasonal field - is a multi-polygon area. To define a seasonal boundary we need the details of area (boundary), time (season) and crop. New seasonal fields are expected to be created for every growing season.
- Boundary - is the actual multi-polygon area expressed as a geometry (in geojson). It is normally associated with a field or a seasonal field. Satellite, weather and farm operations data is linked to a boundary.
- Cascade delete - Agronomic data is stored hierarchically with farmer as the root. The hierarchy includes Farmer -> Farms -> Fields -> Seasonal Fields -> Boundaries -> Associated data (satellite, weather, farm operations). Cascade delete refers to the process of deleting any node and its subtree. 

### [Scenes][scenes]
Scenes refers to images normally ingested using satellite APIs. This includes raw bands and derived bands (Ex: NDVI). Scenes may also include spatial outputs of an inference or AI/ML model (Ex: LAI).

### [Farm Operations][farm_operations_docs]
Fam operations includes details pertaining to tilling, planting, application of pesticides & nutrients, and harvesting. This can either be manually pushed into FarmBeats using APIs or the same information can be pulled from farm equipment service providers like John Deere. 

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
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agrifood/azure-verticals-agrifood-farming/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agrifood/azure-verticals-agrifood-farming/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[product_documentation]: https://aka.ms/FarmBeatsProductDocumentationPaaS
[azure_portal]: https://portal.azure.com
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://mvnrepository.com/artifact/com.azure/azure-verticals-agrifood-farming
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/agrifood/azure-verticals-agrifood-farming/src/samples/README.md
[farm_hierarchy]: https://aka.ms/FarmBeatsFarmHierarchyDocs
[farm_operations_docs]: https://aka.ms/FarmBeatsFarmOperationsDocumentation
[install_farmbeats]: https://aka.ms/FarmBeatsInstallDocumentationPaaS

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fagrifood%2Fazure-verticals-agrifood-farming%2FREADME.png)
