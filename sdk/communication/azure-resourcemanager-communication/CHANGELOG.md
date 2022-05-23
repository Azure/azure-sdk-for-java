# Release History

## 1.0.0-beta.1 (2022-05-23)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2022-01-24)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationStatus` was removed

* `models.Status` was removed

* `models.OperationStatuses` was removed

#### `models.CommunicationServices` was modified

* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner)` was removed

#### `CommunicationManager` was modified

* `operationStatuses()` was removed

### Features Added

* `models.CommunicationServiceResource$DefinitionStages` was added

* `models.CommunicationServiceResource$Update` was added

* `models.CommunicationServiceResource$UpdateStages` was added

* `models.CommunicationServiceResource$Definition` was added

#### `models.CommunicationServiceResource` was modified

* `name()` was added
* `refresh()` was added
* `update()` was added
* `linkNotificationHubWithResponse(models.LinkNotificationHubParameters,com.azure.core.util.Context)` was added
* `linkNotificationHub()` was added
* `refresh(com.azure.core.util.Context)` was added
* `region()` was added
* `regenerateKey(models.RegenerateKeyParameters)` was added
* `listKeys()` was added
* `id()` was added
* `type()` was added
* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `regenerateKeyWithResponse(models.RegenerateKeyParameters,com.azure.core.util.Context)` was added
* `regionName()` was added

#### `models.CommunicationServices` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added

#### `CommunicationManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0 (2021-04-08)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ErrorAdditionalInfo` was removed

## 1.0.0-beta.1 (2021-03-23)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

