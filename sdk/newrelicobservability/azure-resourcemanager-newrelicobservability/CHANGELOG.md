# Release History

## 1.3.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0-beta.1 (2025-11-24)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2025-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BillingCycle` was removed

#### `models.PlanData` was modified

* `models.BillingCycle billingCycle()` -> `java.lang.String billingCycle()`
* `withBillingCycle(models.BillingCycle)` was removed

### Features Added

* `models.SaaS` was added

* `models.LatestLinkedSaaSResponse` was added

* `models.ActivateSaaSParameterRequest` was added

* `models.ResubscribeProperties` was added

* `models.SaaSData` was added

* `models.SaaSResourceDetailsResponse` was added

#### `models.PlanData` was modified

* `withBillingCycle(java.lang.String)` was added

#### `models.MarketplaceSaaSInfo` was modified

* `withOfferId(java.lang.String)` was added
* `offerId()` was added
* `withPublisherId(java.lang.String)` was added
* `publisherId()` was added

#### `models.NewRelicMonitorResource$Update` was modified

* `withSaaSData(models.SaaSData)` was added

#### `models.NewRelicMonitorResource` was modified

* `resubscribe(models.ResubscribeProperties,com.azure.core.util.Context)` was added
* `saaSData()` was added
* `latestLinkedSaaSWithResponse(com.azure.core.util.Context)` was added
* `linkSaaS(models.SaaSData,com.azure.core.util.Context)` was added
* `resubscribe()` was added
* `refreshIngestionKey()` was added
* `latestLinkedSaaS()` was added
* `linkSaaS(models.SaaSData)` was added
* `refreshIngestionKeyWithResponse(com.azure.core.util.Context)` was added

#### `models.NewRelicMonitorResource$Definition` was modified

* `withSaaSData(models.SaaSData)` was added

#### `models.NewRelicMonitorResourceUpdate` was modified

* `saaSData()` was added
* `withSaaSData(models.SaaSData)` was added

#### `models.MonitoredSubscriptionProperties` was modified

* `systemData()` was added

#### `models.Monitors` was modified

* `refreshIngestionKey(java.lang.String,java.lang.String)` was added
* `linkSaaS(java.lang.String,java.lang.String,models.SaaSData,com.azure.core.util.Context)` was added
* `refreshIngestionKeyWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `resubscribe(java.lang.String,java.lang.String,models.ResubscribeProperties,com.azure.core.util.Context)` was added
* `latestLinkedSaaSWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `latestLinkedSaaS(java.lang.String,java.lang.String)` was added
* `linkSaaS(java.lang.String,java.lang.String,models.SaaSData)` was added
* `resubscribe(java.lang.String,java.lang.String)` was added

#### `NewRelicObservabilityManager` was modified

* `saaS()` was added

## 1.2.0 (2024-12-19)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2024-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.1.0 (2024-03-15)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2024-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ConfigurationName` was added

* `models.MarketplaceSaaSInfo` was added

* `models.MonitoredSubscriptionProperties$UpdateStages` was added

* `models.MonitoredSubscription` was added

* `models.MonitoredSubscriptionProperties$Definition` was added

* `models.BillingInfoResponse` was added

* `models.MonitoredSubscriptionPropertiesList` was added

* `models.ConnectedPartnerResources` was added

* `models.SubscriptionList` was added

* `models.ConnectedPartnerResourcesListResponse` was added

* `models.ConnectedPartnerResourcesListFormat` was added

* `models.BillingInfoes` was added

* `models.PatchOperation` was added

* `models.MonitoredSubscriptionProperties$Update` was added

* `models.Status` was added

* `models.MonitoredSubscriptionProperties$DefinitionStages` was added

* `models.ConnectedPartnerResourceProperties` was added

* `models.MonitoredSubscriptionProperties` was added

* `models.LinkedResource` was added

* `models.PartnerBillingEntity` was added

* `models.MonitoredSubscriptions` was added

* `models.LinkedResourceListResponse` was added

#### `models.NewRelicMonitorResource` was modified

* `saaSAzureSubscriptionStatus()` was added
* `listLinkedResources(com.azure.core.util.Context)` was added
* `listLinkedResources()` was added
* `subscriptionState()` was added

#### `models.NewRelicMonitorResource$Definition` was modified

* `withSaaSAzureSubscriptionStatus(java.lang.String)` was added
* `withSubscriptionState(java.lang.String)` was added

#### `models.Monitors` was modified

* `listLinkedResources(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listLinkedResources(java.lang.String,java.lang.String)` was added

#### `NewRelicObservabilityManager` was modified

* `connectedPartnerResources()` was added
* `monitoredSubscriptions()` was added
* `billingInfoes()` was added

## 1.0.0 (2023-05-18)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2022-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2023-04-18)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2022-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NewRelicMonitorResource` was modified

* `void switchBilling(models.SwitchBillingRequest)` -> `models.NewRelicMonitorResource switchBilling(models.SwitchBillingRequest)`

#### `models.Monitors` was modified

* `void switchBilling(java.lang.String,java.lang.String,models.SwitchBillingRequest)` -> `models.NewRelicMonitorResource switchBilling(java.lang.String,java.lang.String,models.SwitchBillingRequest)`

### Features Added

* `models.MonitorsSwitchBillingResponse` was added

* `models.MonitorsSwitchBillingHeaders` was added

## 1.0.0-beta.1 (2023-03-27)

- Azure Resource Manager NewRelicObservability client library for Java. This package contains Microsoft Azure SDK for NewRelicObservability Management SDK.  Package tag package-2022-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
