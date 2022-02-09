# Azure.DigitalTwins.Core

## Local copy of the swagger document

A local copy of the official swagger documents are stored in this directory for convenience and testing purposes. Please make sure that you do not use these swagger documents for official code generation purposes.

## Official swagger document

The official swagger specification for Azure DigitalTwins can be found [here](https://github.com/Azure/azure-rest-api-specs/tree/master/specification/digitaltwins/data-plane/Microsoft.DigitalTwins).

## Code generation

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
#When generating from the official specifications repository
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/14fb40342c19f8b483e132038f8424ee62b745d9/specification/digitaltwins/data-plane/Microsoft.DigitalTwins/stable/2020-10-31/digitaltwins.json

#if you want to generate using local swagger copy:
#input-file: $(this-folder)/swagger/2020-`0-31/digitaltwins.json

output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.4'
java:
    add-context-parameter: true
    namespace: com.azure.digitaltwins.core
    add-credentials: true
    sync-methods: none
    client-side-validations: true
    generate-client-as-impl: true
    implementation-subpackage: implementation
    models-subpackage: implementation.models
    context-client-method-parameter: true
    custom-types-subpackage: models
    required-fields-as-ctor-args: true
    service-interface-as-public: true
```

## This directive removes the specified enum values from the swagger so the code generator will expose IfNonMatch header as an option instead of always attaching it to requests with its only default value.
``` yaml

directive:
- from: swagger-document
  where: $..[?(@.name=='If-None-Match')]
  transform: delete $.enum;

```
