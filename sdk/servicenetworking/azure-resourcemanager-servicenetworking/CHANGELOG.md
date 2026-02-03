# Release History

## 1.2.0-beta.1 (Unreleased)

- Azure Resource Manager Traffic Controller client library for Java. This package contains Microsoft Azure SDK for Traffic Controller Management SDK. Traffic Controller Provider management API. Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.IpAccessRule` was added

* `models.FrontendUpdateProperties` was added

* `models.IpAccessRuleAction` was added

* `models.IpAccessRulesPolicy` was added

* `models.IpAccessRulesSecurityPolicy` was added

#### `models.Frontend$Update` was modified

* `withProperties(models.FrontendUpdateProperties)` was added

#### `models.SecurityPolicyProperties` was modified

* `withIpAccessRulesPolicy(models.IpAccessRulesPolicy)` was added
* `ipAccessRulesPolicy()` was added

#### `models.SecurityPolicyConfigurations` was modified

* `ipAccessRulesSecurityPolicy()` was added
* `withIpAccessRulesSecurityPolicy(models.IpAccessRulesSecurityPolicy)` was added

#### `models.FrontendUpdate` was modified

* `withProperties(models.FrontendUpdateProperties)` was added
* `properties()` was added

#### `models.SecurityPolicyUpdateProperties` was modified

* `ipAccessRulesPolicy()` was added
* `withIpAccessRulesPolicy(models.IpAccessRulesPolicy)` was added

#### `models.FrontendProperties` was modified

* `securityPolicyConfigurations()` was added
* `withSecurityPolicyConfigurations(models.SecurityPolicyConfigurations)` was added

## 1.1.0 (2025-02-06)

- Azure Resource Manager Traffic Controller client library for Java. This package contains Microsoft Azure SDK for Traffic Controller Management SDK. Traffic Controller Provider management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.FrontendListResult` was removed

#### `models.OperationListResult` was removed

#### `models.AssociationListResult` was removed

#### `models.TrafficControllerListResult` was removed

#### `models.ResourceId` was modified

* `withId(java.lang.String)` was removed

## 1.1.0-beta.1 (2024-09-30)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag 2024-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.WafSecurityPolicy` was added

* `models.SecurityPolicyConfigurationsUpdate` was added

* `models.SecurityPolicy$Update` was added

* `models.SecurityPoliciesInterfaces` was added

* `models.SecurityPolicyListResult` was added

* `models.SecurityPolicy$UpdateStages` was added

* `models.SecurityPolicy$DefinitionStages` was added

* `models.TrafficControllerUpdateProperties` was added

* `models.SecurityPolicyProperties` was added

* `models.SecurityPolicyConfigurations` was added

* `models.SecurityPolicyUpdate` was added

* `models.SecurityPolicy` was added

* `models.SecurityPolicy$Definition` was added

* `models.SecurityPolicyUpdateProperties` was added

* `models.WafPolicy` was added

* `models.WafSecurityPolicyUpdate` was added

* `models.WafPolicyUpdate` was added

* `models.PolicyType` was added

#### `models.FrontendListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrafficControllerUpdate` was modified

* `withProperties(models.TrafficControllerUpdateProperties)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `properties()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `TrafficControllerManager` was modified

* `securityPoliciesInterfaces()` was added

#### `models.AssociationUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssociationSubnet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssociationSubnetUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrafficControllerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `securityPolicyConfigurations()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withSecurityPolicyConfigurations(models.SecurityPolicyConfigurations)` was added
* `securityPolicies()` was added

#### `models.FrontendUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssociationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssociationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceId` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrafficController$Update` was modified

* `withProperties(models.TrafficControllerUpdateProperties)` was added

#### `models.TrafficControllerListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AssociationUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FrontendProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0 (2023-11-27)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag package-2023-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Association$Definition` was modified

* `withAssociationType(models.AssociationType)` was removed
* `withSubnet(models.AssociationSubnet)` was removed

#### `models.AssociationUpdate` was modified

* `subnet()` was removed
* `withAssociationType(models.AssociationType)` was removed
* `withSubnet(models.AssociationSubnetUpdate)` was removed
* `associationType()` was removed

#### `models.Association$Update` was modified

* `withSubnet(models.AssociationSubnetUpdate)` was removed
* `withAssociationType(models.AssociationType)` was removed

#### `models.TrafficController` was modified

* `associations()` was removed
* `provisioningState()` was removed
* `frontends()` was removed
* `configurationEndpoints()` was removed

#### `models.Association` was modified

* `subnet()` was removed
* `provisioningState()` was removed
* `associationType()` was removed

#### `models.Frontend` was modified

* `provisioningState()` was removed
* `fqdn()` was removed

### Features Added

* `models.TrafficControllerProperties` was added

* `models.AssociationProperties` was added

* `models.AssociationUpdateProperties` was added

* `models.FrontendProperties` was added

#### `models.Association$Definition` was modified

* `withProperties(models.AssociationProperties)` was added

#### `models.AssociationUpdate` was modified

* `withProperties(models.AssociationUpdateProperties)` was added
* `properties()` was added

#### `models.Association$Update` was modified

* `withProperties(models.AssociationUpdateProperties)` was added

#### `models.TrafficController` was modified

* `properties()` was added

#### `models.Association` was modified

* `properties()` was added

#### `models.Frontend$Definition` was modified

* `withProperties(models.FrontendProperties)` was added

#### `models.Frontend` was modified

* `properties()` was added

#### `models.TrafficController$Definition` was modified

* `withProperties(models.TrafficControllerProperties)` was added

## 1.0.0-beta.2 (2023-05-17)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag package-2023-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.FrontendUpdateProperties` was removed

* `models.FrontendPropertiesIpAddress` was removed

* `models.FrontendMode` was removed

* `models.FrontendIpAddressVersion` was removed

* `models.AssociationUpdateProperties` was removed

#### `models.Frontend$Update` was modified

* `withProperties(models.FrontendUpdateProperties)` was removed

#### `models.AssociationType` was modified

* `models.AssociationType[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.TrafficControllerUpdate` was modified

* `properties()` was removed
* `withProperties(java.lang.Object)` was removed

#### `models.AssociationUpdate` was modified

* `withProperties(models.AssociationUpdateProperties)` was removed
* `properties()` was removed

#### `models.Association$Update` was modified

* `withProperties(models.AssociationUpdateProperties)` was removed

#### `models.Frontend$Definition` was modified

* `withIpAddressVersion(models.FrontendIpAddressVersion)` was removed
* `withPublicIpAddress(models.FrontendPropertiesIpAddress)` was removed
* `withMode(models.FrontendMode)` was removed

#### `models.FrontendUpdate` was modified

* `withProperties(models.FrontendUpdateProperties)` was removed
* `properties()` was removed

#### `models.Frontend` was modified

* `ipAddressVersion()` was removed
* `publicIpAddress()` was removed
* `mode()` was removed

#### `models.TrafficController$Update` was modified

* `withProperties(java.lang.Object)` was removed

### Features Added

* `models.AssociationSubnetUpdate` was added

#### `models.AssociationUpdate` was modified

* `subnet()` was added
* `associationType()` was added
* `withAssociationType(models.AssociationType)` was added
* `withSubnet(models.AssociationSubnetUpdate)` was added

#### `models.Association$Update` was modified

* `withSubnet(models.AssociationSubnetUpdate)` was added
* `withAssociationType(models.AssociationType)` was added

#### `models.Frontend` was modified

* `fqdn()` was added

## 1.0.0-beta.1 (2022-12-22)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag package-2022-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
