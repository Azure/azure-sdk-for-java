# Release History

## 1.0.0 (2023-04-17)

- Azure Resource Manager Workloads client library for Java. This package contains Microsoft Azure SDK for Workloads Management SDK. Workloads client provides access to various workload operations.<br>Azure Center for SAP solutions is currently in PREVIEW. See the [Azure Center for SAP solutions - Legal Terms](https://learn.microsoft.com/legal/azure-center-for-sap-solutions/azure-center-for-sap-solutions-legal-terms) for legal notices applicable to Azure Center for SAP solutions. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ImageReference` was modified

* `withSharedGalleryImageId(java.lang.String)` was removed
* `sharedGalleryImageId()` was removed
* `exactVersion()` was removed

## 1.0.0-beta.2 (2023-02-07)

- Azure Resource Manager Workloads client library for Java. This package contains Microsoft Azure SDK for Workloads Management SDK. Workloads client provides access to various workload operations. Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.UserProfile` was removed

* `models.NodeProfile` was removed

* `models.NetworkProfile` was removed

* `models.PhpWorkloadProvisioningState` was removed

* `models.SkuCost` was removed

* `models.WordpressVersions` was removed

* `models.DatabaseTier` was removed

* `models.OSImagePublisher` was removed

* `models.RedisCacheFamily` was removed

* `models.PhpWorkloads` was removed

* `models.OSImageSku` was removed

* `models.SearchType` was removed

* `models.PhpWorkloadResource$DefinitionStages` was removed

* `models.SiteProfile` was removed

* `models.VmssNodesProfile` was removed

* `models.WorkloadKind` was removed

* `models.LoadBalancerType` was removed

* `models.SkuRestrictionReasonCode` was removed

* `models.EnableSslEnforcement` was removed

* `models.BackupProfile` was removed

* `models.ApplicationProvisioningState` was removed

* `models.PhpWorkloadResource$Definition` was removed

* `models.HAEnabled` was removed

* `models.SkuRestriction` was removed

* `models.DatabaseProfile` was removed

* `models.WordpressInstances` was removed

* `models.SkusListResult` was removed

* `models.DiskInfo` was removed

* `models.WordpressInstanceResourceList` was removed

* `models.SkuZoneDetail` was removed

* `models.LocationType` was removed

* `models.SkuLocationAndZones` was removed

* `models.FileshareProfile` was removed

* `models.PhpProfile` was removed

* `models.FileShareType` was removed

* `models.Sku` was removed

* `models.OSImageVersion` was removed

* `models.OsImageProfile` was removed

* `models.DiskStorageType` was removed

* `models.CacheProfile` was removed

* `models.PhpWorkloadResource` was removed

* `models.AzureFrontDoorEnabled` was removed

* `models.PhpWorkloadResource$UpdateStages` was removed

* `models.DatabaseType` was removed

* `models.PhpWorkloadResource$Update` was removed

* `models.PatchResourceRequestBodyIdentity` was removed

* `models.OSImageOffer` was removed

* `models.SkuDefinition` was removed

* `models.PhpVersion` was removed

* `models.PhpWorkloadResourceList` was removed

* `models.PhpWorkloadResourceIdentity` was removed

* `models.SkuCapability` was removed

* `models.SkuTier` was removed

* `models.PatchResourceRequestBody` was removed

* `models.SearchProfile` was removed

* `models.SkuRestrictionType` was removed

* `models.FileShareStorageType` was removed

* `models.WordpressInstanceResource` was removed

* `models.EnableBackup` was removed

* `models.Skus` was removed

#### `models.SapApplicationServerInstance` was modified

* `virtualMachineId()` was removed

#### `models.StopRequest` was modified

* `hardStop()` was removed
* `withHardStop(java.lang.Boolean)` was removed

#### `models.SapDiskConfiguration` was modified

* `withDiskSizeGB(java.lang.Long)` was removed
* `diskCount()` was removed
* `diskType()` was removed
* `volume()` was removed
* `withDiskIopsReadWrite(java.lang.Long)` was removed
* `diskIopsReadWrite()` was removed
* `diskMBpsReadWrite()` was removed
* `withDiskType(java.lang.String)` was removed
* `diskStorageType()` was removed
* `withVolume(java.lang.String)` was removed
* `diskSizeGB()` was removed
* `withDiskStorageType(java.lang.String)` was removed
* `withDiskCount(java.lang.Long)` was removed
* `withDiskMBpsReadWrite(java.lang.Long)` was removed

#### `WorkloadsManager` was modified

* `wordpressInstances()` was removed
* `skus()` was removed
* `phpWorkloads()` was removed

#### `models.SapNetWeaverProviderInstanceProperties` was modified

* `sapSslCertificateUri()` was removed
* `withSapSslCertificateUri(java.lang.String)` was removed

#### `models.SapDiskConfigurationsResult` was modified

* `diskConfigurations()` was removed

#### `models.SapVirtualInstances` was modified

* `stop(java.lang.String,java.lang.String,models.StopRequest)` was removed

#### `models.SapVirtualInstance` was modified

* `stop(models.StopRequest)` was removed

#### `models.HanaDbProviderInstanceProperties` was modified

* `dbSslCertificateUri()` was removed
* `withDbSslCertificateUri(java.lang.String)` was removed

### Features Added

* `models.DiskSkuName` was added

* `models.SapLandscapeMonitorListResult` was added

* `models.VirtualMachineResourceNames` was added

* `models.DatabaseServerFullResourceNames` was added

* `models.SapLandscapeMonitors` was added

* `models.DiskVolumeConfiguration` was added

* `models.SingleServerCustomResourceNames` was added

* `models.ConfigurationType` was added

* `models.ApplicationServerVirtualMachineType` was added

* `models.SkipFileShareConfiguration` was added

* `models.LoadBalancerDetails` was added

* `models.SapLandscapeMonitorPropertiesGrouping` was added

* `models.ExternalInstallationSoftwareConfiguration` was added

* `models.SapLandscapeMonitorSidMapping` was added

* `models.ApplicationServerFullResourceNames` was added

* `models.DiskDetails` was added

* `models.CentralServerFullResourceNames` was added

* `models.SslPreference` was added

* `models.NamingPatternType` was added

* `models.CreateAndMountFileShareConfiguration` was added

* `models.NetworkInterfaceResourceNames` was added

* `models.StorageInformation` was added

* `models.ThreeTierCustomResourceNames` was added

* `models.DiskSku` was added

* `models.SharedStorageResourceNames` was added

* `models.LoadBalancerResourceNames` was added

* `models.StorageConfiguration` was added

* `models.SapLandscapeMonitorMetricThresholds` was added

* `models.SapLandscapeMonitorProvisioningState` was added

* `models.ThreeTierFullResourceNames` was added

* `models.DiskConfiguration` was added

* `models.MountFileShareConfiguration` was added

* `models.FileShareConfiguration` was added

* `models.ApplicationServerVmDetails` was added

* `models.SingleServerFullResourceNames` was added

* `models.SapLandscapeMonitor` was added

#### `models.SapCentralServerInstance` was modified

* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was added
* `loadBalancerDetails()` was added
* `stopInstance()` was added
* `startInstance(com.azure.core.util.Context)` was added
* `startInstance()` was added

#### `models.Monitor` was modified

* `storageAccountArmId()` was added
* `zoneRedundancyPreference()` was added

#### `models.DiscoveryConfiguration` was modified

* `managedRgStorageAccountName()` was added
* `withManagedRgStorageAccountName(java.lang.String)` was added

#### `models.SapApplicationServerInstance` was modified

* `vmDetails()` was added
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was added
* `startInstance(com.azure.core.util.Context)` was added
* `stopInstance()` was added
* `startInstance()` was added
* `loadBalancerDetails()` was added

#### `models.StopRequest` was modified

* `softStopTimeoutSeconds()` was added
* `withSoftStopTimeoutSeconds(java.lang.Long)` was added

#### `models.SapDiskConfiguration` was modified

* `recommendedConfiguration()` was added
* `withRecommendedConfiguration(models.DiskVolumeConfiguration)` was added
* `withSupportedConfigurations(java.util.List)` was added
* `supportedConfigurations()` was added

#### `models.ThreeTierConfiguration` was modified

* `customResourceNames()` was added
* `withStorageConfiguration(models.StorageConfiguration)` was added
* `withCustomResourceNames(models.ThreeTierCustomResourceNames)` was added
* `storageConfiguration()` was added

#### `WorkloadsManager` was modified

* `sapLandscapeMonitors()` was added

#### `models.SapDatabaseInstances` was modified

* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was added
* `startInstance(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.SapNetWeaverProviderInstanceProperties` was modified

* `withSslPreference(models.SslPreference)` was added
* `withSslCertificateUri(java.lang.String)` was added
* `sslCertificateUri()` was added
* `sslPreference()` was added

#### `models.CentralServerVmDetails` was modified

* `storageDetails()` was added

#### `models.SapDiskConfigurationsResult` was modified

* `volumeConfigurations()` was added

#### `models.DB2ProviderInstanceProperties` was modified

* `withSslPreference(models.SslPreference)` was added
* `sslPreference()` was added
* `sslCertificateUri()` was added
* `withSslCertificateUri(java.lang.String)` was added

#### `models.SingleServerConfiguration` was modified

* `withCustomResourceNames(models.SingleServerCustomResourceNames)` was added
* `dbDiskConfiguration()` was added
* `withDbDiskConfiguration(models.DiskConfiguration)` was added
* `customResourceNames()` was added

#### `models.PrometheusHaClusterProviderInstanceProperties` was modified

* `withSslPreference(models.SslPreference)` was added
* `sslPreference()` was added
* `withSslCertificateUri(java.lang.String)` was added
* `sslCertificateUri()` was added

#### `models.MsSqlServerProviderInstanceProperties` was modified

* `sslPreference()` was added
* `withSslPreference(models.SslPreference)` was added
* `sslCertificateUri()` was added
* `withSslCertificateUri(java.lang.String)` was added

#### `models.DatabaseVmDetails` was modified

* `storageDetails()` was added

#### `models.SapApplicationServerInstances` was modified

* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was added
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was added
* `startInstance(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrometheusOSProviderInstanceProperties` was modified

* `sapSid()` was added
* `sslCertificateUri()` was added
* `sslPreference()` was added
* `withSslPreference(models.SslPreference)` was added
* `withSapSid(java.lang.String)` was added
* `withSslCertificateUri(java.lang.String)` was added

#### `models.HanaDbProviderInstanceProperties` was modified

* `sslPreference()` was added
* `withSapSid(java.lang.String)` was added
* `withSslPreference(models.SslPreference)` was added
* `sslCertificateUri()` was added
* `withSslCertificateUri(java.lang.String)` was added
* `sapSid()` was added

#### `models.SapDatabaseInstance` was modified

* `loadBalancerDetails()` was added
* `startInstance()` was added
* `stopInstance(models.StopRequest,com.azure.core.util.Context)` was added
* `stopInstance()` was added
* `startInstance(com.azure.core.util.Context)` was added

#### `models.Monitor$Definition` was modified

* `withZoneRedundancyPreference(java.lang.String)` was added

#### `models.SapCentralInstances` was modified

* `startInstance(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `stopInstance(java.lang.String,java.lang.String,java.lang.String)` was added
* `stopInstance(java.lang.String,java.lang.String,java.lang.String,models.StopRequest,com.azure.core.util.Context)` was added
* `startInstance(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.DatabaseConfiguration` was modified

* `diskConfiguration()` was added
* `withDiskConfiguration(models.DiskConfiguration)` was added

## 1.0.0-beta.1 (2022-06-30)

- Azure Resource Manager Workloads client library for Java. This package contains Microsoft Azure SDK for Workloads Management SDK. Workloads client provides access to various workload operations. Package tag package-2021-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

