# Release History

## 1.5.0 (2026-03-17)

- Azure Resource Manager Storage Mover client library for Java. This package contains Microsoft Azure SDK for Storage Mover Management SDK. The Azure Storage Mover REST API. Package api-version 2025-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.S3WithHmacEndpointProperties` was added

* `models.Connection$UpdateStages` was added

* `models.Connection$DefinitionStages` was added

* `models.Connection$Update` was added

* `models.TriggerType` was added

* `models.Connection` was added

* `models.AzureKeyVaultS3WithHmacCredentials` was added

* `models.S3WithHmacEndpointUpdateProperties` was added

* `models.Frequency` was added

* `models.S3WithHmacSourceType` was added

* `models.ConnectionProperties` was added

* `models.EndpointKind` was added

* `models.ScheduleInfo` was added

* `models.ConnectionStatus` was added

* `models.JobRunWarning` was added

* `models.Connection$Definition` was added

* `models.Connections` was added

* `models.DataIntegrityValidation` was added

#### `StorageMoverManager` was modified

* `connections()` was added

#### `models.SmbMountEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

#### `models.EndpointBaseProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added
* `endpointKind()` was added

#### `models.AzureStorageNfsFileShareEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

#### `models.AzureMultiCloudConnectorEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

#### `models.AzureStorageSmbFileShareEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

#### `models.JobDefinitionUpdateParameters` was modified

* `dataIntegrityValidation()` was added
* `withDataIntegrityValidation(models.DataIntegrityValidation)` was added
* `connections()` was added
* `withConnections(java.util.List)` was added

#### `models.AzureStorageBlobContainerEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

#### `models.JobDefinition` was modified

* `connections()` was added
* `dataIntegrityValidation()` was added
* `schedule()` was added
* `preservePermissions()` was added

#### `models.JobDefinition$Update` was modified

* `withDataIntegrityValidation(models.DataIntegrityValidation)` was added
* `withConnections(java.util.List)` was added

#### `models.JobRun` was modified

* `warnings()` was added
* `scheduledExecutionTime()` was added
* `triggerType()` was added

#### `models.JobDefinition$Definition` was modified

* `withDataIntegrityValidation(models.DataIntegrityValidation)` was added
* `withPreservePermissions(java.lang.Boolean)` was added
* `withConnections(java.util.List)` was added
* `withSchedule(models.ScheduleInfo)` was added

#### `models.EndpointType` was modified

* `S3WITH_HMAC` was added

#### `models.CredentialType` was modified

* `AZURE_KEY_VAULT_S3WITH_HMAC` was added

#### `models.NfsMountEndpointProperties` was modified

* `withEndpointKind(models.EndpointKind)` was added

## 1.4.0 (2025-08-29)

- Azure Resource Manager Storage Mover client library for Java. This package contains Microsoft Azure SDK for Storage Mover Management SDK. The Azure Storage Mover REST API. Package api-version 2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.UploadLimit` was removed

#### `models.JobRunList` was removed

#### `models.AgentList` was removed

#### `models.JobDefinitionList` was removed

#### `models.StorageMoverList` was removed

#### `models.OperationListResult` was removed

#### `models.EndpointList` was removed

#### `models.ProjectList` was removed

#### `models.JobRunError` was modified

* `withCode(java.lang.String)` was removed
* `withTarget(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.AgentPropertiesErrorDetails` was modified

* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

### Features Added

* `models.UserAssignedIdentity` was added

* `models.AzureMultiCloudConnectorEndpointUpdateProperties` was added

* `models.SourceEndpointProperties` was added

* `models.TargetEndpointProperties` was added

* `models.AzureStorageNfsFileShareEndpointProperties` was added

* `models.AzureMultiCloudConnectorEndpointProperties` was added

* `models.JobDefinitionPropertiesSourceTargetMap` was added

* `models.ManagedServiceIdentity` was added

* `models.SourceTargetMap` was added

* `models.ManagedServiceIdentityType` was added

* `models.TargetEndpoint` was added

* `models.AzureStorageNfsFileShareEndpointUpdateProperties` was added

* `models.JobType` was added

* `models.SourceEndpoint` was added

#### `models.EndpointBaseUpdateParameters` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Minute` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Endpoint$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Endpoint$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.JobDefinition` was modified

* `jobType()` was added
* `sourceTargetMap()` was added

#### `models.Endpoint` was modified

* `identity()` was added

#### `models.JobDefinition$Definition` was modified

* `withJobType(models.JobType)` was added
* `withSourceTargetMap(models.JobDefinitionPropertiesSourceTargetMap)` was added

## 1.3.0 (2024-12-23)

- Azure Resource Manager Storage Mover client library for Java. This package contains Microsoft Azure SDK for Storage Mover Management SDK. The Azure Storage Mover REST API. Package tag package-2024-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `StorageMoverManager` was modified

* `fluent.StorageMoverClient serviceClient()` -> `fluent.StorageMoverManagementClient serviceClient()`

#### `models.Minute` was modified

* `fromInt(int)` was removed

### Features Added

#### `models.Minute` was modified

* `getValue()` was added
* `hashCode()` was added
* `equals(java.lang.Object)` was added
* `toString()` was added
* `fromValue(java.lang.Integer)` was added

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
