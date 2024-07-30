# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2024-06-17)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.Ipv4Route` was added

* `models.RoutingInfoModel` was added

* `models.UserConsentConfiguration` was added

* `models.SimMove` was added

* `models.UserPlaneDataRoutesItem` was added

* `models.Ipv4RouteNextHop` was added

* `models.RoutingInfoListResult` was added

* `models.SimGroupResourceId` was added

* `models.NasEncryptionType` was added

* `models.RoutingInfoes` was added

* `models.SimClone` was added

#### `models.ExtendedUeInfoProperties` was modified

* `ratType()` was added

#### `models.PacketCoreControlPlane` was modified

* `userConsent()` was added

#### `models.UeInfo4G` was modified

* `ratType()` was added

#### `models.Sims` was modified

* `clone(java.lang.String,java.lang.String,models.SimClone)` was added
* `move(java.lang.String,java.lang.String,models.SimMove,com.azure.core.util.Context)` was added
* `move(java.lang.String,java.lang.String,models.SimMove)` was added
* `clone(java.lang.String,java.lang.String,models.SimClone,com.azure.core.util.Context)` was added

#### `models.Platform` was modified

* `withHaUpgradesAvailable(java.util.List)` was added
* `haUpgradesAvailable()` was added

#### `models.UeInfo5G` was modified

* `ratType()` was added

#### `models.SignalingConfiguration` was modified

* `nasEncryption()` was added
* `withNasEncryption(java.util.List)` was added

#### `models.MobileNetwork` was modified

* `listSimGroups()` was added
* `listSimGroups(com.azure.core.util.Context)` was added

#### `models.MobileNetworks` was modified

* `listSimGroups(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSimGroups(java.lang.String,java.lang.String)` was added

#### `MobileNetworkManager` was modified

* `routingInfoes()` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withUserConsent(models.UserConsentConfiguration)` was added

#### `models.InterfaceProperties` was modified

* `bfdIpv4Endpoints()` was added
* `withIpv4AddressList(java.util.List)` was added
* `withBfdIpv4Endpoints(java.util.List)` was added
* `ipv4AddressList()` was added
* `withVlanId(java.lang.Integer)` was added
* `vlanId()` was added

## 1.1.0 (2024-03-19)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.GlobalRanNodeId` was added

* `models.UeInformations` was added

* `models.HomeNetworkPrivateKeysProvisioning` was added

* `models.UeIpAddress` was added

* `models.PublicLandMobileNetworkHomeNetworkPublicKeys` was added

* `models.UeInfo` was added

* `models.RatType` was added

* `models.AmfId` was added

* `models.HomeNetworkPublicKey` was added

* `models.ExtendedUeInfoProperties` was added

* `models.UeInfo4GProperties` was added

* `models.UeUsageSetting` was added

* `models.UeSessionInfo5G` was added

* `models.MmeId` was added

* `models.UeSessionInfo4G` was added

* `models.Guti5G` was added

* `models.GNbId` was added

* `models.ExtendedUeInformations` was added

* `models.Guti4G` was added

* `models.UeConnectionInfo5G` was added

* `models.UeInfoList` was added

* `models.UeInfo4G` was added

* `models.UeConnectionInfo4G` was added

* `models.UeInfo5G` was added

* `models.UeQosFlow` was added

* `models.UeState` was added

* `models.UeInfo5GProperties` was added

* `models.PublicLandMobileNetwork` was added

* `models.PdnType` was added

* `models.UeInfoPropertiesFormat` was added

* `models.ExtendedUeInfo` was added

* `models.HomeNetworkPrivateKeysProvisioningState` was added

* `models.UeLocationInfo` was added

* `models.RrcEstablishmentCause` was added

* `models.DnnIpPair` was added

#### `models.PacketCoreControlPlane` was modified

* `homeNetworkPrivateKeysProvisioning()` was added

#### `models.MobileNetwork$Definition` was modified

* `withPublicLandMobileNetworks(java.util.List)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MobileNetwork$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MobileNetwork` was modified

* `publicLandMobileNetworks()` was added
* `identity()` was added

#### `MobileNetworkManager` was modified

* `ueInformations()` was added
* `extendedUeInformations()` was added

## 1.0.0 (2023-11-15)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2023-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SignalingConfiguration` was added

* `models.NasRerouteConfiguration` was added

* `models.EventHubConfiguration` was added

#### `models.PacketCoreDataPlane$Definition` was modified

* `withUserPlaneAccessVirtualIpv4Addresses(java.util.List)` was added

#### `models.PacketCoreControlPlaneVersion` was modified

* `systemData()` was added

#### `models.PacketCoreControlPlane` was modified

* `signaling()` was added
* `controlPlaneAccessVirtualIpv4Addresses()` was added
* `eventHub()` was added

#### `models.Sim` was modified

* `systemData()` was added

#### `models.PacketCapture` was modified

* `outputFiles()` was added
* `systemData()` was added

#### `models.PacketCoreDataPlane` was modified

* `userPlaneAccessVirtualIpv4Addresses()` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withEventHub(models.EventHubConfiguration)` was added
* `withSignaling(models.SignalingConfiguration)` was added
* `withControlPlaneAccessVirtualIpv4Addresses(java.util.List)` was added

## 1.0.0-beta.6 (2023-07-19)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedServiceIdentity` was modified

* `tenantId()` was removed
* `principalId()` was removed

#### `models.PacketCoreControlPlaneVersion` was modified

* `systemData()` was removed

#### `models.Sim` was modified

* `systemData()` was removed

#### `models.Installation` was modified

* `withState(models.InstallationState)` was removed
* `withOperation(models.AsyncOperationId)` was removed

### Features Added

* `models.DiagnosticsPackage` was added

* `models.InstallationReason` was added

* `models.PacketCapture$Definition` was added

* `models.PacketCoreControlPlaneResourceId` was added

* `models.ReinstallRequired` was added

* `models.IdentityAndTagsObject` was added

* `models.PacketCapture$UpdateStages` was added

* `models.PacketCaptureStatus` was added

* `models.PacketCapture$DefinitionStages` was added

* `models.PacketCapture` was added

* `models.PacketCapture$Update` was added

* `models.DiagnosticsPackages` was added

* `models.PacketCaptures` was added

* `models.PacketCaptureListResult` was added

* `models.DesiredInstallationState` was added

* `models.SiteDeletePacketCore` was added

* `models.DiagnosticsPackageStatus` was added

* `models.DiagnosticsPackageListResult` was added

* `models.DiagnosticsUploadConfiguration` was added

#### `models.PacketCoreControlPlane$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.PacketCoreControlPlaneVersions` was modified

* `getBySubscriptionWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listBySubscription()` was added
* `listBySubscription(com.azure.core.util.Context)` was added
* `getBySubscription(java.lang.String)` was added

#### `models.PacketCoreControlPlane` was modified

* `diagnosticsUpload()` was added
* `installedVersion()` was added

#### `models.Site` was modified

* `deletePacketCore(models.SiteDeletePacketCore)` was added
* `deletePacketCore(models.SiteDeletePacketCore,com.azure.core.util.Context)` was added

#### `models.Sites` was modified

* `deletePacketCore(java.lang.String,java.lang.String,java.lang.String,models.SiteDeletePacketCore)` was added
* `deletePacketCore(java.lang.String,java.lang.String,java.lang.String,models.SiteDeletePacketCore,com.azure.core.util.Context)` was added

#### `models.Installation` was modified

* `withDesiredState(models.DesiredInstallationState)` was added
* `reasons()` was added
* `reinstallRequired()` was added
* `desiredState()` was added

#### `models.SimGroup$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `MobileNetworkManager` was modified

* `diagnosticsPackages()` was added
* `packetCaptures()` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withInstallation(models.Installation)` was added
* `withDiagnosticsUpload(models.DiagnosticsUploadConfiguration)` was added

## 1.0.0-beta.5 (2023-01-16)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PacketCoreControlPlaneOperations` was removed

* `models.SimOperations` was removed

#### `MobileNetworkManager` was modified

* `simOperations()` was removed
* `packetCoreControlPlaneOperations()` was removed

### Features Added

#### `models.PacketCoreControlPlane` was modified

* `collectDiagnosticsPackage(models.PacketCoreControlPlaneCollectDiagnosticsPackage)` was added
* `rollback()` was added
* `collectDiagnosticsPackage(models.PacketCoreControlPlaneCollectDiagnosticsPackage,com.azure.core.util.Context)` was added
* `reinstall(com.azure.core.util.Context)` was added
* `reinstall()` was added
* `rollback(com.azure.core.util.Context)` was added

#### `models.Sims` was modified

* `bulkUpload(java.lang.String,java.lang.String,models.SimUploadList,com.azure.core.util.Context)` was added
* `bulkDelete(java.lang.String,java.lang.String,models.SimDeleteList,com.azure.core.util.Context)` was added
* `bulkUploadEncrypted(java.lang.String,java.lang.String,models.EncryptedSimUploadList,com.azure.core.util.Context)` was added
* `bulkDelete(java.lang.String,java.lang.String,models.SimDeleteList)` was added
* `bulkUpload(java.lang.String,java.lang.String,models.SimUploadList)` was added
* `bulkUploadEncrypted(java.lang.String,java.lang.String,models.EncryptedSimUploadList)` was added

#### `models.PacketCoreControlPlanes` was modified

* `reinstall(java.lang.String,java.lang.String)` was added
* `collectDiagnosticsPackage(java.lang.String,java.lang.String,models.PacketCoreControlPlaneCollectDiagnosticsPackage,com.azure.core.util.Context)` was added
* `rollback(java.lang.String,java.lang.String)` was added
* `collectDiagnosticsPackage(java.lang.String,java.lang.String,models.PacketCoreControlPlaneCollectDiagnosticsPackage)` was added
* `reinstall(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `rollback(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.4 (2022-12-21)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this API specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SimIdListResult` was removed

* `models.KeyVaultCertificate` was removed

#### `models.AttachedDataNetwork$DefinitionStages` was modified

* Stage 4 was added

#### `models.PacketCoreControlPlane$DefinitionStages` was modified

* `withMobileNetwork(models.MobileNetworkResourceId)` was removed in stage 3
* `withControlPlaneAccessInterface(models.InterfaceProperties)` was removed in stage 4
* `withSku(models.BillingSku)` was removed in stage 5
* Stage 6, 7 was added

#### `models.PacketCoreControlPlaneVersions` was modified

* `listByResourceGroup(com.azure.core.util.Context)` was removed
* `listByResourceGroup()` was removed

#### `models.PacketCoreControlPlaneVersion` was modified

* `versionState()` was removed
* `recommendedVersion()` was removed

#### `models.PacketCoreControlPlane` was modified

* `mobileNetwork()` was removed

#### `models.Sims` was modified

* `listBySimGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listBySimGroup(java.lang.String,java.lang.String)` was removed

#### `models.Site$Definition` was modified

* `withNetworkFunctions(java.util.List)` was removed

#### `models.LocalDiagnosticsAccessConfiguration` was modified

* `models.KeyVaultCertificate httpsServerCertificate()` -> `models.HttpsServerCertificate httpsServerCertificate()`
* `withHttpsServerCertificate(models.KeyVaultCertificate)` was removed

#### `models.MobileNetwork` was modified

* `listSimIds(com.azure.core.util.Context)` was removed
* `listSimIds()` was removed

#### `models.MobileNetworks` was modified

* `listSimIds(java.lang.String,java.lang.String)` was removed
* `listSimIds(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CoreNetworkType` was modified

* `java.util.Collection values()` -> `models.CoreNetworkType[] values()`

#### `models.PacketCoreControlPlane$Definition` was modified

* `withMobileNetwork(models.MobileNetworkResourceId)` was removed

### Features Added

* `models.CertificateProvisioning` was added

* `models.PacketCoreControlPlaneOperations` was added

* `models.AuthenticationType` was added

* `models.PacketCoreControlPlaneCollectDiagnosticsPackage` was added

* `models.SimUploadList` was added

* `models.SimDeleteList` was added

* `models.SiteProvisioningState` was added

* `models.CertificateProvisioningState` was added

* `models.SimOperations` was added

* `models.HttpsServerCertificate` was added

* `models.AsyncOperationId` was added

* `models.SiteResourceId` was added

* `models.InstallationState` was added

* `models.ObsoleteVersion` was added

* `models.Installation` was added

* `models.Platform` was added

* `models.EncryptedSimUploadList` was added

* `models.CommonSimPropertiesFormat` was added

* `models.SimNameAndEncryptedProperties` was added

* `models.AzureStackHciClusterResourceId` was added

* `models.SimNameAndProperties` was added

* `models.AsyncOperationStatus` was added

#### `models.PacketCoreControlPlaneVersions` was modified

* `list(com.azure.core.util.Context)` was added
* `list()` was added

#### `models.DataNetworkConfiguration` was modified

* `maximumNumberOfBufferedPackets()` was added
* `withMaximumNumberOfBufferedPackets(java.lang.Integer)` was added

#### `models.PacketCoreControlPlaneVersion` was modified

* `platforms()` was added

#### `models.PacketCoreControlPlane` was modified

* `installation()` was added
* `rollbackVersion()` was added
* `ueMtu()` was added
* `sites()` was added

#### `models.Sim` was modified

* `siteProvisioningState()` was added
* `vendorKeyFingerprint()` was added
* `systemData()` was added
* `vendorName()` was added

#### `models.Sims` was modified

* `listByGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByGroup(java.lang.String,java.lang.String)` was added

#### `models.LocalDiagnosticsAccessConfiguration` was modified

* `authenticationType()` was added
* `withAuthenticationType(models.AuthenticationType)` was added
* `withHttpsServerCertificate(models.HttpsServerCertificate)` was added

#### `models.SimPolicy` was modified

* `siteProvisioningState()` was added

#### `models.PlatformConfiguration` was modified

* `withAzureStackHciCluster(models.AzureStackHciClusterResourceId)` was added
* `azureStackEdgeDevices()` was added
* `azureStackHciCluster()` was added

#### `models.AttachedDataNetwork` was modified

* `systemData()` was added

#### `models.CoreNetworkType` was modified

* `valueOf(java.lang.String)` was added
* `toString()` was added

#### `MobileNetworkManager` was modified

* `simOperations()` was added
* `packetCoreControlPlaneOperations()` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withUeMtu(java.lang.Integer)` was added
* `withSites(java.util.List)` was added

## 1.0.0-beta.3 (2022-07-25)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Sim$DefinitionStages` was modified

* `withRegion(java.lang.String)` was removed in stage 1
* `withRegion(com.azure.core.management.Region)` was removed in stage 1
* `withExistingResourceGroup(java.lang.String)` was removed in stage 2

#### `models.PacketCoreControlPlane$DefinitionStages` was modified

* Stage 5 was added

#### `models.PacketCoreControlPlane` was modified

* `customLocation()` was removed

#### `models.Sim` was modified

* `regionName()` was removed
* `mobileNetwork()` was removed
* `region()` was removed
* `location()` was removed
* `systemData()` was removed
* `tags()` was removed

#### `models.Sims` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `getByResourceGroup(java.lang.String,java.lang.String)` was removed
* `listByResourceGroup(java.lang.String)` was removed
* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list()` was removed

#### `models.Sim$Definition` was modified

* `withRegion(java.lang.String)` was removed
* `withMobileNetwork(models.MobileNetworkResourceId)` was removed
* `withExistingResourceGroup(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withRegion(com.azure.core.management.Region)` was removed

#### `models.PacketCoreControlPlane$Definition` was modified

* `withCustomLocation(models.CustomLocationResourceId)` was removed

#### `models.Sim$Update` was modified

* `withTags(java.util.Map)` was removed

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.RecommendedVersion` was added

* `models.ConnectedClusterResourceId` was added

* `models.BillingSku` was added

* `models.SimGroupListResult` was added

* `models.PacketCoreControlPlaneVersions` was added

* `models.UserAssignedIdentity` was added

* `models.PacketCoreControlPlaneVersion` was added

* `models.SimGroups` was added

* `models.ManagedServiceIdentityType` was added

* `models.KeyVaultCertificate` was added

* `models.KeyVaultKey` was added

* `models.SimGroup$Definition` was added

* `models.PacketCoreControlPlaneVersionListResult` was added

* `models.LocalDiagnosticsAccessConfiguration` was added

* `models.SimGroup$UpdateStages` was added

* `models.SimGroup$Update` was added

* `models.PlatformType` was added

* `models.AzureStackEdgeDeviceResourceId` was added

* `models.PlatformConfiguration` was added

* `models.SimGroup$DefinitionStages` was added

* `models.SimGroup` was added

* `models.VersionState` was added

#### `models.DataNetwork` was modified

* `resourceGroupName()` was added

#### `models.Slice` was modified

* `resourceGroupName()` was added

#### `models.PacketCoreControlPlane` was modified

* `resourceGroupName()` was added
* `localDiagnosticsAccess()` was added
* `sku()` was added
* `interopSettings()` was added
* `platform()` was added
* `identity()` was added

#### `models.Site` was modified

* `resourceGroupName()` was added

#### `models.Sim` was modified

* `resourceGroupName()` was added

#### `models.Sims` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySimGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySimGroup(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.AttachedDataNetwork$Definition` was modified

* `withDnsAddresses(java.util.List)` was added

#### `models.SimPolicy` was modified

* `resourceGroupName()` was added

#### `models.MobileNetwork` was modified

* `resourceGroupName()` was added

#### `models.AttachedDataNetwork` was modified

* `resourceGroupName()` was added
* `dnsAddresses()` was added

#### `models.PacketCoreDataPlane` was modified

* `resourceGroupName()` was added

#### `models.Sim$Definition` was modified

* `withExistingSimGroup(java.lang.String,java.lang.String)` was added

#### `MobileNetworkManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `simGroups()` was added
* `packetCoreControlPlaneVersions()` was added

#### `models.Service` was modified

* `resourceGroupName()` was added

#### `MobileNetworkManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withPlatform(models.PlatformConfiguration)` was added
* `withLocalDiagnosticsAccess(models.LocalDiagnosticsAccessConfiguration)` was added
* `withInteropSettings(java.lang.Object)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withSku(models.BillingSku)` was added

#### `models.Sim$Update` was modified

* `withAuthenticationKey(java.lang.String)` was added
* `withSimPolicy(models.SimPolicyResourceId)` was added
* `withStaticIpConfiguration(java.util.List)` was added
* `withDeviceType(java.lang.String)` was added
* `withIntegratedCircuitCardIdentifier(java.lang.String)` was added
* `withOperatorKeyCode(java.lang.String)` was added

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
