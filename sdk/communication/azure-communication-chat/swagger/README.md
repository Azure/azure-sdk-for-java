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

Before generating the code, you need to run swagger_replace.ps1 to rename the swagger classes from *Request to *Options

```ps
cd <swagger-folder>
.\swagger_replace.ps1
autorest README.md --java --v4 --use=@autorest/java@4.0.1
```

## Update generated files for chat service
To update generated files for chat service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.2

### Code generation settings
``` yaml
input-file: swagger.json
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
