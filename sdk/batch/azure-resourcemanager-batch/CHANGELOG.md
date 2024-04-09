# Release History

## 1.1.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.4 (2024-03-27)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UpgradePolicy` was added

* `models.RollingUpgradePolicy` was added

* `models.UpgradeMode` was added

* `models.AutomaticOSUpgradePolicy` was added

#### `models.SupportedSku` was modified

* `batchSupportEndOfLife()` was added

#### `models.Pool$Definition` was modified

* `withUpgradePolicy(models.UpgradePolicy)` was added

#### `models.Pool` was modified

* `upgradePolicy()` was added

#### `models.Pool$Update` was modified

* `withUpgradePolicy(models.UpgradePolicy)` was added

## 1.1.0-beta.3 (2023-12-22)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SecurityTypes` was added

* `models.SecurityProfile` was added

* `models.UefiSettings` was added

* `models.ManagedDisk` was added

* `models.ServiceArtifactReference` was added

#### `models.VirtualMachineConfiguration` was modified

* `securityProfile()` was added
* `serviceArtifactReference()` was added
* `withServiceArtifactReference(models.ServiceArtifactReference)` was added
* `withSecurityProfile(models.SecurityProfile)` was added

#### `models.OSDisk` was modified

* `diskSizeGB()` was added
* `withDiskSizeGB(java.lang.Integer)` was added
* `withWriteAcceleratorEnabled(java.lang.Boolean)` was added
* `withCaching(models.CachingType)` was added
* `caching()` was added
* `managedDisk()` was added
* `writeAcceleratorEnabled()` was added
* `withManagedDisk(models.ManagedDisk)` was added

#### `models.Pool$Definition` was modified

* `withResourceTags(java.util.Map)` was added

#### `models.Pool` was modified

* `resourceTags()` was added

#### `models.Pool$Update` was modified

* `withResourceTags(java.util.Map)` was added

## 1.1.0-beta.2 (2023-07-26)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ContainerConfiguration` was modified

* `java.lang.String type()` -> `models.ContainerType type()`
* `withType(java.lang.String)` was removed

### Features Added

* `models.ContainerType` was added

#### `models.NetworkConfiguration` was modified

* `withEnableAcceleratedNetworking(java.lang.Boolean)` was added
* `enableAcceleratedNetworking()` was added

#### `models.ContainerConfiguration` was modified

* `withType(models.ContainerType)` was added

#### `models.VMExtension` was modified

* `enableAutomaticUpgrade()` was added
* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added

## 1.1.0-beta.1 (2022-11-24)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionsUpdateHeaders` was removed

* `models.BatchAccountsCreateResponse` was removed

* `models.PoolsDeleteHeaders` was removed

* `models.BatchAccountsDeleteHeaders` was removed

* `models.CertificatesDeleteResponse` was removed

* `models.BatchAccountsDeleteResponse` was removed

* `models.CertificatesDeleteHeaders` was removed

* `models.BatchAccountsCreateHeaders` was removed

* `models.PrivateEndpointConnectionsUpdateResponse` was removed

* `models.PoolsDeleteResponse` was removed

#### `models.NetworkConfiguration` was modified

* `withDynamicVNetAssignmentScope(models.DynamicVNetAssignmentScope)` was removed
* `dynamicVNetAssignmentScope()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionRequired()` was removed

#### `models.Certificate$Definition` was modified

* `withData(java.lang.String)` was removed

#### `models.PrivateEndpointConnections` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,java.lang.String)` was removed

### Features Added

* `models.NetworkProfile` was added

* `models.EndpointAccessProfile` was added

* `models.NodeCommunicationMode` was added

* `models.EndpointAccessDefaultAction` was added

* `models.IpRule` was added

#### `models.NetworkConfiguration` was modified

* `withDynamicVnetAssignmentScope(models.DynamicVNetAssignmentScope)` was added
* `dynamicVnetAssignmentScope()` was added

#### `BatchManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Application` was modified

* `resourceGroupName()` was added

#### `models.BatchAccount` was modified

* `networkProfile()` was added
* `resourceGroupName()` was added
* `nodeManagementEndpoint()` was added

#### `models.BatchAccountUpdateParameters` was modified

* `networkProfile()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `withNetworkProfile(models.NetworkProfile)` was added
* `publicNetworkAccess()` was added

#### `models.Certificate` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `BatchManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionsRequired()` was added

#### `models.BatchAccount$Definition` was modified

* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Pool$Definition` was modified

* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added

#### `models.BatchAccount$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccessType)` was added
* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.BatchAccountCreateParameters` was modified

* `networkProfile()` was added
* `withNetworkProfile(models.NetworkProfile)` was added

#### `models.Pool` was modified

* `currentNodeCommunicationMode()` was added
* `resourceGroupName()` was added
* `targetNodeCommunicationMode()` was added

#### `models.Pool$Update` was modified

* `withTargetNodeCommunicationMode(models.NodeCommunicationMode)` was added

## 1.0.0 (2022-03-23)

- Azure Resource Manager Batch client library for Java. This package contains Microsoft Azure SDK for Batch Management SDK. Batch Client. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `BatchManager` was modified

* `fluent.BatchManagement serviceClient()` -> `fluent.BatchManagementClient serviceClient()`

### Features Added

* `models.DynamicVNetAssignmentScope` was added

* `models.DetectorListResult` was added

* `models.DetectorResponse` was added

#### `models.NetworkConfiguration` was modified

* `dynamicVNetAssignmentScope()` was added
* `withDynamicVNetAssignmentScope(models.DynamicVNetAssignmentScope)` was added

#### `models.BatchAccounts` was modified

* `getDetectorWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listDetectors(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDetector(java.lang.String,java.lang.String,java.lang.String)` was added
* `listDetectors(java.lang.String,java.lang.String)` was added

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
