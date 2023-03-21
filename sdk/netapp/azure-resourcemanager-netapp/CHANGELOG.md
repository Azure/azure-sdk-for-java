# Release History

## 1.0.0-beta.13 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.12 (2023-03-16)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2022-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Vaults` was removed

* `models.IdentityType` was removed

* `models.Identity` was removed

* `models.Vault` was removed

* `models.VaultList` was removed

#### `models.NetAppAccount$Definition` was modified

* `withIdentity(models.Identity)` was removed

#### `models.Volume` was modified

* `breakReplication(models.BreakReplicationRequest)` was removed
* `relocate(models.RelocateVolumeRequest)` was removed

#### `NetAppFilesManager` was modified

* `vaults()` was removed

#### `models.Volumes` was modified

* `breakReplication(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.BreakReplicationRequest)` was removed
* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RelocateVolumeRequest)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.NetAppAccount` was modified

* `models.Identity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.UserAssignedIdentity` was modified

* `java.lang.String clientId()` -> `java.util.UUID clientId()`
* `java.lang.String principalId()` -> `java.util.UUID principalId()`

#### `models.VolumeBackupProperties` was modified

* `withVaultId(java.lang.String)` was removed
* `vaultId()` was removed

### Features Added

* `models.BreakFileLocksRequest` was added

* `models.ManagedServiceIdentity` was added

* `models.VolumeRelocationProperties` was added

* `models.BackupRestoreFiles` was added

* `models.ManagedServiceIdentityType` was added

* `models.FileAccessLogs` was added

#### `models.NetAppAccount$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Volume` was modified

* `dataStoreResourceId()` was added
* `provisionedAvailabilityZone()` was added
* `isLargeVolume()` was added
* `breakFileLocks()` was added
* `breakFileLocks(models.BreakFileLocksRequest,com.azure.core.util.Context)` was added
* `fileAccessLogs()` was added

#### `models.ActiveDirectory` was modified

* `withPreferredServersForLdapClient(java.lang.String)` was added
* `preferredServersForLdapClient()` was added

#### `models.VolumePropertiesDataProtection` was modified

* `volumeRelocation()` was added
* `withVolumeRelocation(models.VolumeRelocationProperties)` was added

#### `models.Backup` was modified

* `systemData()` was added
* `restoreFiles(models.BackupRestoreFiles)` was added
* `restoreFiles(models.BackupRestoreFiles,com.azure.core.util.Context)` was added

#### `models.Backups` was modified

* `restoreFiles(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.BackupRestoreFiles,com.azure.core.util.Context)` was added
* `restoreFiles(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.BackupRestoreFiles)` was added

#### `models.Volume$Definition` was modified

* `withIsLargeVolume(java.lang.Boolean)` was added

#### `models.VolumeQuotaRule$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.Snapshot` was modified

* `systemData()` was added

#### `models.Volumes` was modified

* `breakFileLocks(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `breakFileLocks(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.BreakFileLocksRequest,com.azure.core.util.Context)` was added

#### `models.VolumeGroupVolumeProperties` was modified

* `fileAccessLogs()` was added
* `withIsLargeVolume(java.lang.Boolean)` was added
* `dataStoreResourceId()` was added
* `provisionedAvailabilityZone()` was added
* `isLargeVolume()` was added

#### `models.VolumeQuotaRulePatch` was modified

* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.NetAppAccount` was modified

* `systemData()` was added

#### `models.SubscriptionQuotaItem` was modified

* `systemData()` was added

## 1.0.0-beta.11 (2022-09-19)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Volume` was modified

* `relocate(com.azure.core.util.Context)` was removed

#### `models.Volumes` was modified

* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Vault` was modified

* `location()` was removed

#### `models.AccountEncryption` was modified

* `java.lang.String keySource()` -> `models.KeySource keySource()`
* `withKeySource(java.lang.String)` was removed

### Features Added

* `models.KeyVaultProperties` was added

* `models.EncryptionIdentity` was added

* `models.IdentityType` was added

* `models.RelocateVolumeRequest` was added

* `models.KeyVaultStatus` was added

* `models.SmbNonBrowsable` was added

* `models.KeySource` was added

* `models.Identity` was added

* `models.RegionInfoAvailabilityZoneMappingsItem` was added

* `models.RegionInfo` was added

* `models.RegionStorageToNetworkProximity` was added

* `models.UserAssignedIdentity` was added

* `models.SmbAccessBasedEnumeration` was added

#### `models.NetAppAccount$Definition` was modified

* `withIdentity(models.Identity)` was added

#### `models.Volume` was modified

* `smbNonBrowsable()` was added
* `deleteBaseSnapshot()` was added
* `relocate(models.RelocateVolumeRequest)` was added
* `smbAccessBasedEnumeration()` was added
* `relocate(models.RelocateVolumeRequest,com.azure.core.util.Context)` was added

#### `models.NetAppResources` was modified

* `queryRegionInfoWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `queryRegionInfo(java.lang.String)` was added

#### `models.Volume$Definition` was modified

* `withSmbNonBrowsable(models.SmbNonBrowsable)` was added
* `withDeleteBaseSnapshot(java.lang.Boolean)` was added
* `withSmbAccessBasedEnumeration(models.SmbAccessBasedEnumeration)` was added

#### `models.Volumes` was modified

* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RelocateVolumeRequest)` was added
* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RelocateVolumeRequest,com.azure.core.util.Context)` was added

#### `models.NetAppAccountPatch` was modified

* `disableShowmount()` was added

#### `models.VolumeGroupVolumeProperties` was modified

* `withSmbAccessBasedEnumeration(models.SmbAccessBasedEnumeration)` was added
* `smbNonBrowsable()` was added
* `deleteBaseSnapshot()` was added
* `withDeleteBaseSnapshot(java.lang.Boolean)` was added
* `withSmbNonBrowsable(models.SmbNonBrowsable)` was added
* `smbAccessBasedEnumeration()` was added

#### `models.Accounts` was modified

* `renewCredentials(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `renewCredentials(java.lang.String,java.lang.String)` was added

#### `models.NetAppAccount` was modified

* `renewCredentials()` was added
* `disableShowmount()` was added
* `identity()` was added
* `renewCredentials(com.azure.core.util.Context)` was added

#### `models.AccountEncryption` was modified

* `withKeySource(models.KeySource)` was added
* `withIdentity(models.EncryptionIdentity)` was added
* `withKeyVaultProperties(models.KeyVaultProperties)` was added
* `keyVaultProperties()` was added
* `identity()` was added

## 1.0.0-beta.10 (2022-07-21)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2022-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ReestablishReplicationRequest` was added

#### `models.Volume` was modified

* `reestablishReplication(models.ReestablishReplicationRequest)` was added
* `keyVaultPrivateEndpointResourceId()` was added
* `reestablishReplication(models.ReestablishReplicationRequest,com.azure.core.util.Context)` was added

#### `models.Volume$Update` was modified

* `withCoolAccess(java.lang.Boolean)` was added
* `withCoolnessPeriod(java.lang.Integer)` was added

#### `models.VolumePatch` was modified

* `withCoolnessPeriod(java.lang.Integer)` was added
* `withCoolAccess(java.lang.Boolean)` was added
* `coolAccess()` was added
* `coolnessPeriod()` was added

#### `models.CapacityPool$Update` was modified

* `withCoolAccess(java.lang.Boolean)` was added

#### `models.Volume$Definition` was modified

* `withKeyVaultPrivateEndpointResourceId(java.lang.String)` was added

#### `models.Volumes` was modified

* `reestablishReplication(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ReestablishReplicationRequest,com.azure.core.util.Context)` was added
* `reestablishReplication(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ReestablishReplicationRequest)` was added

#### `models.CapacityPoolPatch` was modified

* `coolAccess()` was added
* `withCoolAccess(java.lang.Boolean)` was added

#### `models.VolumeGroupVolumeProperties` was modified

* `withKeyVaultPrivateEndpointResourceId(java.lang.String)` was added
* `keyVaultPrivateEndpointResourceId()` was added

## 1.0.0-beta.9 (2022-06-20)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2022-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Volume` was modified

* `java.lang.String encryptionKeySource()` -> `models.EncryptionKeySource encryptionKeySource()`

#### `models.VolumeGroupDetails$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.VolumeGroupDetails` was modified

* `tags()` was removed

#### `models.Volume$Definition` was modified

* `withEncryptionKeySource(java.lang.String)` was removed

#### `models.VolumeGroupVolumeProperties` was modified

* `java.lang.String encryptionKeySource()` -> `models.EncryptionKeySource encryptionKeySource()`
* `withEncryptionKeySource(java.lang.String)` was removed

#### `models.VolumeGroup` was modified

* `tags()` was removed

#### `models.NetAppAccount` was modified

* `systemData()` was removed

#### `models.SubscriptionQuotaItem` was modified

* `systemData()` was removed

### Features Added

* `models.VolumeQuotaRulesList` was added

* `models.VolumeQuotaRules` was added

* `models.EncryptionKeySource` was added

* `models.VolumeQuotaRule$Definition` was added

* `models.VolumeQuotaRule$UpdateStages` was added

* `models.ProvisioningState` was added

* `models.VolumeQuotaRule$Update` was added

* `models.VolumeQuotaRule$DefinitionStages` was added

* `models.Replication` was added

* `models.VolumeQuotaRule` was added

* `models.VolumeQuotaRulePatch` was added

* `models.ListReplications` was added

* `models.Type` was added

#### `models.Volume` was modified

* `listReplications(com.azure.core.util.Context)` was added
* `resetCifsPassword()` was added
* `resetCifsPassword(com.azure.core.util.Context)` was added
* `finalizeRelocation(com.azure.core.util.Context)` was added
* `revertRelocation(com.azure.core.util.Context)` was added
* `revertRelocation()` was added
* `relocate(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `encrypted()` was added
* `finalizeRelocation()` was added
* `zones()` was added
* `listReplications()` was added
* `relocate()` was added

#### `NetAppFilesManager` was modified

* `volumeQuotaRules()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Backup` was modified

* `resourceGroupName()` was added

#### `models.CapacityPool` was modified

* `resourceGroupName()` was added

#### `models.Volume$Definition` was modified

* `withEncryptionKeySource(models.EncryptionKeySource)` was added
* `withZones(java.util.List)` was added

#### `models.Volumes` was modified

* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `revertRelocation(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `resetCifsPassword(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `relocate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `finalizeRelocation(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `finalizeRelocation(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReplications(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `revertRelocation(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReplications(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `resetCifsPassword(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.SubvolumeInfo` was modified

* `resourceGroupName()` was added

#### `models.VolumeGroupVolumeProperties` was modified

* `withEncryptionKeySource(models.EncryptionKeySource)` was added
* `encrypted()` was added

#### `models.BackupPolicy` was modified

* `resourceGroupName()` was added

#### `NetAppFilesManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.NetAppAccount` was modified

* `resourceGroupName()` was added

#### `models.SnapshotPolicy` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.8 (2022-02-15)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Volumes` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.SubvolumeInfo$Update` was added

* `models.SubvolumePatchRequest` was added

* `models.SubvolumeInfo$DefinitionStages` was added

* `models.SubvolumeInfo` was added

* `models.SubvolumeModel` was added

* `models.SnapshotRestoreFiles` was added

* `models.LdapSearchScopeOpt` was added

* `models.EnableSubvolumes` was added

* `models.SubvolumeInfo$Definition` was added

* `models.SubvolumesList` was added

* `models.SubvolumeInfo$UpdateStages` was added

* `models.Subvolumes` was added

#### `models.Volume` was modified

* `maximumNumberOfFiles()` was added
* `systemData()` was added
* `enableSubvolumes()` was added

#### `models.Volume$Update` was modified

* `withUnixPermissions(java.lang.String)` was added

#### `models.VolumePatch` was modified

* `unixPermissions()` was added
* `withUnixPermissions(java.lang.String)` was added

#### `models.ActiveDirectory` was modified

* `withLdapSearchScope(models.LdapSearchScopeOpt)` was added
* `ldapSearchScope()` was added

#### `NetAppFilesManager` was modified

* `subvolumes()` was added

#### `models.CapacityPool` was modified

* `systemData()` was added

#### `models.Volume$Definition` was modified

* `withEnableSubvolumes(models.EnableSubvolumes)` was added

#### `models.Snapshot` was modified

* `restoreFiles(models.SnapshotRestoreFiles,com.azure.core.util.Context)` was added
* `restoreFiles(models.SnapshotRestoreFiles)` was added

#### `models.Volumes` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.VolumeGroupVolumeProperties` was modified

* `maximumNumberOfFiles()` was added
* `enableSubvolumes()` was added
* `withEnableSubvolumes(models.EnableSubvolumes)` was added

#### `models.BackupPolicy` was modified

* `systemData()` was added

#### `models.SnapshotPolicy` was modified

* `systemData()` was added

#### `models.Snapshots` was modified

* `restoreFiles(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SnapshotRestoreFiles)` was added
* `restoreFiles(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SnapshotRestoreFiles,com.azure.core.util.Context)` was added

## 1.0.0-beta.7 (2021-12-06)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BackupPolicyPatch` was modified

* `namePropertiesName()` was removed

#### `models.BackupPolicy` was modified

* `namePropertiesName()` was removed

#### `models.SubscriptionQuotaItem` was modified

* `namePropertiesName()` was removed

### Features Added

* `models.VolumeGroupDetails$Definition` was added

* `models.PlacementKeyValuePairs` was added

* `models.VolumeGroupMetadata` was added

* `models.VolumeGroupDetails` was added

* `models.VolumeGroupList` was added

* `models.VolumeGroups` was added

* `models.VolumeGroupVolumeProperties` was added

* `models.VolumeGroup` was added

* `models.VolumeGroupDetails$DefinitionStages` was added

* `models.ApplicationType` was added

#### `models.Volume` was modified

* `proximityPlacementGroup()` was added
* `capacityPoolResourceId()` was added
* `placementRules()` was added
* `volumeSpecName()` was added
* `volumeGroupName()` was added
* `t2Network()` was added

#### `models.ActiveDirectory` was modified

* `encryptDCConnections()` was added
* `withEncryptDCConnections(java.lang.Boolean)` was added

#### `NetAppFilesManager` was modified

* `volumeGroups()` was added

#### `models.Volume$Definition` was modified

* `withVolumeSpecName(java.lang.String)` was added
* `withCapacityPoolResourceId(java.lang.String)` was added
* `withPlacementRules(java.util.List)` was added
* `withProximityPlacementGroup(java.lang.String)` was added

## 1.0.0-beta.6 (2021-09-22)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.VolumeStorageToNetworkProximity` was added

* `models.LogSpecification` was added

* `models.NetAppResourceQuotaLimits` was added

* `models.NetworkFeatures` was added

* `models.SubscriptionQuotaItemList` was added

* `models.SubscriptionQuotaItem` was added

#### `models.ServiceSpecification` was modified

* `withLogSpecifications(java.util.List)` was added
* `logSpecifications()` was added

#### `models.MetricSpecification` was modified

* `withEnableRegionalMdmAccount(java.lang.Boolean)` was added
* `isInternal()` was added
* `withIsInternal(java.lang.Boolean)` was added
* `enableRegionalMdmAccount()` was added

#### `models.Volume` was modified

* `networkSiblingSetId()` was added
* `storageToNetworkProximity()` was added
* `networkFeatures()` was added

#### `NetAppFilesManager` was modified

* `netAppResourceQuotaLimits()` was added

#### `models.Volume$Definition` was modified

* `withNetworkFeatures(models.NetworkFeatures)` was added

## 1.0.0-beta.5 (2021-08-25)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.MountTargetProperties` was removed

#### `models.BackupPolicy$Definition` was modified

* `withYearlyBackupsToKeep(java.lang.Integer)` was removed
* `withVolumeBackups(java.util.List)` was removed
* `withVolumesAssigned(java.lang.Integer)` was removed

#### `models.BackupPolicyPatch` was modified

* `yearlyBackupsToKeep()` was removed
* `withVolumesAssigned(java.lang.Integer)` was removed
* `withVolumeBackups(java.util.List)` was removed
* `withYearlyBackupsToKeep(java.lang.Integer)` was removed

#### `models.BackupPolicy$Update` was modified

* `withYearlyBackupsToKeep(java.lang.Integer)` was removed
* `withVolumesAssigned(java.lang.Integer)` was removed
* `withVolumeBackups(java.util.List)` was removed

#### `models.BackupPolicy` was modified

* `yearlyBackupsToKeep()` was removed

### Features Added

* `models.AvsDataStore` was added

* `models.EncryptionType` was added

* `models.MetricAggregationType` was added

#### `models.MetricSpecification` was modified

* `supportedTimeGrainTypes()` was added
* `withSupportedAggregationTypes(java.util.List)` was added
* `internalMetricName()` was added
* `sourceMdmAccount()` was added
* `sourceMdmNamespace()` was added
* `withSourceMdmNamespace(java.lang.String)` was added
* `supportedAggregationTypes()` was added
* `withSupportedTimeGrainTypes(java.util.List)` was added
* `withInternalMetricName(java.lang.String)` was added
* `withSourceMdmAccount(java.lang.String)` was added

#### `models.Volume` was modified

* `avsDataStore()` was added
* `isDefaultQuotaEnabled()` was added
* `defaultUserQuotaInKiBs()` was added
* `etag()` was added
* `cloneProgress()` was added
* `defaultGroupQuotaInKiBs()` was added

#### `models.Volume$Update` was modified

* `withDefaultGroupQuotaInKiBs(java.lang.Long)` was added
* `withIsDefaultQuotaEnabled(java.lang.Boolean)` was added
* `withDefaultUserQuotaInKiBs(java.lang.Long)` was added

#### `models.VolumePatch` was modified

* `isDefaultQuotaEnabled()` was added
* `defaultUserQuotaInKiBs()` was added
* `withDefaultUserQuotaInKiBs(java.lang.Long)` was added
* `defaultGroupQuotaInKiBs()` was added
* `withIsDefaultQuotaEnabled(java.lang.Boolean)` was added
* `withDefaultGroupQuotaInKiBs(java.lang.Long)` was added

#### `models.CapacityPool$Definition` was modified

* `withEncryptionType(models.EncryptionType)` was added

#### `models.CapacityPool` was modified

* `encryptionType()` was added
* `etag()` was added

#### `models.Volume$Definition` was modified

* `withAvsDataStore(models.AvsDataStore)` was added
* `withDefaultUserQuotaInKiBs(java.lang.Long)` was added
* `withDefaultGroupQuotaInKiBs(java.lang.Long)` was added
* `withIsDefaultQuotaEnabled(java.lang.Boolean)` was added

#### `models.BackupPolicyPatch` was modified

* `namePropertiesName()` was added
* `backupPolicyId()` was added

#### `models.Accounts` was modified

* `list(com.azure.core.util.Context)` was added
* `list()` was added

#### `models.BackupPolicy` was modified

* `etag()` was added
* `namePropertiesName()` was added
* `backupPolicyId()` was added

#### `models.NetAppAccount` was modified

* `etag()` was added

#### `models.SnapshotPolicy` was modified

* `etag()` was added

## 1.0.0-beta.4 (2021-06-16)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.NetAppResources` was modified

* `checkFilePathAvailabilityWithResponse(java.lang.String,models.ResourceNameAvailabilityRequest,com.azure.core.util.Context)` was removed
* `checkFilePathAvailability(java.lang.String,models.ResourceNameAvailabilityRequest)` was removed

### New Feature

* `models.ChownMode` was added

* `models.RestoreStatus` was added

* `models.FilePathAvailabilityRequest` was added

#### `models.BackupStatus` was modified

* `totalTransferBytes()` was added
* `lastTransferSize()` was added
* `lastTransferType()` was added

#### `models.Volume` was modified

* `coolnessPeriod()` was added
* `coolAccess()` was added
* `unixPermissions()` was added

#### `models.CapacityPool$Definition` was modified

* `withCoolAccess(java.lang.Boolean)` was added

#### `models.ActiveDirectory` was modified

* `withAdministrators(java.util.List)` was added
* `administrators()` was added

#### `models.CapacityPool` was modified

* `coolAccess()` was added

#### `models.NetAppResources` was modified

* `checkFilePathAvailabilityWithResponse(java.lang.String,models.FilePathAvailabilityRequest,com.azure.core.util.Context)` was added
* `checkFilePathAvailability(java.lang.String,models.FilePathAvailabilityRequest)` was added

#### `models.Backups` was modified

* `getVolumeRestoreStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getVolumeRestoreStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.Volume$Definition` was modified

* `withUnixPermissions(java.lang.String)` was added
* `withCoolnessPeriod(java.lang.Integer)` was added
* `withCoolAccess(java.lang.Boolean)` was added

#### `models.ExportPolicyRule` was modified

* `withChownMode(models.ChownMode)` was added
* `chownMode()` was added

#### `NetAppFilesManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.3 (2021-05-13)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.VolumeBackupStatus` was removed

#### `NetAppFilesManager` was modified

* `volumeBackupStatus()` was removed

#### `models.Backup` was modified

* `java.lang.String backupType()` -> `models.BackupType backupType()`

#### `models.BackupsList` was modified

* `innerModel()` was removed
* `java.util.List value()` -> `java.util.List value()`

#### `models.BackupPatch` was modified

* `java.lang.String backupType()` -> `models.BackupType backupType()`

#### `models.AccountBackups` was modified

* `models.BackupsList list(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable list(java.lang.String,java.lang.String)`
* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### New Feature

* `models.BackupType` was added

#### `models.Backup` was modified

* `useExistingSnapshot()` was added

#### `models.Backups` was modified

* `getStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.VolumePatchPropertiesDataProtection` was modified

* `snapshot()` was added
* `withSnapshot(models.VolumeSnapshotProperties)` was added

#### `models.BackupsList` was modified

* `validate()` was added
* `withValue(java.util.List)` was added

#### `models.BackupPatch` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added
* `useExistingSnapshot()` was added

#### `models.Backup$Update` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added

#### `models.AccountBackups` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Backup$Definition` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added

## 1.0.0-beta.2 (2021-03-15)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2020-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.KeySource` was removed

* `models.CreatedByType` was removed

* `models.SystemData` was removed

#### `models.SnapshotPolicyPatch` was modified

* `namePropertiesName()` was removed

#### `models.NetAppAccount` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.AccountEncryption` was modified

* `models.KeySource keySource()` -> `java.lang.String keySource()`
* `withKeySource(models.KeySource)` was removed

### New Feature

* `models.BackupStatus` was added

* `models.VolumeBackupStatus` was added

#### `models.Volume` was modified

* `ldapEnabled()` was added

#### `models.ActiveDirectory` was modified

* `withAllowLocalNfsUsersWithLdap(java.lang.Boolean)` was added
* `allowLocalNfsUsersWithLdap()` was added

#### `NetAppFilesManager` was modified

* `volumeBackupStatus()` was added

#### `models.Backup` was modified

* `volumeName()` was added

#### `models.Volume$Definition` was modified

* `withLdapEnabled(java.lang.Boolean)` was added

#### `models.BackupPatch` was modified

* `volumeName()` was added

#### `models.AccountEncryption` was modified

* `withKeySource(java.lang.String)` was added

## 1.0.0-beta.1 (2021-02-22)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2020-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
