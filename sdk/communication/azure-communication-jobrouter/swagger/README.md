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
autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442
```

## Update generated files for Job Router service
To update generated files for JobRouter service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442

## Code generation settings
``` yaml
tag: package-jobrouter-2021-10-20
input-file: https://github.com/Azure/azure-rest-api-specs/blob/240e5a3289b8c698a1ffa1f1a3a86e7637199411/specification/communication/data-plane/JobRouter/preview/2022-07-18-preview/communicationservicejobrouter.json
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.jobrouter
generate-client-as-impl: true
custom-types: CreateClassificationPolicyOptions
custom-types-subpackage: models
models-subpackage: implementation.models
customization-class: src/main/java/JobRouterCustomization.java
generate-client-interfaces: false
generate-sync-async-clients: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
enable-xml: false
required-parameter-client-methods: true
```
