# Azure Elevation

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
    where: $.paths["/elevation/line/{format}"].get
    transform: >
      var linesParameter = $.parameters.find(param => param.name === "lines");
      delete linesParameter["x-ms-skip-url-encoding"];

title: ElevationClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/DEM/preview/1.0/elevation.json
namespace: com.azure.maps.elevation
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
custom-types: ElevationResult
customization-jar-path: target/azure-maps-elevation-customization-1.0.0-beta.1.jar
customization-class: ElevationCustomization
```