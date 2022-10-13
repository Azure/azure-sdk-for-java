# Release History

## 1.0.0-beta.2 (2022-10-13)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NginxDeploymentProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.NginxCertificateProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.NginxConfigurationProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

### Features Added

* `models.NginxCertificate$UpdateStages` was added

* `models.NginxCertificate$Update` was added

#### `models.NginxCertificate` was modified

* `resourceGroupName()` was added
* `update()` was added

#### `models.NginxConfigurationProperties` was modified

* `withProtectedFiles(java.util.List)` was added
* `protectedFiles()` was added

## 1.0.0-beta.1 (2022-08-30)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
