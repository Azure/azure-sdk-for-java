# Release History

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
