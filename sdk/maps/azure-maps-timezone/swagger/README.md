# Azure Timezone

> see https://aka.ms/autorest

This is the AutoRest configuration file for Timezone Client

---

## Getting Started

To build the SDK for Search, simply [Install AutoRest](https://aka.ms/autorest/install) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

---

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

### Basic Information

These are the global settings for Timezone Client.

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

title: TimezoneClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Timezone/preview/1.0/timezone.json
namespace: com.azure.maps.timezone
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
custom-types: CountryRecord,IanaId,TimezoneWindows,TimezoneResult,TimezoneOptions,TimezoneNames,TimezoneId,TimezoneIanaVersionResult,ReferenceTime,TimeTransition
customization-jar-path: target/azure-maps-timezone-customization-1.0.0-beta.1.jar
customization-class: TimezoneCustomization
```