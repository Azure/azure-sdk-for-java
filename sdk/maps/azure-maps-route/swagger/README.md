# Azure Maps Route for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Route.
---
## Getting Started

To build the SDK for Maps Route, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = [];
  - from: swagger-document
    where: $..responses
    debug: true
    transform: >
      $["default"] = { 
        "description": "An unexpected error occurred.",
        "schema": {
          "$ref": "../../../../../common-types/data-plane/v1/types.json#/definitions/ErrorResponse"
        },
        "x-ms-error-response": true
      }

  - rename-model:
        from: RouteMatrixQuery
        to: RouteMatrixQueryPrivate     
  - rename-model:
        from: RouteDirectionParameters
        to: RouteDirectionParametersPrivate 
  - rename-model:
        from: RouteMatrixResult
        to: RouteMatrixResultPrivate
  - rename-model:
        from: RouteDirectionsBatchResult
        to: RouteDirectionsBatchResultPrivate  
  - rename-model:
        from: Route
        to: MapsSearchRoute  

title: RouteClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Route/preview/1.0/route.json
namespace: com.azure.maps.route
java: true
use: '@autorest/java@4.1.52'
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
custom-types: AlternativeRouteType,BatchResultSummary,BatchResultItem,ComputeTravelTime,DelayMagnitude,DrivingSide,EffectiveSetting,GuidanceInstructionType,GuidanceManeuver,InclineLevel,JunctionType,Report,RouteSectionType,RouteTravelMode,MapsSearchRoute,RouteAvoidType,RouteDelayReason,RouteDirections,RouteDirectionsBatchItem,RouteDirectionsBatchItemResponse,RouteGuidance,RouteInstructionGroup,RouteInstruction,RouteMatrix,RouteRange,RouteInstructionsType,RouteLeg,RouteLegSummary,RouteMatrixSummary,RouteOptimizedWaypoint,RouteRangeResult,RouteReport,RouteRepresentationForBestOrder,RouteSection,RouteSectionTec,RouteSectionTecCause,RouteSummary,RouteType,SectionType,TravelMode,VehicleEngineType,VehicleLoadType,WindingnessLevel
customization-class: src/main/java/RouteCustomization.java
generic-response-type: true
no-custom-headers: true
```

### Rename ResponseSectionType to RouteSectionType
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.ResponseSectionType
    transform: >
      $["x-ms-enum"].name = "RouteSectionType";
```

### Rename ResponseTravelMode to RouteSectionType
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.ResponseTravelMode
    transform: >
      $["x-ms-enum"].name = "RouteTravelMode";
```

### Rename SimpleCategory to RouteDelayReason
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.SimpleCategory
    transform: >
      $["x-ms-enum"].name = "RouteDelayReason";
```
