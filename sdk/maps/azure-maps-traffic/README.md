# Azure Maps SDK Traffic client library for Java

Azure Maps SDK Traffic client library for Java.

This package contains the Azure Maps SDK Traffic client library which contains Azure Maps Traffic APIs. For documentation on how to use this package, please see [Azure Maps Traffic SDK for Java](https://learn.microsoft.com/rest/api/maps/traffic).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product


[//]: # ({x-version-update-start;com.azure:azure-maps-traffic;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-traffic</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Maps Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

We recommend using Microsoft Entra ID with [`DefaultAzureCredential`][azure_identity]. This allows you to configure authentication through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

By default, Azure Active Directory token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java com.azure.maps.traffic.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a builder
TrafficClientBuilder builder = new TrafficClientBuilder();
builder.credential(tokenCredential);
builder.trafficClientId(System.getenv("MAPS_CLIENT_ID"));
builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

// Builds a client
TrafficClient client = builder.buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Get Traffic Flow Segment
```java com.azure.maps.traffic.sync.get_traffic_flow_segment
System.out.println("Get Traffic Flow Segment:");

// options
client.getTrafficFlowSegment(
    new TrafficFlowSegmentOptions()
        .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
        .setCoordinates(new GeoPosition(4.84239, 52.41072)));

// complete
client.getTrafficFlowSegment(
    new TrafficFlowSegmentOptions()
        .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
        .setCoordinates(new GeoPosition(4.84239, 52.41072)).setOpenLr(false)
        .setThickness(2).setUnit(SpeedUnit.MPH));
```

Get Traffic Flow Tile
```java com.azure.maps.traffic.sync.get_traffic_flow_tile
System.out.println("Get Traffic Flow Tile:");

// options
client.getTrafficFlowTile(
    new TrafficFlowTileOptions()
        .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
        .setTileIndex(new TileIndex().setX(1022).setY(680)));

// complete
client.getTrafficFlowTile(
    new TrafficFlowTileOptions()
        .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
        .setTileIndex(new TileIndex().setX(1022).setY(680)).setThickness(10));
```

Get Traffic Incident Detail
```java com.azure.maps.traffic.sync.get_traffic_incident_detail
System.out.println("Get Traffic Incident Detail:");

// options
client.getTrafficIncidentDetail(
    new TrafficIncidentDetailOptions()
        .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
        .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
        .setTrafficmodelId("1335294634919"));

// complete
client.getTrafficIncidentDetail(
    new TrafficIncidentDetailOptions()
        .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
        .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
        .setTrafficmodelId("1335294634919").setLanguage("en")
        .setProjectionStandard(ProjectionStandard.EPSG900913).setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
        .setExpandCluster(false).setOriginalPosition(false));
```

Get Traffic Incident Tile
```java com.azure.maps.traffic.sync.get_traffic_incident_tile
System.out.println("Get Traffic Incident Tile:");

// options
client.getTrafficIncidentTile(
    new TrafficIncidentTileOptions()
        .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
        .setZoom(10).setTileIndex(new TileIndex().setX(1022).setY(680)));

// complete
client.getTrafficIncidentTile(
    new TrafficIncidentTileOptions()
        .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
        .setZoom(10).setTileIndex(new TileIndex().setX(175).setY(408)));
```

Get Traffic Incident Viewport
```java com.azure.maps.traffic.sync.get_traffic_incident_viewport
System.out.println("Get Traffic Incident Viewport:");

// options
client.getTrafficIncidentViewport(
    new TrafficIncidentViewportOptions()
        .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
        .setBoundingZoom(2).setOverview(new GeoBoundingBox(45, 45, 45, 45))
        .setOverviewZoom(2));

// complete
client.getTrafficIncidentViewport(
    new TrafficIncidentViewportOptions()
        .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
        .setBoundingZoom(2).setOverview(new GeoBoundingBox(45, 45, 45, 45))
        .setOverviewZoom(2).setCopyright(true));
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating `400 - Bad Request`
## Next steps

Several Azure Maps Traffic Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Traffic Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-traffic/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-traffic/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-traffic/src/samples
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

