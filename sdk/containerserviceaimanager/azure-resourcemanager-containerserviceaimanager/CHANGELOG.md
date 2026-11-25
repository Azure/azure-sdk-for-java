# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-09-16)

- Azure Resource Manager Container Service AI Manager client library for Java. This package contains Microsoft Azure SDK for Container Service AI Manager Management SDK. Azure Kubernetes AI Manager api client. Package api-version 2026-09-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CalculateCostRequest` was removed

#### `models.AIModels` was modified

* `calculateCost(java.lang.String,java.lang.String,models.CalculateCostRequest)` was removed
* `calculateCostWithResponse(java.lang.String,java.lang.String,models.CalculateCostRequest,com.azure.core.util.Context)` was removed

### Features Added

* `models.ManagedIdentityCredential` was added

* `models.BaseModelReference` was added

* `models.CustomAIModel` was added

* `models.CustomAIModels` was added

* `models.CustomAIModel$Update` was added

* `models.CustomAIModelSpec` was added

* `models.CustomAIModel$DefinitionStages` was added

* `models.CustomAIModel$Definition` was added

* `models.CustomAIModel$UpdateStages` was added

* `models.MicrosoftFoundrySource` was added

* `models.CustomAIModelProvisioningState` was added

* `models.CustomAIModelProperties` was added

#### `models.CredentialValue` was modified

* `managedIdentity()` was added
* `withManagedIdentity(models.ManagedIdentityCredential)` was added

#### `models.ModelSourceType` was modified

* `MICROSOFT_FOUNDRY` was added

#### `models.AIManagerProperties` was modified

* `clusterResourceId()` was added
* `withClusterResourceId(java.lang.String)` was added

#### `ContainerServiceAIManagerManager` was modified

* `customAIModels()` was added

#### `models.AIModels` was modified

* `calculateCostWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `calculateCost(java.lang.String,java.lang.String)` was added

#### `models.ModelSourceProperties` was modified

* `withMicrosoftFoundry(models.MicrosoftFoundrySource)` was added
* `microsoftFoundry()` was added

## 1.0.0-beta.1 (2026-08-06)

- Azure Resource Manager Container Service AI Manager client library for Java. This package contains Microsoft Azure SDK for Container Service AI Manager Management SDK. Azure Kubernetes AI Manager api client. Package api-version 2026-05-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-containerserviceaimanager Java SDK.
