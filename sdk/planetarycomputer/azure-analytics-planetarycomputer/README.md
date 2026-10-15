# Azure Planetary Computer client library for Java

The Azure Planetary Computer client library provides programmatic access to Microsoft Planetary Computer Pro, a geospatial data management service built on Azure's hyperscale infrastructure. Microsoft Planetary Computer Pro empowers organizations to unlock the full potential of geospatial data by providing foundational capabilities to ingest, manage, search, and distribute geospatial datasets using the SpatioTemporal Asset Catalog (STAC) open specification.

This client library enables developers to interact with GeoCatalog resources, supporting workflows from gigabytes to tens of petabytes of geospatial data.

## Key capabilities

- **STAC Collection Management**: Create, read, update, and delete STAC collections and items to organize your geospatial datasets
- **Collection Configuration**: Configure render options, mosaics, tile settings, and queryables to optimize query performance and visualization
- **Data Visualization**: Generate map tiles (XYZ, TileJSON, WMTS), preview images, crop by GeoJSON or bounding box, extract point values, and compute statistics
- **Mosaic Operations**: Register STAC search-based mosaics for pixel-wise data query and retrieval, generate tiles from multiple items
- **Map Legends**: Retrieve class map legends (categorical) and interval legends (continuous) as JSON or PNG images
- **Data Ingestion**: Set up ingestion sources (Managed Identity or SAS token), define ingestions from STAC catalogs, and monitor ingestion runs
- **STAC API Operations**: Full CRUD operations on items, search with spatial/temporal filters and sorting, retrieve queryable properties, and check API conformance
- **Secure Access**: Generate SAS tokens for collections, sign asset HREFs for secure downloads, and revoke tokens — all secured via Microsoft Entra ID

[Source code][source] | [API reference documentation][docs] | [Product documentation][product_documentation] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- A deployed Microsoft Planetary Computer Pro GeoCatalog resource in your Azure subscription

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-analytics-planetarycomputer;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-planetarycomputer</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

Microsoft Entra ID authentication is required to interact with your GeoCatalog resource.

#### Create the client with Microsoft Entra ID credential

To use `DefaultAzureCredential` or other credential types, add the `azure-identity` dependency:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.15.4</version>
</dependency>
```

`DefaultAzureCredential` will use the values from environment variables `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, and `AZURE_CLIENT_SECRET`, or authenticate via Azure CLI, IntelliJ, or VS Code.

```java readme-sample-createStacClient
StacClient stacClient = new PlanetaryComputerProClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<your-endpoint>")
    .buildStacClient();
```

## Key concepts

### STAC (SpatioTemporal Asset Catalog)

The Planetary Computer Pro service is built around the [STAC specification](https://stacspec.org/), which provides a common structure for describing geospatial information. Key STAC concepts include:

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
| `DataClient` / `DataAsyncClient` | Tiler operations — map tiles, previews, statistics, legends, and point values |
| `IngestionClient` / `IngestionAsyncClient` | Manage data ingestion — definitions, runs, sources, and operations |
| `SharedAccessSignatureClient` / `SharedAccessSignatureAsyncClient` | Generate and manage SAS tokens for accessing underlying Azure Blob Storage |

### Long-Running Operations

Some operations such as creating collections, deleting collections, and creating items are long-running operations (LROs). These methods return a `SyncPoller<Operation, Void>` (or `PollerFlux` for async) that can be used to track the operation status.

## Examples

The following sections provide code snippets covering common Planetary Computer tasks:

- [Create a client](#create-a-client)
- [List STAC collections](#list-stac-collections)
- [Get a STAC collection](#get-a-stac-collection)
- [Search for STAC items](#search-for-stac-items)
- [Get STAC item details](#get-stac-item-details)
- [Create a STAC collection](#create-a-stac-collection)
- [Configure collection visualization](#configure-collection-visualization)
- [Register and render mosaic tiles](#register-and-render-mosaic-tiles)
- [Extract point values](#extract-point-values)
- [Generate map tiles](#generate-map-tiles)
- [Set up ingestion sources](#set-up-ingestion-sources)
- [Data ingestion management](#data-ingestion-management)
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

### List STAC collections

Browse all available STAC collections in your GeoCatalog:

```java readme-sample-listCollections
StacCatalogCollections collections = stacClient.getCollections();
for (StacCollection col : collections.getCollections()) {
    System.out.printf("Collection: %s - %s%n", col.getId(), col.getTitle());
}
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

### Get STAC item details

Retrieve a specific item from a collection:

```java readme-sample-getItem
StacItem item = stacClient.getItem("naip-atl", "ga_m_3308421_se_16_060_20211114");
System.out.printf("Item ID: %s, Collection: %s%n", item.getId(), item.getCollection());
System.out.printf("Assets: %s%n", item.getAssets().keySet());
```

### Create a STAC collection

Create a new collection using a long-running operation:

```java readme-sample-createCollection
String collectionJson = "{"
    + "\"id\": \"my-collection\","
    + "\"type\": \"Collection\","
    + "\"stac_version\": \"1.0.0\","
    + "\"description\": \"My geospatial dataset\","
    + "\"license\": \"proprietary\","
    + "\"extent\": {\"spatial\": {\"bbox\": [[-180, -90, 180, 90]]},"
    + "\"temporal\": {\"interval\": [[\"2020-01-01T00:00:00Z\", null]]}},"
    + "\"links\": []}";

SyncPoller<BinaryData, BinaryData> poller = stacClient.beginCreateCollection(
    BinaryData.fromString(collectionJson), new RequestOptions());
poller.getFinalResult();
```

### Configure collection visualization

Add render options to control how collection data is visualized on map tiles:

```java readme-sample-configureVisualization
RenderOption renderOption = new RenderOption("natural-color", "Natural Color")
    .setType(RenderOptionType.RASTER_TILE)
    .setOptions("assets=image&asset_bidx=image|1,2,3")
    .setMinZoom(6);
RenderOption created = stacClient.createRenderOption("naip-atl", renderOption);
System.out.printf("Created render option: %s%n", created.getId());
```

### Register and render mosaic tiles

Register a search-based mosaic and retrieve tiles:

```java readme-sample-mosaicTiles
String searchBody = "{\"filter\":{\"op\":\"and\",\"args\":["
    + "{\"op\":\"=\",\"args\":[{\"property\":\"collection\"},\"naip-atl\"]},"
    + "{\"op\":\">=\",\"args\":[{\"property\":\"datetime\"},\"2021-01-01T00:00:00Z\"]}"
    + "]},\"filter-lang\":\"cql2-json\"}";

Response<BinaryData> searchResponse = dataClient.registerMosaicsSearchWithResponse(
    BinaryData.fromString(searchBody), new RequestOptions());
String searchId = searchResponse.getValue()
    .toObject(TilerMosaicSearchRegistrationResponse.class).getSearchId();
System.out.printf("Registered mosaic search: %s%n", searchId);
```

### Extract point values

Query pixel values at a specific geographic coordinate:

```java readme-sample-pointValues
RequestOptions options = new RequestOptions();
options.addQueryParam("assets", "image", false);
Response<BinaryData> response = dataClient.getItemPointWithResponse(
    "naip-atl", "ga_m_3308421_se_16_060_20211114", -84.386, 33.676, options);
System.out.printf("Point data: %s%n", response.getValue().toString());
```

### Generate map tiles

Retrieve a map tile for a specific item:

```java readme-sample-mapTile
RequestOptions options = new RequestOptions();
options.addQueryParam("assets", "image", false);
options.addQueryParam("asset_bidx", "image|1,2,3", false);
BinaryData tile = dataClient.getTileWithTmsByFormatWithResponse(
    "naip-atl", "ga_m_3308421_se_16_060_20211114",
    "WebMercatorQuad", 14, 4349, 6564, "png", options).getValue();
System.out.printf("Tile size: %d bytes%n", tile.toBytes().length);
```

### Set up ingestion sources

Configure an ingestion source for importing data into your GeoCatalog:

```java readme-sample-ingestionSource
IngestionSource source = ingestionClient.createSource(
    new ManagedIdentityIngestionSource("source-id",
        new ManagedIdentityConnection("https://storage.blob.core.windows.net/container",
            "managed-identity-object-id")));
System.out.printf("Created ingestion source: %s%n", source.getId());
```

### Data ingestion management

Create an ingestion definition and start a run:

```java readme-sample-ingestion
IngestionDefinition ingestion = new IngestionDefinition();
ingestion.setImportType(IngestionType.STATIC_CATALOG);
ingestion.setDisplayName("My Dataset Ingestion");
ingestion.setSourceCatalogUrl("https://example.com/catalog.json");

IngestionDefinition created = ingestionClient.create("my-collection", ingestion);
IngestionRun run = ingestionClient.createRun("my-collection", created.getId());
System.out.printf("Ingestion run started: %s%n", run.getId());
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

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can be found here: [log levels][log_levels].

### Default HTTP Client

All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

- Get started with the Azure Planetary Computer client library by exploring the [samples][samples].
- Review the [API reference documentation][docs] for detailed information on available operations.
- Learn more about [Microsoft Planetary Computer Pro][product_documentation].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/planetarycomputer/azure-analytics-planetarycomputer/src
[docs]: https://azure.github.io/azure-sdk-for-java/
[product_documentation]: https://learn.microsoft.com/azure/planetary-computer/
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/planetarycomputer/azure-analytics-planetarycomputer/src/samples
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[log_levels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
