# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2023-11-16)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.MachineExtensionUpdateProperties` was removed

* `models.MachineUpdateProperties` was removed

#### `models.MachineUpdate` was modified

* `properties()` was removed
* `withProperties(models.MachineUpdateProperties)` was removed

#### `models.MachineExtensionProperties` was modified

* `java.lang.Object settings()` -> `java.util.Map settings()`
* `withProtectedSettings(java.lang.Object)` was removed
* `withSettings(java.lang.Object)` was removed
* `java.lang.Object protectedSettings()` -> `java.util.Map protectedSettings()`

#### `models.Machine` was modified

* `properties()` was removed

#### `models.MachineProperties` was modified

* `java.lang.String parentClusterResourceId()` -> `java.lang.String parentClusterResourceId()`
* `withLocationData(models.LocationData)` was removed
* `java.util.List extensions()` -> `java.util.List extensions()`
* `java.time.OffsetDateTime lastStatusChange()` -> `java.time.OffsetDateTime lastStatusChange()`
* `java.lang.String displayName()` -> `java.lang.String displayName()`
* `models.LocationData locationData()` -> `models.LocationData locationData()`
* `java.lang.String vmId()` -> `java.lang.String vmId()`
* `java.lang.String provisioningState()` -> `java.lang.String provisioningState()`
* `java.lang.String clientPublicKey()` -> `java.lang.String clientPublicKey()`
* `withClientPublicKey(java.lang.String)` was removed
* `validate()` was removed
* `java.lang.String machineFqdn()` -> `java.lang.String machineFqdn()`
* `java.lang.String dnsFqdn()` -> `java.lang.String dnsFqdn()`
* `withExtensions(java.util.List)` was removed
* `models.OSProfile osProfile()` -> `models.OSProfile osProfile()`
* `java.lang.String osVersion()` -> `java.lang.String osVersion()`
* `java.lang.String vmUuid()` -> `java.lang.String vmUuid()`
* `withPrivateLinkScopeResourceId(java.lang.String)` was removed
* `java.util.Map detectedProperties()` -> `java.util.Map detectedProperties()`
* `java.util.List errorDetails()` -> `java.util.List errorDetails()`
* `models.StatusTypes status()` -> `models.StatusTypes status()`
* `withParentClusterResourceId(java.lang.String)` was removed
* `java.lang.String privateLinkScopeResourceId()` -> `java.lang.String privateLinkScopeResourceId()`
* `java.lang.String osSku()` -> `java.lang.String osSku()`
* `java.lang.String domainName()` -> `java.lang.String domainName()`
* `java.lang.String adFqdn()` -> `java.lang.String adFqdn()`
* `java.lang.String agentVersion()` -> `java.lang.String agentVersion()`
* `withVmId(java.lang.String)` was removed
* `java.lang.String osName()` -> `java.lang.String osName()`

#### `models.MachineExtensionUpdate` was modified

* `withProperties(models.MachineExtensionUpdateProperties)` was removed
* `properties()` was removed

#### `models.MachineExtension$Update` was modified

* `withProperties(models.MachineExtensionUpdateProperties)` was removed

#### `models.Machines` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.LicenseCoreType` was added

* `models.IpAddress` was added

* `models.AgentConfigurationMode` was added

* `models.ExtensionValue` was added

* `models.LicenseProfileArmEsuPropertiesWithoutAssignedLicense` was added

* `models.CloudMetadata` was added

* `models.LicenseType` was added

* `models.LicenseProfilesListResult` was added

* `models.License$UpdateStages` was added

* `models.PrivateEndpointConnectionDataModel` was added

* `models.ResourceProviders` was added

* `models.AgentUpgrade` was added

* `models.VMGuestPatchRebootSetting` was added

* `models.AgentVersion` was added

* `models.LicenseTarget` was added

* `models.AssessmentModeTypes` was added

* `models.AgentConfiguration` was added

* `models.OSProfileLinuxConfiguration` was added

* `models.MachineExtensionUpgrade` was added

* `models.LicenseProfile` was added

* `models.ExtensionMetadatas` was added

* `models.ExtensionValueListResult` was added

* `models.VMGuestPatchClassificationWindows` was added

* `models.LicenseProfileMachineInstanceViewEsuProperties` was added

* `models.MachineInstallPatchesParameters` was added

* `models.NetworkProfile` was added

* `models.LicenseProfile$DefinitionStages` was added

* `models.ServiceStatus` was added

* `models.LicenseProfiles` was added

* `models.EsuKeyState` was added

* `models.LicenseUpdate` was added

* `models.LicenseState` was added

* `models.ServiceStatuses` was added

* `models.ArcKindEnum` was added

* `models.LicenseProfile$Definition` was added

* `models.VMGuestPatchClassificationLinux` was added

* `models.LicenseAssignmentState` was added

* `models.Licenses` was added

* `models.LicenseProfileUpdate` was added

* `models.OSProfileWindowsConfiguration` was added

* `models.LicenseProfile$UpdateStages` was added

* `models.AvailablePatchCountByClassification` was added

* `models.LicenseDetails` was added

* `models.PatchServiceUsed` was added

* `models.LicenseProfile$Update` was added

* `models.PatchOperationStatus` was added

* `models.Subnet` was added

* `models.License$Definition` was added

* `models.EsuKey` was added

* `models.EsuServerType` was added

* `models.NetworkProfiles` was added

* `models.OsType` was added

* `models.AgentVersionsList` was added

* `models.LicenseProfileStorageModelEsuProperties` was added

* `models.HybridIdentityMetadataList` was added

* `models.LicenseEdition` was added

* `models.License$Update` was added

* `models.HybridIdentityMetadatas` was added

* `models.EsuEligibility` was added

* `models.LastAttemptStatusEnum` was added

* `models.VMGuestPatchRebootStatus` was added

* `models.LicensesListResult` was added

* `models.LinuxParameters` was added

* `models.LicenseProfileMachineInstanceView` was added

* `models.License$DefinitionStages` was added

* `models.License` was added

* `models.ExtensionTargetProperties` was added

* `models.ProvisioningState` was added

* `models.AgentVersions` was added

* `models.PatchOperationStartedBy` was added

* `models.WindowsParameters` was added

* `models.MachineInstallPatchesResult` was added

* `models.PatchModeTypes` was added

* `models.HybridIdentityMetadata` was added

* `models.ConfigurationExtension` was added

* `models.NetworkInterface` was added

* `models.MachineAssessPatchesResult` was added

#### `models.MachineUpdate` was modified

* `parentClusterResourceId()` was added
* `cloudMetadata()` was added
* `withParentClusterResourceId(java.lang.String)` was added
* `osProfile()` was added
* `withLocationData(models.LocationData)` was added
* `withOsProfile(models.OSProfile)` was added
* `withCloudMetadata(models.CloudMetadata)` was added
* `withKind(models.ArcKindEnum)` was added
* `withPrivateLinkScopeResourceId(java.lang.String)` was added
* `withAgentUpgrade(models.AgentUpgrade)` was added
* `agentUpgrade()` was added
* `privateLinkScopeResourceId()` was added
* `kind()` was added
* `locationData()` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `groupIds()` was added

#### `models.MachineExtensionProperties` was modified

* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added
* `withSettings(java.util.Map)` was added
* `withProtectedSettings(java.util.Map)` was added
* `enableAutomaticUpgrade()` was added

#### `models.OSProfile` was modified

* `windowsConfiguration()` was added
* `withWindowsConfiguration(models.OSProfileWindowsConfiguration)` was added
* `linuxConfiguration()` was added
* `withLinuxConfiguration(models.OSProfileLinuxConfiguration)` was added

#### `models.Machine` was modified

* `mssqlDiscovered()` was added
* `detectedProperties()` was added
* `adFqdn()` was added
* `networkProfile()` was added
* `osVersion()` was added
* `resources()` was added
* `kind()` was added
* `lastStatusChange()` was added
* `clientPublicKey()` was added
* `vmUuid()` was added
* `agentConfiguration()` was added
* `osSku()` was added
* `displayName()` was added
* `cloudMetadata()` was added
* `domainName()` was added
* `osName()` was added
* `agentUpgrade()` was added
* `provisioningState()` was added
* `status()` was added
* `locationData()` was added
* `extensions()` was added
* `privateLinkScopeResourceId()` was added
* `machineFqdn()` was added
* `agentVersion()` was added
* `licenseProfile()` was added
* `osProfile()` was added
* `parentClusterResourceId()` was added
* `errorDetails()` was added
* `dnsFqdn()` was added
* `osType()` was added
* `serviceStatuses()` was added
* `vmId()` was added

#### `models.OperationValue` was modified

* `isDataAction()` was added

#### `models.MachineProperties` was modified

* `agentUpgrade()` was added
* `mssqlDiscovered()` was added
* `agentConfiguration()` was added
* `cloudMetadata()` was added
* `osType()` was added
* `innerModel()` was added
* `serviceStatuses()` was added
* `networkProfile()` was added
* `licenseProfile()` was added

#### `models.MachineExtensionUpdate` was modified

* `autoUpgradeMinorVersion()` was added
* `type()` was added
* `publisher()` was added
* `withProtectedSettings(java.util.Map)` was added
* `withTypeHandlerVersion(java.lang.String)` was added
* `withForceUpdateTag(java.lang.String)` was added
* `withAutoUpgradeMinorVersion(java.lang.Boolean)` was added
* `protectedSettings()` was added
* `forceUpdateTag()` was added
* `withType(java.lang.String)` was added
* `enableAutomaticUpgrade()` was added
* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added
* `withPublisher(java.lang.String)` was added
* `settings()` was added
* `typeHandlerVersion()` was added
* `withSettings(java.util.Map)` was added

#### `HybridComputeManager` was modified

* `resourceProviders()` was added
* `licenses()` was added
* `agentVersions()` was added
* `hybridIdentityMetadatas()` was added
* `licenseProfiles()` was added
* `extensionMetadatas()` was added
* `networkProfiles()` was added

#### `models.MachineExtension$Update` was modified

* `withProtectedSettings(java.util.Map)` was added
* `withForceUpdateTag(java.lang.String)` was added
* `withType(java.lang.String)` was added
* `withSettings(java.util.Map)` was added
* `withPublisher(java.lang.String)` was added
* `withTypeHandlerVersion(java.lang.String)` was added
* `withAutoUpgradeMinorVersion(java.lang.Boolean)` was added
* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added

#### `models.Machines` was modified

* `assessPatches(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `installPatches(java.lang.String,java.lang.String,models.MachineInstallPatchesParameters)` was added
* `assessPatches(java.lang.String,java.lang.String)` was added
* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `installPatches(java.lang.String,java.lang.String,models.MachineInstallPatchesParameters,com.azure.core.util.Context)` was added

#### `models.HybridComputePrivateLinkScopeProperties` was modified

* `privateEndpointConnections()` was added

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Machines` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.MachineExtension` was modified

* `resourceGroupName()` was added

#### `HybridComputeManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.HybridComputePrivateLinkScope` was modified

* `resourceGroupName()` was added

#### `HybridComputeManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Machines` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
