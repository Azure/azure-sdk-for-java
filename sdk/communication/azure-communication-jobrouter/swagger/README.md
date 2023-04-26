# Azure Communication Service Job Router client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Job Router Client, simply Install AutoRest and in this folder, run:

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

There is one swagger for JobRouter management APIs.

```ps
cd <swagger-folder>
autorest README.md --java
```

## Update generated files for Job Router service
To update generated files for JobRouter service, run the following command

> autorest README.md --java

## Code generation settings
``` yaml
tag: package-jobrouter-2022-07-18-preview
require:
- https://raw.githubusercontent.com/williamzhao87/azure-rest-api-specs/17ac729b6e3e6fe173efccf9822e6d5d7338031b/specification/communication/data-plane/JobRouter/readme.md
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.jobrouter
custom-types-subpackage: models
generate-client-as-impl: true
service-interface-as-public: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
customization-class: src/main/java/JobRouterCustomization.java
```
