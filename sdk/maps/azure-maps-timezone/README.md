# Azure Maps SDK Timezone client library for Java

Azure Maps SDK Timezone client library for Java.

This package contains the Azure Maps SDK Timezone client library which contains Azure Maps Timezone APIs. For documentation on how to use this package, please see [Azure Maps Timezone SDK for Java](https://docs.microsoft.com/rest/api/maps/timezone).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-timezone;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-timezone</artifactId>
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

```java com.azure.maps.timezone.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a client
TimezoneClient client = new TimezoneClientBuilder()
    .credential(tokenCredential)
    .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
    .buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Get Timezone By Id
```java com.azure.maps.timezone.sync.get_timezone_by_id
TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL);
client.getTimezoneById(options);
```

Get Timezone By Coordinates
```java com.azure.maps.timezone.sync.get_timezone_by_coordinates
GeoPosition cd = new GeoPosition(-122, 47.0);
TimezoneCoordinateOptions op = new TimezoneCoordinateOptions(cd).setTimezoneOptions(TimezoneOptions.ALL);
client.getTimezoneByCoordinates(op);
```

Get Iana Timezone Ids
```java com.azure.maps.timezone.async.get_timezone_enum_iana
asyncClient.getIanaTimezoneIds();
```

Get Iana Version
```java com.azure.maps.timezone.sync.get_timezone_iana_version
client.getIanaVersion();
```

Convert Windows Timezone To Iana
Get Iana Version
```java com.azure.maps.timezone.sync.convert_windows_timezone_to_iana
client.convertWindowsTimezoneToIana("pacific standard time", null);
```

Get Windows Timezone Ids
```java com.azure.maps.timezone.async.get_timezone_enum_windows
asyncClient.getWindowsTimezoneIds();
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps
Several Azure Maps Search Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Timezone Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-timezone/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-timezone/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-timezone/src/samples
[rest_docs]: https://docs.microsoft.com/rest/api/maps
[product_docs]: https://docs.microsoft.com/azure/azure-maps/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmaps%2Fazure-maps-timezone%2FREADME.png)
