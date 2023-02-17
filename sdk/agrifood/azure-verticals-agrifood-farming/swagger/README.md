## Generate autorest code
``` yaml
input-file: https://github.com/Azure/azure-rest-api-specs/blob/e38daec67d57ef9c4804b1e3055753407e45fa71/specification/agrifood/data-plane/Microsoft.AgFoodPlatform/preview/2022-11-01-preview/agfood.json
java: true
output-folder: ../
namespace: com.azure.verticals.agrifood.farming
license-header: MICROSOFT_MIT_SMALL
data-plane: true
security: AADToken
security-scopes: https://farmbeats.azure.net/.default
title: FarmBeatsClient
directive:
  - from: swagger-document
    where: $
    transform: |
      $["x-ms-parameterized-host"] = {
        "hostTemplate": "{endpoint}",
        "useSchemePrefix": false,
        "positionInOperation": "first",
        "parameters": [
            {
            "name": "endpoint",
            "description": "The Azure FarmBeats account endpoint.",
            "required": true,
            "type": "string",
            "in": "path",
            "x-ms-skip-url-encoding": true,
            "x-ms-parameter-location": "client"
            }
        ]
        }
```
