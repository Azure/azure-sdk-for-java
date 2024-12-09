# Release History

## 1.0.0-beta.2 (2024-12-09)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.TransportAuthentication` was removed

#### `implementation.models.PagedOperation` was removed

#### `models.DataPointsObservabilityMode` was removed

#### `models.EventsObservabilityMode` was removed

#### `models.OwnCertificate` was removed

#### `models.UserAuthentication` was removed

#### `models.UserAuthenticationMode` was removed

#### `models.Event` was modified

* `eventNotifier()` was removed
* `name()` was removed
* `withCapabilityId(java.lang.String)` was removed
* `capabilityId()` was removed
* `eventConfiguration()` was removed
* `withObservabilityMode(models.EventsObservabilityMode)` was removed
* `models.EventsObservabilityMode observabilityMode()` -> `models.EventObservabilityMode observabilityMode()`

#### `models.UsernamePasswordCredentials` was modified

* `usernameReference()` was removed
* `passwordReference()` was removed
* `withPasswordReference(java.lang.String)` was removed
* `withUsernameReference(java.lang.String)` was removed

#### `models.AssetProperties` was modified

* `withDefaultDataPointsConfiguration(java.lang.String)` was removed
* `withAssetEndpointProfileUri(java.lang.String)` was removed
* `java.lang.Integer version()` -> `java.lang.Long version()`
* `assetEndpointProfileUri()` was removed
* `defaultDataPointsConfiguration()` was removed
* `assetType()` was removed
* `withAssetType(java.lang.String)` was removed
* `dataPoints()` was removed
* `withDataPoints(java.util.List)` was removed

#### `models.X509Credentials` was modified

* `withCertificateReference(java.lang.String)` was removed
* `certificateReference()` was removed

#### `models.AssetStatus` was modified

* `java.lang.Integer version()` -> `java.lang.Long version()`

#### `models.OperationStatusResult` was modified

* `java.lang.Integer percentComplete()` -> `java.lang.Double percentComplete()`

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `withUserAuthentication(models.UserAuthentication)` was removed
* `transportAuthentication()` was removed
* `userAuthentication()` was removed
* `withTransportAuthentication(models.TransportAuthentication)` was removed

#### `models.DataPoint` was modified

* `name()` was removed
* `models.DataPointsObservabilityMode observabilityMode()` -> `models.DataPointObservabilityMode observabilityMode()`
* `withObservabilityMode(models.DataPointsObservabilityMode)` was removed
* `capabilityId()` was removed
* `dataSource()` was removed
* `dataPointConfiguration()` was removed
* `withCapabilityId(java.lang.String)` was removed

#### `models.AssetEndpointProfileProperties` was modified

* `transportAuthentication()` was removed
* `withUserAuthentication(models.UserAuthentication)` was removed
* `withTransportAuthentication(models.TransportAuthentication)` was removed
* `userAuthentication()` was removed

#### `models.AssetUpdateProperties` was modified

* `dataPoints()` was removed
* `defaultDataPointsConfiguration()` was removed
* `withDataPoints(java.util.List)` was removed
* `withAssetType(java.lang.String)` was removed
* `withDefaultDataPointsConfiguration(java.lang.String)` was removed
* `assetType()` was removed

### Features Added

* `models.AssetEndpointProfileStatus` was added

* `models.Dataset` was added

* `models.DataPointObservabilityMode` was added

* `models.AssetStatusDataset` was added

* `models.Authentication` was added

* `models.AssetEndpointProfileStatusError` was added

* `models.BillingContainer` was added

* `models.AssetStatusEvent` was added

* `models.AuthenticationMethod` was added

* `models.DataPointBase` was added

* `models.BillingContainerProperties` was added

* `models.EventBase` was added

* `implementation.models.OperationListResult` was added

* `models.BillingContainers` was added

* `models.MessageSchemaReference` was added

* `models.EventObservabilityMode` was added

* `models.TopicRetainType` was added

* `implementation.models.BillingContainerListResult` was added

* `models.Topic` was added

#### `models.Event` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withTopic(models.Topic)` was added
* `withObservabilityMode(models.EventObservabilityMode)` was added
* `withName(java.lang.String)` was added
* `withEventNotifier(java.lang.String)` was added
* `withEventConfiguration(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsernamePasswordCredentials` was modified

* `withPasswordSecretName(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `passwordSecretName()` was added
* `withUsernameSecretName(java.lang.String)` was added
* `usernameSecretName()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetProperties` was modified

* `withDiscoveredAssetRefs(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `defaultDatasetsConfiguration()` was added
* `withDefaultTopic(models.Topic)` was added
* `datasets()` was added
* `defaultTopic()` was added
* `assetEndpointProfileRef()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withAssetEndpointProfileRef(java.lang.String)` was added
* `withDatasets(java.util.List)` was added
* `discoveredAssetRefs()` was added
* `withDefaultDatasetsConfiguration(java.lang.String)` was added

#### `models.ExtendedLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.X509Credentials` was modified

* `withCertificateSecretName(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `certificateSecretName()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `implementation.models.AssetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetStatus` was modified

* `events()` was added
* `datasets()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `authentication()` was added
* `withEndpointProfileType(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAuthentication(models.Authentication)` was added
* `endpointProfileType()` was added

#### `models.AssetEndpointProfileUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetStatusError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `DeviceRegistryManager` was modified

* `billingContainers()` was added

#### `models.DataPoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDataSource(java.lang.String)` was added
* `withDataPointConfiguration(java.lang.String)` was added
* `withObservabilityMode(models.DataPointObservabilityMode)` was added
* `withName(java.lang.String)` was added

#### `models.AssetEndpointProfileProperties` was modified

* `withDiscoveredAssetEndpointProfileRef(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `discoveredAssetEndpointProfileRef()` was added
* `status()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAuthentication(models.Authentication)` was added
* `authentication()` was added
* `endpointProfileType()` was added
* `withEndpointProfileType(java.lang.String)` was added

#### `models.AssetUpdateProperties` was modified

* `withDefaultTopic(models.Topic)` was added
* `defaultTopic()` was added
* `withDefaultDatasetsConfiguration(java.lang.String)` was added
* `datasets()` was added
* `defaultDatasetsConfiguration()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDatasets(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `implementation.models.AssetEndpointProfileListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2024-04-26)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
