# Release History

## 1.0.0-beta.2 (2024-12-10)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.EventsObservabilityMode` was removed

#### `models.OwnCertificate` was removed

#### `models.UserAuthentication` was removed

#### `models.UserAuthenticationMode` was removed

#### `models.TransportAuthentication` was removed

#### `implementation.models.PagedOperation` was removed

#### `models.DataPointsObservabilityMode` was removed

#### `models.Event` was modified

* `eventNotifier()` was removed
* `models.EventsObservabilityMode observabilityMode()` -> `models.EventObservabilityMode observabilityMode()`
* `capabilityId()` was removed
* `eventConfiguration()` was removed
* `name()` was removed
* `withCapabilityId(java.lang.String)` was removed
* `withObservabilityMode(models.EventsObservabilityMode)` was removed

#### `models.AssetProperties` was modified

* `withDefaultDataPointsConfiguration(java.lang.String)` was removed
* `java.lang.Integer version()` -> `java.lang.Long version()`
* `defaultDataPointsConfiguration()` was removed
* `withDataPoints(java.util.List)` was removed
* `assetType()` was removed
* `withAssetType(java.lang.String)` was removed
* `dataPoints()` was removed
* `withAssetEndpointProfileUri(java.lang.String)` was removed
* `assetEndpointProfileUri()` was removed

#### `models.X509Credentials` was modified

* `certificateReference()` was removed
* `withCertificateReference(java.lang.String)` was removed

#### `models.AssetStatus` was modified

* `java.lang.Integer version()` -> `java.lang.Long version()`

#### `models.OperationStatusResult` was modified

* `java.lang.Integer percentComplete()` -> `java.lang.Double percentComplete()`

#### `DeviceRegistryManager` was modified

* `fluent.DeviceRegistryClient serviceClient()` -> `fluent.DeviceRegistryManagementClient serviceClient()`

#### `models.AssetEndpointProfileProperties` was modified

* `transportAuthentication()` was removed
* `userAuthentication()` was removed
* `withUserAuthentication(models.UserAuthentication)` was removed
* `withTransportAuthentication(models.TransportAuthentication)` was removed

#### `models.AssetUpdateProperties` was modified

* `withDataPoints(java.util.List)` was removed
* `defaultDataPointsConfiguration()` was removed
* `assetType()` was removed
* `withDefaultDataPointsConfiguration(java.lang.String)` was removed
* `dataPoints()` was removed
* `withAssetType(java.lang.String)` was removed

#### `models.UsernamePasswordCredentials` was modified

* `withPasswordReference(java.lang.String)` was removed
* `usernameReference()` was removed
* `withUsernameReference(java.lang.String)` was removed
* `passwordReference()` was removed

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `userAuthentication()` was removed
* `withUserAuthentication(models.UserAuthentication)` was removed
* `transportAuthentication()` was removed
* `withTransportAuthentication(models.TransportAuthentication)` was removed

#### `models.DataPoint` was modified

* `capabilityId()` was removed
* `dataSource()` was removed
* `withCapabilityId(java.lang.String)` was removed
* `models.DataPointsObservabilityMode observabilityMode()` -> `models.DataPointObservabilityMode observabilityMode()`
* `dataPointConfiguration()` was removed
* `withObservabilityMode(models.DataPointsObservabilityMode)` was removed
* `name()` was removed

### Features Added

* `models.AssetEndpointProfileStatus` was added

* `models.DiscoveredEvent` was added

* `models.Dataset` was added

* `models.Schema` was added

* `models.DataPointObservabilityMode` was added

* `models.Authentication` was added

* `models.DiscoveredAsset$Definition` was added

* `models.SchemaProperties` was added

* `models.DiscoveredAsset$DefinitionStages` was added

* `models.DiscoveredAssets` was added

* `implementation.models.SchemaListResult` was added

* `models.SystemAssignedServiceIdentityType` was added

* `models.SchemaRegistry$Definition` was added

* `models.AuthenticationMethod` was added

* `implementation.models.DiscoveredAssetEndpointProfileListResult` was added

* `models.DataPointBase` was added

* `models.EventBase` was added

* `implementation.models.OperationListResult` was added

* `models.DiscoveredAssetEndpointProfile$Definition` was added

* `models.SchemaVersion` was added

* `models.DiscoveredDataPoint` was added

* `models.Schema$DefinitionStages` was added

* `models.Format` was added

* `models.EventObservabilityMode` was added

* `models.TopicRetainType` was added

* `models.SchemaRegistryUpdateProperties` was added

* `models.DiscoveredAssetEndpointProfile` was added

* `models.SystemAssignedServiceIdentity` was added

* `models.SchemaRegistryProperties` was added

* `models.DiscoveredAssetEndpointProfile$DefinitionStages` was added

* `models.DiscoveredAssetEndpointProfiles` was added

* `models.SchemaVersion$DefinitionStages` was added

* `models.SchemaVersionProperties` was added

* `models.AssetStatusDataset` was added

* `implementation.models.SchemaVersionListResult` was added

* `models.DiscoveredAssetProperties` was added

* `models.DiscoveredAssetEndpointProfile$UpdateStages` was added

* `models.DiscoveredAssetEndpointProfileProperties` was added

* `models.AssetEndpointProfileStatusError` was added

* `models.SchemaRegistry$UpdateStages` was added

* `models.DiscoveredAssetEndpointProfileUpdateProperties` was added

* `models.BillingContainer` was added

* `models.AssetStatusEvent` was added

* `models.DiscoveredAsset$Update` was added

* `models.BillingContainerProperties` was added

* `implementation.models.SchemaRegistryListResult` was added

* `models.DiscoveredAssetUpdateProperties` was added

* `models.SchemaRegistry$Update` was added

* `models.SchemaType` was added

* `models.SchemaRegistries` was added

* `models.SchemaRegistryUpdate` was added

* `models.DiscoveredAsset$UpdateStages` was added

* `models.Schema$Definition` was added

* `models.SchemaVersion$Definition` was added

* `models.Schemas` was added

* `models.BillingContainers` was added

* `models.DiscoveredAssetEndpointProfile$Update` was added

* `models.SchemaRegistry$DefinitionStages` was added

* `models.SchemaVersions` was added

* `models.MessageSchemaReference` was added

* `models.DiscoveredAssetUpdate` was added

* `models.DiscoveredAssetEndpointProfileUpdate` was added

* `models.SchemaRegistry` was added

* `implementation.models.BillingContainerListResult` was added

* `implementation.models.DiscoveredAssetListResult` was added

* `models.DiscoveredDataset` was added

* `models.Topic` was added

* `models.DiscoveredAsset` was added

#### `models.Event` was modified

* `withEventConfiguration(java.lang.String)` was added
* `withTopic(models.Topic)` was added
* `withEventNotifier(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withName(java.lang.String)` was added
* `withObservabilityMode(models.EventObservabilityMode)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withDefaultTopic(models.Topic)` was added
* `defaultTopic()` was added
* `withDefaultDatasetsConfiguration(java.lang.String)` was added
* `withDatasets(java.util.List)` was added
* `withDiscoveredAssetRefs(java.util.List)` was added
* `discoveredAssetRefs()` was added
* `datasets()` was added
* `defaultDatasetsConfiguration()` was added
* `assetEndpointProfileRef()` was added
* `withAssetEndpointProfileRef(java.lang.String)` was added

#### `models.X509Credentials` was modified

* `withCertificateSecretName(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `certificateSecretName()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `implementation.models.AssetListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetStatus` was modified

* `events()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `datasets()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetStatusError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `DeviceRegistryManager` was modified

* `schemaVersions()` was added
* `schemaRegistries()` was added
* `discoveredAssets()` was added
* `schemas()` was added
* `discoveredAssetEndpointProfiles()` was added
* `billingContainers()` was added

#### `models.AssetEndpointProfileProperties` was modified

* `withEndpointProfileType(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withAuthentication(models.Authentication)` was added
* `withDiscoveredAssetEndpointProfileRef(java.lang.String)` was added
* `authentication()` was added
* `endpointProfileType()` was added
* `discoveredAssetEndpointProfileRef()` was added
* `status()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetUpdateProperties` was modified

* `defaultDatasetsConfiguration()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `defaultTopic()` was added
* `datasets()` was added
* `withDatasets(java.util.List)` was added
* `withDefaultTopic(models.Topic)` was added
* `withDefaultDatasetsConfiguration(java.lang.String)` was added

#### `models.UsernamePasswordCredentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `usernameSecretName()` was added
* `withUsernameSecretName(java.lang.String)` was added
* `withPasswordSecretName(java.lang.String)` was added
* `passwordSecretName()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtendedLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `withEndpointProfileType(java.lang.String)` was added
* `authentication()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `endpointProfileType()` was added
* `withAuthentication(models.Authentication)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssetEndpointProfileUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataPoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withDataSource(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withObservabilityMode(models.DataPointObservabilityMode)` was added
* `withDataPointConfiguration(java.lang.String)` was added
* `withName(java.lang.String)` was added

#### `implementation.models.AssetEndpointProfileListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2024-04-26)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
