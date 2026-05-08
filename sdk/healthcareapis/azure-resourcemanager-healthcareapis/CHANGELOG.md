# Release History

## 1.3.0-beta.1 (2026-05-08)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK.  Package api-version 2025-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.IotConnectorCollection` was removed

#### `models.ServicesResource` was removed

#### `models.ServicesDescriptionListResult` was removed

#### `models.DicomServiceCollection` was removed

#### `models.WorkspaceList` was removed

#### `models.PrivateLinkResource` was removed

#### `models.ServiceManagedIdentity` was removed

#### `models.ListOperations` was removed

#### `models.IotFhirDestinationCollection` was removed

#### `models.ResourceCore` was removed

#### `models.FhirServiceCollection` was removed

#### `models.PrivateEndpointConnectionListResultDescription` was removed

#### `models.TaggedResource` was removed

#### `models.LocationBasedResource` was removed

#### `models.PrivateEndpointConnectionDescription$DefinitionStages` was modified

* `withExistingService(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ServicesPatchDescription` was modified

* `validate()` was removed

#### `models.ServiceAcrConfigurationInfo` was modified

* `validate()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `validate()` was removed
* `withLogSpecifications(java.util.List)` was removed
* `withMetricSpecifications(java.util.List)` was removed

#### `models.MetricDimension` was modified

* `MetricDimension()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.ServiceOciArtifactEntry` was modified

* `validate()` was removed

#### `models.ServiceAuthenticationConfigurationInfo` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.ImplementationGuidesConfiguration` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.CheckNameAvailabilityParameters` was modified

* `validate()` was removed

#### `models.FhirServiceAcrConfiguration` was modified

* `validate()` was removed

#### `models.ResourceTags` was modified

* `validate()` was removed

#### `models.IotConnectorPatchResource` was modified

* `validate()` was removed
* `withTags(java.util.Map)` was removed

#### `models.ServiceAccessPolicyEntry` was modified

* `validate()` was removed

#### `models.ServiceManagedIdentityIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.DicomServicePatchResource` was modified

* `validate()` was removed
* `withTags(java.util.Map)` was removed

#### `models.PrivateEndpointConnection` was modified

* `validate()` was removed

#### `models.SmartIdentityProviderConfiguration` was modified

* `validate()` was removed

#### `models.ServiceImportConfigurationInfo` was modified

* `validate()` was removed

#### `models.StorageConfiguration` was modified

* `validate()` was removed

#### `models.IotDestinationProperties` was modified

* `validate()` was removed

#### `models.WorkspaceProperties` was modified

* `validate()` was removed

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withCategory(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `validate()` was removed
* `withSupportedAggregationTypes(java.util.List)` was removed
* `withSourceMdmAccount(java.lang.String)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `withMetricFilterPattern(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withSourceMdmNamespace(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withIsInternal(java.lang.Boolean)` was removed
* `withEnableRegionalMdmAccount(java.lang.Boolean)` was removed
* `withDimensions(java.util.List)` was removed
* `withResourceIdDimensionNameOverride(java.lang.String)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed

#### `models.WorkspacePrivateEndpointConnections` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionDescriptionInner)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionDescriptionInner,com.azure.core.util.Context)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.PrivateEndpointConnections` was modified

* `deleteById(java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `define(java.lang.String)` was removed

#### `models.ServicesResourceIdentity` was modified

* `validate()` was removed

#### `models.FhirServiceCorsConfiguration` was modified

* `validate()` was removed

#### `models.ServiceCorsConfigurationInfo` was modified

* `validate()` was removed

#### `models.DicomServiceAuthenticationConfiguration` was modified

* `validate()` was removed

#### `models.Encryption` was modified

* `validate()` was removed

#### `models.FhirServiceAuthenticationConfiguration` was modified

* `validate()` was removed

#### `models.FhirServiceExportConfiguration` was modified

* `validate()` was removed

#### `models.IotMappingProperties` was modified

* `validate()` was removed

#### `models.OperationProperties` was modified

* `OperationProperties()` was changed to private access
* `validate()` was removed
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.ServicesProperties` was modified

* `validate()` was removed

#### `models.ServiceCosmosDbConfigurationInfo` was modified

* `validate()` was removed

#### `models.SmartIdentityProviderApplication` was modified

* `validate()` was removed

#### `models.FhirServiceImportConfiguration` was modified

* `validate()` was removed

#### `models.FhirServicePatchResource` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed

#### `models.CorsConfiguration` was modified

* `validate()` was removed

#### `models.IotEventHubIngestionEndpointConfiguration` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnectionDescription$Definition` was modified

* `withExistingService(java.lang.String,java.lang.String)` was removed

#### `models.WorkspacePatchResource` was modified

* `validate()` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.ResourceVersionPolicyConfiguration` was modified

* `validate()` was removed

#### `models.ServiceExportConfigurationInfo` was modified

* `validate()` was removed

#### `models.EncryptionCustomerManagedKeyEncryption` was modified

* `validate()` was removed

### Features Added

* `models.StorageIndexingConfiguration` was added

#### `models.PrivateLinkResourceListResultDescription` was modified

* `nextLink()` was added

#### `models.IotConnectorPatchResource` was modified

* `tags()` was added

#### `models.DicomServicePatchResource` was modified

* `tags()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.StorageConfiguration` was modified

* `storageIndexingConfiguration()` was added
* `withStorageIndexingConfiguration(models.StorageIndexingConfiguration)` was added

#### `models.WorkspacePrivateEndpointConnections` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrivateEndpointConnections` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,models.PrivateEndpointConnection,com.azure.core.util.Context)` was added

#### `models.FhirServicePatchResource` was modified

* `tags()` was added

#### `models.PrivateEndpointConnectionDescription$Definition` was modified

* `withExistingWorkspace(java.lang.String,java.lang.String)` was added

## 1.2.0 (2024-12-19)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK. Azure Healthcare APIs Client. Package tag package-2024-03-31. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.ServicesResource` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.PrivateLinkResource` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.PrivateEndpointConnection` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.ResourceCore` was modified

* `id()` was added
* `type()` was added
* `name()` was added

#### `models.TaggedResource` was modified

* `id()` was added
* `name()` was added
* `type()` was added

#### `models.LocationBasedResource` was modified

* `name()` was added
* `type()` was added
* `id()` was added

## 1.1.0 (2024-04-15)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK. Azure Healthcare APIs Client. Package tag package-2024-03-31. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SmartDataActions` was added

* `models.SmartIdentityProviderConfiguration` was added

* `models.StorageConfiguration` was added

* `models.SmartIdentityProviderApplication` was added

#### `models.DicomService` was modified

* `enableDataPartitions()` was added
* `storageConfiguration()` was added

#### `models.FhirServiceAuthenticationConfiguration` was modified

* `withSmartIdentityProviders(java.util.List)` was added
* `smartIdentityProviders()` was added

#### `models.DicomService$Definition` was modified

* `withEnableDataPartitions(java.lang.Boolean)` was added
* `withStorageConfiguration(models.StorageConfiguration)` was added

## 1.0.0 (2023-12-21)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK. Azure Healthcare APIs Client. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.FhirServiceAccessPolicyEntry` was removed

#### `models.FhirService$Definition` was modified

* `withAccessPolicies(java.util.List)` was removed

#### `models.FhirService` was modified

* `accessPolicies()` was removed

### Features Added

* `models.ImplementationGuidesConfiguration` was added

* `models.ServiceImportConfigurationInfo` was added

* `models.Encryption` was added

* `models.FhirServiceImportConfiguration` was added

* `models.CorsConfiguration` was added

* `models.EncryptionCustomerManagedKeyEncryption` was added

#### `models.FhirService$Definition` was modified

* `withImportConfiguration(models.FhirServiceImportConfiguration)` was added
* `withEncryption(models.Encryption)` was added
* `withImplementationGuidesConfiguration(models.ImplementationGuidesConfiguration)` was added

#### `models.ServicesDescription` was modified

* `resourceGroupName()` was added

#### `models.DicomService` was modified

* `eventState()` was added
* `corsConfiguration()` was added
* `resourceGroupName()` was added
* `encryption()` was added

#### `models.IotConnector` was modified

* `resourceGroupName()` was added

#### `models.MetricSpecification` was modified

* `enableRegionalMdmAccount()` was added
* `metricFilterPattern()` was added
* `sourceMdmAccount()` was added
* `withIsInternal(java.lang.Boolean)` was added
* `withEnableRegionalMdmAccount(java.lang.Boolean)` was added
* `withMetricFilterPattern(java.lang.String)` was added
* `withResourceIdDimensionNameOverride(java.lang.String)` was added
* `isInternal()` was added
* `resourceIdDimensionNameOverride()` was added
* `withSourceMdmAccount(java.lang.String)` was added

#### `models.Workspace` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnectionDescription` was modified

* `resourceGroupName()` was added

#### `models.FhirService` was modified

* `implementationGuidesConfiguration()` was added
* `resourceGroupName()` was added
* `importConfiguration()` was added
* `encryption()` was added

#### `models.ServicesProperties` was modified

* `importConfiguration()` was added
* `withImportConfiguration(models.ServiceImportConfigurationInfo)` was added

#### `models.ServiceCosmosDbConfigurationInfo` was modified

* `crossTenantCmkApplicationId()` was added
* `withCrossTenantCmkApplicationId(java.lang.String)` was added

#### `models.IotFhirDestination` was modified

* `resourceGroupName()` was added

#### `models.DicomService$Definition` was modified

* `withEncryption(models.Encryption)` was added
* `withCorsConfiguration(models.CorsConfiguration)` was added

## 1.0.0-beta.2 (2022-04-11)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK. Azure Healthcare APIs Client. Package tag package-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationListResult` was removed

* `models.Operation` was removed

### Features Added

* `models.FhirService$Definition` was added

* `models.Workspace$DefinitionStages` was added

* `models.FhirDestinations` was added

* `models.IotConnectorCollection` was added

* `models.ServiceSpecification` was added

* `models.ServiceManagedIdentityType` was added

* `models.MetricDimension` was added

* `models.IotConnector$Definition` was added

* `models.ActionType` was added

* `models.ServiceOciArtifactEntry` was added

* `models.DicomService$Update` was added

* `models.DicomServiceCollection` was added

* `models.IotConnector$UpdateStages` was added

* `models.OperationDetail` was added

* `models.FhirResourceVersionPolicy` was added

* `models.UserAssignedIdentity` was added

* `models.WorkspaceList` was added

* `models.FhirService$UpdateStages` was added

* `models.FhirService$Update` was added

* `models.FhirServiceAcrConfiguration` was added

* `models.IotFhirDestination$Definition` was added

* `models.ResourceTags` was added

* `models.IotConnectorPatchResource` was added

* `models.ServiceManagedIdentity` was added

* `models.Workspace$UpdateStages` was added

* `models.ServiceManagedIdentityIdentity` was added

* `models.ServiceEventState` was added

* `models.DicomServicePatchResource` was added

* `models.ListOperations` was added

* `models.IotFhirDestinationCollection` was added

* `models.DicomService` was added

* `models.ResourceCore` was added

* `models.IotConnector$DefinitionStages` was added

* `models.DicomServices` was added

* `models.IotDestinationProperties` was added

* `models.WorkspaceProperties` was added

* `models.IotConnector` was added

* `models.MetricSpecification` was added

* `models.WorkspacePrivateEndpointConnections` was added

* `models.Workspace` was added

* `models.IotIdentityResolutionType` was added

* `models.FhirServices` was added

* `models.FhirServiceCorsConfiguration` was added

* `models.IotFhirDestination$Update` was added

* `models.IotFhirDestination$DefinitionStages` was added

* `models.Workspaces` was added

* `models.DicomServiceAuthenticationConfiguration` was added

* `models.FhirServiceKind` was added

* `models.Workspace$Update` was added

* `models.FhirServiceCollection` was added

* `models.FhirServiceAccessPolicyEntry` was added

* `models.FhirServiceAuthenticationConfiguration` was added

* `models.TaggedResource` was added

* `models.FhirServiceExportConfiguration` was added

* `models.IotMappingProperties` was added

* `models.OperationProperties` was added

* `models.IotFhirDestination$UpdateStages` was added

* `models.IotConnectors` was added

* `models.FhirService` was added

* `models.IotConnector$Update` was added

* `models.IotConnectorFhirDestinations` was added

* `models.DicomService$UpdateStages` was added

* `models.DicomService$DefinitionStages` was added

* `models.LocationBasedResource` was added

* `models.FhirServicePatchResource` was added

* `models.WorkspacePrivateLinkResources` was added

* `models.IotEventHubIngestionEndpointConfiguration` was added

* `models.Workspace$Definition` was added

* `models.WorkspacePatchResource` was added

* `models.LogSpecification` was added

* `models.ResourceVersionPolicyConfiguration` was added

* `models.IotFhirDestination` was added

* `models.DicomService$Definition` was added

* `models.FhirService$DefinitionStages` was added

#### `models.ServiceAcrConfigurationInfo` was modified

* `ociArtifacts()` was added
* `withOciArtifacts(java.util.List)` was added

#### `HealthcareApisManager` was modified

* `workspacePrivateEndpointConnections()` was added
* `iotConnectorFhirDestinations()` was added
* `fhirServices()` was added
* `fhirDestinations()` was added
* `workspacePrivateLinkResources()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `dicomServices()` was added
* `workspaces()` was added
* `iotConnectors()` was added

#### `models.OperationResultsDescription` was modified

* `endTime()` was added

#### `HealthcareApisManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager HealthcareApis client library for Java. This package contains Microsoft Azure SDK for HealthcareApis Management SDK. Azure Healthcare APIs Client. Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
