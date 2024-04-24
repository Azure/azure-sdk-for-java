# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
