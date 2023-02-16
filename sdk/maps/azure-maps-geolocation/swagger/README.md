# Azure Geolocation

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

``` yaml $(java)
directive:

  - from: swagger-document
    where: "$"
    transform: >
        $["securityDefinitions"] = {};
  - from: swagger-document
    where: "$"
    transform: >
        $["security"] = [];

title: GeolocationClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Geolocation/preview/1.0/geolocation.json
namespace: com.azure.maps.geolocation
license-header: MICROSOFT_MIT_SMALL
java: true
output-folder: ../
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
custom-types: CountryRegion,IpAddressToLocationResult
customization-jar-path: target/azure-maps-geolocation-customization-1.0.0-beta.1.jar
customization-class: GeoLocationCustomization
```