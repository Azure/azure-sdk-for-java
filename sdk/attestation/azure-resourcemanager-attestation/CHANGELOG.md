# Release History

## 1.0.0 (2026-03-06)

- Azure Resource Manager Attestation client library for Java. This package contains Microsoft Azure SDK for Attestation Management SDK. Various APIs for managing resources in attestation service. This primarily encompasses per-provider management. Package api-version 2021-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.OperationList` was removed

#### `models.AttestationServicePatchParams` was modified

* `validate()` was removed

#### `models.AttestationServiceCreationParams` was modified

* `validate()` was removed

#### `models.AttestationServiceCreationSpecificParams` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.OperationsDisplayDefinition` was modified

* `OperationsDisplayDefinition()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `validate()` was removed
* `withProvider(java.lang.String)` was removed

#### `models.Operations` was modified

* `models.OperationList list()` -> `com.azure.core.http.rest.PagedIterable list()`
* `listWithResponse(com.azure.core.util.Context)` was removed

#### `models.JsonWebKeySet` was modified

* `validate()` was removed

#### `models.JsonWebKey` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.OperationsDefinition` was modified

* `OperationsDefinition()` was removed
* `withName(java.lang.String)` was removed
* `models.OperationsDisplayDefinition display()` -> `models.OperationsDisplayDefinition display()`
* `validate()` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `withDisplay(models.OperationsDisplayDefinition)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed

### Features Added

* `models.PrivateLinkResources` was added

* `models.PrivateLinkResource` was added

* `models.TpmAttestationAuthenticationType` was added

* `models.OperationProperties` was added

* `models.AttestationServicePatchSpecificParams` was added

* `models.PublicNetworkAccessType` was added

* `models.PrivateLinkResourceListResult` was added

* `models.ServiceSpecification` was added

* `models.PrivateLinkResourceProperties` was added

* `models.LogSpecification` was added

#### `models.AttestationServicePatchParams` was modified

* `properties()` was added
* `withProperties(models.AttestationServicePatchSpecificParams)` was added

#### `models.AttestationProvider$Update` was modified

* `withProperties(models.AttestationServicePatchSpecificParams)` was added

#### `models.AttestationServiceCreationSpecificParams` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `withTpmAttestationAuthentication(models.TpmAttestationAuthenticationType)` was added
* `publicNetworkAccess()` was added
* `tpmAttestationAuthentication()` was added

#### `models.AttestationProvider` was modified

* `publicNetworkAccess()` was added
* `tpmAttestationAuthentication()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `AttestationManager` was modified

* `privateLinkResources()` was added

#### `models.OperationsDefinition` was modified

* `innerModel()` was added
* `properties()` was added

## 1.0.0-beta.3 (2024-10-14)

- Azure Resource Manager Attestation client library for Java. This package contains Microsoft Azure SDK for Attestation Management SDK. Various APIs for managing resources in attestation service. This primarily encompasses per-provider management. Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.JsonWebKeySet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JsonWebKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttestationServicePatchParams` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttestationServiceCreationParams` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttestationServiceCreationSpecificParams` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationsDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationsDisplayDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2023-01-11)

- Azure Resource Manager Attestation client library for Java. This package contains Microsoft Azure SDK for Attestation Management SDK. Various APIs for managing resources in attestation service. This primarily encompasses per-provider management. Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AttestationProviders` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.AttestationProviders` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AttestationProvider` was modified

* `resourceGroupName()` was added

#### `AttestationManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `AttestationManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager Attestation client library for Java. This package contains Microsoft Azure SDK for Attestation Management SDK. Various APIs for managing resources in attestation service. This primarily encompasses per-provider management. Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
