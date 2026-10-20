# Release History

## 1.2.0-beta.1 (2026-10-20)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK.  Package api-version 2026-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.TaskQueueThroughput` was added

* `models.DetectorDataset` was added

* `models.TransparentDataEncryptionKeySource` was added

* `models.TaskQueue$Definition` was added

* `models.DetectorStatusId` was added

* `models.DetectorMetadata` was added

* `models.TaskQueue$UpdateStages` was added

* `models.DetectorTableColumn` was added

* `models.UserAssignedIdentity` was added

* `models.TaskQueue` was added

* `models.ManagedServiceIdentity` was added

* `models.DetectorRenderingProperties` was added

* `models.DetectorStatus` was added

* `models.ReplicationRole` was added

* `models.FailoverState` was added

* `models.TaskQueueUpdate` was added

* `models.TaskQueues` was added

* `models.FailoverInstanceProperties` was added

* `models.FailoverInstances` was added

* `models.DetectorType` was added

* `models.TransparentDataEncryptionProperties` was added

* `models.Detectors` was added

* `models.DetectorProperties` was added

* `models.TransparentDataEncryption` was added

* `models.TaskQueue$DefinitionStages` was added

* `models.TransparentDataEncryptions` was added

* `models.ManagedServiceIdentityType` was added

* `models.TaskQueue$Update` was added

* `models.FailoverInstance` was added

* `models.TaskQueueProperties` was added

* `models.DetectorRenderingType` was added

* `models.DetectorTable` was added

* `models.Detector` was added

#### `models.Scheduler$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Schedulers` was modified

* `restart(java.lang.String,java.lang.String)` was added
* `restartWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `DurableTaskManager` was modified

* `taskQueues()` was added
* `detectors()` was added
* `failoverInstances()` was added
* `transparentDataEncryptions()` was added

#### `models.Scheduler` was modified

* `restart()` was added
* `identity()` was added
* `restartWithResponse(com.azure.core.util.Context)` was added

#### `models.Scheduler$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.TaskHubProperties` was modified

* `withCapabilities(java.util.List)` was added
* `capabilities()` was added

#### `models.SchedulerUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

## 1.1.0 (2026-03-09)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK.  Package api-version 2026-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointConnectionProperties` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.SchedulerPrivateLinkResource` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.PrivateLinkResourceProperties` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnectionUpdate` was added

* `models.OptionalPropertiesUpdateableProperties` was added

#### `models.Schedulers` was modified

* `listPrivateLinks(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deletePrivateEndpointConnectionById(java.lang.String)` was added
* `listPrivateLinks(java.lang.String,java.lang.String)` was added
* `listPrivateEndpointConnections(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deletePrivateEndpointConnection(java.lang.String,java.lang.String,java.lang.String)` was added
* `deletePrivateEndpointConnection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getPrivateEndpointConnectionById(java.lang.String)` was added
* `getPrivateEndpointConnection(java.lang.String,java.lang.String,java.lang.String)` was added
* `getPrivateEndpointConnectionByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deletePrivateEndpointConnectionByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getPrivateLinkWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getPrivateEndpointConnectionWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listPrivateEndpointConnections(java.lang.String,java.lang.String)` was added
* `getPrivateLink(java.lang.String,java.lang.String,java.lang.String)` was added
* `definePrivateEndpointConnection(java.lang.String)` was added

#### `models.SchedulerPropertiesUpdate` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `publicNetworkAccess()` was added

#### `models.SchedulerProperties` was modified

* `publicNetworkAccess()` was added
* `privateEndpointConnections()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

## 1.0.0 (2025-09-25)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK.  Package api-version 2025-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SchedulerSku` was modified

* `validate()` was removed
* `java.lang.String name()` -> `models.SchedulerSkuName name()`
* `withName(java.lang.String)` was removed

#### `models.RetentionPolicyDetails` was modified

* `validate()` was removed

#### `models.SchedulerPropertiesUpdate` was modified

* `validate()` was removed

#### `models.RetentionPolicyProperties` was modified

* `validate()` was removed

#### `models.SchedulerProperties` was modified

* `validate()` was removed

#### `models.TaskHubProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.SchedulerSkuUpdate` was modified

* `validate()` was removed
* `withName(java.lang.String)` was removed
* `java.lang.String name()` -> `models.SchedulerSkuName name()`

#### `models.SchedulerUpdate` was modified

* `validate()` was removed

### Features Added

* `models.SchedulerSkuName` was added

#### `models.SchedulerSku` was modified

* `withName(models.SchedulerSkuName)` was added

#### `models.SchedulerSkuUpdate` was modified

* `withName(models.SchedulerSkuName)` was added

## 1.0.0-beta.2 (2025-04-24)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.RetentionPolicyProperties` was added

* `models.RetentionPolicies` was added

* `models.RetentionPolicyDetails` was added

* `implementation.models.RetentionPolicyListResult` was added

* `models.RetentionPolicy` was added

* `models.PurgeableOrchestrationState` was added

#### `DurableTaskManager` was modified

* `retentionPolicies()` was added

## 1.0.0-beta.1 (2025-03-25)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-durabletask Java SDK.
