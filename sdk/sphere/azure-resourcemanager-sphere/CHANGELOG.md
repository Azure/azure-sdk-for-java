# Release History

## 1.0.0 (2024-03-26)

- Azure Resource Manager AzureSphereMgmt client library for Java. This package contains Microsoft Azure SDK for AzureSphereMgmt Management SDK. Azure Sphere resource management API. Package tag package-2024-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `AzureSphereManager` was removed

* `AzureSphereManager$Configurable` was removed

#### `models.CountDeviceResponse` was modified

* `innerModel()` was removed
* `value()` was removed

#### `models.Product$Definition` was modified

* `withDescription(java.lang.String)` was removed

#### `models.ImageListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.PagedDeviceInsight` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.DeviceGroup$Update` was modified

* `withDescription(java.lang.String)` was removed
* `withOsFeedType(models.OSFeedType)` was removed
* `withAllowCrashDumpsCollection(models.AllowCrashDumpCollection)` was removed
* `withUpdatePolicy(models.UpdatePolicy)` was removed
* `withRegionalDataBoundary(models.RegionalDataBoundary)` was removed

#### `models.DeviceGroupListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Catalogs` was modified

* `models.CountDeviceResponse countDevices(java.lang.String,java.lang.String)` -> `models.CountDevicesResponse countDevices(java.lang.String,java.lang.String)`

#### `models.DeploymentListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Device$Definition` was modified

* `withDeviceId(java.lang.String)` was removed

#### `models.Catalog` was modified

* `provisioningState()` was removed
* `models.CountDeviceResponse countDevices()` -> `models.CountDevicesResponse countDevices()`

#### `models.Device` was modified

* `deviceId()` was removed
* `lastUpdateRequestUtc()` was removed
* `lastOsUpdateUtc()` was removed
* `chipSku()` was removed
* `provisioningState()` was removed
* `lastAvailableOsVersion()` was removed
* `lastInstalledOsVersion()` was removed

#### `models.Product$Update` was modified

* `withDescription(java.lang.String)` was removed

#### `models.ProductListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.DeviceGroup$Definition` was modified

* `withUpdatePolicy(models.UpdatePolicy)` was removed
* `withRegionalDataBoundary(models.RegionalDataBoundary)` was removed
* `withAllowCrashDumpsCollection(models.AllowCrashDumpCollection)` was removed
* `withOsFeedType(models.OSFeedType)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.DeviceGroup` was modified

* `provisioningState()` was removed
* `updatePolicy()` was removed
* `description()` was removed
* `hasDeployment()` was removed
* `osFeedType()` was removed
* `allowCrashDumpsCollection()` was removed
* `regionalDataBoundary()` was removed
* `models.CountDeviceResponse countDevices()` -> `models.CountDevicesResponse countDevices()`

#### `models.DeviceUpdate` was modified

* `deviceGroupId()` was removed
* `withDeviceGroupId(java.lang.String)` was removed

#### `models.CatalogListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.DeviceListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Product` was modified

* `provisioningState()` was removed
* `models.CountDeviceResponse countDevices()` -> `models.CountDevicesResponse countDevices()`
* `description()` was removed

#### `models.Device$Update` was modified

* `withDeviceGroupId(java.lang.String)` was removed

#### `models.Image$Definition` was modified

* `withImageId(java.lang.String)` was removed
* `withRegionalDataBoundary(models.RegionalDataBoundary)` was removed
* `withImage(java.lang.String)` was removed

#### `models.Deployment` was modified

* `provisioningState()` was removed
* `deploymentDateUtc()` was removed
* `deploymentId()` was removed
* `deployedImages()` was removed

#### `models.DeviceGroups` was modified

* `models.CountDeviceResponse countDevices(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.CountDevicesResponse countDevices(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`

#### `models.Products` was modified

* `models.CountDeviceResponse countDevices(java.lang.String,java.lang.String,java.lang.String)` -> `models.CountDevicesResponse countDevices(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ProductUpdate` was modified

* `withDescription(java.lang.String)` was removed
* `description()` was removed

#### `models.DeviceGroupUpdate` was modified

* `osFeedType()` was removed
* `regionalDataBoundary()` was removed
* `description()` was removed
* `withOsFeedType(models.OSFeedType)` was removed
* `withAllowCrashDumpsCollection(models.AllowCrashDumpCollection)` was removed
* `allowCrashDumpsCollection()` was removed
* `withRegionalDataBoundary(models.RegionalDataBoundary)` was removed
* `withUpdatePolicy(models.UpdatePolicy)` was removed
* `withDescription(java.lang.String)` was removed
* `updatePolicy()` was removed

#### `models.CertificateListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Image` was modified

* `provisioningState()` was removed
* `imageName()` was removed
* `uri()` was removed
* `description()` was removed
* `componentId()` was removed
* `image()` was removed
* `regionalDataBoundary()` was removed
* `imageType()` was removed
* `imageId()` was removed

#### `models.Deployment$Definition` was modified

* `withDeployedImages(java.util.List)` was removed
* `withDeploymentId(java.lang.String)` was removed

#### `models.Certificate` was modified

* `status()` was removed
* `expiryUtc()` was removed
* `provisioningState()` was removed
* `certificate()` was removed
* `notBeforeUtc()` was removed
* `thumbprint()` was removed
* `subject()` was removed

### Features Added

* `AzureSphereMgmtManager` was added

* `models.DeviceGroupProperties` was added

* `models.DeviceProperties` was added

* `models.CertificateProperties` was added

* `models.CountDevicesResponse` was added

* `models.DeploymentProperties` was added

* `models.DeviceUpdateProperties` was added

* `models.DeviceGroupUpdateProperties` was added

* `models.CatalogProperties` was added

* `models.ProductUpdateProperties` was added

* `models.ImageProperties` was added

* `AzureSphereMgmtManager$Configurable` was added

* `models.ProductProperties` was added

#### `models.CountDeviceResponse` was modified

* `withValue(int)` was added
* `validate()` was added

#### `models.Product$Definition` was modified

* `withProperties(models.ProductProperties)` was added

#### `models.DeviceGroup$Update` was modified

* `withProperties(models.DeviceGroupUpdateProperties)` was added

#### `models.Catalogs` was modified

* `uploadImage(java.lang.String,java.lang.String,fluent.models.ImageInner)` was added
* `uploadImage(java.lang.String,java.lang.String,fluent.models.ImageInner,com.azure.core.util.Context)` was added

#### `models.Device$Definition` was modified

* `withProperties(models.DeviceProperties)` was added

#### `models.Catalog` was modified

* `uploadImage(fluent.models.ImageInner)` was added
* `properties()` was added
* `uploadImage(fluent.models.ImageInner,com.azure.core.util.Context)` was added

#### `models.Device` was modified

* `systemData()` was added
* `properties()` was added

#### `models.Product$Update` was modified

* `withProperties(models.ProductUpdateProperties)` was added

#### `models.DeviceGroup$Definition` was modified

* `withProperties(models.DeviceGroupProperties)` was added

#### `models.DeviceGroup` was modified

* `systemData()` was added
* `properties()` was added

#### `models.DeviceUpdate` was modified

* `withProperties(models.DeviceUpdateProperties)` was added
* `properties()` was added

#### `models.Product` was modified

* `systemData()` was added
* `properties()` was added

#### `models.Device$Update` was modified

* `withProperties(models.DeviceUpdateProperties)` was added

#### `models.Image$Definition` was modified

* `withProperties(models.ImageProperties)` was added

#### `models.Deployment` was modified

* `systemData()` was added
* `properties()` was added

#### `models.ProductUpdate` was modified

* `properties()` was added
* `withProperties(models.ProductUpdateProperties)` was added

#### `models.DeviceGroupUpdate` was modified

* `withProperties(models.DeviceGroupUpdateProperties)` was added
* `properties()` was added

#### `models.Catalog$Definition` was modified

* `withProperties(models.CatalogProperties)` was added

#### `models.Image` was modified

* `properties()` was added
* `systemData()` was added

#### `models.Deployment$Definition` was modified

* `withProperties(models.DeploymentProperties)` was added

#### `models.Certificate` was modified

* `properties()` was added

## 1.0.0-beta.1 (2023-07-21)

- Azure Resource Manager AzureSphere client library for Java. This package contains Microsoft Azure SDK for AzureSphere Management SDK. Azure Sphere resource management API. Package tag package-2022-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
