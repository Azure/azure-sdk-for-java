# Release History

## 1.1.0-beta.1 (2026-07-23)

- Azure Resource Manager DevOps Infrastructure client library for Java. This package contains Microsoft Azure SDK for DevOps Infrastructure Management SDK.  Package api-version 2026-07-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.StatelessAgentProfile` was modified

* `validate()` was removed

#### `models.PoolUpdateProperties` was modified

* `validate()` was removed

#### `models.ResourcePredictionsProfile` was modified

* `validate()` was removed

#### `models.AzureDevOpsOrganizationProfile` was modified

* `validate()` was removed

#### `models.StorageProfile` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed

#### `models.DataDisk` was modified

* `validate()` was removed

#### `models.DevOpsAzureSku` was modified

* `validate()` was removed

#### `models.GitHubOrganization` was modified

* `validate()` was removed

#### `models.ResourceSkuRestrictions` was modified

* `validate()` was removed

#### `models.AgentProfile` was modified

* `validate()` was removed

#### `models.OsProfile` was modified

* `validate()` was removed

#### `models.PoolUpdate` was modified

* `validate()` was removed

#### `models.ImageVersionProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ManualResourcePredictionsProfile` was modified

* `validate()` was removed

#### `models.AzureDevOpsPermissionProfile` was modified

* `validate()` was removed

#### `models.GitHubOrganizationProfile` was modified

* `validate()` was removed

#### `models.Stateful` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.VmssFabricProfile` was modified

* `validate()` was removed

#### `models.OrganizationProfile` was modified

* `validate()` was removed

#### `models.PoolImage` was modified

* `validate()` was removed

#### `models.ResourceDetailsObjectProperties` was modified

* `validate()` was removed

#### `models.PoolProperties` was modified

* `validate()` was removed

#### `models.Organization` was modified

* `validate()` was removed

#### `models.ResourcePredictions` was modified

* `validate()` was removed

#### `models.AutomaticResourcePredictionsProfile` was modified

* `validate()` was removed

#### `models.SecretsManagementSettings` was modified

* `validate()` was removed

#### `models.QuotaName` was modified

* `validate()` was removed

#### `models.FabricProfile` was modified

* `validate()` was removed

#### `models.ResourceSkuZoneDetails` was modified

* `validate()` was removed

#### `models.NetworkProfile` was modified

* `validate()` was removed

#### `models.ResourceSkuCapabilities` was modified

* `validate()` was removed

#### `models.ResourceSkuLocationInfo` was modified

* `validate()` was removed

#### `models.ResourceSkuRestrictionInfo` was modified

* `validate()` was removed

#### `models.ResourceSkuProperties` was modified

* `validate()` was removed

### Features Added

* `models.CheckNameAvailability` was added

* `models.VmSize` was added

* `models.CheckNameAvailabilityResult` was added

* `models.DevOpsInfrastructureResourceType` was added

* `models.DeleteResourcesDetails` was added

* `models.RuntimeConfiguration` was added

* `models.CheckNameAvailabilityReason` was added

* `models.AvailabilityStatus` was added

* `models.CertificateStoreNameOption` was added

* `models.EphemeralType` was added

#### `models.PoolUpdateProperties` was modified

* `runtimeConfiguration()` was added
* `withRuntimeConfiguration(models.RuntimeConfiguration)` was added

#### `models.AzureDevOpsOrganizationProfile` was modified

* `description()` was added
* `updateDescription()` was added
* `alias()` was added
* `withDescription(java.lang.String)` was added
* `withAlias(java.lang.String)` was added
* `withUpdateDescription(java.lang.Boolean)` was added

#### `models.Pool` was modified

* `deleteResources(models.DeleteResourcesDetails)` was added
* `deleteResourcesWithResponse(models.DeleteResourcesDetails,com.azure.core.util.Context)` was added

#### `models.DevOpsAzureSku` was modified

* `withWindowsNvmeDrive(java.lang.String)` was added
* `linuxNvmePath()` was added
* `withVmSizes(java.util.List)` was added
* `windowsNvmeDrive()` was added
* `withLinuxNvmePath(java.lang.String)` was added
* `vmSizes()` was added

#### `models.PoolImage` was modified

* `provisioningScriptShouldRestart()` was added
* `provisioningScriptEntryPoint()` was added
* `ephemeralType()` was added
* `withProvisioningScriptShouldRestart(java.lang.Boolean)` was added
* `withProvisioningScriptStorageAccountResourceId(java.lang.String)` was added
* `withEphemeralType(models.EphemeralType)` was added
* `provisioningScriptManagedIdentityClientId()` was added
* `provisioningScriptStorageAccountResourceId()` was added
* `withProvisioningScriptEntryPoint(java.lang.String)` was added
* `isEphemeral()` was added
* `withProvisioningScriptManagedIdentityClientId(java.lang.String)` was added

#### `models.PoolProperties` was modified

* `withRuntimeConfiguration(models.RuntimeConfiguration)` was added
* `runtimeConfiguration()` was added

#### `models.Organization` was modified

* `withAlias(java.lang.String)` was added
* `alias()` was added
* `withOpenAccess(java.lang.Boolean)` was added
* `openAccess()` was added

#### `models.SecretsManagementSettings` was modified

* `certificateStoreName()` was added
* `withCertificateStoreName(models.CertificateStoreNameOption)` was added

#### `models.Pools` was modified

* `checkNameAvailabilityWithResponse(models.CheckNameAvailability,com.azure.core.util.Context)` was added
* `checkNameAvailability(models.CheckNameAvailability)` was added
* `deleteResourcesWithResponse(java.lang.String,java.lang.String,models.DeleteResourcesDetails,com.azure.core.util.Context)` was added
* `deleteResources(java.lang.String,java.lang.String,models.DeleteResourcesDetails)` was added

#### `models.NetworkProfile` was modified

* `ipAddresses()` was added
* `staticIpAddressCount()` was added
* `withStaticIpAddressCount(java.lang.Integer)` was added

## 1.0.0 (2024-11-21)

- Azure Resource Manager DevOps Infrastructure client library for Java. This package contains Microsoft Azure SDK for DevOps Infrastructure Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `implementation.models.QuotaListResult` was removed

#### `implementation.models.PagedOperation` was removed

#### `models.QuotaProperties` was removed

#### `models.StatelessAgentProfile` was modified

* `withResourcePredictions(java.lang.Object)` was removed

#### `models.SubscriptionUsages` was modified

* `listByLocation(java.lang.String)` was removed
* `listByLocation(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Quota` was modified

* `java.lang.String name()` -> `models.QuotaName name()`
* `systemData()` was removed
* `type()` was removed
* `properties()` was removed

#### `models.AgentProfile` was modified

* `java.lang.Object resourcePredictions()` -> `models.ResourcePredictions resourcePredictions()`
* `withResourcePredictions(java.lang.Object)` was removed

#### `models.Stateful` was modified

* `withResourcePredictions(java.lang.Object)` was removed

#### `models.UserAssignedIdentity` was modified

* `withClientId(java.lang.String)` was removed
* `withPrincipalId(java.lang.String)` was removed

#### `DevOpsInfrastructureManager` was modified

* `fluent.DevOpsInfrastructureClient serviceClient()` -> `fluent.DevOpsInfrastructureManagementClient serviceClient()`

### Features Added

* `implementation.models.PagedQuota` was added

* `implementation.models.OperationListResult` was added

* `models.ResourcePredictions` was added

#### `models.StatelessAgentProfile` was modified

* `withResourcePredictions(models.ResourcePredictions)` was added

#### `models.SubscriptionUsages` was modified

* `usages(java.lang.String)` was added
* `usages(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Quota` was modified

* `limit()` was added
* `unit()` was added
* `currentValue()` was added

#### `models.AgentProfile` was modified

* `withResourcePredictions(models.ResourcePredictions)` was added

## 1.0.0-beta.1 (2024-05-23)

- Azure Resource Manager DevOps Infrastructure client library for Java. This package contains Microsoft Azure SDK for DevOps Infrastructure Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
