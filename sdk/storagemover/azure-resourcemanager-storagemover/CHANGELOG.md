# Release History

## 1.4.0-beta.1 (2025-06-27)

- Azure Resource Manager StorageMover client library for Java. This package contains Microsoft Azure SDK for StorageMover Management SDK. The Azure Storage Mover REST API. Package api-version 2024-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.UploadLimit` was removed

#### `models.JobRunList` was removed

#### `models.AgentList` was removed

#### `models.Recurrence` was removed

#### `models.WeeklyRecurrence` was removed

#### `models.JobDefinitionList` was removed

#### `models.StorageMoverList` was removed

#### `models.OperationListResult` was removed

#### `models.EndpointList` was removed

#### `models.ProjectList` was removed

#### `models.JobDefinitions` was modified

* `startJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `startJobWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `stopJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `stopJobWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.JobDefinition` was modified

* `startJobWithResponse(com.azure.core.util.Context)` was removed
* `stopJob()` was removed
* `startJob()` was removed
* `stopJobWithResponse(com.azure.core.util.Context)` was removed

#### `models.JobRunError` was modified

* `withMessage(java.lang.String)` was removed
* `withTarget(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

#### `models.JobRun` was modified

* `java.lang.Object targetProperties()` -> `com.azure.core.util.BinaryData targetProperties()`
* `java.lang.Object jobDefinitionProperties()` -> `com.azure.core.util.BinaryData jobDefinitionProperties()`
* `java.lang.Object sourceProperties()` -> `com.azure.core.util.BinaryData sourceProperties()`

#### `models.UploadLimitWeeklyRecurrence` was modified

* `withLimitInMbps(int)` was removed
* `withDays(java.util.List)` was removed
* `withEndTime(models.Time)` was removed
* `withStartTime(models.Time)` was removed
* `int limitInMbps()` -> `java.lang.Integer limitInMbps()`

#### `models.Agent` was modified

* `localIpAddress()` was removed

#### `models.AgentPropertiesErrorDetails` was modified

* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

### Features Added

* `implementation.models.ProjectList` was added

* `implementation.models.EndpointList` was added

* `implementation.models.JobRunList` was added

* `models.StopJobRequestContentType` was added

* `models.StartJobRequestContentType` was added

* `implementation.models.StorageMoverList` was added

* `implementation.models.AgentList` was added

* `implementation.models.JobDefinitionList` was added

* `implementation.models.OperationListResult` was added

#### `models.JobDefinitions` was modified

* `startJobWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.StartJobRequestContentType,com.azure.core.util.Context)` was added
* `stopJobWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.StopJobRequestContentType,com.azure.core.util.Context)` was added
* `startJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.StartJobRequestContentType)` was added
* `stopJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.StopJobRequestContentType)` was added

#### `models.Minute` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UploadLimitWeeklyRecurrence` was modified

* `endTime()` was added
* `startTime()` was added
* `days()` was added
* `withLimitInMbps(java.lang.Integer)` was added

#### `models.Agent` was modified

* `localIPAddress()` was added

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
