# Release History

## 1.0.0-beta.3 (2023-04-17)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `QuotaManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `QuotaManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.2 (2021-11-11)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2021-03-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Operations` was removed

* `models.LimitValue` was removed

#### `QuotaManager` was modified

* `operations()` was removed

#### `models.LimitObject` was modified

* `withLimitObjectType(models.LimitType)` was removed
* `limitObjectType()` was removed

### Features Added

* `models.QuotaOperations` was added

#### `QuotaManager` was modified

* `quotaOperations()` was added

## 1.0.0-beta.1 (2021-09-13)

- Azure Resource Manager quota client library for Java. This package contains Microsoft Azure SDK for quota Management SDK. Microsoft Azure Quota Resource Provider. Package tag package-2021-03-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
