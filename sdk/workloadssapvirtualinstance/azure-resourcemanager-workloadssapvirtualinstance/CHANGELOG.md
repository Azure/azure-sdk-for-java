# Release History

## 1.0.0 (2024-10-23)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SapCentralInstances` was removed

* `models.Operation` was removed

* `models.ConfigurationType` was removed

* `models.ResourceProviders` was removed

* `models.SapVirtualInstanceList` was removed

* `models.Origin` was removed

* `models.OperationDisplay` was removed

* `models.OperationListResult` was removed

* `models.SapDatabaseInstanceList` was removed

* `models.UserAssignedServiceIdentity` was removed

* `models.SapApplicationServerInstanceList` was removed

* `models.ActionType` was removed

* `models.Operations` was removed

* `models.SapCentralInstanceList` was removed

#### `models.SapCentralServerInstance` was modified

* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance()` was removed
* `startInstance()` was removed

#### `models.SingleServerRecommendationResult` was modified

* `withVmSku(java.lang.String)` was removed

#### `models.SapVirtualInstance` was modified

* `models.UserAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.DiskDetails` was modified

* `withSizeGB(java.lang.Long)` was removed
* `withSku(models.DiskSku)` was removed
* `withDiskTier(java.lang.String)` was removed
* `withMinimumSupportedDiskCount(java.lang.Long)` was removed
* `withMbpsReadWrite(java.lang.Long)` was removed
* `withIopsReadWrite(java.lang.Long)` was removed
* `withMaximumSupportedDiskCount(java.lang.Long)` was removed

#### `models.SapApplicationServerInstances` was modified

* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.OperationStatusResult` was modified

* `java.lang.Float percentComplete()` -> `java.lang.Double percentComplete()`

#### `models.SapSupportedSku` was modified

* `withIsAppServerCertified(java.lang.Boolean)` was removed
* `withIsDatabaseCertified(java.lang.Boolean)` was removed
* `withVmSku(java.lang.String)` was removed

#### `models.SapApplicationServerInstance` was modified

* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `stopInstance()` was removed
* `startInstance()` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed

#### `models.SapVirtualInstance$Definition` was modified

* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `models.SapVirtualInstanceError` was modified

* `withProperties(models.ErrorDefinition)` was removed

#### `models.SapVirtualInstance$Update` was modified

* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`

#### `models.SapDatabaseInstance` was modified

* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance()` was removed
* `stopInstance()` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `withIdentity(models.UserAssignedServiceIdentity)` was removed
* `models.UserAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.ThreeTierRecommendationResult` was modified

* `withDbVmSku(java.lang.String)` was removed
* `withCentralServerVmSku(java.lang.String)` was removed
* `withApplicationServerInstanceCount(java.lang.Long)` was removed
* `withApplicationServerVmSku(java.lang.String)` was removed
* `withDatabaseInstanceCount(java.lang.Long)` was removed
* `withCentralServerInstanceCount(java.lang.Long)` was removed

#### `models.VirtualMachineResourceNames` was modified

* `hostname()` was removed
* `withHostname(java.lang.String)` was removed

#### `WorkloadsSapVirtualInstanceManager` was modified

* `resourceProviders()` was removed
* `operations()` was removed
* `fluent.WorkloadsClient serviceClient()` -> `fluent.WorkloadsSapVirtualInstanceMgmtClient serviceClient()`
* `sapCentralInstances()` was removed

#### `models.SapAvailabilityZonePair` was modified

* `withZoneA(java.lang.Long)` was removed
* `withZoneB(java.lang.Long)` was removed

#### `models.SapDatabaseInstances` was modified

* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.SapDiskConfiguration` was modified

* `withRecommendedConfiguration(models.DiskVolumeConfiguration)` was removed
* `withSupportedConfigurations(java.util.List)` was removed

### Features Added

* `implementation.models.SAPDatabaseInstanceListResult` was added

* `implementation.models.SAPApplicationServerInstanceListResult` was added

* `models.FileShareConfigurationType` was added

* `models.SapCentralServerInstances` was added

* `models.ManagedServiceIdentity` was added

* `implementation.models.SAPCentralServerInstanceListResult` was added

* `implementation.models.SAPVirtualInstanceListResult` was added

#### `models.SapConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added

#### `models.ImageReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapCentralServerInstance` was modified

* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `stop()` was added
* `start()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added

#### `models.SapApplicationServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreeTierFullResourceNames` was modified

* `namingPatternType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiscoveryConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MountFileShareConfiguration` was modified

* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WindowsConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `osType()` was added

#### `models.DeployerVmPackages` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSupportedSkusRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationServerVmDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerRecommendationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `deploymentType()` was added

#### `models.UpdateSapApplicationInstanceRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HighAvailabilitySoftwareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapAvailabilityZoneDetailsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SoftwareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CentralServerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CentralServerVmDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapDatabaseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileShareConfiguration` was modified

* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatabaseConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InfrastructureConfiguration` was modified

* `deploymentType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSizingRecommendationResult` was modified

* `deploymentType()` was added

#### `models.SapApplicationServerInstances` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added

#### `models.EnqueueReplicationServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GatewayServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SharedStorageResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StartRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LoadBalancerResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshPublicKey` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ErrorDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSupportedSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskVolumeConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapApplicationServerInstance` was modified

* `stop()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `start()` was added

#### `models.CentralServerFullResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapVirtualInstanceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapInstallWithoutOSConfigSoftwareConfiguration` was modified

* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierCustomResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added

#### `models.NetworkInterfaceResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OSConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `osType()` was added

#### `models.UpdateSapVirtualInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinuxConfiguration` was modified

* `osType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceInitiatedSoftwareConfiguration` was modified

* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentWithOSConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstance$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CreateAndMountFileShareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshKeyPair` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstances` was modified

* `getDiskConfigurations(java.lang.String,models.SapDiskConfigurationsRequest)` was added
* `getSizingRecommendations(java.lang.String,models.SapSizingRecommendationRequest)` was added
* `getSapSupportedSku(java.lang.String,models.SapSupportedSkusRequest)` was added
* `getSizingRecommendationsWithResponse(java.lang.String,models.SapSizingRecommendationRequest,com.azure.core.util.Context)` was added
* `getAvailabilityZoneDetailsWithResponse(java.lang.String,models.SapAvailabilityZoneDetailsRequest,com.azure.core.util.Context)` was added
* `getDiskConfigurationsWithResponse(java.lang.String,models.SapDiskConfigurationsRequest,com.azure.core.util.Context)` was added
* `getAvailabilityZoneDetails(java.lang.String,models.SapAvailabilityZoneDetailsRequest)` was added
* `getSapSupportedSkuWithResponse(java.lang.String,models.SapSupportedSkusRequest,com.azure.core.util.Context)` was added

#### `models.OSProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OsSapConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstanceError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MessageServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstance$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.DatabaseServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapDatabaseInstance` was modified

* `start()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `stop()` was added

#### `models.ExternalInstallationSoftwareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDiskConfigurationsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnqueueServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierRecommendationResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `deploymentType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `hostName()` was added
* `withHostName(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `WorkloadsSapVirtualInstanceManager` was modified

* `sapCentralServerInstances()` was added

#### `models.SapAvailabilityZonePair` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDatabaseInstances` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added

#### `models.ManagedRGConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerFullResourceNames` was modified

* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapCentralServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentConfiguration` was modified

* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StopRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateSapDatabaseInstanceRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationServerConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LoadBalancerDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapSizingRecommendationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateSapCentralInstanceRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDiskConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseVmDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HighAvailabilityConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerCustomResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `namingPatternType()` was added

#### `models.NetworkConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkipFileShareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added

#### `models.ThreeTierConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added

## 1.0.0-beta.1 (2024-03-22)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

