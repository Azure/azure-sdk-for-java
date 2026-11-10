# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-09-28)

- Azure Resource Manager Secrets Store Extension client library for Java. This package contains Microsoft Azure SDK for Secrets Store Extension Management SDK. Microsoft.SecretSyncController resource provider. Package api-version 2026-09-25-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SecretSyncUpdateProperties` was modified

* `kubernetesSecretType()` was removed
* `validate()` was removed
* `withKubernetesSecretType(models.KubernetesSecretType)` was removed

#### `models.AzureKeyVaultSecretProviderClassUpdate` was modified

* `validate()` was removed

#### `models.AzureKeyVaultSecretProviderClassProperties` was modified

* `validate()` was removed

#### `models.ExtendedLocation` was modified

* `validate()` was removed

#### `models.KubernetesSecretObjectMapping` was modified

* `validate()` was removed

#### `models.AzureKeyVaultSecretProviderClassUpdateProperties` was modified

* `validate()` was removed

#### `models.SecretSyncCondition` was modified

* `validate()` was removed

#### `models.SecretSyncUpdate` was modified

* `validate()` was removed

#### `models.SecretSyncProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.SecretSyncStatus` was modified

* `validate()` was removed

### Features Added

* `models.AzureCloudName` was added

#### `models.AzureKeyVaultSecretProviderClassProperties` was modified

* `withCloudName(models.AzureCloudName)` was added
* `cloudName()` was added

#### `models.AzureKeyVaultSecretProviderClassUpdateProperties` was modified

* `withCloudName(models.AzureCloudName)` was added
* `cloudName()` was added

## 1.0.0-beta.1 (2025-05-08)

- Azure Resource Manager Secrets Store Extension client library for Java. This package contains Microsoft Azure SDK for Secrets Store Extension Management SDK. Microsoft.SecretSyncController resource provider. Package api-version 2024-08-21-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-secretsstoreextension Java SDK.
