# Azure IoT Digital Twins for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for IoT Digital Twins.
---
## Getting Started

To build the SDK for IoT Digital Twins, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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

``` yaml
#When generating from the official specifications repository
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/764484f6d4d2eeace159a19d3df364abc0645c7e/specification/digitaltwins/data-plane/Microsoft.DigitalTwins/stable/2023-10-31/digitaltwins.json

#if you want to generate using local swagger copy:
#input-file: $(this-folder)/swagger/2023-10-31/digitaltwins.json

output-folder: ../
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.1.52'
java: true
namespace: com.azure.digitaltwins.core
add-credentials: true
sync-methods: none
generate-client-as-impl: true
implementation-subpackage: implementation
models-subpackage: implementation.models
custom-types-subpackage: models
required-fields-as-ctor-args: true
customization-class: src/main/java/DigitalTwinsCustomization.java
```

## This directive removes the specified enum values from the swagger so the code generator will expose IfNonMatch header as an option instead of always attaching it to requests with its only default value.

``` yaml

directive:
- from: swagger-document
  where: $..[?(@.name=='If-None-Match')]
  transform: delete $.enum;
```
