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
  - rename-model:
        from: TrafficIncidentViewportViewpResp
        to: TrafficIncidentViewportResponse
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = []; 

title: TrafficClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Traffic/preview/1.0/traffic.json
namespace: com.azure.maps.traffic
java: true
use: '@autorest/java@4.1.42'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
client-logger: true
generate-client-as-impl: true
sync-methods: none
output-model: immutable
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: TileFormat,TrafficFlowTileStyle,TileIndex,DelayMagnitude,IconCategory,IncidentDetailStyle,IncidentGeometryType,ProjectionStandard,SpeedUnit,TileFormat,TrafficFlowSegmentStyle,TrafficFlowTileStyle,TrafficIncidentTileStyle,TrafficIncidentPointOfInterest,TrafficFlowSegmentData,TrafficFlowSegmentDataFlowSegmentDataCoordinates,TrafficIncidentViewport,TrafficIncidentViewportViewpResp,TrafficState,MapsPoint,TrafficIncidentDetail
customization-class: src/main/java/TrafficCustomization.java
```
