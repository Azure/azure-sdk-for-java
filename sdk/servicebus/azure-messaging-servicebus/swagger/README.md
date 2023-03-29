# Azure Service Bus Management APIs for Java

> see https://aka.ms/autorest

## Getting Started

To build the SDK for ServiceBusAdministrationClient and ServiceBusAdministrationAsyncClient, simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/install/readme.md) and in this folder, run:

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

There is one swagger for Service Bus management APIs.

```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
use: ['@autorest/java@4.1.16', '@autorest/modelerfour@4.25.0']
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0f7b134efb524de6aadc6965d4e8fd2a78929989/specification/servicebus/data-plane/Microsoft.ServiceBus/stable/2021-05/servicebus.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.messaging.servicebus.administration
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: essential
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: AccessRights,EntityStatus,NamespaceProperties,MessagingSku,NamespaceType
custom-types-subpackage: models
context-client-method-parameter: true
customization-class: src/main/java/AdministrationClientCustomization.java
enable-xml: true
enable-sync-stack: true
```
