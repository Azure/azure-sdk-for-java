# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-02-15)

- Azure Resource Manager AzureDigitalTwins client library for Java. This package contains Microsoft Azure SDK for AzureDigitalTwins Management SDK. Azure Digital Twins Client for managing DigitalTwinsInstance. Package tag package-2021-06-30-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionProperties` was removed

* `models.ConnectionPropertiesPrivateEndpoint` was removed

#### `models.ConnectionProperties` was modified

* `withPrivateLinkServiceConnectionState(models.ConnectionState)` was removed
* `models.ConnectionState privateLinkServiceConnectionState()` -> `models.ConnectionPropertiesPrivateLinkServiceConnectionState privateLinkServiceConnectionState()`

### Features Added

* `models.TimeSeriesDatabaseConnections` was added

* `models.TimeSeriesDatabaseConnection$DefinitionStages` was added

* `models.TimeSeriesDatabaseConnectionListResult` was added

* `models.TimeSeriesDatabaseConnection` was added

* `models.ConnectionType` was added

* `models.TimeSeriesDatabaseConnectionProperties` was added

* `models.TimeSeriesDatabaseConnection$Definition` was added

* `models.AzureDataExplorerConnectionProperties` was added

* `models.TimeSeriesDatabaseConnection$Update` was added

* `models.TimeSeriesDatabaseConnectionState` was added

* `models.TimeSeriesDatabaseConnection$UpdateStages` was added

#### `models.DigitalTwinsDescription` was modified

* `systemData()` was added

#### `models.ConnectionProperties` was modified

* `withPrivateLinkServiceConnectionState(models.ConnectionPropertiesPrivateLinkServiceConnectionState)` was added

#### `models.ExternalResource` was modified

* `systemData()` was added

#### `models.DigitalTwinsResource` was modified

* `systemData()` was added

#### `AzureDigitalTwinsManager` was modified

* `timeSeriesDatabaseConnections()` was added

#### `models.DigitalTwinsEndpointResource` was modified

* `systemData()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `AzureDigitalTwinsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Operation` was modified

* `properties()` was added

## 1.0.0-beta.1 (2021-03-02)

- Azure Resource Manager AzureDigitalTwins client library for Java. This package contains Microsoft Azure SDK for AzureDigitalTwins Management SDK. Azure Digital Twins Client for managing DigitalTwinsInstance. Package tag package-2020-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
