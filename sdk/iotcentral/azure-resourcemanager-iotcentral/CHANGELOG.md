# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2021-10-18)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.CloudErrorBody` was removed

#### `models.App$Update` was modified

* `withTemplate(java.lang.String)` was removed

### Features Added

* `models.SystemAssignedServiceIdentityType` was added

* `models.SystemAssignedServiceIdentity` was added

* `models.AppState` was added

#### `models.App$Definition` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added

#### `IotCentralManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.App` was modified

* `state()` was added
* `identity()` was added

#### `models.AppPatch` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added
* `identity()` was added
* `state()` was added

#### `models.App$Update` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-2018-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
