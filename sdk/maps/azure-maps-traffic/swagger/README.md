# Azure Maps Traffic for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Traffic.
---
## Getting Started

To build the SDK for Maps Traffic, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

### Code generation settings

## Java

``` yaml
directive:
  - rename-model:
        from: Point
        to: MapsPoint  
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = []; 

title: TrafficClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Traffic/preview/1.0/traffic.json
namespace: com.azure.maps.traffic
java: true
use: '@autorest/java@4.1.50'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generic-response-type: true
generate-client-as-impl: true
sync-methods: none
output-model: immutable
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: TileFormat,TrafficFlowTileStyle,TileIndex,DelayMagnitude,IconCategory,IncidentDetailStyle,IncidentGeometryType,ProjectionStandard,SpeedUnit,TileFormat,TrafficFlowSegmentStyle,TrafficFlowTileStyle,TrafficIncidentTileStyle,TrafficIncidentPointOfInterest,TrafficFlowSegmentData,TrafficFlowSegmentDataPropertiesCoordinates,TrafficIncidentViewport,TrafficIncidentViewportResponse,TrafficState,MapsPoint,TrafficIncidentDetail
customization-class: src/main/java/TrafficCustomization.java
no-custom-headers: true
use-eclipse-language-server: false
```

### Create definition TrafficFlowSegmentDataPropertiesCoordinates

``` yaml
directive:
  - from: traffic.json
    where: $.definitions
    transform: >
      $.TrafficFlowSegmentDataPropertiesCoordinates = $.TrafficFlowSegmentData.properties.flowSegmentData.properties.coordinates;
      delete $.TrafficFlowSegmentData.properties.flowSegmentData.properties.coordinates;
      $.TrafficFlowSegmentData.properties.flowSegmentData.properties.coordinates = { "$ref": "#/definitions/TrafficFlowSegmentDataPropertiesCoordinates" };
```

### Create definition TrafficIncidentViewportResponse

``` yaml
directive:
  - from: traffic.json
    where: $.definitions
    transform: >
      $.TrafficIncidentViewportResponse = $.TrafficIncidentViewport.properties.viewpResp;
      delete $.TrafficIncidentViewport.properties.viewpResp;
      $.TrafficIncidentViewport.properties.viewpResp = { "$ref": "#/definitions/TrafficIncidentViewportResponse" };
```
