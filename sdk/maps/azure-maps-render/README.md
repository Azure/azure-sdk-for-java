# Azure Maps SDK Render client library for Java

Azure Maps SDK Render client library for Java.

This package contains Microsoft Azure SDK for Render Management SDK which contains Azure Maps Render REST APIs. Azure Maps Render retrieves copyrigt information or map/state tiles. For documentation on how to use this package, please see [Azure Maps Render](https://learn.microsoft.com/rest/api/maps/render).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-render;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-render</artifactId>
    <version>2.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Maps Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

There are 3 ways to authenticate the client: Shared key authentication, Microsoft Entra ID authentication, and shared access signature (SAS) authentication.

We recommend using Microsoft Entra ID with [`DefaultAzureCredential`][azure_identity]. This allows you to configure authentication through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java com.azure.maps.render.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a builder
MapsRenderClientBuilder builder = new MapsRenderClientBuilder();
builder.credential(tokenCredential);
builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

// Builds a client
MapsRenderClient client = builder.buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

For SAS-based authentication, please refer to [AccountsListSasSamples.java][https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-resourcemanager-maps/src/samples/java/com/azure/resourcemanager/maps/generated/AccountsListSasSamples.java].

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
### Get Map Tile
```java com.azure.maps.render.sync.get_map_tile
System.out.println("Get Map Tile");
MapTileOptions mapTileOptions = new MapTileOptions();
mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(6));
client.getMapTile(mapTileOptions);
```

### Get Map Tileset
```java com.azure.maps.render.sync.get_map_tileset
System.out.println("Get Map Tileset");
new TilesetId();
client.getMapTileset(TilesetId.MICROSOFT_BASE);
```

### Get Map Attribution
```java com.azure.maps.render.sync.get_map_attribution
System.out.println("Get Map Attribution");
GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
new TilesetId();
client.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds);
```

### Get Copyright Caption
```java com.azure.maps.render.sync.get_copyright_caption
System.out.println("Get Copyright Caption");
client.getCopyrightCaption();
```

### Get Map Static Image
```java com.azure.maps.render.sync.get_map_static_image
System.out.println("Get Map Static Image");
GeoBoundingBox bbox = new GeoBoundingBox(1.355233, 42.982261, 24.980233, 56.526017);
new StaticMapLayer();
new RasterTileFormat();
MapStaticImageOptions mapStaticImageOptions = new MapStaticImageOptions().setStaticMapLayer(StaticMapLayer.BASIC)
    .setBoundingBox(bbox);
client.getMapStaticImage(mapStaticImageOptions).toStream();
```

### Get Copyright From Bounding Box
```java com.azure.maps.render.sync.get_copyright_from_bounding_box
GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
client.getCopyrightFromBoundingBox(boundingBox, true);
```

### Get Copyright For Tile
```java com.azure.maps.render.sync.get_copyright_for_tile
client.getCopyrightForTile(new TileIndex().setX(9).setY(22).setZ(6), true);
```

### Get Copyright For World
```java com.azure.maps.render.sync.get_copyright_for_world
client.getCopyrightForWorld(true);
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps

Several Azure Maps Render Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Render Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-render/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-render/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-render/src/samples
[rest_docs]: https://learn.microsoft.com/rest/api/maps
[product_docs]: https://learn.microsoft.com/azure/azure-maps/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential

