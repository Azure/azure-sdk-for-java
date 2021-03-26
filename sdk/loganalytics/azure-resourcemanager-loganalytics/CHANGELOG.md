# Release History

## 1.0.0-beta.2 (2021-03-25)

- Azure Resource Manager LogAnalytics client library for Java. This package contains Microsoft Azure SDK for LogAnalytics Management SDK. Operational Insights Client. Package tag package-2020-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.Cluster$Definition` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Cluster` was modified

* `nextLink()` was removed

### New Feature

* `models.BillingType` was added

* `models.UserIdentityProperties` was added

* `models.CapacityReservationProperties` was added

* `models.AssociatedWorkspace` was added

#### `models.Cluster$Definition` was modified

* `withAssociatedWorkspaces(java.util.List)` was added
* `withBillingType(models.BillingType)` was added
* `withCapacityReservationProperties(models.CapacityReservationProperties)` was added
* `withIsDoubleEncryptionEnabled(java.lang.Boolean)` was added
* `withIsAvailabilityZonesEnabled(java.lang.Boolean)` was added

#### `models.Cluster$Update` was modified

* `withIdentity(models.Identity)` was added

#### `models.Workspace$Update` was modified

* `withFeatures(java.util.Map)` was added
* `withForceCmkForQuery(java.lang.Boolean)` was added

#### `models.Identity` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.ClusterPatch` was modified

* `identity()` was added
* `withIdentity(models.Identity)` was added

#### `models.Workspace$Definition` was modified

* `withFeatures(java.util.Map)` was added
* `withForceCmkForQuery(java.lang.Boolean)` was added

#### `models.WorkspacePatch` was modified

* `withFeatures(java.util.Map)` was added
* `createdDate()` was added
* `forceCmkForQuery()` was added
* `withForceCmkForQuery(java.lang.Boolean)` was added
* `features()` was added
* `modifiedDate()` was added

#### `models.Workspace` was modified

* `features()` was added
* `modifiedDate()` was added
* `forceCmkForQuery()` was added
* `createdDate()` was added

#### `models.KeyVaultProperties` was modified

* `withKeyRsaSize(java.lang.Integer)` was added
* `keyRsaSize()` was added

#### `models.Table` was modified

* `isTroubleshootingAllowed()` was added
* `lastTroubleshootDate()` was added
* `isTroubleshootEnabled()` was added

#### `models.Cluster` was modified

* `isAvailabilityZonesEnabled()` was added
* `isDoubleEncryptionEnabled()` was added
* `billingType()` was added
* `createdDate()` was added
* `capacityReservationProperties()` was added
* `lastModifiedDate()` was added
* `associatedWorkspaces()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager LogAnalytics client library for Java. This package contains Microsoft Azure SDK for LogAnalytics Management SDK. Operational Insights Client. Package tag package-2020-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
