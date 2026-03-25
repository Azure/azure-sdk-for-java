# Azure PlanetaryComputer client library for Java

Azure PlanetaryComputer client library for Java.

This package contains Microsoft Azure PlanetaryComputer client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-analytics-planetarycomputer;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-planetarycomputer</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

### STAC (SpatioTemporal Asset Catalog)

The PlanetaryComputer service is built around the [STAC specification](https://stacspec.org/), which provides a common structure for describing geospatial information. Key STAC concepts include:

- **Collection** — A grouping of related geospatial assets (e.g., satellite imagery, land cover data). Each collection has metadata including spatial and temporal extents, license information, and provider details.
- **Item** — A GeoJSON Feature representing a single spatiotemporal asset within a collection. Items contain geometry, temporal metadata, and references to data assets.
- **Asset** — A file or resource associated with a collection or item, such as a GeoTIFF image, thumbnail, or metadata file.
- **Mosaic** — A composite view of multiple items that can be queried as a single tiled layer for map visualization.
- **Queryable** — A field definition that describes searchable properties within a collection.

### Service Clients

The SDK provides four client pairs (synchronous and asynchronous) built from a single `PlanetaryComputerProClientBuilder`:

| Client | Purpose |
|--------|---------|
| `StacClient` / `StacAsyncClient` | Manage STAC catalog resources — collections, items, mosaics, render options, queryables, and search |
| `DataClient` / `DataAsyncClient` | Tiler operations — map tiles, previews, statistics, static images, and legends |
| `IngestionClient` / `IngestionAsyncClient` | Manage data ingestion — definitions, runs, sources, and operations |
| `SharedAccessSignatureClient` / `SharedAccessSignatureAsyncClient` | Generate and manage SAS tokens for accessing underlying Azure Blob Storage |

### Long-Running Operations

Some operations such as creating collections, deleting collections, and creating items are long-running operations (LROs). These methods return a `SyncPoller<Operation, Void>` (or `PollerFlux` for async) that can be used to track the operation status.

## Examples

The following sections provide code snippets covering common PlanetaryComputer tasks:

- [Create a client](#create-a-client)
- [Get a STAC collection](#get-a-stac-collection)
- [Search for STAC items](#search-for-stac-items)
- [Generate a SAS token](#generate-a-sas-token)

### Create a client

All clients are created using `PlanetaryComputerProClientBuilder`. The builder requires an endpoint and a `TokenCredential` for authentication.

```java readme-sample-createStacClient
StacClient stacClient = new PlanetaryComputerProClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<your-endpoint>")
    .buildStacClient();
```

To create an asynchronous client, call `buildStacAsyncClient()` instead:

```java readme-sample-createStacAsyncClient
StacAsyncClient stacAsyncClient = new PlanetaryComputerProClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<your-endpoint>")
    .buildStacAsyncClient();
```

### Get a STAC collection

Retrieve a specific collection by its identifier:

```java readme-sample-getCollection
StacCollection collection = stacClient.getCollection("naip-atl", null, null);
System.out.printf("Collection ID: %s, Description: %s%n",
    collection.getId(), collection.getDescription());
```

### Search for STAC items

Use `StacSearchParameters` to search across collections with spatial and temporal filters:

```java readme-sample-searchItems
StacItemCollection results = stacClient.search(
    new StacSearchParameters()
        .setCollections(Arrays.asList("naip-atl"))
        .setDatetime("2021-01-01T00:00:00Z/2022-12-31T00:00:00Z")
        .setLimit(10),
    null, null);
System.out.printf("Found %d items%n", results.getFeatures().size());
```

### Generate a SAS token

Generate a SAS token to access data assets in Azure Blob Storage:

```java readme-sample-getToken
SharedAccessSignatureClient sasClient = new PlanetaryComputerProClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<your-endpoint>")
    .buildSharedAccessSignatureClient();
SharedAccessSignatureToken token = sasClient.getToken("naip-atl", null);
```

### Building the package

To compile the package into a standalone jar, use the following command from the root of the repository:

```bash
mvn clean package -f sdk/planetarycomputer/azure-analytics-planetarycomputer/pom.xml
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
