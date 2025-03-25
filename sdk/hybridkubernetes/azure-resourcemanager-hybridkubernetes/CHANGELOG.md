# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2025-03-25)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2024-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ConnectedClusterPatch` was modified

* `properties()` was removed
* `withProperties(java.lang.Object)` was removed

#### `models.ConnectedCluster$Update` was modified

* `withProperties(java.lang.Object)` was removed

### Features Added

* `models.ArcAgentryConfigurations` was added

* `models.ArcAgentProfile` was added

* `models.AzureHybridBenefit` was added

* `models.SecurityProfile` was added

* `models.AgentError` was added

* `models.ConnectedClusterKind` was added

* `models.SystemComponent` was added

* `models.AutoUpgradeOptions` was added

* `models.SecurityProfileWorkloadIdentity` was added

* `models.OidcIssuerProfile` was added

* `models.Gateway` was added

* `models.PrivateLinkState` was added

* `models.AadProfile` was added

#### `models.ConnectedClusterPatch` was modified

* `withDistributionVersion(java.lang.String)` was added
* `azureHybridBenefit()` was added
* `distributionVersion()` was added
* `withAzureHybridBenefit(models.AzureHybridBenefit)` was added
* `withDistribution(java.lang.String)` was added
* `distribution()` was added

#### `models.HybridConnectionConfig` was modified

* `relayType()` was added
* `relayTid()` was added

#### `models.ConnectedCluster$Definition` was modified

* `withOidcIssuerProfile(models.OidcIssuerProfile)` was added
* `withAzureHybridBenefit(models.AzureHybridBenefit)` was added
* `withDistributionVersion(java.lang.String)` was added
* `withGateway(models.Gateway)` was added
* `withAadProfile(models.AadProfile)` was added
* `withPrivateLinkState(models.PrivateLinkState)` was added
* `withPrivateLinkScopeResourceId(java.lang.String)` was added
* `withArcAgentProfile(models.ArcAgentProfile)` was added
* `withArcAgentryConfigurations(java.util.List)` was added
* `withKind(models.ConnectedClusterKind)` was added
* `withSecurityProfile(models.SecurityProfile)` was added

#### `models.ConnectedCluster` was modified

* `gateway()` was added
* `distributionVersion()` was added
* `azureHybridBenefit()` was added
* `arcAgentProfile()` was added
* `privateLinkScopeResourceId()` was added
* `aadProfile()` was added
* `oidcIssuerProfile()` was added
* `arcAgentryConfigurations()` was added
* `miscellaneousProperties()` was added
* `securityProfile()` was added
* `privateLinkState()` was added
* `kind()` was added

#### `models.ConnectedCluster$Update` was modified

* `withDistributionVersion(java.lang.String)` was added
* `withAzureHybridBenefit(models.AzureHybridBenefit)` was added
* `withDistribution(java.lang.String)` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager HybridKubernetes client library for Java.

## 1.0.0-beta.4 (2024-10-17)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ConnectedClusterList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectedClusterIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectedClusterPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListClusterUserCredentialProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HybridConnectionConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.3 (2023-01-12)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `HybridKubernetesManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `HybridKubernetesManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.ConnectedCluster` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.2 (2021-10-08)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorAdditionalInfo` was removed

* `models.LastModifiedByType` was removed

* `models.CreatedByType` was removed

* `models.SystemData` was removed

#### `models.ConnectedCluster` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

### Features Added

* `models.AuthenticationMethod` was added

* `models.CredentialResults` was added

* `models.ListClusterUserCredentialProperties` was added

* `models.HybridConnectionConfig` was added

* `models.CredentialResult` was added

#### `HybridKubernetesManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.ConnectedCluster` was modified

* `listClusterUserCredentialWithResponse(models.ListClusterUserCredentialProperties,com.azure.core.util.Context)` was added
* `listClusterUserCredential(models.ListClusterUserCredentialProperties)` was added

#### `models.ConnectedClusters` was modified

* `listClusterUserCredential(java.lang.String,java.lang.String,models.ListClusterUserCredentialProperties)` was added
* `listClusterUserCredentialWithResponse(java.lang.String,java.lang.String,models.ListClusterUserCredentialProperties,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2021-03-01)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
