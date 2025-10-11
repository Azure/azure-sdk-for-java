# Release History

## 1.0.0 (2025-10-07)

- Azure Resource Manager Trusted Signing client library for Java. This package contains Microsoft Azure SDK for Trusted Signing Management SDK. Code Signing resource provider api. Package api-version 2025-10-13. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RevokeCertificate` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.CheckNameAvailability` was modified

* `validate()` was removed

#### `models.CodeSigningAccountPatch` was modified

* `withSku(models.AccountSku)` was removed
* `models.AccountSku sku()` -> `models.AccountSkuPatch sku()`
* `validate()` was removed

#### `models.AccountSku` was modified

* `validate()` was removed

#### `models.CodeSigningAccount$Update` was modified

* `withSku(models.AccountSku)` was removed

#### `models.CodeSigningAccountProperties` was modified

* `validate()` was removed

#### `models.Certificate` was modified

* `validate()` was removed

#### `models.CertificateProfileProperties` was modified

* `validate()` was removed
* `country()` was removed
* `state()` was removed
* `commonName()` was removed
* `enhancedKeyUsage()` was removed
* `organizationUnit()` was removed
* `city()` was removed
* `organization()` was removed
* `postalCode()` was removed
* `streetAddress()` was removed

### Features Added

* `models.AccountSkuPatch` was added

#### `models.CodeSigningAccountPatch` was modified

* `withSku(models.AccountSkuPatch)` was added

#### `models.CodeSigningAccount$Update` was modified

* `withSku(models.AccountSkuPatch)` was added

#### `models.Certificate` was modified

* `enhancedKeyUsage()` was added

## 1.0.0-beta.1 (2024-09-27)

- Azure Resource Manager Trusted Signing client library for Java. This package contains Microsoft Azure SDK for Trusted Signing Management SDK. Code Signing resource provider api. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-trustedsigning Java SDK.
