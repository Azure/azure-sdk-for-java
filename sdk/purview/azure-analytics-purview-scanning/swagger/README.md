## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/purview/data-plane/Azure.Analytics.Purview.Scanning/preview/2018-12-01-preview/scanningService.json
java: true
output-folder: ../
namespace: com.azure.analytics.purview.scanning
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewScanningClient
```
