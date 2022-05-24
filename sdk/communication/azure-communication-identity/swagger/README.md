# Azure Communication Identity library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Identity library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2X
```

### Code generation settings
``` yaml
tag: package-2022-06
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/5b0818f55339dbff370a967e3f068e180c6ad5a1/specification/communication/data-plane/Identity/readme.md
add-context-parameter: true
custom-types: CommunicationTokenScope,TeamsUserExchangeTokenRequest
custom-types-subpackage: models
models-subpackage: implementation.models
customization-class: src/main/java/TeamsUserExchangeTokenRequestCustomization.java
```

### Rename CommunicationIdentityTokenScope to CommunicationTokenScope
```yaml
directive:
  - from: swagger-document
    where: $.definitions.CommunicationIdentityTokenScope
    transform: >
      $["x-ms-enum"].name = "CommunicationTokenScope";
```

### Code generation settings

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.identity
generate-client-as-impl: true
service-interface-as-public: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
