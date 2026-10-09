# Release History

## 1.0.0 (2026-10-09)

- Azure Resource Manager Commvault Content Store client library for Java. This package contains Microsoft Azure SDK for Commvault Content Store Management SDK.  Package api-version 2026-09-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CloudAccountUpdateProperties` was removed

#### `models.CloudAccountUpdate` was removed

#### `models.ActivateSaaSParameterRequest` was modified

* `withSaaSGuid(java.lang.String)` was removed
* `saaSGuid()` was removed

#### `models.ProtectionGroupProperties` was modified

* `java.lang.Integer numberOfProtectedItems()` -> `int numberOfProtectedItems()`
* `java.lang.Long lastBackUpTime()` -> `long lastBackUpTime()`

#### `models.CloudAccount$Update` was modified

* `withProperties(models.CloudAccountUpdateProperties)` was removed

#### `models.CloudAccountProperties` was modified

* `withMultiPersonAuthorizationOnCcaCreate(models.EntityInfo)` was removed
* `withBackupAdminOnCcaCreate(models.EntityInfo)` was removed
* `backupAdminOnCcaCreate()` was removed
* `multiPersonAuthorizationOnCcaCreate()` was removed

### Features Added

* `models.CompanyProfile` was added

* `models.ActivateSaaSRequestParam` was added

* `models.ComplianceLockStatus` was added

#### `models.ActivateSaaSParameterRequest` was modified

* `withPublisherId(java.lang.String)` was added
* `saasGuid()` was added
* `withActivateSaaSRequestParam(models.ActivateSaaSRequestParam)` was added
* `activateSaaSRequestParam()` was added
* `publisherId()` was added
* `withSaasGuid(java.lang.String)` was added

#### `models.Storages` was modified

* `disableComplianceLock(java.lang.String,java.lang.String,java.lang.String)` was added
* `refresh(java.lang.String,java.lang.String,java.lang.String)` was added
* `enableComplianceLockWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `enableComplianceLock(java.lang.String,java.lang.String,java.lang.String)` was added
* `refreshWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `disableComplianceLockWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CloudAccount$Update` was modified

* `withProperties(models.CloudAccountProperties)` was added

#### `models.Storage` was modified

* `refreshWithResponse(com.azure.core.util.Context)` was added
* `disableComplianceLockWithResponse(com.azure.core.util.Context)` was added
* `enableComplianceLockWithResponse(com.azure.core.util.Context)` was added
* `enableComplianceLock()` was added
* `disableComplianceLock()` was added

#### `models.StorageProperties` was modified

* `complianceLockStatus()` was added

#### `models.CloudAccountProperties` was modified

* `withRoleAssignmentsOnCcaCreate(java.util.List)` was added
* `company()` was added
* `roleAssignmentsOnCcaCreate()` was added
* `withCompany(models.CompanyProfile)` was added

## 1.0.0-beta.1 (2026-06-24)

- Azure Resource Manager Commvault Content Store client library for Java. This package contains Microsoft Azure SDK for Commvault Content Store Management SDK.  Package api-version 2026-07-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-commvaultcontentstore Java SDK.
