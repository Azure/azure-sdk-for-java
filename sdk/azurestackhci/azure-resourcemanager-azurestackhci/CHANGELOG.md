# Release History

## 1.0.0-beta.6 (2025-12-17)

- Azure Resource Manager Azure Stack Hci client library for Java. This package contains Microsoft Azure SDK for Azure Stack Hci Management SDK. Azure Stack HCI service. Package api-version 2025-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SkuList` was removed

#### `models.EdgeDeviceListResult` was removed

#### `models.UpdateRunList` was removed

#### `models.OperationListResult` was removed

#### `models.ClusterList` was removed

#### `models.UpdateSummariesList` was removed

#### `models.ArcSettingList` was removed

#### `models.SecuritySettingListResult` was removed

#### `models.UpdateList` was removed

#### `models.PublisherList` was removed

#### `models.OfferList` was removed

#### `models.ExtensionList` was removed

#### `models.DeploymentSettingListResult` was removed

#### `models.SoftwareAssuranceChangeRequestProperties` was modified

* `validate()` was removed

#### `models.IsolatedVmAttestationConfiguration` was modified

* `IsolatedVmAttestationConfiguration()` was changed to private access
* `validate()` was removed

#### `models.HciOsProfile` was modified

* `HciOsProfile()` was changed to private access
* `validate()` was removed

#### `models.SoftwareAssuranceChangeRequest` was modified

* `validate()` was removed

#### `models.ClusterNode` was modified

* `ClusterNode()` was changed to private access
* `java.lang.Float coreCount()` -> `java.lang.Double coreCount()`
* `java.lang.Float memoryInGiB()` -> `java.lang.Double memoryInGiB()`
* `java.lang.Float id()` -> `java.lang.Double id()`
* `validate()` was removed

#### `models.SwitchDetail` was modified

* `SwitchDetail()` was changed to private access
* `validate()` was removed

#### `models.PrecheckResultTags` was modified

* `validate()` was removed

#### `models.HciEdgeDevice` was modified

* `validate()` was removed

#### `models.DeploymentData` was modified

* `validate()` was removed

#### `models.HciReportedProperties` was modified

* `HciReportedProperties()` was changed to private access
* `validate()` was removed

#### `models.LogCollectionRequestProperties` was modified

* `validate()` was removed

#### `models.HciEdgeDeviceProperties` was modified

* `validate()` was removed

#### `models.SoftwareAssuranceProperties` was modified

* `validate()` was removed

#### `models.Operation` was modified

* `Operation()` was removed
* `withDisplay(models.OperationDisplay)` was removed
* `models.ActionType actionType()` -> `models.ActionType actionType()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `models.Origin origin()` -> `models.Origin origin()`
* `validate()` was removed
* `java.lang.Boolean isDataAction()` -> `java.lang.Boolean isDataAction()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String name()` -> `java.lang.String name()`

#### `models.ClusterDesiredProperties` was modified

* `validate()` was removed

#### `models.HciValidationFailureDetail` was modified

* `HciValidationFailureDetail()` was changed to private access
* `validate()` was removed

#### `models.SecuritySetting$Update` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.ArcSetting$Definition` was modified

* `withConnectivityProperties(java.lang.Object)` was removed

#### `models.ClusterReportedProperties` was modified

* `ClusterReportedProperties()` was changed to private access
* `withDiagnosticLevel(models.DiagnosticLevel)` was removed
* `validate()` was removed

#### `models.PhysicalNodes` was modified

* `validate()` was removed

#### `models.Storage` was modified

* `validate()` was removed

#### `models.EceDeploymentSecrets` was modified

* `validate()` was removed

#### `models.DeviceConfiguration` was modified

* `validate()` was removed

#### `models.ExtensionUpgradeParameters` was modified

* `validate()` was removed

#### `models.DeploymentCluster` was modified

* `validate()` was removed

#### `models.ExtensionPatchParameters` was modified

* `validate()` was removed

#### `models.DeploymentSettingHostNetwork` was modified

* `validate()` was removed

#### `models.DeploymentStep` was modified

* `DeploymentStep()` was changed to private access
* `validate()` was removed

#### `models.EceActionStatus` was modified

* `EceActionStatus()` was changed to private access
* `validate()` was removed

#### `models.Cluster` was modified

* `java.lang.Float trialDaysRemaining()` -> `java.lang.Double trialDaysRemaining()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.UploadCertificateRequest` was modified

* `validate()` was removed

#### `models.LogCollectionRequest` was modified

* `validate()` was removed

#### `models.PerNodeState` was modified

* `PerNodeState()` was changed to private access
* `validate()` was removed

#### `models.ScaleUnits` was modified

* `validate()` was removed

#### `models.ExtensionPatch` was modified

* `validate()` was removed

#### `models.OptionalServices` was modified

* `validate()` was removed

#### `models.HciEdgeDeviceAdapterPropertyOverrides` was modified

* `HciEdgeDeviceAdapterPropertyOverrides()` was changed to private access
* `validate()` was removed

#### `models.DeploymentSettingStorageAdapterIpInfo` was modified

* `validate()` was removed

#### `models.SkuMappings` was modified

* `SkuMappings()` was changed to private access
* `withMarketplaceSkuVersions(java.util.List)` was removed
* `validate()` was removed
* `withCatalogPlanId(java.lang.String)` was removed
* `withMarketplaceSkuId(java.lang.String)` was removed

#### `models.ValidateRequest` was modified

* `validate()` was removed

#### `models.SdnIntegration` was modified

* `validate()` was removed

#### `models.HciNetworkProfile` was modified

* `HciNetworkProfile()` was changed to private access
* `validate()` was removed

#### `models.LogCollectionProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.InfrastructureNetwork` was modified

* `validate()` was removed

#### `models.HciEdgeDeviceArcExtension` was modified

* `HciEdgeDeviceArcExtension()` was changed to private access
* `validate()` was removed

#### `models.ExtensionInstanceViewStatus` was modified

* `ExtensionInstanceViewStatus()` was changed to private access
* `withLevel(models.StatusLevelTypes)` was removed
* `withTime(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withDisplayStatus(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.NetworkController` was modified

* `validate()` was removed

#### `models.HciEdgeDeviceIntents` was modified

* `HciEdgeDeviceIntents()` was changed to private access
* `validate()` was removed

#### `models.LogCollectionSession` was modified

* `LogCollectionSession()` was changed to private access
* `validate()` was removed

#### `models.SwitchExtension` was modified

* `SwitchExtension()` was changed to private access
* `validate()` was removed

#### `models.PerNodeRemoteSupportSession` was modified

* `PerNodeRemoteSupportSession()` was changed to private access
* `validate()` was removed

#### `models.SbeDeploymentPackageInfo` was modified

* `SbeDeploymentPackageInfo()` was changed to private access
* `validate()` was removed

#### `models.SecurityComplianceStatus` was modified

* `SecurityComplianceStatus()` was changed to private access
* `validate()` was removed

#### `models.IpPools` was modified

* `validate()` was removed

#### `models.ReportedProperties` was modified

* `validate()` was removed

#### `models.RemoteSupportProperties` was modified

* `validate()` was removed

#### `models.QosPolicyOverrides` was modified

* `validate()` was removed

#### `models.DeploymentSettingVirtualSwitchConfigurationOverrides` was modified

* `validate()` was removed

#### `models.SbePartnerProperties` was modified

* `validate()` was removed

#### `models.NicDetail` was modified

* `validate()` was removed

#### `models.RawCertificateData` was modified

* `validate()` was removed

#### `models.HciNicDetail` was modified

* `HciNicDetail()` was changed to private access
* `validate()` was removed

#### `models.EdgeDeviceProperties` was modified

* `validate()` was removed

#### `models.UpdatePrerequisite` was modified

* `validate()` was removed

#### `models.EceReportedProperties` was modified

* `EceReportedProperties()` was changed to private access
* `validate()` was removed

#### `models.HciEdgeDeviceStorageNetworks` was modified

* `HciEdgeDeviceStorageNetworks()` was changed to private access
* `validate()` was removed

#### `models.HciUpdate$Update` was modified

* `withPackageSizeInMb(java.lang.Float)` was removed
* `withProgressPercentage(java.lang.Float)` was removed

#### `models.ArcSetting` was modified

* `java.lang.Object connectivityProperties()` -> `models.ArcConnectivityProperties connectivityProperties()`

#### `models.DeploymentSettingAdapterPropertyOverrides` was modified

* `validate()` was removed

#### `models.SbePartnerInfo` was modified

* `validate()` was removed

#### `models.DeploymentSettingIntents` was modified

* `validate()` was removed

#### `models.DefaultExtensionDetails` was modified

* `DefaultExtensionDetails()` was changed to private access
* `validate()` was removed

#### `models.DeploymentSecuritySettings` was modified

* `validate()` was removed

#### `models.RemoteSupportRequest` was modified

* `validate()` was removed

#### `models.SecuritySetting$Definition` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.SbeCredentials` was modified

* `validate()` was removed

#### `models.ClusterPatch` was modified

* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.HciUpdate$Definition` was modified

* `withPackageSizeInMb(java.lang.Float)` was removed
* `withProgressPercentage(java.lang.Float)` was removed

#### `models.HciUpdate` was modified

* `java.lang.Float packageSizeInMb()` -> `java.lang.Double packageSizeInMb()`
* `java.lang.Float progressPercentage()` -> `java.lang.Double progressPercentage()`

#### `models.ArcSetting$Update` was modified

* `withConnectivityProperties(java.lang.Object)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `validate()` was removed

#### `models.Operations` was modified

* `models.OperationListResult list()` -> `com.azure.core.http.rest.PagedIterable list()`
* `listWithResponse(com.azure.core.util.Context)` was removed

#### `models.ExtensionProfile` was modified

* `ExtensionProfile()` was changed to private access
* `validate()` was removed

#### `models.HciEdgeDeviceHostNetwork` was modified

* `HciEdgeDeviceHostNetwork()` was changed to private access
* `validate()` was removed

#### `models.HciEdgeDeviceStorageAdapterIpInfo` was modified

* `HciEdgeDeviceStorageAdapterIpInfo()` was changed to private access
* `validate()` was removed

#### `models.SbeDeploymentInfo` was modified

* `validate()` was removed

#### `models.RemoteSupportNodeSettings` was modified

* `RemoteSupportNodeSettings()` was changed to private access
* `validate()` was removed

#### `models.PrecheckResult` was modified

* `validate()` was removed

#### `AzureStackHciManager` was modified

* `fluent.AzureStackHciClient serviceClient()` -> `fluent.AzureStackHciManagementClient serviceClient()`

#### `models.DeploymentConfiguration` was modified

* `validate()` was removed

#### `models.HciEdgeDeviceVirtualSwitchConfigurationOverrides` was modified

* `HciEdgeDeviceVirtualSwitchConfigurationOverrides()` was changed to private access
* `validate()` was removed

#### `models.RemoteSupportRequestProperties` was modified

* `validate()` was removed

#### `models.LogCollectionError` was modified

* `LogCollectionError()` was changed to private access
* `validate()` was removed

#### `models.ArcSettingsPatch` was modified

* `java.lang.Object connectivityProperties()` -> `models.ArcConnectivityProperties connectivityProperties()`
* `withConnectivityProperties(java.lang.Object)` was removed
* `validate()` was removed

#### `models.PerNodeExtensionState` was modified

* `PerNodeExtensionState()` was changed to private access
* `validate()` was removed

#### `models.ExtensionInstanceView` was modified

* `ExtensionInstanceView()` was changed to private access
* `withStatus(models.ExtensionInstanceViewStatus)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withTypeHandlerVersion(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.PackageVersionInfo` was modified

* `validate()` was removed

#### `models.DeploymentSettingStorageNetworks` was modified

* `validate()` was removed

#### `models.Observability` was modified

* `validate()` was removed

### Features Added

* `models.RemoteSupportSession` was added

* `models.JobStatus` was added

* `models.StorageConfiguration` was added

* `models.DnsServerConfig` was added

* `models.HciEdgeDeviceJobProperties` was added

* `models.HciHardwareProfile` was added

* `models.IgvmStatusDetail` was added

* `models.ValidatedSolutionRecipes` was added

* `models.ProvisionOsJobProperties` was added

* `models.EdgeMachinePatch` was added

* `models.HciEdgeDeviceJobType` was added

* `models.DownloadRequest` was added

* `models.ValidatedSolutionRecipeComponent` was added

* `models.ReconcileArcSettingsRequest` was added

* `models.ReconcileArcSettingsRequestProperties` was added

* `models.UpdateContent` was added

* `models.ProvisionOsReportedProperties` was added

* `models.SecretType` was added

* `models.PlatformUpdateProperties` was added

* `models.EdgeMachineProperties` was added

* `models.LocalAvailabilityZones` was added

* `models.EdgeDeviceJobs` was added

* `models.ValidatedSolutionRecipeComponentMetadata` was added

* `models.EdgeMachineJob` was added

* `models.DnsZones` was added

* `models.UpdateContents` was added

* `models.ContentPayload` was added

* `models.ValidateOwnershipVouchersRequest` was added

* `models.TimeConfiguration` was added

* `models.EdgeMachineJobProperties` was added

* `models.EdgeMachineRemoteSupportJobProperties` was added

* `models.HciStorageProfile` was added

* `models.EdgeMachines` was added

* `models.ValidatedSolutionRecipeComponentPayload` was added

* `models.ClusterPattern` was added

* `models.EdgeMachine$Definition` was added

* `models.PlatformPayload` was added

* `models.ConfidentialVmProfile` was added

* `models.ConfidentialVmIntent` was added

* `models.ValidatedSolutionRecipeInfo` was added

* `models.OsImages` was added

* `models.EdgeMachineJobs` was added

* `models.IpAddressRange` was added

* `models.AssemblyInfo` was added

* `models.OSOperationType` was added

* `models.KubernetesVersion` was added

* `models.SecretsLocationsChangeRequest` was added

* `models.EdgeMachine$DefinitionStages` was added

* `models.OsImageProperties` was added

* `models.EdgeMachineJob$DefinitionStages` was added

* `models.EdgeMachineRemoteSupportJobReportedProperties` was added

* `models.DownloadOsJobProperties` was added

* `models.EdgeMachineJob$Definition` was added

* `models.RemoteSupportAccessLevel` was added

* `models.OnboardingConfiguration` was added

* `models.PlatformUpdates` was added

* `models.LogCollectionJobSession` was added

* `models.ValidatedSolutionRecipeCapabilities` was added

* `models.SecretsType` was added

* `models.AssemblyInfoPayload` was added

* `models.ClusterSdnProperties` was added

* `models.KubernetesVersions` was added

* `models.RemoteSupportJobReportedProperties` was added

* `models.EdgeMachineNicDetail` was added

* `models.OnboardingResourceType` was added

* `models.EdgeMachine` was added

* `models.UpdateContentProperties` was added

* `models.EdgeMachineKind` was added

* `models.RemoteSupportJobNodeSettings` was added

* `models.RdmaCapability` was added

* `models.OwnershipVoucherValidationStatus` was added

* `models.SdnStatus` was added

* `models.OwnershipVoucherDetails` was added

* `models.OsProvisionProfile` was added

* `models.SiteDetails` was added

* `models.ValidateOwnershipVouchersResponse` was added

* `models.SupportStatus` was added

* `models.IgvmStatus` was added

* `models.UserDetails` was added

* `models.DeviceLogCollectionStatus` was added

* `models.TargetDeviceConfiguration` was added

* `models.ValidatedSolutionRecipeContent` was added

* `models.IpAssignmentType` was added

* `models.ServiceConfiguration` was added

* `models.ChangeRingRequestProperties` was added

* `models.NetworkConfiguration` was added

* `models.ProvisioningRequest` was added

* `models.DownloadOsProfile` was added

* `models.PlatformUpdateDetails` was added

* `models.OsImage` was added

* `models.SecretsLocationDetails` was added

* `models.ConfidentialVmProperties` was added

* `models.ArcConnectivityProperties` was added

* `models.EdgeMachineReportedProperties` was added

* `models.SdnIntegrationIntent` was added

* `models.EdgeMachineRemoteSupportNodeSettings` was added

* `models.HciRemoteSupportJobProperties` was added

* `models.OwnershipVoucherValidationDetails` was added

* `models.IdentityProvider` was added

* `models.NetworkAdapter` was added

* `models.KubernetesVersionProperties` was added

* `models.ProvisioningOsType` was added

* `models.HardwareClass` was added

* `models.EdgeMachineJob$Update` was added

* `models.EdgeMachine$UpdateStages` was added

* `models.EdgeMachineJobType` was added

* `models.OwnerKeyType` was added

* `models.EdgeMachineCollectLogJobReportedProperties` was added

* `models.OperationDetail` was added

* `models.StorageProfile` was added

* `models.HardwareProfile` was added

* `models.EdgeMachineConnectivityStatus` was added

* `models.ValidatedSolutionRecipeCapability` was added

* `models.ServiceName` was added

* `models.OwnershipVouchers` was added

* `models.EdgeMachineCollectLogJobProperties` was added

* `models.EdgeDeviceJob` was added

* `models.LogCollectionReportedProperties` was added

* `models.EdgeMachineState` was added

* `models.WebProxyConfiguration` was added

* `models.SdnProperties` was added

* `models.EdgeMachineJob$UpdateStages` was added

* `models.EdgeDeviceKind` was added

* `models.ChangeRingRequest` was added

* `models.ValidatedSolutionRecipeProperties` was added

* `models.ProvisioningDetails` was added

* `models.HciEdgeDeviceJob` was added

* `models.EdgeMachine$Update` was added

* `models.OsProfile` was added

* `models.PlatformUpdate` was added

* `models.ConfidentialVmStatus` was added

* `models.ValidatedSolutionRecipe` was added

* `models.EdgeMachineNetworkProfile` was added

* `models.HciCollectLogJobProperties` was added

#### `models.DeploymentData` was modified

* `isManagementCluster()` was added
* `withIsManagementCluster(java.lang.Boolean)` was added
* `withAssemblyInfo(models.AssemblyInfo)` was added
* `assemblyInfo()` was added
* `identityProvider()` was added
* `withLocalAvailabilityZones(java.util.List)` was added
* `withIdentityProvider(models.IdentityProvider)` was added
* `localAvailabilityZones()` was added

#### `models.HciReportedProperties` was modified

* `confidentialVmProfile()` was added
* `storageProfile()` was added
* `hardwareProfile()` was added
* `lastSyncTimestamp()` was added

#### `models.Operation` was modified

* `innerModel()` was added

#### `models.ArcSetting$Definition` was modified

* `withConnectivityProperties(models.ArcConnectivityProperties)` was added

#### `models.Clusters` was modified

* `changeRing(java.lang.String,java.lang.String,models.ChangeRingRequest,com.azure.core.util.Context)` was added
* `updateSecretsLocations(java.lang.String,java.lang.String,models.SecretsLocationsChangeRequest,com.azure.core.util.Context)` was added
* `changeRing(java.lang.String,java.lang.String,models.ChangeRingRequest)` was added
* `updateSecretsLocations(java.lang.String,java.lang.String,models.SecretsLocationsChangeRequest)` was added

#### `models.ClusterReportedProperties` was modified

* `msiExpirationTimeStamp()` was added
* `hardwareClass()` was added

#### `models.DeploymentCluster` was modified

* `clusterPattern()` was added
* `withClusterPattern(models.ClusterPattern)` was added
* `hardwareClass()` was added

#### `models.Cluster` was modified

* `confidentialVmProperties()` was added
* `clusterPattern()` was added
* `localAvailabilityZones()` was added
* `changeRing(models.ChangeRingRequest)` was added
* `secretsLocations()` was added
* `changeRing(models.ChangeRingRequest,com.azure.core.util.Context)` was added
* `sdnProperties()` was added
* `supportStatus()` was added
* `updateSecretsLocations(models.SecretsLocationsChangeRequest)` was added
* `kind()` was added
* `updateSecretsLocations(models.SecretsLocationsChangeRequest,com.azure.core.util.Context)` was added
* `ring()` was added
* `isManagementCluster()` was added
* `identityProvider()` was added

#### `models.HciNetworkProfile` was modified

* `sdnProperties()` was added

#### `models.InfrastructureNetwork` was modified

* `withDnsZones(java.util.List)` was added
* `withDnsServerConfig(models.DnsServerConfig)` was added
* `dnsServerConfig()` was added
* `dnsZones()` was added

#### `models.ReportedProperties` was modified

* `confidentialVmProfile()` was added
* `lastSyncTimestamp()` was added

#### `models.ArcSettings` was modified

* `reconcile(java.lang.String,java.lang.String,java.lang.String,models.ReconcileArcSettingsRequest,com.azure.core.util.Context)` was added
* `reconcile(java.lang.String,java.lang.String,java.lang.String,models.ReconcileArcSettingsRequest)` was added

#### `models.HciNicDetail` was modified

* `rdmaCapability()` was added

#### `models.HciUpdate$Update` was modified

* `withPackageSizeInMb(java.lang.Double)` was added
* `withProgressPercentage(java.lang.Double)` was added

#### `models.ArcSetting` was modified

* `reconcile(models.ReconcileArcSettingsRequest,com.azure.core.util.Context)` was added
* `reconcile(models.ReconcileArcSettingsRequest)` was added

#### `models.HciUpdate$Definition` was modified

* `withPackageSizeInMb(java.lang.Double)` was added
* `withProgressPercentage(java.lang.Double)` was added

#### `models.ArcSetting$Update` was modified

* `withConnectivityProperties(models.ArcConnectivityProperties)` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.Cluster$Definition` was modified

* `withLocalAvailabilityZones(java.util.List)` was added
* `withSecretsLocations(java.util.List)` was added
* `withKind(java.lang.String)` was added

#### `AzureStackHciManager` was modified

* `ownershipVouchers()` was added
* `edgeDeviceJobs()` was added
* `platformUpdates()` was added
* `updateContents()` was added
* `validatedSolutionRecipes()` was added
* `kubernetesVersions()` was added
* `edgeMachineJobs()` was added
* `edgeMachines()` was added
* `osImages()` was added

#### `models.ArcSettingsPatch` was modified

* `withConnectivityProperties(models.ArcConnectivityProperties)` was added

## 1.0.0-beta.5 (2024-08-26)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.GalleryOSDiskImage` was removed

* `models.Subnet` was removed

* `models.IpConfiguration` was removed

* `models.LogicalNetworkStatusProvisioningStatus` was removed

* `models.NetworkProfileUpdateNetworkInterfacesItem` was removed

* `models.VirtualMachineInstanceUpdateRequest` was removed

* `models.GalleryImageVersion` was removed

* `models.IpPool` was removed

* `models.MarketplaceGalleryImageStatusDownloadStatus` was removed

* `models.IpConfigurationProperties` was removed

* `models.VirtualMachineInstancePropertiesOsProfile` was removed

* `models.MarketplaceGalleryImages$Definition` was removed

* `models.NetworkInterfaces` was removed

* `models.SubnetPropertiesFormatIpConfigurationReferencesItem` was removed

* `models.GalleryImagesUpdateRequest` was removed

* `models.OsProfileUpdate` was removed

* `models.VirtualMachineInstancePropertiesOsProfileWindowsConfiguration` was removed

* `models.HybridIdentityMetadataList` was removed

* `models.VirtualMachineInstancePropertiesStorageProfileDataDisksItem` was removed

* `models.StorageContainersOperations` was removed

* `models.StorageContainers$UpdateStages` was removed

* `models.LogicalNetworksUpdateRequest` was removed

* `models.GuestAgentInstallStatus` was removed

* `models.LogicalNetworkPropertiesDhcpOptions` was removed

* `models.VirtualMachineInstanceStatus` was removed

* `models.IpAllocationMethodEnum` was removed

* `models.VirtualMachineConfigAgentInstanceView` was removed

* `models.MarketplaceGalleryImages` was removed

* `models.LogicalNetworks$DefinitionStages` was removed

* `models.HybridIdentityMetadata` was removed

* `models.VirtualMachineInstancePropertiesOsProfileLinuxConfiguration` was removed

* `models.NetworkInterfaces$Update` was removed

* `models.NetworkInterfaceStatus` was removed

* `models.VirtualHardDisksUpdateRequest` was removed

* `models.IpConfigurationPropertiesSubnet` was removed

* `models.InstanceViewStatus` was removed

* `models.NetworkInterfacesUpdateRequest` was removed

* `models.VirtualHardDisksListResult` was removed

* `models.OsProfileUpdateLinuxConfiguration` was removed

* `models.LogicalNetworks$UpdateStages` was removed

* `models.SshConfiguration` was removed

* `models.MarketplaceGalleryImages$Update` was removed

* `models.HyperVGeneration` was removed

* `models.VirtualMachineInstancePropertiesHardwareProfile` was removed

* `models.MarketplaceGalleryImageStatusProvisioningStatus` was removed

* `models.StorageProfileUpdateDataDisksItem` was removed

* `models.GalleryImages$Update` was removed

* `models.GalleryDiskImage` was removed

* `models.InterfaceDnsSettings` was removed

* `models.NetworkInterfacesOperations` was removed

* `models.LogicalNetworks` was removed

* `models.GuestAgents` was removed

* `models.NetworkInterfaces$UpdateStages` was removed

* `models.IpPoolTypeEnum` was removed

* `models.Route` was removed

* `models.LogicalNetworks$Definition` was removed

* `models.VirtualMachineInstancePropertiesSecurityProfile` was removed

* `models.NetworkProfileUpdate` was removed

* `models.LogicalNetworks$Update` was removed

* `models.StorageContainers$Definition` was removed

* `models.VirtualHardDisks$DefinitionStages` was removed

* `models.StorageContainersUpdateRequest` was removed

* `models.VirtualMachineInstancePropertiesStorageProfile` was removed

* `models.IpPoolInfo` was removed

* `models.VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem` was removed

* `models.StorageContainerStatus` was removed

* `models.GalleryImagesListResult` was removed

* `models.ProvisioningStateEnum` was removed

* `models.NetworkInterfaces$Definition` was removed

* `models.OperatingSystemTypes` was removed

* `models.ResourceIdentityType` was removed

* `models.GalleryImageStatusDownloadStatus` was removed

* `models.GalleryImages$UpdateStages` was removed

* `models.MarketplaceGalleryImagesOperations` was removed

* `models.DiskFileFormat` was removed

* `models.GalleryImageStatus` was removed

* `models.StorageProfileUpdate` was removed

* `models.VirtualMachineInstancePropertiesHardwareProfileDynamicMemoryConfig` was removed

* `models.GalleryImageIdentifier` was removed

* `models.GuestAgentsOperations` was removed

* `models.NetworkInterfacesListResult` was removed

* `models.VirtualMachineInstancePropertiesSecurityProfileUefiSettings` was removed

* `models.VirtualMachineInstancePropertiesNetworkProfile` was removed

* `models.MarketplaceGalleryImagesListResult` was removed

* `models.MarketplaceGalleryImagesUpdateRequest` was removed

* `models.StatusTypes` was removed

* `models.CloudInitDataSource` was removed

* `models.OsProfileUpdateWindowsConfiguration` was removed

* `models.SshPublicKey` was removed

* `models.HttpProxyConfiguration` was removed

* `models.StorageContainers$Update` was removed

* `models.VirtualMachineInstanceStatusProvisioningStatus` was removed

* `models.VirtualMachineInstanceUpdateProperties` was removed

* `models.GalleryImageVersionStorageProfile` was removed

* `models.HybridIdentityMetadatas` was removed

* `models.ProvisioningAction` was removed

* `models.MarketplaceGalleryImageStatus` was removed

* `models.VirtualMachineInstancePropertiesStorageProfileOsDisk` was removed

* `models.VirtualMachineInstances` was removed

* `models.GalleryImageStatusProvisioningStatus` was removed

* `models.VirtualHardDisks$Update` was removed

* `models.NetworkInterfaceStatusProvisioningStatus` was removed

* `models.VirtualHardDiskStatusProvisioningStatus` was removed

* `models.GuestAgent` was removed

* `models.LogicalNetworksOperations` was removed

* `models.VirtualHardDisks$UpdateStages` was removed

* `models.StorageContainerStatusProvisioningStatus` was removed

* `models.PowerStateEnum` was removed

* `models.VmSizeEnum` was removed

* `models.VirtualMachineInstanceListResult` was removed

* `models.LogicalNetworkStatus` was removed

* `models.VirtualHardDisks` was removed

* `models.SecurityTypes` was removed

* `models.Identity` was removed

* `models.StorageContainers` was removed

* `models.GalleryImagesOperations` was removed

* `models.VirtualMachineInstancePropertiesStorageProfileImageReference` was removed

* `models.ExtendedLocationTypes` was removed

* `models.VirtualHardDiskStatus` was removed

* `models.GuestAgentList` was removed

* `models.GalleryImages` was removed

* `models.LogicalNetworksListResult` was removed

* `models.VirtualHardDisksOperations` was removed

* `models.GalleryImages$DefinitionStages` was removed

* `models.HardwareProfileUpdate` was removed

* `models.StorageContainersListResult` was removed

* `models.VirtualMachineInstanceView` was removed

* `models.MarketplaceGalleryImages$UpdateStages` was removed

* `models.GalleryImages$Definition` was removed

* `models.VirtualMachineInstance` was removed

* `models.GuestCredential` was removed

* `models.ExtendedLocation` was removed

* `models.NetworkInterfaces$DefinitionStages` was removed

* `models.MarketplaceGalleryImages$DefinitionStages` was removed

* `models.VirtualHardDisks$Definition` was removed

* `models.StorageContainers$DefinitionStages` was removed

* `models.RouteTable` was removed

#### `models.Operation` was modified

* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `innerModel()` was removed
* `models.Origin origin()` -> `models.Origin origin()`
* `models.ActionType actionType()` -> `models.ActionType actionType()`
* `java.lang.Boolean isDataAction()` -> `java.lang.Boolean isDataAction()`
* `java.lang.String name()` -> `java.lang.String name()`

#### `models.OperationListResult` was modified

* `validate()` was removed
* `java.util.List value()` -> `java.util.List value()`
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`

#### `models.Operations` was modified

* `com.azure.core.http.rest.PagedIterable list()` -> `models.OperationListResult list()`
* `list(com.azure.core.util.Context)` was removed

#### `AzureStackHciManager` was modified

* `storageContainersOperations()` was removed
* `guestAgents()` was removed
* `virtualMachineInstances()` was removed
* `hybridIdentityMetadatas()` was removed
* `guestAgentsOperations()` was removed
* `galleryImagesOperations()` was removed
* `virtualHardDisksOperations()` was removed
* `networkInterfacesOperations()` was removed
* `logicalNetworksOperations()` was removed
* `marketplaceGalleryImagesOperations()` was removed

### Features Added

* `models.SoftwareAssuranceChangeRequestProperties` was added

* `models.Cluster$Update` was added

* `models.HciUpdate$DefinitionStages` was added

* `models.IsolatedVmAttestationConfiguration` was added

* `models.LogCollectionJobType` was added

* `models.HciOsProfile` was added

* `models.SoftwareAssuranceChangeRequest` was added

* `models.ClusterNode` was added

* `models.SwitchDetail` was added

* `models.Extension$Definition` was added

* `models.PrecheckResultTags` was added

* `models.SkuList` was added

* `models.Updates` was added

* `models.UpdateRun$DefinitionStages` was added

* `models.EdgeDeviceListResult` was added

* `models.ArcIdentityResponse` was added

* `models.HciEdgeDevice` was added

* `models.DeploymentData` was added

* `models.AccessLevel` was added

* `models.ImdsAttestation` was added

* `models.Cluster$DefinitionStages` was added

* `models.ExtensionManagedBy` was added

* `models.HciReportedProperties` was added

* `models.ManagedServiceIdentityType` was added

* `models.LogCollectionRequestProperties` was added

* `models.WindowsServerSubscription` was added

* `models.ConnectivityStatus` was added

* `models.UpdateRuns` was added

* `models.HciEdgeDeviceProperties` was added

* `models.SoftwareAssuranceProperties` was added

* `models.ClusterDesiredProperties` was added

* `models.Extension$UpdateStages` was added

* `models.HciValidationFailureDetail` was added

* `models.DeviceKind` was added

* `models.SecuritySetting$Update` was added

* `models.ArcSetting$Definition` was added

* `models.Cluster$UpdateStages` was added

* `models.Clusters` was added

* `models.ClusterReportedProperties` was added

* `models.ArcSettingAggregateState` was added

* `models.PhysicalNodes` was added

* `models.OperationType` was added

* `models.Storage` was added

* `models.EceDeploymentSecrets` was added

* `models.DeviceConfiguration` was added

* `models.ExtensionUpgradeParameters` was added

* `models.DeploymentCluster` was added

* `models.RemoteSupportType` was added

* `models.ExtensionPatchParameters` was added

* `models.UpdateRunPropertiesState` was added

* `models.DeploymentSettingHostNetwork` was added

* `models.UpdateRunList` was added

* `models.HciUpdate$UpdateStages` was added

* `models.DeploymentStep` was added

* `models.EceActionStatus` was added

* `models.Offer` was added

* `models.Cluster` was added

* `models.UploadCertificateRequest` was added

* `models.ClusterIdentityResponse` was added

* `models.LogCollectionRequest` was added

* `models.PerNodeState` was added

* `models.DeploymentSettings` was added

* `models.ScaleUnits` was added

* `models.ClusterList` was added

* `models.ExtensionPatch` was added

* `models.OptionalServices` was added

* `models.HealthState` was added

* `models.HciEdgeDeviceAdapterPropertyOverrides` was added

* `models.DeploymentSettingStorageAdapterIpInfo` was added

* `models.NodeArcState` was added

* `models.UpdateSummariesList` was added

* `models.UpdateRun$UpdateStages` was added

* `models.ArcSettingList` was added

* `models.SkuMappings` was added

* `models.DeploymentSetting$UpdateStages` was added

* `models.DiagnosticLevel` was added

* `models.EdgeDevice` was added

* `models.ValidateRequest` was added

* `models.OemActivation` was added

* `models.SdnIntegration` was added

* `models.LogCollectionStatus` was added

* `models.ValidateResponse` was added

* `models.HciNetworkProfile` was added

* `models.LogCollectionProperties` was added

* `models.InfrastructureNetwork` was added

* `models.HciEdgeDeviceArcExtension` was added

* `models.UpdateSummariesPropertiesState` was added

* `models.ExtensionInstanceViewStatus` was added

* `models.NetworkController` was added

* `models.ExtensionAggregateState` was added

* `models.UpdateSummariesOperations` was added

* `models.Extension` was added

* `models.HciEdgeDeviceIntents` was added

* `models.LogCollectionSession` was added

* `models.NodeExtensionState` was added

* `models.SwitchExtension` was added

* `models.ArcSetting$DefinitionStages` was added

* `models.Offers` was added

* `models.PerNodeRemoteSupportSession` was added

* `models.SecuritySetting` was added

* `models.ArcExtensionState` was added

* `models.SbeDeploymentPackageInfo` was added

* `models.SoftwareAssuranceIntent` was added

* `models.SecurityComplianceStatus` was added

* `models.IpPools` was added

* `models.SecuritySettingListResult` was added

* `models.ComplianceAssignmentType` was added

* `models.ReportedProperties` was added

* `models.RemoteSupportProperties` was added

* `models.DeploymentMode` was added

* `models.UpdateList` was added

* `models.QosPolicyOverrides` was added

* `models.DeploymentSettingVirtualSwitchConfigurationOverrides` was added

* `models.SbePartnerProperties` was added

* `models.ArcSettings` was added

* `models.SoftwareAssuranceStatus` was added

* `models.NicDetail` was added

* `models.PublisherList` was added

* `models.RawCertificateData` was added

* `models.HciNicDetail` was added

* `models.UpdateRun` was added

* `models.EdgeDeviceProperties` was added

* `models.EceSecrets` was added

* `models.OfferList` was added

* `models.AvailabilityType` was added

* `models.UpdatePrerequisite` was added

* `models.EceReportedProperties` was added

* `models.DeploymentSetting$Definition` was added

* `models.HciEdgeDeviceStorageNetworks` was added

* `models.ExtensionList` was added

* `models.HciUpdate$Update` was added

* `models.ArcSetting` was added

* `models.DeploymentSettingAdapterPropertyOverrides` was added

* `models.SbePartnerInfo` was added

* `models.DeploymentSettingIntents` was added

* `models.DeploymentSettingListResult` was added

* `models.Sku` was added

* `models.DefaultExtensionDetails` was added

* `models.DeploymentSecuritySettings` was added

* `models.RebootRequirement` was added

* `models.RemoteSupportRequest` was added

* `models.Skus` was added

* `models.ProvisioningState` was added

* `models.SecuritySetting$Definition` was added

* `models.SbeCredentials` was added

* `models.State` was added

* `models.Publisher` was added

* `models.ArcSetting$UpdateStages` was added

* `models.ClusterPatch` was added

* `models.ComplianceStatus` was added

* `models.HciUpdate$Definition` was added

* `models.UpdateRun$Update` was added

* `models.Severity` was added

* `models.HciUpdate` was added

* `models.ArcSetting$Update` was added

* `models.UserAssignedIdentity` was added

* `models.ExtensionProfile` was added

* `models.Cluster$Definition` was added

* `models.DeploymentSetting$DefinitionStages` was added

* `models.Publishers` was added

* `models.HciEdgeDeviceHostNetwork` was added

* `models.HciEdgeDeviceStorageAdapterIpInfo` was added

* `models.SecuritySetting$UpdateStages` was added

* `models.Extensions` was added

* `models.SbeDeploymentInfo` was added

* `models.UpdateRun$Definition` was added

* `models.EdgeDevices` was added

* `models.RemoteSupportNodeSettings` was added

* `models.PrecheckResult` was added

* `models.DeploymentSetting` was added

* `models.DeploymentConfiguration` was added

* `models.Extension$DefinitionStages` was added

* `models.HciEdgeDeviceVirtualSwitchConfigurationOverrides` was added

* `models.DeviceState` was added

* `models.RemoteSupportRequestProperties` was added

* `models.LogCollectionError` was added

* `models.ArcSettingsPatch` was added

* `models.SecuritySetting$DefinitionStages` was added

* `models.DeploymentSetting$Update` was added

* `models.ClusterNodeType` was added

* `models.SecuritySettings` was added

* `models.Extension$Update` was added

* `models.PerNodeExtensionState` was added

* `models.UpdateSummaries` was added

* `models.ExtensionInstanceView` was added

* `models.PackageVersionInfo` was added

* `models.DeploymentSettingStorageNetworks` was added

* `models.Observability` was added

* `models.PasswordCredential` was added

#### `models.Operation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validate()` was added
* `withDisplay(models.OperationDisplay)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `innerModel()` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operations` was modified

* `listWithResponse(com.azure.core.util.Context)` was added

#### `AzureStackHciManager` was modified

* `deploymentSettings()` was added
* `updateSummariesOperations()` was added
* `securitySettings()` was added
* `skus()` was added
* `clusters()` was added
* `extensions()` was added
* `updateRuns()` was added
* `arcSettings()` was added
* `updates()` was added
* `edgeDevices()` was added
* `publishers()` was added
* `offers()` was added

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
