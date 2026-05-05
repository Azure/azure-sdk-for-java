# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-02-26)

- Azure Resource Manager Disconnected Operations client library for Java. This package contains Microsoft Azure SDK for Disconnected Operations Management SDK. Disconnected operations service API. Package api-version 2026-03-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.BenefitPlanStatus` was added

* `models.SystemReboot` was added

* `models.HardwareSetting` was added

* `models.BillingConfiguration` was added

* `models.HardwareSetting$Definition` was added

* `models.HardwareSetting$UpdateStages` was added

* `models.BillingStatus` was added

* `models.HardwareSettingProperties` was added

* `models.BillingPeriod` was added

* `models.AutoRenew` was added

* `models.ImageUpdateProperties` was added

* `models.HardwareSetting$DefinitionStages` was added

* `models.HardwareSetting$Update` was added

* `models.BenefitPlans` was added

* `models.PricingModel` was added

* `models.HardwareSettings` was added

#### `models.DisconnectedOperationProperties` was modified

* `benefitPlans()` was added
* `billingConfiguration()` was added
* `withBillingConfiguration(models.BillingConfiguration)` was added
* `withBenefitPlans(models.BenefitPlans)` was added

#### `models.DisconnectedOperationUpdateProperties` was modified

* `billingConfiguration()` was added
* `withBenefitPlans(models.BenefitPlans)` was added
* `benefitPlans()` was added
* `withBillingConfiguration(models.BillingConfiguration)` was added

#### `DisconnectedOperationsManager` was modified

* `hardwareSettings()` was added

#### `models.ImageDownloadResult` was modified

* `updateProperties()` was added

#### `models.ImageProperties` was modified

* `updateProperties()` was added

#### `models.DisconnectedOperationDeploymentManifest` was modified

* `benefitPlans()` was added
* `billingConfiguration()` was added

## 1.0.0-beta.1 (2025-09-24)

- Azure Resource Manager Disconnected Operations client library for Java. This package contains Microsoft Azure SDK for Disconnected Operations Management SDK. Disconnected operations service API. Package api-version 2025-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-disconnectedoperations Java SDK.
