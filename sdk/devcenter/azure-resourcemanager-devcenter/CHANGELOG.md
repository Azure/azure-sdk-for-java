# Release History

## 1.0.0-beta.8 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
