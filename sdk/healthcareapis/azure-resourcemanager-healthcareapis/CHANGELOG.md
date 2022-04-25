# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
