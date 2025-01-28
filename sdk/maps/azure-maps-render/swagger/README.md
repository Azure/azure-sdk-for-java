# Azure Maps Render for Java

> see https://aka.ms/autorest
This is the AutoRest configuration file for Maps Render.
---
## Getting Started

To build the SDK for Maps Render, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`
To see additional help and options, run:

> `autorest --help`
### Setup
```ps
npm install -g autorest
```

### Code generation settings

## Java

``` yaml    
title: RenderClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Render/stable/2024-04-01/render.json
namespace: com.azure.maps.render
java: true
use: '@autorest/java@4.1.29'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: ErrorAdditionalInfo,ErrorDetail,ErrorResponse,ErrorResponseException,LocalizedMapView,MapImageStyle,RasterTileFormat,StaticMapLayer,MapTileSize,TileIndex,TilesetID,Copyright,CopyrightCaption,MapAttribution,RegionCopyrights,RegionCopyrightsCountry,MapTileset
customization-class: src/main/java/RenderCustomization.java
generic-response-type: true
no-custom-headers: true
modelerfour:
  additional-checks: false
  lenient-model-deduplication: true
```

``` yaml $(java)
- from: swagger-document
  where: "$"
  transform: >
    $["securityDefinitions"] = {};
    $["security"] = [];

- from: swagger-document
  where: $..responses
  transform: >
    $["default"] = {
      "description": "An unexpected error occurred.",
      "schema": {
        "$ref": "../../../../../common-types/data-plane/v1/types.json#/definitions/ErrorResponse"
      },
      "x-ms-error-response": true
    };
```
