# Azure Queue Storage for Java

> see https://aka.ms/autorest

### Setup
> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
autorest
```

### Code generation settings
``` yaml
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/f031b8ef2830772c5c33d1768886551f7e538b7c/specification/storage/data-plane/Microsoft.QueueStorage/stable/2018-03-28/queue.json
java: true
output-folder: ../
namespace: com.azure.storage.queue
generate-client-as-impl: true
license-header: MICROSOFT_MIT_SMALL
enable-sync-stack: true
default-http-exception-type: com.azure.storage.queue.implementation.models.QueueStorageExceptionInternal
models-subpackage: implementation.models
custom-types: QueueErrorCode,QueueSignedIdentifier,SendMessageResult,QueueMessageItem,PeekedMessageItem,QueueItem,QueueServiceProperties,QueueServiceStatistics,QueueCorsRule,QueueAccessPolicy,QueueAnalyticsLogging,QueueMetrics,QueueRetentionPolicy,GeoReplicationStatus,GeoReplicationStatusType,GeoReplication
custom-types-subpackage: models
customization-class: src/main/java/QueueStorageCustomization.java
use-input-stream-for-binary: true
disable-client-builder: true
```

### Rename MessageItems
``` yaml
directive:
- rename-model:
    from: DequeuedMessageItem
    to: QueueMessageItemInternal
- rename-model:
    from: PeekedMessageItem
    to: PeekedMessageItemInternal
- rename-model:
    from: EnqueuedMessage
    to: SendMessageResult
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
- rename-model:
    from: Logging
    to: QueueAnalyticsLogging
- rename-model:
    from: Metrics
    to: QueueMetrics
- rename-model:
    from: CorsRule
    to: QueueCorsRule
- rename-model:
    from: RetentionPolicy
    to: QueueRetentionPolicy
- rename-model:
    from: StorageServiceProperties
    to: QueueServiceProperties
      
- from: swagger-document
  where: $.definitions
  transform: >
    $.QueueServiceProperties.properties.Logging["x-ms-client-name"] = "analyticsLogging";
    $.QueueMetrics.properties.IncludeAPIs["x-ms-client-name"] = "IncludeApis";
    $.QueueServiceProperties.xml = {"name": "StorageServiceProperties"};
    $.QueueCorsRule.xml = {"name": "CorsRule"};

- from: swagger-document
  where: $.parameters
  transform: >
    $.StorageServiceProperties.name = "QueueServiceProperties";
```

### QueueServiceStatistics
``` yaml
directive:
- rename-model:
    from: StorageServiceStats
    to: QueueServiceStatistics
```

### QueueAccessPolicy and QueueSignedIdentifier
``` yaml
directive:
- rename-model:
    from: SignedIdentifier
    to: QueueSignedIdentifier
- rename-model:
    from: AccessPolicy
    to: QueueAccessPolicy

- from: swagger-document
  where: $.definitions
  transform: >
    $.QueueSignedIdentifier.xml = {"name": "SignedIdentifier"};
    $.QueueAccessPolicy.properties.Start["x-ms-client-name"] = "StartsOn";
    $.QueueAccessPolicy.properties.Expiry["x-ms-client-name"] = "ExpiresOn";
    $.QueueAccessPolicy.properties.Permission["x-ms-client-name"] = "Permissions";
```

### ListQueuesSegment x-ms-pageable itemName
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/?comp=list"].get
  transform: >
    $["x-ms-pageable"].itemName = "QueueItems";
```


