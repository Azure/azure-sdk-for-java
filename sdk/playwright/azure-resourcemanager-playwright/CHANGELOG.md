# Release History

## 1.1.0-beta.1 (2026-04-06)

- Azure Resource Manager Playwright client library for Java. This package contains Microsoft Azure SDK for Playwright Management SDK. Playwright Service Management API provides access to Playwright workspace resources and their operations through Azure Resource Manager. Package api-version 2026-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PlaywrightWorkspaceFreeTrialProperties` was modified

* `validate()` was removed

#### `models.PlaywrightQuotaProperties` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.PlaywrightWorkspaceQuotaProperties` was modified

* `validate()` was removed

#### `models.PlaywrightWorkspaceUpdate` was modified

* `validate()` was removed

#### `models.PlaywrightWorkspaceProperties` was modified

* `validate()` was removed

#### `models.PlaywrightWorkspaceUpdateProperties` was modified

* `validate()` was removed

#### `models.FreeTrialProperties` was modified

* `validate()` was removed

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.UserAssignedIdentity` was added

* `models.ManagedServiceIdentityType` was added

#### `models.PlaywrightWorkspace$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.PlaywrightWorkspace$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.PlaywrightWorkspace` was modified

* `identity()` was added

#### `models.PlaywrightWorkspaceUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.PlaywrightWorkspaceProperties` was modified

* `withStorageUri(java.lang.String)` was added
* `reporting()` was added
* `storageUri()` was added
* `withReporting(models.EnablementStatus)` was added

#### `models.PlaywrightWorkspaceUpdateProperties` was modified

* `reporting()` was added
* `withReporting(models.EnablementStatus)` was added
* `withStorageUri(java.lang.String)` was added
* `storageUri()` was added

## 1.0.0 (2025-08-26)

- Azure Resource Manager Playwright client library for Java. This package contains Microsoft Azure SDK for Playwright Management SDK. Playwright Service Management API provides access to Playwright workspace resources and their operations through Azure Resource Manager. Package api-version 2025-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.PlaywrightWorkspaceProperties` was modified

* `workspaceId()` was added

## 1.0.0-beta.1 (2025-07-14)

- Azure Resource Manager Playwright client library for Java. This package contains Microsoft Azure SDK for Playwright Management SDK. Playwright service provides access to Playwright workspace resource and it's operations. Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-playwright Java SDK.
