# Release History

## 1.3.0-beta.1 (2026-05-28)

- Azure Resource Manager DesktopVirtualization client library for Java. This package contains Microsoft Azure SDK for DesktopVirtualization Management SDK. This Typespec represents the Desktop Virtualization API interfaces. Package api-version 2026-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ExpandMsixImageList` was removed

#### `models.HostPoolList` was removed

#### `models.SessionHostList` was removed

#### `models.StartMenuItemList` was removed

#### `models.ResourceIdentityType` was removed

#### `models.ScalingPlanPersonalScheduleList` was removed

#### `models.ResourceModelWithAllowedPropertySetPlan` was removed

#### `models.MsixPackageList` was removed

#### `models.ResourceProviderOperationList` was removed

#### `models.ResourceModelWithAllowedPropertySet` was removed

#### `models.WorkspaceList` was removed

#### `models.ApplicationList` was removed

#### `models.ApplicationGroupList` was removed

#### `models.Identity` was removed

#### `models.ScalingPlanPooledScheduleList` was removed

#### `models.AppAttachPackageList` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.ScalingScheduleDaysOfWeekItem` was removed

#### `models.PrivateEndpointConnectionListResultWithSystemData` was removed

#### `models.ScalingPlanList` was removed

#### `models.ResourceModelWithAllowedPropertySetIdentity` was removed

#### `models.UserSessionList` was removed

#### `models.DesktopList` was removed

#### `models.ResourceModelWithAllowedPropertySetSku` was removed

#### `models.ScalingPlanPersonalSchedule$DefinitionStages` was modified

* Required stage 2 was added

#### `models.ScalingPlan$DefinitionStages` was modified

* `withTimeZone(java.lang.String)` was removed in stage 3

#### `models.ScalingPlanPooledSchedule$DefinitionStages` was modified

* Required stage 2 was added

#### `models.ApplicationGroup$DefinitionStages` was modified

* `withHostPoolArmPath(java.lang.String)` was removed in stage 3

#### `models.MsixPackage$DefinitionStages` was modified

* Required stage 2 was added

#### `models.HostPool$DefinitionStages` was modified

* `withHostPoolType(models.HostPoolType)` was removed in stage 3

#### `models.Application$DefinitionStages` was modified

* `withCommandLineSetting(models.CommandLineSetting)` was removed in stage 2

#### `models.PrivateEndpointConnectionWithSystemData` was modified

* `provisioningState()` was removed
* `privateEndpoint()` was removed
* `groupIds()` was removed
* `privateLinkServiceConnectionState()` was removed

#### `models.ScalingPlanPooledSchedule$Definition` was modified

* `withRampDownStartTime(models.Time)` was removed
* `withRampDownCapacityThresholdPct(java.lang.Integer)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withRampDownStopHostsWhen(models.StopHostsWhen)` was removed
* `withOffPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampUpCapacityThresholdPct(java.lang.Integer)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withRampUpLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownMinimumHostsPct(java.lang.Integer)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withRampUpMinimumHostsPct(java.lang.Integer)` was removed
* `withPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownWaitTimeMinutes(java.lang.Integer)` was removed
* `withRampDownForceLogoffUsers(java.lang.Boolean)` was removed
* `withRampDownNotificationMessage(java.lang.String)` was removed
* `withDaysOfWeek(java.util.List)` was removed

#### `models.ImportPackageInfoRequest` was modified

* `validate()` was removed

#### `models.UserSession` was modified

* `objectId()` was removed
* `activeDirectoryUsername()` was removed
* `applicationType()` was removed
* `sessionState()` was removed
* `createTime()` was removed
* `userPrincipalName()` was removed

#### `models.SessionHostHealthCheckReport` was modified

* `SessionHostHealthCheckReport()` was changed to private access
* `validate()` was removed

#### `models.RegistrationInfoPatch` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedule$Update` was modified

* `withDaysOfWeek(java.util.List)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `withOffPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withRampDownForceLogoffUsers(java.lang.Boolean)` was removed
* `withRampUpMinimumHostsPct(java.lang.Integer)` was removed
* `withRampDownStopHostsWhen(models.StopHostsWhen)` was removed
* `withRampDownLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownWaitTimeMinutes(java.lang.Integer)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownCapacityThresholdPct(java.lang.Integer)` was removed
* `withRampUpLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownMinimumHostsPct(java.lang.Integer)` was removed
* `withRampUpCapacityThresholdPct(java.lang.Integer)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withRampDownNotificationMessage(java.lang.String)` was removed

#### `models.HostPool$Update` was modified

* `withMaxSessionLimit(java.lang.Integer)` was removed
* `withAgentUpdate(models.AgentUpdatePatchProperties)` was removed
* `withSsoClientSecretKeyVaultPath(java.lang.String)` was removed
* `withRing(java.lang.Integer)` was removed
* `withCustomRdpProperty(java.lang.String)` was removed
* `withSsoSecretType(models.SsoSecretType)` was removed
* `withSsoadfsAuthority(java.lang.String)` was removed
* `withVmTemplate(java.lang.String)` was removed
* `withLoadBalancerType(models.LoadBalancerType)` was removed
* `withSsoClientId(java.lang.String)` was removed
* `withRegistrationInfo(models.RegistrationInfoPatch)` was removed
* `withStartVMOnConnect(java.lang.Boolean)` was removed
* `withDescription(java.lang.String)` was removed
* `withPersonalDesktopAssignmentType(models.PersonalDesktopAssignmentType)` was removed
* `withPreferredAppGroupType(models.PreferredAppGroupType)` was removed
* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withValidationEnvironment(java.lang.Boolean)` was removed

#### `models.ScalingPlan$Update` was modified

* `withSchedules(java.util.List)` was removed
* `withHostPoolReferences(java.util.List)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withExclusionTag(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.AppAttachPackagePatch` was modified

* `validate()` was removed

#### `models.StartMenuItem` was modified

* `iconPath()` was removed
* `iconIndex()` was removed
* `commandLineArguments()` was removed
* `filePath()` was removed
* `appAlias()` was removed

#### `models.OperationProperties` was modified

* `OperationProperties()` was changed to private access
* `validate()` was removed
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.AppAttachPackagePatchProperties` was modified

* `validate()` was removed

#### `models.WorkspacePatch` was modified

* `withDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `validate()` was removed
* `withApplicationGroupReferences(java.util.List)` was removed
* `description()` was removed
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed
* `friendlyName()` was removed
* `applicationGroupReferences()` was removed
* `publicNetworkAccess()` was removed

#### `models.ScalingPlanPersonalSchedule` was modified

* `rampDownActionOnLogoff()` was removed
* `offPeakActionOnLogoff()` was removed
* `peakStartVMOnConnect()` was removed
* `offPeakMinutesToWaitOnDisconnect()` was removed
* `rampDownActionOnDisconnect()` was removed
* `rampUpStartVMOnConnect()` was removed
* `peakActionOnDisconnect()` was removed
* `rampUpActionOnDisconnect()` was removed
* `rampUpMinutesToWaitOnLogoff()` was removed
* `peakMinutesToWaitOnLogoff()` was removed
* `rampUpActionOnLogoff()` was removed
* `rampUpAutoStartHosts()` was removed
* `rampUpMinutesToWaitOnDisconnect()` was removed
* `offPeakActionOnDisconnect()` was removed
* `peakMinutesToWaitOnDisconnect()` was removed
* `peakStartTime()` was removed
* `rampDownStartTime()` was removed
* `rampDownMinutesToWaitOnDisconnect()` was removed
* `offPeakStartTime()` was removed
* `peakActionOnLogoff()` was removed
* `rampDownStartVMOnConnect()` was removed
* `offPeakMinutesToWaitOnLogoff()` was removed
* `rampDownMinutesToWaitOnLogoff()` was removed
* `rampUpStartTime()` was removed
* `daysOfWeek()` was removed
* `offPeakStartVMOnConnect()` was removed

#### `models.ScalingPlanPersonalSchedule$Definition` was modified

* `withRampDownActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampUpActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withOffPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withOffPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampDownActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampDownStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampDownMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampUpMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampDownMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `withPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpAutoStartHosts(models.StartupBehavior)` was removed

#### `models.MsixPackage$Update` was modified

* `withIsRegularRegistration(java.lang.Boolean)` was removed
* `withIsActive(java.lang.Boolean)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.Workspace` was modified

* `applicationGroupReferences()` was removed
* `description()` was removed
* `friendlyName()` was removed
* `publicNetworkAccess()` was removed
* `models.ResourceModelWithAllowedPropertySetSku sku()` -> `models.Sku sku()`
* `cloudPcResource()` was removed
* `models.ResourceModelWithAllowedPropertySetPlan plan()` -> `models.Plan plan()`
* `privateEndpointConnections()` was removed
* `objectId()` was removed
* `models.ResourceModelWithAllowedPropertySetIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.MsixPackage$Definition` was modified

* `withPackageDependencies(java.util.List)` was removed
* `withIsActive(java.lang.Boolean)` was removed
* `withPackageRelativePath(java.lang.String)` was removed
* `withPackageFamilyName(java.lang.String)` was removed
* `withPackageName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withImagePath(java.lang.String)` was removed
* `withPackageApplications(java.util.List)` was removed
* `withVersion(java.lang.String)` was removed
* `withLastUpdated(java.time.OffsetDateTime)` was removed
* `withIsRegularRegistration(java.lang.Boolean)` was removed

#### `models.ScalingHostPoolReference` was modified

* `validate()` was removed

#### `models.DesktopPatch` was modified

* `withFriendlyName(java.lang.String)` was removed
* `description()` was removed
* `validate()` was removed
* `tags()` was removed
* `withTags(java.util.Map)` was removed
* `friendlyName()` was removed
* `withDescription(java.lang.String)` was removed

#### `models.Application` was modified

* `commandLineSetting()` was removed
* `iconPath()` was removed
* `objectId()` was removed
* `iconIndex()` was removed
* `applicationType()` was removed
* `friendlyName()` was removed
* `commandLineArguments()` was removed
* `iconContent()` was removed
* `showInPortal()` was removed
* `msixPackageApplicationId()` was removed
* `iconHash()` was removed
* `msixPackageFamilyName()` was removed
* `description()` was removed
* `filePath()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.AppAttachPackageInfoProperties` was modified

* `validate()` was removed

#### `models.ScalingSchedule` was modified

* `validate()` was removed

#### `models.Application$Update` was modified

* `withShowInPortal(java.lang.Boolean)` was removed
* `withCommandLineArguments(java.lang.String)` was removed
* `withFilePath(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withCommandLineSetting(models.CommandLineSetting)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withIconIndex(java.lang.Integer)` was removed
* `withApplicationType(models.RemoteApplicationType)` was removed
* `withDescription(java.lang.String)` was removed
* `withMsixPackageApplicationId(java.lang.String)` was removed
* `withIconPath(java.lang.String)` was removed
* `withMsixPackageFamilyName(java.lang.String)` was removed

#### `models.MsixPackageApplications` was modified

* `appUserModelId()` was removed
* `validate()` was removed
* `withAppUserModelId(java.lang.String)` was removed

#### `models.Application$Definition` was modified

* `withIconPath(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withShowInPortal(java.lang.Boolean)` was removed
* `withMsixPackageApplicationId(java.lang.String)` was removed
* `withCommandLineSetting(models.CommandLineSetting)` was removed
* `withMsixPackageFamilyName(java.lang.String)` was removed
* `withApplicationType(models.RemoteApplicationType)` was removed
* `withCommandLineArguments(java.lang.String)` was removed
* `withFilePath(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withIconIndex(java.lang.Integer)` was removed

#### `models.ApplicationGroup` was modified

* `models.ResourceModelWithAllowedPropertySetSku sku()` -> `models.Sku sku()`
* `objectId()` was removed
* `cloudPcResource()` was removed
* `models.ResourceModelWithAllowedPropertySetIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `workspaceArmPath()` was removed
* `friendlyName()` was removed
* `applicationGroupType()` was removed
* `description()` was removed
* `models.ResourceModelWithAllowedPropertySetPlan plan()` -> `models.Plan plan()`
* `hostPoolArmPath()` was removed
* `showInFeed()` was removed

#### `models.HostPool` was modified

* `hostPoolType()` was removed
* `appAttachPackageReferences()` was removed
* `description()` was removed
* `models.RegistrationTokenList listRegistrationTokens()` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens()`
* `cloudPcResource()` was removed
* `preferredAppGroupType()` was removed
* `ssoClientId()` was removed
* `startVMOnConnect()` was removed
* `ssoSecretType()` was removed
* `publicNetworkAccess()` was removed
* `models.ResourceModelWithAllowedPropertySetIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `maxSessionLimit()` was removed
* `customRdpProperty()` was removed
* `privateEndpointConnections()` was removed
* `friendlyName()` was removed
* `personalDesktopAssignmentType()` was removed
* `ssoClientSecretKeyVaultPath()` was removed
* `models.ResourceModelWithAllowedPropertySetSku sku()` -> `models.Sku sku()`
* `validationEnvironment()` was removed
* `ring()` was removed
* `applicationGroupReferences()` was removed
* `models.ResourceModelWithAllowedPropertySetPlan plan()` -> `models.Plan plan()`
* `vmTemplate()` was removed
* `listRegistrationTokensWithResponse(com.azure.core.util.Context)` was removed
* `registrationInfo()` was removed
* `agentUpdate()` was removed
* `objectId()` was removed
* `ssoadfsAuthority()` was removed
* `loadBalancerType()` was removed

#### `models.HostPoolPatch` was modified

* `vmTemplate()` was removed
* `registrationInfo()` was removed
* `withDescription(java.lang.String)` was removed
* `validationEnvironment()` was removed
* `withSsoSecretType(models.SsoSecretType)` was removed
* `withCustomRdpProperty(java.lang.String)` was removed
* `loadBalancerType()` was removed
* `validate()` was removed
* `withSsoadfsAuthority(java.lang.String)` was removed
* `preferredAppGroupType()` was removed
* `withSsoClientSecretKeyVaultPath(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withStartVMOnConnect(java.lang.Boolean)` was removed
* `ssoClientId()` was removed
* `description()` was removed
* `withLoadBalancerType(models.LoadBalancerType)` was removed
* `withAgentUpdate(models.AgentUpdatePatchProperties)` was removed
* `withSsoClientId(java.lang.String)` was removed
* `personalDesktopAssignmentType()` was removed
* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was removed
* `ssoadfsAuthority()` was removed
* `maxSessionLimit()` was removed
* `ssoSecretType()` was removed
* `startVMOnConnect()` was removed
* `customRdpProperty()` was removed
* `withRing(java.lang.Integer)` was removed
* `withPersonalDesktopAssignmentType(models.PersonalDesktopAssignmentType)` was removed
* `withValidationEnvironment(java.lang.Boolean)` was removed
* `friendlyName()` was removed
* `withRegistrationInfo(models.RegistrationInfoPatch)` was removed
* `ssoClientSecretKeyVaultPath()` was removed
* `ring()` was removed
* `withPreferredAppGroupType(models.PreferredAppGroupType)` was removed
* `publicNetworkAccess()` was removed
* `withVmTemplate(java.lang.String)` was removed
* `agentUpdate()` was removed
* `withMaxSessionLimit(java.lang.Integer)` was removed

#### `models.PrivateEndpointConnections` was modified

* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed
* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed
* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed

#### `models.ScalingPlanPersonalSchedule$Update` was modified

* `withRampUpMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampDownActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withPeakStartTime(models.Time)` was removed
* `withRampDownMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withOffPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `withOffPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampDownStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampDownMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withOffPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withRampUpAutoStartHosts(models.StartupBehavior)` was removed
* `withOffPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withRampUpActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampDownActionOnDisconnect(models.SessionHandlingOperation)` was removed

#### `models.ResourceProviderOperationDisplay` was modified

* `ResourceProviderOperationDisplay()` was changed to private access
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.SessionHostHealthCheckFailureDetails` was modified

* `SessionHostHealthCheckFailureDetails()` was changed to private access
* `validate()` was removed

#### `models.PrivateLinkResource` was modified

* `groupId()` was removed
* `requiredMembers()` was removed
* `requiredZoneNames()` was removed

#### `DesktopVirtualizationManager` was modified

* `fluent.DesktopVirtualizationApiClient serviceClient()` -> `fluent.DesktopVirtualizationManagementClient serviceClient()`

#### `models.PrivateEndpointConnection` was modified

* `PrivateEndpointConnection()` was changed to private access
* `privateEndpoint()` was removed
* `privateLinkServiceConnectionState()` was removed
* `validate()` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was removed
* `withPrivateEndpoint(models.PrivateEndpoint)` was removed
* `provisioningState()` was removed
* `groupIds()` was removed

#### `models.AppAttachPackages` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.AgentUpdateProperties` was modified

* `validate()` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withBlobDuration(java.lang.String)` was removed

#### `models.SendMessage` was modified

* `validate()` was removed

#### `models.AppAttachPackageProperties` was modified

* `validate()` was removed

#### `models.AgentUpdatePatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `withRampUpLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `rampDownCapacityThresholdPct()` was removed
* `rampDownStopHostsWhen()` was removed
* `withRampDownWaitTimeMinutes(java.lang.Integer)` was removed
* `withPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `withRampDownForceLogoffUsers(java.lang.Boolean)` was removed
* `peakStartTime()` was removed
* `rampDownNotificationMessage()` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `validate()` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withRampDownMinimumHostsPct(java.lang.Integer)` was removed
* `offPeakStartTime()` was removed
* `offPeakLoadBalancingAlgorithm()` was removed
* `withRampUpStartTime(models.Time)` was removed
* `rampUpCapacityThresholdPct()` was removed
* `withPeakStartTime(models.Time)` was removed
* `withOffPeakLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `rampUpMinimumHostsPct()` was removed
* `withRampDownNotificationMessage(java.lang.String)` was removed
* `withRampDownStopHostsWhen(models.StopHostsWhen)` was removed
* `withRampUpMinimumHostsPct(java.lang.Integer)` was removed
* `withRampDownStartTime(models.Time)` was removed
* `rampDownWaitTimeMinutes()` was removed
* `rampUpLoadBalancingAlgorithm()` was removed
* `daysOfWeek()` was removed
* `peakLoadBalancingAlgorithm()` was removed
* `rampDownStartTime()` was removed
* `rampUpStartTime()` was removed
* `withRampDownCapacityThresholdPct(java.lang.Integer)` was removed
* `withRampDownLoadBalancingAlgorithm(models.SessionHostLoadBalancingAlgorithm)` was removed
* `rampDownMinimumHostsPct()` was removed
* `rampDownForceLogoffUsers()` was removed
* `rampDownLoadBalancingAlgorithm()` was removed
* `withRampUpCapacityThresholdPct(java.lang.Integer)` was removed

#### `models.Desktop` was modified

* `friendlyName()` was removed
* `description()` was removed
* `iconHash()` was removed
* `iconContent()` was removed
* `objectId()` was removed

#### `models.SessionHostPatch` was modified

* `withFriendlyName(java.lang.String)` was removed
* `withAssignedUser(java.lang.String)` was removed
* `assignedUser()` was removed
* `withAllowNewSession(java.lang.Boolean)` was removed
* `friendlyName()` was removed
* `validate()` was removed
* `allowNewSession()` was removed

#### `models.MsixPackagePatch` was modified

* `withIsRegularRegistration(java.lang.Boolean)` was removed
* `displayName()` was removed
* `withDisplayName(java.lang.String)` was removed
* `isActive()` was removed
* `isRegularRegistration()` was removed
* `validate()` was removed
* `withIsActive(java.lang.Boolean)` was removed

#### `models.MsixImageUri` was modified

* `validate()` was removed

#### `models.ApplicationGroup$Definition` was modified

* `withApplicationGroupType(models.ApplicationGroupType)` was removed
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was removed
* `withHostPoolArmPath(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withPlan(models.ResourceModelWithAllowedPropertySetPlan)` was removed
* `withShowInFeed(java.lang.Boolean)` was removed
* `withSku(models.ResourceModelWithAllowedPropertySetSku)` was removed

#### `models.MsixPackage` was modified

* `packageFamilyName()` was removed
* `packageName()` was removed
* `version()` was removed
* `isRegularRegistration()` was removed
* `packageApplications()` was removed
* `packageRelativePath()` was removed
* `lastUpdated()` was removed
* `displayName()` was removed
* `isActive()` was removed
* `packageDependencies()` was removed
* `imagePath()` was removed

#### `models.ApplicationGroupPatch` was modified

* `showInFeed()` was removed
* `withDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `validate()` was removed
* `withShowInFeed(java.lang.Boolean)` was removed
* `friendlyName()` was removed
* `description()` was removed

#### `models.HostPool$Definition` was modified

* `withMaxSessionLimit(java.lang.Integer)` was removed
* `withSsoSecretType(models.SsoSecretType)` was removed
* `withPersonalDesktopAssignmentType(models.PersonalDesktopAssignmentType)` was removed
* `withSsoClientId(java.lang.String)` was removed
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was removed
* `withHostPoolType(models.HostPoolType)` was removed
* `withLoadBalancerType(models.LoadBalancerType)` was removed
* `withRegistrationInfo(fluent.models.RegistrationInfoInner)` was removed
* `withPlan(models.ResourceModelWithAllowedPropertySetPlan)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withValidationEnvironment(java.lang.Boolean)` was removed
* `withDescription(java.lang.String)` was removed
* `withAgentUpdate(models.AgentUpdateProperties)` was removed
* `withSsoadfsAuthority(java.lang.String)` was removed
* `withVmTemplate(java.lang.String)` was removed
* `withSsoClientSecretKeyVaultPath(java.lang.String)` was removed
* `withSku(models.ResourceModelWithAllowedPropertySetSku)` was removed
* `withPublicNetworkAccess(models.HostpoolPublicNetworkAccess)` was removed
* `withStartVMOnConnect(java.lang.Boolean)` was removed
* `withCustomRdpProperty(java.lang.String)` was removed
* `withPreferredAppGroupType(models.PreferredAppGroupType)` was removed
* `withRing(java.lang.Integer)` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.MaintenanceWindowProperties` was modified

* `validate()` was removed

#### `models.MaintenanceWindowPatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedule` was modified

* `rampDownWaitTimeMinutes()` was removed
* `rampDownMinimumHostsPct()` was removed
* `rampUpMinimumHostsPct()` was removed
* `peakStartTime()` was removed
* `offPeakLoadBalancingAlgorithm()` was removed
* `rampDownForceLogoffUsers()` was removed
* `rampDownStartTime()` was removed
* `daysOfWeek()` was removed
* `rampDownLoadBalancingAlgorithm()` was removed
* `rampUpLoadBalancingAlgorithm()` was removed
* `rampDownNotificationMessage()` was removed
* `rampUpCapacityThresholdPct()` was removed
* `rampDownCapacityThresholdPct()` was removed
* `rampUpStartTime()` was removed
* `peakLoadBalancingAlgorithm()` was removed
* `rampDownStopHostsWhen()` was removed
* `offPeakStartTime()` was removed

#### `models.ScalingPlanPatch` was modified

* `schedules()` was removed
* `withSchedules(java.util.List)` was removed
* `friendlyName()` was removed
* `timeZone()` was removed
* `withHostPoolReferences(java.util.List)` was removed
* `exclusionTag()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withExclusionTag(java.lang.String)` was removed
* `withTimeZone(java.lang.String)` was removed
* `validate()` was removed
* `hostPoolReferences()` was removed
* `withDescription(java.lang.String)` was removed
* `description()` was removed

#### `models.Workspace$Update` was modified

* `withDescription(java.lang.String)` was removed
* `withApplicationGroupReferences(java.util.List)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

#### `models.SessionHost` was modified

* `sxSStackVersion()` was removed
* `updateState()` was removed
* `objectId()` was removed
* `assignedUser()` was removed
* `resourceId()` was removed
* `updateErrorMessage()` was removed
* `friendlyName()` was removed
* `lastHeartBeat()` was removed
* `sessions()` was removed
* `sessionHostHealthCheckResults()` was removed
* `status()` was removed
* `lastUpdateTime()` was removed
* `statusTimestamp()` was removed
* `agentVersion()` was removed
* `osVersion()` was removed
* `allowNewSession()` was removed
* `virtualMachineId()` was removed

#### `models.ExpandMsixImage` was modified

* `packageRelativePath()` was removed
* `isRegularRegistration()` was removed
* `packageFullName()` was removed
* `packageApplications()` was removed
* `packageDependencies()` was removed
* `certificateName()` was removed
* `imagePath()` was removed
* `version()` was removed
* `displayName()` was removed
* `packageAlias()` was removed
* `certificateExpiry()` was removed
* `packageFamilyName()` was removed
* `isActive()` was removed
* `packageName()` was removed
* `lastUpdated()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `validate()` was removed
* `withLogSpecifications(java.util.List)` was removed

#### `models.SessionHosts` was modified

* `update(java.lang.String,java.lang.String,java.lang.String)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,models.SessionHostPatch,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.Time` was modified

* `validate()` was removed

#### `models.ScalingPlanPersonalSchedulePatch` was modified

* `rampUpActionOnDisconnect()` was removed
* `peakMinutesToWaitOnDisconnect()` was removed
* `withRampUpMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `peakActionOnLogoff()` was removed
* `rampDownStartTime()` was removed
* `withPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withOffPeakActionOnLogoff(models.SessionHandlingOperation)` was removed
* `offPeakActionOnDisconnect()` was removed
* `withOffPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `rampUpStartVMOnConnect()` was removed
* `peakMinutesToWaitOnLogoff()` was removed
* `validate()` was removed
* `withRampDownActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `rampUpMinutesToWaitOnDisconnect()` was removed
* `withRampUpActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `rampUpAutoStartHosts()` was removed
* `rampDownMinutesToWaitOnLogoff()` was removed
* `withPeakStartTime(models.Time)` was removed
* `offPeakMinutesToWaitOnDisconnect()` was removed
* `withRampDownStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `offPeakMinutesToWaitOnLogoff()` was removed
* `withRampDownMinutesToWaitOnDisconnect(java.lang.Integer)` was removed
* `withOffPeakStartTime(models.Time)` was removed
* `withPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `daysOfWeek()` was removed
* `peakActionOnDisconnect()` was removed
* `withDaysOfWeek(java.util.List)` was removed
* `withOffPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `rampDownMinutesToWaitOnDisconnect()` was removed
* `withRampUpStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `withOffPeakActionOnDisconnect(models.SessionHandlingOperation)` was removed
* `withRampDownMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `rampUpMinutesToWaitOnLogoff()` was removed
* `peakStartVMOnConnect()` was removed
* `withRampDownActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `offPeakActionOnLogoff()` was removed
* `withRampDownStartTime(models.Time)` was removed
* `rampUpActionOnLogoff()` was removed
* `rampDownStartVMOnConnect()` was removed
* `withPeakStartVMOnConnect(models.SetStartVMOnConnect)` was removed
* `peakStartTime()` was removed
* `withOffPeakMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `withRampUpMinutesToWaitOnLogoff(java.lang.Integer)` was removed
* `rampUpStartTime()` was removed
* `withRampUpStartTime(models.Time)` was removed
* `withRampUpActionOnLogoff(models.SessionHandlingOperation)` was removed
* `withRampUpAutoStartHosts(models.StartupBehavior)` was removed
* `rampDownActionOnDisconnect()` was removed
* `offPeakStartTime()` was removed
* `offPeakStartVMOnConnect()` was removed
* `rampDownActionOnLogoff()` was removed
* `withPeakMinutesToWaitOnDisconnect(java.lang.Integer)` was removed

#### `models.MsixPackageDependencies` was modified

* `validate()` was removed

#### `models.ScalingPlan` was modified

* `models.ResourceModelWithAllowedPropertySetSku sku()` -> `models.Sku sku()`
* `timeZone()` was removed
* `hostPoolType()` was removed
* `hostPoolReferences()` was removed
* `exclusionTag()` was removed
* `objectId()` was removed
* `models.ResourceModelWithAllowedPropertySetPlan plan()` -> `models.Plan plan()`
* `friendlyName()` was removed
* `description()` was removed
* `models.ResourceModelWithAllowedPropertySetIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `schedules()` was removed

#### `models.HostPools` was modified

* `models.RegistrationTokenList listRegistrationTokens(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens(java.lang.String,java.lang.String)`
* `listRegistrationTokensWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ScalingPlan$Definition` was modified

* `withPlan(models.ResourceModelWithAllowedPropertySetPlan)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withSchedules(java.util.List)` was removed
* `withSku(models.ResourceModelWithAllowedPropertySetSku)` was removed
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was removed
* `withDescription(java.lang.String)` was removed
* `withExclusionTag(java.lang.String)` was removed
* `withHostPoolType(models.ScalingHostPoolType)` was removed
* `withHostPoolReferences(java.util.List)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.Workspace$Definition` was modified

* `withDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withApplicationGroupReferences(java.util.List)` was removed
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was removed
* `withSku(models.ResourceModelWithAllowedPropertySetSku)` was removed
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed
* `withPlan(models.ResourceModelWithAllowedPropertySetPlan)` was removed

#### `models.ApplicationPatch` was modified

* `commandLineArguments()` was removed
* `friendlyName()` was removed
* `commandLineSetting()` was removed
* `description()` was removed
* `withFilePath(java.lang.String)` was removed
* `iconIndex()` was removed
* `validate()` was removed
* `msixPackageFamilyName()` was removed
* `withMsixPackageFamilyName(java.lang.String)` was removed
* `filePath()` was removed
* `withCommandLineArguments(java.lang.String)` was removed
* `msixPackageApplicationId()` was removed
* `tags()` was removed
* `iconPath()` was removed
* `withMsixPackageApplicationId(java.lang.String)` was removed
* `withApplicationType(models.RemoteApplicationType)` was removed
* `withCommandLineSetting(models.CommandLineSetting)` was removed
* `withDescription(java.lang.String)` was removed
* `withShowInPortal(java.lang.Boolean)` was removed
* `showInPortal()` was removed
* `withIconIndex(java.lang.Integer)` was removed
* `withIconPath(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `applicationType()` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.Plan` was modified

* `validate()` was removed

#### `models.RegistrationTokenMinimal` was modified

* `RegistrationTokenMinimal()` was removed
* `java.time.OffsetDateTime expirationTime()` -> `java.time.OffsetDateTime expirationTime()`
* `java.lang.String token()` -> `java.lang.String token()`
* `withToken(java.lang.String)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `validate()` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `withExpirationTime(java.time.OffsetDateTime)` was removed

#### `models.ApplicationGroup$Update` was modified

* `withShowInFeed(java.lang.Boolean)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

### Features Added

* `models.MsixPackageProperties` was added

* `models.VirtualMachineDiskType` was added

* `models.MarketplaceInfoPatchProperties` was added

* `models.ApplicationProperties` was added

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

* `models.ScalingPlanPersonalScheduleProperties` was added

* `models.KeyVaultCredentialsProperties` was added

* `models.ManagedServiceIdentity` was added

* `models.SessionHostManagementProvisioningStatusProperties` was added

* `models.KeyVaultCredentialsPatchProperties` was added

* `models.SessionHostConfiguration` was added

* `models.ApplicationGroupProperties` was added

* `models.DesktopProperties` was added

* `models.ScalingPlanPooledSchedulePatchProperties` was added

* `models.DesktopPatchProperties` was added

* `models.SecurityInfoPatchProperties` was added

* `models.ActiveSessionHostConfigurationProperties` was added

* `models.SessionHostProvisioningConfigurationPatchProperties` was added

* `models.HostPoolPatchProperties` was added

* `models.BootDiagnosticsInfoProperties` was added

* `models.ScalingPlanPooledScheduleProperties` was added

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

* `models.WorkspacePatchProperties` was added

* `models.ScalingPlanPatchProperties` was added

* `models.NetworkInfoPatchProperties` was added

* `models.SessionHostConfigurationProperties` was added

* `models.SessionHostManagement` was added

* `models.MarketplaceInfoProperties` was added

* `models.SecurityInfoProperties` was added

* `models.PrivateEndpointConnectionProperties` was added

* `models.PrivateLinkResourceProperties` was added

* `models.HostPoolUpdateAction` was added

* `models.WorkspaceProperties` was added

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

* `models.SessionHostProperties` was added

* `models.ManagementType` was added

* `models.SessionHostManagementPatchProperties` was added

* `models.SessionHostManagementUpdateStatusProperties` was added

* `models.HostPoolProvisioningAction` was added

* `models.RelayUDP` was added

* `models.SessionHost$Update` was added

* `models.ActiveDirectoryInfoPatchProperties` was added

* `models.DomainJoinType` was added

* `models.ScalingPlanPersonalSchedulePatchProperties` was added

* `models.UserSessionProperties` was added

* `models.DiffDiskPlacement` was added

* `models.HostPoolUpdateConfigurationPatchProperties` was added

* `models.ApplicationPatchProperties` was added

* `models.CustomInfoProperties` was added

* `models.SessionHost$UpdateStages` was added

* `models.SessionHostManagementProvisioningOperationStatus` was added

* `models.ScalingPlanProperties` was added

* `models.SessionHostManagements` was added

* `models.Type` was added

* `models.StartMenuItemProperties` was added

* `models.ControlSessionHostProvisionings` was added

* `models.CreateDeleteProperties` was added

* `models.FailedSessionHostCleanupPolicySHC` was added

* `models.SessionHostManagementProperties` was added

#### `models.PrivateEndpointConnectionWithSystemData` was modified

* `properties()` was added

#### `models.ScalingPlanPooledSchedule$Definition` was modified

* `withProperties(models.ScalingPlanPooledScheduleProperties)` was added

#### `models.UserSession` was modified

* `properties()` was added

#### `models.ScalingPlanPooledSchedule$Update` was modified

* `withProperties(models.ScalingPlanPooledSchedulePatchProperties)` was added

#### `models.HostPool$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withProperties(models.HostPoolPatchProperties)` was added

#### `models.ScalingPlan$Update` was modified

* `withProperties(models.ScalingPlanPatchProperties)` was added

#### `models.AppAttachPackagePatch` was modified

* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.StartMenuItem` was modified

* `properties()` was added

#### `models.AppAttachPackagePatchProperties` was modified

* `withPackageLookbackUrl(java.lang.String)` was added
* `customData()` was added
* `packageLookbackUrl()` was added
* `withCustomData(java.lang.String)` was added

#### `models.WorkspacePatch` was modified

* `properties()` was added
* `withProperties(models.WorkspacePatchProperties)` was added

#### `models.ScalingPlanPersonalSchedule` was modified

* `properties()` was added

#### `models.ScalingPlanPersonalSchedule$Definition` was modified

* `withProperties(models.ScalingPlanPersonalScheduleProperties)` was added

#### `models.MsixPackage$Update` was modified

* `withProperties(models.MsixPackagePatchProperties)` was added

#### `models.Workspace` was modified

* `properties()` was added

#### `models.MsixPackage$Definition` was modified

* `withProperties(models.MsixPackageProperties)` was added

#### `models.DesktopPatch` was modified

* `properties()` was added
* `withProperties(models.DesktopPatchProperties)` was added

#### `models.Application` was modified

* `properties()` was added

#### `models.ScalingSchedule` was modified

* `createDelete()` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added
* `scalingMethod()` was added
* `withScalingMethod(models.ScalingMethodType)` was added

#### `models.Application$Update` was modified

* `withProperties(models.ApplicationPatchProperties)` was added

#### `models.MsixPackageApplications` was modified

* `appUserModelID()` was added
* `withAppUserModelID(java.lang.String)` was added

#### `models.Application$Definition` was modified

* `withProperties(models.ApplicationProperties)` was added

#### `models.ApplicationGroup` was modified

* `properties()` was added

#### `models.HostPool` was modified

* `properties()` was added
* `listRegistrationTokens(com.azure.core.util.Context)` was added

#### `models.HostPoolPatch` was modified

* `withProperties(models.HostPoolPatchProperties)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `properties()` was added
* `identity()` was added

#### `models.PrivateEndpointConnections` was modified

* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added
* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added

#### `models.ScalingPlanPersonalSchedule$Update` was modified

* `withProperties(models.ScalingPlanPersonalSchedulePatchProperties)` was added

#### `models.PrivateLinkResource` was modified

* `properties()` was added

#### `models.HostPoolProperties` was modified

* `managedPrivateUDP()` was added
* `deploymentScope()` was added
* `publicUDP()` was added
* `oboTenantId()` was added
* `relayUDP()` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `directUDP()` was added
* `managementType()` was added

#### `DesktopVirtualizationManager` was modified

* `controlSessionHostUpdates()` was added
* `sessionHostManagements()` was added
* `sessionHostManagementUpdateStatuses()` was added
* `activeSessionHostConfigurations()` was added
* `sessionHostManagementProvisioningStatuses()` was added
* `initiateSessionHostUpdates()` was added
* `controlSessionHostProvisionings()` was added
* `sessionHostConfigurations()` was added

#### `models.PrivateEndpointConnection` was modified

* `properties()` was added

#### `models.ScalingHostPoolType` was modified

* `PERSONAL` was added

#### `models.AppAttachPackages` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String)` was added
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.AppAttachPackage$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.AppAttachPackageProperties` was modified

* `deploymentScope()` was added
* `withPackageLookbackUrl(java.lang.String)` was added
* `customData()` was added
* `withPackageOwnerName(java.lang.String)` was added
* `withDeploymentScope(models.DeploymentScope)` was added
* `withCustomData(java.lang.String)` was added
* `packageLookbackUrl()` was added
* `packageOwnerName()` was added

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `properties()` was added
* `withProperties(models.ScalingPlanPooledSchedulePatchProperties)` was added

#### `models.Desktop` was modified

* `properties()` was added

#### `models.SessionHostPatch` was modified

* `properties()` was added
* `withProperties(models.SessionHostPatchProperties)` was added

#### `models.MsixPackagePatch` was modified

* `properties()` was added
* `withProperties(models.MsixPackagePatchProperties)` was added

#### `models.ApplicationGroup$Definition` was modified

* `withSku(models.Sku)` was added
* `withProperties(models.ApplicationGroupProperties)` was added
* `withPlan(models.Plan)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MsixPackage` was modified

* `properties()` was added

#### `models.LoadBalancerType` was modified

* `MULTIPLE_PERSISTENT` was added

#### `models.ApplicationGroupPatch` was modified

* `properties()` was added
* `withProperties(models.ApplicationGroupPatchProperties)` was added

#### `models.HostPool$Definition` was modified

* `withProperties(fluent.models.HostPoolPropertiesInner)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withSku(models.Sku)` was added
* `withPlan(models.Plan)` was added

#### `models.ScalingPlanPooledSchedule` was modified

* `properties()` was added

#### `models.ScalingPlanPatch` was modified

* `withProperties(models.ScalingPlanPatchProperties)` was added
* `properties()` was added

#### `models.Workspace$Update` was modified

* `withProperties(models.WorkspacePatchProperties)` was added

#### `models.SessionHost` was modified

* `retryProvisioning()` was added
* `properties()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added
* `refresh(com.azure.core.util.Context)` was added
* `refresh()` was added
* `update()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties)` was added
* `resourceGroupName()` was added
* `retryProvisioningWithResponse(com.azure.core.util.Context)` was added

#### `models.ExpandMsixImage` was modified

* `properties()` was added

#### `models.SessionHosts` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties)` was added
* `retryProvisioningWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `retryProvisioning(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ScalingPlanPersonalSchedulePatch` was modified

* `withProperties(models.ScalingPlanPersonalSchedulePatchProperties)` was added
* `properties()` was added

#### `models.ScalingPlan` was modified

* `properties()` was added

#### `models.HostPools` was modified

* `listRegistrationTokens(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ScalingPlan$Definition` was modified

* `withProperties(models.ScalingPlanProperties)` was added
* `withPlan(models.Plan)` was added
* `withSku(models.Sku)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Workspace$Definition` was modified

* `withProperties(models.WorkspaceProperties)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withSku(models.Sku)` was added
* `withPlan(models.Plan)` was added

#### `models.ApplicationPatch` was modified

* `properties()` was added
* `withProperties(models.ApplicationPatchProperties)` was added

#### `models.RegistrationTokenMinimal` was modified

* `innerModel()` was added

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
