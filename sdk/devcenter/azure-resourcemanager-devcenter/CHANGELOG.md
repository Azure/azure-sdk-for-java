# Release History

## 1.1.0-beta.1 (2026-03-06)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package api-version 2026-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GalleryListResult` was removed

#### `models.NetworkConnectionListResult` was removed

#### `models.OutboundEnvironmentEndpointCollection` was removed

#### `models.EnvironmentDefinitionListResult` was removed

#### `models.SkuListResult` was removed

#### `models.ProjectEnvironmentTypeListResult` was removed

#### `models.AllowedEnvironmentTypeListResult` was removed

#### `models.HealthCheckStatusDetailsListResult` was removed

#### `models.CatalogListResult` was removed

#### `models.ListUsagesResult` was removed

#### `models.DevCenterListResult` was removed

#### `models.DevBoxDefinitionListResult` was removed

#### `models.EnvironmentTypeListResult` was removed

#### `models.AttachedNetworkListResult` was removed

#### `models.ImageVersionListResult` was removed

#### `models.OperationListResult` was removed

#### `models.TrackedResourceUpdate` was removed

#### `models.ProjectListResult` was removed

#### `models.ImageListResult` was removed

#### `models.PoolListResult` was removed

#### `models.ScheduleListResult` was removed

#### `models.RecommendedMachineConfiguration` was modified

* `RecommendedMachineConfiguration()` was changed to private access
* `validate()` was removed

#### `models.CatalogConflictError` was modified

* `CatalogConflictError()` was changed to private access
* `validate()` was removed

#### `models.DevCenter` was modified

* `provisioningState()` was removed
* `projectCatalogSettings()` was removed
* `displayName()` was removed
* `devCenterUri()` was removed
* `encryption()` was removed

#### `models.DevCenterUpdate` was modified

* `validate()` was removed
* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.CatalogErrorDetails` was modified

* `CatalogErrorDetails()` was changed to private access
* `validate()` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.CheckScopedNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.HealthCheck` was modified

* `HealthCheck()` was changed to private access
* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.Catalog` was modified

* `adoGit()` was removed
* `lastConnectionTime()` was removed
* `connectionState()` was removed
* `lastSyncTime()` was removed
* `tags()` was removed
* `syncType()` was removed
* `gitHub()` was removed
* `syncState()` was removed
* `provisioningState()` was removed
* `lastSyncStats()` was removed

#### `models.DevCenter$Definition` was modified

* `withEncryption(models.Encryption)` was removed
* `withProjectCatalogSettings(models.DevCenterProjectCatalogSettings)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.ProjectCatalogSettings` was modified

* `validate()` was removed

#### `models.ImageVersion` was modified

* `publishedDate()` was removed
* `provisioningState()` was removed
* `excludeFromLatest()` was removed
* `namePropertiesName()` was removed
* `osDiskImageSizeInGb()` was removed

#### `models.EnvironmentRole` was modified

* `validate()` was removed

#### `models.GitCatalog` was modified

* `validate()` was removed

#### `models.Capability` was modified

* `Capability()` was changed to private access
* `validate()` was removed

#### `models.CustomerManagedKeyEncryptionKeyIdentity` was modified

* `withDelegatedIdentityClientId(java.util.UUID)` was removed
* `java.util.UUID delegatedIdentityClientId()` -> `java.lang.String delegatedIdentityClientId()`
* `validate()` was removed

#### `models.HealthStatusDetail` was modified

* `HealthStatusDetail()` was changed to private access
* `validate()` was removed

#### `models.ScheduleUpdate` was modified

* `validate()` was removed

#### `models.Project` was modified

* `devCenterId()` was removed
* `description()` was removed
* `maxDevBoxesPerUser()` was removed
* `catalogSettings()` was removed
* `displayName()` was removed
* `devCenterUri()` was removed
* `provisioningState()` was removed

#### `models.EndpointDetail` was modified

* `EndpointDetail()` was changed to private access
* `validate()` was removed

#### `models.AttachedNetworkConnection` was modified

* `provisioningState()` was removed
* `networkConnectionLocation()` was removed
* `healthCheckStatus()` was removed
* `domainJoinType()` was removed
* `networkConnectionId()` was removed

#### `models.Gallery$Definition` was modified

* `withGalleryResourceId(java.lang.String)` was removed

#### `models.CustomerManagedKeyEncryption` was modified

* `validate()` was removed

#### `models.AttachedNetworkConnection$Definition` was modified

* `withNetworkConnectionId(java.lang.String)` was removed

#### `models.NetworkConnection$Definition` was modified

* `withDomainUsername(java.lang.String)` was removed
* `withOrganizationUnit(java.lang.String)` was removed
* `withDomainName(java.lang.String)` was removed
* `withNetworkingResourceGroupName(java.lang.String)` was removed
* `withDomainPassword(java.lang.String)` was removed
* `withDomainJoinType(models.DomainJoinType)` was removed
* `withSubnetId(java.lang.String)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.Pool` was modified

* `devBoxDefinitionName()` was removed
* `devBoxCount()` was removed
* `stopOnDisconnect()` was removed
* `healthStatusDetails()` was removed
* `singleSignOnStatus()` was removed
* `healthStatus()` was removed
* `managedVirtualNetworkRegions()` was removed
* `localAdministrator()` was removed
* `licenseType()` was removed
* `displayName()` was removed
* `networkConnectionName()` was removed
* `provisioningState()` was removed
* `virtualNetworkType()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.Encryption` was modified

* `validate()` was removed

#### `models.EnvironmentDefinition` was modified

* `parameters()` was removed
* `description()` was removed
* `validationStatus()` was removed
* `templatePath()` was removed

#### `models.Gallery` was modified

* `galleryResourceId()` was removed
* `provisioningState()` was removed

#### `models.PoolUpdate` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed
* `withLocation(java.lang.String)` was removed

#### `models.OperationStatusesGetHeaders` was modified

* `validate()` was removed
* `withLocation(java.lang.String)` was removed

#### `models.DevBoxDefinition` was modified

* `sku()` was removed
* `activeImageReference()` was removed
* `hibernateSupport()` was removed
* `imageReference()` was removed
* `imageValidationStatus()` was removed
* `provisioningState()` was removed
* `imageValidationErrorDetails()` was removed
* `validationStatus()` was removed
* `osStorageType()` was removed

#### `models.SyncStats` was modified

* `SyncStats()` was changed to private access
* `withSyncedCatalogItemTypes(java.util.List)` was removed
* `validate()` was removed

#### `models.Pool$Definition` was modified

* `withVirtualNetworkType(models.VirtualNetworkType)` was removed
* `withNetworkConnectionName(java.lang.String)` was removed
* `withLocalAdministrator(models.LocalAdminStatus)` was removed
* `withLicenseType(models.LicenseType)` was removed
* `withManagedVirtualNetworkRegions(java.util.List)` was removed
* `withStopOnDisconnect(models.StopOnDisconnectConfiguration)` was removed
* `withDevBoxDefinitionName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withSingleSignOnStatus(models.SingleSignOnStatus)` was removed

#### `models.CatalogSyncError` was modified

* `CatalogSyncError()` was changed to private access
* `validate()` was removed

#### `models.Project$Definition` was modified

* `withDevCenterId(java.lang.String)` was removed
* `withMaxDevBoxesPerUser(java.lang.Integer)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withCatalogSettings(models.ProjectCatalogSettings)` was removed

#### `models.EndpointDependency` was modified

* `EndpointDependency()` was changed to private access
* `validate()` was removed

#### `models.ProjectEnvironmentTypeUpdate` was modified

* `validate()` was removed

#### `models.NetworkConnectionUpdate` was modified

* `validate()` was removed
* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.ImageReference` was modified

* `validate()` was removed

#### `models.EnvironmentType` was modified

* `displayName()` was removed
* `provisioningState()` was removed

#### `models.DevBoxDefinitionUpdate` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed
* `withLocation(java.lang.String)` was removed

#### `models.Image` was modified

* `publisher()` was removed
* `provisioningState()` was removed
* `recommendedMachineConfiguration()` was removed
* `hibernateSupport()` was removed
* `offer()` was removed
* `description()` was removed
* `sku()` was removed

#### `models.ImageValidationErrorDetails` was modified

* `ImageValidationErrorDetails()` was changed to private access
* `withCode(java.lang.String)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `models.EnvironmentType$Definition` was modified

* `withDisplayName(java.lang.String)` was removed

#### `models.Schedules` was modified

* `getByIdWithResponse(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.Schedule$Definition` was modified

* `withRegion(java.lang.String)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withFrequency(models.ScheduledFrequency)` was removed
* `withTime(java.lang.String)` was removed
* `withRegion(com.azure.core.management.Region)` was removed
* `withTags(java.util.Map)` was removed
* `withTypePropertiesType(models.ScheduledType)` was removed
* `withState(models.ScheduleEnableStatus)` was removed

#### `models.UserRoleAssignmentValue` was modified

* `validate()` was removed

#### `models.EnvironmentDefinitionParameter` was modified

* `EnvironmentDefinitionParameter()` was changed to private access
* `validate()` was removed

#### `models.ProjectEnvironmentTypeUpdatePropertiesCreatorRoleAssignment` was modified

* `validate()` was removed

#### `models.Schedule` was modified

* `typePropertiesType()` was removed
* `location()` was removed
* `frequency()` was removed
* `provisioningState()` was removed
* `timeZone()` was removed
* `state()` was removed
* `time()` was removed
* `regionName()` was removed
* `tags()` was removed
* `region()` was removed

#### `models.DevBoxDefinition$Definition` was modified

* `withOsStorageType(java.lang.String)` was removed
* `withHibernateSupport(models.HibernateSupport)` was removed
* `withSku(models.Sku)` was removed
* `withImageReference(models.ImageReference)` was removed

#### `models.ResourceRange` was modified

* `ResourceRange()` was changed to private access
* `validate()` was removed

#### `models.EnvironmentTypeUpdate` was modified

* `validate()` was removed

#### `models.OperationStatusResult` was modified

* `models.OperationStatusResult withName(java.lang.String)` -> `models.OperationStatusResult withName(java.lang.String)`
* `models.OperationStatusResult withStatus(java.lang.String)` -> `models.OperationStatusResult withStatus(java.lang.String)`
* `validate()` was removed
* `models.OperationStatusResult withOperations(java.util.List)` -> `models.OperationStatusResult withOperations(java.util.List)`
* `models.OperationStatusResult withEndTime(java.time.OffsetDateTime)` -> `models.OperationStatusResult withEndTime(java.time.OffsetDateTime)`
* `withPercentComplete(java.lang.Float)` was removed
* `models.OperationStatusResult withError(com.azure.core.management.exception.ManagementError)` -> `models.OperationStatusResult withError(com.azure.core.management.exception.ManagementError)`
* `resourceId()` was removed
* `java.lang.Float percentComplete()` -> `java.lang.Double percentComplete()`
* `models.OperationStatusResult withStartTime(java.time.OffsetDateTime)` -> `models.OperationStatusResult withStartTime(java.time.OffsetDateTime)`
* `models.OperationStatusResult withId(java.lang.String)` -> `models.OperationStatusResult withId(java.lang.String)`

#### `models.CatalogUpdate` was modified

* `validate()` was removed

#### `models.Catalog$Definition` was modified

* `withSyncType(models.CatalogSyncType)` was removed
* `withAdoGit(models.GitCatalog)` was removed
* `withTags(java.util.Map)` was removed
* `withGitHub(models.GitCatalog)` was removed

#### `models.OperationStatus` was modified

* `java.lang.Float percentComplete()` -> `java.lang.Double percentComplete()`
* `resourceId()` was removed

#### `models.ProjectUpdate` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed
* `withLocation(java.lang.String)` was removed

#### `models.NetworkConnection` was modified

* `domainUsername()` was removed
* `domainName()` was removed
* `provisioningState()` was removed
* `healthCheckStatus()` was removed
* `subnetId()` was removed
* `networkingResourceGroupName()` was removed
* `organizationUnit()` was removed
* `domainJoinType()` was removed
* `domainPassword()` was removed

#### `models.StopOnDisconnectConfiguration` was modified

* `validate()` was removed

#### `models.DevCenterProjectCatalogSettings` was modified

* `validate()` was removed

#### `models.ProjectEnvironmentType$Definition` was modified

* `withCreatorRoleAssignment(models.ProjectEnvironmentTypeUpdatePropertiesCreatorRoleAssignment)` was removed
* `withUserRoleAssignments(java.util.Map)` was removed
* `withDeploymentTargetId(java.lang.String)` was removed
* `withStatus(models.EnvironmentTypeEnableStatus)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.AllowedEnvironmentType` was modified

* `displayName()` was removed
* `provisioningState()` was removed

#### `models.HealthCheckStatusDetails` was modified

* `healthChecks()` was removed
* `endDateTime()` was removed
* `startDateTime()` was removed

#### `models.ProjectEnvironmentType` was modified

* `status()` was removed
* `deploymentTargetId()` was removed
* `displayName()` was removed
* `creatorRoleAssignment()` was removed
* `userRoleAssignments()` was removed
* `environmentCount()` was removed
* `provisioningState()` was removed

#### `models.UsageName` was modified

* `UsageName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withLocalizedValue(java.lang.String)` was removed
* `validate()` was removed

### Features Added

* `models.StopOnNoConnectEnableStatus` was added

* `models.DefaultValue` was added

* `models.EnvironmentTypeProperties` was added

* `models.PoolDevBoxDefinition` was added

* `models.ProjectCatalogImageDefinitions` was added

* `models.PoolProperties` was added

* `models.CatalogAutoImageBuildEnableStatus` was added

* `models.CustomizationTaskInstance` was added

* `models.DevBoxTunnelEnableStatus` was added

* `models.ImageDefinition` was added

* `models.ImageDefinitionBuildProperties` was added

* `models.KeepAwakeEnableStatus` was added

* `models.DevCenterNetworkSettings` was added

* `models.ScheduleProperties` was added

* `models.PoolDevBoxDefinitionType` was added

* `models.ImageVersionProperties` was added

* `models.CustomizationTaskInputType` was added

* `models.ProjectCatalogImageDefinitionBuildsOperations` was added

* `models.CatalogProperties` was added

* `models.FeatureStatus` was added

* `models.AutoImageBuildStatus` was added

* `models.ProjectPolicyUpdate` was added

* `models.CustomizationTaskProperties` was added

* `models.ProjectPolicy$DefinitionStages` was added

* `models.DevCenterCatalogImageDefinitionBuilds` was added

* `models.InheritedProjectCatalogSettings` was added

* `models.ImageDefinitionBuildTask` was added

* `models.UserCustomizationsEnableStatus` was added

* `models.CmkIdentityType` was added

* `models.StopOnNoConnectConfiguration` was added

* `models.AllowedEnvironmentTypeProperties` was added

* `models.AssignedGroup` was added

* `models.HealthCheckStatusDetailsProperties` was added

* `models.FeatureStateModifiable` was added

* `models.DevCenterEncryptionSet$DefinitionStages` was added

* `models.DevboxDisksEncryptionEnableStatus` was added

* `models.ImageDefinitionReference` was added

* `models.ImageDefinitionBuild` was added

* `models.DevBoxScheduleDeleteSettings` was added

* `models.DevBoxProvisioningSettings` was added

* `models.ProjectPolicy$UpdateStages` was added

* `models.AssignedGroupScope` was added

* `models.DevCenterEncryptionSet$Definition` was added

* `models.ProjectPolicy$Update` was added

* `models.ActiveHoursConfiguration` was added

* `models.AttachedNetworkConnectionProperties` was added

* `models.ProjectNetworkSettings` was added

* `models.ProjectEnvironmentTypeProperties` was added

* `models.NetworkProperties` was added

* `models.CancelOnConnectEnableStatus` was added

* `models.WorkspaceStorageSettings` was added

* `models.InheritedSettingsForProject` was added

* `models.ImageDefinitionBuildTaskParametersItem` was added

* `models.DevCenterCatalogImageDefinitions` was added

* `models.EncryptionSetUpdate` was added

* `models.ServerlessGpuSessionsSettings` was added

* `models.ProjectProperties` was added

* `models.CustomizationTasks` was added

* `models.DevBoxDefinitionProperties` was added

* `models.ProjectPolicy` was added

* `models.EnvironmentDefinitionProperties` was added

* `models.LatestImageBuild` was added

* `models.AzureAiServicesSettings` was added

* `models.DevCenterEncryptionSet$UpdateStages` was added

* `models.MicrosoftHostedNetworkEnableStatus` was added

* `models.PolicyAction` was added

* `models.ImageDefinitionBuildStatus` was added

* `models.DefinitionParametersItem` was added

* `models.GalleryProperties` was added

* `models.ImageDefinitionBuildTaskGroup` was added

* `models.DevCenterCatalogImageDefinitionBuildsOperations` was added

* `models.ServerlessGpuSessionsMode` was added

* `models.DevCenterResourceType` was added

* `models.ResourcePolicy` was added

* `models.DevCenterProperties` was added

* `models.ProjectPolicy$Definition` was added

* `models.ImageDefinitionProperties` was added

* `models.ImageProperties` was added

* `models.DevBoxDeleteMode` was added

* `models.FeatureState` was added

* `models.DayOfWeek` was added

* `models.WorkspaceStorageMode` was added

* `models.ProjectCustomizationIdentityType` was added

* `models.CustomizationTask` was added

* `models.ProjectCustomizationManagedIdentity` was added

* `models.ProjectCustomizationSettings` was added

* `models.KeyEncryptionKeyIdentity` was added

* `models.AzureAiServicesMode` was added

* `models.EncryptionSets` was added

* `models.ProjectCatalogImageDefinitionBuilds` was added

* `models.AutoStartEnableStatus` was added

* `models.DevCenterEncryptionSet` was added

* `models.ProjectPolicyProperties` was added

* `models.ImageCreationErrorDetails` was added

* `models.ConfigurationPolicies` was added

* `models.DevCenterEncryptionSetProperties` was added

* `models.CustomizationTaskInput` was added

* `models.InstallAzureMonitorAgentEnableStatus` was added

* `models.ProjectPolicies` was added

* `models.DevCenterEncryptionSet$Update` was added

* `models.ArchitectureType` was added

* `models.ImageDefinitionBuildDetails` was added

#### `models.DevCenter$Update` was modified

* `withDevBoxProvisioningSettings(models.DevBoxProvisioningSettings)` was added
* `withNetworkSettings(models.DevCenterNetworkSettings)` was added

#### `models.DevCenter` was modified

* `properties()` was added

#### `models.DevCenterUpdate` was modified

* `location()` was added
* `withNetworkSettings(models.DevCenterNetworkSettings)` was added
* `withDevBoxProvisioningSettings(models.DevBoxProvisioningSettings)` was added
* `networkSettings()` was added
* `tags()` was added
* `devBoxProvisioningSettings()` was added

#### `models.Catalog$Update` was modified

* `withAutoImageBuildEnableStatus(models.CatalogAutoImageBuildEnableStatus)` was added

#### `models.Catalog` was modified

* `properties()` was added

#### `models.DevCenter$Definition` was modified

* `withProperties(models.DevCenterProperties)` was added

#### `models.ImageVersion` was modified

* `properties()` was added

#### `models.CustomerManagedKeyEncryptionKeyIdentity` was modified

* `withDelegatedIdentityClientId(java.lang.String)` was added

#### `models.Images` was modified

* `getByProjectWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByProject(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByProject(java.lang.String,java.lang.String)` was added
* `listByProject(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Project$Update` was modified

* `withCustomizationSettings(models.ProjectCustomizationSettings)` was added
* `withAssignedGroups(java.util.List)` was added
* `withAzureAiServicesSettings(models.AzureAiServicesSettings)` was added
* `withServerlessGpuSessionsSettings(models.ServerlessGpuSessionsSettings)` was added
* `withWorkspaceStorageSettings(models.WorkspaceStorageSettings)` was added
* `withDevBoxScheduleDeleteSettings(models.DevBoxScheduleDeleteSettings)` was added

#### `models.Project` was modified

* `getInheritedSettingsWithResponse(com.azure.core.util.Context)` was added
* `properties()` was added
* `getInheritedSettings()` was added

#### `models.Projects` was modified

* `getInheritedSettingsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getInheritedSettings(java.lang.String,java.lang.String)` was added

#### `models.Pool$Update` was modified

* `withDevBoxDefinition(models.PoolDevBoxDefinition)` was added
* `withActiveHoursConfiguration(models.ActiveHoursConfiguration)` was added
* `withDevBoxDefinitionType(models.PoolDevBoxDefinitionType)` was added
* `withStopOnNoConnect(models.StopOnNoConnectConfiguration)` was added
* `withDevBoxTunnelEnableStatus(models.DevBoxTunnelEnableStatus)` was added

#### `models.AttachedNetworkConnection` was modified

* `properties()` was added

#### `models.Gallery$Definition` was modified

* `withProperties(models.GalleryProperties)` was added

#### `models.AttachedNetworkConnection$Definition` was modified

* `withProperties(models.AttachedNetworkConnectionProperties)` was added

#### `models.NetworkConnection$Definition` was modified

* `withProperties(models.NetworkProperties)` was added

#### `models.Pool` was modified

* `properties()` was added

#### `DevCenterManager` was modified

* `customizationTasks()` was added
* `projectCatalogImageDefinitionBuilds()` was added
* `devCenterCatalogImageDefinitionBuilds()` was added
* `devCenterCatalogImageDefinitions()` was added
* `projectCatalogImageDefinitions()` was added
* `projectPolicies()` was added
* `projectCatalogImageDefinitionBuildsOperations()` was added
* `devCenterCatalogImageDefinitionBuildsOperations()` was added
* `encryptionSets()` was added

#### `models.EnvironmentDefinition` was modified

* `properties()` was added

#### `models.Gallery` was modified

* `properties()` was added

#### `models.PoolUpdate` was modified

* `location()` was added
* `withDevBoxTunnelEnableStatus(models.DevBoxTunnelEnableStatus)` was added
* `devBoxDefinitionType()` was added
* `withDevBoxDefinition(models.PoolDevBoxDefinition)` was added
* `withActiveHoursConfiguration(models.ActiveHoursConfiguration)` was added
* `withDevBoxDefinitionType(models.PoolDevBoxDefinitionType)` was added
* `devBoxDefinition()` was added
* `devBoxTunnelEnableStatus()` was added
* `stopOnNoConnect()` was added
* `activeHoursConfiguration()` was added
* `withStopOnNoConnect(models.StopOnNoConnectConfiguration)` was added
* `tags()` was added

#### `models.DevBoxDefinition` was modified

* `properties()` was added

#### `models.Pool$Definition` was modified

* `withProperties(models.PoolProperties)` was added

#### `models.Project$Definition` was modified

* `withProperties(models.ProjectProperties)` was added

#### `models.NetworkConnectionUpdate` was modified

* `tags()` was added
* `location()` was added

#### `models.EnvironmentType` was modified

* `properties()` was added

#### `models.Gallery$Update` was modified

* `withProperties(models.GalleryProperties)` was added

#### `models.DevBoxDefinitionUpdate` was modified

* `tags()` was added
* `location()` was added

#### `models.Image` was modified

* `properties()` was added

#### `models.EnvironmentType$Definition` was modified

* `withProperties(models.EnvironmentTypeProperties)` was added

#### `models.Schedules` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Schedule$Definition` was modified

* `withProperties(models.ScheduleProperties)` was added

#### `models.Schedule` was modified

* `properties()` was added

#### `models.DevBoxDefinition$Definition` was modified

* `withProperties(models.DevBoxDefinitionProperties)` was added

#### `models.CatalogUpdate` was modified

* `withAutoImageBuildEnableStatus(models.CatalogAutoImageBuildEnableStatus)` was added
* `autoImageBuildEnableStatus()` was added

#### `models.Catalog$Definition` was modified

* `withProperties(models.CatalogProperties)` was added

#### `models.ProjectUpdate` was modified

* `workspaceStorageSettings()` was added
* `customizationSettings()` was added
* `tags()` was added
* `withDevBoxScheduleDeleteSettings(models.DevBoxScheduleDeleteSettings)` was added
* `location()` was added
* `assignedGroups()` was added
* `withAzureAiServicesSettings(models.AzureAiServicesSettings)` was added
* `serverlessGpuSessionsSettings()` was added
* `withWorkspaceStorageSettings(models.WorkspaceStorageSettings)` was added
* `devBoxScheduleDeleteSettings()` was added
* `withAssignedGroups(java.util.List)` was added
* `withServerlessGpuSessionsSettings(models.ServerlessGpuSessionsSettings)` was added
* `azureAiServicesSettings()` was added
* `withCustomizationSettings(models.ProjectCustomizationSettings)` was added

#### `models.ImageVersions` was modified

* `listByProject(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByProject(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByProjectWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByProject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.Skus` was modified

* `listByProject(java.lang.String,java.lang.String)` was added
* `listByProject(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AttachedNetworkConnection$Update` was modified

* `withProperties(models.AttachedNetworkConnectionProperties)` was added

#### `models.NetworkConnection` was modified

* `properties()` was added

#### `models.ProjectEnvironmentType$Definition` was modified

* `withProperties(models.ProjectEnvironmentTypeProperties)` was added

#### `models.AllowedEnvironmentType` was modified

* `properties()` was added

#### `models.HealthCheckStatusDetails` was modified

* `properties()` was added

#### `models.ProjectEnvironmentType` was modified

* `properties()` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager DevCenter client library for Java.

## 1.0.0-beta.8 (2024-12-04)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0-beta.7 (2024-04-23)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.CustomizationTasks` was removed

* `models.CustomizationTaskInputType` was removed

* `models.CatalogDevBoxDefinitions` was removed

* `models.CustomizationTaskListResult` was removed

* `models.CustomizationTask` was removed

* `models.CustomizationTaskInput` was removed

#### `models.Catalog$DefinitionStages` was modified

* `withExistingDevcenter(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ScheduleUpdate` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.Catalog$Definition` was modified

* `withExistingDevcenter(java.lang.String,java.lang.String)` was removed

#### `DevCenterManager` was modified

* `catalogDevBoxDefinitions()` was removed
* `customizationTasks()` was removed

#### `models.Catalogs` was modified

* `deleteById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed
* `define(java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.CatalogItemSyncEnableStatus` was added

* `models.CheckScopedNameAvailabilities` was added

* `models.CheckScopedNameAvailabilityRequest` was added

* `models.ProjectCatalogSettings` was added

* `models.ProjectCatalogEnvironmentDefinitions` was added

* `models.ProjectCatalogs` was added

* `models.DevCenterProjectCatalogSettings` was added

* `models.OperationStatusesGetHeaders` was added

* `models.OperationStatusesGetResponse` was added

* `models.CatalogItemType` was added

#### `models.SyncStats` was modified

* `withSyncedCatalogItemTypes(java.util.List)` was added
* `syncedCatalogItemTypes()` was added

#### `models.DevCenter$Update` was modified

* `withProjectCatalogSettings(models.DevCenterProjectCatalogSettings)` was added

#### `models.DevCenter` was modified

* `projectCatalogSettings()` was added

#### `models.DevCenterUpdate` was modified

* `withProjectCatalogSettings(models.DevCenterProjectCatalogSettings)` was added
* `projectCatalogSettings()` was added

#### `models.Project$Definition` was modified

* `withCatalogSettings(models.ProjectCatalogSettings)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ProjectEnvironmentTypeUpdate` was modified

* `withDisplayName(java.lang.String)` was added
* `displayName()` was added

#### `models.Catalog` was modified

* `tags()` was added

#### `models.DevCenter$Definition` was modified

* `withProjectCatalogSettings(models.DevCenterProjectCatalogSettings)` was added

#### `models.Schedule$Definition` was modified

* `withRegion(com.azure.core.management.Region)` was added
* `withRegion(java.lang.String)` was added
* `withTags(java.util.Map)` was added

#### `models.Schedule` was modified

* `tags()` was added
* `regionName()` was added
* `location()` was added
* `region()` was added

#### `models.Project$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withCatalogSettings(models.ProjectCatalogSettings)` was added

#### `models.OperationStatusResult` was modified

* `resourceId()` was added

#### `models.ScheduleUpdate` was modified

* `tags()` was added
* `location()` was added

#### `models.Project` was modified

* `identity()` was added
* `catalogSettings()` was added

#### `models.Catalog$Definition` was modified

* `withExistingProject(java.lang.String,java.lang.String)` was added
* `withTags(java.util.Map)` was added

#### `models.ProjectUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added
* `withCatalogSettings(models.ProjectCatalogSettings)` was added
* `catalogSettings()` was added

#### `models.EnvironmentDefinitions` was modified

* `listByProjectCatalog(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByProjectCatalog(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `getByProjectCatalogWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByProjectCatalog(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ProjectEnvironmentType$Update` was modified

* `withDisplayName(java.lang.String)` was added

#### `DevCenterManager` was modified

* `checkScopedNameAvailabilities()` was added
* `projectCatalogs()` was added
* `projectCatalogEnvironmentDefinitions()` was added

#### `models.Catalogs` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,models.CatalogUpdate,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,models.CatalogUpdate)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.CatalogInner,com.azure.core.util.Context)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.CatalogInner)` was added

## 1.0.0-beta.6 (2023-10-23)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CatalogConflictError` was added

* `models.SyncStats` was added

* `models.CatalogSyncError` was added

* `models.ParameterType` was added

* `models.CustomizationTasks` was added

* `models.CatalogErrorDetails` was added

* `models.CustomizationTaskInputType` was added

* `models.CatalogResourceValidationErrorDetails` was added

* `models.IdentityType` was added

* `models.EnvironmentDefinitionParameter` was added

* `models.CustomerManagedKeyEncryptionKeyIdentity` was added

* `models.CatalogDevBoxDefinitions` was added

* `models.CustomizationTaskListResult` was added

* `models.EnvironmentDefinitionListResult` was added

* `models.SingleSignOnStatus` was added

* `models.CustomizationTask` was added

* `models.SyncErrorDetails` was added

* `models.VirtualNetworkType` was added

* `models.CustomerManagedKeyEncryption` was added

* `models.EnvironmentDefinitions` was added

* `models.CatalogSyncType` was added

* `models.CatalogResourceValidationStatus` was added

* `models.Encryption` was added

* `models.EnvironmentDefinition` was added

* `models.CustomizationTaskInput` was added

* `models.CatalogConnectionState` was added

#### `models.Usage` was modified

* `id()` was added

#### `models.DevBoxDefinition` was modified

* `validationStatus()` was added

#### `models.DevCenter$Update` was modified

* `withEncryption(models.Encryption)` was added
* `withDisplayName(java.lang.String)` was added

#### `models.DevCenter` was modified

* `displayName()` was added
* `encryption()` was added

#### `models.DevCenterUpdate` was modified

* `withEncryption(models.Encryption)` was added
* `displayName()` was added
* `withDisplayName(java.lang.String)` was added
* `encryption()` was added

#### `models.Pool$Definition` was modified

* `withDisplayName(java.lang.String)` was added
* `withVirtualNetworkType(models.VirtualNetworkType)` was added
* `withManagedVirtualNetworkRegions(java.util.List)` was added
* `withSingleSignOnStatus(models.SingleSignOnStatus)` was added

#### `models.Project$Definition` was modified

* `withDisplayName(java.lang.String)` was added

#### `models.Catalog$Update` was modified

* `withSyncType(models.CatalogSyncType)` was added

#### `models.EnvironmentType` was modified

* `displayName()` was added

#### `models.Catalog` was modified

* `connectionState()` was added
* `getSyncErrorDetails()` was added
* `connect()` was added
* `connect(com.azure.core.util.Context)` was added
* `lastConnectionTime()` was added
* `syncType()` was added
* `getSyncErrorDetailsWithResponse(com.azure.core.util.Context)` was added
* `lastSyncStats()` was added

#### `models.DevCenter$Definition` was modified

* `withDisplayName(java.lang.String)` was added
* `withEncryption(models.Encryption)` was added

#### `models.EnvironmentType$Definition` was modified

* `withDisplayName(java.lang.String)` was added

#### `models.Project$Update` was modified

* `withDisplayName(java.lang.String)` was added

#### `models.EnvironmentTypeUpdate` was modified

* `displayName()` was added
* `withDisplayName(java.lang.String)` was added

#### `models.CatalogUpdate` was modified

* `syncType()` was added
* `withSyncType(models.CatalogSyncType)` was added

#### `models.Project` was modified

* `displayName()` was added

#### `models.Catalog$Definition` was modified

* `withSyncType(models.CatalogSyncType)` was added

#### `models.Pool$Update` was modified

* `withSingleSignOnStatus(models.SingleSignOnStatus)` was added
* `withManagedVirtualNetworkRegions(java.util.List)` was added
* `withDisplayName(java.lang.String)` was added
* `withVirtualNetworkType(models.VirtualNetworkType)` was added

#### `models.ProjectUpdate` was modified

* `displayName()` was added
* `withDisplayName(java.lang.String)` was added

#### `models.Pool` was modified

* `managedVirtualNetworkRegions()` was added
* `singleSignOnStatus()` was added
* `displayName()` was added
* `devBoxCount()` was added
* `virtualNetworkType()` was added

#### `DevCenterManager` was modified

* `customizationTasks()` was added
* `environmentDefinitions()` was added
* `catalogDevBoxDefinitions()` was added

#### `models.ProjectEnvironmentType$Definition` was modified

* `withDisplayName(java.lang.String)` was added

#### `models.EnvironmentType$Update` was modified

* `withDisplayName(java.lang.String)` was added

#### `models.AllowedEnvironmentType` was modified

* `displayName()` was added

#### `models.Catalogs` was modified

* `connect(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getSyncErrorDetailsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getSyncErrorDetails(java.lang.String,java.lang.String,java.lang.String)` was added
* `connect(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.PoolUpdate` was modified

* `withDisplayName(java.lang.String)` was added
* `managedVirtualNetworkRegions()` was added
* `displayName()` was added
* `withManagedVirtualNetworkRegions(java.util.List)` was added
* `withSingleSignOnStatus(models.SingleSignOnStatus)` was added
* `singleSignOnStatus()` was added
* `withVirtualNetworkType(models.VirtualNetworkType)` was added
* `virtualNetworkType()` was added

#### `models.ProjectEnvironmentType` was modified

* `environmentCount()` was added
* `displayName()` was added

## 1.0.0-beta.5 (2023-05-17)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.EnableStatus` was removed

#### `models.ProjectEnvironmentTypeUpdate` was modified

* `models.EnableStatus status()` -> `models.EnvironmentTypeEnableStatus status()`
* `withStatus(models.EnableStatus)` was removed

#### `models.ImageReference` was modified

* `withPublisher(java.lang.String)` was removed
* `publisher()` was removed
* `sku()` was removed
* `withOffer(java.lang.String)` was removed
* `offer()` was removed
* `withSku(java.lang.String)` was removed

#### `models.Schedule$Definition` was modified

* `withState(models.EnableStatus)` was removed

#### `models.Schedule` was modified

* `models.EnableStatus state()` -> `models.ScheduleEnableStatus state()`

#### `models.Schedule$Update` was modified

* `withState(models.EnableStatus)` was removed

#### `models.ScheduleUpdate` was modified

* `withState(models.EnableStatus)` was removed
* `models.EnableStatus state()` -> `models.ScheduleEnableStatus state()`

#### `models.ProjectEnvironmentType$Update` was modified

* `withStatus(models.EnableStatus)` was removed

#### `models.ProjectEnvironmentType$Definition` was modified

* `withStatus(models.EnableStatus)` was removed

#### `models.ProjectEnvironmentType` was modified

* `models.EnableStatus status()` -> `models.EnvironmentTypeEnableStatus status()`

### Features Added

* `models.EndpointDependency` was added

* `models.StopOnDisconnectEnableStatus` was added

* `models.HealthStatus` was added

* `models.OutboundEnvironmentEndpointCollection` was added

* `models.HealthStatusDetail` was added

* `models.EnvironmentTypeEnableStatus` was added

* `models.OutboundEnvironmentEndpoint` was added

* `models.ScheduleEnableStatus` was added

* `models.EndpointDetail` was added

* `models.StopOnDisconnectConfiguration` was added

#### `models.Pool$Definition` was modified

* `withStopOnDisconnect(models.StopOnDisconnectConfiguration)` was added

#### `models.Project$Definition` was modified

* `withMaxDevBoxesPerUser(java.lang.Integer)` was added

#### `models.ProjectEnvironmentTypeUpdate` was modified

* `withStatus(models.EnvironmentTypeEnableStatus)` was added

#### `models.Image` was modified

* `hibernateSupport()` was added

#### `models.Schedule$Definition` was modified

* `withState(models.ScheduleEnableStatus)` was added

#### `models.Schedule$Update` was modified

* `withState(models.ScheduleEnableStatus)` was added

#### `models.Project$Update` was modified

* `withMaxDevBoxesPerUser(java.lang.Integer)` was added

#### `models.ScheduleUpdate` was modified

* `withState(models.ScheduleEnableStatus)` was added

#### `models.Project` was modified

* `maxDevBoxesPerUser()` was added

#### `models.Pool$Update` was modified

* `withStopOnDisconnect(models.StopOnDisconnectConfiguration)` was added

#### `models.ProjectUpdate` was modified

* `withMaxDevBoxesPerUser(java.lang.Integer)` was added
* `maxDevBoxesPerUser()` was added

#### `models.Pool` was modified

* `runHealthChecks(com.azure.core.util.Context)` was added
* `stopOnDisconnect()` was added
* `healthStatus()` was added
* `runHealthChecks()` was added
* `healthStatusDetails()` was added

#### `models.ProjectEnvironmentType$Update` was modified

* `withStatus(models.EnvironmentTypeEnableStatus)` was added

#### `models.Pools` was modified

* `runHealthChecks(java.lang.String,java.lang.String,java.lang.String)` was added
* `runHealthChecks(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ProjectEnvironmentType$Definition` was modified

* `withStatus(models.EnvironmentTypeEnableStatus)` was added

#### `models.PoolUpdate` was modified

* `withStopOnDisconnect(models.StopOnDisconnectConfiguration)` was added
* `stopOnDisconnect()` was added

#### `models.NetworkConnections` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

## 1.0.0-beta.4 (2022-11-24)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorAdditionalInfo` was removed

* `models.ErrorDetail` was removed

#### `models.DevBoxDefinition` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.DevCenter` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.EnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Image` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Catalog` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.ImageVersion` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Schedule` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.OperationStatusResult` was modified

* `models.ErrorDetail error()` -> `com.azure.core.management.exception.ManagementError error()`
* `withError(models.ErrorDetail)` was removed

#### `models.Project` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.OperationStatus` was modified

* `models.ErrorDetail error()` -> `com.azure.core.management.exception.ManagementError error()`

#### `models.AttachedNetworkConnection` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.NetworkConnection` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`
* `runHealthChecksWithResponse(com.azure.core.util.Context)` was removed

#### `models.Pool` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Gallery` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.AllowedEnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.NetworkConnections` was modified

* `runHealthChecksWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ProjectEnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

### Features Added

* `models.ProvisioningState` was added

* `models.CheckNameAvailabilityReason` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.HibernateSupport` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.CheckNameAvailabilities` was added

#### `models.DevBoxDefinition` was modified

* `hibernateSupport()` was added

#### `models.DevCenter` was modified

* `devCenterUri()` was added

#### `models.DevBoxDefinitionUpdate` was modified

* `withHibernateSupport(models.HibernateSupport)` was added
* `hibernateSupport()` was added

#### `models.DevBoxDefinition$Update` was modified

* `withHibernateSupport(models.HibernateSupport)` was added

#### `models.DevBoxDefinition$Definition` was modified

* `withHibernateSupport(models.HibernateSupport)` was added

#### `models.OperationStatusResult` was modified

* `withError(com.azure.core.management.exception.ManagementError)` was added

#### `models.Project` was modified

* `devCenterUri()` was added

#### `models.NetworkConnection` was modified

* `runHealthChecks(com.azure.core.util.Context)` was added

#### `DevCenterManager` was modified

* `checkNameAvailabilities()` was added

#### `models.NetworkConnections` was modified

* `runHealthChecks(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2022-11-18)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationStatusError` was removed

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate)` was removed

#### `models.Schedule$Update` was modified

* `withTypePropertiesType(models.ScheduledType)` was removed

#### `models.OperationStatus` was modified

* `models.OperationStatusError error()` -> `models.ErrorDetail error()`

#### `DevCenterManager` was modified

* `fluent.DevCenterClient serviceClient()` -> `fluent.DevCenterManagementClient serviceClient()`

### Features Added

* `models.ErrorAdditionalInfo` was added

* `models.CatalogSyncState` was added

* `models.OperationStatusResult` was added

* `models.ErrorDetail` was added

#### `models.Catalog` was modified

* `syncState()` was added

#### `models.Schedule$Update` was modified

* `withTags(java.util.Map)` was added
* `withType(models.ScheduledType)` was added

#### `models.OperationStatus` was modified

* `operations()` was added

## 1.0.0-beta.2 (2022-10-12)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)` was removed

### Features Added

* `models.ProjectAllowedEnvironmentTypes` was added

* `models.AllowedEnvironmentTypeListResult` was added

* `models.AllowedEnvironmentType` was added

#### `models.DevCenter` was modified

* `systemData()` was added

#### `models.Image` was modified

* `systemData()` was added

#### `models.OperationStatus` was modified

* `resourceId()` was added

#### `DevCenterManager` was modified

* `projectAllowedEnvironmentTypes()` was added

## 1.0.0-beta.1 (2022-08-19)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
