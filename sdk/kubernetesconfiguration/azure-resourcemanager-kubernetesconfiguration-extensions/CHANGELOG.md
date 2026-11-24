# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-04-07)

- Azure Resource Manager Extensions client library for Java. This package contains Microsoft Azure SDK for Extensions Management SDK. Use these APIs to create extension resources through ARM, for Kubernetes Clusters. Package api-version 2025-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ExtensionsList` was removed

#### `models.ScopeNamespace` was modified

* `validate()` was removed

#### `models.PatchExtension` was modified

* `validate()` was removed

#### `models.Identity` was modified

* `validate()` was removed

#### `models.Scope` was modified

* `validate()` was removed

#### `models.ScopeCluster` was modified

* `validate()` was removed

#### `models.AksIdentityType` was modified

* `valueOf(java.lang.String)` was removed
* `models.AksIdentityType[] values()` -> `java.util.Collection<com.azure.resourcemanager.kubernetesconfiguration.extensions.models.AksIdentityType> values()`
* `toString()` was removed

#### `models.ExtensionPropertiesAksAssignedIdentity` was modified

* `validate()` was removed

#### `models.ExtensionStatus` was modified

* `validate()` was removed

#### `models.Plan` was modified

* `validate()` was removed

### Features Added

* `models.AccessDetail` was added

* `models.AdditionalDetails` was added

* `models.AutoUpgradeMode` was added

* `models.ManagementDetails` was added

#### `models.PatchExtension` was modified

* `withAutoUpgradeMode(models.AutoUpgradeMode)` was added
* `autoUpgradeMode()` was added

#### `models.Extension` was modified

* `extensionState()` was added
* `additionalDetails()` was added
* `managedBy()` was added
* `managementDetails()` was added
* `autoUpgradeMode()` was added

#### `models.AksIdentityType` was modified

* `AksIdentityType()` was added
* `WORKLOAD` was added

#### `models.ExtensionPropertiesAksAssignedIdentity` was modified

* `resourceId()` was added
* `withObjectId(java.lang.String)` was added
* `clientId()` was added
* `withClientId(java.lang.String)` was added
* `withResourceId(java.lang.String)` was added
* `objectId()` was added

## 1.0.0-beta.1 (2025-05-23)

- Azure Resource Manager Extensions client library for Java. This package contains Microsoft Azure SDK for Extensions Management SDK. KubernetesConfiguration Extensions Client. Package tag package-2024-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-kubernetesconfiguration-extensions Java SDK.
