# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2023-10-20)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-preview-2023-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Extension` was removed

* `models.Cluster$Update` was removed

* `models.NodeExtensionState` was removed

* `models.ArcSetting$DefinitionStages` was removed

* `models.ClusterNode` was removed

* `models.Extension$Definition` was removed

* `models.ArcIdentityResponse` was removed

* `models.ArcSettings` was removed

* `models.ImdsAttestation` was removed

* `models.Cluster$DefinitionStages` was removed

* `models.RawCertificateData` was removed

* `models.WindowsServerSubscription` was removed

* `models.ClusterDesiredProperties` was removed

* `models.Extension$UpdateStages` was removed

* `models.ArcSetting$Definition` was removed

* `models.Cluster$UpdateStages` was removed

* `models.Clusters` was removed

* `models.ExtensionList` was removed

* `models.ClusterReportedProperties` was removed

* `models.ArcSetting` was removed

* `models.ArcSettingAggregateState` was removed

* `models.ProvisioningState` was removed

* `models.Cluster` was removed

* `models.UploadCertificateRequest` was removed

* `models.ClusterIdentityResponse` was removed

* `models.ArcSetting$UpdateStages` was removed

* `models.ClusterPatch` was removed

* `models.PerNodeState` was removed

* `models.ClusterList` was removed

* `models.ArcSetting$Update` was removed

* `models.Cluster$Definition` was removed

* `models.NodeArcState` was removed

* `models.Extensions` was removed

* `models.ArcSettingList` was removed

* `models.DiagnosticLevel` was removed

* `models.Extension$DefinitionStages` was removed

* `models.ArcSettingsPatch` was removed

* `models.Extension$Update` was removed

* `models.PerNodeExtensionState` was removed

* `models.ExtensionAggregateState` was removed

* `models.PasswordCredential` was removed

#### `models.Operation` was modified

* `validate()` was removed
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `java.lang.String name()` -> `java.lang.String name()`
* `java.lang.Boolean isDataAction()` -> `java.lang.Boolean isDataAction()`
* `models.Origin origin()` -> `models.Origin origin()`
* `withDisplay(models.OperationDisplay)` was removed
* `models.ActionType actionType()` -> `models.ActionType actionType()`

#### `models.OperationListResult` was modified

* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `innerModel()` was removed
* `java.util.List value()` -> `java.util.List value()`

#### `models.Operations` was modified

* `models.OperationListResult list()` -> `com.azure.core.http.rest.PagedIterable list()`
* `listWithResponse(com.azure.core.util.Context)` was removed

#### `AzureStackHciManager` was modified

* `extensions()` was removed
* `clusters()` was removed
* `arcSettings()` was removed

### Features Added

* `models.DiskFileFormat` was added

* `models.GalleryOSDiskImage` was added

* `models.Subnet` was added

* `models.IpConfiguration` was added

* `models.LogicalNetworkStatusProvisioningStatus` was added

* `models.GalleryImageStatus` was added

* `models.StorageProfileUpdate` was added

* `models.VirtualMachineInstancePropertiesHardwareProfileDynamicMemoryConfig` was added

* `models.NetworkProfileUpdateNetworkInterfacesItem` was added

* `models.GalleryImageIdentifier` was added

* `models.VirtualMachineInstanceUpdateRequest` was added

* `models.GalleryImageVersion` was added

* `models.GuestAgentsOperations` was added

* `models.NetworkInterfacesListResult` was added

* `models.VirtualMachineInstancePropertiesSecurityProfileUefiSettings` was added

* `models.VirtualMachineInstancePropertiesNetworkProfile` was added

* `models.IpPool` was added

* `models.MarketplaceGalleryImageStatusDownloadStatus` was added

* `models.IpConfigurationProperties` was added

* `models.MarketplaceGalleryImagesListResult` was added

* `models.MarketplaceGalleryImagesUpdateRequest` was added

* `models.StatusTypes` was added

* `models.VirtualMachineInstancePropertiesOsProfile` was added

* `models.CloudInitDataSource` was added

* `models.OsProfileUpdateWindowsConfiguration` was added

* `models.SshPublicKey` was added

* `models.MarketplaceGalleryImages$Definition` was added

* `models.NetworkInterfaces` was added

* `models.HttpProxyConfiguration` was added

* `models.StorageContainers$Update` was added

* `models.SubnetPropertiesFormatIpConfigurationReferencesItem` was added

* `models.VirtualMachineInstanceStatusProvisioningStatus` was added

* `models.VirtualMachineInstanceUpdateProperties` was added

* `models.GalleryImagesUpdateRequest` was added

* `models.OsProfileUpdate` was added

* `models.VirtualMachineInstancePropertiesOsProfileWindowsConfiguration` was added

* `models.GalleryImageVersionStorageProfile` was added

* `models.HybridIdentityMetadatas` was added

* `models.HybridIdentityMetadataList` was added

* `models.ProvisioningAction` was added

* `models.MarketplaceGalleryImageStatus` was added

* `models.VirtualMachineInstancePropertiesStorageProfileDataDisksItem` was added

* `models.StatusLevelTypes` was added

* `models.StorageContainersOperations` was added

* `models.StorageContainers$UpdateStages` was added

* `models.LogicalNetworksUpdateRequest` was added

* `models.GuestAgentInstallStatus` was added

* `models.LogicalNetworkPropertiesDhcpOptions` was added

* `models.VirtualMachineInstancePropertiesStorageProfileOsDisk` was added

* `models.VirtualMachineInstances` was added

* `models.VirtualMachineInstanceStatus` was added

* `models.IpAllocationMethodEnum` was added

* `models.GalleryImageStatusProvisioningStatus` was added

* `models.VirtualHardDisks$Update` was added

* `models.NetworkInterfaceStatusProvisioningStatus` was added

* `models.VirtualMachineConfigAgentInstanceView` was added

* `models.VirtualHardDiskStatusProvisioningStatus` was added

* `models.MarketplaceGalleryImages` was added

* `models.GuestAgent` was added

* `models.LogicalNetworksOperations` was added

* `models.VirtualHardDisks$UpdateStages` was added

* `models.LogicalNetworks$DefinitionStages` was added

* `models.StorageContainerStatusProvisioningStatus` was added

* `models.HybridIdentityMetadata` was added

* `models.VirtualMachineInstancePropertiesOsProfileLinuxConfiguration` was added

* `models.NetworkInterfaces$Update` was added

* `models.PowerStateEnum` was added

* `models.VmSizeEnum` was added

* `models.NetworkInterfaceStatus` was added

* `models.VirtualHardDisksUpdateRequest` was added

* `models.VirtualMachineInstanceListResult` was added

* `models.IpConfigurationPropertiesSubnet` was added

* `models.InstanceViewStatus` was added

* `models.LogicalNetworkStatus` was added

* `models.NetworkInterfacesUpdateRequest` was added

* `models.VirtualHardDisks` was added

* `models.VirtualHardDisksListResult` was added

* `models.OsProfileUpdateLinuxConfiguration` was added

* `models.SecurityTypes` was added

* `models.LogicalNetworks$UpdateStages` was added

* `models.SshConfiguration` was added

* `models.MarketplaceGalleryImages$Update` was added

* `models.HyperVGeneration` was added

* `models.Identity` was added

* `models.VirtualMachineInstancePropertiesHardwareProfile` was added

* `models.MarketplaceGalleryImageStatusProvisioningStatus` was added

* `models.StorageContainers` was added

* `models.StorageProfileUpdateDataDisksItem` was added

* `models.GalleryImagesOperations` was added

* `models.GalleryImages$Update` was added

* `models.GalleryDiskImage` was added

* `models.VirtualMachineInstancePropertiesStorageProfileImageReference` was added

* `models.InterfaceDnsSettings` was added

* `models.ExtendedLocationTypes` was added

* `models.NetworkInterfacesOperations` was added

* `models.LogicalNetworks` was added

* `models.VirtualHardDiskStatus` was added

* `models.GuestAgents` was added

* `models.NetworkInterfaces$UpdateStages` was added

* `models.GuestAgentList` was added

* `models.IpPoolTypeEnum` was added

* `models.Route` was added

* `models.LogicalNetworks$Definition` was added

* `models.VirtualMachineInstancePropertiesSecurityProfile` was added

* `models.NetworkProfileUpdate` was added

* `models.GalleryImages` was added

* `models.LogicalNetworks$Update` was added

* `models.StorageContainers$Definition` was added

* `models.LogicalNetworksListResult` was added

* `models.VirtualHardDisks$DefinitionStages` was added

* `models.StorageContainersUpdateRequest` was added

* `models.VirtualMachineInstancePropertiesStorageProfile` was added

* `models.IpPoolInfo` was added

* `models.VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem` was added

* `models.StorageContainerStatus` was added

* `models.VirtualHardDisksOperations` was added

* `models.GalleryImages$DefinitionStages` was added

* `models.GalleryImagesListResult` was added

* `models.HardwareProfileUpdate` was added

* `models.StorageContainersListResult` was added

* `models.ProvisioningStateEnum` was added

* `models.VirtualMachineInstanceView` was added

* `models.MarketplaceGalleryImages$UpdateStages` was added

* `models.GalleryImages$Definition` was added

* `models.VirtualMachineInstance` was added

* `models.GuestCredential` was added

* `models.NetworkInterfaces$Definition` was added

* `models.ExtendedLocation` was added

* `models.NetworkInterfaces$DefinitionStages` was added

* `models.MarketplaceGalleryImages$DefinitionStages` was added

* `models.VirtualHardDisks$Definition` was added

* `models.StorageContainers$DefinitionStages` was added

* `models.OperatingSystemTypes` was added

* `models.RouteTable` was added

* `models.ResourceIdentityType` was added

* `models.GalleryImageStatusDownloadStatus` was added

* `models.GalleryImages$UpdateStages` was added

* `models.MarketplaceGalleryImagesOperations` was added

#### `models.Operation` was modified

* `innerModel()` was added

#### `models.OperationListResult` was modified

* `validate()` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `AzureStackHciManager` was modified

* `hybridIdentityMetadatas()` was added
* `virtualMachineInstances()` was added
* `guestAgentsOperations()` was added
* `storageContainersOperations()` was added
* `logicalNetworksOperations()` was added
* `marketplaceGalleryImagesOperations()` was added
* `galleryImagesOperations()` was added
* `guestAgents()` was added
* `virtualHardDisksOperations()` was added
* `networkInterfacesOperations()` was added

## 1.0.0-beta.3 (2022-05-25)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Clusters` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

### Features Added

* `models.UploadCertificateRequest` was added

* `models.ClusterIdentityResponse` was added

* `models.ArcSetting$UpdateStages` was added

* `models.ArcSettingsPatch` was added

* `models.ArcSetting$Update` was added

* `models.ArcIdentityResponse` was added

* `models.PasswordCredential` was added

* `models.RawCertificateData` was added

#### `models.Extension` was modified

* `resourceGroupName()` was added

#### `models.Cluster` was modified

* `resourceGroupName()` was added
* `serviceEndpoint()` was added
* `aadServicePrincipalObjectId()` was added
* `aadApplicationObjectId()` was added
* `createIdentity(com.azure.core.util.Context)` was added
* `uploadCertificate(models.UploadCertificateRequest,com.azure.core.util.Context)` was added
* `uploadCertificate(models.UploadCertificateRequest)` was added
* `createIdentity()` was added

#### `models.ArcSetting$Definition` was modified

* `withArcApplicationClientId(java.lang.String)` was added
* `withArcServicePrincipalObjectId(java.lang.String)` was added
* `withConnectivityProperties(java.lang.Object)` was added
* `withArcApplicationObjectId(java.lang.String)` was added
* `withArcApplicationTenantId(java.lang.String)` was added

#### `models.Clusters` was modified

* `createIdentity(java.lang.String,java.lang.String)` was added
* `uploadCertificate(java.lang.String,java.lang.String,models.UploadCertificateRequest)` was added
* `uploadCertificate(java.lang.String,java.lang.String,models.UploadCertificateRequest,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createIdentity(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ArcSetting` was modified

* `arcServicePrincipalObjectId()` was added
* `createIdentity(com.azure.core.util.Context)` was added
* `generatePassword()` was added
* `resourceGroupName()` was added
* `arcApplicationTenantId()` was added
* `createIdentity()` was added
* `connectivityProperties()` was added
* `arcApplicationClientId()` was added
* `update()` was added
* `arcApplicationObjectId()` was added
* `generatePasswordWithResponse(com.azure.core.util.Context)` was added

#### `models.Cluster$Definition` was modified

* `withAadApplicationObjectId(java.lang.String)` was added
* `withAadServicePrincipalObjectId(java.lang.String)` was added

#### `models.ArcSettings` was modified

* `generatePassword(java.lang.String,java.lang.String,java.lang.String)` was added
* `createIdentity(java.lang.String,java.lang.String,java.lang.String)` was added
* `createIdentity(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `generatePasswordWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2022-04-12)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AvailableOperations` was removed

* `models.OperationDetail` was removed

* `models.CreatedByType` was removed

* `models.ClusterUpdate` was removed

#### `models.Cluster` was modified

* `lastModifiedBy()` was removed
* `createdByType()` was removed
* `createdBy()` was removed
* `lastModifiedByType()` was removed
* `createdAt()` was removed
* `lastModifiedAt()` was removed

#### `models.OperationDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.Operations` was modified

* `models.AvailableOperations list()` -> `models.OperationListResult list()`

#### `models.Cluster$Definition` was modified

* `withCreatedBy(java.lang.String)` was removed
* `withCreatedByType(models.CreatedByType)` was removed
* `withReportedProperties(models.ClusterReportedProperties)` was removed
* `withLastModifiedBy(java.lang.String)` was removed
* `withLastModifiedByType(models.CreatedByType)` was removed
* `withCreatedAt(java.time.OffsetDateTime)` was removed
* `withLastModifiedAt(java.time.OffsetDateTime)` was removed

### Features Added

* `models.Extension` was added

* `models.DiagnosticLevel` was added

* `models.WindowsServerSubscription` was added

* `models.Operation` was added

* `models.ClusterDesiredProperties` was added

* `models.Extension$DefinitionStages` was added

* `models.Extension$UpdateStages` was added

* `models.NodeExtensionState` was added

* `models.ArcSetting$DefinitionStages` was added

* `models.ClusterPatch` was added

* `models.ArcSetting$Definition` was added

* `models.ActionType` was added

* `models.PerNodeState` was added

* `models.OperationListResult` was added

* `models.Extension$Definition` was added

* `models.ExtensionList` was added

* `models.ArcSetting` was added

* `models.ArcSettingAggregateState` was added

* `models.Extension$Update` was added

* `models.PerNodeExtensionState` was added

* `models.Origin` was added

* `models.NodeArcState` was added

* `models.ArcSettings` was added

* `models.ImdsAttestation` was added

* `models.Extensions` was added

* `models.ExtensionAggregateState` was added

* `models.ArcSettingList` was added

#### `models.Cluster$Update` was modified

* `withCloudManagementEndpoint(java.lang.String)` was added
* `withDesiredProperties(models.ClusterDesiredProperties)` was added
* `withAadClientId(java.lang.String)` was added
* `withAadTenantId(java.lang.String)` was added

#### `AzureStackHciManager` was modified

* `arcSettings()` was added
* `extensions()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Cluster` was modified

* `cloudManagementEndpoint()` was added
* `desiredProperties()` was added
* `systemData()` was added

#### `models.ClusterNode` was modified

* `windowsServerSubscription()` was added

#### `models.ClusterReportedProperties` was modified

* `diagnosticLevel()` was added
* `imdsAttestation()` was added
* `withDiagnosticLevel(models.DiagnosticLevel)` was added

#### `models.Cluster$Definition` was modified

* `withDesiredProperties(models.ClusterDesiredProperties)` was added
* `withCloudManagementEndpoint(java.lang.String)` was added

#### `AzureStackHciManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2020-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
