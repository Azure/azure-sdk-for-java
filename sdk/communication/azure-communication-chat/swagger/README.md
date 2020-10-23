# Azure Communication Service chat client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Chat Client, simply Install AutoRest and in this folder, run:

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

There is one swagger for Chat management APIs. 

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

## Update generated files for chat service
To update generated files for chat service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.2

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/838c5092f11e8ca26e262b1f1099d5c5cdfedc3f/specification/communication/data-plane/Microsoft.CommunicationServicesChat/preview/2020-09-21-preview2/communicationserviceschat.json
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.chat
generate-client-as-impl: true
custom-types: ChatMessagePriority,ChatThreadInfo,CreateChatThreadResult,PostReadReceiptOptions,SendChatMessageOptions,SendChatMessageResult,UpdateChatMessageOptions,UpdateChatThreadOptions,Error,ErrorException
custom-types-subpackage: models
models-subpackage: implementation.models
generate-client-interfaces: false
generate-sync-async-clients: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
enable-xml: false
required-parameter-client-methods: true
```
