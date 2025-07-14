# Azure Attestation for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Attestation.

---
## Getting Started
To build the SDK for Attestation, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

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
tag: package-2020-10-01
require:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/attestation/data-plane/readme.md
java: true
use: '@autorest/java@4.1.52'
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.security.attestation
generate-client-as-impl: true
custom-types: AttestationType,PolicyModification,CertificateModification
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: none
client-side-validations: true
required-fields-as-ctor-args: true
disable-client-builder: true
```
