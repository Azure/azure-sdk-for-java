# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2022-03-24)

- Azure Resource Manager SourceControlConfiguration client library for Java. This package contains Microsoft Azure SDK for SourceControlConfiguration Management SDK. KubernetesConfiguration Client. Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ExtensionsClusterRp` was removed

* `models.ExtensionsClusterResourceName` was removed

#### `models.ExtensionPropertiesAksAssignedIdentity` was modified

* `models.ResourceIdentityType type()` -> `models.AksIdentityType type()`
* `withType(models.ResourceIdentityType)` was removed

#### `models.Extensions` was modified

* `list(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,com.azure.core.util.Context)` was removed
* `create(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,fluent.models.ExtensionInner,com.azure.core.util.Context)` was removed
* `create(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,fluent.models.ExtensionInner)` was removed
* `delete(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `delete(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `list(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String)` was removed
* `update(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,models.PatchExtension)` was removed
* `get(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `update(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,models.PatchExtension,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String)` was removed

#### `models.OperationStatus` was modified

* `get(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,models.ExtensionsClusterRp,models.ExtensionsClusterResourceName,java.lang.String)` was removed

### Features Added

* `models.ProvisioningStateType` was added

* `models.MessageLevelType` was added

* `models.HelmReleasePropertiesDefinition` was added

* `models.ObjectStatusConditionDefinition` was added

* `models.AksIdentityType` was added

* `models.FluxConfigOperationStatus` was added

* `models.ComplianceStatus` was added

* `models.SourceControlConfigurationList` was added

* `models.OperatorScopeType` was added

* `models.ObjectReferenceDefinition` was added

* `models.SourceControlConfiguration` was added

* `models.FluxConfigurations` was added

* `models.HelmOperatorProperties` was added

* `models.FluxConfiguration` was added

* `models.BucketDefinition` was added

* `models.OperatorType` was added

* `models.FluxComplianceState` was added

* `models.SourceControlConfigurations` was added

* `models.KustomizationPatchDefinition` was added

* `models.KustomizationDefinition` was added

* `models.ComplianceStateType` was added

* `models.FluxConfigurationsList` was added

* `models.GitRepositoryPatchDefinition` was added

* `models.BucketPatchDefinition` was added

* `models.SourceKindType` was added

* `models.FluxConfigurationPatch` was added

* `models.RepositoryRefDefinition` was added

* `models.ObjectStatusDefinition` was added

* `models.ScopeType` was added

* `models.GitRepositoryDefinition` was added

#### `models.ExtensionPropertiesAksAssignedIdentity` was modified

* `withType(models.AksIdentityType)` was added

#### `SourceControlConfigurationManager` was modified

* `fluxConfigOperationStatus()` was added
* `fluxConfigurations()` was added
* `sourceControlConfigurations()` was added

#### `models.Extension` was modified

* `installedVersion()` was added

#### `models.Extensions` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PatchExtension,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.ExtensionInner)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PatchExtension)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.ExtensionInner,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.OperationStatus` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

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

