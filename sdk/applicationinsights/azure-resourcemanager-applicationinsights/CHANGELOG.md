# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-02-18)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2024-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MyWorkbooksListResult` was removed

#### `models.MyWorkbookManagedIdentityType` was removed

#### `models.MyWorkbook` was removed

#### `models.MyWorkbookUserAssignedIdentities` was removed

#### `models.MyWorkbookResource` was removed

#### `models.MyWorkbookManagedIdentity` was removed

#### `models.MyWorkbooks` was removed

#### `models.MyWorkbook$UpdateStages` was removed

#### `models.MyWorkbook$DefinitionStages` was removed

#### `models.Kind` was removed

#### `models.MyWorkbook$Update` was removed

#### `models.MyWorkbook$Definition` was removed

#### `models.ErrorDefinitionException` was removed

#### `models.ErrorDefinition` was removed

#### `ApplicationInsightsManager` was modified

* `myWorkbooks()` was removed

### Features Added

* `models.ComponentLinkedStorageAccounts` was added

* `models.ComponentLinkedStorageAccounts$Update` was added

* `models.ComponentLinkedStorageAccounts$DefinitionStages` was added

* `models.DeletedWorkbookResource` was added

* `models.DeletedWorkbooks` was added

* `models.ComponentLinkedStorageAccountsOperations` was added

* `models.DeletedWorkbooksListResult` was added

* `models.DeletedWorkbookErrorDefinitionException` was added

* `models.ComponentLinkedStorageAccountsPatch` was added

* `models.DeletedWorkbookInnerErrorTrace` was added

* `models.StorageType` was added

* `models.DeletedWorkbookErrorDefinition` was added

* `models.DeletedWorkbook` was added

* `models.ComponentLinkedStorageAccounts$Definition` was added

* `models.ComponentLinkedStorageAccounts$UpdateStages` was added

#### `ApplicationInsightsManager` was modified

* `deletedWorkbooks()` was added
* `componentLinkedStorageAccountsOperations()` was added

## 1.1.0 (2024-12-13)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2022-06-15-java. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.WorkbookResource` was modified

* `name()` was added
* `type()` was added
* `id()` was added

#### `models.AnnotationError` was modified

* `getAdditionalInfo()` was added
* `getDetails()` was added
* `getMessage()` was added
* `getTarget()` was added
* `getCode()` was added

#### `models.WorkbookErrorDefinition` was modified

* `getDetails()` was added
* `getTarget()` was added
* `getCode()` was added
* `getAdditionalInfo()` was added
* `getMessage()` was added

#### `models.WorkbookResourceIdentity` was modified

* `principalId()` was added
* `tenantId()` was added

#### `models.WebtestsResource` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.ComponentsResource` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.WorkItemConfigurationError` was modified

* `getDetails()` was added
* `getAdditionalInfo()` was added
* `getCode()` was added
* `getTarget()` was added
* `getMessage()` was added

#### `models.ErrorDefinition` was modified

* `getCode()` was added
* `getTarget()` was added
* `getAdditionalInfo()` was added
* `getDetails()` was added
* `getMessage()` was added

#### `models.WorkbookTemplateResource` was modified

* `name()` was added
* `id()` was added
* `type()` was added

## 1.0.0 (2023-10-27)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2022-06-15-java. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ComponentLinkedStorageAccounts` was removed

* `models.ComponentLinkedStorageAccounts$Update` was removed

* `models.ComponentLinkedStorageAccounts$DefinitionStages` was removed

* `models.ComponentLinkedStorageAccountsOperations` was removed

* `models.ComponentLinkedStorageAccountsPatch` was removed

* `models.StorageType` was removed

* `models.ComponentLinkedStorageAccounts$Definition` was removed

* `models.ComponentLinkedStorageAccounts$UpdateStages` was removed

#### `models.Workbooks` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WebTests` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WorkbookErrorDefinition` was modified

* `getInnerError()` was removed

#### `models.MyWorkbooks` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `ApplicationInsightsManager` was modified

* `componentLinkedStorageAccountsOperations()` was removed

#### `models.Components` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WorkbookTemplates` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.WorkbookInnerErrorTrace` was added

* `models.WebTestPropertiesRequest` was added

* `models.WebTestPropertiesValidationRules` was added

* `models.HeaderField` was added

* `models.WebTestPropertiesValidationRulesContentValidation` was added

#### `models.Workbooks` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.WebTests` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.WorkbookErrorDefinition` was modified

* `getInnererror()` was added

#### `models.WebTest` was modified

* `validationRules()` was added
* `request()` was added

#### `models.MyWorkbooks` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.WebTest$Definition` was modified

* `withValidationRules(models.WebTestPropertiesValidationRules)` was added
* `withRequest(models.WebTestPropertiesRequest)` was added

#### `models.Components` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.WorkbookTemplates` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.5 (2022-06-09)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2022-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SharedTypeKind` was removed

#### `models.WorkbookUpdateParameters` was modified

* `models.SharedTypeKind kind()` -> `models.WorkbookUpdateSharedTypeKind kind()`
* `withKind(models.SharedTypeKind)` was removed

#### `models.WorkbookResource` was modified

* `models.Kind kind()` -> `models.WorkbookSharedTypeKind kind()`
* `withKind(models.Kind)` was removed

#### `models.Workbook$Definition` was modified

* `withKind(models.Kind)` was removed

#### `models.Workbook$Update` was modified

* `withKind(models.SharedTypeKind)` was removed

#### `models.Workbook` was modified

* `models.Kind kind()` -> `models.WorkbookSharedTypeKind kind()`

### Features Added

* `models.WorkbookUpdateSharedTypeKind` was added

* `models.WorkbookSharedTypeKind` was added

#### `models.WorkbookUpdateParameters` was modified

* `withKind(models.WorkbookUpdateSharedTypeKind)` was added

#### `models.ApplicationInsightsComponent` was modified

* `resourceGroupName()` was added

#### `models.ComponentLinkedStorageAccounts` was modified

* `resourceGroupName()` was added

#### `models.WorkbookResource` was modified

* `withKind(models.WorkbookSharedTypeKind)` was added

#### `models.MyWorkbook` was modified

* `resourceGroupName()` was added

#### `models.Workbook$Definition` was modified

* `withKind(models.WorkbookSharedTypeKind)` was added

#### `models.WebTest` was modified

* `resourceGroupName()` was added

#### `models.Workbook$Update` was modified

* `withKind(models.WorkbookUpdateSharedTypeKind)` was added

#### `ApplicationInsightsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Workbook` was modified

* `resourceGroupName()` was added

#### `ApplicationInsightsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.WorkbookTemplate` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.4 (2022-03-24)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2022-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Workbooks` was modified

* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.Workbooks` was modified

* `getByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2022-01-30)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2022-01-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ApplicationInsightsComponent` was modified

* `disableLocalAuth()` was added
* `etag()` was added
* `forceCustomerStorageForProfiler()` was added
* `workspaceResourceId()` was added
* `laMigrationDate()` was added
* `namePropertiesName()` was added

#### `models.ApplicationInsightsComponent$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withWorkspaceResourceId(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withForceCustomerStorageForProfiler(java.lang.Boolean)` was added

## 1.0.0-beta.2 (2021-12-13)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2021-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedIdentityAutoGenerated` was removed

* `models.ManagedIdentityType` was removed

* `models.WorkbookError` was removed

* `models.UserAssignedIdentitiesAutoGenerated` was removed

* `models.UserAssignedIdentities` was removed

* `models.ManagedIdentity` was removed

* `models.WorkbookErrorException` was removed

#### `models.MyWorkbook` was modified

* `models.ManagedIdentity identity()` -> `models.MyWorkbookManagedIdentity identity()`

#### `models.MyWorkbookResource` was modified

* `withIdentity(models.ManagedIdentity)` was removed
* `models.ManagedIdentity identity()` -> `models.MyWorkbookManagedIdentity identity()`

#### `models.Workbook$Definition` was modified

* `withEtag(java.util.Map)` was removed
* `withIdentity(models.ManagedIdentityAutoGenerated)` was removed
* `withName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.Workbook$Update` was modified

* `sourceId(java.lang.String)` was removed

#### `models.Workbook` was modified

* `models.ManagedIdentityAutoGenerated identity()` -> `models.WorkbookResourceIdentity identity()`
* `java.lang.String timeModified()` -> `java.time.OffsetDateTime timeModified()`
* `java.util.Map etag()` -> `java.lang.String etag()`

#### `models.MyWorkbook$Update` was modified

* `withName(java.lang.String)` was removed
* `withIdentity(models.ManagedIdentity)` was removed
* `sourceIdParameter(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.MyWorkbook$Definition` was modified

* `withIdentity(models.ManagedIdentity)` was removed

### Features Added

* `models.WorkbookTemplateLocalizedGallery` was added

* `models.ComponentLinkedStorageAccounts` was added

* `models.ComponentLinkedStorageAccounts$Update` was added

* `models.ComponentLinkedStorageAccounts$DefinitionStages` was added

* `models.LiveTokenResponse` was added

* `models.WorkbookErrorDefinitionException` was added

* `models.WorkbookTemplateUpdateParameters` was added

* `models.WorkbookResource` was added

* `models.MyWorkbookManagedIdentityType` was added

* `models.MyWorkbookUserAssignedIdentities` was added

* `models.WorkbookErrorDefinition` was added

* `models.ComponentLinkedStorageAccountsOperations` was added

* `models.WorkbookTemplate$Definition` was added

* `models.WorkbookTemplate$Update` was added

* `models.WorkbookResourceIdentity` was added

* `models.UserAssignedIdentity` was added

* `models.MyWorkbookManagedIdentity` was added

* `models.LiveTokens` was added

* `models.WorkbookTemplate$UpdateStages` was added

* `models.WorkbookTemplate$DefinitionStages` was added

* `models.ComponentLinkedStorageAccountsPatch` was added

* `models.ManagedServiceIdentity` was added

* `models.WorkbookTemplate` was added

* `models.StorageType` was added

* `models.WorkbookTemplateGallery` was added

* `models.PublicNetworkAccessType` was added

* `models.WorkbookTemplatesListResult` was added

* `models.WorkbookTemplates` was added

* `models.ComponentLinkedStorageAccounts$Definition` was added

* `models.ComponentLinkedStorageAccounts$UpdateStages` was added

* `models.WorkbookTemplateResource` was added

* `models.ManagedServiceIdentityType` was added

#### `models.WorkbookUpdateParameters` was modified

* `withRevision(java.lang.String)` was added
* `revision()` was added
* `withDescription(java.lang.String)` was added
* `description()` was added

#### `models.ApplicationInsightsComponent` was modified

* `publicNetworkAccessForIngestion()` was added
* `purgeWithResponse(models.ComponentPurgeBody,com.azure.core.util.Context)` was added
* `purge(models.ComponentPurgeBody)` was added
* `publicNetworkAccessForQuery()` was added

#### `models.ApplicationInsightsComponent$Definition` was modified

* `withPublicNetworkAccessForQuery(models.PublicNetworkAccessType)` was added
* `withPublicNetworkAccessForIngestion(models.PublicNetworkAccessType)` was added

#### `models.Workbooks` was modified

* `revisionsList(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `revisionsList(java.lang.String,java.lang.String)` was added
* `revisionGet(java.lang.String,java.lang.String,java.lang.String)` was added
* `revisionGetWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.MyWorkbook` was modified

* `systemData()` was added

#### `models.MyWorkbookResource` was modified

* `withIdentity(models.MyWorkbookManagedIdentity)` was added

#### `models.Workbook$Definition` was modified

* `withEtag(java.lang.String)` was added
* `withDescription(java.lang.String)` was added
* `withIdentity(models.WorkbookResourceIdentity)` was added

#### `models.Workbook$Update` was modified

* `withDescription(java.lang.String)` was added
* `withRevision(java.lang.String)` was added
* `withSourceId(java.lang.String)` was added

#### `ApplicationInsightsManager` was modified

* `liveTokens()` was added
* `workbookTemplates()` was added
* `componentLinkedStorageAccountsOperations()` was added

#### `models.Workbook` was modified

* `description()` was added
* `systemData()` was added
* `revision()` was added

#### `ApplicationInsightsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.MyWorkbook$Update` was modified

* `withIdentity(models.MyWorkbookManagedIdentity)` was added
* `withSourceIdParameter(java.lang.String)` was added

#### `models.MyWorkbook$Definition` was modified

* `withIdentity(models.MyWorkbookManagedIdentity)` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager ApplicationInsights client library for Java. This package contains Microsoft Azure SDK for ApplicationInsights Management SDK. Composite Swagger for Application Insights Management Client. Package tag package-2020-10-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
