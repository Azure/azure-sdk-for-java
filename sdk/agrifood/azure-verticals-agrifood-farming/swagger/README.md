## Generate autorest code
``` yaml
input-file: https://github.com/Azure/azure-rest-api-specs/blob/779859ed61d44dfe74c32137312ff9fe24894df4/specification/agrifood/data-plane/Microsoft.AgFoodPlatform/preview/2021-07-31-preview/agfood.json
java: true
output-folder: ../
namespace: com.azure.verticals.agrifood.farming
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://farmbeats.azure.net/.default
title: FarmBeatsClient
```
