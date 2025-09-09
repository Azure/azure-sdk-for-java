# Release History

## 1.1.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.2 (2025-09-09)

### Other Changes

Please note, this package has been deprecated and will no longer be maintained after 03/08/2026. We encourage you to upgrade to the replacement package, azure-resourcemanager-playwright, to continue receiving updates. Refer to the migration guide https://aka.ms/mpt/migration-guidance for guidance on upgrading. Refer to our deprecation policy (https://aka.ms/azsdk/support-policies) for more details.

## 1.1.0-beta.1 (2025-09-08)

### Other Changes

Please note, this package has been deprecated and will no longer be maintained after 02/09/2026. We encourage you to upgrade to the replacement package, azure-resourcemanager-playwright, to continue receiving updates. Refer to the migration guide https://aka.ms/mpt/migration-guidance for guidance on upgrading. Refer to our deprecation policy (https://aka.ms/azsdk/support-policies) for more details.

## 1.0.0 (2024-12-16)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Microsoft.AzurePlaywrightService Resource Provider Management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AccountListResult` was removed

#### `models.OperationListResult` was removed

#### `models.QuotaListResult` was removed

#### `models.QuotaProperties` was modified

* `withFreeTrial(models.FreeTrialProperties)` was removed

#### `models.FreeTrialProperties` was modified

* `usedValue()` was removed
* `expiryAt()` was removed
* `allocatedValue()` was removed
* `createdAt()` was removed
* `percentageUsed()` was removed

### Features Added

* `models.CheckNameAvailabilityResponse` was added

* `implementation.models.AccountListResult` was added

* `implementation.models.OperationListResult` was added

* `models.CheckNameAvailabilityReason` was added

* `implementation.models.AccountQuotaListResult` was added

* `models.AccountFreeTrialProperties` was added

* `models.AccountQuotaProperties` was added

* `models.CheckNameAvailabilityRequest` was added

* `implementation.models.QuotaListResult` was added

* `models.AccountQuota` was added

* `models.OfferingType` was added

* `models.AccountQuotas` was added

#### `PlaywrightTestingManager` was modified

* `accountQuotas()` was added

#### `models.QuotaProperties` was modified

* `offeringType()` was added

#### `models.AccountProperties` was modified

* `localAuth()` was added
* `withLocalAuth(models.EnablementStatus)` was added

#### `models.AccountUpdateProperties` was modified

* `localAuth()` was added
* `withLocalAuth(models.EnablementStatus)` was added

#### `models.Accounts` was modified

* `checkNameAvailabilityWithResponse(models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added
* `checkNameAvailability(models.CheckNameAvailabilityRequest)` was added

## 1.0.0-beta.3 (2024-12-03)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Azure Playwright testing management service. Package tag package-2023-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0-beta.2 (2024-02-27)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Azure Playwright testing management service. Package tag package-2023-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Account$Definition` was modified

* `withScalableExecution(models.EnablementStatus)` was removed
* `withReporting(models.EnablementStatus)` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed

#### `models.Account$Update` was modified

* `withReporting(models.EnablementStatus)` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed
* `withScalableExecution(models.EnablementStatus)` was removed

#### `models.AccountUpdate` was modified

* `reporting()` was removed
* `withScalableExecution(models.EnablementStatus)` was removed
* `withReporting(models.EnablementStatus)` was removed
* `scalableExecution()` was removed
* `regionalAffinity()` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed

#### `models.Quota` was modified

* `provisioningState()` was removed
* `freeTrial()` was removed

#### `models.Account` was modified

* `reporting()` was removed
* `dashboardUri()` was removed
* `provisioningState()` was removed
* `regionalAffinity()` was removed
* `scalableExecution()` was removed

### Features Added

* `models.QuotaProperties` was added

* `models.AccountProperties` was added

* `models.AccountUpdateProperties` was added

#### `models.Account$Definition` was modified

* `withProperties(models.AccountProperties)` was added

#### `models.Account$Update` was modified

* `withProperties(models.AccountUpdateProperties)` was added

#### `models.AccountUpdate` was modified

* `properties()` was added
* `withProperties(models.AccountUpdateProperties)` was added

#### `models.Quota` was modified

* `properties()` was added

#### `models.Account` was modified

* `properties()` was added

## 1.0.0-beta.1 (2023-09-27)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Azure Playwright testing management service. Package tag package-2023-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
