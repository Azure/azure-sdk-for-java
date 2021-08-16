# Release History

## 1.0.0-beta.3 (Unreleased)


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
