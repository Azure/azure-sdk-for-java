# Azure Maps Elevation for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Elevation.
---
## Getting Started

To build the SDK for Maps Elevation, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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
  - from: swagger-document
    where: "$"
    transform: >
        $["security"] = [];
  - from: swagger-document
    where: $.paths["/elevation/line/{format}"].get
    transform: >
      var linesParameter = $.parameters.find(param => param.name === "lines");
      delete linesParameter["x-ms-skip-url-encoding"];

title: ElevationClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/DEM/preview/1.0/elevation.json
namespace: com.azure.maps.elevation
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
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: ElevationResult
customization-class: src/main/java/ElevationCustomization.java
disable-client-builder: true
```
