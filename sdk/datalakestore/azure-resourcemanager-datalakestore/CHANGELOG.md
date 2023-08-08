# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-16)

- Azure Resource Manager DataLakeStore client library for Java. This package contains Microsoft Azure SDK for DataLakeStore Management SDK. Creates an Azure Data Lake Store account management client. Package tag package-2016-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DataLakeStoreAccountPropertiesBasic` was removed

* `models.DataLakeStoreAccountProperties` was removed

### Features Added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `DataLakeStoreManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.DataLakeStoreAccount` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetworkRule` was modified

* `resourceGroupName()` was added

#### `DataLakeStoreManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.TrustedIdProvider` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager DataLakeStore client library for Java. This package contains Microsoft Azure SDK for DataLakeStore Management SDK. Creates an Azure Data Lake Store account management client. Package tag package-2016-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
