## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/agfood/data-plane/Microsoft.AgFoodPlatform/preview/2021-03-31-preview/agfood.json
java: true
output-folder: ../
namespace: com.azure.verticals.agrifood.farming
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://farmbeats.azure.net/.default
title: FarmBeatsClient
```
