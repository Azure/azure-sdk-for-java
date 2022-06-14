# Release History

## 1.0.0-beta.1 (2022-06-14)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-03-25)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ConfigurationState` was removed

#### `models.Sim` was modified

* `configurationState()` was removed

### Features Added

* `models.SimState` was added

#### `models.DataNetwork` was modified

* `systemData()` was added

#### `models.Slice` was modified

* `systemData()` was added

#### `models.PacketCoreControlPlane` was modified

* `systemData()` was added

#### `models.Site` was modified

* `systemData()` was added

#### `models.Sim` was modified

* `simState()` was added
* `systemData()` was added

#### `models.SimPolicy` was modified

* `systemData()` was added

#### `models.MobileNetwork` was modified

* `systemData()` was added

#### `models.PacketCoreDataPlane` was modified

* `systemData()` was added

#### `models.Service` was modified

* `systemData()` was added

#### `models.InterfaceProperties` was modified

* `ipv4Subnet()` was added
* `withIpv4Subnet(java.lang.String)` was added
* `withIpv4Gateway(java.lang.String)` was added
* `ipv4Gateway()` was added
* `ipv4Address()` was added
* `withIpv4Address(java.lang.String)` was added

## 1.0.0-beta.1 (2022-02-28)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
