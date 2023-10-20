# Azure DevCenter client library for Java

This package contains Microsoft Azure DevCenter client library.

## Documentation

Various documentation is available to help you get started

- [Product documentation: Azure Deployment Environments][environments_documentation]
- [Product documentation: Microsoft Dev Box][devbox_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- The minimum requirements to create Dev Box resources using this SDK are to create DevCenter, Project, and Pool resources.
- The minimum requirements to create Environment resources using this SDK are to create DevCenter, Project, EnvironmentType, and CatalogItem resources.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-developer-devcenter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-developer-devcenter</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts




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
[environments_documentation]: https://learn.microsoft.com/azure/deployment-environments/
[devbox_documentation]: https://learn.microsoft.com/azure/dev-box/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
