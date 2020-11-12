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

### Rename AddChatThreadMembersRequest to AddChatThreadMembersOptions
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.AddChatThreadMembersOptions) {
      $.AddChatThreadMembersOptions = $.AddChatThreadMembersRequest;
      delete $.AddChatThreadMembersRequest;
    }
- from: swagger-document
  where: $["paths"]["/chat/threads/{chatThreadId}/members"].post.parameters[2]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("AddChatThreadMembersRequest")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/AddChatThreadMembersOptions");
        $.schema = { "$ref": path };
    }
```

### Rename CreateChatThreadRequest to CreateChatThreadOptions
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.CreateChatThreadOptions) {
      $.CreateChatThreadOptions = $.CreateChatThreadRequest;
      delete $.CreateChatThreadRequest;
    }
- from: swagger-document
  where: $["paths"]["/chat/threads"].post.parameters[1]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("CreateChatThreadRequest")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/CreateChatThreadOptions");
        $.schema = { "$ref": path };
    }
```

### Rename SendChatMessageRequest to SendChatMessageOptions
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.SendChatMessageOptions) {
      $.SendChatMessageOptions = $.SendChatMessageRequest;
      delete $.SendChatMessageRequest;
    }
- from: swagger-document
  where: $["paths"]["/chat/threads/{chatThreadId}/messages"].post.parameters[2]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("SendChatMessageRequest")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/SendChatMessageOptions");
        $.schema = { "$ref": path };
    }
```

### Rename UpdateChatMessageRequest to UpdateChatMessageOptions
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.UpdateChatMessageOptions) {
      $.UpdateChatMessageOptions = $.UpdateChatMessageRequest;
      delete $.UpdateChatMessageRequest;
    }
- from: swagger-document
  where: $["paths"]["/chat/threads/{chatThreadId}/messages/{chatMessageId}"].patch.parameters[3]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("UpdateChatMessageRequest")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/UpdateChatMessageOptions");
        $.schema = { "$ref": path };
    }
```

### Rename UpdateChatThreadRequest to UpdateChatThreadOptions
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.UpdateChatThreadOptions) {
      $.UpdateChatThreadOptions = $.UpdateChatThreadRequest;
      delete $.UpdateChatThreadRequest;
    }
- from: swagger-document
  where: $["paths"]["/chat/threads/{chatThreadId}"].patch.parameters[2]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("UpdateChatThreadRequest")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/UpdateChatThreadOptions");
        $.schema = { "$ref": path };
    }
```