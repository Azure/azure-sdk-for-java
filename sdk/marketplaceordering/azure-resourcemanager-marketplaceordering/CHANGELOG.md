# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2024-10-09)

- Azure Resource Manager MarketplaceOrdering client library for Java. This package contains Microsoft Azure SDK for MarketplaceOrdering Management SDK. REST API for MarketplaceOrdering Agreements. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MarketplaceAgreements` was modified

* `models.AgreementTerms cancel(java.lang.String,java.lang.String,java.lang.String)` -> `models.OldAgreementTerms cancel(java.lang.String,java.lang.String,java.lang.String)`
* `models.AgreementTerms getAgreement(java.lang.String,java.lang.String,java.lang.String)` -> `models.OldAgreementTerms getAgreement(java.lang.String,java.lang.String,java.lang.String)`
* `java.util.List list()` -> `models.OldAgreementTermsList list()`
* `models.AgreementTerms sign(java.lang.String,java.lang.String,java.lang.String)` -> `models.OldAgreementTerms sign(java.lang.String,java.lang.String,java.lang.String)`

### Features Added

* `models.OldAgreementTerms` was added

* `models.State` was added

* `models.OldAgreementTermsList` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2022-04-07)

- Azure Resource Manager MarketplaceOrdering client library for Java. This package contains Microsoft Azure SDK for MarketplaceOrdering Management SDK. REST API for MarketplaceOrdering Agreements. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.OperationDisplay` was modified

* `withDescription(java.lang.String)` was added
* `description()` was added

#### `MarketplaceOrderingManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `MarketplaceOrderingManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager MarketplaceOrdering client library for Java. This package contains Microsoft Azure SDK for MarketplaceOrdering Management SDK. REST API for MarketplaceOrdering Agreements. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
