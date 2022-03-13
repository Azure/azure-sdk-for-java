# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-01-17)

- Azure Resource Manager LabServices client library for Java. This package contains Microsoft Azure SDK for LabServices Management SDK. REST API for managing Azure Lab Services images. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.GalleryImage$DefinitionStages` was removed

* `ManagedLabsManager` was removed

* `models.LabAccount$UpdateStages` was removed

* `models.SizeInfoFragment` was removed

* `models.EnvironmentFragment` was removed

* `models.UserFragment` was removed

* `models.ResponseWithContinuationUser` was removed

* `models.ResourceSettingsFragment` was removed

* `models.EnvironmentSettings` was removed

* `models.GetPersonalPreferencesResponse` was removed

* `models.ConfigurationState` was removed

* `models.EnvironmentOperationsPayload` was removed

* `models.EnvironmentSettingCreationParameters` was removed

* `models.CreateLabProperties` was removed

* `models.EnvironmentDetails` was removed

* `models.SizeAvailability` was removed

* `models.LabUserAccessMode` was removed

* `models.ResponseWithContinuationLab` was removed

* `models.EnvironmentSetting$Definition` was removed

* `models.ProviderOperationResult` was removed

* `models.ListLabsResponse` was removed

* `models.LabAccounts` was removed

* `models.Environments` was removed

* `models.ResourceSettingCreationParameters` was removed

* `models.GalleryImageFragment` was removed

* `models.Environment$Update` was removed

* `models.AddUsersPayload` was removed

* `models.LabAccount` was removed

* `models.Environment$DefinitionStages` was removed

* `models.ManagedLabVmSize` was removed

* `models.GalleryImages` was removed

* `models.RegisterPayload` was removed

* `models.EnvironmentSetting$Update` was removed

* `models.ResponseWithContinuationEnvironment` was removed

* `models.GetRegionalAvailabilityResponse` was removed

* `models.ProviderOperations` was removed

* `models.EnvironmentSetting` was removed

* `models.Environment$Definition` was removed

* `models.LabAccountFragment` was removed

* `models.OperationStatusPayload` was removed

* `models.OperationMetadata` was removed

* `ManagedLabsManager$Configurable` was removed

* `models.LabAccount$Update` was removed

* `models.PublishPayload` was removed

* `models.GalleryImageReference` was removed

* `models.ResourceSettings` was removed

* `models.GalleryImage$Update` was removed

* `models.GetEnvironmentResponse` was removed

* `models.SizeInfo` was removed

* `models.EnvironmentSizeFragment` was removed

* `models.PersonalPreferencesOperationsPayload` was removed

* `models.AddRemove` was removed

* `models.LabAccount$Definition` was removed

* `models.OperationBatchStatusResponse` was removed

* `models.EnvironmentSize` was removed

* `models.OperationStatusResponse` was removed

* `models.ResetPasswordPayload` was removed

* `models.GlobalUsers` was removed

* `models.ReferenceVm` was removed

* `models.VirtualMachineDetails` was removed

* `models.ListEnvironmentsResponse` was removed

* `models.Environment$UpdateStages` was removed

* `models.GalleryImage$Definition` was removed

* `models.OperationError` was removed

* `models.EnvironmentSettingFragment` was removed

* `models.ReferenceVmCreationParameters` was removed

* `models.PublishingState` was removed

* `models.EnvironmentSetting$UpdateStages` was removed

* `models.RegionalAvailability` was removed

* `models.LatestOperationResult` was removed

* `models.EnvironmentSetting$DefinitionStages` was removed

* `models.ResponseWithContinuationGalleryImage` was removed

* `models.OperationBatchStatusPayload` was removed

* `models.GalleryImage$UpdateStages` was removed

* `models.OperationBatchStatusResponseItem` was removed

* `models.GalleryImage` was removed

* `models.NetworkInterface` was removed

* `models.LabDetails` was removed

* `models.ListEnvironmentsPayload` was removed

* `models.ResponseWithContinuationLabAccount` was removed

* `models.ResponseWithContinuationEnvironmentSetting` was removed

* `models.VmStateDetails` was removed

* `models.LabCreationParameters` was removed

* `models.LabAccount$DefinitionStages` was removed

* `models.ResourceSetFragment` was removed

* `models.SizeConfigurationProperties` was removed

* `models.Environment` was removed

* `models.LabFragment` was removed

* `models.OperationMetadataDisplay` was removed

* `models.ReferenceVmFragment` was removed

* `models.ResourceSet` was removed

#### `models.Lab$DefinitionStages` was modified

* `withExistingLabaccount(java.lang.String,java.lang.String)` was removed in stage 2

#### `models.User$DefinitionStages` was modified

* `withRegion(java.lang.String)` was removed in stage 1
* `withExistingLab(java.lang.String,java.lang.String,java.lang.String)` was removed in stage 1
* `withRegion(com.azure.core.management.Region)` was removed in stage 1
* Stage 2 was added

#### `models.Lab$Update` was modified

* `withTags(java.util.Map)` was removed
* `withMaxUsersInLab(java.lang.Integer)` was removed
* `withUsageQuota(java.time.Duration)` was removed
* `withUserAccessMode(models.LabUserAccessMode)` was removed
* `withUniqueIdentifier(java.lang.String)` was removed
* `withProvisioningState(java.lang.String)` was removed

#### `models.User$Update` was modified

* `withUniqueIdentifier(java.lang.String)` was removed
* `withProvisioningState(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.OperationResult` was modified

* `models.OperationError error()` -> `com.azure.core.management.exception.ManagementError error()`
* `java.lang.String status()` -> `models.OperationStatus status()`

#### `models.Users` was modified

* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Operations` was modified

* `get(java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Lab$Definition` was modified

* `withUserAccessMode(models.LabUserAccessMode)` was removed
* `withExistingLabaccount(java.lang.String,java.lang.String)` was removed
* `withMaxUsersInLab(java.lang.Integer)` was removed
* `withUsageQuota(java.time.Duration)` was removed
* `withProvisioningState(java.lang.String)` was removed
* `withUniqueIdentifier(java.lang.String)` was removed

#### `models.User` was modified

* `tags()` was removed
* `familyName()` was removed
* `regionName()` was removed
* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`
* `tenantId()` was removed
* `latestOperationResult()` was removed
* `uniqueIdentifier()` was removed
* `location()` was removed
* `givenName()` was removed
* `region()` was removed

#### `models.Lab` was modified

* `register()` was removed
* `createdDate()` was removed
* `createdByObjectId()` was removed
* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`
* `maxUsersInLab()` was removed
* `userQuota()` was removed
* `registerWithResponse(com.azure.core.util.Context)` was removed
* `usageQuota()` was removed
* `userAccessMode()` was removed
* `createdByUserPrincipalName()` was removed
* `uniqueIdentifier()` was removed
* `latestOperationResult()` was removed
* `invitationCode()` was removed
* `addUsersWithResponse(models.AddUsersPayload,com.azure.core.util.Context)` was removed
* `addUsers(models.AddUsersPayload)` was removed

#### `models.Labs` was modified

* `addUsersWithResponse(java.lang.String,java.lang.String,java.lang.String,models.AddUsersPayload,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `register(java.lang.String,java.lang.String,java.lang.String)` was removed
* `addUsers(java.lang.String,java.lang.String,java.lang.String,models.AddUsersPayload)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `registerWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.User$Definition` was modified

* `withTags(java.util.Map)` was removed
* `withRegion(java.lang.String)` was removed
* `withProvisioningState(java.lang.String)` was removed
* `withUniqueIdentifier(java.lang.String)` was removed
* `withExistingLab(java.lang.String,java.lang.String,java.lang.String)` was removed
* `withRegion(com.azure.core.management.Region)` was removed

### Features Added

* `models.VirtualMachineAdditionalCapabilities` was added

* `models.Usages` was added

* `models.LabServicesSku` was added

* `models.LabPlanNetworkProfile` was added

* `models.InviteBody` was added

* `models.LabPlan$UpdateStages` was added

* `models.OperationResults` was added

* `models.LabState` was added

* `models.PagedVirtualMachines` was added

* `models.SupportInfo` was added

* `models.UsageName` was added

* `models.LabUpdate` was added

* `models.ResetPasswordBody` was added

* `LabServicesManager` was added

* `models.Schedule$Update` was added

* `models.RestrictionReasonCode` was added

* `models.LabPlan$Update` was added

* `models.Image$UpdateStages` was added

* `models.Images` was added

* `models.PagedUsers` was added

* `LabServicesManager$Configurable` was added

* `models.Schedules` was added

* `models.Operation` was added

* `models.LabServicesSkuCapabilities` was added

* `models.OperationDisplay` was added

* `models.LabPlanUpdate` was added

* `models.Schedule$UpdateStages` was added

* `models.OperationListResult` was added

* `models.VirtualMachineType` was added

* `models.Sku` was added

* `models.LabServicesSkuCost` was added

* `models.UsageUnit` was added

* `models.Credentials` was added

* `models.RecurrenceFrequency` was added

* `models.VirtualMachine` was added

* `models.LabServicesSkuTier` was added

* `models.LabPlan` was added

* `models.Schedule$Definition` was added

* `models.LabServicesSkuCapacity` was added

* `models.PagedLabs` was added

* `models.LabPlan$Definition` was added

* `models.PagedSchedules` was added

* `models.LabPlans` was added

* `models.Image$Update` was added

* `models.ProvisioningState` was added

* `models.Schedule` was added

* `models.VirtualMachineConnectionProfile` was added

* `models.ConnectionType` was added

* `models.OperationStatus` was added

* `models.ScheduleUpdate` was added

* `models.TrackedResourceUpdate` was added

* `models.Usage` was added

* `models.PagedLabServicesSkus` was added

* `models.Image$DefinitionStages` was added

* `models.SecurityProfile` was added

* `models.UserUpdate` was added

* `models.Schedule$DefinitionStages` was added

* `models.ListUsagesResult` was added

* `models.CreateOption` was added

* `models.SkuTier` was added

* `models.RecurrencePattern` was added

* `models.PagedImages` was added

* `models.WeekDay` was added

* `models.Image$Definition` was added

* `models.LabNetworkProfile` was added

* `models.VirtualMachineState` was added

* `models.RegistrationState` was added

* `models.VirtualMachineProfile` was added

* `models.ImageUpdate` was added

* `models.Image` was added

* `models.ShutdownOnIdleMode` was added

* `models.OsState` was added

* `models.Origin` was added

* `models.SaveImageBody` was added

* `models.ConnectionProfile` was added

* `models.LabPlan$DefinitionStages` was added

* `models.VirtualMachines` was added

* `models.AutoShutdownProfile` was added

* `models.OsType` was added

* `models.RestrictionType` was added

* `models.EnableState` was added

* `models.ImageReference` was added

* `models.LabServicesSkuRestrictions` was added

* `models.RosterProfile` was added

* `models.ActionType` was added

* `models.InvitationState` was added

* `models.PagedLabPlans` was added

* `models.Skus` was added

* `models.ScaleType` was added

#### `models.Lab$Update` was modified

* `withVirtualMachineProfile(models.VirtualMachineProfile)` was added
* `withTitle(java.lang.String)` was added
* `withTags(java.util.List)` was added
* `withDescription(java.lang.String)` was added
* `withConnectionProfile(models.ConnectionProfile)` was added
* `withLabPlanId(java.lang.String)` was added
* `withSecurityProfile(models.SecurityProfile)` was added
* `withAutoShutdownProfile(models.AutoShutdownProfile)` was added
* `withRosterProfile(models.RosterProfile)` was added

#### `models.User$Update` was modified

* `withAdditionalUsageQuota(java.time.Duration)` was added

#### `models.OperationResult` was modified

* `percentComplete()` was added
* `id()` was added
* `startTime()` was added
* `name()` was added
* `endTime()` was added

#### `models.Users` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByLab(java.lang.String,java.lang.String)` was added
* `listByLab(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `invite(java.lang.String,java.lang.String,java.lang.String,models.InviteBody,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `invite(java.lang.String,java.lang.String,java.lang.String,models.InviteBody)` was added

#### `models.Operations` was modified

* `list()` was added
* `list(com.azure.core.util.Context)` was added

#### `models.Lab$Definition` was modified

* `withTitle(java.lang.String)` was added
* `withLabPlanId(java.lang.String)` was added
* `withNetworkProfile(models.LabNetworkProfile)` was added
* `withDescription(java.lang.String)` was added
* `withAutoShutdownProfile(models.AutoShutdownProfile)` was added
* `withVirtualMachineProfile(models.VirtualMachineProfile)` was added
* `withSecurityProfile(models.SecurityProfile)` was added
* `withExistingResourceGroup(java.lang.String)` was added
* `withRosterProfile(models.RosterProfile)` was added
* `withConnectionProfile(models.ConnectionProfile)` was added

#### `models.User` was modified

* `invitationSent()` was added
* `invitationState()` was added
* `invite(models.InviteBody,com.azure.core.util.Context)` was added
* `invite(models.InviteBody)` was added
* `registrationState()` was added
* `displayName()` was added
* `systemData()` was added
* `additionalUsageQuota()` was added

#### `models.Lab` was modified

* `systemData()` was added
* `connectionProfile()` was added
* `syncGroup(com.azure.core.util.Context)` was added
* `autoShutdownProfile()` was added
* `state()` was added
* `labPlanId()` was added
* `publish(com.azure.core.util.Context)` was added
* `rosterProfile()` was added
* `networkProfile()` was added
* `title()` was added
* `publish()` was added
* `securityProfile()` was added
* `virtualMachineProfile()` was added
* `syncGroup()` was added
* `description()` was added

#### `models.Labs` was modified

* `syncGroup(java.lang.String,java.lang.String)` was added
* `list(java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroup(java.lang.String,java.lang.String)` was added
* `syncGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `list()` was added
* `publish(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `publish(java.lang.String,java.lang.String)` was added

#### `models.User$Definition` was modified

* `withAdditionalUsageQuota(java.time.Duration)` was added
* `withExistingLab(java.lang.String,java.lang.String)` was added
* `withEmail(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager ManagedLabs client library for Java. This package contains Microsoft Azure SDK for ManagedLabs Management SDK. The Managed Labs Client. Package tag package-2018-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
