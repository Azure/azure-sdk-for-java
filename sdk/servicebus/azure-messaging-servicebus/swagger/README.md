# Azure Service Bus Management APIs for Java

> see https://aka.ms/autorest

## Getting Started

To build the SDK for ServiceBusManagementClient and ServiceBusManagementClient, simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/installing-autorest.md) and in this folder, run:

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

There is one swagger for Service Bus management APIs.

```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
input-file: E:\git\conniey\azure-rest-api-specs-pr\specification\servicebus\data-plane\servicebus-swagger.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.messaging.servicebus.administration
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: AccessRights,AuthorizationRule,EntityStatus,NamespaceProperties,MessagingSku
custom-types-subpackage: models
context-client-method-parameter: true
enable-xml: true
```
