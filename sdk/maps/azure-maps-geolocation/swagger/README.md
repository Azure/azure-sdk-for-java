# Azure Maps Geolocation for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Geolocation.
---
## Getting Started

To build the SDK for Maps Geolocation, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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
        
title: GeolocationClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Geolocation/preview/1.0/geolocation.json
namespace: com.azure.maps.geolocation
license-header: MICROSOFT_MIT_SMALL
java: true
use: '@autorest/java@4.1.29'
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
customization-class: src/main/java/GeoLocationCustomization.java
```
