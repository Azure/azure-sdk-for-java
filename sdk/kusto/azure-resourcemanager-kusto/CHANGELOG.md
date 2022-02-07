# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
