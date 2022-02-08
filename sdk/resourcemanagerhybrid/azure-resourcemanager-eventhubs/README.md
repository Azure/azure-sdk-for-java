# Azure Resource Manager EventHubs client library for Java (Hybrid)

Azure Resource Manager EventHubs client library for Java (Hybrid) using [API Profiles][api_profile] to allow building hybrid cloud solutions
that target both Azure and Azure Stack Hub.

For documentation on how to use this package, please see [Azure Management Libraries for Java (Hybrid)][resourcemanagerhybrid_lib].

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-eventhubs</artifactId>
    <version>1.0.0-hybrid</version>
</dependency>
```

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

```java com.azure.resourcemanager.eventhubs.authenticate
String armEndpoint = "https://management.<region>.<your-domain>";
AzureProfile profile = new AzureProfile(getAzureEnvironmentFromArmEndpoint(armEndpoint));
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
EventHubsManager manager = EventHubsManager
    .authenticate(credential, profile);
```

Change `armEndpoint` to point to the Azure Resource Manager endpoint of your Azure Stack Hub. The azure environment's
properties above can be populated with the following example:

```java com.azure.resourcemanager.eventhubs.getazureenvironment
private static AzureEnvironment getAzureEnvironmentFromArmEndpoint(String armEndpoint) {
    // Create HTTP client and request
    HttpClient httpClient = HttpClient.createDefault();

    HttpRequest request = new HttpRequest(HttpMethod.GET,
            String.format("%s/metadata/endpoints?api-version=2019-10-01", armEndpoint))
            .setHeader("accept", "application/json");

    // Execute the request and read the response
    HttpResponse response = httpClient.send(request).block();
    if (response.getStatusCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : " + response.getStatusCode());
    }
    String body = response.getBodyAsString().block();
    try {
        ArrayNode metadataArray = JacksonAdapter.createDefaultSerializerAdapter()
                .deserialize(body, ArrayNode.class, SerializerEncoding.JSON);

        if (metadataArray == null || metadataArray.isEmpty()) {
            throw new RuntimeException("Failed to find metadata : " + body);
        }

        JsonNode metadata = metadataArray.iterator().next();
        AzureEnvironment azureEnvironment = new AzureEnvironment(new HashMap<String, String>() {
            {
                put("managementEndpointUrl", metadata.at("/authentication/audiences/0").asText());
                put("resourceManagerEndpointUrl", armEndpoint);
                put("galleryEndpointUrl", metadata.at("/gallery").asText());
                put("activeDirectoryEndpointUrl", metadata.at("/authentication/loginEndpoint").asText());
                put("activeDirectoryResourceId", metadata.at("/authentication/audiences/0").asText());
                put("activeDirectoryGraphResourceId", metadata.at("/graph").asText());
                put("storageEndpointSuffix", "." + metadata.at("/suffixes/storage").asText());
                put("keyVaultDnsSuffix", "." + metadata.at("/suffixes/keyVaultDns").asText());
            }
        });
        return azureEnvironment;
    } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new RuntimeException(ioe);
    }
}
```

When targeting a hybrid solution to global Azure instead of your Azure Stack Hub, `AzureEnvironment.AZURE` can be used instead.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

See [Samples][sample] for code snippets and samples.

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
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[api_profile]: https://docs.microsoft.com/azure-stack/user/azure-stack-version-profiles
[resourcemanagerhybrid_lib]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanagerhybrid
