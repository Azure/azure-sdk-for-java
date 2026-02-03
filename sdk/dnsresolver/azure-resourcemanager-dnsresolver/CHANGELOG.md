# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-10-07)

- Azure Resource Manager Dns Resolver client library for Java. This package contains Microsoft Azure SDK for Dns Resolver Management SDK. The DNS Resolver Management Client. Package api-version 2025-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DnsResolverPolicyListResult` was removed

#### `models.VirtualNetworkLinkListResult` was removed

#### `models.VirtualNetworkDnsForwardingRulesetListResult` was removed

#### `models.OutboundEndpointListResult` was removed

#### `models.DnsResolverDomainListResult` was removed

#### `models.DnsSecurityRuleListResult` was removed

#### `models.SubResourceListResult` was removed

#### `models.ForwardingRuleListResult` was removed

#### `models.DnsForwardingRulesetListResult` was removed

#### `models.DnsResolverPolicyVirtualNetworkLinkListResult` was removed

#### `models.DnsResolverListResult` was removed

#### `models.InboundEndpointListResult` was removed

#### `models.VirtualNetworkLinkPatch` was modified

* `validate()` was removed

#### `models.DnsSecurityRuleAction` was modified

* `validate()` was removed

#### `models.DnsResolvers` was modified

* `delete(java.lang.String,java.lang.String)` was removed

#### `models.OutboundEndpointPatch` was modified

* `validate()` was removed

#### `models.DnsForwardingRulesets` was modified

* `delete(java.lang.String,java.lang.String)` was removed

#### `models.DnsResolverPolicies` was modified

* `delete(java.lang.String,java.lang.String)` was removed

#### `models.TargetDnsServer` was modified

* `validate()` was removed

#### `models.IpConfiguration` was modified

* `validate()` was removed

#### `models.DnsSecurityRulePatch` was modified

* `validate()` was removed

#### `models.DnsResolverDomainLists` was modified

* `delete(java.lang.String,java.lang.String)` was removed

#### `models.DnsResolverDomainListPatch` was modified

* `validate()` was removed

#### `models.DnsResolverPolicyPatch` was modified

* `validate()` was removed

#### `models.DnsForwardingRulesetPatch` was modified

* `validate()` was removed

#### `models.DnsResolverDomainListBulk` was modified

* `validate()` was removed

#### `models.InboundEndpointPatch` was modified

* `validate()` was removed

#### `models.DnsResolverPolicyVirtualNetworkLinkPatch` was modified

* `validate()` was removed

#### `models.DnsResolverPatch` was modified

* `validate()` was removed

#### `models.ForwardingRulePatch` was modified

* `validate()` was removed

### Features Added

* `models.ManagedDomainList` was added

#### `models.DnsSecurityRule$Definition` was modified

* `withManagedDomainLists(java.util.List)` was added

#### `models.DnsResolvers` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.DnsForwardingRulesets` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.DnsResolverPolicies` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.DnsSecurityRule$Update` was modified

* `withManagedDomainLists(java.util.List)` was added

#### `models.DnsSecurityRule` was modified

* `managedDomainLists()` was added

#### `models.DnsSecurityRulePatch` was modified

* `managedDomainLists()` was added
* `withManagedDomainLists(java.util.List)` was added

#### `models.DnsResolverDomainLists` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

## 1.1.0 (2025-06-16)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. DNS Resolver Client. Package tag package-2025-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DnsResolverPolicyVirtualNetworkLink` was added

* `models.DnsSecurityRuleAction` was added

* `models.DnsResolverPolicyListResult` was added

* `models.DnsSecurityRule$UpdateStages` was added

* `models.DnsResolverPolicyVirtualNetworkLink$DefinitionStages` was added

* `models.DnsSecurityRule$Definition` was added

* `models.DnsResolverDomainList` was added

* `models.DnsResolverPolicyVirtualNetworkLink$Definition` was added

* `models.DnsResolverDomainListResult` was added

* `models.DnsResolverPolicies` was added

* `models.DnsResolverDomainList$DefinitionStages` was added

* `models.DnsSecurityRule$Update` was added

* `models.DnsResolverPolicyVirtualNetworkLink$Update` was added

* `models.DnsSecurityRuleListResult` was added

* `models.ActionType` was added

* `models.DnsResolverPolicyVirtualNetworkLinks` was added

* `models.DnsSecurityRule` was added

* `models.DnsResolverPolicy$DefinitionStages` was added

* `models.DnsSecurityRulePatch` was added

* `models.DnsResolverDomainLists` was added

* `models.DnsResolverDomainListPatch` was added

* `models.Action` was added

* `models.DnsSecurityRuleState` was added

* `models.DnsSecurityRules` was added

* `models.DnsResolverPolicyPatch` was added

* `models.DnsSecurityRule$DefinitionStages` was added

* `models.DnsResolverDomainListBulk` was added

* `models.DnsResolverDomainList$Definition` was added

* `models.DnsResolverPolicyVirtualNetworkLinkListResult` was added

* `models.DnsResolverPolicy$Update` was added

* `models.DnsResolverDomainList$UpdateStages` was added

* `models.DnsResolverDomainList$Update` was added

* `models.DnsResolverPolicyVirtualNetworkLink$UpdateStages` was added

* `models.DnsResolverPolicy$UpdateStages` was added

* `models.DnsResolverPolicyVirtualNetworkLinkPatch` was added

* `models.DnsResolverPolicy$Definition` was added

* `models.DnsResolverPolicy` was added

#### `DnsResolverManager` was modified

* `dnsSecurityRules()` was added
* `dnsResolverPolicyVirtualNetworkLinks()` was added
* `dnsResolverDomainLists()` was added
* `dnsResolverPolicies()` was added

## 1.0.0 (2025-01-02)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. DNS Resolver Client. Package tag package-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager DnsResolver client library for Java.

## 1.0.0-beta.4 (2024-10-23)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. DNS Resolver Client. Package tag package-preview-2023-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DnsResolverPolicyVirtualNetworkLink` was added

* `models.DnsSecurityRuleAction` was added

* `models.DnsResolverPolicyListResult` was added

* `models.DnsSecurityRule$UpdateStages` was added

* `models.DnsResolverPolicyVirtualNetworkLink$DefinitionStages` was added

* `models.DnsSecurityRule$Definition` was added

* `models.DnsResolverDomainList` was added

* `models.DnsResolverPolicyVirtualNetworkLink$Definition` was added

* `models.DnsResolverDomainListResult` was added

* `models.DnsResolverPolicies` was added

* `models.DnsResolverDomainList$DefinitionStages` was added

* `models.BlockResponseCode` was added

* `models.DnsSecurityRule$Update` was added

* `models.DnsResolverPolicyVirtualNetworkLink$Update` was added

* `models.DnsSecurityRuleListResult` was added

* `models.ActionType` was added

* `models.DnsResolverPolicyVirtualNetworkLinks` was added

* `models.DnsSecurityRule` was added

* `models.DnsResolverPolicy$DefinitionStages` was added

* `models.DnsSecurityRulePatch` was added

* `models.DnsResolverDomainLists` was added

* `models.DnsResolverDomainListPatch` was added

* `models.DnsSecurityRuleState` was added

* `models.DnsSecurityRules` was added

* `models.DnsResolverPolicyPatch` was added

* `models.DnsSecurityRule$DefinitionStages` was added

* `models.DnsResolverDomainList$Definition` was added

* `models.DnsResolverPolicyVirtualNetworkLinkListResult` was added

* `models.DnsResolverPolicy$Update` was added

* `models.DnsResolverDomainList$UpdateStages` was added

* `models.DnsResolverDomainList$Update` was added

* `models.DnsResolverPolicyVirtualNetworkLink$UpdateStages` was added

* `models.DnsResolverPolicy$UpdateStages` was added

* `models.DnsResolverPolicyVirtualNetworkLinkPatch` was added

* `models.DnsResolverPolicy$Definition` was added

* `models.DnsResolverPolicy` was added

#### `DnsResolverManager` was modified

* `dnsResolverDomainLists()` was added
* `dnsResolverPolicyVirtualNetworkLinks()` was added
* `dnsResolverPolicies()` was added
* `dnsSecurityRules()` was added

## 1.0.0-beta.3 (2024-10-10)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. DNS Resolver Client. Package tag package-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.VirtualNetworkLinks` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.DnsResolvers` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.DnsForwardingRulesets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.InboundEndpoints` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.OutboundEndpoints` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

### Features Added

#### `models.VirtualNetworkLinkPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkLinkListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkDnsForwardingRulesetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsForwardingRulesetPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForwardingRuleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsForwardingRulesetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OutboundEndpointPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OutboundEndpointListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InboundEndpointPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TargetDnsServer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DnsResolverListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SubResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InboundEndpointListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsResolverPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IpConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForwardingRulePatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2022-09-15)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. The DNS Resolver Management Client. Package tag package-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DnsResolver$DefinitionStages` was modified

* Stage 3 was added

#### `models.InboundEndpoint$DefinitionStages` was modified

* Stage 3 was added

#### `models.DnsForwardingRuleset$DefinitionStages` was modified

* Stage 3 was added

#### `models.OutboundEndpoint$DefinitionStages` was modified

* Stage 3 was added

#### `models.ForwardingRule$DefinitionStages` was modified

* Stage 2, 3 was added

#### `models.VirtualNetworkLink$DefinitionStages` was modified

* Stage 2 was added

### Features Added

#### `models.OutboundEndpoint` was modified

* `resourceGroupName()` was added

#### `DnsResolverManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.InboundEndpoint` was modified

* `resourceGroupName()` was added

#### `models.DnsForwardingRuleset` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetworkLink` was modified

* `resourceGroupName()` was added

#### `models.DnsForwardingRulesetPatch` was modified

* `dnsResolverOutboundEndpoints()` was added
* `withDnsResolverOutboundEndpoints(java.util.List)` was added

#### `models.DnsForwardingRuleset$Update` was modified

* `withDnsResolverOutboundEndpoints(java.util.List)` was added

#### `models.ForwardingRule` was modified

* `resourceGroupName()` was added

#### `models.DnsResolver` was modified

* `resourceGroupName()` was added

#### `DnsResolverManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2022-02-15)

- Azure Resource Manager DnsResolver client library for Java. This package contains Microsoft Azure SDK for DnsResolver Management SDK. The DNS Resolver Management Client. Package tag package-2020-04-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
