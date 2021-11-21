# Release History

## 1.0.0-beta.3 (Unreleased)


## 1.0.0-beta.2 (2021-07-29)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK.  Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BatchAccountIdentityUserAssignedIdentities` was removed

* `models.CertificateProperties` was removed

* `models.CertificateCreateOrUpdateProperties` was removed

* `models.BatchPoolIdentityUserAssignedIdentities` was removed

#### `models.Certificate$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.Pool$Update` was modified

* `ifMatch(java.lang.String)` was removed

### Features Added

* `models.EndpointDependency` was added

* `models.DiffDiskPlacement` was added

* `models.SupportedSkusResult` was added

* `models.EndpointDetail` was added

* `models.SupportedSku` was added

* `models.AutoStorageAuthenticationMode` was added

* `models.ComputeNodeIdentityReference` was added

* `models.OSDisk` was added

* `models.OutboundEnvironmentEndpointCollection` was added

* `models.SkuCapability` was added

* `models.UserAssignedIdentities` was added

* `models.DiffDiskSettings` was added

* `models.AuthenticationMode` was added

* `models.OutboundEnvironmentEndpoint` was added

#### `BatchManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.BatchAccount` was modified

* `allowedAuthenticationModes()` was added

#### `models.BatchAccountUpdateParameters` was modified

* `allowedAuthenticationModes()` was added
* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.Certificate$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.ResourceFile` was modified

* `identityReference()` was added
* `withIdentityReference(models.ComputeNodeIdentityReference)` was added

#### `models.BatchAccounts` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String)` was added

#### `models.VirtualMachineConfiguration` was modified

* `osDisk()` was added
* `withOsDisk(models.OSDisk)` was added

#### `models.AutoStorageProperties` was modified

* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added
* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added

#### `models.BatchAccount$Definition` was modified

* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.AutoStorageBaseProperties` was modified

* `withAuthenticationMode(models.AutoStorageAuthenticationMode)` was added
* `authenticationMode()` was added
* `withNodeIdentityReference(models.ComputeNodeIdentityReference)` was added
* `nodeIdentityReference()` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.AzureBlobFileSystemConfiguration` was modified

* `withIdentityReference(models.ComputeNodeIdentityReference)` was added
* `identityReference()` was added

#### `models.Locations` was modified

* `listSupportedVirtualMachineSkus(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSupportedCloudServiceSkus(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listSupportedCloudServiceSkus(java.lang.String)` was added
* `listSupportedVirtualMachineSkus(java.lang.String)` was added

#### `models.ContainerRegistry` was modified

* `identityReference()` was added
* `withIdentityReference(models.ComputeNodeIdentityReference)` was added

#### `models.BatchAccount$Update` was modified

* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.BatchAccountCreateParameters` was modified

* `allowedAuthenticationModes()` was added
* `withAllowedAuthenticationModes(java.util.List)` was added

#### `models.Pool$Update` was modified

* `withIfMatch(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK.  Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
