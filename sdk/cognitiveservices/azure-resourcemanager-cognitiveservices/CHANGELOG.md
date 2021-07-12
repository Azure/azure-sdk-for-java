# Release History

## 1.0.0-beta.3 (Unreleased)


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
