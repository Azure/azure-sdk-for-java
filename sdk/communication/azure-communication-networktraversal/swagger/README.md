# Azure Communication Network Traversal library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Network Traversal library, simply Install AutoRest and in this folder, run:

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
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

### Code generation settings
``` yaml
tag: package-2022-02-01
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/1824478ffd262205f5e7ea8218b1d667fe091d9d/specification/communication/data-plane/NetworkTraversal/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.networktraversal
generate-client-as-impl: true
custom-types: CommunicationIceServer,CommunicationRelayConfiguration,RouteType
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
customization-class: src/main/java/CommunicationRelayCustomization.java
```
