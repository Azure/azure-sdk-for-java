# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-02-22)

- Azure Resource Manager ApiCenter client library for Java. This package contains Microsoft Azure SDK for ApiCenter Management SDK. Azure API Center Resource Provider. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ServiceCollection` was removed

#### `models.Service` was modified

* `provisioningState()` was removed

#### `models.ServiceUpdate` was modified

* `provisioningState()` was removed

### Features Added

* `models.ApiDefinition$DefinitionStages` was added

* `models.Api` was added

* `models.MetadataSchema$Update` was added

* `models.WorkspaceProperties` was added

* `models.MetadataSchema$UpdateStages` was added

* `models.Deployments` was added

* `models.Environment$DefinitionStages` was added

* `models.Api$Definition` was added

* `models.ApiSpecExportResult` was added

* `models.Deployment$DefinitionStages` was added

* `models.ExternalDocumentation` was added

* `models.ApiKind` was added

* `models.DeploymentsCreateOrUpdateResponse` was added

* `models.ApiVersion$DefinitionStages` was added

* `models.MetadataSchemaExportRequest` was added

* `models.Environment$Definition` was added

* `models.ApiVersions` was added

* `models.Deployment` was added

* `models.Environment$Update` was added

* `models.ServiceProperties` was added

* `models.WorkspacesGetHeaders` was added

* `models.ApiDefinition$Definition` was added

* `models.ApiSpecImportSourceFormat` was added

* `models.ApiSpecImportRequest` was added

* `models.DeploymentProperties` was added

* `models.DeploymentListResult` was added

* `models.ApiVersion$UpdateStages` was added

* `models.MetadataSchema$DefinitionStages` was added

* `models.MetadataSchemasCreateOrUpdateHeaders` was added

* `models.ApiDefinition$UpdateStages` was added

* `models.MetadataSchema$Definition` was added

* `models.MetadataSchemaExportResult` was added

* `models.ApiSpecExportResultFormat` was added

* `models.EnvironmentProperties` was added

* `models.ApiVersion$Definition` was added

* `models.ApiProperties` was added

* `models.ApiListResult` was added

* `models.MetadataSchemasGetHeaders` was added

* `models.EnvironmentsCreateOrUpdateHeaders` was added

* `models.ApiDefinitions` was added

* `models.ApiDefinitionsGetResponse` was added

* `models.Apis` was added

* `models.ApiDefinitionProperties` was added

* `models.Contact` was added

* `models.Environment` was added

* `models.Api$UpdateStages` was added

* `models.EnvironmentKind` was added

* `models.ApiVersionsGetHeaders` was added

* `models.WorkspacesGetResponse` was added

* `models.Workspace$Update` was added

* `models.EnvironmentsGetResponse` was added

* `models.EnvironmentsGetHeaders` was added

* `models.Workspace$Definition` was added

* `models.DeploymentsGetHeaders` was added

* `models.MetadataSchemaProperties` was added

* `models.ApiVersion` was added

* `models.DeploymentServer` was added

* `models.MetadataSchemas` was added

* `models.EnvironmentServerType` was added

* `models.Api$Update` was added

* `models.ApisGetResponse` was added

* `models.Workspace` was added

* `models.DeploymentsCreateOrUpdateHeaders` was added

* `models.ApiVersionListResult` was added

* `models.ApiDefinitionsGetHeaders` was added

* `models.ApiDefinitionsCreateOrUpdateHeaders` was added

* `models.MetadataSchemasCreateOrUpdateResponse` was added

* `models.ApiVersionsGetResponse` was added

* `models.EnvironmentServer` was added

* `models.ApiDefinition` was added

* `models.ApiDefinitionsCreateOrUpdateResponse` was added

* `models.Deployment$UpdateStages` was added

* `models.Deployment$Definition` was added

* `models.Environment$UpdateStages` was added

* `models.WorkspacesCreateOrUpdateResponse` was added

* `models.Api$DefinitionStages` was added

* `models.MetadataSchemaExportFormat` was added

* `models.Workspace$DefinitionStages` was added

* `models.License` was added

* `models.MetadataSchema` was added

* `models.ApisCreateOrUpdateHeaders` was added

* `models.Environments` was added

* `models.EnvironmentsCreateOrUpdateResponse` was added

* `models.TermsOfService` was added

* `models.Onboarding` was added

* `models.ApiVersionProperties` was added

* `models.Workspaces` was added

* `models.WorkspaceListResult` was added

* `models.LifecycleStage` was added

* `models.Workspace$UpdateStages` was added

* `models.ApisGetHeaders` was added

* `models.ApiDefinitionPropertiesSpecification` was added

* `models.ApiVersionsCreateOrUpdateHeaders` was added

* `models.Deployment$Update` was added

* `models.MetadataAssignmentEntity` was added

* `models.EnvironmentListResult` was added

* `models.ApisCreateOrUpdateResponse` was added

* `models.WorkspacesCreateOrUpdateHeaders` was added

* `models.MetadataSchemaListResult` was added

* `models.ApiVersionsCreateOrUpdateResponse` was added

* `models.ServiceListResult` was added

* `models.DeploymentState` was added

* `models.MetadataSchemasGetResponse` was added

* `models.ApiVersion$Update` was added

* `models.DeploymentsGetResponse` was added

* `models.MetadataAssignment` was added

* `models.ApiSpecImportRequestSpecification` was added

* `models.ApiDefinitionListResult` was added

* `models.ApiDefinition$Update` was added

#### `ApiCenterManager` was modified

* `environments()` was added
* `apis()` was added
* `apiVersions()` was added
* `workspaces()` was added
* `apiDefinitions()` was added
* `deployments()` was added
* `metadataSchemas()` was added

#### `models.Service$Definition` was modified

* `withProperties(models.ServiceProperties)` was added

#### `models.Service` was modified

* `exportMetadataSchema(models.MetadataSchemaExportRequest)` was added
* `properties()` was added
* `exportMetadataSchema(models.MetadataSchemaExportRequest,com.azure.core.util.Context)` was added

#### `models.Services` was modified

* `exportMetadataSchema(java.lang.String,java.lang.String,models.MetadataSchemaExportRequest,com.azure.core.util.Context)` was added
* `exportMetadataSchema(java.lang.String,java.lang.String,models.MetadataSchemaExportRequest)` was added

#### `models.ServiceUpdate` was modified

* `tags()` was added
* `identity()` was added
* `withTags(java.util.Map)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Service$Update` was modified

* `withTags(java.util.Map)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.1 (2023-08-25)

- Azure Resource Manager ApiCenter client library for Java. This package contains Microsoft Azure SDK for ApiCenter Management SDK. Azure API Center Resource Provider. Package tag package-2023-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
