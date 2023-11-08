# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-05-17)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag package-2023-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.FrontendUpdateProperties` was removed

* `models.FrontendPropertiesIpAddress` was removed

* `models.FrontendMode` was removed

* `models.FrontendIpAddressVersion` was removed

* `models.AssociationUpdateProperties` was removed

#### `models.Frontend$Update` was modified

* `withProperties(models.FrontendUpdateProperties)` was removed

#### `models.AssociationType` was modified

* `models.AssociationType[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.TrafficControllerUpdate` was modified

* `properties()` was removed
* `withProperties(java.lang.Object)` was removed

#### `models.AssociationUpdate` was modified

* `withProperties(models.AssociationUpdateProperties)` was removed
* `properties()` was removed

#### `models.Association$Update` was modified

* `withProperties(models.AssociationUpdateProperties)` was removed

#### `models.Frontend$Definition` was modified

* `withIpAddressVersion(models.FrontendIpAddressVersion)` was removed
* `withPublicIpAddress(models.FrontendPropertiesIpAddress)` was removed
* `withMode(models.FrontendMode)` was removed

#### `models.FrontendUpdate` was modified

* `withProperties(models.FrontendUpdateProperties)` was removed
* `properties()` was removed

#### `models.Frontend` was modified

* `ipAddressVersion()` was removed
* `publicIpAddress()` was removed
* `mode()` was removed

#### `models.TrafficController$Update` was modified

* `withProperties(java.lang.Object)` was removed

### Features Added

* `models.AssociationSubnetUpdate` was added

#### `models.AssociationUpdate` was modified

* `subnet()` was added
* `associationType()` was added
* `withAssociationType(models.AssociationType)` was added
* `withSubnet(models.AssociationSubnetUpdate)` was added

#### `models.Association$Update` was modified

* `withSubnet(models.AssociationSubnetUpdate)` was added
* `withAssociationType(models.AssociationType)` was added

#### `models.Frontend` was modified

* `fqdn()` was added

## 1.0.0-beta.1 (2022-12-22)

- Azure Resource Manager TrafficController client library for Java. This package contains Microsoft Azure SDK for TrafficController Management SDK. Traffic Controller Provider management API. Package tag package-2022-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
