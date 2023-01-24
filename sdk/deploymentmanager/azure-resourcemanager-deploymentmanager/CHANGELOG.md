# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-16)

- Azure Resource Manager Deployment client library for Java. This package contains Microsoft Azure SDK for Deployment Management SDK. REST APIs for orchestrating deployments using the Azure Deployment Manager (ADM). Package tag package-2019-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.RolloutsCreateOrUpdateHeaders` was removed

* `models.ServiceUnitsCreateOrUpdateHeaders` was removed

* `models.ServiceUnitsCreateOrUpdateResponse` was removed

* `models.ServiceResourceProperties` was removed

* `models.RolloutRequestProperties` was removed

* `models.RolloutProperties` was removed

* `models.RolloutsCreateOrUpdateResponse` was removed

* `models.ServiceUnitResourceProperties` was removed

* `models.ServiceTopologyResourceProperties` was removed

* `models.ArtifactSourceProperties` was removed

#### `models.Steps` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Rollouts` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ServiceTopologies` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ArtifactSources` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.Steps` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ServiceUnitResource` was modified

* `resourceGroupName()` was added

#### `models.ServiceResource` was modified

* `resourceGroupName()` was added

#### `models.ServiceTopologyResource` was modified

* `resourceGroupName()` was added

#### `models.Rollouts` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `DeploymentManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.ServiceTopologies` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ArtifactSources` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StepResource` was modified

* `resourceGroupName()` was added

#### `DeploymentManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.ArtifactSource` was modified

* `resourceGroupName()` was added

#### `models.RolloutRequest` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager Deployment client library for Java. This package contains Microsoft Azure SDK for Deployment Management SDK. REST APIs for orchestrating deployments using the Azure Deployment Manager (ADM). See https://docs.microsoft.com/azure/azure-resource-manager/deployment-manager-overview for more information. Package tag package-2019-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

