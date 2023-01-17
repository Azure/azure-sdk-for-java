# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager Logic client library for Java. This package contains Microsoft Azure SDK for Logic Management SDK. REST API for Azure Logic Apps. Package tag package-2019-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedApiListResult` was removed

* `models.WorkflowRunActionRepetitionProperties` was removed

* `models.ManagedApi` was removed

#### `models.IntegrationServiceEnvironments` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.IntegrationAccount$Definition` was modified

* `withIntegrationServiceEnvironment(fluent.models.IntegrationServiceEnvironmentInner)` was removed

#### `models.IntegrationAccount` was modified

* `models.IntegrationServiceEnvironment integrationServiceEnvironment()` -> `models.ResourceReference integrationServiceEnvironment()`

#### `models.ApiResourceProperties` was modified

* `withCategory(models.ApiTier)` was removed
* `withBackendService(models.ApiResourceBackendService)` was removed
* `withName(java.lang.String)` was removed
* `withPolicies(models.ApiResourcePolicies)` was removed
* `withMetadata(models.ApiResourceMetadata)` was removed
* `withRuntimeUrls(java.util.List)` was removed
* `withCapabilities(java.util.List)` was removed
* `withConnectionParameters(java.util.Map)` was removed
* `withApiDefinitions(models.ApiResourceDefinitions)` was removed
* `withProvisioningState(models.WorkflowProvisioningState)` was removed
* `withGeneralInformation(models.ApiResourceGeneralInformation)` was removed
* `withApiDefinitionUrl(java.lang.String)` was removed

#### `models.IntegrationAccounts` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.IntegrationAccount$Update` was modified

* `withIntegrationServiceEnvironment(fluent.models.IntegrationServiceEnvironmentInner)` was removed

#### `models.ContentLink` was modified

* `withContentSize(java.lang.Long)` was removed
* `withMetadata(java.lang.Object)` was removed
* `withContentHash(models.ContentHash)` was removed
* `withContentVersion(java.lang.String)` was removed

#### `models.Workflows` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.IntegrationServiceEnvironmentManagedApis` was modified

* `models.ManagedApi get(java.lang.String,java.lang.String,java.lang.String)` -> `models.IntegrationServiceEnvironmentManagedApi get(java.lang.String,java.lang.String,java.lang.String)`
* `put(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `put(java.lang.String,java.lang.String,java.lang.String)` was removed

### Features Added

* `models.UserAssignedIdentity` was added

* `models.ManagedServiceIdentity` was added

* `models.IntegrationServiceEnvironmentManagedApi$Definition` was added

* `models.IntegrationServiceEnvironmentManagedApi$DefinitionStages` was added

* `models.IntegrationServiceEnvironmentManagedApiListResult` was added

* `models.IntegrationServiceEnvironmentManagedApi$Update` was added

* `models.ManagedServiceIdentityType` was added

* `models.IntegrationServiceEnvironmentManagedApi$UpdateStages` was added

* `models.IntegrationServiceEnvironmentManagedApiDeploymentParameters` was added

* `models.IntegrationServiceEnvironmentManagedApi` was added

#### `models.IntegrationAccountSchema` was modified

* `resourceGroupName()` was added

#### `models.IntegrationServiceEnvironments` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.IntegrationAccount$Definition` was modified

* `withIntegrationServiceEnvironment(models.ResourceReference)` was added

#### `models.IntegrationAccountCertificate` was modified

* `resourceGroupName()` was added

#### `models.IntegrationServiceEnvironment` was modified

* `resourceGroupName()` was added
* `identity()` was added

#### `models.IntegrationAccount` was modified

* `resourceGroupName()` was added

#### `models.IntegrationAccounts` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.IntegrationAccount$Update` was modified

* `withIntegrationServiceEnvironment(models.ResourceReference)` was added

#### `models.IntegrationAccountSession` was modified

* `resourceGroupName()` was added

#### `models.IntegrationServiceEnvironment$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Workflow$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.IntegrationAccountPartner` was modified

* `resourceGroupName()` was added

#### `models.Workflow$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `LogicManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Workflow` was modified

* `identity()` was added
* `resourceGroupName()` was added

#### `models.IntegrationServiceEnvironment$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.IntegrationAccountMap` was modified

* `resourceGroupName()` was added

#### `models.Workflows` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `LogicManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.OpenAuthenticationAccessPolicy` was modified

* `withType(models.OpenAuthenticationProviderType)` was added

#### `models.IntegrationServiceEnvironmentManagedApis` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added

#### `models.AssemblyDefinition` was modified

* `resourceGroupName()` was added

#### `models.IntegrationAccountAgreement` was modified

* `resourceGroupName()` was added

#### `models.BatchConfiguration` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-16)

- Azure Resource Manager Logic client library for Java. This package contains Microsoft Azure SDK for Logic Management SDK. REST API for Azure Logic Apps. Package tag package-2019-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
