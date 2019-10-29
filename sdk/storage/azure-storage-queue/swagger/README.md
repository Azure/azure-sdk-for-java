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
models-subpackage: implementation.models
custom-types: QueueErrorCode,QueueSignedIdentifier,SendMessageResult,QueueMessageItem,PeekedMessageItem,QueueItem,QueueServiceProperties,QueueServiceStatistics,QueueCorsRule,QueueAccessPolicy,QueueAnalyticsLogging,QueueMetrics,QueueRetentionPolicy,GeoReplicationStatus,GeoReplicationStatusType
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
    if (!$.QueueMessageItem) {
        $.QueueMessageItem = $.DequeuedMessageItem;
        delete $.DequeuedMessageItem;
        $.DequeuedMessagesList.items.$ref = $.DequeuedMessagesList.items.$ref.replace("DequeuedMessageItem", "QueueMessageItem");
    }
    if (!$.SendMessageResult) {
        $.SendMessageResult = $.EnqueuedMessage;
        delete $.EnqueuedMessage;
        $.EnqueuedMessageList.items.$ref = $.EnqueuedMessageList.items.$ref.replace("EnqueuedMessage", "SendMessageResult");
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

### GeoReplication
``` yaml
directive:
- from: swagger-document
  where: $.definitions.GeoReplication.properties.Status
  transform: >
    $["x-ms-enum"].name = "GeoReplicationStatus";
```

### QueueErrorCode
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ErrorCode
  transform: >
    $["x-ms-enum"].name = "QueueErrorCode";
```

### QueueServiceProperties, QueueAnalyticsLogging, QueueMetrics, QueueCorsRule, and QueueRetentionPolicy
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.QueueServiceProperties) {
        $.QueueServiceProperties = $.StorageServiceProperties;
        delete $.StorageServiceProperties;
        $.QueueServiceProperties.xml = { "name": "StorageServiceProperties" };
    }
    if (!$.QueueAnalyticsLogging) {
      $.QueueAnalyticsLogging = $.Logging;
      delete $.Logging;
      $.QueueAnalyticsLogging.xml = {"name": "Logging"};
      $.QueueServiceProperties.properties.Logging["$ref"] = "#/definitions/QueueAnalyticsLogging";
      $.QueueServiceProperties.properties.Logging["x-ms-client-name"] = "analyticsLogging";
    }
    if (!$.QueueMetrics) {
      $.QueueMetrics = $.Metrics;
      delete $.Metrics;
      $.QueueMetrics.xml = {"name": "Metrics"};
      $.QueueMetrics.properties.IncludeApis = $.QueueMetrics.properties.IncludeAPIs;
      delete $.QueueMetrics.properties.IncludeAPIs;
      $.QueueMetrics.properties.IncludeApis.xml = {"name": "IncludeAPIs"};
      $.QueueServiceProperties.properties.HourMetrics["$ref"] = "#/definitions/QueueMetrics";
      $.QueueServiceProperties.properties.MinuteMetrics["$ref"] = "#/definitions/QueueMetrics";
    }
    if (!$.QueueCorsRule) {
      $.QueueCorsRule = $.CorsRule;
      delete $.CorsRule;
      $.QueueCorsRule.xml = {"name": "CorsRule"};
      $.QueueServiceProperties.properties.Cors.items["$ref"] = "#/definitions/QueueCorsRule";
    }
    if (!$.QueueRetentionPolicy) {
      $.QueueRetentionPolicy = $.RetentionPolicy;
      delete $.RetentionPolicy;
      $.QueueRetentionPolicy.xml = {"name": "RetentionPolicy"};
      $.QueueAnalyticsLogging.properties.RetentionPolicy["$ref"] = "#/definitions/QueueRetentionPolicy";
      $.QueueMetrics.properties.RetentionPolicy["$ref"] = "#/definitions/QueueRetentionPolicy";
    }
- from: swagger-document
  where: $.parameters
  transform: >
    if (!$.QueueServiceProperties) {
        const props = $.QueueServiceProperties = $.StorageServiceProperties;
        props.name = "QueueServiceProperties";
        props["x-ms-client-name"] = "properties";
        props.schema = { "$ref": props.schema.$ref.replace(/[#].*$/, "#/definitions/QueueServiceProperties") };
        delete $.StorageServiceProperties;
    }
- from: swagger-document
  where: $["x-ms-paths"]["/?restype=service&comp=properties"]
  transform: >
    const param = $.put.parameters[0];
    if (param && param["$ref"] && param["$ref"].endsWith("StorageServiceProperties")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/QueueServiceProperties");
        $.put.parameters[0] = { "$ref": path };
    }
    const def = $.get.responses["200"].schema;
    if (def && def["$ref"] && def["$ref"].endsWith("StorageServiceProperties")) {
        const path = def["$ref"].replace(/[#].*$/, "#/definitions/QueueServiceProperties");
        $.get.responses["200"].schema = { "$ref": path };
    }
```

### QueueServiceStatistics
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.QueueServiceStatistics) {
        $.QueueServiceStatistics = $.StorageServiceStats;
        delete $.StorageServiceStats;
        $.QueueServiceStatistics.xml = { "name": "StorageServiceStats" }
        $.QueueServiceStatistics.description = "Statistics for the storage service.";
    }
- from: swagger-document
  where: $["x-ms-paths"]["/?restype=service&comp=stats"].get.responses["200"]
  transform: >
    if ($.schema && $.schema.$ref && $.schema.$ref.endsWith("StorageServiceStats")) {
        const path = $.schema.$ref.replace(/[#].*$/, "#/definitions/QueueServiceStatistics");
        $.schema = { "$ref": path };
    }
```

### QueueAccessPolicy and QueueSignedIdentifier
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.QueueSignedIdentifier) {
      $.QueueSignedIdentifier = $.SignedIdentifier;
      delete $.SignedIdentifier;
      $.QueueSignedIdentifier.xml = {"name": "SignedIdentifier"};
      $.SignedIdentifiers.items["$ref"] = "#/definitions/QueueSignedIdentifier";
    }
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.QueueAccessPolicy) {
      $.QueueAccessPolicy = $.AccessPolicy;
      delete $.AccessPolicy;
      $.QueueAccessPolicy.xml = {"name": "AccessPolicy"};
      $.QueueAccessPolicy.properties.StartsOn = $.QueueAccessPolicy.properties.Start;
      $.QueueAccessPolicy.properties.StartsOn.xml = {"name": "Start"};
      delete $.QueueAccessPolicy.properties.Start;
      $.QueueAccessPolicy.properties.ExpiresOn = $.QueueAccessPolicy.properties.Expiry;
      $.QueueAccessPolicy.properties.ExpiresOn.xml = {"name": "Expiry"};
      delete $.QueueAccessPolicy.properties.Expiry;
      $.QueueAccessPolicy.properties.Permissions = $.QueueAccessPolicy.properties.Permission;
      $.QueueAccessPolicy.properties.Permissions.xml = {"name": "Permission"};
      delete $.QueueAccessPolicy.properties.Permission;
    }
    $.QueueSignedIdentifier.properties.AccessPolicy["$ref"] = "#/definitions/QueueAccessPolicy";
```

### QueueServiceProperties Annotation Fix
``` yaml
directive:
- from: QueueServiceProperties.java
  where: $
  transform: >
    return $.replace('@JsonProperty(value = "Metrics")\n    private QueueMetrics hourMetrics;', '@JsonProperty(value = "HourMetrics")\n    private QueueMetrics hourMetrics;').
      replace('@JsonProperty(value = "Metrics")\n    private QueueMetrics minuteMetrics;', '@JsonProperty(value = "MinuteMetrics")\n    private QueueMetrics minuteMetrics;');
```

### Change StorageErrorException to StorageException
``` yaml
directive:
- from: ServicesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.queue.implementation.models.StorageErrorException",
        "com.azure.storage.queue.models.QueueStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(QueueStorageException.class)"
      );
- from: QueuesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.queue.implementation.models.StorageErrorException",
        "com.azure.storage.queue.models.QueueStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(QueueStorageException.class)"
      );
- from: MessagesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.queue.implementation.models.StorageErrorException",
        "com.azure.storage.queue.models.QueueStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(QueueStorageException.class)"
      );
- from: MessageIdsImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.queue.implementation.models.StorageErrorException",
        "com.azure.storage.queue.models.QueueStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(QueueStorageException.class)"
      );
```


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-queue%2Fswagger%2FREADME.png)
