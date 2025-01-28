# Release History

## 1.1.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.3 (2025-01-22)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. This Swagger is for Azure Group Quota using GroupQuota Entity. Package tag package-2024-12-18-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GroupingId` was removed

#### `models.EnforcementState` was removed

#### `models.GroupQuotasEnforcementResponse` was removed

#### `models.ResourceUsages` was removed

#### `models.AdditionalAttributes` was removed

#### `models.GroupingIdType` was removed

#### `models.GroupQuotasEnforcementListResponse` was removed

#### `models.EnvironmentType` was removed

#### `models.GroupQuotasEnforcementResponseProperties` was removed

#### `models.GroupQuotaLocationSettings` was removed

#### `models.ResourceUsageList` was removed

#### `models.AdditionalAttributesPatch` was removed

#### `models.GroupQuotaUsages` was removed

#### `models.GroupQuotaUsagesBase` was removed

#### `models.GroupQuotasEntityBase` was modified

* `withAdditionalAttributes(models.AdditionalAttributes)` was removed
* `additionalAttributes()` was removed

#### `models.GroupQuotasEntityPatch` was modified

* `models.GroupQuotasEntityBasePatch properties()` -> `models.GroupQuotasEntityPatchProperties properties()`
* `withProperties(models.GroupQuotasEntityBasePatch)` was removed

#### `models.GroupQuotaLimits` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `com.azure.core.http.rest.PagedIterable list(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.GroupQuotaLimitList list(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.GroupQuotaLimitList` was modified

* `value()` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `nextLink()` was removed
* `withValue(java.util.List)` was removed
* `validate()` was removed
* `fromJson(com.azure.json.JsonReader)` was removed

#### `models.GroupQuotasEntity` was modified

* `models.GroupQuotasEntityBase properties()` -> `models.GroupQuotasEntityProperties properties()`

#### `models.GroupQuotasEntityBasePatch` was modified

* `withAdditionalAttributes(models.AdditionalAttributesPatch)` was removed
* `additionalAttributes()` was removed

#### `models.SubscriptionQuotaDetails` was modified

* `region()` was removed
* `withRegion(java.lang.String)` was removed

#### `models.SubscriptionQuotaAllocations` was modified

* `innerModel()` was removed
* `name()` was removed
* `type()` was removed
* `id()` was removed
* `systemData()` was removed
* `models.SubscriptionQuotaDetails properties()` -> `models.SubscriptionQuotaAllocationsProperties properties()`

#### `models.SubscriptionQuotaAllocationsList` was modified

* `value()` was removed
* `validate()` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `withValue(java.util.List)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `nextLink()` was removed

#### `QuotaManager` was modified

* `groupQuotaLocationSettings()` was removed
* `groupQuotaUsages()` was removed

#### `models.GroupQuotaSubscriptionAllocationRequests` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.QuotaAllocationRequestStatusInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.QuotaAllocationRequestStatusInner)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.QuotaAllocationRequestStatusInner)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.QuotaAllocationRequestStatusInner,com.azure.core.util.Context)` was removed

#### `models.GroupQuotaLimitsRequests` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.SubmittedResourceRequestStatusInner,com.azure.core.util.Context)` was removed
* `models.SubmittedResourceRequestStatus update(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.GroupQuotaLimitList update(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.SubmittedResourceRequestStatusInner,com.azure.core.util.Context)` was removed

#### `models.GroupQuotaLimit` was modified

* `type()` was removed
* `innerModel()` was removed
* `name()` was removed
* `systemData()` was removed
* `id()` was removed
* `models.GroupQuotaDetails properties()` -> `models.GroupQuotaLimitProperties properties()`

#### `models.GroupQuotaDetails` was modified

* `withRegion(java.lang.String)` was removed
* `region()` was removed

#### `models.GroupQuotaSubscriptionAllocations` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.SubscriptionQuotaAllocationsProperties` was added

* `models.GroupQuotasEntityPatchProperties` was added

* `models.GroupQuotasEntityProperties` was added

* `models.GroupQuotaLimitProperties` was added

* `models.GroupQuotaLimitListProperties` was added

* `models.SubscriptionQuotaAllocationsListProperties` was added

#### `models.GroupQuotasEntityPatch` was modified

* `withProperties(models.GroupQuotasEntityPatchProperties)` was added

#### `models.GroupQuotaLimits` was modified

* `listWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.GroupQuotaLimitList` was modified

* `systemData()` was added
* `innerModel()` was added
* `id()` was added
* `name()` was added
* `type()` was added
* `properties()` was added

#### `models.SubscriptionQuotaDetails` was modified

* `resourceName()` was added
* `withResourceName(java.lang.String)` was added

#### `models.SubscriptionQuotaAllocations` was modified

* `validate()` was added
* `withProperties(models.SubscriptionQuotaAllocationsProperties)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GroupQuotaSubscriptionIdProperties` was modified

* `withSubscriptionId(java.lang.String)` was added

#### `models.SubscriptionQuotaAllocationsList` was modified

* `properties()` was added
* `innerModel()` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `id()` was added

#### `models.GroupQuotaSubscriptionAllocationRequests` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.SubscriptionQuotaAllocationsListInner,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.SubscriptionQuotaAllocationsListInner)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.GroupQuotaLimitsRequests` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.GroupQuotaLimitListInner,com.azure.core.util.Context)` was added

#### `models.GroupQuotaLimit` was modified

* `validate()` was added
* `withProperties(models.GroupQuotaLimitProperties)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GroupQuotaDetails` was modified

* `withResourceName(java.lang.String)` was added
* `resourceName()` was added

#### `models.GroupQuotaSubscriptionAllocations` was modified

* `listWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

## 1.1.0-beta.2 (2024-12-04)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. This Swagger is for Azure Group Quota using GroupQuota Entity. Package tag package-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.GroupQuotasEntityPatch` was modified

* `type()` was added
* `name()` was added
* `id()` was added

## 1.1.0-beta.1 (2024-04-24)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. This Swagger is for Azure Group Quota using GroupQuota Entity. Package tag package-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.GroupQuotaRequestBase` was added

* `models.GroupingId` was added

* `models.GroupQuotasEntityBase` was added

* `models.EnforcementState` was added

* `models.GroupQuotasEntityPatch` was added

* `models.GroupQuotasEnforcementResponse` was added

* `models.RequestState` was added

* `models.GroupQuotaLimits` was added

* `models.ResourceUsages` was added

* `models.AdditionalAttributes` was added

* `models.QuotaAllocationRequestBase` was added

* `models.GroupQuotaSubscriptions` was added

* `models.QuotaAllocationRequestStatusList` was added

* `models.AllocatedQuotaToSubscriptionList` was added

* `models.SubmittedResourceRequestStatusProperties` was added

* `models.SubmittedResourceRequestStatus` was added

* `models.SubmittedResourceRequestStatusList` was added

* `models.GroupQuotaList` was added

* `models.GroupingIdType` was added

* `models.GroupQuotasEnforcementListResponse` was added

* `models.EnvironmentType` was added

* `models.GroupQuotaSubscriptionRequestStatusProperties` was added

* `models.GroupQuotasEnforcementResponseProperties` was added

* `models.GroupQuotaLimitList` was added

* `models.GroupQuotaSubscriptionRequestStatusList` was added

* `models.GroupQuotaSubscriptionIdList` was added

* `models.GroupQuotaLocationSettings` was added

* `models.GroupQuotasEntity` was added

* `models.ResourceUsageList` was added

* `models.AdditionalAttributesPatch` was added

* `models.GroupQuotasEntityBasePatch` was added

* `models.SubscriptionQuotaDetails` was added

* `models.GroupQuotas` was added

* `models.GroupQuotaSubscriptionRequests` was added

* `models.GroupQuotaSubscriptionRequestStatus` was added

* `models.SubscriptionQuotaAllocations` was added

* `models.GroupQuotaSubscriptionIdProperties` was added

* `models.SubscriptionQuotaAllocationsList` was added

* `models.GroupQuotaSubscriptionAllocationRequests` was added

* `models.QuotaAllocationRequestStatus` was added

* `models.GroupQuotaUsages` was added

* `models.LroResponseProperties` was added

* `models.LroResponse` was added

* `models.GroupQuotaUsagesBase` was added

* `models.GroupQuotaLimitsRequests` was added

* `models.GroupQuotaLimit` was added

* `models.GroupQuotaDetails` was added

* `models.GroupQuotaSubscriptionAllocations` was added

* `models.AllocatedToSubscription` was added

* `models.GroupQuotaSubscriptionId` was added

#### `models.LimitJsonObject` was modified

* `limitObjectType()` was added

#### `QuotaManager` was modified

* `groupQuotaLimitsRequests()` was added
* `groupQuotaLocationSettings()` was added
* `groupQuotaLimits()` was added
* `groupQuotaSubscriptionAllocationRequests()` was added
* `groupQuotas()` was added
* `groupQuotaUsages()` was added
* `groupQuotaSubscriptionAllocations()` was added
* `groupQuotaSubscriptionRequests()` was added
* `groupQuotaSubscriptions()` was added

#### `models.LimitObject` was modified

* `limitObjectType()` was added

## 1.0.0 (2023-11-21)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.3 (2023-04-17)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `QuotaManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `QuotaManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.2 (2021-11-11)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2021-03-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Operations` was removed

* `models.LimitValue` was removed

#### `QuotaManager` was modified

* `operations()` was removed

#### `models.LimitObject` was modified

* `withLimitObjectType(models.LimitType)` was removed
* `limitObjectType()` was removed

### Features Added

* `models.QuotaOperations` was added

#### `QuotaManager` was modified

* `quotaOperations()` was added

## 1.0.0-beta.1 (2021-09-13)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2021-03-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
