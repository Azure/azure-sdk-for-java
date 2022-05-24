# Release History

## 1.0.0-beta.1 (2022-05-24)

- Azure Resource Manager HybridKubernetes client library for Java. This package contains Microsoft Azure SDK for HybridKubernetes Management SDK. Hybrid Kubernetes Client. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
