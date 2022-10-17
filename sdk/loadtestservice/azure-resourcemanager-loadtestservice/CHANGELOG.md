# Release History

## 1.0.0-beta.2 (2022-10-17)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SystemAssignedServiceIdentityType` was removed

* `models.SystemAssignedServiceIdentity` was removed

* `models.LoadTestResourcePatchRequestBodyProperties` was removed

#### `models.LoadTestResource$Definition` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was removed

#### `models.LoadTestResource` was modified

* `models.SystemAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.LoadTestResourcePatchRequestBody` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was removed
* `withProperties(models.LoadTestResourcePatchRequestBodyProperties)` was removed
* `models.SystemAssignedServiceIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `properties()` was removed

#### `models.LoadTestResource$Update` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was removed
* `withProperties(models.LoadTestResourcePatchRequestBodyProperties)` was removed

### Features Added

* `models.EncryptionProperties` was added

* `models.Type` was added

* `models.QuotaBucketRequest` was added

* `models.OutboundEnvironmentEndpointCollection` was added

* `models.ManagedServiceIdentity` was added

* `models.CheckQuotaAvailabilityResponse` was added

* `models.QuotaBucketRequestPropertiesDimensions` was added

* `models.Quotas` was added

* `models.EndpointDetail` was added

* `models.EndpointDependency` was added

* `models.OutboundEnvironmentEndpoint` was added

* `models.EncryptionPropertiesIdentity` was added

* `models.QuotaResource` was added

* `models.ManagedServiceIdentityType` was added

* `models.QuotaResourceList` was added

* `models.UserAssignedIdentity` was added

#### `LoadTestManager` was modified

* `quotas()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.LoadTestResource$Definition` was modified

* `withEncryption(models.EncryptionProperties)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `LoadTestManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.LoadTestResource` was modified

* `encryption()` was added
* `resourceGroupName()` was added
* `systemData()` was added

#### `models.LoadTestResourcePatchRequestBody` was modified

* `description()` was added
* `withEncryption(models.EncryptionProperties)` was added
* `withDescription(java.lang.String)` was added
* `encryption()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.LoadTests` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.LoadTestResource$Update` was modified

* `withDescription(java.lang.String)` was added
* `withEncryption(models.EncryptionProperties)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.1 (2021-12-02)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2021-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
