# Release History

## 1.0.0-beta.3 (Unreleased)


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
