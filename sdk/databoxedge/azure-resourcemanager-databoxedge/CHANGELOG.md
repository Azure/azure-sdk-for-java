# Release History

## 1.1.0 (2026-03-06)

- Azure Resource Manager DataBoxEdge client library for Java. This package contains Microsoft Azure SDK for DataBoxEdge Management SDK.  Package api-version 2023-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Skus` was removed

#### `models.SkuRestrictionInfo` was removed

#### `models.SkuInformationList` was removed

#### `models.ContainerList` was removed

#### `models.SkuRestriction` was removed

#### `models.BandwidthSchedulesList` was removed

#### `models.OrderList` was removed

#### `models.SkuRestrictionReasonCode` was removed

#### `models.RoleList` was removed

#### `models.StorageAccountList` was removed

#### `models.DataBoxEdgeDeviceList` was removed

#### `models.ResourceTypeSku` was removed

#### `models.TriggerList` was removed

#### `models.OperationsList` was removed

#### `models.NodeList` was removed

#### `models.ShareList` was removed

#### `models.UserList` was removed

#### `models.AlertList` was removed

#### `models.StorageAccountCredentialList` was removed

#### `models.StorageAccount$DefinitionStages` was modified

* Required stage 2 was added

#### `models.JobErrorItem` was modified

* `JobErrorItem()` was changed to private access
* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.MetricDimensionV1` was modified

* `MetricDimensionV1()` was changed to private access
* `validate()` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.Address` was modified

* `validate()` was removed

#### `models.UpdateInstallProgress` was modified

* `UpdateInstallProgress()` was changed to private access
* `validate()` was removed

#### `models.TrackingInfo` was modified

* `TrackingInfo()` was changed to private access
* `withTrackingId(java.lang.String)` was removed
* `withSerialNumber(java.lang.String)` was removed
* `validate()` was removed
* `withTrackingUrl(java.lang.String)` was removed
* `withCarrierName(java.lang.String)` was removed

#### `models.RefreshDetails` was modified

* `validate()` was removed

#### `models.AlertErrorDetails` was modified

* `AlertErrorDetails()` was changed to private access
* `validate()` was removed

#### `models.RoleSinkInfo` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed

#### `models.ClientAccessRight` was modified

* `validate()` was removed

#### `models.PeriodicTimerSourceInfo` was modified

* `validate()` was removed

#### `models.SecuritySettings` was modified

* `validate()` was removed

#### `models.IoTRole` was modified

* `validate()` was removed

#### `models.JobErrorDetails` was modified

* `JobErrorDetails()` was changed to private access
* `validate()` was removed

#### `models.AzureContainerInfo` was modified

* `validate()` was removed

#### `models.MetricSpecificationV1` was modified

* `MetricSpecificationV1()` was changed to private access
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withSupportedAggregationTypes(java.util.List)` was removed
* `withCategory(models.MetricCategory)` was removed
* `withDimensions(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `withAggregationType(models.MetricAggregationType)` was removed
* `withUnit(models.MetricUnit)` was removed
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withResourceIdDimensionNameOverride(java.lang.String)` was removed

#### `models.SkuLocationInfo` was modified

* `SkuLocationInfo()` was changed to private access
* `validate()` was removed

#### `models.OrderState` was modified

* `AWAITING_FULFILMENT` was removed

#### `models.User$Definition` was modified

* `withShareAccessRights(java.util.List)` was removed

#### `DataBoxEdgeManager` was modified

* `skus()` was removed

#### `models.IoTDeviceInfo` was modified

* `validate()` was removed

#### `models.FileEventTrigger` was modified

* `validate()` was removed

#### `models.NetworkAdapter` was modified

* `NetworkAdapter()` was changed to private access
* `validate()` was removed
* `withDhcpStatus(models.NetworkAdapterDhcpStatus)` was removed
* `withRdmaStatus(models.NetworkAdapterRdmaStatus)` was removed

#### `models.MountPointMap` was modified

* `validate()` was removed

#### `models.NetworkAdapterPosition` was modified

* `NetworkAdapterPosition()` was changed to private access
* `validate()` was removed

#### `models.ShareAccessRight` was modified

* `ShareAccessRight()` was changed to private access
* `validate()` was removed
* `withShareId(java.lang.String)` was removed
* `withAccessType(models.ShareAccessType)` was removed

#### `models.AsymmetricEncryptedSecret` was modified

* `validate()` was removed

#### `models.UploadCertificateRequest` was modified

* `validate()` was removed

#### `models.PeriodicTimerEventTrigger` was modified

* `validate()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `validate()` was removed
* `withMetricSpecifications(java.util.List)` was removed

#### `models.Authentication` was modified

* `validate()` was removed

#### `models.OrderStatus` was modified

* `OrderStatus()` was changed to private access
* `validate()` was removed
* `withComments(java.lang.String)` was removed
* `withStatus(models.OrderState)` was removed

#### `models.Ipv4Config` was modified

* `Ipv4Config()` was changed to private access
* `validate()` was removed

#### `models.DataBoxEdgeDevicePatch` was modified

* `validate()` was removed

#### `models.SkuCost` was modified

* `SkuCost()` was changed to private access
* `validate()` was removed

#### `models.SymmetricKey` was modified

* `validate()` was removed

#### `models.DataBoxEdgeDevice$Definition` was modified

* `withDescription(java.lang.String)` was removed
* `withDataBoxEdgeDeviceStatus(models.DataBoxEdgeDeviceStatus)` was removed
* `withModelDescription(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.ContactDetails` was modified

* `validate()` was removed

#### `models.Ipv6Config` was modified

* `Ipv6Config()` was changed to private access
* `validate()` was removed

#### `models.FileSourceInfo` was modified

* `validate()` was removed

#### `models.User$Update` was modified

* `withShareAccessRights(java.util.List)` was removed

#### `models.ArmBaseModel` was modified

* `validate()` was removed

#### `models.UserAccessRight` was modified

* `validate()` was removed

#### `models.UpdateDownloadProgress` was modified

* `UpdateDownloadProgress()` was changed to private access
* `validate()` was removed

### Features Added

* `models.DiagnosticProactiveLogCollectionSettings` was added

* `models.MetricCounterSet` was added

* `models.DataResidency` was added

* `models.RemoteApplicationType` was added

* `models.ArcAddon` was added

* `models.UpdateStatus` was added

* `models.VmPlacementRequestResult` was added

* `models.SupportPackages` was added

* `models.UpdateDetails` was added

* `models.EdgeProfile` was added

* `models.TriggerSupportPackageRequest` was added

* `models.MetricDimension` was added

* `models.ShipmentType` was added

* `models.ClusterMemoryCapacity` was added

* `models.PosixComplianceStatus` was added

* `models.AvailableSkus` was added

* `models.CniConfig` was added

* `models.DeviceCapacityRequestInfo` was added

* `models.VmMemory` was added

* `models.SkuAvailability` was added

* `models.ResourceIdentity` was added

* `models.SkuVersion` was added

* `models.CloudEdgeManagementRole` was added

* `models.ResourceMoveDetails` was added

* `models.ClusterCapacityViewData` was added

* `models.UpdateType` was added

* `models.HostPlatformType` was added

* `models.GenerateCertResponse` was added

* `models.Secret` was added

* `models.KubernetesIPConfiguration` was added

* `models.KeyVaultSyncStatus` was added

* `models.RemoteSupportSettings` was added

* `models.KubernetesRoleStorage` was added

* `models.NumaNodeData` was added

* `models.KubernetesRoleNetwork` was added

* `models.EdgeProfileSubscription` was added

* `models.MountType` was added

* `models.MonitoringConfigs` was added

* `models.IoTEdgeAgentInfo` was added

* `models.DeviceCapacityInfo` was added

* `models.MetricCounter` was added

* `models.ProactiveDiagnosticsConsent` was added

* `models.DiagnosticRemoteSupportSettings` was added

* `models.AccessLevel` was added

* `models.LoadBalancerConfig` was added

* `models.ComputeResource` was added

* `models.DCAccessCode` was added

* `models.IoTAddon` was added

* `models.ClusterWitnessType` was added

* `models.ClusterGpuCapacity` was added

* `models.EdgeProfileSubscriptionPatch` was added

* `models.SubscriptionRegisteredFeatures` was added

* `models.InstallationImpact` was added

* `models.Addon` was added

* `models.EdgeProfilePatch` was added

* `models.MECRole` was added

* `models.MonitoringMetricConfiguration` was added

* `models.MsiIdentityType` was added

* `models.KubernetesState` was added

* `models.ImageRepositoryCredential` was added

* `models.SubscriptionState` was added

* `models.DiagnosticSettings` was added

* `models.ResourceMoveStatus` was added

* `models.KubernetesClusterInfo` was added

* `models.KubernetesRoleResources` was added

* `models.DataBoxEdgeSku` was added

* `models.SkuSignupOption` was added

* `models.Addons` was added

* `models.KubernetesRoleCompute` was added

* `models.DataResidencyType` was added

* `models.KubernetesNodeType` was added

* `models.KubernetesRoleStorageClassInfo` was added

* `models.AddonState` was added

* `models.NodeInfo` was added

* `models.ClusterStorageViewData` was added

* `models.HostCapacity` was added

* `models.DeviceCapacityInfoes` was added

* `models.MetricConfiguration` was added

* `models.EtcdInfo` was added

* `models.DataBoxEdgeDeviceKind` was added

* `models.SkuCapability` was added

* `models.KubernetesRole` was added

* `models.AddonType` was added

* `models.DeviceCapacityChecks` was added

* `models.DataBoxEdgeDeviceExtendedInfoPatch` was added

#### `models.SkuName` was modified

* `EP2_64_MX1_W` was added
* `TCA_LARGE` was added
* `GPU` was added
* `EP2_256_GPU2_MX1` was added
* `RDC` was added
* `EDGE_P_HIGH` was added
* `TDC` was added
* `EDGE_PR_BASE` was added
* `EDGE_PR_BASE_UPS` was added
* `EDGE_MR_MINI` was added
* `MANAGEMENT` was added
* `EP2_128_1T4_MX1_W` was added
* `EP2_128_GPU1_MX1_W` was added
* `TCA_SMALL` was added
* `EP2_64_1VPU_W` was added
* `RCA_LARGE` was added
* `EP2_256_2T4_W` was added
* `RCA_SMALL` was added
* `EDGE_P_BASE` was added
* `EDGE_MR_TCP` was added

#### `models.StorageAccountCredential` was modified

* `systemData()` was added

#### `models.JobType` was modified

* `BACKUP` was added
* `RESTORE` was added
* `TRIGGER_SUPPORT_PACKAGE` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.Alert` was modified

* `systemData()` was added

#### `models.DataBoxEdgeDeviceExtendedInfo` was modified

* `deviceSecrets()` was added
* `channelIntegrityKeyVersion()` was added
* `keyVaultSyncStatus()` was added
* `cloudWitnessStorageAccountName()` was added
* `clusterWitnessType()` was added
* `clientSecretStoreUrl()` was added
* `clientSecretStoreId()` was added
* `cloudWitnessStorageEndpoint()` was added
* `fileShareWitnessLocation()` was added
* `fileShareWitnessUsername()` was added
* `channelIntegrityKeyName()` was added
* `cloudWitnessContainerName()` was added
* `systemData()` was added

#### `models.Devices` was modified

* `updateExtendedInformation(java.lang.String,java.lang.String,models.DataBoxEdgeDeviceExtendedInfoPatch)` was added
* `updateExtendedInformationWithResponse(java.lang.String,java.lang.String,models.DataBoxEdgeDeviceExtendedInfoPatch,com.azure.core.util.Context)` was added
* `generateCertificate(java.lang.String,java.lang.String)` was added
* `generateCertificateWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Orders` was modified

* `listDCAccessCode(java.lang.String,java.lang.String)` was added
* `listDCAccessCodeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.IoTRole` was modified

* `hostPlatformType()` was added
* `systemData()` was added
* `withIoTEdgeAgentInfo(models.IoTEdgeAgentInfo)` was added
* `computeResource()` was added
* `withComputeResource(models.ComputeResource)` was added
* `ioTEdgeAgentInfo()` was added

#### `models.Trigger` was modified

* `systemData()` was added

#### `models.Order` was modified

* `systemData()` was added
* `kind()` was added
* `shipmentType()` was added
* `orderId()` was added

#### `models.OrderState` was modified

* `PICKUP_COMPLETED` was added
* `AWAITING_PICKUP` was added
* `AWAITING_DROP` was added
* `AWAITING_FULFILLMENT` was added

#### `DataBoxEdgeManager` was modified

* `addons()` was added
* `supportPackages()` was added
* `monitoringConfigs()` was added
* `availableSkus()` was added
* `diagnosticSettings()` was added
* `deviceCapacityInfoes()` was added
* `deviceCapacityChecks()` was added

#### `models.UpdateSummary` was modified

* `lastInstallJobStatus()` was added
* `lastSuccessfulScanJobTime()` was added
* `systemData()` was added
* `lastCompletedDownloadJobId()` was added
* `lastCompletedInstallJobId()` was added
* `updates()` was added
* `lastSuccessfulInstallJobDateTime()` was added
* `totalTimeInMinutes()` was added
* `lastDownloadJobStatus()` was added

#### `models.FileEventTrigger` was modified

* `systemData()` was added

#### `models.DataBoxEdgeDevice` was modified

* `systemDataPropertiesSystemData()` was added
* `kubernetesWorkloadProfile()` was added
* `kind()` was added
* `generateCertificateWithResponse(com.azure.core.util.Context)` was added
* `dataResidency()` was added
* `updateExtendedInformation(models.DataBoxEdgeDeviceExtendedInfoPatch)` was added
* `systemData()` was added
* `updateExtendedInformationWithResponse(models.DataBoxEdgeDeviceExtendedInfoPatch,com.azure.core.util.Context)` was added
* `generateCertificate()` was added
* `identity()` was added
* `edgeProfile()` was added
* `resourceMoveDetails()` was added

#### `models.Container` was modified

* `systemData()` was added

#### `models.MountPointMap` was modified

* `mountType()` was added

#### `models.User` was modified

* `systemData()` was added

#### `models.StorageAccount` was modified

* `systemData()` was added

#### `models.PeriodicTimerEventTrigger` was modified

* `systemData()` was added

#### `models.OrderStatus` was modified

* `trackingInformation()` was added

#### `models.Role` was modified

* `systemData()` was added

#### `models.DataBoxEdgeDevicePatch` was modified

* `withIdentity(models.ResourceIdentity)` was added
* `identity()` was added
* `withEdgeProfile(models.EdgeProfilePatch)` was added
* `edgeProfile()` was added

#### `models.Job` was modified

* `systemData()` was added

#### `models.DataBoxEdgeDevice$Definition` was modified

* `withIdentity(models.ResourceIdentity)` was added
* `withDataResidency(models.DataResidency)` was added

#### `models.DataBoxEdgeDevice$Update` was modified

* `withEdgeProfile(models.EdgeProfilePatch)` was added
* `withIdentity(models.ResourceIdentity)` was added

#### `models.NetworkSettings` was modified

* `systemData()` was added

#### `models.Share` was modified

* `systemData()` was added

#### `models.BandwidthSchedule` was modified

* `systemData()` was added

#### `models.RoleTypes` was modified

* `CLOUD_EDGE_MANAGEMENT` was added
* `KUBERNETES` was added
* `MEC` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager DataBoxEdge client library for Java. This package contains Microsoft Azure SDK for DataBoxEdge Management SDK.  Package tag package-2019-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager DataBoxEdge client library for Java.

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager DataBoxEdge client library for Java. This package contains Microsoft Azure SDK for DataBoxEdge Management SDK.  Package tag package-2019-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.JobErrorItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuRestrictionInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetricDimensionV1` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuInformationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Address` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateInstallProgress` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TrackingInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RefreshDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuRestriction` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BandwidthSchedulesList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OrderList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertErrorDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RoleSinkInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientAccessRight` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeriodicTimerSourceInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecuritySettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.IoTRole` was modified

* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RoleList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Trigger` was modified

* `kind()` was added

#### `models.JobErrorDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureContainerInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageAccountList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxEdgeDeviceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetricSpecificationV1` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuLocationInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IoTDeviceInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileEventTrigger` was modified

* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.NetworkAdapter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MountPointMap` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkAdapterPosition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShareAccessRight` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AsymmetricEncryptedSecret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UploadCertificateRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NodeList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeriodicTimerEventTrigger` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added
* `kind()` was added

#### `models.ServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Authentication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrderStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Ipv4Config` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Role` was modified

* `kind()` was added

#### `models.DataBoxEdgeDevicePatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCost` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShareList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SymmetricKey` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContactDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageAccountCredentialList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Ipv6Config` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileSourceInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ArmBaseModel` was modified

* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAccessRight` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateDownloadProgress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager DataBoxEdge client library for Java. This package contains Microsoft Azure SDK for DataBoxEdge Management SDK.  Package tag package-2019-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.StorageAccountCredential` was modified

* `resourceGroupName()` was added

#### `DataBoxEdgeManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `DataBoxEdgeManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.DataBoxEdgeDevice` was modified

* `resourceGroupName()` was added

#### `models.Container` was modified

* `resourceGroupName()` was added

#### `models.User` was modified

* `resourceGroupName()` was added

#### `models.StorageAccount` was modified

* `resourceGroupName()` was added

#### `models.Share` was modified

* `resourceGroupName()` was added

#### `models.BandwidthSchedule` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-09)

- Azure Resource Manager DataBoxEdge client library for Java. This package contains Microsoft Azure SDK for DataBoxEdge Management SDK.  Package tag package-2019-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
