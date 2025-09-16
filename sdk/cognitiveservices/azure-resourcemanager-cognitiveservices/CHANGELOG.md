# Release History

## 1.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0 (2025-09-16)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2025-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NetworkInjections` was removed

#### `models.AccountProperties` was modified

* `models.NetworkInjections networkInjections()` -> `java.util.List networkInjections()`
* `withNetworkInjections(models.NetworkInjections)` was removed

### Features Added

* `models.NetworkInjection` was added

#### `models.AccountProperties` was modified

* `withNetworkInjections(java.util.List)` was added

## 1.2.0 (2025-07-21)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2025-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ProjectListResult` was added

* `models.Project$Update` was added

* `models.ConnectionOAuth2` was added

* `models.CapabilityHost$Update` was added

* `models.ConnectionServicePrincipal` was added

* `models.ConnectionSharedAccessSignature` was added

* `models.CapabilityHost$UpdateStages` was added

* `models.CapabilityHost` was added

* `models.ResourceBase` was added

* `models.ConnectionPropertiesV2BasicResource` was added

* `models.ServicePrincipalAuthTypeConnectionProperties` was added

* `models.CapabilityHost$Definition` was added

* `models.CapabilityHostKind` was added

* `models.CapabilityHostProperties` was added

* `models.Project$UpdateStages` was added

* `models.ConnectionPropertiesV2` was added

* `models.ConnectionAccessKey` was added

* `models.CustomKeysConnectionProperties` was added

* `models.ProjectConnections` was added

* `models.AccountConnections` was added

* `models.ConnectionAuthType` was added

* `models.ManagedPERequirement` was added

* `models.Project$Definition` was added

* `models.PatAuthTypeConnectionProperties` was added

* `models.ConnectionPropertiesV2BasicResource$Definition` was added

* `models.NoneAuthTypeConnectionProperties` was added

* `models.AccountKeyAuthTypeConnectionProperties` was added

* `models.AccessKeyAuthTypeConnectionProperties` was added

* `models.ConnectionApiKey` was added

* `models.AccountCapabilityHosts` was added

* `models.ManagedPEStatus` was added

* `models.ConnectionUpdateContent` was added

* `models.ConnectionManagedIdentity` was added

* `models.ConnectionPropertiesV2BasicResource$Update` was added

* `models.CapabilityHostProvisioningState` was added

* `models.CustomKeys` was added

* `models.ManagedIdentityAuthTypeConnectionProperties` was added

* `models.ProjectProperties` was added

* `models.ConnectionCategory` was added

* `models.ProjectCapabilityHosts` was added

* `models.Projects` was added

* `models.Project` was added

* `models.OAuth2AuthTypeConnectionProperties` was added

* `models.ApiKeyAuthConnectionProperties` was added

* `models.Project$DefinitionStages` was added

* `models.ConnectionAccountKey` was added

* `models.NetworkInjections` was added

* `models.SasAuthTypeConnectionProperties` was added

* `models.UsernamePasswordAuthTypeConnectionProperties` was added

* `models.CapabilityHost$DefinitionStages` was added

* `models.ConnectionPropertiesV2BasicResourceArmPaginatedResult` was added

* `models.ConnectionGroup` was added

* `models.AadAuthTypeConnectionProperties` was added

* `models.ConnectionUsernamePassword` was added

* `models.ConnectionPropertiesV2BasicResource$DefinitionStages` was added

* `models.ConnectionPropertiesV2BasicResource$UpdateStages` was added

* `models.ConnectionPersonalAccessToken` was added

* `models.ScenarioType` was added

#### `CognitiveServicesManager` was modified

* `projectConnections()` was added
* `projects()` was added
* `projectCapabilityHosts()` was added
* `accountCapabilityHosts()` was added
* `accountConnections()` was added

#### `models.AccountProperties` was modified

* `withDefaultProject(java.lang.String)` was added
* `associatedProjects()` was added
* `networkInjections()` was added
* `withAssociatedProjects(java.util.List)` was added
* `defaultProject()` was added
* `withNetworkInjections(models.NetworkInjections)` was added
* `allowProjectManagement()` was added
* `withAllowProjectManagement(java.lang.Boolean)` was added

#### `models.DeploymentProperties` was modified

* `spilloverDeploymentName()` was added
* `withSpilloverDeploymentName(java.lang.String)` was added

## 1.2.0-beta.1 (2025-05-22)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-preview-2025-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ProjectListResult` was added

* `models.Project$Update` was added

* `models.ConnectionOAuth2` was added

* `models.CapabilityHost$Update` was added

* `models.ConnectionServicePrincipal` was added

* `models.ConnectionSharedAccessSignature` was added

* `models.CapabilityHost$UpdateStages` was added

* `models.CapabilityHost` was added

* `models.ResourceBase` was added

* `models.ConnectionPropertiesV2BasicResource` was added

* `models.ServicePrincipalAuthTypeConnectionProperties` was added

* `models.CapabilityHost$Definition` was added

* `models.CapabilityHostKind` was added

* `models.CapabilityHostProperties` was added

* `models.Project$UpdateStages` was added

* `models.ConnectionPropertiesV2` was added

* `models.ConnectionAccessKey` was added

* `models.CustomKeysConnectionProperties` was added

* `models.ProjectConnections` was added

* `models.AccountConnections` was added

* `models.ConnectionAuthType` was added

* `models.ManagedPERequirement` was added

* `models.Project$Definition` was added

* `models.PatAuthTypeConnectionProperties` was added

* `models.ConnectionPropertiesV2BasicResource$Definition` was added

* `models.NoneAuthTypeConnectionProperties` was added

* `models.AccountKeyAuthTypeConnectionProperties` was added

* `models.AccessKeyAuthTypeConnectionProperties` was added

* `models.ConnectionApiKey` was added

* `models.AccountCapabilityHosts` was added

* `models.ManagedPEStatus` was added

* `models.ConnectionUpdateContent` was added

* `models.ConnectionManagedIdentity` was added

* `models.ConnectionPropertiesV2BasicResource$Update` was added

* `models.CapabilityHostProvisioningState` was added

* `models.CustomKeys` was added

* `models.ManagedIdentityAuthTypeConnectionProperties` was added

* `models.ProjectProperties` was added

* `models.ConnectionCategory` was added

* `models.ProjectCapabilityHosts` was added

* `models.Projects` was added

* `models.Project` was added

* `models.OAuth2AuthTypeConnectionProperties` was added

* `models.ApiKeyAuthConnectionProperties` was added

* `models.Project$DefinitionStages` was added

* `models.ConnectionAccountKey` was added

* `models.NetworkInjections` was added

* `models.SasAuthTypeConnectionProperties` was added

* `models.UsernamePasswordAuthTypeConnectionProperties` was added

* `models.CapabilityHost$DefinitionStages` was added

* `models.ConnectionPropertiesV2BasicResourceArmPaginatedResult` was added

* `models.ConnectionGroup` was added

* `models.AadAuthTypeConnectionProperties` was added

* `models.ConnectionUsernamePassword` was added

* `models.ConnectionPropertiesV2BasicResource$DefinitionStages` was added

* `models.ConnectionPropertiesV2BasicResource$UpdateStages` was added

* `models.ConnectionPersonalAccessToken` was added

* `models.ScenarioType` was added

#### `CognitiveServicesManager` was modified

* `projectCapabilityHosts()` was added
* `projects()` was added
* `projectConnections()` was added
* `accountConnections()` was added
* `accountCapabilityHosts()` was added

#### `models.AccountProperties` was modified

* `withAssociatedProjects(java.util.List)` was added
* `withDefaultProject(java.lang.String)` was added
* `defaultProject()` was added
* `associatedProjects()` was added
* `withNetworkInjections(models.NetworkInjections)` was added
* `allowProjectManagement()` was added
* `withAllowProjectManagement(java.lang.Boolean)` was added
* `networkInjections()` was added

#### `models.DeploymentProperties` was modified

* `spilloverDeploymentName()` was added
* `withSpilloverDeploymentName(java.lang.String)` was added

## 1.1.0 (2024-11-22)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2024-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes
#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.Deployment$Update` was modified

* `withProperties(models.DeploymentProperties)` was removed

### Features Added

* `models.BillingMeterInfo` was added

* `models.EncryptionScopeListResult` was added

* `models.EncryptionScopeProvisioningState` was added

* `models.DeploymentModelVersionUpgradeOption` was added

* `models.RaiBlocklist$DefinitionStages` was added

* `models.ProvisioningIssueProperties` was added

* `models.RaiPolicies` was added

* `models.Usages` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.ModelCapacityListResult` was added

* `models.RaiPolicy$Definition` was added

* `models.DeploymentCapacitySettings` was added

* `models.RaiContentFilter` was added

* `models.AbusePenalty` was added

* `models.NetworkSecurityPerimeterAccessRuleProperties` was added

* `models.RaiBlocklist$Update` was added

* `models.RaiPolicy$DefinitionStages` was added

* `models.RaiBlocklistItems` was added

* `models.ModelListResult` was added

* `models.NetworkSecurityPerimeterAccessRule` was added

* `models.CapacityConfig` was added

* `models.RaiContentFilterProperties` was added

* `models.RaiBlocklistItem$DefinitionStages` was added

* `models.CalculateModelCapacityResult` was added

* `models.DefenderForAISettingResult` was added

* `models.ModelSkuCapacityProperties` was added

* `models.RaiContentFilters` was added

* `models.RaiBlocklistProperties` was added

* `models.DefenderForAISetting$Update` was added

* `models.RaiPolicy$Update` was added

* `models.EncryptionScope$DefinitionStages` was added

* `models.RaiBlocklistConfig` was added

* `models.NetworkSecurityPerimeterAccessRulePropertiesSubscriptionsItem` was added

* `models.RaiContentFilterListResult` was added

* `models.NetworkSecurityPerimeterConfigurations` was added

* `models.ByPassSelection` was added

* `models.ProvisioningIssue` was added

* `models.EncryptionScopes` was added

* `models.RaiBlocklist$Definition` was added

* `models.ModelCapacityCalculatorWorkloadRequestParam` was added

* `models.RaiPolicy` was added

* `models.NetworkSecurityPerimeter` was added

* `models.SkuResource` was added

* `models.CalculateModelCapacityParameter` was added

* `models.AbusePenaltyAction` was added

* `models.ContentLevel` was added

* `models.NspAccessRuleDirection` was added

* `models.RaiPolicyContentFilter` was added

* `models.RaiBlocklistItem$Definition` was added

* `models.DefenderForAISetting$DefinitionStages` was added

* `models.UserOwnedAmlWorkspace` was added

* `models.NetworkSecurityPerimeterProfileInfo` was added

* `models.ModelSku` was added

* `models.Models` was added

* `models.DefenderForAISettings` was added

* `models.LocationBasedModelCapacities` was added

* `models.RaiMonitorConfig` was added

* `models.RaiPolicyContentSource` was added

* `models.ModelCapacities` was added

* `models.RaiBlocklistItemProperties` was added

* `models.RaiBlockListItemsResult` was added

* `models.CalculateModelCapacityResultEstimatedCapacity` was added

* `models.RaiPolicy$UpdateStages` was added

* `models.ModelCapacityCalculatorWorkload` was added

* `models.RaiBlocklist$UpdateStages` was added

* `models.RaiPolicyProperties` was added

* `models.RaiBlocklistItem$UpdateStages` was added

* `models.DefenderForAISetting$Definition` was added

* `models.NetworkSecurityPerimeterConfigurationAssociationInfo` was added

* `models.NetworkSecurityPerimeterConfiguration` was added

* `models.EncryptionScopeProperties` was added

* `models.RaiBlocklistItem$Update` was added

* `models.RaiBlocklistItemBulkRequest` was added

* `models.RaiPolicyMode` was added

* `models.ModelCapacityListResultValueItem` was added

* `models.DefenderForAISettingState` was added

* `models.DefenderForAISetting` was added

* `models.Model` was added

* `models.RaiBlocklistItem` was added

* `models.EncryptionScope` was added

* `models.RaiBlocklist` was added

* `models.DefenderForAISetting$UpdateStages` was added

* `models.EncryptionScopeState` was added

* `models.DeploymentSkuListResult` was added

* `models.RaiBlocklists` was added

* `models.EncryptionScope$UpdateStages` was added

* `models.EncryptionScope$Update` was added

* `models.RaiBlockListResult` was added

* `models.NetworkSecurityPerimeterConfigurationList` was added

* `models.EncryptionScope$Definition` was added

* `models.RaiPolicyType` was added

* `models.CustomBlocklistConfig` was added

* `models.RaiPolicyListResult` was added

#### `models.ResourceProviders` was modified

* `calculateModelCapacity(models.CalculateModelCapacityParameter)` was added
* `calculateModelCapacityWithResponse(models.CalculateModelCapacityParameter,com.azure.core.util.Context)` was added

#### `models.RegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureEntityResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `type()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanAccountAssociation$Definition` was modified

* `withTags(java.util.Map)` was added

#### `models.CommitmentPlanAccountAssociation$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountModel` was modified

* `publisher()` was added
* `source()` was added
* `skus()` was added
* `sourceAccount()` was added
* `isDefaultVersion()` was added

#### `models.ResourceSkuRestrictions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApiProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThrottlingRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommitmentCost` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckSkuAvailabilityParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PatchResourceTagsAndSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuAvailability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkRuleSet` was modified

* `bypass()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withBypass(models.ByPassSelection)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RequestMatchPattern` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommitmentPlanListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Usage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Encryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommitmentQuota` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountModelListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentTierListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegionSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Deployment$Update` was modified

* `withSku(models.Sku)` was added
* `withTags(java.util.Map)` was added

#### `models.Identity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPeriod` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserOwnedStorage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `CognitiveServicesManager` was modified

* `usages()` was added
* `raiContentFilters()` was added
* `raiPolicies()` was added
* `networkSecurityPerimeterConfigurations()` was added
* `raiBlocklists()` was added
* `defenderForAISettings()` was added
* `locationBasedModelCapacities()` was added
* `models()` was added
* `encryptionScopes()` was added
* `modelCapacities()` was added
* `raiBlocklistItems()` was added

#### `models.CheckDomainAvailabilityParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UsageListResult` was modified

* `nextLink()` was added

#### `models.CommitmentPlanAccountAssociationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentModel` was modified

* `withPublisher(java.lang.String)` was added
* `withSourceAccount(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `publisher()` was added
* `sourceAccount()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `source()` was added
* `withSource(java.lang.String)` was added

#### `models.AccountProperties` was modified

* `amlWorkspace()` was added
* `withRaiMonitorConfig(models.RaiMonitorConfig)` was added
* `abusePenalty()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `raiMonitorConfig()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAmlWorkspace(models.UserOwnedAmlWorkspace)` was added

#### `models.Deployment` was modified

* `sku()` was added
* `tags()` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSkuRestrictionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanAssociation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuChangeInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KeyVaultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentProperties` was modified

* `parentDeploymentName()` was added
* `versionUpgradeOption()` was added
* `withCapacitySettings(models.DeploymentCapacitySettings)` was added
* `dynamicThrottlingEnabled()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withParentDeploymentName(java.lang.String)` was added
* `withVersionUpgradeOption(models.DeploymentModelVersionUpgradeOption)` was added
* `withCurrentCapacity(java.lang.Integer)` was added
* `currentCapacity()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `rateLimits()` was added
* `capacitySettings()` was added

#### `models.QuotaLimit` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Deployments` was modified

* `listSkus(java.lang.String,java.lang.String,java.lang.String)` was added
* `listSkus(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.MetricName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSkuListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentScaleSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccountSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MultiRegionSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PatchResourceTags` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CallRateLimit` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanProperties` was modified

* `provisioningIssues()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelDeprecationInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanAccountAssociation` was modified

* `tags()` was added

#### `models.Deployment$Definition` was modified

* `withSku(models.Sku)` was added
* `withTags(java.util.Map)` was added

#### `models.IpRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.1.0-beta.2 (2024-10-31)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.CheckDomainAvailabilityParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureEntityResource` was modified

* `name()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanAccountAssociationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentModel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceSkuRestrictions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApiProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSkuRestrictionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanAssociation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThrottlingRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AbusePenalty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentCost` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuChangeInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckSkuAvailabilityParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KeyVaultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModelListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaLimit` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PatchResourceTagsAndSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CapacityConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetricName` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResource` was modified

* `id()` was added
* `name()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSkuListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuAvailability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkRuleSet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RequestMatchPattern` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentScaleSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccountSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MultiRegionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Encryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PatchResourceTags` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CallRateLimit` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentPlanProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentQuota` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelDeprecationInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccountModelListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommitmentTierListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegionSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Identity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommitmentPeriod` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserOwnedStorage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0-beta.1 (2023-07-19)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Usage` was modified

* `validate()` was removed
* `withLimit(java.lang.Double)` was removed
* `models.MetricName name()` -> `models.MetricName name()`
* `withCurrentValue(java.lang.Double)` was removed
* `withNextResetTime(java.lang.String)` was removed
* `java.lang.String quotaPeriod()` -> `java.lang.String quotaPeriod()`
* `withStatus(models.QuotaUsageStatus)` was removed
* `java.lang.Double currentValue()` -> `java.lang.Double currentValue()`
* `withUnit(models.UnitType)` was removed
* `java.lang.String nextResetTime()` -> `java.lang.String nextResetTime()`
* `models.UnitType unit()` -> `models.UnitType unit()`
* `java.lang.Double limit()` -> `java.lang.Double limit()`
* `withName(models.MetricName)` was removed
* `withQuotaPeriod(java.lang.String)` was removed
* `models.QuotaUsageStatus status()` -> `models.QuotaUsageStatus status()`

### Features Added

* `models.AbusePenaltyAction` was added

* `models.DeploymentModelVersionUpgradeOption` was added

* `models.ModelSku` was added

* `models.Models` was added

* `models.Usages` was added

* `models.AbusePenalty` was added

* `models.ModelListResult` was added

* `models.CapacityConfig` was added

* `models.Model` was added

#### `CognitiveServicesManager` was modified

* `models()` was added
* `usages()` was added

#### `models.UsageListResult` was modified

* `nextLink()` was added

#### `models.DeploymentModel` was modified

* `source()` was added
* `withSource(java.lang.String)` was added

#### `models.AccountModel` was modified

* `source()` was added
* `skus()` was added
* `isDefaultVersion()` was added

#### `models.AccountProperties` was modified

* `abusePenalty()` was added

#### `models.Deployment` was modified

* `sku()` was added

#### `models.DeploymentProperties` was modified

* `withVersionUpgradeOption(models.DeploymentModelVersionUpgradeOption)` was added
* `versionUpgradeOption()` was added
* `rateLimits()` was added

#### `models.Usage` was modified

* `innerModel()` was added

#### `models.CommitmentPlanProperties` was modified

* `provisioningIssues()` was added

#### `models.Deployment$Definition` was modified

* `withSku(models.Sku)` was added

#### `models.Deployment$Update` was modified

* `withSku(models.Sku)` was added

## 1.0.0 (2023-02-24)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.7 (2023-02-21)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CommitmentPlan$DefinitionStages` was modified

* `withExistingAccount(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.CommitmentPlan$Update` was modified

* `withProperties(models.CommitmentPlanProperties)` was removed

#### `models.CommitmentPlans` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `define(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed

#### `models.CommitmentPlan$Definition` was modified

* `withExistingAccount(java.lang.String,java.lang.String)` was removed

### Features Added

* `models.CommitmentPlanProvisioningState` was added

* `models.CommitmentPlanAccountAssociation$Definition` was added

* `models.CommitmentPlanAccountAssociation$Update` was added

* `models.CommitmentPlanAccountAssociationListResult` was added

* `models.CommitmentPlanAccountAssociation$UpdateStages` was added

* `models.CommitmentPlanAssociation` was added

* `models.PatchResourceTagsAndSku` was added

* `models.CommitmentPlanAccountAssociation$DefinitionStages` was added

* `models.MultiRegionSettings` was added

* `models.PatchResourceTags` was added

* `models.CommitmentPlanAccountAssociation` was added

* `models.RoutingMethods` was added

* `models.RegionSetting` was added

* `models.ModelLifecycleStatus` was added

#### `models.AccountModel` was modified

* `finetuneCapabilities()` was added
* `lifecycleStatus()` was added

#### `models.AccountProperties` was modified

* `withLocations(models.MultiRegionSettings)` was added
* `commitmentPlanAssociations()` was added
* `locations()` was added

#### `models.CommitmentPlan` was modified

* `kind()` was added
* `location()` was added
* `sku()` was added
* `region()` was added
* `regionName()` was added
* `tags()` was added

#### `models.CommitmentPlan$Update` was modified

* `withTags(java.util.Map)` was added
* `withSku(models.Sku)` was added

#### `models.CommitmentPlanProperties` was modified

* `provisioningState()` was added
* `withCommitmentPlanGuid(java.lang.String)` was added
* `commitmentPlanGuid()` was added

#### `models.CommitmentPlans` was modified

* `deleteAssociation(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteAssociation(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String)` was added
* `getAssociation(java.lang.String,java.lang.String,java.lang.String)` was added
* `deletePlan(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `definePlan(java.lang.String)` was added
* `defineAssociation(java.lang.String)` was added
* `deletePlanById(java.lang.String)` was added
* `getAssociationWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listPlansBySubscription(com.azure.core.util.Context)` was added
* `deleteAssociationByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added
* `listPlansBySubscription()` was added
* `deleteAssociationById(java.lang.String)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.CommitmentPlanInner)` was added
* `listAssociations(java.lang.String,java.lang.String)` was added
* `deletePlan(java.lang.String,java.lang.String)` was added
* `getAssociationById(java.lang.String)` was added
* `deletePlanByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.CommitmentPlanInner,com.azure.core.util.Context)` was added
* `getAssociationByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listAssociations(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.CommitmentPlan$Definition` was modified

* `withTags(java.util.Map)` was added
* `withKind(java.lang.String)` was added
* `withRegion(java.lang.String)` was added
* `withSku(models.Sku)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withExistingResourceGroup(java.lang.String)` was added

## 1.0.0-beta.6 (2022-11-23)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.DeploymentModel` was modified

* `callRateLimit()` was added

#### `models.AccountModel` was modified

* `callRateLimit()` was added

#### `models.DeploymentProperties` was modified

* `raiPolicyName()` was added
* `callRateLimit()` was added
* `withRaiPolicyName(java.lang.String)` was added
* `capabilities()` was added

## 1.0.0-beta.5 (2022-06-20)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Deployment` was modified

* `resourceGroupName()` was added

#### `models.Account` was modified

* `resourceGroupName()` was added

#### `models.CommitmentPlan` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.4 (2022-04-11)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AccountModel` was added

* `models.ModelDeprecationInfo` was added

* `models.AccountModelListResult` was added

#### `CognitiveServicesManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Accounts` was modified

* `listModels(java.lang.String,java.lang.String)` was added
* `listModels(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AccountProperties` was modified

* `withDynamicThrottlingEnabled(java.lang.Boolean)` was added
* `deletionDate()` was added
* `dynamicThrottlingEnabled()` was added
* `scheduledPurgeDate()` was added

#### `CognitiveServicesManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.DeploymentScaleSettings` was modified

* `activeCapacity()` was added

## 1.0.0-beta.3 (2021-11-17)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DeploymentProvisioningState` was added

* `models.CommitmentPlan$UpdateStages` was added

* `models.DeploymentModel` was added

* `models.CommitmentTiers` was added

* `models.Deployment` was added

* `models.Deployment$UpdateStages` was added

* `models.CommitmentCost` was added

* `models.CommitmentTier` was added

* `models.DeploymentProperties` was added

* `models.Deployments` was added

* `models.DeploymentScaleSettings` was added

* `models.CommitmentPlanListResult` was added

* `models.Deployment$DefinitionStages` was added

* `models.CommitmentPlan$DefinitionStages` was added

* `models.CommitmentPlan` was added

* `models.CommitmentPlan$Update` was added

* `models.CommitmentPlanProperties` was added

* `models.CommitmentPlans` was added

* `models.CommitmentQuota` was added

* `models.HostingModel` was added

* `models.CommitmentTierListResult` was added

* `models.Deployment$Definition` was added

* `models.DeploymentScaleType` was added

* `models.Deployment$Update` was added

* `models.CommitmentPeriod` was added

* `models.DeploymentListResult` was added

* `models.CommitmentPlan$Definition` was added

#### `CognitiveServicesManager` was modified

* `deployments()` was added
* `commitmentTiers()` was added
* `commitmentPlans()` was added

#### `models.CheckDomainAvailabilityParameter` was modified

* `kind()` was added
* `withKind(java.lang.String)` was added

#### `models.DomainAvailability` was modified

* `kind()` was added

#### `CognitiveServicesManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.2 (2021-05-24)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2021-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.OperationEntity` was removed

* `models.CheckDomainAvailabilityResult` was removed

* `models.CognitiveServicesAccount` was removed

* `models.ResourceSkusResult` was removed

* `models.CheckSkuAvailabilityResultList` was removed

* `models.CognitiveServicesAccountSkuChangeInfo` was removed

* `models.CognitiveServicesAccountListResult` was removed

* `models.CognitiveServicesAccountApiProperties` was removed

* `models.CognitiveServicesAccountKeys` was removed

* `models.CognitiveServicesResourceAndSku` was removed

* `models.OperationEntityListResult` was removed

* `models.CheckSkuAvailabilityResult` was removed

* `models.OperationDisplayInfo` was removed

* `models.UsagesResult` was removed

* `models.CognitiveServicesAccount$Update` was removed

* `models.CognitiveServicesAccountEnumerateSkusResult` was removed

* `models.CognitiveServicesAccountProperties` was removed

* `models.CognitiveServicesAccount$DefinitionStages` was removed

* `models.IdentityType` was removed

* `models.CognitiveServicesAccount$UpdateStages` was removed

* `models.CognitiveServicesAccount$Definition` was removed

#### `models.ResourceProviders` was modified

* `models.CheckDomainAvailabilityResult checkDomainAvailability(models.CheckDomainAvailabilityParameter)` -> `models.DomainAvailability checkDomainAvailability(models.CheckDomainAvailabilityParameter)`
* `models.CheckSkuAvailabilityResultList checkSkuAvailability(java.lang.String,models.CheckSkuAvailabilityParameter)` -> `models.SkuAvailabilityListResult checkSkuAvailability(java.lang.String,models.CheckSkuAvailabilityParameter)`

#### `models.Accounts` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `getUsages(java.lang.String,java.lang.String)` was removed
* `getUsagesWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.CognitiveServicesAccountEnumerateSkusResult listSkus(java.lang.String,java.lang.String)` -> `models.AccountSkuListResult listSkus(java.lang.String,java.lang.String)`
* `models.CognitiveServicesAccountKeys regenerateKey(java.lang.String,java.lang.String,models.RegenerateKeyParameters)` -> `models.ApiKeys regenerateKey(java.lang.String,java.lang.String,models.RegenerateKeyParameters)`
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.CognitiveServicesAccount$DefinitionStages$Blank define(java.lang.String)` -> `models.Account$DefinitionStages$Blank define(java.lang.String)`
* `models.CognitiveServicesAccountKeys listKeys(java.lang.String,java.lang.String)` -> `models.ApiKeys listKeys(java.lang.String,java.lang.String)`
* `models.CognitiveServicesAccount getByResourceGroup(java.lang.String,java.lang.String)` -> `models.Account getByResourceGroup(java.lang.String,java.lang.String)`
* `models.CognitiveServicesAccount getById(java.lang.String)` -> `models.Account getById(java.lang.String)`

#### `models.PrivateEndpointConnections` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.UserAssignedIdentity` was modified

* `withClientId(java.lang.String)` was removed
* `withPrincipalId(java.lang.String)` was removed

#### `models.Identity` was modified

* `models.IdentityType type()` -> `models.ResourceIdentityType type()`
* `withType(models.IdentityType)` was removed

### New Feature

* `models.AzureEntityResource` was added

* `models.OperationListResult` was added

* `models.ActionType` was added

* `models.UsageListResult` was added

* `models.SkuAvailabilityListResult` was added

* `models.AccountProperties` was added

* `models.ApiProperties` was added

* `models.ApiKeys` was added

* `models.DeletedAccounts` was added

* `models.DomainAvailability` was added

* `models.Origin` was added

* `models.ThrottlingRule` was added

* `models.Operation` was added

* `models.OperationDisplay` was added

* `models.SkuChangeInfo` was added

* `models.QuotaLimit` was added

* `models.Account$UpdateStages` was added

* `models.ResourceIdentityType` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.ResourceSkuListResult` was added

* `models.SkuAvailability` was added

* `models.RequestMatchPattern` was added

* `models.Account$Definition` was added

* `models.Account$Update` was added

* `models.AccountListResult` was added

* `models.AccountSku` was added

* `models.Account` was added

* `models.CallRateLimit` was added

* `models.AccountSkuListResult` was added

* `models.Account$DefinitionStages` was added

#### `CognitiveServicesManager` was modified

* `deletedAccounts()` was added

#### `models.Sku` was modified

* `capacity()` was added
* `family()` was added
* `withFamily(java.lang.String)` was added
* `withSize(java.lang.String)` was added
* `withTier(models.SkuTier)` was added
* `size()` was added
* `withCapacity(java.lang.Integer)` was added

#### `models.Accounts` was modified

* `listUsagesWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listUsages(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ResourceSkuRestrictions` was modified

* `withRestrictionInfo(models.ResourceSkuRestrictionInfo)` was added
* `withValues(java.util.List)` was added
* `withReasonCode(models.ResourceSkuRestrictionsReasonCode)` was added
* `withType(models.ResourceSkuRestrictionsType)` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `provisioningState()` was added

#### `models.ResourceSkuRestrictionInfo` was modified

* `withLocations(java.util.List)` was added
* `withZones(java.util.List)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KeyVaultProperties` was modified

* `identityClientId()` was added
* `withIdentityClientId(java.lang.String)` was added

#### `models.MetricName` was modified

* `withValue(java.lang.String)` was added
* `withLocalizedValue(java.lang.String)` was added

#### `models.Usage` was modified

* `withNextResetTime(java.lang.String)` was added
* `withQuotaPeriod(java.lang.String)` was added
* `withUnit(models.UnitType)` was added
* `withCurrentValue(java.lang.Double)` was added
* `withName(models.MetricName)` was added
* `withLimit(java.lang.Double)` was added

#### `models.Identity` was modified

* `withType(models.ResourceIdentityType)` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.UserOwnedStorage` was modified

* `identityClientId()` was added
* `withIdentityClientId(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager CognitiveServices client library for Java. This package contains Microsoft Azure SDK for CognitiveServices Management SDK. Cognitive Services Management Client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
