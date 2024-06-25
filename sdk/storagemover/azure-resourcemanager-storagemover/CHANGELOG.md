# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2024-06-24)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package tag package-2024-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UploadLimit` was added

* `models.DayOfWeek` was added

* `models.Recurrence` was added

* `models.Minute` was added

* `models.WeeklyRecurrence` was added

* `models.Time` was added

* `models.UploadLimitWeeklyRecurrence` was added

* `models.UploadLimitSchedule` was added

#### `models.Credentials` was modified

* `type()` was added

#### `models.SmbMountEndpointProperties` was modified

* `endpointType()` was added

#### `models.Agent$Definition` was modified

* `withUploadLimitSchedule(models.UploadLimitSchedule)` was added

#### `models.NfsMountEndpointUpdateProperties` was modified

* `endpointType()` was added

#### `models.AzureKeyVaultSmbCredentials` was modified

* `type()` was added

#### `models.EndpointBaseProperties` was modified

* `endpointType()` was added

#### `models.AzureStorageSmbFileShareEndpointProperties` was modified

* `endpointType()` was added

#### `models.AzureStorageBlobContainerEndpointProperties` was modified

* `endpointType()` was added

#### `models.EndpointBaseUpdateProperties` was modified

* `endpointType()` was added

#### `models.AgentUpdateParameters` was modified

* `withUploadLimitSchedule(models.UploadLimitSchedule)` was added
* `uploadLimitSchedule()` was added

#### `models.AzureStorageSmbFileShareEndpointUpdateProperties` was modified

* `endpointType()` was added

#### `models.Agent` was modified

* `timeZone()` was added
* `uploadLimitSchedule()` was added

#### `models.AzureStorageBlobContainerEndpointUpdateProperties` was modified

* `endpointType()` was added

#### `models.SmbMountEndpointUpdateProperties` was modified

* `endpointType()` was added

#### `models.Agent$Update` was modified

* `withUploadLimitSchedule(models.UploadLimitSchedule)` was added

#### `models.NfsMountEndpointProperties` was modified

* `endpointType()` was added

## 1.1.0 (2023-10-23)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package tag package-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.Credentials` was added

* `models.SmbMountEndpointProperties` was added

* `models.AzureKeyVaultSmbCredentials` was added

* `models.AzureStorageSmbFileShareEndpointProperties` was added

* `models.AzureStorageSmbFileShareEndpointUpdateProperties` was added

* `models.CredentialType` was added

* `models.SmbMountEndpointUpdateProperties` was added

## 1.1.0-beta.1 (2023-07-21)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package tag package-preview-2023-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.JobDefinition` was modified

* `systemData()` was removed

#### `models.Project` was modified

* `systemData()` was removed

#### `models.Endpoint` was modified

* `systemData()` was removed

#### `models.JobRun` was modified

* `systemData()` was removed

### Features Added

* `models.Credentials` was added

* `models.SmbMountEndpointProperties` was added

* `models.AzureKeyVaultSmbCredentials` was added

* `models.AzureStorageSmbFileShareEndpointProperties` was added

* `models.AzureStorageSmbFileShareEndpointUpdateProperties` was added

* `models.CredentialType` was added

* `models.SmbMountEndpointUpdateProperties` was added

## 1.0.0 (2023-03-07)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package tag package-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-02-07)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package tag package-2022-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
