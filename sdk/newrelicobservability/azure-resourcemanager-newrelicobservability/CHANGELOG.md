# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
