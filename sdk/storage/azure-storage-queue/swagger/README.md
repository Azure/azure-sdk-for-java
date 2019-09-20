# Azure Queue Storage for Java

> see https://aka.ms/autorest

### Setup
```ps
cd C:\work
git clone --recursive https://github.com/Azure/autorest.java/
cd autorest.java
git checkout v3
npm install
cd ..
git clone --recursive https://github.com/jianghaolu/autorest.modeler/
cd autorest.modeler
git checkout headerprefixfix
npm install
```

### Generation
```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java --use=C:/work/autorest.modeler --version=2.0.4280
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.QueueStorage/preview/2018-03-28/queue.json
java: true
output-folder: ../
namespace: com.azure.storage.queue
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation
custom-types: StorageError,StorageErrorException,StorageErrorCode,SignedIdentifier,EnqueuedMessage,DequeuedMessage,PeekedMessage,QueueMessage,QueueItem,StorageServiceProperties,StorageServiceStats,CorsRule,AccessPolicy,Logging,Metrics,RetentionPolicy
custom-types-subpackage: models
```

### /{queueName}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.delete.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}?comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}?comp=metadata"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}?comp=acl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}?comp=acl"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}/messages
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}/messages"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.get.parameters.splice(0, 0, { "$ref": path });
        $.delete.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}/messages?visibilitytimeout={visibilityTimeout}&messagettl={messageTimeToLive}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}/messages?visibilitytimeout={visibilityTimeout}&messagettl={messageTimeToLive}"]
  transform: >
    let param = $.post.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.post.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}/messages?peekonly=true
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}/messages?peekonly=true"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{queueName}/messages/{messageid}?popreceipt={popReceipt}&visibilitytimeout={visibilityTimeout}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}/messages/{messageid}?popreceipt={popReceipt}&visibilitytimeout={visibilityTimeout}"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const queueNamePath = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        const messageIdPath = param["$ref"].replace(/[#].*$/, "#/parameters/MessageId");
        $.put.parameters.splice(0, 0, { "$ref": queueNamePath });
        $.put.parameters.splice(1, 0, { "$ref": messageIdPath });
    }
```

### /{queueName}/messages/{messageid}?popreceipt={popReceipt}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{queueName}/messages/{messageid}?popreceipt={popReceipt}"]
  transform: >
    let param = $.delete.parameters[0];
    if (!param["$ref"].endsWith("QueueName")) {
        const queueNamePath = param["$ref"].replace(/[#].*$/, "#/parameters/QueueName");
        const messageIdPath = param["$ref"].replace(/[#].*$/, "#/parameters/MessageId");
        $.delete.parameters.splice(0, 0, { "$ref": queueNamePath });
        $.delete.parameters.splice(1, 0, { "$ref": messageIdPath });
    }
```

### Rename MessageItems
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.DequeuedMessage) {
        $.DequeuedMessage = $.DequeuedMessageItem;
        delete $.DequeuedMessageItem;
        $.DequeuedMessagesList.items.$ref = $.DequeuedMessagesList.items.$ref.replace("DequeuedMessageItem", "DequeuedMessage");
    }
    if (!$.PeekedMessage) {
        $.PeekedMessage = $.PeekedMessageItem;
        delete $.PeekedMessageItem;
        $.PeekedMessagesList.items.$ref = $.PeekedMessagesList.items.$ref.replace("PeekedMessageItem", "PeekedMessage");
    }
```

### MessageId
``` yaml
directive:
- from: swagger-document
  where: $.parameters.MessageId
  transform: >
    $.description = "The message ID name.";
```
