# Release History

## 1.1.0 (2026-01-27)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK. The APIs listed in this specification can be used to manage Deployment stack resources through the Azure Resource Manager. Package api-version 2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.UnmanageActionResourceGroupMode` was removed

#### `models.DeploymentStackListResult` was removed

#### `models.DeploymentStackProperties` was removed

#### `models.UnmanageActionManagementGroupMode` was removed

#### `models.AzureResourceBase` was removed

#### `models.UnmanageActionResourceMode` was removed

#### `models.DeploymentStacks` was modified

* `deleteByIdWithResponse(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteAtSubscription(java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteAtManagementGroup(java.lang.String,java.lang.String,models.UnmanageActionResourceMode,models.UnmanageActionResourceGroupMode,models.UnmanageActionManagementGroupMode,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.DeploymentStackTemplateDefinition` was modified

* `java.lang.Object template()` -> `java.util.Map template()`

#### `models.DeploymentStack$Definition` was modified

* `withProperties(models.DeploymentStackProperties)` was removed

#### `models.DeploymentStack` was modified

* `properties()` was removed

#### `models.ResourceReferenceExtended` was modified

* `ResourceReferenceExtended()` was changed to private access
* `validate()` was removed
* `withError(com.azure.core.management.exception.ManagementError)` was removed

#### `models.ManagedResourceReference` was modified

* `ManagedResourceReference()` was changed to private access
* `validate()` was removed
* `withDenyStatus(models.DenyStatusMode)` was removed
* `withStatus(models.ResourceStatusMode)` was removed

#### `models.KeyVaultReference` was modified

* `validate()` was removed

#### `models.DeploymentStack$Update` was modified

* `withProperties(models.DeploymentStackProperties)` was removed

#### `models.ActionOnUnmanage` was modified

* `validate()` was removed

#### `models.DenySettings` was modified

* `validate()` was removed

#### `models.DeploymentStacksTemplateLink` was modified

* `validate()` was removed

#### `models.DeploymentStackValidateProperties` was modified

* `DeploymentStackValidateProperties()` was changed to private access
* `withValidatedResources(java.util.List)` was removed
* `withParameters(java.util.Map)` was removed
* `withTemplateLink(models.DeploymentStacksTemplateLink)` was removed
* `withActionOnUnmanage(models.ActionOnUnmanage)` was removed
* `withCorrelationId(java.lang.String)` was removed
* `validate()` was removed
* `withDeploymentScope(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withDenySettings(models.DenySettings)` was removed

#### `models.DeploymentParameter` was modified

* `java.lang.Object value()` -> `com.azure.core.util.BinaryData value()`
* `validate()` was removed
* `withValue(java.lang.Object)` was removed

#### `models.DeploymentStacksDebugSetting` was modified

* `validate()` was removed

#### `models.KeyVaultParameterReference` was modified

* `validate()` was removed

#### `models.ResourceReference` was modified

* `validate()` was removed

#### `models.DeploymentStacksParametersLink` was modified

* `validate()` was removed

### Features Added

* `models.DeploymentExternalInputDefinition` was added

* `models.DeploymentStacksChangeBase` was added

* `models.DeploymentStacksWhatIfResult` was added

* `models.DeploymentStacksWhatIfResultProperties` was added

* `models.DeploymentExtensionConfigItem` was added

* `models.DeploymentStacksWhatIfResult$UpdateStages` was added

* `models.DeploymentStacksChangeBaseDeploymentStacksManagementStatus` was added

* `models.DeploymentStacksResourcesWithoutDeleteSupportEnum` was added

* `models.DeploymentStacksChangeDeltaRecord` was added

* `models.DeploymentStacksWhatIfPropertyChangeType` was added

* `models.DeploymentStacksWhatIfResultsAtManagementGroups` was added

* `models.DeploymentExtensionConfig` was added

* `models.DeploymentExtension` was added

* `models.DeploymentStacksWhatIfResultsAtSubscriptions` was added

* `models.DeploymentStacksDiagnosticLevel` was added

* `models.DeploymentStacksChangeDeltaDenySettings` was added

* `models.DeploymentStacksWhatIfResourceChange` was added

* `models.DeploymentStacksWhatIfResult$DefinitionStages` was added

* `models.DeploymentStacksWhatIfResultsAtResourceGroups` was added

* `models.DeploymentStacksManagementStatus` was added

* `models.DeploymentExternalInput` was added

* `models.ErrorAdditionalInfo` was added

* `models.DeploymentStacksWhatIfResult$Update` was added

* `models.DeploymentStacksChangeBaseDenyStatusMode` was added

* `models.DeploymentStacksDiagnostic` was added

* `models.ValidationLevel` was added

* `models.DeploymentStacksWhatIfPropertyChange` was added

* `models.DeploymentStacksWhatIfChangeCertainty` was added

* `models.DeploymentStacksWhatIfResult$Definition` was added

* `models.DeploymentStacksWhatIfChange` was added

* `models.DeploymentStacksWhatIfChangeType` was added

#### `models.DeploymentStacks` was modified

* `deleteAtManagementGroup(java.lang.String,java.lang.String,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksResourcesWithoutDeleteSupportEnum,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksResourcesWithoutDeleteSupportEnum,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksResourcesWithoutDeleteSupportEnum,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteAtSubscription(java.lang.String,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksDeleteDetachEnum,models.DeploymentStacksResourcesWithoutDeleteSupportEnum,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.DeploymentStack$Definition` was modified

* `withValidationLevel(models.ValidationLevel)` was added
* `withError(com.azure.core.management.exception.ManagementError)` was added
* `withTemplate(java.util.Map)` was added
* `withTemplateLink(models.DeploymentStacksTemplateLink)` was added
* `withActionOnUnmanage(models.ActionOnUnmanage)` was added
* `withDescription(java.lang.String)` was added
* `withExternalInputDefinitions(java.util.Map)` was added
* `withDeploymentScope(java.lang.String)` was added
* `withParameters(java.util.Map)` was added
* `withDebugSetting(models.DeploymentStacksDebugSetting)` was added
* `withBypassStackOutOfSyncError(java.lang.Boolean)` was added
* `withExternalInputs(java.util.Map)` was added
* `withExtensionConfigs(java.util.Map)` was added
* `withParametersLink(models.DeploymentStacksParametersLink)` was added
* `withDenySettings(models.DenySettings)` was added

#### `models.DeploymentStack` was modified

* `externalInputs()` was added
* `denySettings()` was added
* `bypassStackOutOfSyncError()` was added
* `extensionConfigs()` was added
* `deploymentId()` was added
* `debugSetting()` was added
* `validationLevel()` was added
* `provisioningState()` was added
* `failedResources()` was added
* `template()` was added
* `deploymentScope()` was added
* `deploymentExtensions()` was added
* `outputs()` was added
* `parameters()` was added
* `correlationId()` was added
* `description()` was added
* `templateLink()` was added
* `detachedResources()` was added
* `actionOnUnmanage()` was added
* `parametersLink()` was added
* `error()` was added
* `duration()` was added
* `externalInputDefinitions()` was added
* `deletedResources()` was added
* `resources()` was added

#### `models.ResourceReferenceExtended` was modified

* `identifiers()` was added
* `type()` was added
* `apiVersion()` was added
* `extension()` was added

#### `DeploymentStacksManager` was modified

* `deploymentStacksWhatIfResultsAtManagementGroups()` was added
* `deploymentStacksWhatIfResultsAtResourceGroups()` was added
* `deploymentStacksWhatIfResultsAtSubscriptions()` was added

#### `models.ManagedResourceReference` was modified

* `extension()` was added
* `type()` was added
* `identifiers()` was added
* `apiVersion()` was added

#### `models.DeploymentStack$Update` was modified

* `withParameters(java.util.Map)` was added
* `withDenySettings(models.DenySettings)` was added
* `withDeploymentScope(java.lang.String)` was added
* `withExternalInputDefinitions(java.util.Map)` was added
* `withParametersLink(models.DeploymentStacksParametersLink)` was added
* `withError(com.azure.core.management.exception.ManagementError)` was added
* `withTemplate(java.util.Map)` was added
* `withTemplateLink(models.DeploymentStacksTemplateLink)` was added
* `withActionOnUnmanage(models.ActionOnUnmanage)` was added
* `withBypassStackOutOfSyncError(java.lang.Boolean)` was added
* `withDescription(java.lang.String)` was added
* `withValidationLevel(models.ValidationLevel)` was added
* `withExternalInputs(java.util.Map)` was added
* `withExtensionConfigs(java.util.Map)` was added
* `withDebugSetting(models.DeploymentStacksDebugSetting)` was added

#### `models.ActionOnUnmanage` was modified

* `withResourcesWithoutDeleteSupport(models.DeploymentStacksResourcesWithoutDeleteSupportEnum)` was added
* `resourcesWithoutDeleteSupport()` was added

#### `models.DeploymentStackValidateProperties` was modified

* `validationLevel()` was added
* `deploymentExtensions()` was added

#### `models.DeploymentParameter` was modified

* `withValue(com.azure.core.util.BinaryData)` was added
* `withExpression(java.lang.String)` was added
* `expression()` was added

#### `models.ResourceReference` was modified

* `type()` was added
* `identifiers()` was added
* `extension()` was added
* `apiVersion()` was added

## 1.0.0 (2025-07-10)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK. DeploymentStacks Client. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- The first stable release for the azure-resourcemanager-resources-deploymentstacks Java SDK.

## 1.0.0-beta.1 (2025-06-06)

- Azure Resource Manager Deployment Stacks client library for Java. This package contains Microsoft Azure SDK for Deployment Stacks Management SDK. DeploymentStacks Client. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resources-deploymentstacks Java SDK.
