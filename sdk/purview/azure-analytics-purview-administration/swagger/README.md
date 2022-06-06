## Generate autorest code

``` yaml
require:
  - https://github.com/Azure/azure-rest-api-specs/blob/dw/change-structure-of-purview/specification/purview/data-plane/administrator/readme.md
batch:
  - purview-metadata: true
  - purview-account: true
```

``` yaml $(purview-metadata)
java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
license-header: MICROSOFT_MIT_SMALL
service-interface-as-public: true
data-plane: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
generate-samples: true
title: PurviewMetadataClient
service-name: PurviewMetadata
artifact-id: azure-analytics-purview-administration
service-versions:
  - 2021-07-01-preview
```

``` yaml $(purview-account)
java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
license-header: MICROSOFT_MIT_SMALL
service-interface-as-public: true
data-plane: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
generate-samples: true
title: PurviewAccountClient
artifact-id: azure-analytics-purview-administration
service-name: PurviewAccount
service-versions:
  - 2019-11-01-preview
```
