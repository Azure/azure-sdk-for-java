# Azure Communication Identity library for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Communication Identity
---
## Getting Started

To build the SDK for Communication Identity library, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:
> `autorest --java`

To see additional help and options, run:
> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java
```

### Code generation settings
``` yaml
java: true
output-folder: ..\
use: '@autorest/java@4.1.14'
tag: package-2022-10
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/a8c4340400f1ab1ae6a43b10e8d635ecb9c49a2a/specification/communication/data-plane/Identity/readme.md
add-context-parameter: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.identity
custom-types: CommunicationTokenScope,TeamsUserExchangeTokenRequest
custom-types-subpackage: models
models-subpackage: implementation.models
customization-class: src/main/java/TeamsUserExchangeTokenRequestCustomization.java
custom-strongly-typed-header-deserialization: true
generic-response-type: true
sync-methods: all
disable-client-builder: true
generate-client-as-impl: true
service-interface-as-public: true
context-client-method-parameter: true
```

### Rename CommunicationIdentityTokenScope to CommunicationTokenScope
```yaml
directive:
  - from: swagger-document
    where: $.definitions.CommunicationIdentityTokenScope
    transform: >
      $["x-ms-enum"].name = "CommunicationTokenScope";
```
