# Release History

## 1.2.0-beta.1 (2025-08-13)

- Azure Resource Manager Load Test client library for Java. This package contains Microsoft Azure SDK for Load Test Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package api-version 2024-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Origin` was removed

#### `models.LoadTestResourceListResult` was removed

#### `models.LoadTestResourcePatchRequestBody` was removed

#### `models.PagedOutboundEnvironmentEndpoint` was removed

#### `models.Operations` was removed

#### `models.ActionType` was removed

#### `models.Quotas` was removed

#### `models.OperationDisplay` was removed

#### `models.OperationListResult` was removed

#### `models.QuotaResourceListResult` was removed

#### `models.Operation` was removed

#### `models.LoadTests` was removed

#### `LoadTestManager` was modified

* `loadTests()` was removed
* `fluent.LoadTestClient serviceClient()` -> `fluent.LoadTestManagementClient serviceClient()`
* `quotas()` was removed
* `operations()` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.LoadTestResource$Definition` was modified

* `withEncryption(models.EncryptionProperties)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.LoadTestResource$Update` was modified

* `withDescription(java.lang.String)` was removed
* `withEncryption(models.EncryptionProperties)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.LoadTestResource` was modified

* `dataPlaneUri()` was removed
* `description()` was removed
* `encryption()` was removed
* `provisioningState()` was removed

#### `models.QuotaResource` was modified

* `limit()` was removed
* `provisioningState()` was removed
* `usage()` was removed

### Features Added

* `models.ResourceProviders` was added

* `models.LoadTestResourceUpdateProperties` was added

* `models.LoadTestResourceUpdate` was added

* `models.LoadTestProperties` was added

* `models.QuotaResourceProperties` was added

#### `LoadTestManager` was modified

* `resourceProviders()` was added

#### `models.LoadTestResource$Definition` was modified

* `withProperties(models.LoadTestProperties)` was added

#### `models.LoadTestResource$Update` was modified

* `withProperties(models.LoadTestResourceUpdateProperties)` was added

#### `models.LoadTestResource` was modified

* `properties()` was added

#### `models.QuotaResource` was modified

* `properties()` was added

## 1.1.0 (2024-12-12)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.OutboundEnvironmentEndpointCollection` was removed

#### `models.QuotaResourceList` was removed

#### `models.LoadTestResourcePageList` was removed

### Features Added

* `models.LoadTestResourceListResult` was added

* `models.QuotaResourceListResult` was added

* `models.PagedOutboundEnvironmentEndpoint` was added

## 1.0.0 (2023-02-01)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-01-10)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
