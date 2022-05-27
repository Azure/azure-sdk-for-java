## Generate autorest code

``` yaml
batch:
  - package-metadata: true
  - package-account: true
```

``` yaml $(package-metadata)
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.MetadataPolicies/preview/2021-07-01-preview/purviewMetadataPolicy.json

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

``` yaml $(package-account)
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/preview/2019-11-01-preview/account.json

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
