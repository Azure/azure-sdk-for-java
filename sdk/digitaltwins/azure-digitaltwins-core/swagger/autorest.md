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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e79e929e76c8da146e561b4e1246980e336fdc00/specification/digitaltwins/data-plane/Microsoft.DigitalTwins/stable/2022-05-31/digitaltwins.json

#if you want to generate using local swagger copy:
#input-file: $(this-folder)/swagger/2022-05-31/digitaltwins.json

output-folder: ../
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.1.29'
java: true
add-context-parameter: true
namespace: com.azure.digitaltwins.core
add-credentials: true
sync-methods: none
generate-client-as-impl: true
implementation-subpackage: implementation
models-subpackage: implementation.models
context-client-method-parameter: true
custom-types-subpackage: models
required-fields-as-ctor-args: true
service-interface-as-public: true
generic-response-type: true
no-custom-headers: true
```

## This directive removes the specified enum values from the swagger so the code generator will expose IfNonMatch header as an option instead of always attaching it to requests with its only default value.

``` yaml

directive:
- from: swagger-document
  where: $..[?(@.name=='If-None-Match')]
  transform: delete $.enum;
```
