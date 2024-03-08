# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2023-10-23)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CreateResourceSupportedResponse` was added

* `models.MonitoredSubscriptionProperties$UpdateStages` was added

* `models.CreationSupporteds` was added

* `models.Operation` was added

* `models.MonitoredSubscriptionProperties` was added

* `models.MonitoredSubscriptionPropertiesList` was added

* `models.MonitoredSubscriptionProperties$DefinitionStages` was added

* `models.CreateResourceSupportedResponseList` was added

* `models.MonitoredSubscriptions` was added

* `models.MonitoredSubscriptionProperties$Definition` was added

* `models.MonitoredSubscriptionProperties$Update` was added

* `models.CreateResourceSupportedProperties` was added

* `models.SubscriptionList` was added

* `models.MonitoredSubscription` was added

* `models.Status` was added

#### `MicrosoftDatadogManager` was modified

* `creationSupporteds()` was added
* `monitoredSubscriptions()` was added

#### `models.DatadogOrganizationProperties` was modified

* `withName(java.lang.String)` was added
* `withCspm(java.lang.Boolean)` was added
* `withId(java.lang.String)` was added
* `cspm()` was added

#### `models.MonitorUpdateProperties` was modified

* `cspm()` was added
* `withCspm(java.lang.Boolean)` was added

#### `models.MonitoringTagRulesProperties` was modified

* `automuting()` was added
* `withAutomuting(java.lang.Boolean)` was added

## 1.0.0-beta.4 (2023-01-16)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `MicrosoftDatadogManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.DatadogMonitorResource` was modified

* `resourceGroupName()` was added

#### `models.DatadogSingleSignOnResource` was modified

* `resourceGroupName()` was added

#### `models.MonitoringTagRules` was modified

* `resourceGroupName()` was added

#### `MicrosoftDatadogManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.3 (2021-05-31)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.MonitorProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.DatadogSingleSignOnProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.MonitoringTagRulesProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

### New Feature

#### `models.DatadogMonitorResourceUpdateParameters` was modified

* `withSku(models.ResourceSku)` was added
* `sku()` was added

#### `models.DatadogMonitorResource$Update` was modified

* `withSku(models.ResourceSku)` was added

## 1.0.0-beta.2 (2021-03-30)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

#### `models.DatadogMonitorResource` was modified

* `systemData()` was added

#### `models.DatadogSingleSignOnResource` was modified

* `systemData()` was added

#### `models.MonitoringTagRules` was modified

* `systemData()` was added

#### `models.DatadogAgreementResource` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2021-03-08)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2020-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

