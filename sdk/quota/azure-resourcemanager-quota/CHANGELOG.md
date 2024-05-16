# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
