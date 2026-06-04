# Release History

## 1.3.0-beta.1 (2026-06-04)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK. This Typespec represents the Desktop Virtualization API interfaces. Package api-version 2026-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ExpandMsixImageList` was removed

#### `models.HostPoolList` was removed

#### `models.SessionHostList` was removed

#### `models.StartMenuItemList` was removed

#### `models.ResourceIdentityType` was removed

#### `models.ScalingPlanPersonalScheduleList` was removed

#### `models.MsixPackageList` was removed

#### `models.ResourceProviderOperationList` was removed

#### `models.ResourceModelWithAllowedPropertySet` was removed

#### `models.WorkspaceList` was removed

#### `models.ApplicationList` was removed

#### `models.ApplicationGroupList` was removed

#### `models.Sku` was removed

#### `models.AppAttachPackageProperties` was removed

#### `models.Identity` was removed

#### `models.ScalingPlanPooledScheduleList` was removed

#### `models.AppAttachPackageList` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.ScalingScheduleDaysOfWeekItem` was removed

#### `models.PrivateEndpointConnectionListResultWithSystemData` was removed

#### `models.ScalingPlanList` was removed

#### `models.UserSessionList` was removed

#### `models.DesktopList` was removed

#### `models.Plan` was removed

#### `models.ImportPackageInfoRequest` was modified

* `validate()` was removed

#### `models.SessionHostHealthCheckReport` was modified

* `SessionHostHealthCheckReport()` was changed to private access
* `validate()` was removed

#### `models.RegistrationInfoPatch` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedule$Update` was modified

* `withPeakStartTime(models.Time)` was removed
* `withRampDownCapacityThresholdPct(java.lang.Integer)` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withRampDownLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownForceLogoffUsers(java.lang.Boolean)` was removed
* `withPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownWaitTimeMinutes(java.lang.Integer)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withRampDownStopHostsWhen(models.StopHostsWhen)` was removed
* `withRampUpMinimumHostsPct(java.lang.Integer)` was removed
* `withRampUpLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampUpCapacityThresholdPct(java.lang.Integer)` was removed
* `withRampDownNotificationMessage(java.lang.String)` was removed
* `withOffPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `withRampDownMinimumHostsPct(java.lang.Integer)` was removed

#### `models.HostPool$Update` was modified

* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was removed
* `withRing(java.lang.Integer)` was removed
* `withMaxSessionLimit(java.lang.Integer)` was removed
* `withCustomRdpProperty(java.lang.String)` was removed
* `withAgentUpdate(models.AgentUpdatePatchProperties)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withStartVMOnConnect(java.lang.Boolean)` was removed
* `withDescription(java.lang.String)` was removed
* `withLoadBalancerType(models.LoadBalancerType)` was removed
* `withVmTemplate(java.lang.String)` was removed
* `withSsoadfsAuthority(java.lang.String)` was removed
* `withPreferredAppGroupType(models.PreferredAppGroupType)` was removed
* `withSsoSecretType(models.SsoSecretType)` was removed
* `withSsoClientId(java.lang.String)` was removed
* `withSsoClientSecretKeyVaultPath(java.lang.String)` was removed
* `withPersonalDesktopAssignmentType(models.PersonalDesktopAssignmentType)` was removed
* `withValidationEnvironment(java.lang.Boolean)` was removed
* `withRegistrationInfo(models.RegistrationInfoPatch)` was removed

#### `models.ScalingPlan$Update` was modified

* `withFriendlyName(java.lang.String)` was removed
* `withSchedules(java.util.List)` was removed
* `withHostPoolReferences(java.util.List)` was removed
* `withExclusionTag(java.lang.String)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.AppAttachPackagePatch` was modified

* `validate()` was removed

#### `models.OperationProperties` was modified

* `OperationProperties()` was changed to private access
* `validate()` was removed
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.AppAttachPackagePatchProperties` was modified

* `validate()` was removed

#### `models.WorkspacePatch` was modified

* `validate()` was removed

#### `models.MsixPackage$Update` was modified

* `withDisplayName(java.lang.String)` was removed
* `withIsActive(java.lang.Boolean)` was removed
* `withIsRegularRegistration(java.lang.Boolean)` was removed

#### `models.ScalingHostPoolReference` was modified

* `validate()` was removed

#### `models.DesktopPatch` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed
* `tags()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.AppAttachPackageInfoProperties` was modified

* `validate()` was removed

#### `models.ScalingSchedule` was modified

* `validate()` was removed

#### `models.Application$Update` was modified

* `withMsixPackageApplicationId(java.lang.String)` was removed
* `withShowInPortal(java.lang.Boolean)` was removed
* `withCommandLineArguments(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withMsixPackageFamilyName(java.lang.String)` was removed
* `withIconIndex(java.lang.Integer)` was removed
* `withCommandLineSetting(models.CommandLineSetting)` was removed
* `withFilePath(java.lang.String)` was removed
* `withIconPath(java.lang.String)` was removed
* `withApplicationType(models.RemoteApplicationType)` was removed

#### `models.MsixPackageApplications` was modified

* `validate()` was removed

#### `models.ResourceModelWithAllowedPropertySetPlan` was modified

* `validate()` was removed
* `withPublisher(java.lang.String)` was removed
* `withProduct(java.lang.String)` was removed
* `withPromotionCode(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.HostPool` was modified

* `models.RegistrationTokenList listRegistrationTokens()` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens()`
* `listRegistrationTokensWithResponse(com.azure.core.util.Context)` was removed

#### `models.HostPoolPatch` was modified

* `ssoClientId()` was removed
* `withLoadBalancerType(models.LoadBalancerType)` was removed
* `withPersonalDesktopAssignmentType(models.PersonalDesktopAssignmentType)` was removed
* `withPreferredAppGroupType(models.PreferredAppGroupType)` was removed
* `validate()` was removed
* `customRdpProperty()` was removed
* `startVMOnConnect()` was removed
* `withRegistrationInfo(models.RegistrationInfoPatch)` was removed
* `personalDesktopAssignmentType()` was removed
* `ssoadfsAuthority()` was removed
* `vmTemplate()` was removed
* `validationEnvironment()` was removed
* `withSsoClientSecretKeyVaultPath(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `ssoSecretType()` was removed
* `preferredAppGroupType()` was removed
* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was removed
* `registrationInfo()` was removed
* `withSsoadfsAuthority(java.lang.String)` was removed
* `agentUpdate()` was removed
* `withVmTemplate(java.lang.String)` was removed
* `withSsoClientId(java.lang.String)` was removed
* `maxSessionLimit()` was removed
* `withStartVMOnConnect(java.lang.Boolean)` was removed
* `withMaxSessionLimit(java.lang.Integer)` was removed
* `withRing(java.lang.Integer)` was removed
* `withSsoSecretType(models.SsoSecretType)` was removed
* `withAgentUpdate(models.AgentUpdatePatchProperties)` was removed
* `description()` was removed
* `friendlyName()` was removed
* `withCustomRdpProperty(java.lang.String)` was removed
* `loadBalancerType()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `publicNetworkAccess()` was removed
* `withValidationEnvironment(java.lang.Boolean)` was removed
* `ring()` was removed
* `ssoClientSecretKeyVaultPath()` was removed

#### `models.AppAttachPackage$Definition` was modified

* `withProperties(models.AppAttachPackageProperties)` was removed

#### `models.PrivateEndpointConnections` was modified

* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed
* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed
* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed

#### `models.ScalingPlanPersonalSchedule$Update` was modified

* `withPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withOffPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampDownActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withRampUpMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampDownMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampDownMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withOffPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampDownActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withOffPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `withRampUpMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withRampUpAutoStartHosts(models.StartupBehavior)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withOffPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampDownStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withOffPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed

#### `models.ResourceProviderOperationDisplay` was modified

* `ResourceProviderOperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.SessionHostHealthCheckFailureDetails` was modified

* `SessionHostHealthCheckFailureDetails()` was changed to private access
* `validate()` was removed

#### `models.PrivateLinkResource` was modified

* `requiredMembers()` was removed
* `requiredZoneNames()` was removed
* `groupId()` was removed

#### `DesktopVirtualizationManager` was modified

* `fluent.DesktopVirtualizationApiClient serviceClient()` -> `fluent.DesktopVirtualizationManagementClient serviceClient()`

#### `models.PrivateEndpointConnection` was modified

* `PrivateEndpointConnection()` was changed to private access
* `validate()` was removed
* `provisioningState()` was removed
* `privateLinkServiceConnectionState()` was removed
* `groupIds()` was removed
* `privateEndpoint()` was removed
* `withPrivateEndpoint(models.PrivateEndpoint)` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was removed

#### `models.AppAttachPackages` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AgentUpdateProperties` was modified

* `validate()` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `validate()` was removed
* `withBlobDuration(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.SendMessage` was modified

* `validate()` was removed

#### `models.AgentUpdatePatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `rampDownNotificationMessage()` was removed
* `withRampDownMinimumHostsPct(java.lang.Integer)` was removed
* `offPeakLoadBalancingAlgorithm()` was removed
* `withRampDownForceLogoffUsers(java.lang.Boolean)` was removed
* `rampDownForceLogoffUsers()` was removed
* `withRampUpMinimumHostsPct(java.lang.Integer)` was removed
* `withRampDownNotificationMessage(java.lang.String)` was removed
* `peakLoadBalancingAlgorithm()` was removed
* `withRampDownLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `rampDownMinimumHostsPct()` was removed
* `rampDownStartTime()` was removed
* `rampDownStopHostsWhen()` was removed
* `withRampUpStartTime(models.Time)` was removed
* `offPeakStartTime()` was removed
* `rampUpCapacityThresholdPct()` was removed
* `withRampDownCapacityThresholdPct(java.lang.Integer)` was removed
* `withRampUpLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `daysOfWeek()` was removed
* `withPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `rampUpStartTime()` was removed
* `withRampUpCapacityThresholdPct(java.lang.Integer)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withRampDownWaitTimeMinutes(java.lang.Integer)` was removed
* `rampDownCapacityThresholdPct()` was removed
* `validate()` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withOffPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `peakStartTime()` was removed
* `rampUpLoadBalancingAlgorithm()` was removed
* `rampDownWaitTimeMinutes()` was removed
* `withRampDownStopHostsWhen(models.StopHostsWhen)` was removed
* `rampDownLoadBalancingAlgorithm()` was removed
* `rampUpMinimumHostsPct()` was removed

#### `models.SessionHostPatch` was modified

* `friendlyName()` was removed
* `withAssignedUser(java.lang.String)` was removed
* `allowNewSession()` was removed
* `validate()` was removed
* `withAllowNewSession(java.lang.Boolean)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `assignedUser()` was removed

#### `models.MsixPackagePatch` was modified

* `validate()` was removed
* `withIsActive(java.lang.Boolean)` was removed
* `isActive()` was removed
* `withIsRegularRegistration(java.lang.Boolean)` was removed
* `displayName()` was removed
* `isRegularRegistration()` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.MsixImageUri` was modified

* `validate()` was removed

#### `models.ApplicationGroupPatch` was modified

* `validate()` was removed
* `withShowInFeed(java.lang.Boolean)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `description()` was removed
* `withDescription(java.lang.String)` was removed
* `friendlyName()` was removed
* `showInFeed()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.MaintenanceWindowProperties` was modified

* `validate()` was removed

#### `models.MaintenanceWindowPatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPatch` was modified

* `description()` was removed
* `withTimeZone(java.lang.String)` was removed
* `schedules()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `validate()` was removed
* `hostPoolReferences()` was removed
* `friendlyName()` was removed
* `withHostPoolReferences(java.util.List)` was removed
* `withDescription(java.lang.String)` was removed
* `exclusionTag()` was removed
* `withExclusionTag(java.lang.String)` was removed
* `timeZone()` was removed
* `withSchedules(java.util.List)` was removed

#### `models.AppAttachPackage` was modified

* `properties()` was removed

#### `models.ExpandMsixImage` was modified

* `packageFamilyName()` was removed
* `version()` was removed
* `packageDependencies()` was removed
* `packageName()` was removed
* `isActive()` was removed
* `lastUpdated()` was removed
* `packageAlias()` was removed
* `packageFullName()` was removed
* `certificateExpiry()` was removed
* `isRegularRegistration()` was removed
* `certificateName()` was removed
* `displayName()` was removed
* `packageRelativePath()` was removed
* `imagePath()` was removed
* `packageApplications()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `validate()` was removed
* `withLogSpecifications(java.util.List)` was removed

#### `models.SessionHosts` was modified

* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,models.SessionHostPatch,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.Time` was modified

* `validate()` was removed

#### `models.ScalingPlanPersonalSchedulePatch` was modified

* `withDaysOfWeek(java.util.List)` was removed
* `withRampDownStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `offPeakMinutesToWaitOnLogoff()` was removed
* `withPeakStartTime(models.Time)` was removed
* `rampDownMinutesToWaitOnLogoff()` was removed
* `withOffPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `peakStartVMOnConnect()` was removed
* `rampUpMinutesToWaitOnLogoff()` was removed
* `withRampUpMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `rampUpMinutesToWaitOnDisconnect()` was removed
* `rampDownActionOnDisconnect()` was removed
* `rampUpStartVMOnConnect()` was removed
* `rampUpActionOnLogoff()` was removed
* `withRampDownMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpAutoStartHosts(models.StartupBehavior)` was removed
* `offPeakStartVMOnConnect()` was removed
* `offPeakStartTime()` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `rampUpStartTime()` was removed
* `offPeakMinutesToWaitOnDisconnect()` was removed
* `withRampDownActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampDownMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `rampDownMinutesToWaitOnDisconnect()` was removed
* `rampDownActionOnLogoff()` was removed
* `withPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampUpActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withOffPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `rampUpActionOnDisconnect()` was removed
* `rampUpAutoStartHosts()` was removed
* `withPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `peakActionOnDisconnect()` was removed
* `withPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `daysOfWeek()` was removed
* `rampDownStartVMOnConnect()` was removed
* `withOffPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `peakActionOnLogoff()` was removed
* `rampDownStartTime()` was removed
* `withRampDownActionOnLogoff(models.SessionHandlingOperation)` was removed
* `peakMinutesToWaitOnDisconnect()` was removed
* `validate()` was removed
* `offPeakActionOnDisconnect()` was removed
* `withRampUpActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withOffPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `offPeakActionOnLogoff()` was removed
* `peakStartTime()` was removed
* `withOffPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `peakMinutesToWaitOnLogoff()` was removed
* `withPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpStartVMOnConnect(models.SetStartVMOnConnect)` was removed

#### `models.ResourceModelWithAllowedPropertySetIdentity` was modified

* `withType(models.ResourceIdentityType)` was removed
* `validate()` was removed

#### `models.MsixPackageDependencies` was modified

* `validate()` was removed

#### `models.HostPools` was modified

* `models.RegistrationTokenList listRegistrationTokens(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens(java.lang.String,java.lang.String)`
* `listRegistrationTokensWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ApplicationPatch` was modified

* `validate()` was removed
* `commandLineArguments()` was removed
* `withIconIndex(java.lang.Integer)` was removed
* `tags()` was removed
* `filePath()` was removed
* `iconPath()` was removed
* `msixPackageFamilyName()` was removed
* `withShowInPortal(java.lang.Boolean)` was removed
* `showInPortal()` was removed
* `withTags(java.util.Map)` was removed
* `msixPackageApplicationId()` was removed
* `friendlyName()` was removed
* `withMsixPackageFamilyName(java.lang.String)` was removed
* `iconIndex()` was removed
* `withCommandLineSetting(models.CommandLineSetting)` was removed
* `withApplicationType(models.RemoteApplicationType)` was removed
* `withMsixPackageApplicationId(java.lang.String)` was removed
* `withCommandLineArguments(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `applicationType()` was removed
* `commandLineSetting()` was removed
* `withFilePath(java.lang.String)` was removed
* `description()` was removed
* `withDescription(java.lang.String)` was removed
* `withIconPath(java.lang.String)` was removed

#### `models.RegistrationTokenMinimal` was modified

* `RegistrationTokenMinimal()` was removed
* `java.lang.String token()` -> `java.lang.String token()`
* `withExpirationTime(java.time.OffsetDateTime)` was removed
* `java.time.OffsetDateTime expirationTime()` -> `java.time.OffsetDateTime expirationTime()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `validate()` was removed
* `withToken(java.lang.String)` was removed

#### `models.ResourceModelWithAllowedPropertySetSku` was modified

* `withSize(java.lang.String)` was removed
* `withFamily(java.lang.String)` was removed
* `withTier(models.SkuTier)` was removed
* `withCapacity(java.lang.Integer)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApplicationGroup$Update` was modified

* `withDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withShowInFeed(java.lang.Boolean)` was removed

### Features Added

* `models.VirtualMachineDiskType` was added

* `models.MarketplaceInfoPatchProperties` was added

* `models.SessionHostManagementProvisioningStatus` was added

* `models.ActiveDirectoryInfoProperties` was added

* `models.DiskInfoProperties` was added

* `models.SessionHostConfigurations` was added

* `models.UserAssignedIdentity` was added

* `models.AzureActiveDirectoryInfoProperties` was added

* `models.MsixPackagePatchProperties` was added

* `models.ScalingMethodType` was added

* `models.AllowRDPShortPathWithPrivateLink` was added

* `models.ManagedDiskProperties` was added

* `models.SessionHostManagementUpdateStatus` was added

* `models.PublicUDP` was added

* `models.DomainInfoPatchProperties` was added

* `models.ImageInfoPatchProperties` was added

* `models.SessionHostManagementOperationProgress` was added

* `models.KeyVaultCredentialsProperties` was added

* `models.SessionHostManagementProvisioningStatusProperties` was added

* `models.KeyVaultCredentialsPatchProperties` was added

* `models.SessionHostConfiguration` was added

* `models.ScalingPlanPooledSchedulePatchProperties` was added

* `models.SecurityInfoPatchProperties` was added

* `models.SessionHostProvisioningConfigurationPatchProperties` was added

* `models.HostPoolPatchProperties` was added

* `models.BootDiagnosticsInfoProperties` was added

* `models.SessionHostManagementProvisioningOperationProgress` was added

* `models.ActiveSessionHostConfiguration` was added

* `models.ApplicationGroupPatchProperties` was added

* `models.CanaryPolicy` was added

* `models.ControlSessionHostUpdates` was added

* `models.DomainInfoProperties` was added

* `models.CustomInfoPatchProperties` was added

* `models.ExpandMsixImageProperties` was added

* `models.ManagedServiceIdentityType` was added

* `models.SessionHostProvisioningConfigurationProperties` was added

* `models.SessionHost$DefinitionStages` was added

* `models.ActiveSessionHostConfigurations` was added

* `models.SessionHostManagementProvisioningStatuses` was added

* `models.SessionHostConfigurationPatch` was added

* `models.SessionHostManagementPatch` was added

* `models.BootDiagnosticsInfoPatchProperties` was added

* `models.HostPoolProvisioningControlParameter` was added

* `models.DirectUDP` was added

* `models.ScalingPlanPatchProperties` was added

* `models.NetworkInfoPatchProperties` was added

* `models.SessionHostManagement` was added

* `models.MarketplaceInfoProperties` was added

* `models.SecurityInfoProperties` was added

* `models.PrivateLinkResourceProperties` was added

* `models.HostPoolUpdateAction` was added

* `models.DiffDiskOption` was added

* `models.ManagedPrivateUDP` was added

* `models.UpdateSessionHostsRequestBody` was added

* `models.InitiateSessionHostUpdates` was added

* `models.ProvisioningStateSHC` was added

* `models.HostPoolUpdateControlParameter` was added

* `models.UpdateStatus` was added

* `models.SessionHost$Definition` was added

* `models.ImageInfoProperties` was added

* `models.DeploymentScope` was added

* `models.ScopedRegistrationTokenProperties` was added

* `models.DiffDiskProperties` was added

* `models.HostPoolUpdateConfigurationProperties` was added

* `models.SessionHostPatchProperties` was added

* `models.SessionHostManagementUpdateStatuses` was added

* `models.NetworkInfoProperties` was added

* `models.SessionHostConfigurationPatchProperties` was added

* `models.VirtualMachineSecurityType` was added

* `models.ManagementType` was added

* `models.SessionHostManagementPatchProperties` was added

* `models.SessionHostManagementUpdateStatusProperties` was added

* `models.HostPoolProvisioningAction` was added

* `models.RelayUDP` was added

* `models.SessionHost$Update` was added

* `models.ActiveDirectoryInfoPatchProperties` was added

* `models.DomainJoinType` was added

* `models.ScalingPlanPersonalSchedulePatchProperties` was added

* `models.DiffDiskPlacement` was added

* `models.HostPoolUpdateConfigurationPatchProperties` was added

* `models.ApplicationPatchProperties` was added

* `models.CustomInfoProperties` was added

* `models.SessionHost$UpdateStages` was added

* `models.SessionHostManagementProvisioningOperationStatus` was added

* `models.SessionHostManagements` was added

* `models.Type` was added

* `models.ControlSessionHostProvisionings` was added

* `models.CreateDeleteProperties` was added

* `models.FailedSessionHostCleanupPolicySHC` was added

#### `models.ScalingPlanPooledSchedule$Definition` was modified

* `withScalingMethod(models.ScalingMethodType)` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added

#### `models.ScalingPlanPooledSchedule$Update` was modified

* `withProperties(models.ScalingPlanPooledSchedulePatchProperties)` was added

#### `models.HostPool$Update` was modified

* `withProperties(models.HostPoolPatchProperties)` was added
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was added

#### `models.ScalingPlan$Update` was modified

* `withProperties(models.ScalingPlanPatchProperties)` was added

#### `models.AppAttachPackagePatch` was modified

* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.AppAttachPackagePatchProperties` was modified

* `withPackageLookbackUrl(java.lang.String)` was added
* `withCustomData(java.lang.String)` was added
* `customData()` was added
* `packageLookbackUrl()` was added

#### `models.MsixPackage$Update` was modified

* `withProperties(models.MsixPackagePatchProperties)` was added

#### `models.Workspace` was modified

* `oboTenantId()` was added
* `deploymentScope()` was added

#### `models.ScalingSchedule` was modified

* `withScalingMethod(models.ScalingMethodType)` was added
* `createDelete()` was added
* `scalingMethod()` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added

#### `models.Application$Update` was modified

* `withProperties(models.ApplicationPatchProperties)` was added

#### `models.ResourceModelWithAllowedPropertySetPlan` was modified

* `name()` was added
* `publisher()` was added
* `promotionCode()` was added
* `version()` was added
* `product()` was added

#### `models.ApplicationGroup` was modified

* `oboTenantId()` was added
* `deploymentScope()` was added

#### `models.HostPool` was modified

* `conditionalRdpProperty()` was added
* `relayUDP()` was added
* `managementType()` was added
* `publicUDP()` was added
* `listRegistrationTokens(com.azure.core.util.Context)` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `managedPrivateUDP()` was added
* `directUDP()` was added
* `deploymentScope()` was added
* `oboTenantId()` was added

#### `models.HostPoolPatch` was modified

* `properties()` was added
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was added
* `withProperties(models.HostPoolPatchProperties)` was added
* `identity()` was added

#### `models.AppAttachPackage$Definition` was modified

* `withKeyVaultUrl(java.lang.String)` was added
* `withPackageLookbackUrl(java.lang.String)` was added
* `withDeploymentScope(models.DeploymentScope)` was added
* `withHostPoolReferences(java.util.List)` was added
* `withCustomData(java.lang.String)` was added
* `withPackageOwnerName(java.lang.String)` was added
* `withImage(models.AppAttachPackageInfoProperties)` was added
* `withFailHealthCheckOnStagingFailure(models.FailHealthCheckOnStagingFailure)` was added

#### `models.PrivateEndpointConnections` was modified

* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added
* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added

#### `models.ScalingPlanPersonalSchedule$Update` was modified

* `withProperties(models.ScalingPlanPersonalSchedulePatchProperties)` was added

#### `models.PrivateLinkResource` was modified

* `properties()` was added

#### `models.HostPoolProperties` was modified

* `relayUDP()` was added
* `managedPrivateUDP()` was added
* `deploymentScope()` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `managementType()` was added
* `directUDP()` was added
* `oboTenantId()` was added
* `publicUDP()` was added
* `conditionalRdpProperty()` was added

#### `DesktopVirtualizationManager` was modified

* `initiateSessionHostUpdates()` was added
* `sessionHostConfigurations()` was added
* `sessionHostManagementProvisioningStatuses()` was added
* `sessionHostManagements()` was added
* `controlSessionHostProvisionings()` was added
* `activeSessionHostConfigurations()` was added
* `sessionHostManagementUpdateStatuses()` was added
* `controlSessionHostUpdates()` was added

#### `models.PrivateEndpointConnection` was modified

* `properties()` was added

#### `models.ScalingHostPoolType` was modified

* `PERSONAL` was added

#### `models.AppAttachPackages` was modified

* `delete(java.lang.String,java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.AppAttachPackage$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `withProperties(models.ScalingPlanPooledSchedulePatchProperties)` was added
* `properties()` was added

#### `models.SessionHostPatch` was modified

* `withProperties(models.SessionHostPatchProperties)` was added
* `properties()` was added

#### `models.MsixPackagePatch` was modified

* `properties()` was added
* `withProperties(models.MsixPackagePatchProperties)` was added

#### `models.ApplicationGroup$Definition` was modified

* `withOboTenantId(java.lang.String)` was added

#### `models.LoadBalancerType` was modified

* `MULTIPLE_PERSISTENT` was added

#### `models.ApplicationGroupPatch` was modified

* `withProperties(models.ApplicationGroupPatchProperties)` was added
* `properties()` was added

#### `models.HostPool$Definition` was modified

* `withAllowRDPShortPathWithPrivateLink(models.AllowRDPShortPathWithPrivateLink)` was added
* `withConditionalRdpProperty(java.lang.String)` was added
* `withOboTenantId(java.lang.String)` was added
* `withManagementType(models.ManagementType)` was added
* `withManagedPrivateUDP(models.ManagedPrivateUDP)` was added
* `withDeploymentScope(models.DeploymentScope)` was added
* `withDirectUDP(models.DirectUDP)` was added
* `withPublicUDP(models.PublicUDP)` was added
* `withRelayUDP(models.RelayUDP)` was added

#### `models.ScalingPlanPooledSchedule` was modified

* `createDelete()` was added
* `namePropertiesName()` was added
* `scalingMethod()` was added

#### `models.ScalingPlanPatch` was modified

* `properties()` was added
* `withProperties(models.ScalingPlanPatchProperties)` was added

#### `models.SessionHost` was modified

* `pendingSessions()` was added
* `activeSessions()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added
* `refresh(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `update()` was added
* `disconnectedSessions()` was added
* `refresh()` was added
* `retryProvisioning()` was added
* `lastSessionHostUpdateTime()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties)` was added
* `retryProvisioningWithResponse(com.azure.core.util.Context)` was added
* `sessionHostConfiguration()` was added

#### `models.AppAttachPackage` was modified

* `failHealthCheckOnStagingFailure()` was added
* `packageOwnerName()` was added
* `packageLookbackUrl()` was added
* `deploymentScope()` was added
* `hostPoolReferences()` was added
* `keyVaultUrl()` was added
* `provisioningState()` was added
* `image()` was added
* `customData()` was added

#### `models.ExpandMsixImage` was modified

* `properties()` was added

#### `models.SessionHosts` was modified

* `deleteById(java.lang.String)` was added
* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `retryProvisioning(java.lang.String,java.lang.String,java.lang.String)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `retryProvisioningWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added

#### `models.ScalingPlanPersonalSchedulePatch` was modified

* `properties()` was added
* `withProperties(models.ScalingPlanPersonalSchedulePatchProperties)` was added

#### `models.ResourceModelWithAllowedPropertySetIdentity` was modified

* `type()` was added
* `withType(models.ManagedServiceIdentityType)` was added
* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.Map)` was added

#### `models.HostPools` was modified

* `listRegistrationTokens(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Workspace$Definition` was modified

* `withOboTenantId(java.lang.String)` was added
* `withDeploymentScope(models.DeploymentScope)` was added

#### `models.ApplicationPatch` was modified

* `withProperties(models.ApplicationPatchProperties)` was added
* `properties()` was added

#### `models.RegistrationTokenMinimal` was modified

* `innerModel()` was added

#### `models.ResourceModelWithAllowedPropertySetSku` was modified

* `capacity()` was added
* `family()` was added
* `size()` was added
* `tier()` was added
* `name()` was added

#### `models.ApplicationGroup$Update` was modified

* `withProperties(models.ApplicationGroupPatchProperties)` was added

## 1.2.0 (2024-09-25)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AppAttachPackage$UpdateStages` was added

* `models.ImportPackageInfoRequest` was added

* `models.FailHealthCheckOnStagingFailure` was added

* `models.AppAttachPackagePatch` was added

* `models.AppAttachPackagePatchProperties` was added

* `models.ProvisioningState` was added

* `models.AppAttachPackageInfoProperties` was added

* `models.AppAttachPackage$Definition` was added

* `models.RegistrationTokenList` was added

* `models.AppAttachPackages` was added

* `models.AppAttachPackage$Update` was added

* `models.AppAttachPackageProperties` was added

* `models.AppAttachPackageList` was added

* `models.AppAttachPackage` was added

* `models.AppAttachPackageInfoes` was added

* `models.PackageTimestamped` was added

* `models.AppAttachPackageArchitectures` was added

* `models.RegistrationTokenMinimal` was added

* `models.AppAttachPackage$DefinitionStages` was added

#### `models.PrivateEndpointConnectionWithSystemData` was modified

* `groupIds()` was added

#### `models.ExpandMsixImageList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HostPoolList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SessionHostList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StartMenuItemList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SessionHostHealthCheckReport` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegistrationInfoPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StartMenuItem` was modified

* `systemData()` was added

#### `models.OperationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspacePatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScalingHostPoolReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DesktopPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScalingSchedule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MsixPackageApplications` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScalingPlanPersonalScheduleList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceModelWithAllowedPropertySetPlan` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MsixPackageList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HostPool` was modified

* `listRegistrationTokens()` was added
* `listRegistrationTokensWithResponse(com.azure.core.util.Context)` was added
* `appAttachPackageReferences()` was added

#### `models.ResourceProviderOperationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HostPoolPatch` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `name()` was added

#### `models.ResourceModelWithAllowedPropertySet` was modified

* `type()` was added
* `id()` was added
* `name()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceProviderOperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SessionHostHealthCheckFailureDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.WorkspaceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HostPoolProperties` was modified

* `appAttachPackageReferences()` was added

#### `DesktopVirtualizationManager` was modified

* `appAttachPackages()` was added
* `appAttachPackageInfoes()` was added

#### `models.ApplicationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnection` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `name()` was added
* `groupIds()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.ApplicationGroupList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SendMessage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentUpdatePatchProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Identity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SessionHostPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MsixPackagePatch` was modified

* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added

#### `models.MsixImageUri` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScalingPlanPooledScheduleList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationGroupPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added
* `id()` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MaintenanceWindowProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MaintenanceWindowPatchProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScalingPlanPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExpandMsixImage` was modified

* `certificateName()` was added
* `certificateExpiry()` was added
* `systemData()` was added

#### `models.ServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Time` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResultWithSystemData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScalingPlanList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScalingPlanPersonalSchedulePatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceModelWithAllowedPropertySetIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `tenantId()` was added
* `principalId()` was added

#### `models.MsixPackageDependencies` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserSessionList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HostPools` was modified

* `listRegistrationTokens(java.lang.String,java.lang.String)` was added
* `listRegistrationTokensWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DesktopList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Plan` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceModelWithAllowedPropertySetSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0 (2023-10-16)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-2023-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PrivateEndpointConnectionWithSystemData` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.ScalingPlanPersonalSchedule$UpdateStages` was added

* `models.ScalingPlanPersonalSchedule` was added

* `models.ScalingPlanPersonalSchedule$Definition` was added

* `models.SetStartVMOnConnect` was added

* `models.PrivateEndpoint` was added

* `models.ScalingPlanPersonalScheduleList` was added

* `models.PrivateLinkResources` was added

* `models.SessionHandlingOperation` was added

* `models.ScalingPlanPersonalSchedule$DefinitionStages` was added

* `models.PrivateEndpointConnections` was added

* `models.ScalingPlanPersonalSchedule$Update` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointConnection` was added

* `models.HostpoolPublicNetworkAccess` was added

* `models.ScalingPlanPersonalSchedules` was added

* `models.StartupBehavior` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateEndpointConnectionListResultWithSystemData` was added

* `models.ScalingPlanPersonalSchedulePatch` was added

* `models.PublicNetworkAccess` was added

#### `models.HostPool$Update` was modified

* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was added

#### `models.WorkspacePatch` was modified

* `publicNetworkAccess()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.Workspace` was modified

* `publicNetworkAccess()` was added
* `privateEndpointConnections()` was added

#### `models.ApplicationGroup` was modified

* `showInFeed()` was added

#### `models.HostPool` was modified

* `publicNetworkAccess()` was added
* `privateEndpointConnections()` was added

#### `models.HostPoolPatch` was modified

* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was added
* `publicNetworkAccess()` was added

#### `models.HostPoolProperties` was modified

* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added

#### `DesktopVirtualizationManager` was modified

* `scalingPlanPersonalSchedules()` was added
* `privateEndpointConnections()` was added
* `privateLinkResources()` was added

#### `models.ApplicationGroup$Definition` was modified

* `withShowInFeed(java.lang.Boolean)` was added

#### `models.ApplicationGroupPatch` was modified

* `withShowInFeed(java.lang.Boolean)` was added
* `showInFeed()` was added

#### `models.HostPool$Definition` was modified

* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was added

#### `models.Workspace$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.Workspace$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.ApplicationGroup$Update` was modified

* `withShowInFeed(java.lang.Boolean)` was added

## 1.0.0 (2023-03-20)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.3 (2023-03-16)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionWithSystemData` was removed

* `models.PrivateEndpointConnectionProvisioningState` was removed

* `models.PrivateEndpointConnection` was removed

* `models.MigrationRequestProperties` was removed

* `models.PrivateLinkServiceConnectionState` was removed

* `models.PrivateEndpoint` was removed

* `models.Operation` was removed

* `models.PrivateLinkResourceListResult` was removed

* `models.PrivateEndpointServiceConnectionStatus` was removed

* `models.PrivateEndpointConnectionListResultWithSystemData` was removed

* `models.PrivateLinkResources` was removed

* `models.PrivateEndpointConnections` was removed

* `models.PublicNetworkAccess` was removed

* `models.PrivateLinkResource` was removed

#### `models.ScalingPlan$DefinitionStages` was modified

* Stage 3 was added

#### `models.HostPoolProperties` was modified

* `migrationRequest()` was removed
* `publicNetworkAccess()` was removed

#### `models.StartMenuItems` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `DesktopVirtualizationManager` was modified

* `privateLinkResources()` was removed
* `privateEndpointConnections()` was removed

#### `models.HostPool$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

#### `models.ApplicationGroup$Definition` was modified

* `withMigrationRequest(models.MigrationRequestProperties)` was removed

#### `models.Desktops` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WorkspacePatch` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed
* `publicNetworkAccess()` was removed

#### `models.HostPool$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed
* `withMigrationRequest(models.MigrationRequestProperties)` was removed

#### `models.Workspace` was modified

* `publicNetworkAccess()` was removed

#### `models.Workspace$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

#### `models.ApplicationGroups` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.SessionHosts` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ApplicationGroup` was modified

* `migrationRequest()` was removed

#### `models.ScalingPlans` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByHostPool(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.HostPool` was modified

* `migrationRequest()` was removed
* `publicNetworkAccess()` was removed

#### `models.MsixPackages` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Workspaces` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.HostPoolPatch` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed
* `publicNetworkAccess()` was removed

#### `models.Applications` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.HostPools` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.UserSessions` was modified

* `listByHostPool(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Workspace$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

### Features Added

* `models.ScalingPlanPooledSchedule$Definition` was added

* `models.ScalingPlanPooledSchedule$Update` was added

* `models.AgentUpdateProperties` was added

* `models.AgentUpdatePatchProperties` was added

* `models.ScalingPlanPooledSchedulePatch` was added

* `models.ScalingPlanPooledScheduleList` was added

* `models.SessionHostComponentUpdateType` was added

* `models.MaintenanceWindowProperties` was added

* `models.MaintenanceWindowPatchProperties` was added

* `models.ScalingPlanPooledSchedule` was added

* `models.ScalingPlanPooledSchedule$UpdateStages` was added

* `models.DayOfWeek` was added

* `models.ScalingPlanPooledSchedule$DefinitionStages` was added

* `models.ScalingPlanPooledSchedules` was added

#### `models.HostPoolProperties` was modified

* `agentUpdate()` was added

#### `models.StartMenuItems` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `DesktopVirtualizationManager` was modified

* `scalingPlanPooledSchedules()` was added

#### `models.HostPool$Update` was modified

* `withAgentUpdate(models.AgentUpdatePatchProperties)` was added

#### `models.SessionHostPatch` was modified

* `withFriendlyName(java.lang.String)` was added
* `friendlyName()` was added

#### `models.Desktops` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.HostPool$Definition` was modified

* `withAgentUpdate(models.AgentUpdateProperties)` was added

#### `models.SessionHost` was modified

* `friendlyName()` was added

#### `models.ApplicationGroups` was modified

* `listByResourceGroup(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SessionHosts` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.ScalingPlans` was modified

* `list(java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `listByHostPool(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.HostPool` was modified

* `agentUpdate()` was added

#### `models.MsixPackages` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.Workspaces` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.HostPoolPatch` was modified

* `withAgentUpdate(models.AgentUpdatePatchProperties)` was added
* `agentUpdate()` was added

#### `models.Applications` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.HostPools` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.UserSessions` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added
* `listByHostPool(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2022-06-21)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `DesktopVirtualizationManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `DesktopVirtualizationManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.MsixPackage` was modified

* `resourceGroupName()` was added

#### `models.Workspace` was modified

* `resourceGroupName()` was added

#### `models.Application` was modified

* `resourceGroupName()` was added

#### `models.ApplicationGroup` was modified

* `resourceGroupName()` was added

#### `models.HostPool` was modified

* `resourceGroupName()` was added

#### `models.ScalingPlan` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-11-10)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK.  Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
