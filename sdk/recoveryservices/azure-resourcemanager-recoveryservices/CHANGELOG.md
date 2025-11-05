# Release History

## 1.6.0 (2025-09-29)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK.  Package api-version 2025-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateLinkResources` was removed

#### `models.ReplicationUsageList` was removed

#### `models.ClientDiscoveryResponse` was removed

#### `models.VaultList` was removed

#### `models.VaultUsageList` was removed

#### `models.ClientDiscoveryForLogSpecification` was modified

* `validate()` was removed
* `withBlobDuration(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.CapabilitiesProperties` was modified

* `validate()` was removed

#### `models.CmkKeyVaultProperties` was modified

* `validate()` was removed

#### `models.PatchTrackedResource` was modified

* `validate()` was removed

#### `models.CrossSubscriptionRestoreSettings` was modified

* `validate()` was removed

#### `models.VaultPropertiesRedundancySettings` was modified

* `validate()` was removed

#### `models.ResourceCapabilities` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityParameters` was modified

* `validate()` was removed

#### `models.AzureMonitorAlertSettings` was modified

* `validate()` was removed

#### `models.UpgradeDetails` was modified

* `validate()` was removed

#### `models.JobsSummary` was modified

* `validate()` was removed
* `withFailedJobs(java.lang.Integer)` was removed
* `withSuspendedJobs(java.lang.Integer)` was removed
* `withInProgressJobs(java.lang.Integer)` was removed

#### `models.PrivateEndpointConnectionVaultProperties` was modified

* `validate()` was removed

#### `models.ClassicAlertSettings` was modified

* `validate()` was removed

#### `models.DnsZone` was modified

* `validate()` was removed

#### `models.ResourceCertificateDetails` was modified

* `models.ResourceCertificateDetails withIssuer(java.lang.String)` -> `models.ResourceCertificateDetails withIssuer(java.lang.String)`
* `validate()` was removed
* `models.ResourceCertificateDetails withFriendlyName(java.lang.String)` -> `models.ResourceCertificateDetails withFriendlyName(java.lang.String)`
* `models.ResourceCertificateDetails withSubject(java.lang.String)` -> `models.ResourceCertificateDetails withSubject(java.lang.String)`
* `models.ResourceCertificateDetails withThumbprint(java.lang.String)` -> `models.ResourceCertificateDetails withThumbprint(java.lang.String)`
* `models.ResourceCertificateDetails withValidFrom(java.time.OffsetDateTime)` -> `models.ResourceCertificateDetails withValidFrom(java.time.OffsetDateTime)`
* `models.ResourceCertificateDetails withResourceId(java.lang.Long)` -> `models.ResourceCertificateDetails withResourceId(java.lang.Long)`
* `models.ResourceCertificateDetails withValidTo(java.time.OffsetDateTime)` -> `models.ResourceCertificateDetails withValidTo(java.time.OffsetDateTime)`
* `models.ResourceCertificateDetails withCertificate(byte[])` -> `models.ResourceCertificateDetails withCertificate(byte[])`

#### `models.SoftDeleteSettings` was modified

* `validate()` was removed

#### `models.VaultPropertiesMoveDetails` was modified

* `validate()` was removed

#### `models.ResourceCapabilitiesBase` was modified

* `validate()` was removed

#### `models.Vault$Definition` was modified

* `withXMsAuthorizationAuxiliary(java.lang.String)` was removed

#### `models.SecuritySettings` was modified

* `validate()` was removed

#### `models.IdentityData` was modified

* `validate()` was removed

#### `models.ResourceCertificateAndAadDetails` was modified

* `withAadTenantId(java.lang.String)` was removed
* `withServicePrincipalObjectId(java.lang.String)` was removed
* `validate()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withValidFrom(java.time.OffsetDateTime)` was removed
* `withAzureManagementEndpointAudience(java.lang.String)` was removed
* `withSubject(java.lang.String)` was removed
* `withIssuer(java.lang.String)` was removed
* `withAadAuthority(java.lang.String)` was removed
* `withCertificate(byte[])` was removed
* `withThumbprint(java.lang.String)` was removed
* `withAadAudience(java.lang.String)` was removed
* `withServiceResourceId(java.lang.String)` was removed
* `withValidTo(java.time.OffsetDateTime)` was removed
* `withServicePrincipalClientId(java.lang.String)` was removed
* `withResourceId(java.lang.Long)` was removed

#### `models.Vault$Update` was modified

* `withXMsAuthorizationAuxiliary(java.lang.String)` was removed

#### `models.CertificateRequest` was modified

* `validate()` was removed

#### `models.RestoreSettings` was modified

* `validate()` was removed

#### `models.ImmutabilitySettings` was modified

* `validate()` was removed

#### `models.ClientDiscoveryDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.MonitoringSettings` was modified

* `validate()` was removed

#### `models.ResourceCertificateAndAcsDetails` was modified

* `withResourceId(java.lang.Long)` was removed
* `withThumbprint(java.lang.String)` was removed
* `withSubject(java.lang.String)` was removed
* `withGlobalAcsNamespace(java.lang.String)` was removed
* `withValidFrom(java.time.OffsetDateTime)` was removed
* `withIssuer(java.lang.String)` was removed
* `withGlobalAcsHostname(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withValidTo(java.time.OffsetDateTime)` was removed
* `globalAcsHostname()` was removed
* `withGlobalAcsRPRealm(java.lang.String)` was removed
* `validate()` was removed
* `withCertificate(byte[])` was removed

#### `models.VaultPropertiesEncryption` was modified

* `validate()` was removed

#### `models.ClientDiscoveryForProperties` was modified

* `withServiceSpecification(models.ClientDiscoveryForServiceSpecification)` was removed
* `validate()` was removed

#### `models.PrivateEndpointConnection` was modified

* `withGroupIds(java.util.List)` was removed
* `validate()` was removed

#### `models.RawCertificateData` was modified

* `validate()` was removed

#### `models.NameInfo` was modified

* `withLocalizedValue(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed
* `validate()` was removed

#### `models.SourceScanConfiguration` was modified

* `validate()` was removed

#### `models.PatchVault` was modified

* `validate()` was removed

#### `models.MonitoringSummary` was modified

* `withUnHealthyVmCount(java.lang.Integer)` was removed
* `validate()` was removed
* `withEventsCount(java.lang.Integer)` was removed
* `withUnsupportedProviderCount(java.lang.Integer)` was removed
* `withUnHealthyProviderCount(java.lang.Integer)` was removed
* `withDeprecatedProviderCount(java.lang.Integer)` was removed
* `withSupportedProviderCount(java.lang.Integer)` was removed

#### `models.DnsZoneResponse` was modified

* `withSubResource(models.VaultSubResourceType)` was removed
* `validate()` was removed
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.CmkKekIdentity` was modified

* `validate()` was removed

#### `models.CapabilitiesResponseProperties` was modified

* `withDnsZones(java.util.List)` was removed
* `validate()` was removed

#### `models.AssociatedIdentity` was modified

* `validate()` was removed

#### `models.UserIdentity` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.ClientDiscoveryForServiceSpecification` was modified

* `validate()` was removed
* `withLogSpecifications(java.util.List)` was removed

#### `models.VaultProperties` was modified

* `validate()` was removed

### Features Added

* `models.DeletedVaultUndeleteInput` was added

* `models.DeletedVaultProperties` was added

* `models.DeletedVaultUndeleteInputProperties` was added

* `models.DeletedVault` was added

* `models.DeletedVaults` was added

#### `models.VaultExtendedInfoResource` was modified

* `systemData()` was added

#### `models.PatchTrackedResource` was modified

* `systemData()` was added

#### `RecoveryServicesManager` was modified

* `deletedVaults()` was added

#### `models.ResourceCertificateAndAcsDetails` was modified

* `globalAcsHostName()` was added

#### `models.PatchVault` was modified

* `systemData()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

## 1.5.0 (2025-06-19)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2025-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.State` was added

* `models.SourceScanConfiguration` was added

* `models.AssociatedIdentity` was added

* `models.IdentityType` was added

#### `models.SecuritySettings` was modified

* `sourceScanConfiguration()` was added
* `withSourceScanConfiguration(models.SourceScanConfiguration)` was added

## 1.4.0 (2024-12-23)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.PatchTrackedResource` was modified

* `type()` was added
* `name()` was added
* `id()` was added

#### `models.PatchVault` was modified

* `type()` was added
* `name()` was added
* `id()` was added

## 1.3.0 (2024-05-20)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Vaults` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.BcdrSecurityLevel` was added

* `models.EnhancedSecurityState` was added

#### `models.VaultPropertiesRedundancySettings` was modified

* `withStandardTierStorageRedundancy(models.StandardTierStorageRedundancy)` was added
* `withCrossRegionRestore(models.CrossRegionRestore)` was added

#### `models.AzureMonitorAlertSettings` was modified

* `alertsForAllReplicationIssues()` was added
* `withAlertsForAllReplicationIssues(models.AlertsState)` was added
* `alertsForAllFailoverIssues()` was added
* `withAlertsForAllFailoverIssues(models.AlertsState)` was added

#### `models.ClassicAlertSettings` was modified

* `emailNotificationsForSiteRecovery()` was added
* `withEmailNotificationsForSiteRecovery(models.AlertsState)` was added

#### `models.ResourceCertificateDetails` was modified

* `authType()` was added

#### `models.SoftDeleteSettings` was modified

* `withEnhancedSecurityState(models.EnhancedSecurityState)` was added
* `enhancedSecurityState()` was added

#### `models.Vault$Definition` was modified

* `withXMsAuthorizationAuxiliary(java.lang.String)` was added

#### `models.Vaults` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ResourceCertificateAndAadDetails` was modified

* `authType()` was added

#### `models.Vault$Update` was modified

* `withXMsAuthorizationAuxiliary(java.lang.String)` was added

#### `models.ResourceCertificateAndAcsDetails` was modified

* `authType()` was added

#### `models.VaultProperties` was modified

* `bcdrSecurityLevel()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `resourceGuardOperationRequests()` was added

## 1.2.0 (2023-08-22)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MultiUserAuthorization` was added

* `models.SoftDeleteSettings` was added

* `models.SecureScoreLevel` was added

* `models.SoftDeleteState` was added

#### `models.SecuritySettings` was modified

* `multiUserAuthorization()` was added
* `softDeleteSettings()` was added
* `withSoftDeleteSettings(models.SoftDeleteSettings)` was added

#### `models.VaultProperties` was modified

* `secureScore()` was added

## 1.1.0 (2023-05-17)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2023-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CrossSubscriptionRestoreSettings` was added

* `models.RestoreSettings` was added

* `models.CrossSubscriptionRestoreState` was added

#### `models.VaultProperties` was modified

* `withRestoreSettings(models.RestoreSettings)` was added
* `restoreSettings()` was added

## 1.0.0 (2023-02-27)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.5 (2023-02-14)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.4 (2022-12-20)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Vaults` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.CapabilitiesProperties` was added

* `models.ImmutabilityState` was added

* `models.ResourceCapabilities` was added

* `models.DnsZone` was added

* `models.ResourceCapabilitiesBase` was added

* `models.SecuritySettings` was added

* `models.CapabilitiesResponse` was added

* `models.VaultSubResourceType` was added

* `models.PublicNetworkAccess` was added

* `models.ImmutabilitySettings` was added

* `models.DnsZoneResponse` was added

* `models.CapabilitiesResponseProperties` was added

#### `models.RecoveryServices` was modified

* `capabilitiesWithResponse(java.lang.String,models.ResourceCapabilities,com.azure.core.util.Context)` was added
* `capabilities(java.lang.String,models.ResourceCapabilities)` was added

#### `models.Vault$Definition` was modified

* `withEtag(java.lang.String)` was added

#### `models.Vaults` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added
* `withGroupIds(java.util.List)` was added

#### `models.Vault` was modified

* `etag()` was added

#### `models.VaultProperties` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `publicNetworkAccess()` was added
* `securitySettings()` was added
* `withSecuritySettings(models.SecuritySettings)` was added

## 1.0.0-beta.3 (2022-07-25)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2022-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MonitoringSettings` was added

* `models.VaultPropertiesRedundancySettings` was added

* `models.BackupStorageVersion` was added

* `models.StandardTierStorageRedundancy` was added

* `models.AzureMonitorAlertSettings` was added

* `models.ClassicAlertSettings` was added

* `models.CrossRegionRestore` was added

* `models.AlertsState` was added

#### `models.ResourceCertificateAndAadDetails` was modified

* `aadAudience()` was added
* `withAadAudience(java.lang.String)` was added

#### `models.VaultProperties` was modified

* `withMonitoringSettings(models.MonitoringSettings)` was added
* `backupStorageVersion()` was added
* `withRedundancySettings(models.VaultPropertiesRedundancySettings)` was added
* `redundancySettings()` was added
* `monitoringSettings()` was added

## 1.0.0-beta.2 (2022-07-19)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2021-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationResource` was added

* `models.ResourceProviders` was added

* `models.InfrastructureEncryptionState` was added

* `models.CmkKeyVaultProperties` was added

* `models.VaultPropertiesEncryption` was added

* `models.CmkKekIdentity` was added

* `models.UserIdentity` was added

* `models.VaultPropertiesMoveDetails` was added

* `models.ResourceMoveState` was added

#### `models.Sku` was modified

* `withSize(java.lang.String)` was added
* `family()` was added
* `withCapacity(java.lang.String)` was added
* `capacity()` was added
* `size()` was added
* `tier()` was added
* `withFamily(java.lang.String)` was added
* `withTier(java.lang.String)` was added

#### `RecoveryServicesManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `RecoveryServicesManager` was modified

* `resourceProviders()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PrivateEndpointConnectionVaultProperties` was modified

* `name()` was added
* `type()` was added
* `location()` was added

#### `models.Vault` was modified

* `systemData()` was added
* `resourceGroupName()` was added

#### `models.IdentityData` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.ResourceCertificateAndAadDetails` was modified

* `serviceResourceId()` was added
* `withServiceResourceId(java.lang.String)` was added

#### `models.VaultProperties` was modified

* `moveDetails()` was added
* `encryption()` was added
* `withEncryption(models.VaultPropertiesEncryption)` was added
* `moveState()` was added
* `withMoveDetails(models.VaultPropertiesMoveDetails)` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2016-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
