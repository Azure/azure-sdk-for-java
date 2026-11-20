# Azure Purview DataMap client library for Java

Microsoft Purview Data Map provides the foundation for data discovery and data governance. Microsoft Purview Data Map is a cloud native PaaS service that captures metadata about enterprise data present in analytics and operation systems on-premises and cloud. Azure Purview DataMap client provides a set of APIs in Purview Data Map Data Plane. For a full list of APIs, please refer to [Data Map API](https://learn.microsoft.com/rest/api/purview/datamapdataplane/operation-groups?view=rest-purview-datamapdataplane-2023-09-01).

This package contains Microsoft Azure PurviewDataMap client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-analytics-purview-datamap;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-purview-datamap</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

See [DataMap APIs][https://learn.microsoft.com/rest/api/purview/datamapdataplane/operation-groups?view=rest-purview-datamapdataplane-2023-09-01] for general introduction the API scenarios.

## Examples

### Get Type Definition By Name
```java com.azure.analytics.purview.datamap.readme

ClientSecretCredential cred = new ClientSecretCredentialBuilder()
    .tenantId(Configuration.getGlobalConfiguration().get("TENANT_ID"))
    .authorityHost(Configuration.getGlobalConfiguration().get("AUTHORITY_HOST"))
    .clientId(Configuration.getGlobalConfiguration().get("CLIENT_ID"))
    .clientSecret(Configuration.getGlobalConfiguration().get("CLIENT_SECRET"))
    .build();
DataMapClientBuilder clientBuilder = new DataMapClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
    .credential(cred);

TypeDefinitionClient typeDefinitionClient = clientBuilder.buildTypeDefinitionClient();
AtlasEntityDef type = typeDefinitionClient.getEntityByName("AtlasGlossary");
```

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
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity


