# Release History

## 1.0.0-beta.2 (2026-10-29)

- Azure Resource Manager FileShares client library for Java. This package contains Microsoft Azure SDK for FileShares Management SDK. Azure File Shares Resource Provider API. Package api-version 2026-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.InformationalOperations` was modified

* `getLimitsWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ManagedServiceIdentityType` was added

* `models.UserAssignedIdentity` was added

* `models.StartRedundancyConversionRequest` was added

* `models.KeyEncryptionKeyIdentityType` was added

* `models.KeyEncryptionKeyIdentity` was added

* `models.InfrastructureEncryption` was added

* `models.StartRedundancyConversionInput` was added

* `models.MigrationState` was added

* `models.FileShareLimitsRequest` was added

* `models.ManagedServiceIdentity` was added

* `models.ScaleMode` was added

* `models.RedundancyConversionStatusResponse` was added

* `models.FileShareEncryption` was added

* `models.RedundancyConversionStatus` was added

* `models.CustomerManagedKeyEncryption` was added

* `models.RedundancyConversionStatusOutput` was added

* `models.FileShareOfferInput` was added

#### `models.FileShare$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.FileShareUpdate` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.FileShareUpdateProperties` was modified

* `withEncryption(models.FileShareEncryption)` was added
* `encryption()` was added

#### `models.FileShare$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.InformationalOperations` was modified

* `getLimitsWithResponse(java.lang.String,models.FileShareLimitsRequest,com.azure.core.util.Context)` was added

#### `models.FileShares` was modified

* `startRedundancyConversionWithResponse(java.lang.String,java.lang.String,models.StartRedundancyConversionRequest,com.azure.core.util.Context)` was added
* `getRedundancyConversionStatusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getRedundancyConversionStatus(java.lang.String,java.lang.String)` was added
* `startRedundancyConversion(java.lang.String,java.lang.String,models.StartRedundancyConversionRequest)` was added

#### `models.FileShare` was modified

* `getRedundancyConversionStatus()` was added
* `startRedundancyConversion(models.StartRedundancyConversionRequest)` was added
* `startRedundancyConversionWithResponse(models.StartRedundancyConversionRequest,com.azure.core.util.Context)` was added
* `identity()` was added
* `getRedundancyConversionStatusWithResponse(com.azure.core.util.Context)` was added

#### `models.FileShareProperties` was modified

* `migrationState()` was added
* `withScaleMode(models.ScaleMode)` was added
* `withEncryption(models.FileShareEncryption)` was added
* `scaleMode()` was added
* `encryption()` was added

#### `models.FileShareProvisioningRecommendationInput` was modified

* `protocol()` was added
* `scaleMode()` was added
* `mediaTier()` was added
* `withScaleMode(models.ScaleMode)` was added
* `withMediaTier(models.MediaTier)` was added
* `withProtocol(models.Protocol)` was added

#### `models.MediaTier` was modified

* `HDD` was added

## 1.0.0-beta.1 (2026-05-06)

- Azure Resource Manager FileShares client library for Java. This package contains Microsoft Azure SDK for FileShares Management SDK. Azure File Shares Resource Provider API. Package api-version 2026-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release of the Azure Resource Manager FileShares client library for Java.

