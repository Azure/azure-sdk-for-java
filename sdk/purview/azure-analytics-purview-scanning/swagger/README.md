## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Scanning/preview/2018-12-01-preview/scanningService.json
java: true
use: '@autorest/java@4.1.52'
output-folder: ../
namespace: com.azure.analytics.purview.scanning
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewScanningClient
artifact-id: azure-analytics-purview-scanning
service-name: PurviewScanning
service-versions:
  - 2018-12-01-preview
generate-client-as-impl: true
generate-sync-async-clients: true
generate-samples: true
generate-builder-per-client: false
```
