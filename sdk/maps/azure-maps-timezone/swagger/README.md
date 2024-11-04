# Azure Maps Timezone for Java

> see https://aka.ms/autorest
This is the AutoRest configuration file for Maps Timezone.
---
## Getting Started

To build the SDK for Maps Timezone, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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

### Basic Information

These are the global settings for Timezone Client.

``` yaml
directive:
  - rename-model:
        from: TimezoneIanaVersionResult
        to: TimeZoneIanaVersionResult  
  - rename-model:
        from: TimezoneId
        to: TimeZoneId
  - rename-model:
        from: TimezoneNames
        to: TimeZoneNames
  - rename-model:
        from: TimezoneResult
        to: TimeZoneResult
  - rename-model:
        from: TimezoneWindows
        to: TimeZoneWindows
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = [];

title: TimezoneClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Timezone/preview/1.0/timezone.json
namespace: com.azure.maps.timezone
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
custom-types: CountryRecord,IanaId,TimeZoneWindows,TimeZoneResult,TimezoneOptions,TimeZoneNames,TimeZoneId,TimeZoneIanaVersionResult,ReferenceTime,TimeTransition
customization-class: src/main/java/TimezoneCustomization.java
generic-response-type: true
no-custom-headers: true
```
