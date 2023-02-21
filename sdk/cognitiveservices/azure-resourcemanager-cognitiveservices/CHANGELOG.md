# Release History

## 1.0.0-beta.8 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
