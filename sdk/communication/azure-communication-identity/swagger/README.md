# Azure Communication Identity library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Identity library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/5b19c6e69cd2bb9dbe4e5c1237b2c5a175d90ca5/specification/communication/data-plane/Microsoft.CommunicationServicesIdentity/stable/2021-03-07/CommunicationIdentity.json
add-context-parameter: true
custom-types: CommunicationIdentityTokenScope, CommunicationTokenScope
custom-types-subpackage: models
models-subpackage: implementation.models
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
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
