# Azure Maps SDK GeoLocation client library for Java

Azure Maps SDK GeoLocation client library for Java.

This package contains the Azure Maps SDK GeoLocation client library which contains Azure Maps GeoLocation APIs. For documentation on how to use this package, please see [Azure Maps GeoLocation SDK for Java](https://learn.microsoft.com/rest/api/maps/geolocation).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-geolocation;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-geolocation</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

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

```java com.azure.maps.geolocation.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a client
GeolocationClient client = new GeolocationClientBuilder()
    .credential(tokenCredential)
    .clientId(System.getenv("MAPS_CLIENT_ID"))
    .buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

For SAS-based authentication, please refer to [AccountsListSasSamples.java][https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-resourcemanager-maps/src/samples/java/com/azure/resourcemanager/maps/generated/AccountsListSasSamples.java].

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Get Polygons
```java com.azure.maps.geolocation.sync.get_ip_to_location
client.getLocation(InetAddress.getByName("131.107.0.89"));
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an IP address, a error is returned, indicating `400 - Bad Request`.

## Next steps

Several Azure Maps GeoLocation Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps GeoLocation Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-geolocation/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-geolocation/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-geolocation/src/samples
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

