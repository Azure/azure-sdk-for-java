# Release History

## 1.0.0-beta.4 (Unreleased)


## 1.0.0-beta.3 (2021-07-09)

- Azure Resource Manager AppConfiguration client library for Java. This package contains Microsoft Azure SDK for AppConfiguration Management SDK.  Package tag package-2021-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ConfigurationStore$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.ConfigurationStoreUpdateParameters` was modified

* `publicNetworkAccess()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `AppConfigurationManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.2 (2021-05-14)

- Azure Resource Manager AppConfiguration client library for Java. This package contains Microsoft Azure SDK for AppConfiguration Management SDK.  Package tag package-2021-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ListKeyValueParameters` was removed

#### `models.ConfigurationStore$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

#### `models.ConfigurationStoreUpdateParameters` was modified

* `publicNetworkAccess()` was removed
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was removed

#### `models.ConfigurationStore` was modified

* `listKeyValue(models.ListKeyValueParameters)` was removed
* `listKeyValueWithResponse(models.ListKeyValueParameters,com.azure.core.util.Context)` was removed

#### `models.ConfigurationStores` was modified

* `listKeyValue(java.lang.String,java.lang.String,models.ListKeyValueParameters)` was removed
* `listKeyValueWithResponse(java.lang.String,java.lang.String,models.ListKeyValueParameters,com.azure.core.util.Context)` was removed

### New Feature

* `models.KeyValue$Definition` was added

* `models.KeyValue$UpdateStages` was added

* `models.ServiceSpecification` was added

* `models.OperationProperties` was added

* `models.KeyValues` was added

* `models.LogSpecification` was added

* `models.MetricDimension` was added

* `models.KeyValueListResult` was added

* `models.KeyValue$Update` was added

* `models.KeyValue$DefinitionStages` was added

* `models.MetricSpecification` was added

#### `models.ConfigurationStore$Update` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.ConfigurationStoreUpdateParameters` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `disableLocalAuth()` was added

#### `models.OperationDefinition` was modified

* `properties()` was added
* `isDataAction()` was added
* `origin()` was added

#### `models.ConfigurationStore` was modified

* `systemData()` was added
* `disableLocalAuth()` was added

#### `AppConfigurationManager` was modified

* `keyValues()` was added

#### `models.ConfigurationStore$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.KeyValue` was modified

* `refresh(com.azure.core.util.Context)` was added
* `type()` was added
* `refresh()` was added
* `name()` was added
* `id()` was added
* `update()` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager AppConfiguration client library for Java. This package contains Microsoft Azure SDK for AppConfiguration Management SDK.  Package tag package-2020-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
