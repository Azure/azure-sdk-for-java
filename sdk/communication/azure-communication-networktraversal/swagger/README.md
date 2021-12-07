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
tag: package-2021-10-08-preview
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/f2e08ab373eb0e96b54920e89f9fc96d683355ca/specification/communication/data-plane/NetworkTraversal/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.networktraversal
generate-client-as-impl: true
custom-types: CommunicationIceServer,CommunicationErrorResponse,CommunicationRelayConfiguration,CommunicationRelayConfigurationRequest,CommunicationError,CommunicationErrorResponseException,RouteType
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
```
