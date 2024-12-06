# Release History

## 2.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.0.0 (2024-09-26)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2024-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionsUpdateHeaders` was removed

* `models.BatchAccountsCreateResponse` was removed

* `models.CloudServiceConfiguration` was removed

* `models.PoolsDeleteHeaders` was removed

* `models.BatchAccountsDeleteHeaders` was removed

* `models.CertificatesDeleteResponse` was removed

* `models.BatchAccountsDeleteResponse` was removed

* `models.CertificatesDeleteHeaders` was removed

* `models.BatchAccountsCreateHeaders` was removed

* `models.PrivateEndpointConnectionsUpdateResponse` was removed

* `models.PoolsDeleteResponse` was removed

#### `models.DeploymentConfiguration` was modified

* `cloudServiceConfiguration()` was removed
* `withCloudServiceConfiguration(models.CloudServiceConfiguration)` was removed

#### `models.NetworkConfiguration` was modified

* `dynamicVNetAssignmentScope()` was removed
* `withDynamicVNetAssignmentScope(models.DynamicVNetAssignmentScope)` was removed

#### `models.ContainerConfiguration` was modified

* `java.lang.String type()` -> `models.ContainerType type()`
* `withType(java.lang.String)` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionRequired()` was removed

#### `models.Certificate$Definition` was modified

* `withData(java.lang.String)` was removed

#### `models.PrivateEndpointConnections` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,java.lang.String)` was removed

#### `models.Locations` was modified

* `listSupportedCloudServiceSkus(java.lang.String)` was removed
* `listSupportedCloudServiceSkus(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ContainerHostDataPath` was added

* `models.SecurityTypes` was added

* `models.Severity` was added

* `models.SecurityProfile` was added

* `models.ContainerType` was added

* `models.VMDiskSecurityProfile` was added

* `models.UefiSettings` was added

* `models.ProvisioningIssue` was added

* `models.NetworkSecurityProfile` was added

* `models.ManagedDisk` was added

* `models.AccessRule` was added

* `models.NetworkProfile` was added

* `models.NetworkSecurityPerimeters` was added

* `models.UpgradePolicy` was added

* `models.EndpointAccessProfile` was added

* `models.ProvisioningIssueProperties` was added

* `models.AccessRulePropertiesSubscriptionsItem` was added

* `models.NetworkSecurityPerimeter` was added

* `models.NetworkSecurityPerimeterConfiguration` was added

* `models.RollingUpgradePolicy` was added

* `models.UpgradeMode` was added

* `models.ServiceArtifactReference` was added

* `models.AccessRuleProperties` was added

* `models.SecurityEncryptionTypes` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.AutomaticOSUpgradePolicy` was added

* `models.NodeCommunicationMode` was added

* `models.EndpointAccessDefaultAction` was added

* `models.ResourceAssociationAccessMode` was added

* `models.AccessRuleDirection` was added

* `models.ResourceAssociation` was added

* `models.ContainerHostBatchBindMountEntry` was added

* `models.IssueType` was added

* `models.NetworkSecurityPerimeterConfigurationListResult` was added

* `models.NetworkSecurityPerimeterConfigurationProvisioningState` was added

* `models.AzureProxyResource` was added

* `models.IpRule` was added

#### `models.DeploymentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchAccountRegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ListPoolsResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `dynamicVnetAssignmentScope()` was added
* `enableAcceleratedNetworking()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDynamicVnetAssignmentScope(models.DynamicVNetAssignmentScope)` was added
* `withEnableAcceleratedNetworking(java.lang.Boolean)` was added

#### `models.AutoScaleRunError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchAccountIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `BatchManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureFileShareConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Application` was modified

* `resourceGroupName()` was added
* `tags()` was added

#### `models.DiskEncryptionConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutoScaleRun` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchAccount` was modified

* `networkProfile()` was added
* `resourceGroupName()` was added
* `nodeManagementEndpoint()` was added

#### `models.NodePlacementConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivateApplicationPackageParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TaskContainerSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withContainerHostBatchBindMounts(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `containerHostBatchBindMounts()` was added

#### `models.NfsMountConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DetectorListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResizeOperationStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PoolEndpointConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchAccountUpdateParameters` was modified

* `withNetworkProfile(models.NetworkProfile)` was added
* `publicNetworkAccess()` was added
* `networkProfile()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FixedScaleSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Certificate` was modified

* `tags()` was added
* `resourceGroupName()` was added

#### `models.WindowsUserConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Certificate$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.AutoScaleSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchPoolIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoUserSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added
* `tags()` was added

#### `models.ContainerConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withType(models.ContainerType)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceFile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SupportedSkusResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EndpointDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InboundNatPool` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentSetting` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `BatchManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `networkSecurityPerimeters()` was added

#### `models.VMExtension` was modified

* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `enableAutomaticUpgrade()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `serviceArtifactReference()` was added
* `withServiceArtifactReference(models.ServiceArtifactReference)` was added
* `withSecurityProfile(models.SecurityProfile)` was added
* `securityProfile()` was added

#### `models.ScaleSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataDisk` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationPackage` was modified

* `tags()` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionsRequired()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoStorageProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListApplicationPackagesResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BatchAccount$Definition` was modified

* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.SupportedSku` was modified

* `batchSupportEndOfLife()` was added

#### `models.UserAccount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `tags()` was added

#### `models.AutoStorageBaseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TaskSchedulingPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationPackageReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageReference` was modified

* `communityGalleryImageId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withCommunityGalleryImageId(java.lang.String)` was added
* `withSharedGalleryImageId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `sharedGalleryImageId()` was added

#### `models.KeyVaultReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Certificate$Definition` was modified

* `withTags(java.util.Map)` was added

#### `models.NetworkSecurityGroupRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateCreateOrUpdateParameters` was modified

* `type()` was added
* `name()` was added
* `withTags(java.util.Map)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.ComputeNodeIdentityReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationPackage$Definition` was modified

* `withTags(java.util.Map)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobFileSystemConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineFamilyCoreQuota` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.OSDisk` was modified

* `writeAcceleratorEnabled()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDiskSizeGB(java.lang.Integer)` was added
* `diskSizeGB()` was added
* `caching()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withManagedDisk(models.ManagedDisk)` was added
* `withWriteAcceleratorEnabled(java.lang.Boolean)` was added
* `withCaching(models.CachingType)` was added
* `managedDisk()` was added

#### `models.CifsMountConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerRegistry` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Pool$Definition` was modified

* `withResourceTags(java.util.Map)` was added
* `withTags(java.util.Map)` was added
* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added
* `withUpgradePolicy(models.UpgradePolicy)` was added

#### `models.BatchAccount$Update` was modified

* `withNetworkProfile(models.NetworkProfile)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added

#### `models.StartTask` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OutboundEnvironmentEndpointCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MountConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PublicIpAddressConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Application$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.CertificateBaseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WindowsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BatchAccountCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `networkProfile()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.ListPrivateEndpointConnectionsResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchAccountListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinuxUserConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListCertificatesResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeleteCertificateError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentities` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListApplicationsResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Pool` was modified

* `targetNodeCommunicationMode()` was added
* `tags()` was added
* `upgradePolicy()` was added
* `resourceGroupName()` was added
* `resourceTags()` was added
* `currentNodeCommunicationMode()` was added

#### `models.DetectorResponse` was modified

* `tags()` was added

#### `models.Pool$Update` was modified

* `withResourceTags(java.util.Map)` was added
* `withUpgradePolicy(models.UpgradePolicy)` was added
* `withTags(java.util.Map)` was added
* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added

#### `models.ListPrivateLinkResourcesResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Application$Definition` was modified

* `withTags(java.util.Map)` was added

#### `models.DiffDiskSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResizeError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0-beta.4 (2024-03-27)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UpgradePolicy` was added

* `models.RollingUpgradePolicy` was added

* `models.UpgradeMode` was added

* `models.AutomaticOSUpgradePolicy` was added

#### `models.SupportedSku` was modified

* `batchSupportEndOfLife()` was added

#### `models.Pool$Definition` was modified

* `withUpgradePolicy(models.UpgradePolicy)` was added

#### `models.Pool` was modified

* `upgradePolicy()` was added

#### `models.Pool$Update` was modified

* `withUpgradePolicy(models.UpgradePolicy)` was added

## 1.1.0-beta.3 (2023-12-22)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SecurityTypes` was added

* `models.SecurityProfile` was added

* `models.UefiSettings` was added

* `models.ManagedDisk` was added

* `models.ServiceArtifactReference` was added

#### `models.VirtualMachineConfiguration` was modified

* `securityProfile()` was added
* `serviceArtifactReference()` was added
* `withServiceArtifactReference(models.ServiceArtifactReference)` was added
* `withSecurityProfile(models.SecurityProfile)` was added

#### `models.OSDisk` was modified

* `diskSizeGB()` was added
* `withDiskSizeGB(java.lang.Integer)` was added
* `withWriteAcceleratorEnabled(java.lang.Boolean)` was added
* `withCaching(models.CachingType)` was added
* `caching()` was added
* `managedDisk()` was added
* `writeAcceleratorEnabled()` was added
* `withManagedDisk(models.ManagedDisk)` was added

#### `models.Pool$Definition` was modified

* `withResourceTags(java.util.Map)` was added

#### `models.Pool` was modified

* `resourceTags()` was added

#### `models.Pool$Update` was modified

* `withResourceTags(java.util.Map)` was added

## 1.1.0-beta.2 (2023-07-26)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ContainerConfiguration` was modified

* `java.lang.String type()` -> `models.ContainerType type()`
* `withType(java.lang.String)` was removed

### Features Added

* `models.ContainerType` was added

#### `models.NetworkConfiguration` was modified

* `withEnableAcceleratedNetworking(java.lang.Boolean)` was added
* `enableAcceleratedNetworking()` was added

#### `models.ContainerConfiguration` was modified

* `withType(models.ContainerType)` was added

#### `models.VMExtension` was modified

* `enableAutomaticUpgrade()` was added
* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added

## 1.1.0-beta.1 (2022-11-24)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionsUpdateHeaders` was removed

* `models.BatchAccountsCreateResponse` was removed

* `models.PoolsDeleteHeaders` was removed

* `models.BatchAccountsDeleteHeaders` was removed

* `models.CertificatesDeleteResponse` was removed

* `models.BatchAccountsDeleteResponse` was removed

* `models.CertificatesDeleteHeaders` was removed

* `models.BatchAccountsCreateHeaders` was removed

* `models.PrivateEndpointConnectionsUpdateResponse` was removed

* `models.PoolsDeleteResponse` was removed

#### `models.NetworkConfiguration` was modified

* `withDynamicVNetAssignmentScope(models.DynamicVNetAssignmentScope)` was removed
* `dynamicVNetAssignmentScope()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionRequired()` was removed

#### `models.Certificate$Definition` was modified

* `withData(java.lang.String)` was removed

#### `models.PrivateEndpointConnections` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,java.lang.String)` was removed

### Features Added

* `models.NetworkProfile` was added

* `models.EndpointAccessProfile` was added

* `models.NodeCommunicationMode` was added

* `models.EndpointAccessDefaultAction` was added

* `models.IpRule` was added

#### `models.NetworkConfiguration` was modified

* `withDynamicVnetAssignmentScope(models.DynamicVNetAssignmentScope)` was added
* `dynamicVnetAssignmentScope()` was added

#### `BatchManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Application` was modified

* `resourceGroupName()` was added

#### `models.BatchAccount` was modified

* `networkProfile()` was added
* `resourceGroupName()` was added
* `nodeManagementEndpoint()` was added

#### `models.BatchAccountUpdateParameters` was modified

* `networkProfile()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `withNetworkProfile(models.NetworkProfile)` was added
* `publicNetworkAccess()` was added

#### `models.Certificate` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `BatchManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionsRequired()` was added

#### `models.BatchAccount$Definition` was modified

* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Pool$Definition` was modified

* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added

#### `models.BatchAccount$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.BatchAccountCreateParameters` was modified

* `networkProfile()` was added
* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.Pool` was modified

* `currentNodeCommunicationMode()` was added
* `resourceGroupName()` was added
* `targetNodeCommunicationMode()` was added

#### `models.Pool$Update` was modified

* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added

## 1.0.0 (2022-03-23)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `BatchManager` was modified

* `fluent.BatchManagement serviceClient()` -> `fluent.BatchManagementClient serviceClient()`

### Features Added

* `models.DynamicVNetAssignmentScope` was added

* `models.DetectorListResult` was added

* `models.DetectorResponse` was added

#### `models.NetworkConfiguration` was modified

* `dynamicVNetAssignmentScope()` was added
* `withDynamicVNetAssignmentScope(models.DynamicVNetAssignmentScope)` was added

#### `models.BatchAccounts` was modified

* `getDetectorWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listDetectors(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDetector(java.lang.String,java.lang.String,java.lang.String)` was added
* `listDetectors(java.lang.String,java.lang.String)` was added

## 1.0.0-beta.2 (2021-07-29)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK.  Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BatchAccountIdentityUserAssignedIdentities` was removed

* `models.CertificateProperties` was removed

* `models.CertificateCreateOrUpdateProperties` was removed

* `models.BatchPoolIdentityUserAssignedIdentities` was removed

#### `models.Certificate$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.Pool$Update` was modified

* `ifMatch(java.lang.String)` was removed

### Features Added

* `models.EndpointDependency` was added

* `models.DiffDiskPlacement` was added

* `models.SupportedSkusResult` was added

* `models.EndpointDetail` was added

* `models.SupportedSku` was added

* `models.AutoStorageAuthenticationMode` was added

* `models.ComputeNodeIdentityReference` was added

* `models.OSDisk` was added

* `models.OutboundEnvironmentEndpointCollection` was added

* `models.SkuCapability` was added

* `models.UserAssignedIdentities` was added

* `models.DiffDiskSettings` was added

* `models.AuthenticationMode` was added

* `models.OutboundEnvironmentEndpoint` was added

#### `BatchManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.BatchAccount` was modified

* `allowedAuthenticationModes()` was added

#### `models.BatchAccountUpdateParameters` was modified

* `allowedAuthenticationModes()` was added
* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.Certificate$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.ResourceFile` was modified

* `identityReference()` was added
* `withIdentityReference(models.ComputeNodeIdentityReference)` was added

#### `models.BatchAccounts` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String)` was added

#### `models.VirtualMachineConfiguration` was modified

* `osDisk()` was added
* `withOsDisk(models.OSDisk)` was added

#### `models.AutoStorageProperties` was modified

* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added
* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added

#### `models.BatchAccount$Definition` was modified

* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.AutoStorageBaseProperties` was modified

* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added
* `authenticationMode()` was added
* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `nodeIdentityReference()` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.AzureBlobFileSystemConfiguration` was modified

* `withIdentityReference(models.ComputeNodeIdentityReference)` was added
* `identityReference()` was added

#### `models.Locations` was modified

* `listSupportedVirtualMachineSkus(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSupportedCloudServiceSkus(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSupportedCloudServiceSkus(java.lang.String)` was added
* `listSupportedVirtualMachineSkus(java.lang.String)` was added

#### `models.ContainerRegistry` was modified

* `identityReference()` was added
* `withIdentityReference(models.ComputeNodeIdentityReference)` was added

#### `models.BatchAccount$Update` was modified

* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.BatchAccountCreateParameters` was modified

* `allowedAuthenticationModes()` was added
* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.Pool$Update` was modified

* `withIfMatch(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK.  Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
