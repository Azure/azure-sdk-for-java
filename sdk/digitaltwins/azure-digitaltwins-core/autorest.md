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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/digitaltwins/data-plane/Microsoft.DigitalTwins/preview/2020-05-31-preview/digitaltwins.json

#When generating from the local copy:
#input-file: 2020-05-31-preview/digitaltwins.json

output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.2'
java:
    add-context-parameter: true
    namespace: com.azure.digitaltwins.core
    add-credentials: true
    sync-methods: none
    generate-client-as-impl: true
    implementation-subpackage: implementation
    models-subpackage: implementation.models
    custom-types-subpackage: models
    custom-types: ModelData
    context-client-method-parameter: true
```
