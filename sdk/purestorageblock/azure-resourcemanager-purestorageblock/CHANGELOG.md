# Release History

## 1.1.0-beta.1 (2026-07-29)

- Azure Resource Manager Pure Storage Block client library for Java. This package contains Microsoft Azure SDK for Pure Storage Block Management SDK.  Package api-version 2026-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AvsVmVolumeUpdateProperties` was modified

* `validate()` was removed

#### `models.ReservationUpdate` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed

#### `models.RangeLimits` was modified

* `validate()` was removed

#### `models.AvsDiskDetails` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.PerformancePolicyLimits` was modified

* `validate()` was removed

#### `models.HealthDetails` was modified

* `validate()` was removed

#### `models.VolumeProperties` was modified

* `validate()` was removed

#### `models.AvsStorageContainerProperties` was modified

* `validate()` was removed

#### `models.AzureVmwareService` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.Address` was modified

* `validate()` was removed

#### `models.AvsVmProperties` was modified

* `validate()` was removed

#### `models.MarketplaceDetails` was modified

* `validate()` was removed

#### `models.CompanyDetails` was modified

* `validate()` was removed

#### `models.StoragePoolUpdate` was modified

* `validate()` was removed

#### `models.AvsVmDetails` was modified

* `validate()` was removed

#### `models.BillingUsageProperty` was modified

* `validate()` was removed

#### `models.ReservationPropertiesBaseResourceProperties` was modified

* `validate()` was removed

#### `models.OfferDetails` was modified

* `validate()` was removed

#### `models.SoftDeletion` was modified

* `validate()` was removed

#### `models.StoragePoolFinalizeAvsConnectionPost` was modified

* `validate()` was removed

#### `models.StoragePoolUpdateProperties` was modified

* `validate()` was removed

#### `models.StoragePoolEnableAvsConnectionPost` was modified

* `validate()` was removed

#### `models.AvsStorageContainerVolumeUpdate` was modified

* `validate()` was removed

#### `models.ServiceInitializationHandle` was modified

* `validate()` was removed

#### `models.AvsVmVolumeUpdate` was modified

* `validate()` was removed

#### `models.IopsUsage` was modified

* `validate()` was removed

#### `models.ReservationUpdateProperties` was modified

* `validate()` was removed

#### `models.AvsVmUpdate` was modified

* `validate()` was removed

#### `models.Space` was modified

* `validate()` was removed

#### `models.UserDetails` was modified

* `validate()` was removed

#### `models.VolumeLimits` was modified

* `validate()` was removed

#### `models.AvsStorageContainerVolumeUpdateProperties` was modified

* `validate()` was removed

#### `models.Alert` was modified

* `validate()` was removed

#### `models.StoragePoolLimits` was modified

* `validate()` was removed

#### `models.AvsVmUpdateProperties` was modified

* `validate()` was removed

#### `models.ProtectionPolicyLimits` was modified

* `validate()` was removed

#### `models.ServiceInitializationInfo` was modified

* `validate()` was removed

#### `models.BandwidthUsage` was modified

* `validate()` was removed

#### `models.StoragePoolProperties` was modified

* `validate()` was removed

#### `models.VnetInjection` was modified

* `validate()` was removed

### Features Added

* `models.VolumeGroup$UpdateStages` was added

* `models.SaaSResourceDetailsResponse` was added

* `models.VolumeGroupSnapshotProperties` was added

* `models.ProtectionParameters` was added

* `models.LinkSaaSRequest` was added

* `models.RecoverableVolumeGroups` was added

* `models.PlatformConsoleActivationCode` was added

* `models.VolumeGroupSnapshots` was added

* `models.VolumeGroupSnapshot$Definition` was added

* `models.SshPlatformConsoleAuthConfig` was added

* `models.RecoverableVolumeGroupProperties` was added

* `models.VolumeGroups` was added

* `models.VolumeGroup$Definition` was added

* `models.IscsiConnectionParameters` was added

* `models.VolumeGroupProperties` was added

* `models.Volume$Definition` was added

* `models.PlatformConsoleSubnet` was added

* `models.VolumeGroup$DefinitionStages` was added

* `models.VolumeGroupSnapshotListRequest` was added

* `models.PlatformConsoleAuthResult` was added

* `models.VolumeGroupSnapshotPostListResult` was added

* `models.SshPlatformConsoleAuthResult` was added

* `models.VolumeGroupSnapshot$DefinitionStages` was added

* `models.VolumeSourceType` was added

* `models.VolumeOverwriteRequest` was added

* `models.VolumeGroupOverwriteRequest` was added

* `models.LatestLinkedSaaSResponse` was added

* `models.RecoverableVolumeGroup` was added

* `models.VolumeUpdate` was added

* `models.DestroyedStateProperties` was added

* `models.PlatformConsoleRole` was added

* `models.PerformanceParameters` was added

* `models.VolumeGroupSourceType` was added

* `models.VolumeSnapshotSource` was added

* `models.Volume$Update` was added

* `models.VolumeSnapshotInfo` was added

* `models.PlatformConsoleAccessSettings` was added

* `models.Volume$UpdateStages` was added

* `models.PlatformConsoleSettings` was added

* `models.PlatformConsoleAuthConfig` was added

* `models.ActivateSaaSRequest` was added

* `models.VolumeGroup$Update` was added

* `models.ConnectionParametersResponse` was added

* `models.SaaSOperationGroups` was added

* `models.Volume$DefinitionStages` was added

* `models.VolumeGroupUpdate` was added

* `models.Volume` was added

* `models.AzureVolumeProperties` was added

* `models.VolumeGroupSnapshot` was added

* `models.VolumeGroupUpdateProperties` was added

* `models.VolumeUpdateProperties` was added

* `models.PlatformConsoleAuthType` was added

* `models.IscsiEndpoint` was added

* `models.VolumeGroup` was added

* `models.VolumeGroupStatus` was added

* `models.Volumes` was added

#### `models.StoragePool` was modified

* `configurePlatformConsoleAuthWithResponse(models.PlatformConsoleAuthConfig,com.azure.core.util.Context)` was added
* `listPlatformConsoleActivationCode()` was added
* `listPlatformConsoleActivationCodeWithResponse(com.azure.core.util.Context)` was added
* `configurePlatformConsoleAuth(models.PlatformConsoleAuthConfig)` was added

#### `models.MarketplaceDetails` was modified

* `saaSResourceId()` was added
* `withSaaSResourceId(java.lang.String)` was added

#### `models.StoragePoolUpdateProperties` was modified

* `platformConsoleSettings()` was added
* `withPlatformConsoleSettings(models.PlatformConsoleSettings)` was added

#### `PureStorageBlockManager` was modified

* `volumeGroupSnapshots()` was added
* `recoverableVolumeGroups()` was added
* `volumeGroups()` was added
* `volumes()` was added
* `saaSOperationGroups()` was added

#### `models.Reservations` was modified

* `latestLinkedSaaSWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `linkSaaS(java.lang.String,java.lang.String,models.LinkSaaSRequest,com.azure.core.util.Context)` was added
* `latestLinkedSaaS(java.lang.String,java.lang.String)` was added
* `linkSaaS(java.lang.String,java.lang.String,models.LinkSaaSRequest)` was added

#### `models.Reservation` was modified

* `linkSaaS(models.LinkSaaSRequest)` was added
* `linkSaaS(models.LinkSaaSRequest,com.azure.core.util.Context)` was added
* `latestLinkedSaaSWithResponse(com.azure.core.util.Context)` was added
* `latestLinkedSaaS()` was added

#### `models.StoragePools` was modified

* `configurePlatformConsoleAuthWithResponse(java.lang.String,java.lang.String,models.PlatformConsoleAuthConfig,com.azure.core.util.Context)` was added
* `configurePlatformConsoleAuth(java.lang.String,java.lang.String,models.PlatformConsoleAuthConfig)` was added
* `listPlatformConsoleActivationCode(java.lang.String,java.lang.String)` was added
* `listPlatformConsoleActivationCodeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StoragePoolProperties` was modified

* `platformConsoleSettings()` was added
* `withPlatformConsoleSettings(models.PlatformConsoleSettings)` was added

## 1.0.0 (2025-06-30)

- Azure Resource Manager Pure Storage Block client library for Java. This package contains Microsoft Azure SDK for Pure Storage Block Management SDK.  Package api-version 2024-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-purestorageblock Java SDK.

## 1.0.0-beta.1 (2025-05-27)

- Azure Resource Manager Pure Storage Block client library for Java. This package contains Microsoft Azure SDK for Pure Storage Block Management SDK.  Package api-version 2024-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-purestorageblock Java SDK.
