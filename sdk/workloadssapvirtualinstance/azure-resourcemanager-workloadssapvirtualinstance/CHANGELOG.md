# Release History

## 1.0.0 (2025-02-20)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SapCentralInstances` was removed

#### `models.ConfigurationType` was removed

#### `models.ResourceProviders` was removed

#### `models.SapVirtualInstanceList` was removed

#### `models.OperationListResult` was removed

#### `models.SapDatabaseInstanceList` was removed

#### `models.UserAssignedServiceIdentity` was removed

#### `models.SapApplicationServerInstanceList` was removed

#### `models.ManagedServiceIdentityType` was removed

#### `models.SapCentralInstanceList` was removed

#### `models.SapCentralServerInstance` was modified

* `stopInstance()` was removed
* `startInstance()` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.SingleServerRecommendationResult` was modified

* `withVmSku(java.lang.String)` was removed

#### `models.SapVirtualInstance` was modified

* `models.UserAssignedServiceIdentity identity()` -> `models.SAPVirtualInstanceIdentity identity()`

#### `models.DiskDetails` was modified

* `withMbpsReadWrite(java.lang.Long)` was removed
* `withIopsReadWrite(java.lang.Long)` was removed
* `withMinimumSupportedDiskCount(java.lang.Long)` was removed
* `withSizeGB(java.lang.Long)` was removed
* `withDiskTier(java.lang.String)` was removed
* `withSku(models.DiskSku)` was removed
* `withMaximumSupportedDiskCount(java.lang.Long)` was removed

#### `models.SapApplicationServerInstances` was modified

* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.OperationStatusResult` was modified

* `java.lang.Float percentComplete()` -> `java.lang.Double percentComplete()`

#### `models.SapSupportedSku` was modified

* `withVmSku(java.lang.String)` was removed
* `withIsAppServerCertified(java.lang.Boolean)` was removed
* `withIsDatabaseCertified(java.lang.Boolean)` was removed

#### `models.SapApplicationServerInstance` was modified

* `startInstance()` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance()` was removed

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

* `startInstance()` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance()` was removed

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `models.UserAssignedServiceIdentity identity()` -> `models.SAPVirtualInstanceIdentity identity()`
* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `models.ThreeTierRecommendationResult` was modified

* `withCentralServerVmSku(java.lang.String)` was removed
* `withApplicationServerInstanceCount(java.lang.Long)` was removed
* `withDatabaseInstanceCount(java.lang.Long)` was removed
* `withCentralServerInstanceCount(java.lang.Long)` was removed
* `withApplicationServerVmSku(java.lang.String)` was removed
* `withDbVmSku(java.lang.String)` was removed

#### `models.VirtualMachineResourceNames` was modified

* `withHostname(java.lang.String)` was removed
* `hostname()` was removed

#### `WorkloadsSapVirtualInstanceManager` was modified

* `sapCentralInstances()` was removed
* `resourceProviders()` was removed
* `fluent.WorkloadsClient serviceClient()` -> `fluent.WorkloadsSapVirtualInstanceMgmtClient serviceClient()`

#### `models.SapAvailabilityZonePair` was modified

* `withZoneA(java.lang.Long)` was removed
* `withZoneB(java.lang.Long)` was removed

#### `models.SapDatabaseInstances` was modified

* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.SapDiskConfiguration` was modified

* `withRecommendedConfiguration(models.DiskVolumeConfiguration)` was removed
* `withSupportedConfigurations(java.util.List)` was removed

### Features Added

* `implementation.models.SAPDatabaseInstanceListResult` was added

* `implementation.models.SAPApplicationServerInstanceListResult` was added

* `models.SAPVirtualInstanceIdentity` was added

* `implementation.models.OperationListResult` was added

* `models.SAPVirtualInstanceIdentityType` was added

* `models.FileShareConfigurationType` was added

* `models.SapCentralServerInstances` was added

* `implementation.models.SAPCentralServerInstanceListResult` was added

* `implementation.models.SAPVirtualInstanceListResult` was added

#### `models.SapConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added

#### `models.ImageReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapCentralServerInstance` was modified

* `start()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `stop()` was added
* `start(models.StartRequest,com.azure.core.util.Context)` was added

#### `models.SapApplicationServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreeTierFullResourceNames` was modified

* `namingPatternType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiscoveryConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MountFileShareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WindowsConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `osType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeployerVmPackages` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSupportedSkusRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationServerVmDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SingleServerRecommendationResult` was modified

* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added

#### `models.CentralServerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CentralServerVmDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapDatabaseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileShareConfiguration` was modified

* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InfrastructureConfiguration` was modified

* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapSizingRecommendationResult` was modified

* `deploymentType()` was added

#### `models.SapApplicationServerInstances` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.EnqueueReplicationServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GatewayServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationStatusResult` was modified

* `resourceId()` was added

#### `models.SharedStorageResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StartRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LoadBalancerResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshPublicKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ErrorDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapSupportedSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskVolumeConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapApplicationServerInstance` was modified

* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `stop()` was added
* `start()` was added

#### `models.CentralServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapVirtualInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapInstallWithoutOSConfigSoftwareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreeTierCustomResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterfaceResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OSConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `osType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateSapVirtualInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinuxConfiguration` was modified

* `osType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceInitiatedSoftwareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `softwareInstallationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentWithOSConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added

#### `models.SapVirtualInstance$Definition` was modified

* `withIdentity(models.SAPVirtualInstanceIdentity)` was added

#### `models.CreateAndMountFileShareConfiguration` was modified

* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshKeyPair` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstances` was modified

* `getAvailabilityZoneDetailsWithResponse(java.lang.String,models.SapAvailabilityZoneDetailsRequest,com.azure.core.util.Context)` was added
* `getDiskConfigurationsWithResponse(java.lang.String,models.SapDiskConfigurationsRequest,com.azure.core.util.Context)` was added
* `getSapSupportedSku(java.lang.String,models.SapSupportedSkusRequest)` was added
* `getSizingRecommendations(java.lang.String,models.SapSizingRecommendationRequest)` was added
* `getDiskConfigurations(java.lang.String,models.SapDiskConfigurationsRequest)` was added
* `getSapSupportedSkuWithResponse(java.lang.String,models.SapSupportedSkusRequest,com.azure.core.util.Context)` was added
* `getAvailabilityZoneDetails(java.lang.String,models.SapAvailabilityZoneDetailsRequest)` was added
* `getSizingRecommendationsWithResponse(java.lang.String,models.SapSizingRecommendationRequest,com.azure.core.util.Context)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapVirtualInstance$Update` was modified

* `withIdentity(models.SAPVirtualInstanceIdentity)` was added

#### `models.DatabaseServerFullResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDatabaseInstance` was modified

* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `stop()` was added
* `start()` was added

#### `models.ExternalInstallationSoftwareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIdentity(models.SAPVirtualInstanceIdentity)` was added

#### `models.SapDiskConfigurationsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EnqueueServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierRecommendationResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added

#### `models.VirtualMachineResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withHostName(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `hostName()` was added

#### `models.SingleServerConfiguration` was modified

* `deploymentType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `WorkloadsSapVirtualInstanceManager` was modified

* `sapCentralServerInstances()` was added

#### `models.SapAvailabilityZonePair` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapDatabaseInstances` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added

#### `models.ManagedRGConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SingleServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapCentralServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added

#### `models.StopRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateSapDatabaseInstanceRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationServerConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LoadBalancerDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapSizingRecommendationRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateSapCentralInstanceRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDiskConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseVmDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HighAvailabilityConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerCustomResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkipFileShareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `deploymentType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.1 (2024-03-22)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

