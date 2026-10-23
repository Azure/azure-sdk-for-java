# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-10-21)

- Azure Resource Manager Red Hat OpenShift Hosted Control Planes client library for Java. This package contains Microsoft Azure SDK for Red Hat OpenShift Hosted Control Planes Management SDK.  Package api-version 2026-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceStatus` was removed

#### `models.HcpOpenShiftClusterProperties` was modified

* `models.ResourceStatus status()` -> `models.ClusterResourceStatus status()`

#### `models.NodePoolProperties` was modified

* `models.ResourceStatus status()` -> `models.NodePoolResourceStatus status()`

#### `models.ExternalAuthProperties` was modified

* `models.ResourceStatus status()` -> `models.ExternalAuthResourceStatus status()`

### Features Added

* `models.ClusterActiveVersion` was added

* `models.KmsKeyVaultType` was added

* `models.NodePoolActiveVersion` was added

* `models.ContainerRegistryProfile` was added

* `models.ExternalAuthResourceStatus` was added

* `models.ClusterResourceStatus` was added

* `models.NodePoolResourceStatus` was added

#### `models.KmsEncryptionProfile` was modified

* `keyVaultType()` was added
* `withKeyVaultType(models.KmsKeyVaultType)` was added

#### `models.PlatformProfile` was modified

* `withContainerRegistry(models.ContainerRegistryProfile)` was added
* `containerRegistry()` was added

## 1.0.0-beta.1 (2026-08-19)

- Azure Resource Manager Red Hat OpenShift Hosted Control Planes client library for Java. This package contains Microsoft Azure SDK for Red Hat OpenShift Hosted Control Planes Management SDK.  Package api-version 2026-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-redhatopenshifthcp Java SDK.

