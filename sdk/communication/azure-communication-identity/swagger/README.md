# Azure Communication Identity library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Identity library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal -DskipTests
npm install
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
tag: package-2022-10
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/49e2859d9eef95013f083af9506127cfffd1e866/specification/communication/data-plane/Identity/readme.md
add-context-parameter: true
custom-types: CommunicationTokenScope,TeamsUserExchangeTokenRequest
custom-types-subpackage: models
models-subpackage: implementation.models
customization-class: src/main/java/TeamsUserExchangeTokenRequestCustomization.java
custom-strongly-typed-header-deserialization: true
generic-response-type: true
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
