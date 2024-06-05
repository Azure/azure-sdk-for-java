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
tag: package-2020-10-01
require:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/attestation/data-plane/readme.md
java: true
use: '@autorest/java@4.1.27'
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.security.attestation
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
custom-types: AttestationType,PolicyModification,CertificateModification
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: none
client-side-validations: true
context-client-method-parameter: true
required-fields-as-ctor-args: true
disable-client-builder: true
stream-style-serialization: true

#add-credentials: false
#credential-types: tokencredential
#credential-scopes: 'https://attest.azure.net/.default'

#required-parameter-client-methods: true

```
