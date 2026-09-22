# Release History

## 1.0.0-beta.2 (2026-09-22)

- Azure Resource Manager appnetwork client library for Java. This package contains Microsoft Azure SDK for appnetwork Management SDK.  Package api-version 2026-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AppLinkMemberUpdateProperties` was modified

* `withUpgradeProfile(models.UpgradeProfile)` was removed
* `models.UpgradeProfile upgradeProfile()` -> `models.UpgradeProfileUpdate upgradeProfile()`
* `withObservabilityProfile(models.ObservabilityProfile)` was removed
* `observabilityProfile()` was removed
* `withConnectivityProfile(models.ConnectivityProfile)` was removed
* `models.ConnectivityProfile connectivityProfile()` -> `models.ConnectivityProfileUpdate connectivityProfile()`

### Features Added

* `models.FullyManagedUpgradeProfileUpdate` was added

* `models.SelfManagedUpgradeProfileUpdate` was added

* `models.ConnectivityProfileUpdate` was added

* `models.ManagedServiceIdentityUpdate` was added

* `models.EastWestGatewayProfileUpdate` was added

* `models.UpgradeProfileUpdate` was added

#### `models.AppLinkMemberUpdateProperties` was modified

* `withConnectivityProfile(models.ConnectivityProfileUpdate)` was added
* `withUpgradeProfile(models.UpgradeProfileUpdate)` was added

#### `models.AppLink$Update` was modified

* `withIdentity(models.ManagedServiceIdentityUpdate)` was added

#### `models.AppLinkUpdate` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentityUpdate)` was added

#### `models.ConnectivityProfile` was modified

* `network()` was added
* `withNetwork(java.lang.String)` was added

## 1.0.0-beta.1 (2026-03-25)

- Azure Resource Manager appnetwork client library for Java. This package contains Microsoft Azure SDK for appnetwork Management SDK.  Package api-version 2025-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-appnetwork Java SDK.
