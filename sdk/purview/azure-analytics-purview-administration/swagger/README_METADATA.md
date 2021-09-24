## Generate autorest code
``` yaml
input-file:
  - C:/github/azure-rest-api-specs/specification/purview/data-plane/Azure.Analytics.Purview.MetadataPolicies/preview/2021-07-01/purviewMetadataPolicy.json

java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewMetadataClient
service-name: PurviewMetadata
service-versions:
  - '2021-07-01'

pipeline:
  modelerfour:
    flatten-models: false
    flatten-payloads: false
    group-parameters: false
    lenient-model-deduplication: true
```
