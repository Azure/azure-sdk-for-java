# Release History

## 1.2.0-beta.1 (2026-05-20)

- Azure Resource Manager Api Center client library for Java. This package contains Microsoft Azure SDK for Api Center Management SDK. Azure API Center Resource Provider. Package api-version 2024-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.ServiceListResult` was removed

#### `models.DeploymentListResult` was removed

#### `models.ApiListResult` was removed

#### `models.ApiVersionListResult` was removed

#### `models.WorkspaceListResult` was removed

#### `models.EnvironmentListResult` was removed

#### `models.MetadataSchemaListResult` was removed

#### `models.ApiDefinitionListResult` was removed

#### `models.MetadataSchemaExportRequest` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.WorkspacesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiSpecImportRequest` was modified

* `validate()` was removed

#### `models.DeploymentProperties` was modified

* `withCustomProperties(java.lang.Object)` was removed
* `validate()` was removed
* `java.lang.Object customProperties()` -> `models.CustomProperties customProperties()`

#### `models.EnvironmentProperties` was modified

* `validate()` was removed
* `java.lang.Object customProperties()` -> `models.CustomProperties customProperties()`
* `withCustomProperties(java.lang.Object)` was removed

#### `models.ApiProperties` was modified

* `java.lang.Object customProperties()` -> `models.CustomProperties customProperties()`
* `validate()` was removed
* `withCustomProperties(java.lang.Object)` was removed

#### `models.ApiDefinitionProperties` was modified

* `validate()` was removed

#### `models.DeploymentServer` was modified

* `validate()` was removed

#### `models.DeploymentsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiDefinitionsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiDefinitionsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EnvironmentServer` was modified

* `validate()` was removed

#### `models.License` was modified

* `validate()` was removed

#### `models.ApisCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.TermsOfService` was modified

* `validate()` was removed

#### `models.Onboarding` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.ServiceUpdate` was modified

* `validate()` was removed

#### `models.ApisGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspacesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.WorkspaceProperties` was modified

* `validate()` was removed

#### `models.ExternalDocumentation` was modified

* `validate()` was removed

#### `models.ServiceProperties` was modified

* `validate()` was removed

#### `models.MetadataSchemasCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.MetadataSchemasGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EnvironmentsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.Contact` was modified

* `validate()` was removed

#### `ApiCenterManager` was modified

* `fluent.AzureApiCenter serviceClient()` -> `fluent.ApiCenterManagementClient serviceClient()`

#### `models.ApiVersionsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EnvironmentsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.DeploymentsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.MetadataSchemaProperties` was modified

* `validate()` was removed

#### `models.ApiVersionProperties` was modified

* `validate()` was removed

#### `models.ApiDefinitionPropertiesSpecification` was modified

* `ApiDefinitionPropertiesSpecification()` was changed to private access
* `withVersion(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.ApiVersionsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.MetadataAssignment` was modified

* `validate()` was removed

#### `models.ApiSpecImportRequestSpecification` was modified

* `validate()` was removed

### Features Added

* `models.ApiSource` was added

* `models.ApiSourceLinkState` was added

* `models.ApiSourcesGetResponse` was added

* `models.ServiceUpdateProperties` was added

* `models.ApiSourcesGetHeaders` was added

* `models.ApiSourceProperties` was added

* `models.ImportSpecificationOptions` was added

* `models.ApiSourcesCreateOrUpdateResponse` was added

* `models.CustomProperties` was added

* `models.ApiSource$Definition` was added

* `models.DeletedServiceProperties` was added

* `models.ApiSourcesCreateOrUpdateHeaders` was added

* `models.DeletedServicesGetByResourceGroupHeaders` was added

* `models.DeletedServices` was added

* `models.DeletedService` was added

* `models.DeletedServicesGetByResourceGroupResponse` was added

* `models.AzureApiManagementSource` was added

* `models.LinkState` was added

* `models.ApiSource$DefinitionStages` was added

* `models.ApiSource$UpdateStages` was added

* `models.ApiSources` was added

* `models.ApiSource$Update` was added

#### `models.DeploymentProperties` was modified

* `withCustomProperties(models.CustomProperties)` was added

#### `models.EnvironmentProperties` was modified

* `withCustomProperties(models.CustomProperties)` was added

#### `models.ApiProperties` was modified

* `withCustomProperties(models.CustomProperties)` was added

#### `models.ServiceUpdate` was modified

* `withProperties(models.ServiceUpdateProperties)` was added
* `properties()` was added

#### `models.Service$Update` was modified

* `withProperties(models.ServiceUpdateProperties)` was added

#### `models.ServiceProperties` was modified

* `restore()` was added
* `withRestore(java.lang.Boolean)` was added

#### `ApiCenterManager` was modified

* `apiSources()` was added
* `deletedServices()` was added

## 1.1.0 (2024-12-13)

- Azure Resource Manager ApiCenter client library for Java. This package contains Microsoft Azure SDK for ApiCenter Management SDK. Azure API Center Resource Provider. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.MetadataSchema$Update` was modified

* `withProperties(models.MetadataSchemaProperties)` was added

#### `models.Environment$Update` was modified

* `withProperties(models.EnvironmentProperties)` was added

#### `models.Workspace$Update` was modified

* `withProperties(models.WorkspaceProperties)` was added

#### `models.Api$Update` was modified

* `withProperties(models.ApiProperties)` was added

#### `models.Deployment$Update` was modified

* `withProperties(models.DeploymentProperties)` was added

#### `models.ApiVersion$Update` was modified

* `withProperties(models.ApiVersionProperties)` was added

#### `models.ApiDefinition$Update` was modified

* `withProperties(models.ApiDefinitionProperties)` was added

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
