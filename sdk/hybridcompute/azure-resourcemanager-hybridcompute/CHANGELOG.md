# Release History

## 1.0.0-beta.7 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.6 (2024-07-22)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2024-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.HotpatchEnablementStatus` was added

* `models.PatchSettingsStatus` was added

#### `models.MachineRunCommandsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationValueDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentUpgrade` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GatewaysListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineExtensionUpgrade` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtensionValueListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkSecurityPerimeterConfigurations` was modified

* `reconcileForPrivateLinkScope(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `reconcileForPrivateLinkScope(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.MachineExtensionInstanceView` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkScopesResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineExtensionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Identity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OSProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceStatuses` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HybridComputePrivateLinkScopeListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProvisioningIssue` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LicenseProfileUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LocationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineRunCommandUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccessRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ProductFeature` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `billingEndDate()` was added
* `error()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Subnet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EsuKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineExtensionUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionStateProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LicenseProfileStorageModelEsuProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineRunCommand$Update` was modified

* `withSource(models.MachineRunCommandScriptSource)` was added
* `withTimeoutInSeconds(java.lang.Integer)` was added
* `withOutputBlobManagedIdentity(models.RunCommandManagedIdentity)` was added
* `withAsyncExecution(java.lang.Boolean)` was added
* `withErrorBlobManagedIdentity(models.RunCommandManagedIdentity)` was added
* `withProtectedParameters(java.util.List)` was added
* `withRunAsPassword(java.lang.String)` was added
* `withRunAsUser(java.lang.String)` was added
* `withParameters(java.util.List)` was added
* `withErrorBlobUri(java.lang.String)` was added
* `withOutputBlobUri(java.lang.String)` was added

#### `models.GatewayUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinuxParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LicenseProfileMachineInstanceView` was modified

* `billingEndDate()` was added
* `error()` was added

#### `models.MachineExtensionsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExtensionTargetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HybridComputePrivateLinkScopeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConfigurationExtension` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterface` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceAssociation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VolumeLicenseDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpAddress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LicenseProfileArmEsuPropertiesWithoutAssignedLicense` was modified

* `assignedLicenseImmutableId()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `esuKeys()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkSecurityPerimeterProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionDataModel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OSProfileLinuxConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `enableHotpatching()` was added
* `withEnableHotpatching(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `status()` was added

#### `models.TagsResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineInstallPatchesParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtensionsResourceStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LicenseUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MachineRunCommandInstanceView` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunCommandManagedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OSProfileWindowsConfiguration` was modified

* `enableHotpatching()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `status()` was added
* `withEnableHotpatching(java.lang.Boolean)` was added

#### `models.AvailablePatchCountByClassification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LicenseDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkSecurityPerimeterConfigurationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProductFeatureUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LicensesListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MachineRunCommandScriptSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RunCommandInputParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineExtensionInstanceViewStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkSecurityPerimeter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WindowsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.5 (2024-06-04)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.LicenseProfileMachineInstanceViewEsuProperties` was modified

* `withAssignedLicense(models.License)` was removed
* `models.LicenseAssignmentState licenseAssignmentState()` -> `models.LicenseAssignmentState licenseAssignmentState()`
* `validate()` was removed
* `models.License assignedLicense()` -> `models.License assignedLicense()`
* `withLicenseAssignmentState(models.LicenseAssignmentState)` was removed

#### `models.EsuKey` was modified

* `java.lang.String licenseStatus()` -> `java.lang.Integer licenseStatus()`
* `withLicenseStatus(java.lang.String)` was removed

#### `models.MachineRunCommand$Update` was modified

* `withParameters(java.util.List)` was removed
* `withTimeoutInSeconds(java.lang.Integer)` was removed
* `withSource(models.MachineRunCommandScriptSource)` was removed
* `withAsyncExecution(java.lang.Boolean)` was removed
* `withOutputBlobUri(java.lang.String)` was removed
* `withErrorBlobManagedIdentity(models.RunCommandManagedIdentity)` was removed
* `withOutputBlobManagedIdentity(models.RunCommandManagedIdentity)` was removed
* `withProtectedParameters(java.util.List)` was removed
* `withRunAsUser(java.lang.String)` was removed
* `withRunAsPassword(java.lang.String)` was removed
* `withErrorBlobUri(java.lang.String)` was removed

#### `models.LicenseProfileMachineInstanceView` was modified

* `validate()` was removed
* `java.util.List productFeatures()` -> `java.util.List productFeatures()`
* `java.lang.Boolean softwareAssuranceCustomer()` -> `java.lang.Boolean softwareAssuranceCustomer()`
* `withProductType(models.LicenseProfileProductType)` was removed
* `withProductFeatures(java.util.List)` was removed
* `java.time.OffsetDateTime disenrollmentDate()` -> `java.time.OffsetDateTime disenrollmentDate()`
* `models.LicenseProfileProductType productType()` -> `models.LicenseProfileProductType productType()`
* `models.LicenseProfileMachineInstanceViewEsuProperties esuProfile()` -> `models.LicenseProfileMachineInstanceViewEsuProperties esuProfile()`
* `models.LicenseProfileSubscriptionStatus subscriptionStatus()` -> `models.LicenseProfileSubscriptionStatus subscriptionStatus()`
* `java.time.OffsetDateTime enrollmentDate()` -> `java.time.OffsetDateTime enrollmentDate()`
* `withSoftwareAssuranceCustomer(java.lang.Boolean)` was removed
* `withSubscriptionStatus(models.LicenseProfileSubscriptionStatus)` was removed
* `withEsuProfile(models.LicenseProfileMachineInstanceViewEsuProperties)` was removed
* `java.time.OffsetDateTime billingStartDate()` -> `java.time.OffsetDateTime billingStartDate()`
* `models.LicenseStatus licenseStatus()` -> `models.LicenseStatus licenseStatus()`
* `java.lang.String licenseChannel()` -> `java.lang.String licenseChannel()`

#### `models.License` was modified

* `withLicenseType(models.LicenseType)` was removed
* `models.LicenseType licenseType()` -> `models.LicenseType licenseType()`
* `withLocation(java.lang.String)` was removed
* `validate()` was removed
* `models.ProvisioningState provisioningState()` -> `models.ProvisioningState provisioningState()`
* `models.LicenseDetails licenseDetails()` -> `models.LicenseDetails licenseDetails()`
* `withTags(java.util.Map)` was removed
* `com.azure.core.management.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `withLicenseDetails(models.LicenseDetails)` was removed
* `withTenantId(java.lang.String)` was removed
* `java.lang.String tenantId()` -> `java.lang.String tenantId()`

### Features Added

* `models.Gateway$DefinitionStages` was added

* `models.GatewaysListResult` was added

* `models.NetworkSecurityPerimeterConfiguration` was added

* `models.AccessMode` was added

* `models.NetworkSecurityPerimeterConfigurations` was added

* `models.Settings` was added

* `models.Licenses` was added

* `models.ProvisioningIssue` was added

* `models.Gateway$Definition` was added

* `models.SettingsOperations` was added

* `models.License$Definition` was added

* `models.GatewayUpdate` was added

* `models.Gateway$Update` was added

* `models.ResourceAssociation` was added

* `models.VolumeLicenseDetails` was added

* `models.Gateway` was added

* `models.NetworkSecurityPerimeterProfile` was added

* `models.License$UpdateStages` was added

* `models.GatewayType` was added

* `models.Gateways` was added

* `models.NetworkSecurityPerimeterConfigurationListResult` was added

* `models.Gateway$UpdateStages` was added

* `models.License$Update` was added

* `models.LicensesListResult` was added

* `models.License$DefinitionStages` was added

* `models.ProgramYear` was added

* `models.NetworkSecurityPerimeter` was added

#### `models.LicenseProfileMachineInstanceViewEsuProperties` was modified

* `esuKeys()` was added
* `serverType()` was added
* `assignedLicenseImmutableId()` was added
* `esuEligibility()` was added
* `innerModel()` was added
* `esuKeyState()` was added

#### `models.EsuKey` was modified

* `withLicenseStatus(java.lang.Integer)` was added

#### `models.LicenseProfileMachineInstanceView` was modified

* `innerModel()` was added

#### `models.License` was modified

* `innerModel()` was added
* `resourceGroupName()` was added
* `location()` was added
* `tags()` was added
* `update()` was added
* `id()` was added
* `regionName()` was added
* `refresh(com.azure.core.util.Context)` was added
* `name()` was added
* `region()` was added
* `refresh()` was added
* `type()` was added

#### `models.LicenseDetails` was modified

* `withVolumeLicenseDetails(java.util.List)` was added
* `volumeLicenseDetails()` was added

#### `HybridComputeManager` was modified

* `gateways()` was added
* `licenses()` was added
* `networkSecurityPerimeterConfigurations()` was added
* `settingsOperations()` was added

## 1.0.0-beta.4 (2024-04-23)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AgentVersion` was removed

* `models.LicenseProfile$DefinitionStages` was removed

* `models.LicenseProfiles` was removed

* `models.Licenses` was removed

* `models.License$Definition` was removed

* `models.HybridIdentityMetadatas` was removed

* `models.HybridIdentityMetadata` was removed

* `models.LicenseProfilesListResult` was removed

* `models.License$UpdateStages` was removed

* `models.LicenseProfile` was removed

* `models.LicenseProfile$Definition` was removed

* `models.LicenseProfile$UpdateStages` was removed

* `models.LicenseProfile$Update` was removed

* `models.AgentVersionsList` was removed

* `models.HybridIdentityMetadataList` was removed

* `models.License$Update` was removed

* `models.LicensesListResult` was removed

* `models.License$DefinitionStages` was removed

* `models.AgentVersions` was removed

* `models.InstanceViewTypes` was removed

#### `models.AgentUpgrade` was modified

* `java.lang.String lastAttemptTimestamp()` -> `java.time.OffsetDateTime lastAttemptTimestamp()`
* `java.lang.String correlationId()` -> `java.util.UUID correlationId()`
* `withCorrelationId(java.lang.String)` was removed

#### `models.LicenseProfileMachineInstanceViewEsuProperties` was modified

* `esuEligibility()` was removed
* `models.License assignedLicense()` -> `models.License assignedLicense()`
* `models.LicenseAssignmentState licenseAssignmentState()` -> `models.LicenseAssignmentState licenseAssignmentState()`
* `assignedLicenseImmutableId()` was removed
* `serverType()` was removed
* `innerModel()` was removed
* `esuKeys()` was removed
* `esuKeyState()` was removed

#### `models.MachineProperties` was modified

* `java.lang.String vmUuid()` -> `java.util.UUID vmUuid()`
* `java.lang.String vmId()` -> `java.util.UUID vmId()`

#### `models.LicenseProfileMachineInstanceView` was modified

* `models.LicenseProfileMachineInstanceViewEsuProperties esuProfile()` -> `models.LicenseProfileMachineInstanceViewEsuProperties esuProfile()`
* `innerModel()` was removed

#### `models.License` was modified

* `update()` was removed
* `location()` was removed
* `java.lang.String tenantId()` -> `java.lang.String tenantId()`
* `refresh(com.azure.core.util.Context)` was removed
* `refresh()` was removed
* `models.LicenseDetails licenseDetails()` -> `models.LicenseDetails licenseDetails()`
* `type()` was removed
* `region()` was removed
* `name()` was removed
* `id()` was removed
* `models.LicenseType licenseType()` -> `models.LicenseType licenseType()`
* `regionName()` was removed
* `innerModel()` was removed
* `resourceGroupName()` was removed
* `tags()` was removed
* `models.ProvisioningState provisioningState()` -> `models.ProvisioningState provisioningState()`
* `com.azure.core.management.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.Machines` was modified

* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,models.InstanceViewTypes,com.azure.core.util.Context)` was removed

#### `models.MachineInstallPatchesParameters` was modified

* `withMaximumDuration(java.lang.String)` was removed
* `java.lang.String maximumDuration()` -> `java.time.Duration maximumDuration()`

#### `models.Machine` was modified

* `java.lang.String vmId()` -> `java.util.UUID vmId()`
* `java.lang.String vmUuid()` -> `java.util.UUID vmUuid()`

#### `HybridComputeManager` was modified

* `hybridIdentityMetadatas()` was removed
* `licenseProfiles()` was removed
* `agentVersions()` was removed
* `licenses()` was removed

#### `models.MachineAssessPatchesResult` was modified

* `java.lang.String assessmentActivityId()` -> `java.util.UUID assessmentActivityId()`

### Features Added

* `models.MachineRunCommandsListResult` was added

* `models.LicenseProfileSubscriptionStatusUpdate` was added

* `models.MachineRunCommand$UpdateStages` was added

* `models.AccessRuleDirection` was added

* `models.MachineRunCommand` was added

* `models.MachineRunCommandUpdate` was added

* `models.AccessRule` was added

* `models.ProductFeature` was added

* `models.MachineRunCommand$Update` was added

* `models.ProvisioningIssueType` was added

* `models.LicenseProfileSubscriptionStatus` was added

* `models.MachineRunCommand$Definition` was added

* `models.ExtensionsStatusLevelTypes` was added

* `models.LicenseProfileProductType` was added

* `models.ExtensionsResourceStatus` was added

* `models.MachineRunCommandInstanceView` was added

* `models.LicenseStatus` was added

* `models.RunCommandManagedIdentity` was added

* `models.MachineRunCommands` was added

* `models.ProductFeatureUpdate` was added

* `models.MachineRunCommandScriptSource` was added

* `models.RunCommandInputParameter` was added

* `models.ProvisioningIssueSeverity` was added

* `models.MachineRunCommand$DefinitionStages` was added

* `models.ExecutionState` was added

#### `models.AgentUpgrade` was modified

* `withCorrelationId(java.util.UUID)` was added

#### `models.LicenseProfileMachineInstanceViewEsuProperties` was modified

* `withLicenseAssignmentState(models.LicenseAssignmentState)` was added
* `validate()` was added
* `withAssignedLicense(models.License)` was added

#### `models.LicenseProfileUpdate` was modified

* `withProductFeatures(java.util.List)` was added
* `subscriptionStatus()` was added
* `withSubscriptionStatus(models.LicenseProfileSubscriptionStatusUpdate)` was added
* `productType()` was added
* `withSoftwareAssuranceCustomer(java.lang.Boolean)` was added
* `productFeatures()` was added
* `withProductType(models.LicenseProfileProductType)` was added
* `softwareAssuranceCustomer()` was added

#### `models.MachineProperties` was modified

* `osEdition()` was added

#### `models.LicenseProfileMachineInstanceView` was modified

* `billingStartDate()` was added
* `productFeatures()` was added
* `disenrollmentDate()` was added
* `withEsuProfile(models.LicenseProfileMachineInstanceViewEsuProperties)` was added
* `validate()` was added
* `productType()` was added
* `withProductFeatures(java.util.List)` was added
* `withSoftwareAssuranceCustomer(java.lang.Boolean)` was added
* `enrollmentDate()` was added
* `subscriptionStatus()` was added
* `licenseChannel()` was added
* `withProductType(models.LicenseProfileProductType)` was added
* `withSubscriptionStatus(models.LicenseProfileSubscriptionStatus)` was added
* `softwareAssuranceCustomer()` was added
* `licenseStatus()` was added

#### `models.License` was modified

* `validate()` was added
* `withTags(java.util.Map)` was added
* `withLicenseDetails(models.LicenseDetails)` was added
* `withLicenseType(models.LicenseType)` was added
* `withTenantId(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.Machines` was modified

* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.MachineInstallPatchesParameters` was modified

* `withMaximumDuration(java.time.Duration)` was added

#### `models.Machine` was modified

* `osEdition()` was added

#### `HybridComputeManager` was modified

* `machineRunCommands()` was added

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
