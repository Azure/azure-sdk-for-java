# Azure Queue Storage for Java

> see https://aka.ms/autorest

### Setup
> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
mvn install
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.QueueStorage/preview/2018-03-28/queue.json
java: true
output-folder: ../
namespace: com.azure.storage.queue
enable-xml: true
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
models-subpackage: implementation.models
custom-types: QueueErrorCode,QueueSignedIdentifier,SendMessageResult,QueueMessageItem,PeekedMessageItem,QueueItem,QueueServiceProperties,QueueServiceStatistics,QueueCorsRule,QueueAccessPolicy,QueueAnalyticsLogging,QueueMetrics,QueueRetentionPolicy,GeoReplicationStatus,GeoReplicationStatusType,GeoReplication
custom-types-subpackage: models
customization-jar-path: target/azure-storage-queue-customization-1.0.0-beta.1.jar
customization-class: com.azure.storage.queue.customization.QueueStorageCustomization
```

### Rename MessageItems
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.QueueMessageItemInternal) {
        $.QueueMessageItemInternal = $.DequeuedMessageItem;
        delete $.DequeuedMessageItem;
        $.QueueMessageItemInternal["x-az-public"] = false
        $.DequeuedMessagesList.items.$ref = $.DequeuedMessagesList.items.$ref.replace("DequeuedMessageItem", "QueueMessageItemInternal");
    }
    if (!$.PeekedMessageItemInternal) {
        $.PeekedMessageItemInternal = $.PeekedMessageItem;
        delete $.PeekedMessageItem;
        $.PeekedMessageItemInternal["x-az-public"] = false
        $.PeekedMessagesList.items.$ref = $.PeekedMessagesList.items.$ref.replace("PeekedMessageItem", "PeekedMessageItemInternal");
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

### ListQueuesSegment x-ms-pageable itemName
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/?comp=list"].get
  transform: >
    $["x-ms-pageable"].itemName = "QueueItems";
```

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-queue%2Fswagger%2FREADME.png)
