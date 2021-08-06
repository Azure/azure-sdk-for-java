# Release History

## 1.0.0-beta.3 (2021-08-06)

- Azure Resource Manager LogAnalytics client library for Java. This package contains Microsoft Azure SDK for LogAnalytics Management SDK. Operational Insights Client. Package tag package-2020-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorAdditionalInfo` was removed

#### `models.Cluster$Definition` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Workspace$Update` was modified

* `withFeatures(java.util.Map)` was removed

#### `models.Workspace$Definition` was modified

* `withFeatures(java.util.Map)` was removed

#### `models.WorkspacePatch` was modified

* `java.util.Map features()` -> `models.WorkspaceFeatures features()`
* `withFeatures(java.util.Map)` was removed

#### `models.Workspace` was modified

* `java.util.Map features()` -> `models.WorkspaceFeatures features()`

#### `models.WorkspaceSku` was modified

* `maxCapacityReservationLevel()` was removed

#### `models.Cluster` was modified

* `nextLink()` was removed

### Features Added

* `models.BillingType` was added

* `models.UserIdentityProperties` was added

* `models.WorkspaceFeatures` was added

* `models.CapacityReservationProperties` was added

* `models.AssociatedWorkspace` was added

#### `models.Cluster$Definition` was modified

* `withAssociatedWorkspaces(java.util.List)` was added
* `withCapacityReservationProperties(models.CapacityReservationProperties)` was added
* `withIsDoubleEncryptionEnabled(java.lang.Boolean)` was added
* `withIsAvailabilityZonesEnabled(java.lang.Boolean)` was added
* `withBillingType(models.BillingType)` was added

#### `models.Cluster$Update` was modified

* `withIdentity(models.Identity)` was added
* `withBillingType(models.BillingType)` was added

#### `models.Workspace$Update` was modified

* `withFeatures(models.WorkspaceFeatures)` was added

#### `models.Identity` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.ClusterPatch` was modified

* `billingType()` was added
* `withBillingType(models.BillingType)` was added
* `withIdentity(models.Identity)` was added
* `identity()` was added

#### `LogAnalyticsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Workspace$Definition` was modified

* `withFeatures(models.WorkspaceFeatures)` was added

#### `models.WorkspacePatch` was modified

* `withFeatures(models.WorkspaceFeatures)` was added

#### `models.KeyVaultProperties` was modified

* `withKeyRsaSize(java.lang.Integer)` was added
* `keyRsaSize()` was added

#### `models.Cluster` was modified

* `lastModifiedDate()` was added
* `associatedWorkspaces()` was added
* `isDoubleEncryptionEnabled()` was added
* `capacityReservationProperties()` was added
* `isAvailabilityZonesEnabled()` was added
* `createdDate()` was added
* `billingType()` was added

## 1.0.0-beta.2 (2021-03-30)

- Azure Resource Manager LogAnalytics client library for Java. This package contains Microsoft Azure SDK for LogAnalytics Management SDK. Operational Insights Client. Package tag package-2020-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

#### `models.Workspace$Update` was modified

* `withForceCmkForQuery(java.lang.Boolean)` was added
* `withFeatures(java.util.Map)` was added

#### `models.Workspace$Definition` was modified

* `withFeatures(java.util.Map)` was added
* `withForceCmkForQuery(java.lang.Boolean)` was added

#### `models.WorkspacePatch` was modified

* `withFeatures(java.util.Map)` was added
* `createdDate()` was added
* `withForceCmkForQuery(java.lang.Boolean)` was added
* `features()` was added
* `modifiedDate()` was added
* `forceCmkForQuery()` was added

#### `models.Workspace` was modified

* `createdDate()` was added
* `features()` was added
* `modifiedDate()` was added
* `forceCmkForQuery()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager LogAnalytics client library for Java. This package contains Microsoft Azure SDK for LogAnalytics Management SDK. Operational Insights Client. Package tag package-2020-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
