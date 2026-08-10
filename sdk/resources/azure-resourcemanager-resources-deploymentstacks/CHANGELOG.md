# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2026-02-06)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK.  Package api-version 2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AzureResourceBase` was removed

#### `models.DeploymentStackListResult` was removed

#### `models.DeploymentStacksDeleteDetachEnum` was removed

#### `models.DeploymentStackProperties` was modified

* `validate()` was removed
* `withError(com.azure.core.management.exception.ManagementError)` was removed

#### `models.DeploymentStacks` was modified

* `deleteAtSubscription(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteAtManagementGroup(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.ActionOnUnmanage` was modified

* `withResourceGroups(models.DeploymentStacksDeleteDetachEnum)` was removed
* `models.DeploymentStacksDeleteDetachEnum resources()` -> `models.UnmanageActionResourceMode resources()`
* `models.DeploymentStacksDeleteDetachEnum resourceGroups()` -> `models.UnmanageActionResourceGroupMode resourceGroups()`
* `withManagementGroups(models.DeploymentStacksDeleteDetachEnum)` was removed
* `models.DeploymentStacksDeleteDetachEnum managementGroups()` -> `models.UnmanageActionManagementGroupMode managementGroups()`
* `withResources(models.DeploymentStacksDeleteDetachEnum)` was removed
* `validate()` was removed

#### `models.DenySettings` was modified

* `validate()` was removed

#### `models.ResourceReferenceExtended` was modified

* `ResourceReferenceExtended()` was changed to private access
* `validate()` was removed
* `withError(com.azure.core.management.exception.ManagementError)` was removed

#### `models.DeploymentStacksTemplateLink` was modified

* `validate()` was removed

#### `models.DeploymentStackValidateProperties` was modified

* `DeploymentStackValidateProperties()` was changed to private access
* `withDeploymentScope(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withDenySettings(models.DenySettings)` was removed
* `withActionOnUnmanage(models.ActionOnUnmanage)` was removed
* `withParameters(java.util.Map)` was removed
* `withCorrelationId(java.lang.String)` was removed
* `withTemplateLink(models.DeploymentStacksTemplateLink)` was removed
* `withValidatedResources(java.util.List)` was removed

#### `models.DeploymentParameter` was modified

* `validate()` was removed

#### `models.DeploymentStacksDebugSetting` was modified

* `validate()` was removed

#### `models.KeyVaultParameterReference` was modified

* `validate()` was removed

#### `models.ManagedResourceReference` was modified

* `ManagedResourceReference()` was changed to private access
* `withStatus(models.ResourceStatusMode)` was removed
* `withDenyStatus(models.DenyStatusMode)` was removed
* `validate()` was removed

#### `models.ResourceReference` was modified

* `validate()` was removed

#### `models.KeyVaultReference` was modified

* `validate()` was removed

#### `models.DeploymentStacksParametersLink` was modified

* `validate()` was removed

### Features Added

* `models.DeploymentExternalInputDefinition` was added

* `models.DeploymentStacksWhatIfResultsAtSubscriptions` was added

* `models.DeploymentStacksDiagnosticLevel` was added

* `models.DeploymentStacksChangeBase` was added

* `models.DeploymentStacksChangeDeltaDenySettings` was added

* `models.DeploymentStacksWhatIfResourceChange` was added

* `models.ResourcesWithoutDeleteSupportAction` was added

* `models.DeploymentStacksWhatIfResult$DefinitionStages` was added

* `models.DeploymentStacksWhatIfResultsAtResourceGroups` was added

* `models.DeploymentStacksWhatIfResult` was added

* `models.DeploymentStacksManagementStatus` was added

* `models.DeploymentExternalInput` was added

* `models.DeploymentStacksWhatIfResultProperties` was added

* `models.DeploymentExtensionConfigItem` was added

* `models.ErrorAdditionalInfo` was added

* `models.DeploymentStacksWhatIfResult$UpdateStages` was added

* `models.DeploymentStacksWhatIfResult$Update` was added

* `models.DeploymentStacksChangeBaseDenyStatusMode` was added

* `models.DeploymentStacksDiagnostic` was added

* `models.DeploymentStacksChangeBaseDeploymentStacksManagementStatus` was added

* `models.ValidationLevel` was added

* `models.DeploymentStacksChangeDeltaRecord` was added

* `models.DeploymentStacksWhatIfPropertyChange` was added

* `models.DeploymentStacksWhatIfPropertyChangeType` was added

* `models.DeploymentStacksWhatIfResultsAtManagementGroups` was added

* `models.DeploymentStacksWhatIfChangeCertainty` was added

* `models.DeploymentStacksWhatIfResult$Definition` was added

* `models.DeploymentExtensionConfig` was added

* `models.DeploymentExtension` was added

* `models.DeploymentStacksWhatIfChange` was added

* `models.DeploymentStacksWhatIfChangeType` was added

#### `models.DeploymentStackProperties` was modified

* `externalInputs()` was added
* `deploymentExtensions()` was added
* `withExternalInputs(java.util.Map)` was added
* `withExternalInputDefinitions(java.util.Map)` was added
* `withValidationLevel(models.ValidationLevel)` was added
* `withExtensionConfigs(java.util.Map)` was added
* `extensionConfigs()` was added
* `externalInputDefinitions()` was added
* `validationLevel()` was added

#### `models.DeploymentStacks` was modified

* `deleteAtManagementGroup(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,models.ResourcesWithoutDeleteSupportAction,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,models.ResourcesWithoutDeleteSupportAction,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteAtSubscription(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,models.ResourcesWithoutDeleteSupportAction,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,models.ResourcesWithoutDeleteSupportAction,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.ActionOnUnmanage` was modified

* `resourcesWithoutDeleteSupport()` was added
* `withManagementGroups(models.UnmanageActionManagementGroupMode)` was added
* `withResources(models.UnmanageActionResourceMode)` was added
* `withResourceGroups(models.UnmanageActionResourceGroupMode)` was added
* `withResourcesWithoutDeleteSupport(models.ResourcesWithoutDeleteSupportAction)` was added

#### `models.ResourceReferenceExtended` was modified

* `apiVersion()` was added
* `identifiers()` was added
* `type()` was added
* `extension()` was added

#### `DeploymentStacksManager` was modified

* `deploymentStacksWhatIfResultsAtManagementGroups()` was added
* `deploymentStacksWhatIfResultsAtResourceGroups()` was added
* `deploymentStacksWhatIfResultsAtSubscriptions()` was added

#### `models.DeploymentStackValidateProperties` was modified

* `validationLevel()` was added
* `deploymentExtensions()` was added

#### `models.DeploymentParameter` was modified

* `expression()` was added
* `withExpression(java.lang.String)` was added

#### `models.ManagedResourceReference` was modified

* `identifiers()` was added
* `apiVersion()` was added
* `type()` was added
* `extension()` was added

#### `models.ResourceReference` was modified

* `apiVersion()` was added
* `extension()` was added
* `identifiers()` was added
* `type()` was added

## 1.0.0 (2025-07-10)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK. DeploymentStacks Client. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- The first stable release for the azure-resourcemanager-resources-deploymentstacks Java SDK.

## 1.0.0-beta.1 (2025-06-06)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK. DeploymentStacks Client. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resources-deploymentstacks Java SDK.
