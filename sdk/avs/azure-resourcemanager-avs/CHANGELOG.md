# Release History

## 1.2.0 (Unreleased)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.2.0-beta.1 (2024-06-25)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ServiceSpecification` was removed

* `models.MetricSpecification` was removed

* `models.WorkloadNetworkName` was removed

* `models.MetricDimension` was removed

* `models.OperationList` was removed

* `models.LogSpecification` was removed

* `models.OperationProperties` was removed

* `models.CommonClusterProperties` was removed

#### `models.WorkloadNetworks` was modified

* `getWithResponse(java.lang.String,java.lang.String,models.WorkloadNetworkName,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,models.WorkloadNetworkName)` was removed

#### `models.Operation` was modified

* `java.lang.String origin()` -> `models.Origin origin()`
* `properties()` was removed

#### `models.ManagementCluster` was modified

* `withHosts(java.util.List)` was removed
* `withClusterSize(java.lang.Integer)` was removed

### Features Added

* `models.CloudLinkProvisioningState` was added

* `models.IscsiPath` was added

* `models.IscsiPaths` was added

* `models.ScriptCmdletAudience` was added

* `models.VirtualMachineProvisioningState` was added

* `models.HcxEnterpriseSiteProvisioningState` was added

* `models.ActionType` was added

* `models.WorkloadNetworkProvisioningState` was added

* `models.SkuTier` was added

* `models.ScriptCmdletProvisioningState` was added

* `models.DnsZoneType` was added

* `models.ElasticSanVolume` was added

* `models.IscsiPathProvisioningState` was added

* `models.ScriptPackageProvisioningState` was added

* `models.OperationListResult` was added

* `models.Origin` was added

* `models.IscsiPathListResult` was added

#### `models.Cluster$Definition` was modified

* `withVsanDatastoreName(java.lang.String)` was added

#### `models.DatastoreList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added

#### `models.ClusterList` was modified

* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkSegmentPortVif` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptStringExecutionParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.WorkloadNetworkDhcpRelay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `segments()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added
* `dhcpType()` was added

#### `models.ScriptCmdlet` was modified

* `audience()` was added
* `provisioningState()` was added
* `systemData()` was added

#### `models.GlobalReachConnectionList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withValue(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added

#### `models.Cluster` was modified

* `systemData()` was added
* `vsanDatastoreName()` was added

#### `models.WorkloadNetworkSegmentSubnet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailabilityProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GlobalReachConnection` was modified

* `systemData()` was added

#### `models.DiskPoolVolume` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadNetworkDhcpEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dhcpType()` was added

#### `models.WorkloadNetworks` was modified

* `get(java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operation` was modified

* `actionType()` was added

#### `models.AddonList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withValue(java.util.List)` was added

#### `models.AddonArcProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `addonType()` was added

#### `models.ExpressRouteAuthorization` was modified

* `systemData()` was added

#### `models.VmPlacementPolicyProperties` was modified

* `type()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachine` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.VirtualMachinesList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadNetworkDhcpList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionKeyVaultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadNetworkDnsZone` was modified

* `systemData()` was added

#### `models.PlacementPolicy` was modified

* `systemData()` was added

#### `models.ScriptPackage` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.Addon` was modified

* `systemData()` was added

#### `models.Datastore` was modified

* `elasticSanVolume()` was added
* `systemData()` was added

#### `models.ClusterZone` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadNetworkSegment` was modified

* `systemData()` was added

#### `models.Datastore$Definition` was modified

* `withElasticSanVolume(models.ElasticSanVolume)` was added

#### `models.PrivateCloud$Update` was modified

* `withSku(models.Sku)` was added
* `withDnsZoneType(models.DnsZoneType)` was added

#### `models.AddonVrProperties` was modified

* `addonType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptCmdletsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withValue(java.util.List)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IdentitySource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkVirtualMachine` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.PlacementPolicyUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `AvsManager` was modified

* `iscsiPaths()` was added

#### `models.CloudLinkList` was modified

* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddonProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `addonType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkVMGroupsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added

#### `models.Encryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetAppVolume` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudLink` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.ScriptExecutionsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added
* `withNextLink(java.lang.String)` was added

#### `models.WorkloadNetwork` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.HcxEnterpriseSiteList` was modified

* `withValue(java.util.List)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagementCluster` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `clusterSize()` was added
* `withVsanDatastoreName(java.lang.String)` was added
* `vsanDatastoreName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `hosts()` was added
* `clusterId()` was added

#### `models.VmHostPlacementPolicyProperties` was modified

* `provisioningState()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateCloudUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `sku()` was added
* `withDnsZoneType(models.DnsZoneType)` was added
* `withSku(models.Sku)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `dnsZoneType()` was added

#### `models.WorkloadNetworkPortMirroringList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added

#### `models.ScriptSecureStringExecutionParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkPortMirroring` was modified

* `systemData()` was added

#### `models.ScriptExecution` was modified

* `systemData()` was added

#### `models.WorkloadNetworkPublicIp` was modified

* `systemData()` was added

#### `models.WorkloadNetworkVirtualMachinesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added

#### `models.PrivateCloud$Definition` was modified

* `withVirtualNetworkId(java.lang.String)` was added
* `withDnsZoneType(models.DnsZoneType)` was added

#### `models.WorkloadNetworkVMGroup` was modified

* `systemData()` was added

#### `models.ExpressRouteAuthorization$Definition` was modified

* `withExpressRouteId(java.lang.String)` was added

#### `models.ExpressRouteAuthorization$Update` was modified

* `withExpressRouteId(java.lang.String)` was added

#### `models.Cluster$Update` was modified

* `withSku(models.Sku)` was added

#### `models.AddonHcxProperties` was modified

* `provisioningState()` was added
* `addonType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Endpoints` was modified

* `hcxCloudManagerIp()` was added
* `vcenterIp()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `nsxtManagerIp()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PlacementPolicyProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.PSCredentialExecutionParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkList` was modified

* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HcxEnterpriseSite` was modified

* `provisioningState()` was added
* `systemData()` was added

#### `models.Datastore$Update` was modified

* `withElasticSanVolume(models.ElasticSanVolume)` was added

#### `models.PlacementPoliciesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateCloudIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `sku()` was added
* `withSku(models.Sku)` was added

#### `models.WorkloadNetworkPublicIPsList` was modified

* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineRestrictMovement` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `family()` was added
* `size()` was added
* `capacity()` was added
* `withSize(java.lang.String)` was added
* `withCapacity(java.lang.Integer)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `tier()` was added
* `withFamily(java.lang.String)` was added
* `withTier(models.SkuTier)` was added

#### `models.WorkloadNetworkSegmentsList` was modified

* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added

#### `models.ScriptExecutionParameter` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkGateway` was modified

* `systemData()` was added
* `provisioningState()` was added

#### `models.PrivateCloud` was modified

* `virtualNetworkId()` was added
* `dnsZoneType()` was added
* `systemData()` was added

#### `models.WorkloadNetworkDnsService` was modified

* `systemData()` was added

#### `models.WorkloadNetworkDnsServicesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added

#### `models.PrivateCloudList` was modified

* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExpressRouteAuthorizationList` was modified

* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withValue(java.util.List)` was added

#### `models.WorkloadNetworkDnsZonesList` was modified

* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added

#### `models.AddonSrmProperties` was modified

* `addonType()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkGatewayList` was modified

* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withValue(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadNetworkDhcp` was modified

* `systemData()` was added

#### `models.ScriptPackagesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added

#### `models.Circuit` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadNetworkDhcpServer` was modified

* `dhcpType()` was added
* `provisioningState()` was added
* `segments()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0 (2023-08-22)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2023-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.PrivateCloud$Update` was modified

* `withExtendedNetworkBlocks(java.util.List)` was added

#### `models.PrivateCloudUpdate` was modified

* `extendedNetworkBlocks()` was added
* `withExtendedNetworkBlocks(java.util.List)` was added

#### `models.PrivateCloud$Definition` was modified

* `withExtendedNetworkBlocks(java.util.List)` was added

#### `models.PrivateCloud` was modified

* `extendedNetworkBlocks()` was added

## 1.0.0 (2023-02-24)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Cluster$Definition` was modified

* `withClusterSize(java.lang.Integer)` was added
* `withHosts(java.util.List)` was added

#### `models.Cluster` was modified

* `provisioningState()` was added
* `clusterId()` was added
* `hosts()` was added
* `clusterSize()` was added

## 1.0.0-beta.4 (2022-10-17)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Locations` was modified

* `checkTrialAvailabilityWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PrivateCloud$Update` was modified

* `withAvailability(models.AvailabilityProperties)` was removed

### Features Added

* `models.AddonArcProperties` was added

* `models.WorkloadNetworkName` was added

* `models.NsxPublicIpQuotaRaisedEnum` was added

* `models.ClusterZone` was added

* `models.AzureHybridBenefitType` was added

* `models.WorkloadNetwork` was added

* `models.AffinityStrength` was added

* `models.WorkloadNetworkList` was added

* `models.ClusterZoneList` was added

#### `models.Locations` was modified

* `checkTrialAvailabilityWithResponse(java.lang.String,models.Sku,com.azure.core.util.Context)` was added

#### `models.Cluster` was modified

* `listZonesWithResponse(com.azure.core.util.Context)` was added
* `listZones()` was added
* `resourceGroupName()` was added

#### `models.GlobalReachConnection` was modified

* `resourceGroupName()` was added

#### `models.WorkloadNetworks` was modified

* `getWithResponse(java.lang.String,java.lang.String,models.WorkloadNetworkName,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,models.WorkloadNetworkName)` was added
* `list(java.lang.String,java.lang.String)` was added

#### `models.ExpressRouteAuthorization` was modified

* `resourceGroupName()` was added

#### `models.EncryptionKeyVaultProperties` was modified

* `autoDetectedKeyVersion()` was added

#### `models.WorkloadNetworkDnsZone` was modified

* `resourceGroupName()` was added

#### `models.PlacementPolicy` was modified

* `resourceGroupName()` was added

#### `models.ScriptPackage` was modified

* `company()` was added
* `uri()` was added

#### `models.Addon` was modified

* `resourceGroupName()` was added

#### `models.Datastore` was modified

* `resourceGroupName()` was added

#### `models.PlacementPolicy$Update` was modified

* `withAzureHybridBenefitType(models.AzureHybridBenefitType)` was added
* `withAffinityStrength(models.AffinityStrength)` was added

#### `models.WorkloadNetworkSegment` was modified

* `resourceGroupName()` was added

#### `models.PlacementPolicyUpdate` was modified

* `withAffinityStrength(models.AffinityStrength)` was added
* `azureHybridBenefitType()` was added
* `affinityStrength()` was added
* `withAzureHybridBenefitType(models.AzureHybridBenefitType)` was added

#### `AvsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.CloudLink` was modified

* `resourceGroupName()` was added

#### `AvsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.VmHostPlacementPolicyProperties` was modified

* `withAffinityStrength(models.AffinityStrength)` was added
* `azureHybridBenefitType()` was added
* `withAzureHybridBenefitType(models.AzureHybridBenefitType)` was added
* `affinityStrength()` was added

#### `models.WorkloadNetworkPortMirroring` was modified

* `resourceGroupName()` was added

#### `models.ScriptExecution` was modified

* `resourceGroupName()` was added

#### `models.WorkloadNetworkVMGroup` was modified

* `resourceGroupName()` was added

#### `models.HcxEnterpriseSite` was modified

* `resourceGroupName()` was added

#### `models.Clusters` was modified

* `listZones(java.lang.String,java.lang.String,java.lang.String)` was added
* `listZonesWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrivateCloud` was modified

* `resourceGroupName()` was added
* `nsxPublicIpQuotaRaised()` was added

#### `models.WorkloadNetworkDnsService` was modified

* `resourceGroupName()` was added

#### `models.WorkloadNetworkDhcp` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.3 (2021-11-10)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2021-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateCloudProperties` was removed

* `models.PrivateCloudUpdateProperties` was removed

* `models.ClusterProperties` was removed

#### `models.Cluster$Definition` was modified

* `withClusterSize(java.lang.Integer)` was removed

#### `models.Cluster` was modified

* `hosts()` was removed
* `clusterId()` was removed
* `clusterSize()` was removed
* `provisioningState()` was removed

### Features Added

* `models.EncryptionVersionType` was added

* `models.AvailabilityStrategy` was added

* `models.VmHostPlacementPolicyProperties` was added

* `models.AvailabilityProperties` was added

* `models.PlacementPolicy$UpdateStages` was added

* `models.PlacementPolicyState` was added

* `models.EncryptionState` was added

* `models.ResourceIdentityType` was added

* `models.AffinityType` was added

* `models.VmPlacementPolicyProperties` was added

* `models.PlacementPolicyProperties` was added

* `models.VirtualMachine` was added

* `models.DatastoreStatus` was added

* `models.VirtualMachineRestrictMovementState` was added

* `models.VirtualMachinesList` was added

* `models.EncryptionKeyVaultProperties` was added

* `models.PlacementPoliciesList` was added

* `models.PrivateCloudIdentity` was added

* `models.PlacementPolicy` was added

* `models.VirtualMachineRestrictMovement` was added

* `models.PlacementPolicy$Update` was added

* `models.EncryptionKeyStatus` was added

* `models.PlacementPolicy$Definition` was added

* `models.PlacementPolicyType` was added

* `models.PlacementPolicy$DefinitionStages` was added

* `models.PlacementPolicies` was added

* `models.PlacementPolicyUpdate` was added

* `models.VirtualMachines` was added

* `models.PlacementPolicyProvisioningState` was added

* `models.Encryption` was added

#### `models.ManagementCluster` was modified

* `withHosts(java.util.List)` was added
* `withHosts(java.util.List)` was added

#### `models.PrivateCloudUpdate` was modified

* `identity()` was added
* `encryption()` was added
* `withAvailability(models.AvailabilityProperties)` was added
* `withEncryption(models.Encryption)` was added
* `availability()` was added
* `withIdentity(models.PrivateCloudIdentity)` was added

#### `models.PrivateCloud$Definition` was modified

* `withEncryption(models.Encryption)` was added
* `withIdentity(models.PrivateCloudIdentity)` was added
* `withAvailability(models.AvailabilityProperties)` was added
* `withSecondaryCircuit(models.Circuit)` was added

#### `models.GlobalReachConnection` was modified

* `expressRouteId()` was added

#### `models.Cluster$Update` was modified

* `withHosts(java.util.List)` was added

#### `models.ExpressRouteAuthorization` was modified

* `expressRouteId()` was added

#### `models.ClusterUpdate` was modified

* `withHosts(java.util.List)` was added
* `hosts()` was added

#### `models.GlobalReachConnection$Update` was modified

* `withExpressRouteId(java.lang.String)` was added

#### `models.Datastore` was modified

* `status()` was added

#### `models.PrivateCloud$Update` was modified

* `withAvailability(models.AvailabilityProperties)` was added
* `withEncryption(models.Encryption)` was added
* `withIdentity(models.PrivateCloudIdentity)` was added

#### `models.PrivateCloud` was modified

* `availability()` was added
* `secondaryCircuit()` was added
* `encryption()` was added
* `identity()` was added

#### `models.GlobalReachConnection$Definition` was modified

* `withExpressRouteId(java.lang.String)` was added

#### `AvsManager` was modified

* `placementPolicies()` was added
* `virtualMachines()` was added

#### `models.CommonClusterProperties` was modified

* `withHosts(java.util.List)` was added

## 1.0.0-beta.2 (2021-07-15)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2021-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ClusterUpdateProperties` was removed

#### `models.ManagementCluster` was modified

* `models.ClusterUpdateProperties withClusterSize(java.lang.Integer)` -> `models.CommonClusterProperties withClusterSize(java.lang.Integer)`
* `provisioningState()` was removed
* `hosts()` was removed
* `clusterId()` was removed

#### `models.ClusterProperties` was modified

* `models.ManagementCluster withClusterSize(java.lang.Integer)` -> `models.CommonClusterProperties withClusterSize(java.lang.Integer)`
* `withClusterSize(java.lang.Integer)` was removed

### Features Added

* `models.WorkloadNetworkPublicIpProvisioningState` was added

* `models.Addon$DefinitionStages` was added

* `models.Addon$Definition` was added

* `models.CloudLink$Definition` was added

* `models.WorkloadNetworkDhcpProvisioningState` was added

* `models.NetAppVolume` was added

* `models.ScriptExecution$DefinitionStages` was added

* `models.CloudLinks` was added

* `models.CloudLink` was added

* `models.CloudLinkStatus` was added

* `models.MountOptionEnum` was added

* `models.WorkloadNetworkDnsZone$UpdateStages` was added

* `models.WorkloadNetworkPortMirroring$DefinitionStages` was added

* `models.ScriptExecutionsList` was added

* `models.GlobalReachConnections` was added

* `models.ScriptExecutions` was added

* `models.WorkloadNetworkDhcp$Update` was added

* `models.DatastoreList` was added

* `models.WorkloadNetworkDnsZone$Definition` was added

* `models.Addon$UpdateStages` was added

* `models.ScriptExecution$Update` was added

* `models.WorkloadNetworkSegmentProvisioningState` was added

* `models.GlobalReachConnection$DefinitionStages` was added

* `models.CloudLink$DefinitionStages` was added

* `models.WorkloadNetworkSegmentPortVif` was added

* `models.WorkloadNetworkPortMirroringList` was added

* `models.DhcpTypeEnum` was added

* `models.PortMirroringStatusEnum` was added

* `models.ScriptStringExecutionParameter` was added

* `models.ScriptSecureStringExecutionParameter` was added

* `models.WorkloadNetworkPortMirroring` was added

* `models.ScriptExecution` was added

* `models.WorkloadNetworkDhcpRelay` was added

* `models.WorkloadNetworkPublicIp` was added

* `models.ScriptPackages` was added

* `models.ScriptExecutionProvisioningState` was added

* `models.ScriptCmdlet` was added

* `models.WorkloadNetworkVirtualMachinesList` was added

* `models.GlobalReachConnectionList` was added

* `models.WorkloadNetworkPortMirroring$Definition` was added

* `models.ScriptExecutionParameterType` was added

* `models.WorkloadNetworkSegmentSubnet` was added

* `models.CloudLink$Update` was added

* `models.WorkloadNetworkVMGroup` was added

* `models.GlobalReachConnection` was added

* `models.DiskPoolVolume` was added

* `models.WorkloadNetworkDnsZone$DefinitionStages` was added

* `models.WorkloadNetworkDnsZone$Update` was added

* `models.GlobalReachConnection$UpdateStages` was added

* `models.WorkloadNetworkSegment$UpdateStages` was added

* `models.AddonHcxProperties` was added

* `models.WorkloadNetworkPortMirroringProvisioningState` was added

* `models.WorkloadNetworkDhcpEntity` was added

* `models.WorkloadNetworks` was added

* `models.WorkloadNetworkDhcp$UpdateStages` was added

* `models.WorkloadNetworkVMGroup$UpdateStages` was added

* `models.Datastore$UpdateStages` was added

* `models.WorkloadNetworkDnsService$Definition` was added

* `models.WorkloadNetworkDhcp$Definition` was added

* `models.AddonList` was added

* `models.SegmentStatusEnum` was added

* `models.WorkloadNetworkPortMirroring$UpdateStages` was added

* `models.WorkloadNetworkPublicIp$DefinitionStages` was added

* `models.PSCredentialExecutionParameter` was added

* `models.AddonType` was added

* `models.WorkloadNetworkSegment$Definition` was added

* `models.WorkloadNetworkDnsServiceProvisioningState` was added

* `models.WorkloadNetworkDhcpList` was added

* `models.WorkloadNetworkDnsZoneProvisioningState` was added

* `models.Datastore$DefinitionStages` was added

* `models.Datastore$Update` was added

* `models.VMTypeEnum` was added

* `models.Datastores` was added

* `models.WorkloadNetworkPublicIp$Definition` was added

* `models.OptionalParamEnum` was added

* `models.PortMirroringDirectionEnum` was added

* `models.WorkloadNetworkDnsZone` was added

* `models.VMGroupStatusEnum` was added

* `models.WorkloadNetworkSegment$DefinitionStages` was added

* `models.WorkloadNetworkPublicIPsList` was added

* `models.ScriptPackage` was added

* `models.Addon` was added

* `models.GlobalReachConnection$Update` was added

* `models.ScriptParameterTypes` was added

* `models.WorkloadNetworkSegment$Update` was added

* `models.Datastore` was added

* `models.WorkloadNetworkVMGroupProvisioningState` was added

* `models.GlobalReachConnectionProvisioningState` was added

* `models.WorkloadNetworkVMGroup$DefinitionStages` was added

* `models.Addons` was added

* `models.WorkloadNetworkDnsService$DefinitionStages` was added

* `models.WorkloadNetworkDhcp$DefinitionStages` was added

* `models.ScriptCmdlets` was added

* `models.WorkloadNetworkSegment` was added

* `models.Datastore$Definition` was added

* `models.WorkloadNetworkSegmentsList` was added

* `models.ScriptExecution$UpdateStages` was added

* `models.ScriptExecutionParameter` was added

* `models.WorkloadNetworkGateway` was added

* `models.WorkloadNetworkDnsService$Update` was added

* `models.AddonVrProperties` was added

* `models.AddonProvisioningState` was added

* `models.ScriptExecution$Definition` was added

* `models.WorkloadNetworkVMGroup$Update` was added

* `models.ScriptParameter` was added

* `models.Addon$Update` was added

* `models.ScriptCmdletsList` was added

* `models.WorkloadNetworkDnsService` was added

* `models.DnsServiceLogLevelEnum` was added

* `models.WorkloadNetworkVMGroup$Definition` was added

* `models.WorkloadNetworkDnsService$UpdateStages` was added

* `models.GlobalReachConnection$Definition` was added

* `models.DatastoreProvisioningState` was added

* `models.ScriptOutputStreamType` was added

* `models.WorkloadNetworkDnsServicesList` was added

* `models.GlobalReachConnectionStatus` was added

* `models.DnsServiceStatusEnum` was added

* `models.VisibilityParameterEnum` was added

* `models.WorkloadNetworkDnsZonesList` was added

* `models.WorkloadNetworkVirtualMachine` was added

* `models.CloudLinkList` was added

* `models.AddonSrmProperties` was added

* `models.AddonProperties` was added

* `models.WorkloadNetworkVMGroupsList` was added

* `models.WorkloadNetworkGatewayList` was added

* `models.CloudLink$UpdateStages` was added

* `models.WorkloadNetworkDhcp` was added

* `models.ScriptPackagesList` was added

* `models.WorkloadNetworkPortMirroring$Update` was added

* `models.WorkloadNetworkDhcpServer` was added

* `models.CommonClusterProperties` was added

#### `AvsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.PrivateCloudProperties` was modified

* `externalCloudLinks()` was added

#### `models.PrivateClouds` was modified

* `rotateNsxtPassword(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `rotateNsxtPassword(java.lang.String,java.lang.String)` was added
* `rotateVcenterPassword(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `rotateVcenterPassword(java.lang.String,java.lang.String)` was added

#### `models.PrivateCloud` was modified

* `rotateNsxtPassword(com.azure.core.util.Context)` was added
* `externalCloudLinks()` was added
* `rotateNsxtPassword()` was added
* `rotateVcenterPassword(com.azure.core.util.Context)` was added
* `rotateVcenterPassword()` was added

#### `AvsManager` was modified

* `addons()` was added
* `globalReachConnections()` was added
* `cloudLinks()` was added
* `datastores()` was added
* `workloadNetworks()` was added
* `scriptCmdlets()` was added
* `scriptPackages()` was added
* `scriptExecutions()` was added

## 1.0.0-beta.1 (2021-04-13)

- Azure Resource Manager Avs client library for Java. This package contains Microsoft Azure SDK for Avs Management SDK. Azure VMware Solution API. Package tag package-2020-03-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
