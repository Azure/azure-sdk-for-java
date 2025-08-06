# Azure Resource Manager storage client library for Java

Azure Resource Manager storage client library for Java

For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-storage;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-storage</artifactId>
    <version>2.53.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

Microsoft Entra ID token authentication relies on the [credential class][azure_identity_credentials] from [Azure Identity][azure_identity] package.

Azure subscription ID can be configured via `AZURE_SUBSCRIPTION_ID` environment variable.
Azure tenant ID can be configured via `AZURE_TENANT_ID` environment variable.

Assuming the use of the `DefaultAzureCredential` credential class, the client can be authenticated using the following code:

```java readme-sample-authenticate
AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
StorageManager manager = StorageManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

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
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_identity_credentials]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
