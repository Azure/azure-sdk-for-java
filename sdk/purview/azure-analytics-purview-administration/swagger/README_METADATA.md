## Generate autorest code
``` yaml
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.MetadataPolicies/preview/2021-07-01-preview/purviewMetadataPolicy.json

java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewMetadataClient
service-name: PurviewMetadata
service-versions:
  - 2021-07-01-preview
```
