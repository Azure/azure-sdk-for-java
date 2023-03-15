# Azure Traffic

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
  - rename-model:
        from: Point
        to: MapsPoint  
  - from: swagger-document
    where: "$"
    transform: >
        $["securityDefinitions"] = {};
  - from: swagger-document
    where: "$"
    transform: >
        $["security"] = [];
  - rename-model:
        from: TrafficIncidentViewportViewpResp
        to: TrafficIncidentViewportResponse

title: TrafficClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Traffic/preview/1.0/traffic.json
namespace: com.azure.maps.traffic
java: true
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
custom-types: TileFormat,TrafficFlowTileStyle,TileIndex,DelayMagnitude,IconCategory,IncidentDetailStyle,IncidentGeometryType,ProjectionStandard,SpeedUnit,TileFormat,TrafficFlowSegmentStyle,TrafficFlowTileStyle,TrafficIncidentTileStyle,TrafficIncidentPointOfInterest,TrafficFlowSegmentData,TrafficFlowSegmentDataFlowSegmentDataCoordinates,TrafficIncidentViewport,TrafficIncidentViewportViewpResp,TrafficState,MapsPoint,TrafficIncidentDetail
customization-jar-path: target/azure-maps-traffic-customization-1.0.0-beta.1.jar
customization-class: TrafficCustomization
```