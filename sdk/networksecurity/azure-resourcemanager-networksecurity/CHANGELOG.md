# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-11-17)

- Azure Resource Manager Network Security client library for Java. This package contains Microsoft Azure SDK for Network Security Management SDK. Microsoft.NetworkSecurity NeuroShield management API. Package api-version NeuroShield.Management: 2026-10-01-preview, Microsoft.NetworkSecurity: 2026-11-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.NetworkSecurityPolicyType` was added

* `models.AgentFabric$DefinitionStages` was added

* `models.AgentIdentity` was added

* `models.MemberTagsUpdate` was added

* `models.TlsInspectionCertificateMode` was added

* `models.UrlFilteringAddressPrefixesDestination` was added

* `models.AgentFabrics` was added

* `models.TlsInspectionPolicyWorkloadIdPatternsSource` was added

* `models.McpRuleMatchingConditions` was added

* `models.AgentFabricProperties` was added

* `models.WorkloadBase` was added

* `models.PolicyAction` was added

* `models.ClusterAssociationUpdate` was added

* `models.Member` was added

* `models.McpMatchCase` was added

* `models.ClusterAssociation$Update` was added

* `models.EgressTrustBundle` was added

* `models.McpToolMatching` was added

* `models.McpMatchType` was added

* `models.UrlFilteringFqdnsDestination` was added

* `models.AgentFabric$Update` was added

* `models.PolicyGroup` was added

* `models.ContentInspectionPolicy` was added

* `models.McpPolicyWorkloadIdPatternsDestination` was added

* `models.UrlFilteringWorkloadIdPatternsSource` was added

* `models.AgentFabric` was added

* `models.FqdnFilteringWorkloadIdPatternsSource` was added

* `models.McpPolicySourceType` was added

* `models.ClusterAssociationIfNoneMatch` was added

* `models.AksWorkloadPortMapping` was added

* `models.ClusterAssociationUpdateProperties` was added

* `models.PolicyDirection` was added

* `models.ClusterAssociations` was added

* `models.FqdnFilteringSourceType` was added

* `models.McpPolicyFqdnsDestination` was added

* `models.ClusterAssociation` was added

* `models.AgentFabric$UpdateStages` was added

* `models.FqdnFilteringFqdnsDestination` was added

* `models.FqdnFilteringAddressPrefixesSource` was added

* `models.EgressTrustBundles` was added

* `models.ClusterAssociationStatus` was added

* `models.TlsInspectionRuleDestination` was added

* `models.McpArgumentMatching` was added

* `models.UrlFilteringDestination` was added

* `models.TlsInspectionAction` was added

* `models.UrlFilteringPolicy` was added

* `models.AgentFabricSandboxEndpoint` was added

* `models.ContentInspectionPolicyWorkloadIdPatternsSource` was added

* `models.UrlFilteringWorkloadIdPatternsDestination` was added

* `models.AgentFabricSandboxWorkload` was added

* `models.ContentInspectionPolicySource` was added

* `models.McpStringMatching` was added

* `models.PolicyGroups` was added

* `models.SystemAssignedServiceIdentity` was added

* `models.OperationStatus` was added

* `models.TlsInspectionRuleFqdnsDestination` was added

* `models.AgentFabric$Definition` was added

* `models.ClusterAssociationProperties` was added

* `models.McpPolicyDestinationType` was added

* `models.UrlFilteringSource` was added

* `models.PolicyGroup$DefinitionStages` was added

* `models.ClusterAssociationSkuName` was added

* `models.UrlFilteringAddressPrefixesSource` was added

* `models.FqdnFilteringDestination` was added

* `models.McpPolicyAddressPrefixesDestination` was added

* `models.Members` was added

* `models.TlsInspectionPolicy` was added

* `models.UrlFilteringDestinationType` was added

* `models.PolicyGroup$Update` was added

* `models.UrlFilteringSourceType` was added

* `models.McpPolicySource` was added

* `models.PolicyGroup$UpdateStages` was added

* `models.AgentFabricSandboxImage` was added

* `models.ClusterAssociation$Definition` was added

* `models.TlsInspectionRuleDestinationType` was added

* `models.EgressTrustBundleCertificate` was added

* `models.McpRule` was added

* `models.AzureManagedCaCertificate` was added

* `models.ClusterAssociation$DefinitionStages` was added

* `models.Member$Definition` was added

* `models.Member$UpdateStages` was added

* `models.ContentInspectionPolicySourceType` was added

* `models.FqdnFilteringSource` was added

* `models.McpPromptMatching` was added

* `models.TlsInspectionCertificateBase` was added

* `models.ImagePullPolicy` was added

* `models.EgressTrustBundleProperties` was added

* `models.ContentInspectionProfile` was added

* `models.FqdnFilteringPolicy` was added

* `models.McpToolMethod` was added

* `models.SystemAssignedServiceIdentityType` was added

* `models.TlsInspectionPolicySourceType` was added

* `models.FqdnFilteringDestinationType` was added

* `models.McpPolicy` was added

* `models.WorkloadType` was added

* `models.AgentFabricSandboxResources` was added

* `models.TlsInspectionRule` was added

* `models.ContentInspectionPolicyAddressPrefixesSource` was added

* `models.PolicyGroupTagsUpdate` was added

* `models.MemberUpdateProperties` was added

* `models.McpPolicyWorkloadIdPatternsSource` was added

* `models.PromptInjectionOnDetection` was added

* `models.NetworkSecurityPolicyBase` was added

* `models.AgentFabricTagsUpdate` was added

* `models.AksWorkload` was added

* `models.ProvisioningState` was added

* `models.AciWorkload` was added

* `models.ClusterAssociation$UpdateStages` was added

* `models.McpPolicyDestination` was added

* `models.McpRuleDestinationMatching` was added

* `models.HttpMethod` was added

* `models.McpPolicyAddressPrefixesSource` was added

* `models.MemberProperties` was added

* `models.Member$DefinitionStages` was added

* `models.AksWorkloadReference` was added

* `models.PromptInjectionProtection` was added

* `models.ContentInspectionSensitivity` was added

* `models.UrlFilteringRule` was added

* `models.PolicyGroup$Definition` was added

* `models.McpPromptMethod` was added

* `models.TlsInspectionPolicySource` was added

* `models.Member$Update` was added

* `models.PolicyGroupProperties` was added

#### `NetworkSecurityManager` was modified

* `agentFabrics()` was added
* `operationStatus()` was added
* `members()` was added
* `policyGroups()` was added
* `egressTrustBundles()` was added
* `clusterAssociations()` was added

## 1.0.0-beta.1 (2026-11-06)

- Azure Resource Manager Network Security client library for Java. This package contains Microsoft Azure SDK for Network Security Management SDK. Microsoft.NetworkSecurity NeuroShield management API. Package api-version 2026-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-networksecurity Java SDK.

