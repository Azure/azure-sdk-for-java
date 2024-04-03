# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
