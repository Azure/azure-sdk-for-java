# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2021-12-08)

- Azure Resource Manager SourceControlConfiguration client library for Java. This package contains Microsoft Azure SDK for SourceControlConfiguration Management SDK. KubernetesConfiguration Client. Package tag package-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.HelmOperatorProperties` was removed

* `models.ProvisioningStateType` was removed

* `models.MessageLevelType` was removed

* `models.OperatorType` was removed

* `models.SourceControlConfigurationsClusterRp` was removed

* `models.SourceControlConfigurations` was removed

* `models.ComplianceStatus` was removed

* `models.SourceControlConfigurationList` was removed

* `models.OperatorScopeType` was removed

* `models.SourceControlConfigurationsClusterResourceName` was removed

* `models.ComplianceStateType` was removed

* `models.SourceControlConfiguration` was removed

#### `SourceControlConfigurationManager` was modified

* `sourceControlConfigurations()` was removed

### Features Added

* `models.ScopeCluster` was added

* `models.ExtensionsClusterRp` was added

* `models.ExtensionPropertiesAksAssignedIdentity` was added

* `models.Identity` was added

* `models.ProvisioningState` was added

* `models.OperationStatusResult` was added

* `models.ResourceIdentityType` was added

* `models.PatchExtension` was added

* `models.ScopeNamespace` was added

* `models.Scope` was added

* `models.Extension` was added

* `models.Extensions` was added

* `models.OperationStatusList` was added

* `models.ExtensionsList` was added

* `models.ExtensionStatus` was added

* `models.OperationStatus` was added

* `models.LevelType` was added

* `models.ExtensionsClusterResourceName` was added

#### `models.ResourceProviderOperation` was modified

* `origin()` was added

#### `SourceControlConfigurationManager` was modified

* `extensions()` was added
* `operationStatus()` was added

#### `SourceControlConfigurationManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager SourceControlConfiguration client library for Java. This package contains Microsoft Azure SDK for SourceControlConfiguration Management SDK. KubernetesConfiguration Client. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

