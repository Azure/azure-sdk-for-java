# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
