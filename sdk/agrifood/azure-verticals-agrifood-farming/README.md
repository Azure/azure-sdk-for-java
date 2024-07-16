# Azure FarmBeats client library for Java

FarmBeats is a B2B PaaS offering from Microsoft that makes it easy for AgriFood companies to build intelligent digital agriculture solutions on Azure. FarmBeats allows users to acquire, aggregate, and process agricultural data from various sources (farm equipment, weather, satellite) without the need to invest in deep data engineering resources.  Customers can build SaaS solutions on top of FarmBeats and leverage first class support for model building to generate insights at scale.

Use FarmBeats client library for Python to do the following. 

- Create & update parties, farms, fields, seasonal fields and boundaries.
- Ingest satellite and weather data for areas of interest.
- Ingest farm operations data covering tilling, planting, harvesting and application of farm inputs.

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
  <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

#### Using Azure Active Directory

In order to interact with the Azure FarmBeats service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.0</version>
</dependency>
```

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

##### Example - Create Parties Client

```java readme-sample-createPartiesClient
String endpoint = "https://<farmbeats-endpoint>.farmbeats.azure.net";

// Create Parties Client
PartiesClientBuilder partiesBuilder = new PartiesClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build());
PartiesAsyncClient partiesClient = partiesBuilder.buildAsyncClient();

```

##### Example - Create Boundaries Client
```java readme-sample-createBoundariesClient
// Create Boundaries Client
BoundariesClientBuilder boundariesBuilder = new BoundariesClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build());
BoundariesAsyncClient boundariesClient = boundariesBuilder.buildAsyncClient();
```

##### Example - Create Scenes Client
```java readme-sample-createScenesClient
// Create Scenes Client
ScenesClientBuilder scenesBuilder = new ScenesClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build());
ScenesAsyncClient scenesClient = scenesBuilder.buildAsyncClient();
```

## Key concepts

Basic understanding of below terms will help to get started with FarmBeats client library.

### [Farm Hierarchy][farm_hierarchy]
Farm hierarchy is a collection of below entities.
- Party - is the custodian of all the agronomic data.
- Farm - is a logical collection of fields and/or seasonal fields. They do not have any area associated with them.
- Field - is a multi-polygon area. This is expected to be stable across seasons.
- Seasonal field - is a multi-polygon area. To define a seasonal boundary we need the details of area (boundary), time (season) and crop. New seasonal fields are expected to be created for every growing season.
- Boundary - is the actual multi-polygon area expressed as a geometry (in geojson). It is normally associated with a field or a seasonal field. Satellite, weather and farm operations data is linked to a boundary.
- Cascade delete - Agronomic data is stored hierarchically with party as the root. The hierarchy includes Party -> Farms -> Fields -> Seasonal Fields -> Boundaries -> Associated data (satellite, weather, farm operations). Cascade delete refers to the process of deleting any node and its subtree. 

#### Example

```java readme-sample-createFarmHierarchy
// Create Party
JSONObject object = new JSONObject().appendField("name", "party1");
BinaryData party = BinaryData.fromObject(object);
partiesClient.createOrUpdateWithResponse("contoso-party", party, null).block();

// Get Party
Response<BinaryData> response = partiesClient.getWithResponse("contoso-party", new RequestOptions()).block();
System.out.println(response.getValue());

// Create Boundary
BinaryData boundary = BinaryData.fromString("{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[73.70457172393799,20.545385304358106],[73.70457172393799,20.545385304358106],[73.70448589324951,20.542411534243367],[73.70877742767334,20.541688176010233],[73.71023654937744,20.545083911372505],[73.70663166046143,20.546992723579137],[73.70457172393799,20.545385304358106]]]},\"name\":\"string\",\"description\":\"string\"}");
response = boundariesClient.createOrUpdateWithResponse("contoso-party", "contoso-boundary", boundary, null).block();
System.out.println(response.getValue());
```

### [Scenes][scenes]
Scenes refers to images normally ingested using satellite APIs. This includes raw bands and derived bands (Ex: NDVI). Scenes may also include spatial outputs of an inference or AI/ML model (Ex: LAI).

#### Example

```java readme-sample-ingestSatelliteData
// Trigger Satellite job and wait for completion
BinaryData satelliteJob = BinaryData.fromString("{\"boundaryId\":\"contoso-boundary\",\"endDateTime\":\"2022-02-01T00:00:00Z\",\"partyId\":\"contoso-party\",\"source\":\"Sentinel_2_L2A\",\"startDateTime\":\"2022-01-01T00:00:00Z\",\"provider\":\"Microsoft\",\"data\":{\"imageNames\":[\"NDVI\"],\"imageFormats\":[\"TIF\"],\"imageResolutions\":[10]},\"name\":\"string\",\"description\":\"string\"}");
scenesClient.beginCreateSatelliteDataIngestionJob("contoso-job-46856", satelliteJob, null).getSyncPoller().waitForCompletion();
System.out.println(scenesClient.getSatelliteDataIngestionJobDetailsWithResponse("contoso-job-46856", null).block().getValue());

// Iterate through ingested scenes
Iterable<BinaryData> scenes = scenesClient.list("Microsoft", "contoso-party", "contoso-boundary", "Sentinel_2_L2A", null).toIterable();
scenes.forEach(scene -> System.out.println(scene));
```

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
For more extensive documentation please check our [Product Documentation][product_documentation].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, view [Microsoft's CLA](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[samples]: src/samples/java/com/azure/verticals/agrifood/farming
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agrifood/azure-verticals-agrifood-farming/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agrifood/azure-verticals-agrifood-farming/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[product_documentation]: https://aka.ms/FarmBeatsProductDocumentationPaaS
[azure_portal]: https://portal.azure.com
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-verticals-agrifood-farming
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/agrifood/azure-verticals-agrifood-farming/src/samples/README.md
[farm_hierarchy]: https://aka.ms/FarmBeatsFarmHierarchyDocs
[farm_operations_docs]: https://aka.ms/FarmBeatsFarmOperationsDocumentation
[scenes]: https://aka.ms/FarmBeatsSatellitePaaSDocumentation
[install_farmbeats]: https://aka.ms/FarmBeatsInstallDocumentationPaaS
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fagrifood%2Fazure-verticals-agrifood-farming%2FREADME.png)
