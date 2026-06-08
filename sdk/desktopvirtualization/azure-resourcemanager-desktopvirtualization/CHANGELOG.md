# Release History

## 1.3.0-beta.1 (2026-06-08)

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

#### `models.Identity` was removed

#### `models.ScalingPlanPooledScheduleList` was removed

#### `models.AppAttachPackageList` was removed

#### `models.PrivateLinkResourceListResult` was removed

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

* `withTags(java.util.Map)` was removed

#### `models.MsixPackageApplications` was modified

* `validate()` was removed

#### `models.ResourceModelWithAllowedPropertySetPlan` was modified

* `withProduct(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withPublisher(java.lang.String)` was removed
* `withPromotionCode(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed

#### `models.HostPool` was modified

* `listRegistrationTokensWithResponse(com.azure.core.util.Context)` was removed
* `models.RegistrationTokenList listRegistrationTokens()` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens()`

#### `models.HostPoolPatch` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnections` was modified

* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed
* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was removed
* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was removed

#### `models.ResourceProviderOperationDisplay` was modified

* `ResourceProviderOperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed

#### `models.SessionHostHealthCheckFailureDetails` was modified

* `SessionHostHealthCheckFailureDetails()` was changed to private access
* `validate()` was removed

#### `DesktopVirtualizationManager` was modified

* `fluent.DesktopVirtualizationApiClient serviceClient()` -> `fluent.DesktopVirtualizationManagementClient serviceClient()`

#### `models.PrivateEndpointConnection` was modified

* `PrivateEndpointConnection()` was changed to private access
* `provisioningState()` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was removed
* `groupIds()` was removed
* `withPrivateEndpoint(models.PrivateEndpoint)` was removed
* `privateLinkServiceConnectionState()` was removed
* `validate()` was removed
* `privateEndpoint()` was removed

#### `models.AppAttachPackages` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AgentUpdateProperties` was modified

* `validate()` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.SendMessage` was modified

* `validate()` was removed

#### `models.AppAttachPackageProperties` was modified

* `validate()` was removed

#### `models.AgentUpdatePatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `validate()` was removed

#### `models.SessionHostPatch` was modified

* `validate()` was removed

#### `models.MsixPackagePatch` was modified

* `validate()` was removed

#### `models.MsixImageUri` was modified

* `validate()` was removed

#### `models.ApplicationGroupPatch` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.MaintenanceWindowProperties` was modified

* `validate()` was removed

#### `models.MaintenanceWindowPatchProperties` was modified

* `validate()` was removed

#### `models.ScalingPlanPatch` was modified

* `validate()` was removed

#### `models.ExpandMsixImage` was modified

* `packageFamilyName()` was removed
* `displayName()` was removed
* `certificateExpiry()` was removed
* `imagePath()` was removed
* `packageFullName()` was removed
* `packageRelativePath()` was removed
* `isActive()` was removed
* `packageAlias()` was removed
* `isRegularRegistration()` was removed
* `version()` was removed
* `packageApplications()` was removed
* `packageDependencies()` was removed
* `lastUpdated()` was removed
* `packageName()` was removed
* `certificateName()` was removed

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

* `validate()` was removed

#### `models.ResourceModelWithAllowedPropertySetIdentity` was modified

* `withType(models.ResourceIdentityType)` was removed
* `validate()` was removed

#### `models.MsixPackageDependencies` was modified

* `validate()` was removed

#### `models.HostPools` was modified

* `listRegistrationTokensWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.RegistrationTokenList listRegistrationTokens(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listRegistrationTokens(java.lang.String,java.lang.String)`

#### `models.ApplicationPatch` was modified

* `tags()` was removed
* `withTags(java.util.Map)` was removed
* `validate()` was removed

#### `models.RegistrationTokenMinimal` was modified

* `RegistrationTokenMinimal()` was removed
* `java.time.OffsetDateTime expirationTime()` -> `java.time.OffsetDateTime expirationTime()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String token()` -> `java.lang.String token()`
* `validate()` was removed
* `withExpirationTime(java.time.OffsetDateTime)` was removed
* `withToken(java.lang.String)` was removed

#### `models.ResourceModelWithAllowedPropertySetSku` was modified

* `withCapacity(java.lang.Integer)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withTier(models.SkuTier)` was removed
* `withSize(java.lang.String)` was removed
* `withFamily(java.lang.String)` was removed

### Features Added

* `models.VirtualMachineDiskType` was added

* `models.MarketplaceInfoPatchProperties` was added

* `models.SessionHostManagementProvisioningStatus` was added

* `models.ActiveDirectoryInfoProperties` was added

* `models.DiskInfoProperties` was added

* `models.SessionHostConfigurations` was added

* `models.UserAssignedIdentity` was added

* `models.AzureActiveDirectoryInfoProperties` was added

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

* `models.SecurityInfoPatchProperties` was added

* `models.SessionHostProvisioningConfigurationPatchProperties` was added

* `models.BootDiagnosticsInfoProperties` was added

* `models.SessionHostManagementProvisioningOperationProgress` was added

* `models.ActiveSessionHostConfiguration` was added

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

* `models.NetworkInfoPatchProperties` was added

* `models.SessionHostManagement` was added

* `models.MarketplaceInfoProperties` was added

* `models.SecurityInfoProperties` was added

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

* `models.DiffDiskPlacement` was added

* `models.HostPoolUpdateConfigurationPatchProperties` was added

* `models.CustomInfoProperties` was added

* `models.SessionHost$UpdateStages` was added

* `models.SessionHostManagementProvisioningOperationStatus` was added

* `models.SessionHostManagements` was added

* `models.Type` was added

* `models.ControlSessionHostProvisionings` was added

* `models.CreateDeleteProperties` was added

* `models.FailedSessionHostCleanupPolicySHC` was added

#### `models.ScalingPlanPooledSchedule$Definition` was modified

* `withCreateDelete(models.CreateDeleteProperties)` was added
* `withScalingMethod(models.ScalingMethodType)` was added

#### `models.ScalingPlanPooledSchedule$Update` was modified

* `withScalingMethod(models.ScalingMethodType)` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added

#### `models.HostPool$Update` was modified

* `withDirectUDP(models.DirectUDP)` was added
* `withConditionalRdpProperty(java.lang.String)` was added
* `withRelayUDP(models.RelayUDP)` was added
* `withPublicUDP(models.PublicUDP)` was added
* `withManagedPrivateUDP(models.ManagedPrivateUDP)` was added
* `withAllowRDPShortPathWithPrivateLink(models.AllowRDPShortPathWithPrivateLink)` was added
* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was added

#### `models.AppAttachPackagePatch` was modified

* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.AppAttachPackagePatchProperties` was modified

* `customData()` was added
* `packageLookbackUrl()` was added
* `withCustomData(java.lang.String)` was added
* `withPackageLookbackUrl(java.lang.String)` was added

#### `models.Workspace` was modified

* `oboTenantId()` was added
* `deploymentScope()` was added

#### `models.ScalingSchedule` was modified

* `scalingMethod()` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added
* `createDelete()` was added
* `withScalingMethod(models.ScalingMethodType)` was added

#### `models.ResourceModelWithAllowedPropertySetPlan` was modified

* `version()` was added
* `promotionCode()` was added
* `name()` was added
* `product()` was added
* `publisher()` was added

#### `models.ApplicationGroup` was modified

* `deploymentScope()` was added
* `oboTenantId()` was added

#### `models.HostPool` was modified

* `publicUDP()` was added
* `managementType()` was added
* `directUDP()` was added
* `conditionalRdpProperty()` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `oboTenantId()` was added
* `listRegistrationTokens(com.azure.core.util.Context)` was added
* `managedPrivateUDP()` was added
* `deploymentScope()` was added
* `relayUDP()` was added

#### `models.HostPoolPatch` was modified

* `withIdentity(models.ResourceModelWithAllowedPropertySetIdentity)` was added
* `withDirectUDP(models.DirectUDP)` was added
* `withAllowRDPShortPathWithPrivateLink(models.AllowRDPShortPathWithPrivateLink)` was added
* `directUDP()` was added
* `relayUDP()` was added
* `withRelayUDP(models.RelayUDP)` was added
* `conditionalRdpProperty()` was added
* `withConditionalRdpProperty(java.lang.String)` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `withManagedPrivateUDP(models.ManagedPrivateUDP)` was added
* `publicUDP()` was added
* `withPublicUDP(models.PublicUDP)` was added
* `managedPrivateUDP()` was added
* `identity()` was added

#### `models.PrivateEndpointConnections` was modified

* `updateByWorkspace(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added
* `updateByWorkspaceWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByHostPoolWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner,com.azure.core.util.Context)` was added
* `updateByHostPool(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionWithSystemDataInner)` was added

#### `models.HostPoolProperties` was modified

* `directUDP()` was added
* `deploymentScope()` was added
* `allowRDPShortPathWithPrivateLink()` was added
* `oboTenantId()` was added
* `conditionalRdpProperty()` was added
* `managementType()` was added
* `managedPrivateUDP()` was added
* `relayUDP()` was added
* `publicUDP()` was added

#### `DesktopVirtualizationManager` was modified

* `controlSessionHostProvisionings()` was added
* `sessionHostManagements()` was added
* `sessionHostConfigurations()` was added
* `sessionHostManagementProvisioningStatuses()` was added
* `initiateSessionHostUpdates()` was added
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

#### `models.AppAttachPackageProperties` was modified

* `customData()` was added
* `deploymentScope()` was added
* `withCustomData(java.lang.String)` was added
* `withPackageOwnerName(java.lang.String)` was added
* `packageLookbackUrl()` was added
* `withPackageLookbackUrl(java.lang.String)` was added
* `packageOwnerName()` was added
* `withDeploymentScope(models.DeploymentScope)` was added

#### `models.ScalingPlanPooledSchedulePatch` was modified

* `scalingMethod()` was added
* `namePropertiesName()` was added
* `withScalingMethod(models.ScalingMethodType)` was added
* `withCreateDelete(models.CreateDeleteProperties)` was added
* `createDelete()` was added

#### `models.ApplicationGroup$Definition` was modified

* `withOboTenantId(java.lang.String)` was added

#### `models.LoadBalancerType` was modified

* `MULTIPLE_PERSISTENT` was added

#### `models.HostPool$Definition` was modified

* `withDirectUDP(models.DirectUDP)` was added
* `withAllowRDPShortPathWithPrivateLink(models.AllowRDPShortPathWithPrivateLink)` was added
* `withDeploymentScope(models.DeploymentScope)` was added
* `withOboTenantId(java.lang.String)` was added
* `withRelayUDP(models.RelayUDP)` was added
* `withConditionalRdpProperty(java.lang.String)` was added
* `withManagementType(models.ManagementType)` was added
* `withManagedPrivateUDP(models.ManagedPrivateUDP)` was added
* `withPublicUDP(models.PublicUDP)` was added

#### `models.ScalingPlanPooledSchedule` was modified

* `scalingMethod()` was added
* `createDelete()` was added
* `namePropertiesName()` was added

#### `models.SessionHost` was modified

* `retryProvisioning()` was added
* `update()` was added
* `activeSessions()` was added
* `resourceGroupName()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties)` was added
* `pendingSessions()` was added
* `lastSessionHostUpdateTime()` was added
* `listSingleSessionHostRegistrationTokens(models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added
* `disconnectedSessions()` was added
* `refresh(com.azure.core.util.Context)` was added
* `refresh()` was added
* `sessionHostConfiguration()` was added
* `retryProvisioningWithResponse(com.azure.core.util.Context)` was added

#### `models.ExpandMsixImage` was modified

* `properties()` was added

#### `models.SessionHosts` was modified

* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `retryProvisioning(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `retryProvisioningWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.Boolean,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSingleSessionHostRegistrationTokens(java.lang.String,java.lang.String,java.lang.String,models.ScopedRegistrationTokenProperties,com.azure.core.util.Context)` was added

#### `models.ResourceModelWithAllowedPropertySetIdentity` was modified

* `type()` was added
* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.Map)` was added
* `withType(models.ManagedServiceIdentityType)` was added

#### `models.HostPools` was modified

* `listRegistrationTokens(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Workspace$Definition` was modified

* `withDeploymentScope(models.DeploymentScope)` was added
* `withOboTenantId(java.lang.String)` was added

#### `models.RegistrationTokenMinimal` was modified

* `innerModel()` was added

#### `models.ResourceModelWithAllowedPropertySetSku` was modified

* `name()` was added
* `tier()` was added
* `size()` was added
* `family()` was added
* `capacity()` was added

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
