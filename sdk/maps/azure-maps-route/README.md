# Azure Maps SDK Route client library for Java

Azure Maps SDK Route client library for Java.

This package contains Microsoft Azure SDK for Route Management SDK which contains Azure Maps Route REST APIs. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://learn.microsoft.com/rest/api/maps/route).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-route;current})
```xml 
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-route</artifactId>
    <version>1.0.0-beta.4</version>
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

```java com.azure.maps.route.async.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a builder
MapsRouteClientBuilder builder = new MapsRouteClientBuilder();
builder.credential(tokenCredential);
builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

// Builds a client
MapsRouteAsyncClient client = builder.buildAsyncClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

For SAS-based authentication, please refer to [AccountsListSasSamples.java][https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-resourcemanager-maps/src/samples/java/com/azure/resourcemanager/maps/generated/AccountsListSasSamples.java].

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Begin Request Route Matrix
```java com.azure.maps.search.sync.begin_request_route_matrix
System.out.println("Request route matrix");
RouteMatrixQuery matrixQuery = new RouteMatrixQuery();

// origins
GeoPointCollection origins = new GeoPointCollection(Arrays.asList(
    new GeoPoint(4.85106, 52.36006),
    new GeoPoint(4.85056, 52.36187)
));

// destinations
GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(
    new GeoPoint(4.85003, 52.36241),
    new GeoPoint(13.42937, 52.50931)
));

matrixQuery.setDestinations(destinations);
matrixQuery.setOrigins(origins);

RouteMatrixOptions matrixOptions = new RouteMatrixOptions(matrixQuery);
client.beginGetRouteMatrix(matrixOptions).getFinalResult();
```

Get Route Directions
```java com.azure.maps.route.sync.get_route_directions
System.out.println("Get route directions");
List<GeoPosition> routePoints = Arrays.asList(
    new GeoPosition(13.42936, 52.50931),
    new GeoPosition(13.43872, 52.50274));
RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
RouteDirections directions = client.getRouteDirections(routeOptions);
RouteReport report = directions.getReport(); // get the report and use it
```

Get Route Directions With Parameters
```java com.azure.maps.route.sync.get_route_directions_parameters
System.out.println("Get route parameters");
// supporting points
GeoCollection supportingPoints = new GeoCollection(
    Arrays.asList(
        new GeoPoint(13.42936, 52.5093),
        new GeoPoint(13.42859, 52.50844)
        ));

// avoid areas
List<GeoPolygon> polygons = Arrays.asList(
    new GeoPolygon(
        new GeoLinearRing(Arrays.asList(
            new GeoPosition(-122.39456176757811, 47.489368981370724),
            new GeoPosition(-122.00454711914061, 47.489368981370724),
            new GeoPosition(-122.00454711914061, 47.65151268066222),
            new GeoPosition(-122.39456176757811, 47.65151268066222),
            new GeoPosition(-122.39456176757811, 47.489368981370724)
        ))
    ),
    new GeoPolygon(
        new GeoLinearRing(Arrays.asList(
            new GeoPosition(100.0, 0.0),
            new GeoPosition(101.0, 0.0),
            new GeoPosition(101.0, 1.0),
            new GeoPosition(100.0, 1.0),
            new GeoPosition(100.0, 0.0)
        ))
    )
);
GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
RouteDirectionsParameters parameters = new RouteDirectionsParameters()
    .setSupportingPoints(supportingPoints)
    .setAvoidVignette(Arrays.asList("AUS", "CHE"))
    .setAvoidAreas(avoidAreas);
client.getRouteDirections(routeOptions,
    parameters);
```

Get Route Range
```java com.azure.maps.search.sync.route_range
System.out.println("Get route range");
RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452, 5.86605), Duration.ofSeconds(6000));
client.getRouteRange(rangeOptions);
```

Begin Request Route Directions Batch
```java com.azure.maps.search.sync.begin_request_route_directions_batch
RouteDirectionsOptions options1 = new RouteDirectionsOptions(
    Arrays.asList(new GeoPosition(-122.128384, 47.639987),
        new GeoPosition(-122.184408, 47.621252),
        new GeoPosition(-122.332000, 47.596437)))
    .setRouteType(RouteType.FASTEST)
    .setTravelMode(TravelMode.CAR)
    .setMaxAlternatives(5);

RouteDirectionsOptions options2 = new RouteDirectionsOptions(
    Arrays.asList(new GeoPosition(-122.348934, 47.620659),
        new GeoPosition(-122.342015, 47.610101)))
    .setRouteType(RouteType.ECONOMY)
    .setTravelMode(TravelMode.BICYCLE)
    .setUseTrafficData(false);

RouteDirectionsOptions options3 = new RouteDirectionsOptions(
    Arrays.asList(new GeoPosition(-73.985108, 40.759856),
        new GeoPosition(-73.973506, 40.771136)))
    .setRouteType(RouteType.SHORTEST)
    .setTravelMode(TravelMode.PEDESTRIAN);

System.out.println("Get Route Directions Batch");

List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);
SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> poller =
    client.beginRequestRouteDirectionsBatch(optionsList);
poller.getFinalResult();
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps

Several Azure Maps Route Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Route Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-route/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-route/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-route/src/samples
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

