# Azure Communication Media Composition library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Media Composition library, simply Install AutoRest and in this folder, run:

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
tag: package-preview-2021-10
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/53608dc04b81e7123542747d5b9a4f6110d98c5d/specification/communication/data-plane/MediaComposition/preview/2021-12-31-preview/CommunicationMediaComposition.json
add-context-parameter: true
custom-types-subpackage: models
models-subpackage: implementation.models
```

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.mediacomposition
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
