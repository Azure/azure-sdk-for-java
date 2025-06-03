# Release History

## 1.0.0-beta.3 (2025-06-03)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package api-version 2024-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceProviders` was removed

#### `models.ReplicationExtensionModelSystemData` was removed

#### `models.WorkflowOperationStatus` was removed

#### `models.DraModelCustomProperties` was removed

#### `models.AzStackHciFabricModelCustomProperties` was removed

#### `models.ProtectedItemModelPropertiesLastTestFailoverJob` was removed

#### `models.RecoveryPointModelSystemData` was removed

#### `models.VaultModelSystemData` was removed

#### `models.VMwareToAzStackHciPolicyModelCustomProperties` was removed

#### `models.HyperVToAzStackHciEventModelCustomProperties` was removed

#### `models.ReplicationExtensionModelCollection` was removed

#### `models.FailoverWorkflowModelCustomProperties` was removed

#### `models.EmailConfigurationModelSystemData` was removed

#### `models.HyperVToAzStackHciDiskInput` was removed

#### `models.VaultModelCollection` was removed

#### `models.ProtectedItemOperationStatus` was removed

#### `models.OperationListResult` was removed

#### `models.PolicyModelCollection` was removed

#### `models.DraModelProperties` was removed

#### `models.VMwareToAzStackHciProtectedNicProperties` was removed

#### `models.ReplicationExtensionModel` was removed

#### `models.TestFailoverWorkflowModelCustomProperties` was removed

#### `models.ProtectedItemModelPropertiesCurrentJob` was removed

#### `models.RecoveryPointModelCollection` was removed

#### `models.SystemDataModel` was removed

#### `models.PolicyModelSystemData` was removed

#### `models.PolicyOperationStatus` was removed

#### `models.VaultModelUpdateSystemData` was removed

#### `models.HyperVToAzStackHciRepExtnCustomProps` was removed

#### `models.WorkflowModelCollection` was removed

#### `models.ReplicationExtensionModel$DefinitionStages` was removed

#### `models.WorkflowModelSystemData` was removed

#### `models.VMwareDraModelCustomProperties` was removed

#### `models.LastFailedEnableProtectionJob` was removed

#### `models.DraModel$DefinitionStages` was removed

#### `models.DraModel$Definition` was removed

#### `models.VMwareToAzStackHciProtectedDiskProperties` was removed

#### `models.DraModel` was removed

#### `models.WorkflowModelCustomProperties` was removed

#### `models.VMwareToAzStackHciNicInput` was removed

#### `models.WorkflowModelProperties` was removed

#### `models.FabricOperationsStatus` was removed

#### `models.ReplicationExtensionModel$Definition` was removed

#### `models.Dras` was removed

#### `models.EmailConfigurationModelCollection` was removed

#### `models.ProtectedItemModelSystemData` was removed

#### `models.AzStackHciClusterProperties` was removed

#### `models.ProtectedItemModelCollection` was removed

#### `models.EventModelCollection` was removed

#### `models.LastFailedPlannedFailoverJob` was removed

#### `models.VaultOperationStatus` was removed

#### `models.EventModelSystemData` was removed

#### `models.WorkflowObjectType` was removed

#### `models.FabricModelCollection` was removed

#### `models.HyperVToAzStackHciProtectedNicProperties` was removed

#### `models.FabricModelUpdateSystemData` was removed

#### `models.WorkflowState` was removed

#### `models.HyperVToAzStackHciProtectedDiskProperties` was removed

#### `models.TestFailoverCleanupWorkflowModelCustomProperties` was removed

#### `models.DraOperationStatus` was removed

#### `models.DraModelSystemData` was removed

#### `models.VMwareToAzStackHciDiskInput` was removed

#### `models.HyperVToAzStackHciNicInput` was removed

#### `models.HyperVToAzStackHciPolicyModelCustomProperties` was removed

#### `models.Workflows` was removed

#### `models.FabricModelSystemData` was removed

#### `models.WorkflowModel` was removed

#### `models.OperationStatus` was removed

#### `models.DraModelCollection` was removed

#### `models.TaskModel` was modified

* `withChildrenWorkflows(java.util.List)` was removed
* `withCustomProperties(models.TaskModelCustomProperties)` was removed
* `childrenWorkflows()` was removed

#### `models.ReplicationExtensions` was modified

* `models.ReplicationExtensionModel$DefinitionStages$Blank define(java.lang.String)` -> `models.ReplicationExtension$DefinitionStages$Blank define(java.lang.String)`
* `models.ReplicationExtensionModel get(java.lang.String,java.lang.String,java.lang.String)` -> `models.ReplicationExtension get(java.lang.String,java.lang.String,java.lang.String)`
* `models.ReplicationExtensionModel getById(java.lang.String)` -> `models.ReplicationExtension getById(java.lang.String)`

#### `models.ProtectedItems` was modified

* `plannedFailover(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.RecoveryPointModelProperties` was modified

* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `withCustomProperties(models.RecoveryPointModelCustomProperties)` was removed
* `withRecoveryPointType(models.RecoveryPointType)` was removed

#### `models.FabricModelUpdate` was modified

* `models.FabricModelUpdateSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.VMwareToAzStackHciProtectedItemCustomProps` was modified

* `withSourceDraName(java.lang.String)` was removed
* `sourceDraName()` was removed
* `withTargetDraName(java.lang.String)` was removed
* `targetDraName()` was removed

#### `models.Fabrics` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PolicyModel` was modified

* `models.PolicyModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.HealthErrorModel` was modified

* `withAffectedResourceType(java.lang.String)` was removed
* `withAffectedResourceCorrelationIds(java.util.List)` was removed
* `withChildErrors(java.util.List)` was removed

#### `models.HyperVToAzStackHciProtectedItemCustomProps` was modified

* `sourceDraName()` was removed
* `withTargetDraName(java.lang.String)` was removed
* `withSourceDraName(java.lang.String)` was removed
* `targetDraName()` was removed

#### `models.ProtectedItemModel` was modified

* `models.ProtectedItemModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `plannedFailover()` was removed

#### `models.EventModel` was modified

* `models.EventModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.VaultModel` was modified

* `models.VaultModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.FabricModel` was modified

* `models.FabricModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.EventModelProperties` was modified

* `withCustomProperties(models.EventModelCustomProperties)` was removed

#### `models.ProtectedItemModelProperties` was modified

* `models.ProtectedItemModelPropertiesCurrentJob currentJob()` -> `models.ProtectedItemJobProperties currentJob()`
* `models.LastFailedPlannedFailoverJob lastFailedPlannedFailoverJob()` -> `models.ProtectedItemJobProperties lastFailedPlannedFailoverJob()`
* `models.ProtectedItemModelPropertiesLastTestFailoverJob lastTestFailoverJob()` -> `models.ProtectedItemJobProperties lastTestFailoverJob()`
* `draId()` was removed
* `targetDraId()` was removed
* `models.LastFailedEnableProtectionJob lastFailedEnableProtectionJob()` -> `models.ProtectedItemJobProperties lastFailedEnableProtectionJob()`

#### `models.RecoveryPointModel` was modified

* `models.RecoveryPointModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.Events` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Vaults` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.TaskModelCustomProperties` was modified

* `withInstanceType(java.lang.String)` was removed

#### `models.VaultModelUpdate` was modified

* `models.VaultModelUpdateSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.EmailConfigurationModel` was modified

* `models.EmailConfigurationModelSystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `RecoveryServicesDataReplicationManager` was modified

* `vaultOperationStatus()` was removed
* `dras()` was removed
* `fabricOperationsStatus()` was removed
* `protectedItemOperationStatus()` was removed
* `resourceProviders()` was removed
* `workflows()` was removed
* `draOperationStatus()` was removed
* `workflowOperationStatus()` was removed
* `policyOperationStatus()` was removed

### Features Added

* `models.ManagedServiceIdentityType` was added

* `implementation.models.RepExtModelListResult` was added

* `models.HyperVToAzStackHCIEventModelCustomProperties` was added

* `models.FabricAgents` was added

* `implementation.models.ProtectedItemModelListResult` was added

* `implementation.models.JobModelListResult` was added

* `models.AffectedObjectDetailsType` was added

* `implementation.models.PrivEdpConnProxyListResult` was added

* `models.ProtectedItemModelPropertiesUpdate` was added

* `models.HyperVToAzStackHCIPolicyModelCustomProperties` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.ProtectedItemModel$Update` was added

* `models.HyperVToAzStackHciProtectedItemCustomPropsUpdate` was added

* `models.PrivateLinkResourceProperties` was added

* `implementation.models.RecoveryPointModelListResult` was added

* `implementation.models.OperationListResult` was added

* `models.RemotePrivateEndpointConnection` was added

* `models.VMwareToAzStackHCIPolicyModelCustomProperties` was added

* `implementation.models.PrivEdpConnListResult` was added

* `models.ManagedServiceIdentity` was added

* `models.FabricAgentModelCustomProperties` was added

* `models.JobObjectType` was added

* `models.FabricAgentModelProperties` was added

* `models.ProtectedItemModelUpdate` was added

* `models.PrivateEndpoint` was added

* `models.CheckNameAvailabilities` was added

* `models.VMwareToAzStackHCIEventModelCustomProperties` was added

* `models.JobModelCustomPropertiesAffectedObjectDetails` was added

* `models.PrivateEndpointConnections` was added

* `models.VaultIdentityModel` was added

* `models.JobState` was added

* `models.AzStackHCIFabricModelCustomProperties` was added

* `models.PrivateEndpointConnProxies` was added

* `implementation.models.VaultModelListResult` was added

* `models.PrivateEndpointConnectionProxy` was added

* `models.ReplicationExtension` was added

* `models.VMwareToAzStackHCINicInput` was added

* `models.FabricAgentModel$DefinitionStages` was added

* `models.FabricAgentModel$Definition` was added

* `models.VMwareFabricAgentModelCustomProperties` was added

* `models.PrivateLinkServiceProxy` was added

* `models.PrivateLinkResources` was added

* `models.PrivateLinkServiceConnection` was added

* `models.VaultIdentityType` was added

* `implementation.models.EventModelListResult` was added

* `models.DiskControllerInputs` was added

* `models.GroupConnectivityInformation` was added

* `models.VMwareToAzStackHCIRecoveryPointCustomProps` was added

* `models.JobModelProperties` was added

* `models.DeploymentPreflights` was added

* `models.ReplicationExtension$DefinitionStages` was added

* `models.RemotePrivateEndpoint` was added

* `models.HyperVToAzStackHciReplicationExtCustomProps` was added

* `models.PrivateEndpointConnectionProxyProperties` was added

* `models.JobCustomProperties` was added

* `models.JobModel` was added

* `models.ReplicationExtension$Definition` was added

* `models.UserAssignedIdentity` was added

* `models.VMwareToAzStackHCIProtectedNicProperties` was added

* `models.TestFailoverCleanupJobModelCustomProperties` was added

* `models.ProtectedItemModel$UpdateStages` was added

* `models.HyperVToAzStackHCIProtectedDiskProperties` was added

* `implementation.models.PrivateLinkResourceListResult` was added

* `models.PrivateEndpointConnection` was added

* `implementation.models.EmailConfigurationModelListResult` was added

* `implementation.models.PolicyModelListResult` was added

* `models.AzStackHCIClusterProperties` was added

* `models.HyperVToAzStackHCINicInput` was added

* `implementation.models.FabricModelListResult` was added

* `models.PrivateEndpointConnectionResponseProperties` was added

* `models.ProtectedItemModelCustomPropertiesUpdate` was added

* `models.FabricAgentModel` was added

* `models.FailoverJobModelCustomProperties` was added

* `models.PrivateLinkResource` was added

* `models.HyperVToAzStackHCIDiskInput` was added

* `models.Jobs` was added

* `models.VMwareToAzStackHciProtectedItemCustomPropsUpdate` was added

* `models.ConnectionDetails` was added

* `models.VMwareToAzStackHCIProtectedDiskProperties` was added

* `models.VMwareToAzStackHCIDiskInput` was added

* `models.TestFailoverJobModelCustomProperties` was added

* `implementation.models.FabricAgentModelListResult` was added

* `models.PrivateEndpointConnectionProxy$DefinitionStages` was added

* `models.PrivateEndpointConnectionProxy$Definition` was added

* `models.HyperVToAzStackHCIProtectedNicProperties` was added

* `models.PrivateEndpointConnectionStatus` was added

#### `models.TaskModel` was modified

* `childrenJobs()` was added

#### `models.ProtectedItems` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `plannedFailover(java.lang.String,java.lang.String,java.lang.String,fluent.models.PlannedFailoverModelInner)` was added

#### `models.RecoveryPointModelProperties` was modified

* `provisioningState()` was added

#### `models.VaultModel$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.VMwareToAzStackHciProtectedItemCustomProps` was modified

* `withTargetFabricAgentName(java.lang.String)` was added
* `targetFabricAgentName()` was added
* `withSourceFabricAgentName(java.lang.String)` was added
* `sourceFabricAgentName()` was added

#### `models.Fabrics` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.EmailConfigurationModelProperties` was modified

* `provisioningState()` was added

#### `models.HyperVToAzStackHciProtectedItemCustomProps` was modified

* `targetFabricAgentName()` was added
* `sourceFabricAgentName()` was added
* `withTargetFabricAgentName(java.lang.String)` was added
* `withSourceFabricAgentName(java.lang.String)` was added

#### `models.ProtectedItemModel` was modified

* `plannedFailover(fluent.models.PlannedFailoverModelInner)` was added
* `update()` was added
* `resourceGroupName()` was added

#### `models.DeploymentPreflightResource` was modified

* `properties()` was added
* `withProperties(com.azure.core.util.BinaryData)` was added

#### `models.VaultModel` was modified

* `identity()` was added

#### `models.EventModelProperties` was modified

* `provisioningState()` was added

#### `models.ProtectedItemModelProperties` was modified

* `fabricAgentId()` was added
* `targetFabricAgentId()` was added

#### `models.Events` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.Vaults` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.VaultModelUpdate` was modified

* `identity()` was added
* `withIdentity(models.VaultIdentityModel)` was added

#### `models.VaultModel$Update` was modified

* `withIdentity(models.VaultIdentityModel)` was added

#### `RecoveryServicesDataReplicationManager` was modified

* `checkNameAvailabilities()` was added
* `privateLinkResources()` was added
* `deploymentPreflights()` was added
* `fabricAgents()` was added
* `privateEndpointConnections()` was added
* `jobs()` was added
* `privateEndpointConnProxies()` was added

## 1.0.0-beta.2 (2024-12-03)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package tag package-2021-02-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.DraModelCustomProperties` was modified

* `instanceType()` was added

#### `models.AzStackHciFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.ProtectedItemModelPropertiesLastTestFailoverJob` was modified

* `scenarioName()` was added
* `state()` was added
* `name()` was added
* `displayName()` was added
* `id()` was added
* `startTime()` was added
* `endTime()` was added

#### `models.VMwareToAzStackHciPolicyModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciEventModelCustomProperties` was modified

* `instanceType()` was added

#### `models.FailoverWorkflowModelCustomProperties` was modified

* `instanceType()` was added
* `affectedObjectDetails()` was added

#### `models.VMwareMigrateFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.ReplicationExtensionModelCustomProperties` was modified

* `instanceType()` was added

#### `models.FabricModelUpdate` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.VMwareToAzStackHciProtectedItemCustomProps` was modified

* `instanceType()` was added

#### `models.TestFailoverWorkflowModelCustomProperties` was modified

* `affectedObjectDetails()` was added
* `instanceType()` was added

#### `models.ProtectedItemModelPropertiesCurrentJob` was modified

* `id()` was added
* `state()` was added
* `scenarioName()` was added
* `displayName()` was added
* `name()` was added
* `endTime()` was added
* `startTime()` was added

#### `models.HyperVToAzStackHciRepExtnCustomProps` was modified

* `instanceType()` was added

#### `models.RecoveryPointModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciPlannedFailoverCustomProps` was modified

* `instanceType()` was added

#### `models.PlannedFailoverModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVMigrateFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciProtectedItemCustomProps` was modified

* `instanceType()` was added

#### `models.VMwareDraModelCustomProperties` was modified

* `instanceType()` was added

#### `models.LastFailedEnableProtectionJob` was modified

* `startTime()` was added
* `endTime()` was added
* `id()` was added
* `displayName()` was added
* `scenarioName()` was added
* `name()` was added
* `state()` was added

#### `models.ProtectedItemModelCustomProperties` was modified

* `instanceType()` was added

#### `models.WorkflowModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciRecoveryPointCustomProps` was modified

* `instanceType()` was added

#### `models.FabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.PolicyModelCustomProperties` was modified

* `instanceType()` was added

#### `models.LastFailedPlannedFailoverJob` was modified

* `id()` was added
* `endTime()` was added
* `startTime()` was added
* `scenarioName()` was added
* `displayName()` was added
* `state()` was added
* `name()` was added

#### `models.EventModelCustomProperties` was modified

* `instanceType()` was added

#### `models.VMwareToAzStackHciRepExtnCustomProps` was modified

* `instanceType()` was added

#### `models.VMwareToAzStackHciPlannedFailoverCustomProps` was modified

* `instanceType()` was added

#### `models.VaultModelUpdate` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.TestFailoverCleanupWorkflowModelCustomProperties` was modified

* `affectedObjectDetails()` was added
* `instanceType()` was added

#### `models.HyperVToAzStackHciPolicyModelCustomProperties` was modified

* `instanceType()` was added

## 1.0.0-beta.1 (2023-10-24)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package tag package-2021-02-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

