# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-04-15)

- Azure Resource Manager FrontDoor client library for Java. This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ScrubbingRuleEntryMatchOperator` was added

* `models.GroupByVariable` was added

* `models.ScrubbingRuleEntryState` was added

* `models.WebApplicationFirewallScrubbingState` was added

* `models.WebApplicationFirewallScrubbingRules` was added

* `models.ScrubbingRuleEntryMatchVariable` was added

* `models.VariableName` was added

#### `models.RedirectConfiguration` was modified

* `odataType()` was added

#### `models.CustomRule` was modified

* `groupBy()` was added
* `withGroupBy(java.util.List)` was added

#### `models.RouteConfiguration` was modified

* `odataType()` was added

#### `models.PolicySettings` was modified

* `javascriptChallengeExpirationInMinutes()` was added
* `withJavascriptChallengeExpirationInMinutes(java.lang.Integer)` was added
* `withState(models.WebApplicationFirewallScrubbingState)` was added
* `withScrubbingRules(java.util.List)` was added
* `state()` was added
* `scrubbingRules()` was added

#### `models.ForwardingConfiguration` was modified

* `odataType()` was added

## 1.0.0-beta.3 (2023-05-22)

- Azure Resource Manager FrontDoor client library for Java. This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.WebApplicationFirewallPolicy$Update` was modified

* `withEtag(java.lang.String)` was removed
* `withManagedRules(models.ManagedRuleSetList)` was removed
* `withCustomRules(models.CustomRuleList)` was removed
* `withSku(models.Sku)` was removed
* `withPolicySettings(models.PolicySettings)` was removed

### Features Added

* `models.TagsObject` was added

#### `models.Policies` was modified

* `list()` was added
* `list(com.azure.core.util.Context)` was added

#### `models.FrontDoor` was modified

* `extendedProperties()` was added

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager FrontDoor client library for Java. This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2020-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.LoadBalancingSettingsProperties` was removed

* `models.FrontDoorProperties` was removed

* `models.NetworkOperationStatus` was removed

* `models.Error` was removed

* `models.RulesEngineProperties` was removed

* `models.FrontendEndpointProperties` was removed

* `models.RoutingRuleProperties` was removed

* `models.ErrorDetails` was removed

* `models.BackendPoolProperties` was removed

* `models.HealthProbeSettingsProperties` was removed

### Features Added

#### `models.Experiment` was modified

* `resourceGroupName()` was added

#### `FrontDoorManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.FrontDoor` was modified

* `validateCustomDomain(models.ValidateCustomDomainInput)` was added
* `resourceGroupName()` was added
* `validateCustomDomainWithResponse(models.ValidateCustomDomainInput,com.azure.core.util.Context)` was added

#### `models.RulesEngine` was modified

* `resourceGroupName()` was added

#### `models.WebApplicationFirewallPolicy` was modified

* `resourceGroupName()` was added

#### `FrontDoorManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Profile` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-09)

- Azure Resource Manager FrontDoor client library for Java. This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2020-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
