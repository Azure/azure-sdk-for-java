# Azure Maps SDK Elevation client library for Java (Deprecated)

Azure Maps SDK Elevation client library for Java.

This package contains the Azure Maps SDK Elevation client library which contains Azure Maps Elevation APIs. For documentation on how to use this package, please see [Azure Maps Elevation REST APIs](https://docs.microsoft.com/rest/api/maps/elevation).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Important

Please note, this package has been deprecated and will no longer be maintained after 5 May 2023. We encourage you to upgrade to the latest version to continue receiving updates. All other Azure Maps APIs, Services and TilesetIDs are unaffected by this retirement. Refer to the migration guide [Elevation Services Retirement] for guidance on upgrading. Refer to our deprecation policy (https://aka.ms/azsdk/support-policies) for more details.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-elevation;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-elevation</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java com.azure.maps.elevation.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a client
ElevationClient client = new ElevationClientBuilder()
    .credential(tokenCredential)
    .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
    .buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Get Data For Points
```java com.azure.maps.elevation.sync.get_data_for_points
client.getDataForPoints(Arrays.asList(
    new GeoPosition(-121.66853362143818, 46.84646479863713),
    new GeoPosition(-121.68853362143818, 46.856464798637127)));
```

Get Data For Polyline
```java com.azure.maps.elevation.sync.get_data_for_polyline
client.getDataForPolyline(Arrays.asList(
    new GeoPosition(-121.66853362143818, 46.84646479863713),
    new GeoPosition(-121.65853362143818, 46.85646479863713)), 5);
```

Get Data For Bounding Box
```java com.azure.maps.elevation.sync.get_data_for_bounding_box
client.getDataForBoundingBox(new GeoBoundingBox(-121.668533621438, 46.8464647986371,
    -121.658533621438, 46.8564647986371), 3, 3);
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps
Several Azure Maps Elevation Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Elevation Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-elevation/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-elevation/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-elevation/src/samples
[rest_docs]: https://docs.microsoft.com/rest/api/maps
[product_docs]: https://docs.microsoft.com/azure/azure-maps/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[Elevation Services Retirement]: https://azure.microsoft.com/updates/azure-maps-elevation-apis-and-render-v2-dem-tiles-will-be-retired-on-5-may-2023/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmaps%2Fazure-maps-elevation%2FREADME.png)
