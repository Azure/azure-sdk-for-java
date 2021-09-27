## Generate autorest code
``` yaml
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/purview/data-plane/Azure.Analytics.Purview.MetadataPolicies/preview/2021-07-01/purviewMetadataPolicy.json

java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewMetadataClient
service-name: PurviewMetadata
service-versions:
  - '2021-07-01'
```
