# Release History

## 1.0.0-beta.1 (2026-01-08)

- Azure Resource Manager Subscription client library for Java. This package contains Microsoft Azure SDK for Subscription Management SDK. The subscription client. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PutAliasResponse` was removed

#### `models.TenantIdDescription` was removed

#### `models.Subscription` was removed

#### `models.PutAliasListResult` was removed

#### `models.SubscriptionState` was removed

#### `models.Tenants` was removed

#### `models.SpendingLimit` was removed

#### `models.SubscriptionListResult` was removed

#### `models.LocationListResult` was removed

#### `models.TenantListResult` was removed

#### `models.PutAliasResponseProperties` was removed

#### `models.Location` was removed

#### `models.Operation` was modified

* `Operation()` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `validate()` was removed
* `withDisplay(models.OperationDisplay)` was removed
* `withName(java.lang.String)` was removed
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `java.lang.String name()` -> `java.lang.String name()`
* `fromJson(com.azure.json.JsonReader)` was removed

#### `models.Subscriptions` was modified

* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listLocations(java.lang.String)` was removed
* `get(java.lang.String)` was removed
* `listLocations(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed
* `list()` was removed

#### `models.Alias` was modified

* `models.PutAliasResponse create(java.lang.String,models.PutAliasRequest)` -> `models.SubscriptionAliasResponse create(java.lang.String,models.PutAliasRequest)`
* `models.PutAliasResponse get(java.lang.String)` -> `models.SubscriptionAliasResponse get(java.lang.String)`
* `models.PutAliasResponse create(java.lang.String,models.PutAliasRequest,com.azure.core.util.Context)` -> `models.SubscriptionAliasResponse create(java.lang.String,models.PutAliasRequest,com.azure.core.util.Context)`
* `models.PutAliasListResult list()` -> `models.SubscriptionAliasListResult list()`

#### `models.Operations` was modified

* `listWithResponse(com.azure.core.util.Context)` was removed
* `models.OperationListResult list()` -> `com.azure.core.http.rest.PagedIterable list()`

#### `models.SubscriptionPolicies` was modified

* `SubscriptionPolicies()` was removed
* `validate()` was removed
* `spendingLimit()` was removed
* `locationPlacementId()` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `quotaId()` was removed
* `fromJson(com.azure.json.JsonReader)` was removed

#### `models.OperationListResult` was modified

* `java.util.List value()` -> `java.util.List value()`
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `innerModel()` was removed

#### `SubscriptionManager` was modified

* `tenants()` was removed

#### `models.SubscriptionOperations` was modified

* `rename(java.lang.String,models.SubscriptionName)` was removed
* `enableWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `renameWithResponse(java.lang.String,models.SubscriptionName,com.azure.core.util.Context)` was removed
* `cancel(java.lang.String)` was removed
* `enable(java.lang.String)` was removed
* `cancelWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.TenantPolicy` was added

* `models.GetTenantPolicyResponse` was added

* `models.BillingAccountPoliciesResponse` was added

* `models.ServiceTenantResponse` was added

* `models.AcceptOwnershipRequest` was added

* `models.SubscriptionAliasResponseProperties` was added

* `models.PutAliasRequestAdditionalProperties` was added

* `models.AcceptOwnershipRequestProperties` was added

* `models.SubscriptionOperationsGetHeaders` was added

* `models.SubscriptionAliasResponse` was added

* `models.SubscriptionAliasListResult` was added

* `models.GetTenantPolicyListResponse` was added

* `models.BillingAccounts` was added

* `models.SubscriptionOperationsGetResponse` was added

* `models.BillingAccountPoliciesResponseProperties` was added

* `models.PutTenantPolicyRequestProperties` was added

* `models.SubscriptionCreationResult` was added

* `models.Provisioning` was added

* `models.AcceptOwnershipStatusResponse` was added

* `models.AcceptOwnership` was added

#### `models.Operation` was modified

* `innerModel()` was added
* `isDataAction()` was added

#### `models.PutAliasRequestProperties` was modified

* `withAdditionalProperties(models.PutAliasRequestAdditionalProperties)` was added
* `additionalProperties()` was added

#### `models.Subscriptions` was modified

* `acceptOwnership(java.lang.String,models.AcceptOwnershipRequest,com.azure.core.util.Context)` was added
* `rename(java.lang.String,models.SubscriptionName)` was added
* `acceptOwnership(java.lang.String,models.AcceptOwnershipRequest)` was added
* `acceptOwnershipStatus(java.lang.String)` was added
* `enable(java.lang.String)` was added
* `cancel(java.lang.String)` was added
* `renameWithResponse(java.lang.String,models.SubscriptionName,com.azure.core.util.Context)` was added
* `enableWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `cancelWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `acceptOwnershipStatusWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.OperationDisplay` was modified

* `description()` was added
* `withDescription(java.lang.String)` was added

#### `models.SubscriptionPolicies` was modified

* `addUpdatePolicyForTenant(models.PutTenantPolicyRequestProperties)` was added
* `getPolicyForTenantWithResponse(com.azure.core.util.Context)` was added
* `getPolicyForTenant()` was added
* `listPolicyForTenant(com.azure.core.util.Context)` was added
* `listPolicyForTenant()` was added
* `addUpdatePolicyForTenantWithResponse(models.PutTenantPolicyRequestProperties,com.azure.core.util.Context)` was added

#### `models.OperationListResult` was modified

* `OperationListResult()` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `validate()` was added
* `withValue(java.util.List)` was added

#### `SubscriptionManager` was modified

* `subscriptionPolicies()` was added
* `billingAccounts()` was added

#### `models.SubscriptionOperations` was modified

* `get(java.lang.String)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager Subscription client library for Java. This package contains Microsoft Azure SDK for Subscription Management SDK. The subscription client. Package tag package-2020-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Subscription client library for Java.

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager Subscription client library for Java. This package contains Microsoft Azure SDK for Subscription Management SDK. The subscription client. Package tag package-2020-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PutAliasRequestProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PutAliasRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SubscriptionPolicies` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SubscriptionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LocationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TenantListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SubscriptionName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PutAliasResponseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2023-01-19)

- Azure Resource Manager Subscription client library for Java. This package contains Microsoft Azure SDK for Subscription Management SDK. The subscription client. Package tag package-2020-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CanceledSubscriptionId` was modified

* `value()` was removed

#### `models.EnabledSubscriptionId` was modified

* `value()` was removed

#### `models.RenamedSubscriptionId` was modified

* `value()` was removed

### Features Added

#### `SubscriptionManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.TenantIdDescription` was modified

* `domains()` was added
* `defaultDomain()` was added
* `countryCode()` was added
* `tenantCategory()` was added
* `displayName()` was added
* `country()` was added
* `tenantType()` was added

#### `models.CanceledSubscriptionId` was modified

* `subscriptionId()` was added

#### `models.Subscription` was modified

* `tags()` was added
* `tenantId()` was added

#### `models.EnabledSubscriptionId` was modified

* `subscriptionId()` was added

#### `models.RenamedSubscriptionId` was modified

* `subscriptionId()` was added

#### `SubscriptionManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-19)

- Azure Resource Manager Subscription client library for Java. This package contains Microsoft Azure SDK for Subscription Management SDK. The subscription client. Package tag package-2020-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
