# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2025-01-24)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.FollowerDatabaseDefinitionGet` was added

* `models.FollowerDatabaseListResultGet` was added

* `models.ScriptLevel` was added

* `models.ZoneStatus` was added

* `models.CalloutType` was added

* `models.CalloutPolicyToRemove` was added

* `models.PrincipalPermissionsAction` was added

* `models.CalloutPoliciesList` was added

* `models.OutboundAccess` was added

* `models.CalloutPolicy` was added

#### `models.Clusters` was modified

* `listCalloutPolicies(java.lang.String,java.lang.String)` was added
* `listFollowerDatabasesGet(java.lang.String,java.lang.String)` was added
* `addCalloutPolicies(java.lang.String,java.lang.String,models.CalloutPoliciesList,com.azure.core.util.Context)` was added
* `addCalloutPolicies(java.lang.String,java.lang.String,models.CalloutPoliciesList)` was added
* `listFollowerDatabasesGet(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `removeCalloutPolicy(java.lang.String,java.lang.String,models.CalloutPolicyToRemove)` was added
* `removeCalloutPolicy(java.lang.String,java.lang.String,models.CalloutPolicyToRemove,com.azure.core.util.Context)` was added
* `listCalloutPolicies(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Script$Definition` was modified

* `withScriptLevel(models.ScriptLevel)` was added
* `withPrincipalPermissionsAction(models.PrincipalPermissionsAction)` was added

#### `models.SandboxCustomImage$Definition` was modified

* `withBaseImageName(java.lang.String)` was added

#### `models.Cluster` was modified

* `calloutPolicies()` was added
* `listCalloutPolicies()` was added
* `zoneStatus()` was added
* `removeCalloutPolicy(models.CalloutPolicyToRemove)` was added
* `addCalloutPolicies(models.CalloutPoliciesList,com.azure.core.util.Context)` was added
* `removeCalloutPolicy(models.CalloutPolicyToRemove,com.azure.core.util.Context)` was added
* `addCalloutPolicies(models.CalloutPoliciesList)` was added
* `listCalloutPolicies(com.azure.core.util.Context)` was added

#### `models.SandboxCustomImage` was modified

* `baseImageName()` was added

#### `models.Cluster$Definition` was modified

* `withCalloutPolicies(java.util.List)` was added

#### `models.Script` was modified

* `scriptLevel()` was added
* `principalPermissionsAction()` was added

#### `models.ClusterUpdate` was modified

* `withCalloutPolicies(java.util.List)` was added
* `zoneStatus()` was added
* `calloutPolicies()` was added

#### `models.SandboxCustomImage$Update` was modified

* `withBaseImageName(java.lang.String)` was added

#### `models.Cluster$Update` was modified

* `withCalloutPolicies(java.util.List)` was added

#### `models.Script$Update` was modified

* `withScriptLevel(models.ScriptLevel)` was added
* `withPrincipalPermissionsAction(models.PrincipalPermissionsAction)` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.DataConnection` was modified

* `kind()` was added

#### `models.IotHubDataConnection` was modified

* `id()` was added
* `type()` was added
* `kind()` was added
* `name()` was added

#### `models.EventHubDataConnection` was modified

* `name()` was added
* `id()` was added
* `kind()` was added
* `type()` was added

#### `models.EventGridDataConnection` was modified

* `type()` was added
* `name()` was added
* `kind()` was added
* `id()` was added

#### `models.ReadOnlyFollowingDatabase` was modified

* `type()` was added
* `kind()` was added
* `id()` was added
* `name()` was added

#### `models.ClusterUpdate` was modified

* `name()` was added
* `type()` was added
* `id()` was added

#### `models.ReadWriteDatabase` was modified

* `kind()` was added
* `id()` was added
* `type()` was added
* `name()` was added

#### `models.CosmosDbDataConnection` was modified

* `id()` was added
* `name()` was added
* `type()` was added
* `kind()` was added

#### `models.Database` was modified

* `kind()` was added

## 1.0.0 (2023-10-20)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SandboxCustomImages` was added

* `models.SandboxCustomImagesListResult` was added

* `models.SandboxCustomImage$DefinitionStages` was added

* `models.SandboxCustomImage$UpdateStages` was added

* `models.SandboxCustomImage$Definition` was added

* `models.SandboxCustomImage` was added

* `models.Language` was added

* `models.SandboxCustomImagesCheckNameRequest` was added

* `models.SandboxCustomImage$Update` was added

* `models.VnetState` was added

#### `models.VirtualNetworkConfiguration` was modified

* `state()` was added
* `withState(models.VnetState)` was added

#### `KustoManager` was modified

* `sandboxCustomImages()` was added

#### `models.LanguageExtension` was modified

* `languageExtensionCustomImageName()` was added

#### `models.EndpointDetail` was modified

* `withIpAddress(java.lang.String)` was added
* `ipAddress()` was added

#### `models.ClusterUpdate` was modified

* `withZones(java.util.List)` was added
* `zones()` was added

#### `models.Cluster$Update` was modified

* `withZones(java.util.List)` was added

## 1.0.0-beta.7 (2023-07-19)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationsResultsLocations` was modified

* `com.azure.core.http.rest.Response getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationsResultsLocationsGetResponse getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.Databases` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.MigrationClusterRole` was added

* `models.OperationsResultsLocationsGetHeaders` was added

* `models.DatabaseInviteFollowerRequest` was added

* `models.SuspensionDetails` was added

* `models.OperationsResultsLocationsGetResponse` was added

* `models.DatabaseOperations` was added

* `models.ClusterMigrateRequest` was added

* `models.MigrationClusterProperties` was added

* `models.DatabaseInviteFollowerResult` was added

#### `models.Clusters` was modified

* `migrate(java.lang.String,java.lang.String,models.ClusterMigrateRequest)` was added
* `migrate(java.lang.String,java.lang.String,models.ClusterMigrateRequest,com.azure.core.util.Context)` was added

#### `KustoManager` was modified

* `databaseOperations()` was added

#### `models.Cluster` was modified

* `migrate(models.ClusterMigrateRequest)` was added
* `migrate(models.ClusterMigrateRequest,com.azure.core.util.Context)` was added
* `migrationCluster()` was added

#### `models.ReadOnlyFollowingDatabase` was modified

* `suspensionDetails()` was added

#### `models.DatabaseListResult` was modified

* `nextLink()` was added
* `withNextLink(java.lang.String)` was added

#### `models.Databases` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ClusterUpdate` was modified

* `migrationCluster()` was added

#### `models.ReadWriteDatabase` was modified

* `keyVaultProperties()` was added
* `withKeyVaultProperties(models.KeyVaultProperties)` was added
* `suspensionDetails()` was added

## 1.0.0-beta.6 (2023-02-21)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Databases` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole)` was removed

### Features Added

* `models.LanguageExtensionImageName` was added

* `models.ResourceSkuZoneDetails` was added

* `models.ResourceSkuCapabilities` was added

* `models.CosmosDbDataConnection` was added

* `models.Skus` was added

#### `models.SkuLocationInfoItem` was modified

* `withZoneDetails(java.util.List)` was added
* `zoneDetails()` was added

#### `KustoManager` was modified

* `skus()` was added

#### `models.LanguageExtension` was modified

* `languageExtensionImageName()` was added

#### `models.Cluster$Definition` was modified

* `withLanguageExtensions(models.LanguageExtensionsList)` was added

#### `models.ClusterUpdate` was modified

* `withLanguageExtensions(models.LanguageExtensionsList)` was added

#### `models.TableLevelSharingProperties` was modified

* `functionsToInclude()` was added
* `withFunctionsToExclude(java.util.List)` was added
* `functionsToExclude()` was added
* `withFunctionsToInclude(java.util.List)` was added

#### `models.Cluster$Update` was modified

* `withLanguageExtensions(models.LanguageExtensionsList)` was added

## 1.0.0-beta.5 (2022-09-19)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ClustersUpdateResponse` was removed

* `models.DatabasesUpdateResponse` was removed

* `models.ManagedPrivateEndpointsUpdateResponse` was removed

* `models.ManagedPrivateEndpointsUpdateHeaders` was removed

* `models.DataConnectionsUpdateResponse` was removed

* `models.DatabasesUpdateHeaders` was removed

* `models.ScriptsUpdateResponse` was removed

* `models.ScriptsUpdateHeaders` was removed

* `models.ClustersUpdateHeaders` was removed

* `models.DataConnectionsUpdateHeaders` was removed

#### `models.Databases` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,com.azure.core.util.Context)` was removed

### Features Added

* `models.CallerRole` was added

* `models.DatabaseShareOrigin` was added

#### `models.IotHubDataConnection` was modified

* `withRetrievalStartDate(java.time.OffsetDateTime)` was added
* `retrievalStartDate()` was added

#### `KustoManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.FollowerDatabaseDefinition` was modified

* `databaseShareOrigin()` was added
* `tableLevelSharingProperties()` was added

#### `models.EventHubDataConnection` was modified

* `retrievalStartDate()` was added
* `withRetrievalStartDate(java.time.OffsetDateTime)` was added

#### `models.AttachedDatabaseConfiguration$Update` was modified

* `withDatabaseNameOverride(java.lang.String)` was added
* `withDatabaseNamePrefix(java.lang.String)` was added

#### `models.DatabasePrincipalAssignment` was modified

* `resourceGroupName()` was added

#### `models.Cluster` was modified

* `resourceGroupName()` was added

#### `models.ReadOnlyFollowingDatabase` was modified

* `originalDatabaseName()` was added
* `tableLevelSharingProperties()` was added
* `databaseShareOrigin()` was added

#### `models.AttachedDatabaseConfiguration` was modified

* `databaseNameOverride()` was added
* `resourceGroupName()` was added
* `databaseNamePrefix()` was added

#### `models.ClusterPrincipalAssignment` was modified

* `resourceGroupName()` was added

#### `models.Script` was modified

* `resourceGroupName()` was added

#### `models.Databases` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.DatabaseInner,models.CallerRole,com.azure.core.util.Context)` was added

#### `models.AttachedDatabaseConfiguration$Definition` was modified

* `withDatabaseNamePrefix(java.lang.String)` was added
* `withDatabaseNameOverride(java.lang.String)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.ManagedPrivateEndpoint` was modified

* `resourceGroupName()` was added

#### `KustoManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.4 (2022-02-22)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2022-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ClustersUpdateResponse` was added

* `models.DatabasesUpdateResponse` was added

* `models.ManagedPrivateEndpointsUpdateResponse` was added

* `models.ManagedPrivateEndpointsUpdateHeaders` was added

* `models.PublicIpType` was added

* `models.DataConnectionsUpdateResponse` was added

* `models.DatabasesUpdateHeaders` was added

* `models.OperationsResultsLocations` was added

* `models.DatabaseRouting` was added

* `models.ScriptsUpdateResponse` was added

* `models.ScriptsUpdateHeaders` was added

* `models.ClustersUpdateHeaders` was added

* `models.DataConnectionsUpdateHeaders` was added

#### `models.IotHubDataConnection` was modified

* `databaseRouting()` was added
* `withDatabaseRouting(models.DatabaseRouting)` was added

#### `KustoManager` was modified

* `operationsResultsLocations()` was added

#### `models.EventHubDataConnection` was modified

* `databaseRouting()` was added
* `managedIdentityObjectId()` was added
* `withDatabaseRouting(models.DatabaseRouting)` was added

#### `models.Script$Definition` was modified

* `withScriptContent(java.lang.String)` was added

#### `models.EventGridDataConnection` was modified

* `databaseRouting()` was added
* `withEventGridResourceId(java.lang.String)` was added
* `withManagedIdentityResourceId(java.lang.String)` was added
* `eventGridResourceId()` was added
* `managedIdentityResourceId()` was added
* `withDatabaseRouting(models.DatabaseRouting)` was added
* `managedIdentityObjectId()` was added

#### `models.DatabasePrincipalAssignment` was modified

* `aadObjectId()` was added

#### `models.Cluster` was modified

* `virtualClusterGraduationProperties()` was added
* `publicIpType()` was added
* `privateEndpointConnections()` was added

#### `models.OperationResult` was modified

* `provisioningState()` was added

#### `models.Cluster$Definition` was modified

* `withVirtualClusterGraduationProperties(java.lang.String)` was added
* `withPublicIpType(models.PublicIpType)` was added

#### `models.ClusterPrincipalAssignment` was modified

* `aadObjectId()` was added

#### `models.Script` was modified

* `scriptContent()` was added

#### `models.ClusterUpdate` was modified

* `withPublicIpType(models.PublicIpType)` was added
* `virtualClusterGraduationProperties()` was added
* `publicIpType()` was added
* `privateEndpointConnections()` was added
* `withVirtualClusterGraduationProperties(java.lang.String)` was added

#### `models.Cluster$Update` was modified

* `withPublicIpType(models.PublicIpType)` was added

## 1.0.0-beta.3 (2021-09-13)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2021-08-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Script$Update` was modified

* `withScriptUrlSasToken(java.lang.String)` was removed

### Features Added

* `models.PrivateEndpointConnection$Update` was added

* `models.PrivateEndpointConnections` was added

* `models.ManagedPrivateEndpoint$Update` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateLinkResource` was added

* `models.AttachedDatabaseConfigurationsCheckNameRequest` was added

* `models.ManagedPrivateEndpoint$Definition` was added

* `models.PrivateEndpointProperty` was added

* `models.AcceptedAudiences` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateLinkResources` was added

* `models.OutboundNetworkDependenciesEndpoint` was added

* `models.EndpointDependency` was added

* `models.ManagedPrivateEndpoint$DefinitionStages` was added

* `models.EndpointDetail` was added

* `models.PrivateLinkServiceConnectionStateProperty` was added

* `models.ManagedPrivateEndpoints` was added

* `models.ManagedPrivateEndpoint$UpdateStages` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.OutboundNetworkDependenciesEndpointListResult` was added

* `models.ClusterNetworkAccessFlag` was added

* `models.ManagedPrivateEndpointsCheckNameRequest` was added

* `models.PrivateEndpointConnection` was added

* `models.ManagedPrivateEndpoint` was added

* `models.ManagedPrivateEndpointListResult` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

#### `models.AttachedDatabaseConfigurations` was modified

* `checkNameAvailability(java.lang.String,java.lang.String,models.AttachedDatabaseConfigurationsCheckNameRequest)` was added
* `checkNameAvailabilityWithResponse(java.lang.String,java.lang.String,models.AttachedDatabaseConfigurationsCheckNameRequest,com.azure.core.util.Context)` was added

#### `models.Clusters` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String)` was added

#### `KustoManager` was modified

* `privateLinkResources()` was added
* `managedPrivateEndpoints()` was added
* `privateEndpointConnections()` was added

#### `models.Cluster` was modified

* `enableAutoStop()` was added
* `publicNetworkAccess()` was added
* `restrictOutboundNetworkAccess()` was added
* `systemData()` was added
* `acceptedAudiences()` was added
* `allowedFqdnList()` was added
* `allowedIpRangeList()` was added

#### `models.Cluster$Definition` was modified

* `withEnableAutoStop(java.lang.Boolean)` was added
* `withAllowedIpRangeList(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withRestrictOutboundNetworkAccess(models.ClusterNetworkAccessFlag)` was added
* `withAllowedFqdnList(java.util.List)` was added
* `withAcceptedAudiences(java.util.List)` was added

#### `models.ClusterUpdate` was modified

* `acceptedAudiences()` was added
* `withAllowedFqdnList(java.util.List)` was added
* `withEnableAutoStop(java.lang.Boolean)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `enableAutoStop()` was added
* `withAcceptedAudiences(java.util.List)` was added
* `restrictOutboundNetworkAccess()` was added
* `withRestrictOutboundNetworkAccess(models.ClusterNetworkAccessFlag)` was added
* `allowedIpRangeList()` was added
* `publicNetworkAccess()` was added
* `allowedFqdnList()` was added
* `withAllowedIpRangeList(java.util.List)` was added

#### `models.Cluster$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withAllowedFqdnList(java.util.List)` was added
* `withRestrictOutboundNetworkAccess(models.ClusterNetworkAccessFlag)` was added
* `withAllowedIpRangeList(java.util.List)` was added
* `withAcceptedAudiences(java.util.List)` was added
* `withEnableAutoStop(java.lang.Boolean)` was added

#### `KustoManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.2 (2021-04-19)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

* `models.ScriptListResult` was added

* `models.OperationResult` was added

* `models.Script$DefinitionStages` was added

* `models.Script` was added

* `models.TableLevelSharingProperties` was added

* `models.ScriptCheckNameRequest` was added

* `models.Script$Update` was added

* `models.Scripts` was added

* `models.Script$Definition` was added

* `models.Status` was added

* `models.Script$UpdateStages` was added

* `models.OperationsResults` was added

#### `models.AttachedDatabaseConfiguration` was modified

* `tableLevelSharingProperties()` was added

#### `models.Cluster$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `KustoManager` was modified

* `operationsResults()` was added
* `scripts()` was added

#### `models.EventHubDataConnection` was modified

* `withManagedIdentityResourceId(java.lang.String)` was added
* `managedIdentityResourceId()` was added

#### `models.Cluster$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.AttachedDatabaseConfiguration$Definition` was modified

* `withTableLevelSharingProperties(models.TableLevelSharingProperties)` was added

#### `models.AttachedDatabaseConfiguration$Update` was modified

* `withTableLevelSharingProperties(models.TableLevelSharingProperties)` was added

#### `models.Cluster` was modified

* `addLanguageExtensions(models.LanguageExtensionsList)` was added
* `listLanguageExtensions(com.azure.core.util.Context)` was added
* `etag()` was added
* `stop(com.azure.core.util.Context)` was added
* `listLanguageExtensions()` was added
* `listFollowerDatabases()` was added
* `diagnoseVirtualNetwork()` was added
* `detachFollowerDatabases(fluent.models.FollowerDatabaseDefinitionInner)` was added
* `start()` was added
* `removeLanguageExtensions(models.LanguageExtensionsList)` was added
* `detachFollowerDatabases(fluent.models.FollowerDatabaseDefinitionInner,com.azure.core.util.Context)` was added
* `diagnoseVirtualNetwork(com.azure.core.util.Context)` was added
* `stop()` was added
* `removeLanguageExtensions(models.LanguageExtensionsList,com.azure.core.util.Context)` was added
* `listFollowerDatabases(com.azure.core.util.Context)` was added
* `start(com.azure.core.util.Context)` was added
* `addLanguageExtensions(models.LanguageExtensionsList,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager Kusto client library for Java. This package contains Microsoft Azure SDK for Kusto Management SDK. The Azure Kusto management API provides a RESTful set of web services that interact with Azure Kusto services to manage your clusters and databases. The API enables you to create, update, and delete clusters and databases. Package tag package-2020-09-18. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
