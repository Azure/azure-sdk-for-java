# Release History

## 1.0.0-beta.2 (2024-10-08)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. Package tag package-2024-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SapCentralInstances` was removed

* `models.ConfigurationType` was removed

* `models.ResourceProviders` was removed

* `models.SapVirtualInstanceList` was removed

* `models.SapDatabaseInstanceList` was removed

* `models.UserAssignedServiceIdentity` was removed

* `models.SapApplicationServerInstanceList` was removed

* `models.SapCentralInstanceList` was removed

#### `models.SapCentralServerInstance` was modified

* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `startInstance()` was removed
* `stopInstance()` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.SapVirtualInstance` was modified

* `models.UserAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.SapApplicationServerInstances` was modified

* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.SapApplicationServerInstance` was modified

* `startInstance()` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance()` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed

#### `models.SapVirtualInstance$Definition` was modified

* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `models.SapVirtualInstance$Update` was modified

* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `models.SapDatabaseInstance` was modified

* `stopInstance()` was removed
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance()` was removed
* `startInstance(models.StartRequest,com.azure.core.util.Context)` was removed

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `models.UserAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `withIdentity(models.UserAssignedServiceIdentity)` was removed

#### `WorkloadsSapVirtualInstanceManager` was modified

* `resourceProviders()` was removed
* `sapCentralInstances()` was removed

#### `models.SapDatabaseInstances` was modified

* `startInstance(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was removed
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was removed
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was removed

### Features Added

* `models.SapCentralServerInstanceListResult` was added

* `models.SapApplicationServerInstanceListResult` was added

* `models.FileShareConfigurationType` was added

* `models.SapDatabaseInstanceListResult` was added

* `models.SapVirtualInstanceListResult` was added

* `models.SapCentralServerInstances` was added

* `models.ManagedServiceIdentity` was added

#### `models.SapConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added

#### `models.ImageReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapCentralServerInstance` was modified

* `start()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `stop()` was added
* `start(models.StartRequest,com.azure.core.util.Context)` was added

#### `models.SapApplicationServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreeTierFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `namingPatternType()` was added

#### `models.DiscoveryConfiguration` was modified

* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MountFileShareConfiguration` was modified

* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WindowsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `osType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeployerVmPackages` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapSupportedSkusRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapAvailabilityZoneDetailsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SoftwareConfiguration` was modified

* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CentralServerConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added

#### `models.DatabaseConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InfrastructureConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSizingRecommendationResult` was modified

* `deploymentType()` was added

#### `models.SapApplicationServerInstances` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.EnqueueReplicationServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GatewayServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationStatusResult` was modified

* `resourceId()` was added

#### `models.SharedStorageResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StartRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LoadBalancerResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshPublicKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ErrorDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapSupportedSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskVolumeConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapApplicationServerInstance` was modified

* `stop()` was added
* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `start()` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added

#### `models.CentralServerFullResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationServerFullResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapInstallWithoutOSConfigSoftwareConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `softwareInstallationType()` was added

#### `models.ThreeTierCustomResourceNames` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterfaceResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OSConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `osType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateSapVirtualInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinuxConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `osType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceInitiatedSoftwareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `softwareInstallationType()` was added

#### `models.DeploymentWithOSConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `configurationType()` was added

#### `models.SapVirtualInstance$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CreateAndMountFileShareConfiguration` was modified

* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshKeyPair` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapVirtualInstances` was modified

* `invokeSapSupportedSku(java.lang.String,models.SapSupportedSkusRequest)` was added
* `invokeSizingRecommendationsWithResponse(java.lang.String,models.SapSizingRecommendationRequest,com.azure.core.util.Context)` was added
* `invokeSizingRecommendations(java.lang.String,models.SapSizingRecommendationRequest)` was added
* `invokeAvailabilityZoneDetailsWithResponse(java.lang.String,models.SapAvailabilityZoneDetailsRequest,com.azure.core.util.Context)` was added
* `invokeDiskConfigurations(java.lang.String,models.SapDiskConfigurationsRequest)` was added
* `invokeSapSupportedSkuWithResponse(java.lang.String,models.SapSupportedSkusRequest,com.azure.core.util.Context)` was added
* `invokeDiskConfigurationsWithResponse(java.lang.String,models.SapDiskConfigurationsRequest,com.azure.core.util.Context)` was added
* `invokeAvailabilityZoneDetails(java.lang.String,models.SapAvailabilityZoneDetailsRequest)` was added

#### `models.OSProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OsSapConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapVirtualInstanceError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MessageServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapVirtualInstance$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.DatabaseServerFullResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapDatabaseInstance` was modified

* `start(models.StartRequest,com.azure.core.util.Context)` was added
* `stop(models.StopRequest,com.azure.core.util.Context)` was added
* `start()` was added
* `stop()` was added

#### `models.ExternalInstallationSoftwareConfiguration` was modified

* `softwareInstallationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateSapVirtualInstanceRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDiskConfigurationsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnqueueServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierRecommendationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleServerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `WorkloadsSapVirtualInstanceManager` was modified

* `sapCentralServerInstances()` was added

#### `models.SapAvailabilityZonePair` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapDatabaseInstances` was modified

* `start(java.lang.String,java.lang.String,java.lang.String,models.StartRequest,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ManagedRGConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SingleServerFullResourceNames` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `namingPatternType()` was added

#### `models.SapCentralServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentConfiguration` was modified

* `configurationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HighAvailabilityConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SingleServerCustomResourceNames` was modified

* `namingPatternType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkipFileShareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `configurationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreeTierConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `deploymentType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2024-03-22)

- Azure Resource Manager Workloads Sap Virtual Instance client library for Java. This package contains Microsoft Azure SDK for Workloads Sap Virtual Instance Management SDK. Workloads client provides access to various workload operations. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

