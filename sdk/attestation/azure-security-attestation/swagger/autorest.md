# Microsoft.Azure.Attestation

## Local copy of the swagger document

A local copy of the official swagger documents are stored in this directory for convenience and testing purposes. Please make sure that you do not use these swagger documents for official code generation purposes.

## Official swagger document

The official swagger specification for Microsoft Azure Attestation can be found [here](https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/attestation/data-plane/Microsoft.Attestation/stable/2020-10-01/attestation.json).

## Code generation

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
#When generating from the official specifications repository
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/attestation/data-plane/Microsoft.Attestation/stable/2020-10-01/attestation.json

output-folder: "../"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.4'
java:
    add-context-parameter: true
    namespace: com.azure.security.attestation
    add-credentials: true
    sync-methods: all
    client-side-validations: true
#    generate-client-as-impl: true
#    implementation-subpackage: implementation
#    models-subpackage: implementation.models
    context-client-method-parameter: true
    custom-types-subpackage: models
    generate-sync-async-clients: true
    required-fields-as-ctor-args: true
    v3: true
```

## This directive removes the specified enum values from the swagger so the code generator will expose IfNonMatch header as an option instead of always attaching it to requests with its only default value.
``` yaml

#directive:
#- from: swagger-document
#  where: $..[?(@.name=='If-None-Match')]
#  transform: delete $.enum;

```
