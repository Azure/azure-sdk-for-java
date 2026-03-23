# Release History

## 1.2.0 (2026-03-23)

- Azure Resource Manager Front Door client library for Java. This package contains Microsoft Azure SDK for Front Door Management SDK. APIs to manage web application firewall rules. Package api-version 2025-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RulesEngineListResult` was removed

#### `models.WebApplicationFirewallPolicyList` was removed

#### `models.FrontDoorListResult` was removed

#### `models.ManagedRuleSetDefinitionList` was removed

#### `models.ExperimentList` was removed

#### `models.FrontendEndpointsListResult` was removed

#### `models.PreconfiguredEndpointList` was removed

#### `models.ProfileList` was removed

#### `models.ValidateCustomDomainInput` was modified

* `validate()` was removed

#### `models.LatencyMetric` was modified

* `LatencyMetric()` was changed to private access
* `validate()` was removed

#### `models.HealthProbeSettingsModel` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityInput` was modified

* `validate()` was removed

#### `models.RoutingRuleUpdateParameters` was modified

* `validate()` was removed

#### `models.FrontendEndpointUpdateParameters` was modified

* `validate()` was removed

#### `models.ManagedRuleExclusion` was modified

* `validate()` was removed

#### `models.RoutingRuleLink` was modified

* `RoutingRuleLink()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed

#### `models.RulesEngineUpdateParameters` was modified

* `validate()` was removed

#### `models.BackendPoolUpdateParameters` was modified

* `validate()` was removed

#### `models.KeyVaultCertificateSourceParametersVault` was modified

* `validate()` was removed

#### `models.HealthProbeSettingsUpdateParameters` was modified

* `validate()` was removed

#### `models.CustomHttpsConfiguration` was modified

* `validate()` was removed

#### `models.ManagedRuleSetList` was modified

* `validate()` was removed

#### `models.FrontendEndpointLink` was modified

* `FrontendEndpointLink()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed

#### `models.ManagedRuleGroupDefinition` was modified

* `ManagedRuleGroupDefinition()` was changed to private access
* `validate()` was removed

#### `models.ProfileUpdateModel` was modified

* `validate()` was removed

#### `models.Backend` was modified

* `validate()` was removed

#### `models.RedirectConfiguration` was modified

* `validate()` was removed

#### `models.GroupByVariable` was modified

* `validate()` was removed

#### `models.RoutingRule` was modified

* `validate()` was removed

#### `models.CustomRuleList` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.ManagedRuleDefinition` was modified

* `ManagedRuleDefinition()` was changed to private access
* `validate()` was removed

#### `models.LoadBalancingSettingsUpdateParameters` was modified

* `validate()` was removed

#### `models.ManagedRuleSet` was modified

* `validate()` was removed

#### `models.TimeseriesDataPoint` was modified

* `TimeseriesDataPoint()` was changed to private access
* `validate()` was removed
* `withDateTimeUtc(java.lang.String)` was removed
* `withValue(java.lang.Float)` was removed

#### `models.FrontendEndpointUpdateParametersWebApplicationFirewallPolicyLink` was modified

* `validate()` was removed

#### `models.CacheConfiguration` was modified

* `validate()` was removed

#### `models.RulesEngineAction` was modified

* `validate()` was removed

#### `models.HeaderAction` was modified

* `validate()` was removed

#### `models.ManagedRuleGroupOverride` was modified

* `validate()` was removed

#### `models.PurgeParameters` was modified

* `validate()` was removed

#### `models.Endpoint` was modified

* `validate()` was removed

#### `models.WebApplicationFirewallScrubbingRules` was modified

* `validate()` was removed

#### `models.BackendPool` was modified

* `validate()` was removed

#### `models.MatchCondition` was modified

* `validate()` was removed

#### `models.CustomRule` was modified

* `validate()` was removed

#### `models.RouteConfiguration` was modified

* `validate()` was removed

#### `models.PolicySettings` was modified

* `validate()` was removed

#### `models.LoadBalancingSettingsModel` was modified

* `validate()` was removed

#### `models.RulesEngineRule` was modified

* `validate()` was removed

#### `models.ExperimentUpdateModel` was modified

* `validate()` was removed

#### `models.ManagedRuleOverride` was modified

* `validate()` was removed

#### `models.RulesEngineMatchCondition` was modified

* `validate()` was removed

#### `models.BackendPoolsSettings` was modified

* `validate()` was removed

#### `models.TagsObject` was modified

* `validate()` was removed

#### `models.SecurityPolicyLink` was modified

* `SecurityPolicyLink()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.ForwardingConfiguration` was modified

* `validate()` was removed

#### `models.FrontDoorUpdateParameters` was modified

* `validate()` was removed

#### `models.RoutingRuleUpdateParametersWebApplicationFirewallPolicyLink` was modified

* `validate()` was removed

### Features Added

* `models.BasicResource` was added

* `models.ResourcewithSettableName` was added

* `models.SensitivityType` was added

* `models.BasicResourceWithSettableIDName` was added

#### `models.ManagedRuleDefinition` was modified

* `defaultSensitivity()` was added

#### `models.ActionType` was modified

* `CAPTCHA` was added

#### `models.PolicySettings` was modified

* `captchaExpirationInMinutes()` was added
* `withCaptchaExpirationInMinutes(java.lang.Integer)` was added

#### `models.Operator` was modified

* `SERVICE_TAG_MATCH` was added

#### `models.ManagedRuleOverride` was modified

* `sensitivity()` was added
* `withSensitivity(models.SensitivityType)` was added

## 1.1.0 (2024-12-19)

- Azure Resource Manager FrontDoor client library for Java. This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

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
