# Azure Render

> see https://aka.ms/autorest

### Setup
> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
mvn install
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings

## Java

``` yaml
directive:
- from: swagger-document
  where: "$"
  transform: >
    $["securityDefinitions"] = {};
- from: swagger-document
  where: "$"
  transform: >
    $["security"] = [];

- from: swagger-document
  where: $..responses
  debug: true
  transform: |
    $["default"] = { 
            "description": "An unexpected error occurred.",
            "schema": {
              "$ref": "../../../../../common-types/data-plane/v1/types.json#/definitions/ErrorResponse"
            },
            "x-ms-error-response": true
          }
    
title: RenderClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Render/preview/2.1/render.json
namespace: com.azure.maps.render
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: LocalizedMapView,MapImageStyle,RasterTileFormat,StaticMapLayer,MapTileSize,TileIndex,TilesetID,Copyright,CopyrightCaption,MapAttribution,RegionCopyrights,RegionCopyrightsCountry,ErrorAdditionalInfo,ErrorDetail,ErrorResponse,ErrorResponseException,MapTileset
customization-jar-path: target/azure-maps-render-customization-1.0.0-beta.1.jar
customization-class: RenderCustomization
```
