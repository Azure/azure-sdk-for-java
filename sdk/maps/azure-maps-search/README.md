# Azure Maps SDK Search client library for Java

Azure Maps SDK Search client library for Java.

This package contains the Azure Maps SDK Search client library which contains Azure Maps Search APIs. For documentation on how to use this package, please see [Azure Maps Search SDK for Java](https://docs.microsoft.com/rest/api/maps/search).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-search;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-search</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Maps Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java com.azure.maps.search.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a builder
MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
builder.credential(tokenCredential);
builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

// Builds a client
MapsSearchClient client = builder.buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
### Get Polygons
```java sync.get_polygon
System.out.println("Get Polygons:");
GeoPosition coordinates = new GeoPosition(-122.204141, 47.61256);

Boundary result = client.getPolygons(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL);

//with response
Response<Boundary> response = client.getPolygonsWithResponse(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL, Context.NONE);

```

### Get Geocoding
```java sync.get_geocoding
System.out.println("Get Geocoding:");

//simple
client.getGeocoding(new BaseSearchOptions().setQuery("1 Microsoft Way, Redmond, WA 98052"));

//with multiple options
GeocodingResponse result = client.getGeocoding(
    new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5));

// with response
ResponseBase<SearchesGetGeocodingHeaders, GeocodingResponse> response = client.getGeocodingWithResponse(
    new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5), null);

// with response no custom header
Response<GeocodingResponse> responseNoHeader = client.getGeocodingNoCustomHeaderWithResponse(
    new BaseSearchOptions().setCoordinates(new GeoPosition(-74.011454, 40.706270)).setTop(5), null);
```

### Get Geocoding Batch
```java sync.get_geocoding_batch
System.out.println("Get Geocoding Batch:");

//with multiple items
GeocodingBatchRequestBody body = new GeocodingBatchRequestBody();
GeocodingBatchRequestItem addressLineItem = new GeocodingBatchRequestItem();
addressLineItem.setAddressLine("400 Broad St");
GeocodingBatchRequestItem queryItem = new GeocodingBatchRequestItem();
queryItem.setQuery("15171 NE 24th St, Redmond, WA 98052, United States");
body.setBatchItems(Arrays.asList(addressLineItem, queryItem));

GeocodingBatchResponse result = client.getGeocodingBatch(body);

// with response
Response<GeocodingBatchResponse> response = client.getGeocodingBatchWithResponse(body, Context.NONE);

```

### Get Reverse Geocoding
```java sync.get_reverse_geocoding
System.out.println("Get Reverse Geocoding:");

GeoPosition coordinates = new GeoPosition(-122.34255, 47.0);
GeocodingResponse result = client.getReverseGeocoding(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null);

//with response
Response<GeocodingResponse> response = client.getReverseGeocodingWithResponse(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null, Context.NONE);
```

### Get Reverse Geocoding Batch
```java sync.get_reverse_geocoding_batch
System.out.println("Get Reverse Geocoding Batch:");

//with multiple items
ReverseGeocodingBatchRequestBody body = new ReverseGeocodingBatchRequestBody();
ReverseGeocodingBatchRequestItem item1 = new ReverseGeocodingBatchRequestItem();
ReverseGeocodingBatchRequestItem item2 = new ReverseGeocodingBatchRequestItem();
item1.setCoordinates(new GeoPosition(-122.34255, 47.0));
item2.setCoordinates(new GeoPosition(-122.34255, 47.0));
body.setBatchItems(Arrays.asList(item1, item2));

GeocodingBatchResponse result = client.getReverseGeocodingBatch(body);

// with response
Response<GeocodingBatchResponse> response = client.getReverseGeocodingBatchWithResponse(body, Context.NONE);

```


## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps

Several Azure Maps Search Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Search Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-search/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-search/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-search/src/samples
[rest_docs]: https://docs.microsoft.com/rest/api/maps
[product_docs]: https://docs.microsoft.com/azure/azure-maps/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmaps%2Fazure-maps-search%2FREADME.png)
