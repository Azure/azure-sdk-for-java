# Release History

## 2.0.0 (2026-06-03)

- Azure Resource Manager Managed Network Fabric client library for Java. This package contains Microsoft Azure SDK for Managed Network Fabric Management SDK. Self service experience for Azure Network Fabric API. Package api-version 2025-07-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NetworkPacketBrokersListResult` was removed

#### `models.InternalNetworkPatchableProperties` was removed

#### `models.IpCommunityAddOperationProperties` was removed

#### `models.NetworkDevicesListResult` was removed

#### `models.OptionAProperties` was removed

#### `models.L3OptionAProperties` was removed

#### `models.VpnConfigurationPatchablePropertiesOptionAProperties` was removed

#### `models.ExternalNetworksList` was removed

#### `models.IpCommunityDeleteOperationProperties` was removed

#### `models.OperationListResult` was removed

#### `models.ExtensionEnumProperty` was removed

#### `models.NetworkTapPropertiesDestinationsItem` was removed

#### `models.IpExtendedCommunitySetOperationProperties` was removed

#### `models.NeighborGroupsListResult` was removed

#### `models.NetworkTapRulePatchableProperties` was removed

#### `models.NetworkDeviceSkusListResult` was removed

#### `models.NetworkTapRulesListResult` was removed

#### `models.L3IsolationDomainPatchableProperties` was removed

#### `models.L3IsolationDomainsListResult` was removed

#### `models.InternalNetworkPropertiesBgpConfiguration` was removed

#### `models.OptionBProperties` was removed

#### `models.IpPrefixesListResult` was removed

#### `models.TerminalServerPatchableProperties` was removed

#### `models.NetworkFabricPatchableProperties` was removed

#### `models.InternalNetworkPropertiesStaticRouteConfiguration` was removed

#### `models.IpExtendedCommunityListResult` was removed

#### `models.NetworkTapPatchableParametersDestinationsItem` was removed

#### `models.ExternalNetworkPatchableProperties` was removed

#### `models.IpCommunitiesListResult` was removed

#### `models.IpExtendedCommunityPatchableProperties` was removed

#### `models.AccessControlListPatchableProperties` was removed

#### `models.NetworkRacksListResult` was removed

#### `models.NetworkFabricsListResult` was removed

#### `models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration` was removed

#### `models.NeighborGroupPatchableProperties` was removed

#### `models.VpnConfigurationPropertiesOptionAProperties` was removed

#### `models.NetworkFabricPatchablePropertiesTerminalServerConfiguration` was removed

#### `models.PollingIntervalInSeconds` was removed

#### `models.InternetGatewayRulesListResult` was removed

#### `models.ManagementNetworkConfigurationPatchableProperties` was removed

#### `models.NetworkFabricSkusListResult` was removed

#### `models.NetworkToNetworkInterconnectsList` was removed

#### `models.RoutePoliciesListResult` was removed

#### `models.L2IsolationDomainsListResult` was removed

#### `models.IpPrefixPatchableProperties` was removed

#### `models.InternetGatewaysListResult` was removed

#### `models.IpCommunitySetOperationProperties` was removed

#### `models.NetworkInterfacesList` was removed

#### `models.AccessControlListsListResult` was removed

#### `models.IpExtendedCommunityDeleteOperationProperties` was removed

#### `models.InternalNetworksList` was removed

#### `models.NetworkFabricControllersListResult` was removed

#### `models.NetworkDevicePatchableProperties` was removed

#### `models.NetworkTapsListResult` was removed

#### `models.IpExtendedCommunityAddOperationProperties` was removed

#### `models.NetworkDevice$DefinitionStages` was modified

* Required stage 3 was added

#### `models.IpCommunity$DefinitionStages` was modified

* Required stage 3 was added

#### `models.NetworkTapRule$DefinitionStages` was modified

* Required stage 3 was added

#### `models.NeighborGroup$DefinitionStages` was modified

* Required stage 3 was added

#### `models.RoutePolicy$DefinitionStages` was modified

* Required stage 4 was added
* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.IpPrefix$DefinitionStages` was modified

* Required stage 3 was added

#### `models.AccessControlList$DefinitionStages` was modified

* Required stage 3 was added

#### `models.InternetGateway$DefinitionStages` was modified

* `withTypePropertiesType(models.GatewayType)` was removed in stage 3

#### `models.InternalNetwork$Definition` was modified

* `withExportRoutePolicyId(java.lang.String)` was removed
* `withStaticRouteConfiguration(models.InternalNetworkPropertiesStaticRouteConfiguration)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withBgpConfiguration(models.InternalNetworkPropertiesBgpConfiguration)` was removed

#### `models.ImportRoutePolicyInformation` was modified

* `validate()` was removed

#### `models.L2IsolationDomains` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`

#### `models.L3IsolationDomainPatch` was modified

* `models.ConnectedSubnetRoutePolicy connectedSubnetRoutePolicy()` -> `models.ConnectedSubnetRoutePolicyPatch connectedSubnetRoutePolicy()`
* `validate()` was removed
* `withAggregateRouteConfiguration(models.AggregateRouteConfiguration)` was removed
* `models.AggregateRouteConfiguration aggregateRouteConfiguration()` -> `models.AggregateRoutePatchConfiguration aggregateRouteConfiguration()`
* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was removed

#### `models.ExportRoutePolicyInformation` was modified

* `validate()` was removed

#### `models.NetworkTapRules` was modified

* `models.CommonPostActionResponseForStateUpdate resync(java.lang.String,java.lang.String)` -> `models.NetworkTapRuleResyncResponse resync(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForStateUpdate resync(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.NetworkTapRuleResyncResponse resync(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.NetworkTapRuleAction` was modified

* `validate()` was removed

#### `models.IpGroupProperties` was modified

* `validate()` was removed

#### `models.NetworkInterfacePatch` was modified

* `validate()` was removed

#### `models.DestinationProperties` was modified

* `validate()` was removed

#### `models.TerminalServerConfiguration` was modified

* `withUsername(java.lang.String)` was removed
* `validate()` was removed
* `withPassword(java.lang.String)` was removed
* `withSerialNumber(java.lang.String)` was removed

#### `models.NetworkTapRuleMatchCondition` was modified

* `validate()` was removed

#### `models.RebootProperties` was modified

* `validate()` was removed

#### `models.VpnConfigurationPatchableProperties` was modified

* `validate()` was removed
* `withOptionBProperties(models.OptionBProperties)` was removed
* `withOptionAProperties(models.VpnConfigurationPatchablePropertiesOptionAProperties)` was removed
* `models.OptionBProperties optionBProperties()` -> `models.VpnOptionBPatchProperties optionBProperties()`
* `models.VpnConfigurationPatchablePropertiesOptionAProperties optionAProperties()` -> `models.VpnOptionAPatchProperties optionAProperties()`

#### `models.ExternalNetworkPatch` was modified

* `models.L3OptionBProperties optionBProperties()` -> `models.L3OptionBPatchProperties optionBProperties()`
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `models.ExportRoutePolicy exportRoutePolicy()` -> `models.ExportRoutePolicyPatch exportRoutePolicy()`
* `withExportRoutePolicyId(java.lang.String)` was removed
* `importRoutePolicyId()` was removed
* `models.ImportRoutePolicy importRoutePolicy()` -> `models.ImportRoutePolicyPatch importRoutePolicy()`
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withOptionBProperties(models.L3OptionBProperties)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `exportRoutePolicyId()` was removed
* `validate()` was removed

#### `models.SupportedVersionProperties` was modified

* `SupportedVersionProperties()` was changed to private access
* `withIsDefault(models.BooleanEnumProperty)` was removed
* `withVendorOsVersion(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed
* `withVendorFirmwareVersion(java.lang.String)` was removed
* `validate()` was removed

#### `models.ActionIpCommunityProperties` was modified

* `validate()` was removed
* `withAdd(models.IpCommunityIdList)` was removed

#### `models.IpPrefixPatch` was modified

* `validate()` was removed

#### `models.NetworkInterface` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`

#### `models.InternetGatewayRulePatch` was modified

* `validate()` was removed
* `withTags(java.util.Map)` was removed

#### `ManagedNetworkFabricManager` was modified

* `fluent.AzureNetworkFabricManagementServiceApi serviceClient()` -> `fluent.ManagedNetworkFabricManagementClient serviceClient()`

#### `models.ExternalNetworks` was modified

* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.ExternalNetwork$Update` was modified

* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withOptionBProperties(models.L3OptionBProperties)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed

#### `models.NetworkTaps` was modified

* `models.CommonPostActionResponseForStateUpdate resync(java.lang.String,java.lang.String)` -> `models.NetworkTapResyncResponse resync(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate resync(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.NetworkTapResyncResponse resync(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.PortGroupProperties` was modified

* `validate()` was removed

#### `models.L3IsolationDomain` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.VlanMatchCondition` was modified

* `validate()` was removed

#### `models.NeighborAddress` was modified

* `validate()` was removed

#### `models.AccessControlList$Update` was modified

* `withMatchConfigurations(java.util.List)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed

#### `models.InternalNetworks` was modified

* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `java.lang.Integer vlanId()` -> `int vlanId()`
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `java.lang.Long peerAsn()` -> `long peerAsn()`
* `withVlanId(java.lang.Integer)` was removed
* `validate()` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `withPeerAsn(java.lang.Long)` was removed

#### `models.AccessControlListMatchCondition` was modified

* `validate()` was removed

#### `models.ManagedResourceGroupConfiguration` was modified

* `validate()` was removed

#### `models.InternalNetwork$Update` was modified

* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed
* `withConnectedIPv6Subnets(java.util.List)` was removed
* `withConnectedIPv4Subnets(java.util.List)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withBgpConfiguration(models.BgpConfiguration)` was removed

#### `models.NetworkDevices` was modified

* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion,com.azure.core.util.Context)` was removed
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion)` was removed
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(java.lang.String,java.lang.String)` -> `models.NetworkDeviceRefreshConfigurationResponse refreshConfiguration(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.NetworkDeviceRefreshConfigurationResponse refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate reboot(java.lang.String,java.lang.String,models.RebootProperties)` -> `models.OperationStatusResult reboot(java.lang.String,java.lang.String,models.RebootProperties)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState)` -> `models.NetworkDeviceUpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)` -> `models.NetworkDeviceUpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate reboot(java.lang.String,java.lang.String,models.RebootProperties,com.azure.core.util.Context)` -> `models.OperationStatusResult reboot(java.lang.String,java.lang.String,models.RebootProperties,com.azure.core.util.Context)`

#### `models.NetworkTapRule$Definition` was modified

* `withPollingIntervalInSeconds(models.PollingIntervalInSeconds)` was removed

#### `models.L3OptionBProperties` was modified

* `validate()` was removed

#### `models.NetworkTapRule$Update` was modified

* `withMatchConfigurations(java.util.List)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed

#### `models.UpgradeNetworkFabricProperties` was modified

* `validate()` was removed

#### `models.L3ExportRoutePolicy` was modified

* `validate()` was removed

#### `models.AnnotationResource` was modified

* `validate()` was removed

#### `models.RuleProperties` was modified

* `validate()` was removed

#### `models.InternalNetworkPatch` was modified

* `models.StaticRouteConfiguration staticRouteConfiguration()` -> `models.StaticRoutePatchConfiguration staticRouteConfiguration()`
* `exportRoutePolicyId()` was removed
* `withBgpConfiguration(models.BgpConfiguration)` was removed
* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed
* `validate()` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `models.BgpConfiguration bgpConfiguration()` -> `models.BgpPatchConfiguration bgpConfiguration()`
* `importRoutePolicyId()` was removed

#### `models.RoutePolicyPatch` was modified

* `validate()` was removed

#### `models.AccessControlListPatch` was modified

* `validate()` was removed

#### `models.AccessControlListMatchConfiguration` was modified

* `validate()` was removed

#### `models.InternalNetwork` was modified

* `importRoutePolicyId()` was removed
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateBgpAdministrativeState(models.UpdateAdministrativeState)` was removed
* `exportRoutePolicyId()` was removed
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`
* `updateBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `models.InternalNetworkPropertiesStaticRouteConfiguration staticRouteConfiguration()` -> `models.StaticRouteConfiguration staticRouteConfiguration()`
* `models.InternalNetworkPropertiesBgpConfiguration bgpConfiguration()` -> `models.BgpConfiguration bgpConfiguration()`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.IpCommunityIdList` was modified

* `validate()` was removed

#### `models.CommonDynamicMatchConfiguration` was modified

* `validate()` was removed

#### `models.NeighborGroupDestination` was modified

* `validate()` was removed

#### `models.ValidateConfigurationProperties` was modified

* `validate()` was removed

#### `models.NetworkFabrics` was modified

* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(java.lang.String,java.lang.String)` -> `models.OperationStatusResult refreshConfiguration(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForStateUpdate updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate commitConfiguration(java.lang.String,java.lang.String)` -> `models.CommitConfigurationResponse commitConfiguration(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForDeviceUpdate provision(java.lang.String,java.lang.String)` -> `models.OperationStatusResult provision(java.lang.String,java.lang.String)`
* `commitConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.ValidateConfigurationResponse getTopology(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.GetTopologyResponse getTopology(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.ValidateConfigurationResponse getTopology(java.lang.String,java.lang.String)` -> `models.GetTopologyResponse getTopology(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForStateUpdate updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForDeviceUpdate provision(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult provision(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)` -> `models.OperationStatusResult upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate deprovision(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deprovision(java.lang.String,java.lang.String)`
* `models.CommonPostActionResponseForDeviceUpdate deprovision(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deprovision(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties)` -> `models.OperationStatusResult upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties)`
* `models.CommonPostActionResponseForStateUpdate updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`

#### `models.NetworkDevice` was modified

* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(com.azure.core.util.Context)` -> `models.NetworkDeviceRefreshConfigurationResponse refreshConfiguration(com.azure.core.util.Context)`
* `upgrade(models.UpdateVersion,com.azure.core.util.Context)` was removed
* `models.CommonPostActionResponseForStateUpdate reboot(models.RebootProperties,com.azure.core.util.Context)` -> `models.OperationStatusResult reboot(models.RebootProperties,com.azure.core.util.Context)`
* `upgrade(models.UpdateVersion)` was removed
* `models.CommonPostActionResponseForStateUpdate reboot(models.RebootProperties)` -> `models.OperationStatusResult reboot(models.RebootProperties)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateDeviceAdministrativeState)` -> `models.NetworkDeviceUpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateDeviceAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)` -> `models.NetworkDeviceUpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration()` -> `models.NetworkDeviceRefreshConfigurationResponse refreshConfiguration()`

#### `models.NetworkFabricController` was modified

* `workloadManagementNetwork()` was removed

#### `models.ActionIpExtendedCommunityProperties` was modified

* `withAdd(models.IpExtendedCommunityIdList)` was removed
* `validate()` was removed

#### `models.NetworkPacketBrokerPatch` was modified

* `validate()` was removed

#### `models.IpCommunityPatch` was modified

* `validate()` was removed

#### `models.UpdateAdministrativeState` was modified

* `validate()` was removed

#### `models.EnableDisableOnResources` was modified

* `validate()` was removed

#### `models.VpnConfigurationProperties` was modified

* `withOptionAProperties(models.VpnConfigurationPropertiesOptionAProperties)` was removed
* `validate()` was removed
* `models.VpnConfigurationPropertiesOptionAProperties optionAProperties()` -> `models.VpnOptionAProperties optionAProperties()`
* `withOptionBProperties(models.OptionBProperties)` was removed
* `models.OptionBProperties optionBProperties()` -> `models.VpnOptionBProperties optionBProperties()`

#### `models.UpdateDeviceAdministrativeState` was modified

* `validate()` was removed

#### `models.AccessControlList` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`

#### `models.DeviceInterfaceProperties` was modified

* `DeviceInterfaceProperties()` was changed to private access
* `withIdentifier(java.lang.String)` was removed
* `withSupportedConnectorTypes(java.util.List)` was removed
* `validate()` was removed
* `withInterfaceType(java.lang.String)` was removed

#### `models.ConnectedSubnet` was modified

* `validate()` was removed

#### `models.NetworkFabricControllerPatch` was modified

* `validate()` was removed

#### `models.StaticRouteConfiguration` was modified

* `validate()` was removed

#### `models.RoutePolicyStatementProperties` was modified

* `validate()` was removed

#### `models.NetworkToNetworkInterconnect` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`
* `models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration optionBLayer3Configuration()` -> `models.OptionBLayer3Configuration optionBLayer3Configuration()`
* `models.CommonPostActionResponseForStateUpdate updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.NetworkTapRule` was modified

* `models.CommonPostActionResponseForStateUpdate resync()` -> `models.NetworkTapRuleResyncResponse resync()`
* `models.PollingIntervalInSeconds pollingIntervalInSeconds()` -> `java.lang.Integer pollingIntervalInSeconds()`
* `models.CommonPostActionResponseForStateUpdate resync(com.azure.core.util.Context)` -> `models.NetworkTapRuleResyncResponse resync(com.azure.core.util.Context)`

#### `models.NetworkToNetworkInterconnectPatch` was modified

* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was removed
* `name()` was removed
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was removed
* `validate()` was removed
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was removed
* `models.Layer2Configuration layer2Configuration()` -> `models.Layer2ConfigurationPatch layer2Configuration()`
* `models.ExportRoutePolicyInformation exportRoutePolicy()` -> `models.ExportRoutePolicyInformationPatch exportRoutePolicy()`
* `models.OptionBLayer3Configuration optionBLayer3Configuration()` -> `models.OptionBLayer3ConfigurationPatchProperties optionBLayer3Configuration()`
* `withOptionBLayer3Configuration(models.OptionBLayer3Configuration)` was removed
* `withLayer2Configuration(models.Layer2Configuration)` was removed
* `models.NpbStaticRouteConfiguration npbStaticRouteConfiguration()` -> `models.NpbStaticRouteConfigurationPatch npbStaticRouteConfiguration()`
* `models.ImportRoutePolicyInformation importRoutePolicy()` -> `models.ImportRoutePolicyInformationPatch importRoutePolicy()`

#### `models.NpbStaticRouteConfiguration` was modified

* `validate()` was removed

#### `models.L3IsolationDomain$Update` was modified

* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was removed
* `withAggregateRouteConfiguration(models.AggregateRouteConfiguration)` was removed

#### `models.ConnectedSubnetRoutePolicy` was modified

* `validate()` was removed
* `exportRoutePolicyId()` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed

#### `models.StaticRouteProperties` was modified

* `validate()` was removed

#### `models.IpExtendedCommunityPatch` was modified

* `validate()` was removed

#### `models.RoutePolicy` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withOptionBLayer3Configuration(models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration)` was removed

#### `models.VlanGroupProperties` was modified

* `validate()` was removed

#### `models.TagsUpdate` was modified

* `validate()` was removed

#### `models.BgpConfiguration` was modified

* `java.lang.Long peerAsn()` -> `long peerAsn()`
* `withPeerAsn(java.lang.Long)` was removed
* `validate()` was removed

#### `models.NetworkFabricPatch` was modified

* `models.ManagementNetworkConfigurationPatchableProperties managementNetworkConfiguration()` -> `models.ManagementNetworkPatchConfiguration managementNetworkConfiguration()`
* `models.NetworkFabricPatchablePropertiesTerminalServerConfiguration terminalServerConfiguration()` -> `models.TerminalServerPatchConfiguration terminalServerConfiguration()`
* `validate()` was removed
* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was removed

#### `models.AccessControlListPortCondition` was modified

* `validate()` was removed

#### `models.IpPrefixRule` was modified

* `validate()` was removed

#### `models.RoutePolicies` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`

#### `models.NeighborGroup$Update` was modified

* `withDestination(models.NeighborGroupDestination)` was removed

#### `models.AggregateRouteConfiguration` was modified

* `validate()` was removed

#### `models.IsolationDomainProperties` was modified

* `validate()` was removed

#### `models.StatementActionProperties` was modified

* `validate()` was removed

#### `models.PortCondition` was modified

* `validate()` was removed

#### `models.ControllerServices` was modified

* `ControllerServices()` was changed to private access
* `withIpv4AddressSpaces(java.util.List)` was removed
* `withIpv6AddressSpaces(java.util.List)` was removed
* `validate()` was removed

#### `models.NetworkTapRuleMatchConfiguration` was modified

* `validate()` was removed

#### `models.L2IsolationDomain` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`

#### `models.AccessControlLists` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.NetworkFabric` was modified

* `models.CommonPostActionResponseForDeviceUpdate deprovision(com.azure.core.util.Context)` -> `models.OperationStatusResult deprovision(com.azure.core.util.Context)`
* `models.ValidateConfigurationResponse getTopology(com.azure.core.util.Context)` -> `models.GetTopologyResponse getTopology(com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration(com.azure.core.util.Context)` -> `models.OperationStatusResult refreshConfiguration(com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate provision()` -> `models.OperationStatusResult provision()`
* `models.CommonPostActionResponseForStateUpdate commitConfiguration()` -> `models.CommitConfigurationResponse commitConfiguration()`
* `models.CommonPostActionResponseForStateUpdate upgrade(models.UpgradeNetworkFabricProperties)` -> `models.OperationStatusResult upgrade(models.UpgradeNetworkFabricProperties)`
* `models.CommonPostActionResponseForStateUpdate updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState)`
* `commitConfiguration(com.azure.core.util.Context)` was removed
* `models.CommonPostActionResponseForStateUpdate refreshConfiguration()` -> `models.OperationStatusResult refreshConfiguration()`
* `models.CommonPostActionResponseForStateUpdate upgrade(models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)` -> `models.OperationStatusResult upgrade(models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate provision(com.azure.core.util.Context)` -> `models.OperationStatusResult provision(com.azure.core.util.Context)`
* `models.ValidateConfigurationResponse getTopology()` -> `models.GetTopologyResponse getTopology()`
* `models.CommonPostActionResponseForDeviceUpdate deprovision()` -> `models.OperationStatusResult deprovision()`
* `models.CommonPostActionResponseForStateUpdate updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState)`

#### `models.IpCommunityRule` was modified

* `validate()` was removed

#### `models.CommonMatchConditions` was modified

* `validate()` was removed

#### `models.ExternalNetwork` was modified

* `importRoutePolicyId()` was removed
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`
* `exportRoutePolicyId()` was removed
* `models.CommonPostActionResponseForStateUpdate updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)`

#### `models.UpdateVersion` was modified

* `validate()` was removed

#### `models.NetworkFabric$Update` was modified

* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was removed

#### `models.NetworkToNetworkInterconnect$Update` was modified

* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was removed
* `withLayer2Configuration(models.Layer2Configuration)` was removed
* `withOptionBLayer3Configuration(models.OptionBLayer3Configuration)` was removed
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was removed

#### `models.NetworkDevicePatchParameters` was modified

* `validate()` was removed

#### `models.ManagementNetworkConfigurationProperties` was modified

* `validate()` was removed

#### `models.NetworkTap` was modified

* `models.CommonPostActionResponseForStateUpdate resync(com.azure.core.util.Context)` -> `models.NetworkTapResyncResponse resync(com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate resync()` -> `models.NetworkTapResyncResponse resync()`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(models.UpdateAdministrativeState)`

#### `models.ExportRoutePolicy` was modified

* `validate()` was removed

#### `models.L3IsolationDomains` was modified

* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.NetworkToNetworkInterconnects` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `models.CommonPostActionResponseForStateUpdate updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.RoutePolicy$Update` was modified

* `withStatements(java.util.List)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.IpExtendedCommunityIdList` was modified

* `validate()` was removed

#### `models.ExternalNetwork$Definition` was modified

* `withExportRoutePolicyId(java.lang.String)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed

#### `models.NeighborGroupPatch` was modified

* `withDestination(models.NeighborGroupDestination)` was removed
* `models.NeighborGroupDestination destination()` -> `models.NeighborGroupDestinationPatch destination()`
* `validate()` was removed

#### `models.Layer2Configuration` was modified

* `validate()` was removed

#### `models.Layer3IpPrefixProperties` was modified

* `validate()` was removed

#### `models.L2IsolationDomainPatch` was modified

* `validate()` was removed

#### `models.RouteTargetInformation` was modified

* `validate()` was removed

#### `models.ExpressRouteConnectionInformation` was modified

* `validate()` was removed

#### `models.BfdConfiguration` was modified

* `validate()` was removed

#### `models.ImportRoutePolicy` was modified

* `validate()` was removed

#### `models.IpExtendedCommunityRule` was modified

* `validate()` was removed

#### `models.NetworkInterfaces` was modified

* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.UpdateAdministrativeStateResponse updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`

#### `models.ExternalNetworkPatchPropertiesOptionAProperties` was modified

* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `withBfdConfiguration(models.BfdConfiguration)` was removed
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `models.BfdConfiguration bfdConfiguration()` -> `models.BfdPatchConfiguration bfdConfiguration()`
* `validate()` was removed

#### `models.AccessControlListAction` was modified

* `validate()` was removed

#### `models.SupportedConnectorProperties` was modified

* `SupportedConnectorProperties()` was changed to private access
* `validate()` was removed
* `withConnectorType(java.lang.String)` was removed
* `withMaxSpeedInMbps(java.lang.Integer)` was removed

#### `models.NetworkTapRulePatch` was modified

* `validate()` was removed

#### `models.NetworkTapPatch` was modified

* `validate()` was removed

#### `models.AggregateRoute` was modified

* `validate()` was removed

#### `models.StatementConditionProperties` was modified

* `validate()` was removed
* `withIpCommunityIds(java.util.List)` was removed

#### `models.InternetGatewayPatch` was modified

* `validate()` was removed

#### `models.OptionBLayer3Configuration` was modified

* `withVlanId(java.lang.Integer)` was removed
* `withPeerAsn(java.lang.Long)` was removed
* `java.lang.Long peerAsn()` -> `long peerAsn()`
* `validate()` was removed
* `java.lang.Integer vlanId()` -> `int vlanId()`

#### `models.IpMatchCondition` was modified

* `validate()` was removed

### Features Added

* `models.HeaderAddressProperties` was added

* `models.RouteType` was added

* `models.VpnOptionBProperties` was added

* `models.InternalNetworkBmpProperties` was added

* `models.NetworkMonitor$Definition` was added

* `models.ExternalNetworkBmpProperties` was added

* `models.L3OptionBPatchProperties` was added

* `models.NeighborGroupDestinationPatch` was added

* `models.VlanGroupPatchProperties` was added

* `models.NetworkFabricRotateCertificatesResponse` was added

* `models.CertificateArchiveReference` was added

* `models.BgpAdministrativeState` was added

* `models.ExternalNetworkUpdateBfdAdministrativeStateRequest` was added

* `models.GetTopologyResponseProperties` was added

* `models.NetworkBootstrapDeviceUpgradeResponse` was added

* `models.NetworkBootstrapDevices` was added

* `models.StatementActionPatchProperties` was added

* `models.ManagedServiceIdentity` was added

* `models.NetworkBootstrapInterface$UpdateStages` was added

* `models.IcmpConfigurationPatchProperties` was added

* `models.ControlPlaneAclPortMatchType` was added

* `models.VlanMatchConditionPatch` was added

* `models.BfdPatchConfiguration` was added

* `models.StaticRouteRoutePolicy` was added

* `models.UpdateAdministrativeStateResponseProperties` was added

* `models.ArmConfigurationDiffResponseProperties` was added

* `models.InternalNetworkUpdateBfdAdministrativeStateResponse` was added

* `models.IcmpConfigurationProperties` was added

* `models.DiscardCommitBatchResponseProperties` was added

* `models.NetworkBootstrapInterface` was added

* `models.NniStaticRouteConfiguration` was added

* `models.NetworkBootstrapDevice$Definition` was added

* `models.VpnOptionBPatchProperties` was added

* `models.CommonMatchConditionsPatch` was added

* `models.ExternalNetworkStaticRouteConfiguration` was added

* `models.AuthorizedTransceiverProperties` was added

* `models.ImportRoutePolicyInformationPatch` was added

* `models.QosPatchProperties` was added

* `models.AuthorizedTransceiverPatchProperties` was added

* `models.NetworkFabricLockAction` was added

* `models.GlobalNetworkTapRuleActionPatchProperties` was added

* `models.UniqueRouteDistinguisherPatchProperties` was added

* `models.ExternalNetworkBmpPatchProperties` was added

* `models.ConditionalDefaultRouteProperties` was added

* `models.NniUpdateBfdAdministrativeStateResponse` was added

* `models.ControlPlaneAclPortMatchCondition` was added

* `models.ViewDeviceConfigurationOperationResponse` was added

* `models.BmpConfigurationPatchProperties` was added

* `models.ManagedServiceIdentityPatch` was added

* `models.Layer2ConfigurationPatch` was added

* `models.NetworkFabricLockRequest` was added

* `models.BitRateUnit` was added

* `models.CommitBatchStatusResponseProperties` was added

* `models.ControlPlaneAclProperties` was added

* `models.NeighborGroupResyncResponse` was added

* `models.ControlPlaneAclTtlMatchType` was added

* `models.BurstSizeUnit` was added

* `models.BmpExportPolicyProperties` was added

* `models.DestinationPatchProperties` was added

* `models.SynchronizationStatus` was added

* `models.ExternalNetworkStaticRoutePatchConfiguration` was added

* `models.NetworkTapRuleActionPatch` was added

* `models.StorageAccountConfiguration` was added

* `models.ControlPlaneAclActionType` was added

* `models.NetworkBootstrapDeviceUpdateAdministrativeStateResponse` was added

* `models.CommitBatchStatusOperationResponse` was added

* `models.NetworkFabricLockType` was added

* `models.NetworkBootstrapInterfacePatch` was added

* `models.NetworkRackPatch` was added

* `models.NetworkDeviceResyncPasswordsResponse` was added

* `models.StaticRoutePatchConfiguration` was added

* `models.BmpMonitoredAddressFamily` was added

* `models.ArmConfigurationDiffOperationResponse` was added

* `models.CommonErrorResponse` was added

* `models.NetworkBootstrapDevicePatch` was added

* `models.UpdateAdministrativeStateResponse` was added

* `models.NativeIpv6PrefixLimitPatchProperties` was added

* `models.CommitStage` was added

* `models.NetworkBootstrapDevice$Update` was added

* `models.OptionBLayer3PrefixLimitProperties` was added

* `models.ControlPlaneAclAction` was added

* `models.InternalNetworkUpdateBfdAdministrativeStateResponseProperties` was added

* `models.StorageAccountPatchConfiguration` was added

* `models.NetworkMonitor` was added

* `models.InternalNetworkRouteType` was added

* `models.NetworkBootstrapDeviceRefreshConfigurationResponse` was added

* `models.GetTopologyResponse` was added

* `models.ControlPlaneAclTtlMatchCondition` was added

* `models.ProxyResourceBase` was added

* `models.StationConnectionMode` was added

* `models.NetworkDeviceUpdateAdministrativeStateResponse` was added

* `models.StationConfigurationState` was added

* `models.SecretArchiveReference` was added

* `models.CommonPostActionResponseForDeviceROCommands` was added

* `models.L3UniqueRouteDistinguisherProperties` was added

* `models.DeviceRole` was added

* `models.NetworkFabricResyncPasswordsResponse` was added

* `models.NetworkDeviceUpgradeResponse` was added

* `models.NetworkBootstrapInterface$Update` was added

* `models.V4OverV6BgpSessionState` was added

* `models.RoutePrefixLimitPatchProperties` was added

* `models.BgpPatchConfiguration` was added

* `models.PrefixLimitProperties` was added

* `models.NetworkDeviceRefreshConfigurationResponse` was added

* `models.GlobalAccessControlListActionPatchProperties` was added

* `models.ExternalNetworkRouteType` was added

* `models.BurstSize` was added

* `models.NetworkFabricRotatePasswordsResponse` was added

* `models.GlobalAccessControlListActionProperties` was added

* `models.NetworkDeviceRunRwCommandResponse` was added

* `models.ViewDeviceConfigurationResponseProperties` was added

* `models.InternalNetworkUpdateBgpAdministrativeStateResponseProperties` was added

* `models.BmpConfigurationState` was added

* `models.ControlPlaneAclMatchConditionPatch` was added

* `models.NativeIpv4PrefixLimitProperties` was added

* `models.NetworkMonitors` was added

* `models.NetworkFabricResyncCertificatesResponse` was added

* `models.ControlPlaneAclTtlMatchConditionPatch` was added

* `models.ControlPlaneAclMatchConfigurationProperties` was added

* `models.StationConnectionProperties` was added

* `models.NetworkDeviceUpgradeRequest` was added

* `models.NetworkBootstrapDevice$UpdateStages` was added

* `models.IdentitySelectorPatch` was added

* `models.ControlPlaneAclActionPatch` was added

* `models.NetworkBootstrapDeviceResyncPasswordsResponse` was added

* `models.CommitBatchState` was added

* `models.AccessControlListMatchConfigurationPatch` was added

* `models.BmpExportPolicyPatchProperties` was added

* `models.ManagementNetworkPatchConfiguration` was added

* `models.IsolationDomainPatchProperties` was added

* `models.InternalNetworkBmpPatchProperties` was added

* `models.ControlPlaneAclMatchCondition` was added

* `models.PrefixLimitPatchProperties` was added

* `models.ControlPlaneAclPortMatchConditionPatch` was added

* `models.AccessControlListPortConditionPatch` was added

* `models.ExtendedVlan` was added

* `models.ExportRoutePolicyPatch` was added

* `models.RuleCondition` was added

* `models.BmpConfigurationProperties` was added

* `models.ExternalNetworkUpdateBfdAdministrativeStateResponseProperties` was added

* `models.CommonDynamicMatchConfigurationPatch` was added

* `models.NniStaticRoutePatchConfiguration` was added

* `models.ImportRoutePolicyPatch` was added

* `models.QosProperties` was added

* `models.CertificateRotationStatus` was added

* `models.OptionBLayer3PrefixLimitPatchProperties` was added

* `models.NetworkTapRuleMatchConditionPatch` was added

* `models.StaticRoutePatchProperties` was added

* `models.NetworkTapResyncResponse` was added

* `models.OptionBLayer3ConfigurationPatchProperties` was added

* `models.NniBmpPatchProperties` was added

* `models.PortConditionPatch` was added

* `models.RouteTargetPatchInformation` was added

* `models.StationConnectionPatchProperties` was added

* `models.IpGroupPatchProperties` was added

* `models.QosConfigurationState` was added

* `models.NetworkBootstrapInterfaces` was added

* `models.IpMatchConditionPatch` was added

* `models.CommitConfigurationResponse` was added

* `models.FabricLockProperties` was added

* `models.CommitConfigurationRequest` was added

* `models.DiscardCommitBatchRequest` was added

* `models.NetworkMonitorPatch` was added

* `models.VpnOptionAProperties` was added

* `models.NativeIpv6PrefixLimitProperties` was added

* `models.RoutePolicyStatementPatchProperties` was added

* `models.ConnectedSubnetRoutePolicyPatch` was added

* `models.NeighborAddressBgpAdministrativeStatus` was added

* `models.NetworkDeviceRwCommandResponseProperties` was added

* `models.BitRate` was added

* `models.StaticRouteRoutePolicyPatch` was added

* `models.StatementConditionPatchProperties` was added

* `models.NpbStaticRouteConfigurationPatch` was added

* `models.BmpExportPolicy` was added

* `models.CommonPostActionResponseForDeviceROCommandsOperationStatusResult` was added

* `models.NetworkBootstrapDevice` was added

* `models.AccessControlListMatchConditionPatch` was added

* `models.AccessControlListActionPatch` was added

* `models.SecretRotationStatus` was added

* `models.SecretRotationSummary` was added

* `models.UniqueRouteDistinguisherProperties` was added

* `models.ControlPlaneAclIpMatchConditionPatch` was added

* `models.UniqueRouteDistinguisherConfigurationState` was added

* `models.ControlPlaneAclMatchConfigurationPatchProperties` was added

* `models.NetworkTapRuleResyncResponse` was added

* `models.NNIDerivedUniqueRouteDistinguisherConfigurationState` was added

* `models.InternalNetworkUpdateBfdAdministrativeStateRequest` was added

* `models.ControlPlaneAclPatchProperties` was added

* `models.NetworkMonitor$DefinitionStages` was added

* `models.NniUpdateBfdAdministrativeStateRequest` was added

* `models.VpnOptionAPatchProperties` was added

* `models.PortGroupPatchProperties` was added

* `models.LastOperationProperties` was added

* `models.CommitBatchStatusRequest` was added

* `models.InternalNetworkUpdateBgpAdministrativeStateResponse` was added

* `models.ManagedServiceIdentityType` was added

* `models.LockConfigurationState` was added

* `models.DeviceRwCommand` was added

* `models.DeviceRoCommand` was added

* `models.V6OverV4BgpSessionState` was added

* `models.ActionIpExtendedCommunityPatchProperties` was added

* `models.MicroBfdState` was added

* `models.UserAssignedIdentity` was added

* `models.AclType` was added

* `models.OperationStatusResult` was added

* `models.DiscardCommitBatchOperationResponse` was added

* `models.NniUpdateBfdAdministrativeStateResponseProperties` was added

* `models.RoutePrefixLimitProperties` was added

* `models.NativeIpv4PrefixLimitPatchProperties` was added

* `models.ActionIpCommunityPatchProperties` was added

* `models.L3ExportRoutePolicyPatch` was added

* `models.AggregateRoutePatchConfiguration` was added

* `models.PoliceRateConfigurationProperties` was added

* `models.NetworkBootstrapInterface$Definition` was added

* `models.NetworkMonitor$Update` was added

* `models.NeighborAddressBfdAdministrativeStatus` was added

* `models.TerminalServerPatchConfiguration` was added

* `models.ControlPlanAclIpMatchCondition` was added

* `models.FeatureFlagProperties` was added

* `models.NniBmpProperties` was added

* `models.NetworkBootstrapDevice$DefinitionStages` was added

* `models.ExportRoutePolicyInformationPatch` was added

* `models.NetworkBootstrapInterface$DefinitionStages` was added

* `models.InternalNetworkUpdateBgpAdministrativeStateRequest` was added

* `models.ExternalNetworkUpdateBfdAdministrativeStateResponse` was added

* `models.ManagedServiceIdentitySelectorType` was added

* `models.GlobalNetworkTapRuleActionProperties` was added

* `models.NetworkBootstrapDeviceRebootResponse` was added

* `models.NeighborAddressPatch` was added

* `models.CommitConfigurationPolicy` was added

* `models.ControlPlaneAclPortCondition` was added

* `models.IdentitySelector` was added

* `models.Layer3IpPrefixPatchProperties` was added

* `models.NetworkTapRuleMatchConfigurationPatch` was added

* `models.NetworkMonitor$UpdateStages` was added

* `models.CommitBatchDetails` was added

* `models.ConnectedSubnetPatch` was added

#### `models.InternalNetwork$Definition` was modified

* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitProperties)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitProperties)` was added
* `withBgpConfiguration(models.BgpConfiguration)` was added

#### `models.L3IsolationDomainPatch` was modified

* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicyPatch)` was added
* `v4routePrefixLimit()` was added
* `withStaticRouteRoutePolicy(models.StaticRouteRoutePolicyPatch)` was added
* `identity()` was added
* `staticRouteRoutePolicy()` was added
* `v6routePrefixLimit()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withAggregateRouteConfiguration(models.AggregateRoutePatchConfiguration)` was added
* `exportPolicyConfiguration()` was added
* `withV6routePrefixLimit(models.RoutePrefixLimitPatchProperties)` was added
* `withExportPolicyConfiguration(models.BmpExportPolicyPatchProperties)` was added
* `withV4routePrefixLimit(models.RoutePrefixLimitPatchProperties)` was added

#### `models.NeighborGroups` was modified

* `resync(java.lang.String,java.lang.String)` was added
* `resync(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NetworkInterfacePatch` was modified

* `withAdditionalDescription(java.lang.String)` was added
* `additionalDescription()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `identity()` was added

#### `models.NetworkPacketBroker$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.TerminalServerConfiguration` was modified

* `serialNumber()` was added
* `secretRotationStatus()` was added
* `username()` was added
* `password()` was added

#### `models.NetworkDevice$Update` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withIdentitySelector(models.IdentitySelectorPatch)` was added

#### `models.VpnConfigurationPatchableProperties` was modified

* `withOptionBProperties(models.VpnOptionBPatchProperties)` was added
* `withOptionAProperties(models.VpnOptionAPatchProperties)` was added

#### `models.ExternalNetworkPatch` was modified

* `staticRouteConfiguration()` was added
* `withStaticRouteConfiguration(models.ExternalNetworkStaticRoutePatchConfiguration)` was added
* `withOptionBProperties(models.L3OptionBPatchProperties)` was added
* `withExportRoutePolicy(models.ExportRoutePolicyPatch)` was added
* `withImportRoutePolicy(models.ImportRoutePolicyPatch)` was added

#### `models.ActionIpCommunityProperties` was modified

* `add()` was added

#### `models.IpExtendedCommunity` was modified

* `networkFabricId()` was added
* `lastOperation()` was added

#### `models.NetworkInterface` was modified

* `description()` was added
* `additionalDescription()` was added
* `lastOperation()` was added
* `networkFabricId()` was added
* `configurationState()` was added
* `identity()` was added

#### `models.NetworkInterface$Update` was modified

* `withAdditionalDescription(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.InternetGatewayRulePatch` was modified

* `tags()` was added

#### `ManagedNetworkFabricManager` was modified

* `networkBootstrapInterfaces()` was added
* `networkMonitors()` was added
* `networkBootstrapDevices()` was added

#### `models.ExternalNetworks` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.ExternalNetworkUpdateBfdAdministrativeStateRequest)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.ExternalNetworkUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added

#### `models.ExternalNetwork$Update` was modified

* `withStaticRouteConfiguration(models.ExternalNetworkStaticRoutePatchConfiguration)` was added
* `withExportRoutePolicy(models.ExportRoutePolicyPatch)` was added
* `withOptionBProperties(models.L3OptionBPatchProperties)` was added
* `withImportRoutePolicy(models.ImportRoutePolicyPatch)` was added

#### `models.L3IsolationDomain` was modified

* `identity()` was added
* `v6routePrefixLimit()` was added
* `lastOperation()` was added
* `exportPolicyConfiguration()` was added
* `uniqueRdConfiguration()` was added
* `v4routePrefixLimit()` was added
* `staticRouteRoutePolicy()` was added

#### `models.Layer4Protocol` was modified

* `SCTP` was added

#### `models.NetworkFabricController$Update` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.NeighborGroup$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.NeighborAddress` was modified

* `bgpAdministrativeState()` was added
* `bfdAdministrativeState()` was added

#### `models.AccessControlList$Update` was modified

* `withGlobalAccessControlListActions(models.GlobalAccessControlListActionPatchProperties)` was added
* `withControlPlaneAclConfigurationForUpdate(java.util.List)` was added
* `withMatchConfigurationsForUpdate(java.util.List)` was added
* `withAclType(models.AclType)` was added
* `withDynamicMatchConfigurationsForUpdate(java.util.List)` was added
* `withDeviceRole(models.DeviceRole)` was added

#### `models.InternalNetworks` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkUpdateBgpAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkUpdateBgpAdministrativeStateRequest)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkUpdateBfdAdministrativeStateRequest)` was added

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `secondaryIpv6Prefix()` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitProperties)` was added
* `nativeIpv4PrefixLimit()` was added
* `withVlanId(int)` was added
* `withPeerAsn(long)` was added
* `nativeIpv6PrefixLimit()` was added
* `primaryIpv4Prefix()` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitProperties)` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `v6OverV4BgpSession()` was added
* `withBmpConfiguration(models.ExternalNetworkBmpProperties)` was added
* `v4OverV6BgpSession()` was added
* `bmpConfiguration()` was added
* `primaryIpv6Prefix()` was added
* `secondaryIpv4Prefix()` was added

#### `models.AccessControlListMatchCondition` was modified

* `icmpConfiguration()` was added
* `withProtocolNeighbors(java.util.List)` was added
* `protocolNeighbors()` was added
* `withIcmpConfiguration(models.IcmpConfigurationProperties)` was added

#### `models.InternalNetwork$Update` was modified

* `withStaticRouteConfiguration(models.StaticRoutePatchConfiguration)` was added
* `withConnectedIPv6SubnetsForUpdate(java.util.List)` was added
* `withBgpConfiguration(models.BgpPatchConfiguration)` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitPatchProperties)` was added
* `withConnectedIPv4SubnetsForUpdate(java.util.List)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitPatchProperties)` was added

#### `models.NetworkDevices` was modified

* `resyncPasswords(java.lang.String,java.lang.String)` was added
* `upgrade(java.lang.String,java.lang.String,models.NetworkDeviceUpgradeRequest,com.azure.core.util.Context)` was added
* `runRwCommand(java.lang.String,java.lang.String,models.DeviceRwCommand)` was added
* `runRoCommand(java.lang.String,java.lang.String,models.DeviceRoCommand,com.azure.core.util.Context)` was added
* `runRwCommand(java.lang.String,java.lang.String,models.DeviceRwCommand,com.azure.core.util.Context)` was added
* `resyncPasswords(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `upgrade(java.lang.String,java.lang.String,models.NetworkDeviceUpgradeRequest)` was added
* `runRoCommand(java.lang.String,java.lang.String,models.DeviceRoCommand)` was added
* `resyncCertificates(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `resyncCertificates(java.lang.String,java.lang.String)` was added

#### `models.NetworkTapRule$Definition` was modified

* `withPollingIntervalInSeconds(java.lang.Integer)` was added
* `withGlobalNetworkTapRuleActions(models.GlobalNetworkTapRuleActionProperties)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withIdentitySelector(models.IdentitySelector)` was added

#### `models.AdministrativeState` was modified

* `UNDER_MAINTENANCE` was added
* `ENABLED_DEGRADED` was added

#### `models.NetworkTapRule$Update` was modified

* `withIdentitySelector(models.IdentitySelectorPatch)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withMatchConfigurationsForUpdate(java.util.List)` was added
* `withDynamicMatchConfigurationsForUpdate(java.util.List)` was added
* `withGlobalNetworkTapRuleActions(models.GlobalNetworkTapRuleActionPatchProperties)` was added

#### `models.L2IsolationDomain$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withNetworkToNetworkInterconnectId(java.lang.String)` was added
* `withExtendedVlan(models.ExtendedVlan)` was added

#### `models.ConfigurationState` was modified

* `PENDING_ADMINISTRATIVE_UPDATE` was added

#### `models.InternetGateway$Definition` was modified

* `withInternetGatewayType(models.GatewayType)` was added

#### `models.RuleProperties` was modified

* `headerAddressList()` was added
* `withDestinationAddressList(java.util.List)` was added
* `withCondition(models.RuleCondition)` was added
* `condition()` was added
* `destinationAddressList()` was added
* `sourceAddressList()` was added
* `withSourceAddressList(java.util.List)` was added
* `withHeaderAddressList(java.util.List)` was added

#### `models.DeviceAdministrativeState` was modified

* `DISABLE` was added
* `UNDER_MAINTENANCE` was added
* `UNGRACEFUL_RMA` was added
* `UNGRACEFUL_QUARANTINE` was added
* `ENABLE` was added

#### `models.InternalNetworkPatch` was modified

* `withStaticRouteConfiguration(models.StaticRoutePatchConfiguration)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitPatchProperties)` was added
* `withBgpConfiguration(models.BgpPatchConfiguration)` was added
* `nativeIpv4PrefixLimit()` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitPatchProperties)` was added
* `nativeIpv6PrefixLimit()` was added

#### `models.AccessControlListPatch` was modified

* `withControlPlaneAclConfiguration(java.util.List)` was added
* `withAclType(models.AclType)` was added
* `aclType()` was added
* `withGlobalAccessControlListActions(models.GlobalAccessControlListActionPatchProperties)` was added
* `deviceRole()` was added
* `withDeviceRole(models.DeviceRole)` was added
* `globalAccessControlListActions()` was added
* `controlPlaneAclConfiguration()` was added

#### `models.IpPrefix` was modified

* `lastOperation()` was added
* `networkFabricId()` was added

#### `models.InternetGateway` was modified

* `internetGatewayType()` was added
* `lastOperation()` was added

#### `models.NetworkFabricController$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.AclActionType` was modified

* `POLICE_RATE` was added
* `REMARK` was added

#### `models.InternalNetwork` was modified

* `lastOperation()` was added
* `updateBfdAdministrativeState(models.InternalNetworkUpdateBfdAdministrativeStateRequest)` was added
* `nativeIpv4PrefixLimit()` was added
* `updateBgpAdministrativeState(models.InternalNetworkUpdateBgpAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `nativeIpv6PrefixLimit()` was added
* `updateBgpAdministrativeState(models.InternalNetworkUpdateBgpAdministrativeStateRequest)` was added
* `networkFabricId()` was added
* `updateBfdAdministrativeState(models.InternalNetworkUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added

#### `models.NetworkRack` was modified

* `configurationState()` was added
* `lastOperation()` was added

#### `models.NetworkDevice$Definition` was modified

* `withIdentitySelector(models.IdentitySelector)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.NetworkFabric$Definition` was modified

* `withFeatureFlags(java.util.List)` was added
* `withTrustedIpPrefixes(java.util.List)` was added
* `withUniqueRdConfiguration(models.UniqueRouteDistinguisherProperties)` was added
* `withControlPlaneAcls(java.util.List)` was added
* `withQosConfiguration(models.QosProperties)` was added
* `withHardwareAlertThreshold(java.lang.Integer)` was added
* `withAuthorizedTransceiver(models.AuthorizedTransceiverProperties)` was added
* `withStorageAccountConfiguration(models.StorageAccountConfiguration)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withStorageArrayCount(java.lang.Integer)` was added

#### `models.NetworkFabrics` was modified

* `armConfigurationDiff(java.lang.String,java.lang.String)` was added
* `commitBatchStatus(java.lang.String,java.lang.String,models.CommitBatchStatusRequest)` was added
* `resyncPasswords(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `armConfigurationDiff(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `resyncCertificates(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `resyncPasswords(java.lang.String,java.lang.String)` was added
* `discardCommitBatch(java.lang.String,java.lang.String,models.DiscardCommitBatchRequest,com.azure.core.util.Context)` was added
* `commitConfiguration(java.lang.String,java.lang.String,models.CommitConfigurationRequest,com.azure.core.util.Context)` was added
* `resyncCertificates(java.lang.String,java.lang.String)` was added
* `lockFabric(java.lang.String,java.lang.String,models.NetworkFabricLockRequest)` was added
* `discardCommitBatch(java.lang.String,java.lang.String,models.DiscardCommitBatchRequest)` was added
* `rotateCertificates(java.lang.String,java.lang.String)` was added
* `commitBatchStatus(java.lang.String,java.lang.String,models.CommitBatchStatusRequest,com.azure.core.util.Context)` was added
* `viewDeviceConfiguration(java.lang.String,java.lang.String)` was added
* `viewDeviceConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `rotateCertificates(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `lockFabric(java.lang.String,java.lang.String,models.NetworkFabricLockRequest,com.azure.core.util.Context)` was added
* `rotatePasswords(java.lang.String,java.lang.String)` was added
* `rotatePasswords(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NetworkDevice` was modified

* `resyncCertificates(com.azure.core.util.Context)` was added
* `runRwCommand(models.DeviceRwCommand,com.azure.core.util.Context)` was added
* `resyncPasswords(com.azure.core.util.Context)` was added
* `identitySelector()` was added
* `identity()` was added
* `resyncCertificates()` was added
* `rwDeviceConfig()` was added
* `networkFabricId()` was added
* `lastOperation()` was added
* `resyncPasswords()` was added
* `runRwCommand(models.DeviceRwCommand)` was added
* `runRoCommand(models.DeviceRoCommand)` was added
* `secretRotationStatus()` was added
* `upgrade(models.NetworkDeviceUpgradeRequest)` was added
* `certificateRotationStatus()` was added
* `runRoCommand(models.DeviceRoCommand,com.azure.core.util.Context)` was added
* `upgrade(models.NetworkDeviceUpgradeRequest,com.azure.core.util.Context)` was added

#### `models.NetworkFabricController` was modified

* `identity()` was added
* `lastOperation()` was added

#### `models.ActionIpExtendedCommunityProperties` was modified

* `add()` was added

#### `models.NetworkPacketBrokerPatch` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `identity()` was added

#### `models.NetworkInterface$Definition` was modified

* `withAdditionalDescription(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.NetworkPacketBroker$Update` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.VpnConfigurationProperties` was modified

* `withOptionBProperties(models.VpnOptionBProperties)` was added
* `withOptionAProperties(models.VpnOptionAProperties)` was added

#### `models.AccessControlList` was modified

* `lastOperation()` was added
* `aclType()` was added
* `deviceRole()` was added
* `globalAccessControlListActions()` was added
* `networkFabricIds()` was added
* `controlPlaneAclConfiguration()` was added

#### `models.NetworkFabricControllerPatch` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.StaticRouteConfiguration` was modified

* `extension()` was added
* `withExtension(models.Extension)` was added

#### `models.NetworkTap$Update` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.NetworkToNetworkInterconnect` was modified

* `lastOperation()` was added
* `microBfdState()` was added
* `conditionalDefaultRouteConfiguration()` was added
* `updateBfdAdministrativeState(models.NniUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `staticRouteConfiguration()` was added
* `updateBfdAdministrativeState(models.NniUpdateBfdAdministrativeStateRequest)` was added

#### `models.NetworkTapRule` was modified

* `networkTapIds()` was added
* `globalNetworkTapRuleActions()` was added
* `identity()` was added
* `networkFabricIds()` was added
* `lastOperation()` was added
* `identitySelector()` was added

#### `models.NetworkToNetworkInterconnectPatch` was modified

* `withExportRoutePolicy(models.ExportRoutePolicyInformationPatch)` was added
* `withOptionBLayer3Configuration(models.OptionBLayer3ConfigurationPatchProperties)` was added
* `microBfdState()` was added
* `withLayer2Configuration(models.Layer2ConfigurationPatch)` was added
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfigurationPatch)` was added
* `withStaticRouteConfiguration(models.NniStaticRoutePatchConfiguration)` was added
* `withImportRoutePolicy(models.ImportRoutePolicyInformationPatch)` was added
* `staticRouteConfiguration()` was added
* `withMicroBfdState(models.MicroBfdState)` was added

#### `models.L3IsolationDomain$Update` was modified

* `withV4routePrefixLimit(models.RoutePrefixLimitPatchProperties)` was added
* `withStaticRouteRoutePolicy(models.StaticRouteRoutePolicyPatch)` was added
* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicyPatch)` was added
* `withAggregateRouteConfiguration(models.AggregateRoutePatchConfiguration)` was added
* `withV6routePrefixLimit(models.RoutePrefixLimitPatchProperties)` was added
* `withExportPolicyConfiguration(models.BmpExportPolicyPatchProperties)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.IpCommunity` was modified

* `lastOperation()` was added
* `networkFabricId()` was added

#### `models.RoutePolicy` was modified

* `lastOperation()` was added

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withOptionBLayer3Configuration(models.OptionBLayer3Configuration)` was added
* `withStaticRouteConfiguration(models.NniStaticRouteConfiguration)` was added
* `withMicroBfdState(models.MicroBfdState)` was added
* `withConditionalDefaultRouteConfiguration(models.ConditionalDefaultRouteProperties)` was added

#### `models.NetworkTap$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.BgpConfiguration` was modified

* `v4OverV6BgpSession()` was added
* `withPeerAsn(long)` was added
* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `v6OverV4BgpSession()` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `bmpConfiguration()` was added
* `withBmpConfiguration(models.InternalNetworkBmpProperties)` was added

#### `models.NetworkFabricPatch` was modified

* `featureFlags()` was added
* `withAuthorizedTransceiver(models.AuthorizedTransceiverPatchProperties)` was added
* `withQosConfiguration(models.QosPatchProperties)` was added
* `trustedIpPrefixes()` was added
* `controlPlaneAcls()` was added
* `identity()` was added
* `withTerminalServerConfiguration(models.TerminalServerPatchConfiguration)` was added
* `withTrustedIpPrefixes(java.util.List)` was added
* `storageAccountConfiguration()` was added
* `withManagementNetworkConfiguration(models.ManagementNetworkPatchConfiguration)` was added
* `withStorageAccountConfiguration(models.StorageAccountPatchConfiguration)` was added
* `withControlPlaneAcls(java.util.List)` was added
* `authorizedTransceiver()` was added
* `hardwareAlertThreshold()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `qosConfiguration()` was added
* `withHardwareAlertThreshold(java.lang.Integer)` was added
* `withUniqueRdConfiguration(models.UniqueRouteDistinguisherPatchProperties)` was added
* `withFeatureFlags(java.util.List)` was added
* `uniqueRdConfiguration()` was added

#### `models.NeighborGroup$Update` was modified

* `withDestination(models.NeighborGroupDestinationPatch)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.AccessControlList$Definition` was modified

* `withControlPlaneAclConfiguration(java.util.List)` was added
* `withGlobalAccessControlListActions(models.GlobalAccessControlListActionProperties)` was added
* `withDeviceRole(models.DeviceRole)` was added
* `withAclType(models.AclType)` was added

#### `models.L3IsolationDomain$Definition` was modified

* `withV4routePrefixLimit(models.RoutePrefixLimitProperties)` was added
* `withV6routePrefixLimit(models.RoutePrefixLimitProperties)` was added
* `withUniqueRdConfiguration(models.L3UniqueRouteDistinguisherProperties)` was added
* `withExportPolicyConfiguration(models.BmpExportPolicyProperties)` was added
* `withStaticRouteRoutePolicy(models.StaticRouteRoutePolicy)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.L2IsolationDomain$Update` was modified

* `withExtendedVlan(models.ExtendedVlan)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.L2IsolationDomain` was modified

* `networkToNetworkInterconnectId()` was added
* `extendedVlan()` was added
* `identity()` was added
* `lastOperation()` was added

#### `models.NetworkFabric` was modified

* `armConfigurationDiff(com.azure.core.util.Context)` was added
* `commitConfiguration(models.CommitConfigurationRequest,com.azure.core.util.Context)` was added
* `resyncPasswords(com.azure.core.util.Context)` was added
* `rotatePasswords()` was added
* `storageAccountConfiguration()` was added
* `uniqueRdConfiguration()` was added
* `lockFabric(models.NetworkFabricLockRequest)` was added
* `qosConfiguration()` was added
* `commitBatchStatus(models.CommitBatchStatusRequest,com.azure.core.util.Context)` was added
* `discardCommitBatch(models.DiscardCommitBatchRequest)` was added
* `secretRotationSummary()` was added
* `rotateCertificates()` was added
* `activeCommitBatches()` was added
* `viewDeviceConfiguration(com.azure.core.util.Context)` was added
* `featureFlags()` was added
* `identity()` was added
* `resyncPasswords()` was added
* `discardCommitBatch(models.DiscardCommitBatchRequest,com.azure.core.util.Context)` was added
* `resyncCertificates()` was added
* `armConfigurationDiff()` was added
* `hardwareAlertThreshold()` was added
* `fabricLocks()` was added
* `controlPlaneAcls()` was added
* `viewDeviceConfiguration()` was added
* `resyncCertificates(com.azure.core.util.Context)` was added
* `rotatePasswords(com.azure.core.util.Context)` was added
* `lastOperation()` was added
* `commitBatchStatus(models.CommitBatchStatusRequest)` was added
* `rotateCertificates(com.azure.core.util.Context)` was added
* `storageArrayCount()` was added
* `trustedIpPrefixes()` was added
* `lockFabric(models.NetworkFabricLockRequest,com.azure.core.util.Context)` was added
* `authorizedTransceiver()` was added

#### `models.SourceDestinationType` was modified

* `BIDIRECTIONAL` was added

#### `models.ExternalNetwork` was modified

* `updateBfdAdministrativeState(models.ExternalNetworkUpdateBfdAdministrativeStateRequest)` was added
* `staticRouteConfiguration()` was added
* `lastOperation()` was added
* `updateBfdAdministrativeState(models.ExternalNetworkUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `networkFabricId()` was added

#### `models.NetworkFabric$Update` was modified

* `withHardwareAlertThreshold(java.lang.Integer)` was added
* `withUniqueRdConfiguration(models.UniqueRouteDistinguisherPatchProperties)` was added
* `withAuthorizedTransceiver(models.AuthorizedTransceiverPatchProperties)` was added
* `withFeatureFlags(java.util.List)` was added
* `withTerminalServerConfiguration(models.TerminalServerPatchConfiguration)` was added
* `withControlPlaneAcls(java.util.List)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withStorageAccountConfiguration(models.StorageAccountPatchConfiguration)` was added
* `withManagementNetworkConfiguration(models.ManagementNetworkPatchConfiguration)` was added
* `withTrustedIpPrefixes(java.util.List)` was added
* `withQosConfiguration(models.QosPatchProperties)` was added

#### `models.NetworkToNetworkInterconnect$Update` was modified

* `withStaticRouteConfiguration(models.NniStaticRoutePatchConfiguration)` was added
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfigurationPatch)` was added
* `withMicroBfdState(models.MicroBfdState)` was added
* `withOptionBLayer3Configuration(models.OptionBLayer3ConfigurationPatchProperties)` was added
* `withImportRoutePolicy(models.ImportRoutePolicyInformationPatch)` was added
* `withExportRoutePolicy(models.ExportRoutePolicyInformationPatch)` was added
* `withLayer2Configuration(models.Layer2ConfigurationPatch)` was added

#### `models.NetworkDevicePatchParameters` was modified

* `withIdentitySelector(models.IdentitySelectorPatch)` was added
* `identitySelector()` was added
* `identity()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.NetworkTap` was modified

* `identity()` was added
* `networkFabricIds()` was added
* `lastOperation()` was added

#### `models.NetworkToNetworkInterconnects` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.NniUpdateBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.NniUpdateBfdAdministrativeStateRequest)` was added

#### `models.NeighborGroup` was modified

* `configurationState()` was added
* `identity()` was added
* `networkFabricIds()` was added
* `resync(com.azure.core.util.Context)` was added
* `lastOperation()` was added
* `resync()` was added

#### `models.RoutePolicy$Update` was modified

* `withStatementsForUpdate(java.util.List)` was added

#### `models.ExternalNetwork$Definition` was modified

* `withStaticRouteConfiguration(models.ExternalNetworkStaticRouteConfiguration)` was added

#### `models.EnableDisableState` was modified

* `UNDER_MAINTENANCE` was added

#### `models.NeighborGroupPatch` was modified

* `withDestination(models.NeighborGroupDestinationPatch)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `identity()` was added

#### `models.L2IsolationDomainPatch` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `networkToNetworkInterconnectId()` was added
* `extendedVlan()` was added
* `identity()` was added
* `withExtendedVlan(models.ExtendedVlan)` was added
* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.InternetGatewayRule` was modified

* `lastOperation()` was added

#### `models.NetworkPacketBroker` was modified

* `identity()` was added
* `lastOperation()` was added
* `configurationState()` was added

#### `models.ExternalNetworkPatchPropertiesOptionAProperties` was modified

* `v4OverV6BgpSession()` was added
* `primaryIpv6Prefix()` was added
* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitPatchProperties)` was added
* `secondaryIpv6Prefix()` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `primaryIpv4Prefix()` was added
* `secondaryIpv4Prefix()` was added
* `bmpConfiguration()` was added
* `v6OverV4BgpSession()` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitPatchProperties)` was added
* `withBmpConfiguration(models.ExternalNetworkBmpPatchProperties)` was added
* `nativeIpv6PrefixLimit()` was added
* `withBfdConfiguration(models.BfdPatchConfiguration)` was added
* `nativeIpv4PrefixLimit()` was added

#### `models.AccessControlListAction` was modified

* `remarkComment()` was added
* `withRemarkComment(java.lang.String)` was added
* `withPoliceRateConfiguration(models.PoliceRateConfigurationProperties)` was added
* `policeRateConfiguration()` was added

#### `models.NetworkTapRulePatch` was modified

* `withIdentitySelector(models.IdentitySelectorPatch)` was added
* `globalNetworkTapRuleActions()` was added
* `withGlobalNetworkTapRuleActions(models.GlobalNetworkTapRuleActionPatchProperties)` was added
* `identity()` was added
* `identitySelector()` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.NetworkTapPatch` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `identity()` was added

#### `models.StatementConditionProperties` was modified

* `ipCommunityIds()` was added

#### `models.PortType` was modified

* `BIDIRECTIONAL` was added

#### `models.OptionBLayer3Configuration` was modified

* `withPeerAsn(long)` was added
* `prefixLimits()` was added
* `withVlanId(int)` was added
* `withPeLoopbackIpAddress(java.util.List)` was added
* `withBmpConfiguration(models.NniBmpProperties)` was added
* `peLoopbackIpAddress()` was added
* `withPrefixLimits(java.util.List)` was added
* `bmpConfiguration()` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager Managed Network Fabric client library for Java. This package contains Microsoft Azure SDK for Managed Network Fabric Management SDK. Self service experience for Azure Network Fabric API. Package tag package-2023-06-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.PollingIntervalInSeconds` was modified

* `fromInt(int)` was removed

#### `models.NetworkFabric` was modified

* `upgrade(models.UpdateVersion)` was removed
* `upgrade(models.UpdateVersion,com.azure.core.util.Context)` was removed

#### `models.NetworkFabrics` was modified

* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion)` was removed
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion,com.azure.core.util.Context)` was removed

### Features Added

* `models.UpgradeNetworkFabricProperties` was added

* `models.NetworkFabricUpgradeAction` was added

#### `models.AccessControlList` was modified

* `defaultAction()` was added

#### `models.AccessControlListPatchableProperties` was modified

* `defaultAction()` was added
* `withDefaultAction(models.CommunityActionTypes)` was added

#### `models.NetworkToNetworkInterconnect` was modified

* `systemData()` was added

#### `models.NetworkToNetworkInterconnectPatch` was modified

* `systemData()` was added
* `name()` was added
* `type()` was added
* `id()` was added

#### `models.NetworkFabricSku` was modified

* `systemData()` was added

#### `models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration` was modified

* `fabricAsn()` was added

#### `models.RoutePolicy` was modified

* `defaultAction()` was added

#### `models.ExternalNetworkPatch` was modified

* `withNetworkToNetworkInterconnectId(java.lang.String)` was added
* `networkToNetworkInterconnectId()` was added

#### `models.AccessControlList$Definition` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added

#### `models.NetworkInterface` was modified

* `systemData()` was added

#### `models.ExternalNetwork$Update` was modified

* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.AccessControlList$Update` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added

#### `models.NetworkDeviceSku` was modified

* `systemData()` was added

#### `models.PollingIntervalInSeconds` was modified

* `fromValue(java.lang.Integer)` was added
* `hashCode()` was added
* `toString()` was added
* `equals(java.lang.Object)` was added
* `getValue()` was added

#### `models.NetworkFabric` was modified

* `upgrade(models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)` was added
* `upgrade(models.UpgradeNetworkFabricProperties)` was added

#### `models.ExternalNetwork` was modified

* `systemData()` was added

#### `models.InternalNetworkPropertiesBgpConfiguration` was modified

* `fabricAsn()` was added

#### `models.RoutePolicyPatch` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added
* `defaultAction()` was added

#### `models.AccessControlListPatch` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added
* `defaultAction()` was added

#### `models.RoutePolicy$Update` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added

#### `models.ExternalNetwork$Definition` was modified

* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.NetworkFabric$Definition` was modified

* `withFabricVersion(java.lang.String)` was added

#### `models.NetworkFabrics` was modified

* `upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties)` was added
* `upgrade(java.lang.String,java.lang.String,models.UpgradeNetworkFabricProperties,com.azure.core.util.Context)` was added

#### `models.ExternalNetworkPatchableProperties` was modified

* `networkToNetworkInterconnectId()` was added
* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.RoutePolicy$Definition` was modified

* `withDefaultAction(models.CommunityActionTypes)` was added

## 1.0.0 (2023-07-17)

- Azure Resource Manager Managed Network Fabric client library for Java. This package contains Microsoft Azure SDK for Managed Network Fabric Management SDK. Self service experience for Azure Network Fabric API. Package tag package-2023-06-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NetworkRackRoleName` was removed

* `models.FabricBfdConfiguration` was removed

* `models.NetworkFabricPatchParameters` was removed

* `models.NetworkRackSkus` was removed

* `models.NetworkRackSku` was removed

* `models.GetDeviceStatusProperties` was removed

* `models.IsTestVersion` was removed

* `models.NetworkRackSkusListResult` was removed

* `models.OperationalStatus` was removed

* `models.OptionBPropertiesAutoGenerated` was removed

* `models.PowerCycleState` was removed

* `models.TerminalServerPatchParameters` was removed

* `models.ConditionActionType` was removed

* `models.Layer3OptionAProperties` was removed

* `models.UpdateVersionProperties` was removed

* `models.PowerEnd` was removed

* `models.IsCurrentVersion` was removed

* `models.AddressFamily` was removed

* `models.UpdatePowerCycleProperties` was removed

* `models.NetworkDeviceRoleProperties` was removed

* `models.AccessControlListConditionProperties` was removed

* `models.NetworkRackPatch` was removed

* `models.NetworkFabricControllerOperationalState` was removed

* `models.ArpProperties` was removed

* `models.GetDynamicInterfaceMapsPropertiesItem` was removed

* `models.NetworkFabricOperationalState` was removed

* `models.GetStaticInterfaceMapsPropertiesItem` was removed

* `models.State` was removed

* `models.WorkloadServices` was removed

* `models.NetworkDeviceRoleTypes` was removed

* `models.InfrastructureServices` was removed

* `models.InterfaceStatus` was removed

* `models.IpPrefixPropertiesIpPrefixRulesItem` was removed

* `models.DeviceLimits` was removed

* `models.SupportPackageProperties` was removed

* `models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy` was removed

* `models.NetworkDeviceRackRoleType` was removed

* `models.Layer3Configuration` was removed

* `models.EnabledDisabledState` was removed

* `models.ManagementNetworkConfiguration` was removed

#### `models.NetworkRack$DefinitionStages` was modified

* `withNetworkRackSku(java.lang.String)` was removed in stage 3

#### `models.IpExtendedCommunity$DefinitionStages` was modified

* Stage 3 was added

#### `models.NetworkFabric$DefinitionStages` was modified

* Stage 3, 4, 5, 6, 7, 8, 9 was added

#### `models.NetworkToNetworkInterconnect$DefinitionStages` was modified

* Stage 2 was added

#### `models.RoutePolicy$DefinitionStages` was modified

* `withStatements(java.util.List)` was removed in stage 3

#### `models.L3IsolationDomain$DefinitionStages` was modified

* Stage 3 was added

#### `models.L2IsolationDomain$DefinitionStages` was modified

* Stage 3, 4 was added

#### `models.AccessControlList` was modified

* `conditions()` was removed
* `addressFamily()` was removed

#### `models.NetworkFabricControllers` was modified

* `disableWorkloadManagementNetwork(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `enableWorkloadManagementNetwork(java.lang.String,java.lang.String)` was removed
* `enableWorkloadManagementNetwork(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `disableWorkloadManagementNetwork(java.lang.String,java.lang.String)` was removed

#### `models.InternalNetwork$Definition` was modified

* `withBgpConfiguration(models.BgpConfiguration)` was removed
* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed

#### `models.InternalNetworkPatchableProperties` was modified

* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed
* `withBgpConfiguration(models.BgpConfiguration)` was removed
* `staticRouteConfiguration()` was removed
* `bgpConfiguration()` was removed

#### `models.L2IsolationDomains` was modified

* `void updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `clearArpTable(java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `clearNeighborTable(java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `getArpEntries(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `clearNeighborTable(java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `getArpEntries(java.lang.String,java.lang.String)` was removed
* `clearArpTable(java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed

#### `models.L3IsolationDomainPatch` was modified

* `description()` was removed
* `withDescription(java.lang.String)` was removed
* `withConnectedSubnetRoutePolicy(models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy)` was removed
* `tags()` was removed
* `models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy connectedSubnetRoutePolicy()` -> `models.ConnectedSubnetRoutePolicy connectedSubnetRoutePolicy()`

#### `models.NetworkFabricControllerPatch` was modified

* `tags()` was removed

#### `models.NetworkToNetworkInterconnect` was modified

* `models.BooleanEnumProperty isManagementType()` -> `models.IsManagementType isManagementType()`
* `layer3Configuration()` was removed
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `systemData()` was removed

#### `models.NetworkFabricSku` was modified

* `maxSupportedVer()` was removed
* `detailsUri()` was removed
* `java.lang.String typePropertiesType()` -> `models.FabricSkuType typePropertiesType()`
* `minSupportedVer()` was removed
* `systemData()` was removed

#### `models.IpCommunity$Definition` was modified

* `withAction(models.CommunityActionTypes)` was removed
* `withCommunityMembers(java.util.List)` was removed
* `withWellKnownCommunities(java.util.List)` was removed

#### `models.L3IsolationDomain$Update` was modified

* `withConnectedSubnetRoutePolicy(models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.OptionAProperties` was modified

* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `java.lang.Integer peerAsn()` -> `java.lang.Long peerAsn()`
* `withBfdConfiguration(models.FabricBfdConfiguration)` was removed
* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `withPeerAsn(java.lang.Integer)` was removed
* `models.FabricBfdConfiguration bfdConfiguration()` -> `models.BfdConfiguration bfdConfiguration()`

#### `models.TerminalServerConfiguration` was modified

* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `password()` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `username()` was removed
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `serialNumber()` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed

#### `models.NetworkRack$Definition` was modified

* `withNetworkRackSku(java.lang.String)` was removed

#### `models.IpCommunity` was modified

* `wellKnownCommunities()` was removed
* `action()` was removed
* `communityMembers()` was removed

#### `models.IpExtendedCommunityPatch` was modified

* `tags()` was removed

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withIsManagementType(models.BooleanEnumProperty)` was removed
* `withLayer3Configuration(models.Layer3Configuration)` was removed

#### `models.NetworkRack$Update` was modified

* `withProperties(java.lang.Object)` was removed

#### `models.ExternalNetworkPatch` was modified

* `withOptionAProperties(models.Layer3OptionAProperties)` was removed
* `models.OptionBProperties optionBProperties()` -> `models.L3OptionBProperties optionBProperties()`
* `withOptionBProperties(models.OptionBProperties)` was removed
* `models.Layer3OptionAProperties optionAProperties()` -> `models.ExternalNetworkPatchPropertiesOptionAProperties optionAProperties()`

#### `models.SupportedVersionProperties` was modified

* `isTest()` was removed
* `withIsTest(models.IsTestVersion)` was removed
* `withIsCurrent(models.IsCurrentVersion)` was removed
* `isCurrent()` was removed

#### `models.BgpConfiguration` was modified

* `withPeerAsn(int)` was removed
* `java.lang.Integer fabricAsn()` -> `java.lang.Long fabricAsn()`
* `int peerAsn()` -> `java.lang.Long peerAsn()`

#### `models.IpExtendedCommunity` was modified

* `routeTargets()` was removed
* `action()` was removed

#### `models.IpPrefixPatch` was modified

* `tags()` was removed

#### `models.AccessControlList$Definition` was modified

* `withAddressFamily(models.AddressFamily)` was removed
* `withConditions(java.util.List)` was removed

#### `models.NetworkInterface` was modified

* `systemData()` was removed
* `getStatus(com.azure.core.util.Context)` was removed
* `getStatus()` was removed
* `void updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `void updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)`
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`

#### `ManagedNetworkFabricManager` was modified

* `networkRackSkus()` was removed

#### `models.ExternalNetworks` was modified

* `clearArpEntries(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `updateBfdForBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String)` was removed
* `updateBfdForBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `clearArpEntries(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `clearIpv6Neighbors(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `clearIpv6Neighbors(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.ExternalNetwork$Update` was modified

* `withOptionBProperties(models.OptionBProperties)` was removed
* `withOptionAProperties(models.Layer3OptionAProperties)` was removed

#### `models.L3IsolationDomain$Definition` was modified

* `withConnectedSubnetRoutePolicy(models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.L3IsolationDomain` was modified

* `clearArpTable(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `clearArpTable(models.EnableDisableOnResources)` was removed
* `optionBDisabledOnResources()` was removed
* `clearNeighborTable(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `void updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `models.L3IsolationDomainPatchPropertiesConnectedSubnetRoutePolicy connectedSubnetRoutePolicy()` -> `models.ConnectedSubnetRoutePolicy connectedSubnetRoutePolicy()`
* `disabledOnResources()` was removed
* `updateOptionBAdministrativeState(models.UpdateAdministrativeState)` was removed
* `clearNeighborTable(models.EnableDisableOnResources)` was removed
* `void updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)`
* `description()` was removed
* `updateOptionBAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed

#### `models.StatementActionProperties` was modified

* `models.CommunityActionTypes actionType()` -> `models.RoutePolicyActionType actionType()`
* `withActionType(models.CommunityActionTypes)` was removed

#### `models.NeighborAddress` was modified

* `operationalState()` was removed

#### `models.AccessControlList$Update` was modified

* `withAddressFamily(models.AddressFamily)` was removed
* `withConditions(java.util.List)` was removed

#### `models.NetworkDeviceSku` was modified

* `systemData()` was removed
* `limits()` was removed

#### `models.InternalNetworks` was modified

* `clearArpEntries(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `list(java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `updateBfdForStaticRouteAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `void updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `updateBfdForBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `clearIpv6Neighbors(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `updateBfdForStaticRouteAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `clearIpv6Neighbors(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `void updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `clearArpEntries(java.lang.String,java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateBfdForBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `withMtu(java.lang.Integer)` was removed
* `models.Layer3OptionAProperties withSecondaryIpv4Prefix(java.lang.String)` -> `models.Layer3IpPrefixProperties withSecondaryIpv4Prefix(java.lang.String)`
* `models.Layer3OptionAProperties withPrimaryIpv4Prefix(java.lang.String)` -> `models.Layer3IpPrefixProperties withPrimaryIpv4Prefix(java.lang.String)`
* `models.Layer3OptionAProperties withPrimaryIpv6Prefix(java.lang.String)` -> `models.Layer3IpPrefixProperties withPrimaryIpv6Prefix(java.lang.String)`
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withVlanId(java.lang.Integer)` was removed
* `withPeerAsn(java.lang.Integer)` was removed
* `models.Layer3OptionAProperties withSecondaryIpv6Prefix(java.lang.String)` -> `models.Layer3IpPrefixProperties withSecondaryIpv6Prefix(java.lang.String)`
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `withBfdConfiguration(models.BfdConfiguration)` was removed
* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed

#### `models.L2IsolationDomain` was modified

* `void updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState)`
* `clearArpTable(models.EnableDisableOnResources)` was removed
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `getArpEntries(com.azure.core.util.Context)` was removed
* `clearNeighborTable(models.EnableDisableOnResources)` was removed
* `void updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `getArpEntries()` was removed
* `clearArpTable(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `disabledOnResources()` was removed
* `clearNeighborTable(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed

#### `models.AccessControlLists` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.NetworkDevices` was modified

* `updatePowerCycle(java.lang.String,java.lang.String,models.UpdatePowerCycleProperties)` was removed
* `updateVersion(java.lang.String,java.lang.String,models.UpdateVersionProperties,com.azure.core.util.Context)` was removed
* `reboot(java.lang.String,java.lang.String)` was removed
* `restoreConfig(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `updatePowerCycle(java.lang.String,java.lang.String,models.UpdatePowerCycleProperties,com.azure.core.util.Context)` was removed
* `getStaticInterfaceMaps(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getDynamicInterfaceMaps(java.lang.String,java.lang.String)` was removed
* `generateSupportPackage(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getDynamicInterfaceMaps(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateSupportPackage(java.lang.String,java.lang.String)` was removed
* `restoreConfig(java.lang.String,java.lang.String)` was removed
* `updateVersion(java.lang.String,java.lang.String,models.UpdateVersionProperties)` was removed
* `getStaticInterfaceMaps(java.lang.String,java.lang.String)` was removed
* `getStatus(java.lang.String,java.lang.String)` was removed
* `getStatus(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `reboot(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.NetworkFabric` was modified

* `void provision()` -> `models.CommonPostActionResponseForDeviceUpdate provision()`
* `int fabricAsn()` -> `long fabricAsn()`
* `void provision(com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate provision(com.azure.core.util.Context)`
* `operationalState()` was removed
* `void deprovision(com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate deprovision(com.azure.core.util.Context)`
* `models.ManagementNetworkConfiguration managementNetworkConfiguration()` -> `models.ManagementNetworkConfigurationProperties managementNetworkConfiguration()`
* `int rackCount()` -> `java.lang.Integer rackCount()`
* `routerId()` was removed
* `void deprovision()` -> `models.CommonPostActionResponseForDeviceUpdate deprovision()`

#### `models.ExternalNetwork` was modified

* `models.OptionBProperties optionBProperties()` -> `models.L3OptionBProperties optionBProperties()`
* `void updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `clearIpv6Neighbors(models.EnableDisableOnResources)` was removed
* `clearArpEntries(models.EnableDisableOnResources)` was removed
* `updateBgpAdministrativeState(models.UpdateAdministrativeState)` was removed
* `updateBfdForBgpAdministrativeState(models.UpdateAdministrativeState)` was removed
* `clearIpv6Neighbors(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `disabledOnResources()` was removed
* `systemData()` was removed
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `updateBfdForBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `void updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)`
* `clearArpEntries(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed

#### `models.NetworkFabric$Update` was modified

* `withTerminalServerConfiguration(models.TerminalServerPatchableProperties)` was removed

#### `models.NetworkDevicePatchParameters` was modified

* `tags()` was removed

#### `models.L3IsolationDomains` was modified

* `clearNeighborTable(java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateOptionBAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `clearArpTable(java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `clearNeighborTable(java.lang.String,java.lang.String,models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `updateOptionBAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForDeviceUpdate updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `clearArpTable(java.lang.String,java.lang.String,models.EnableDisableOnResources)` was removed

#### `models.NetworkToNetworkInterconnects` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String)` was removed

#### `models.RoutePolicyPatch` was modified

* `tags()` was removed

#### `models.AccessControlListPatch` was modified

* `withConditions(java.util.List)` was removed
* `addressFamily()` was removed
* `withAddressFamily(models.AddressFamily)` was removed
* `conditions()` was removed
* `tags()` was removed

#### `models.InternalNetwork` was modified

* `void updateBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `bfdDisabledOnResources()` was removed
* `bgpDisabledOnResources()` was removed
* `bfdForStaticRoutesDisabledOnResources()` was removed
* `clearArpEntries(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `updateBfdForBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `void updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)`
* `updateBfdForBgpAdministrativeState(models.UpdateAdministrativeState)` was removed
* `clearIpv6Neighbors(models.EnableDisableOnResources)` was removed
* `disabledOnResources()` was removed
* `models.StaticRouteConfiguration staticRouteConfiguration()` -> `models.InternalNetworkPropertiesStaticRouteConfiguration staticRouteConfiguration()`
* `updateBfdForStaticRouteAdministrativeState(models.UpdateAdministrativeState)` was removed
* `clearIpv6Neighbors(models.EnableDisableOnResources,com.azure.core.util.Context)` was removed
* `void updateBgpAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateBgpAdministrativeState(models.UpdateAdministrativeState)`
* `void updateAdministrativeState(models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(models.UpdateAdministrativeState)`
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `models.BgpConfiguration bgpConfiguration()` -> `models.InternalNetworkPropertiesBgpConfiguration bgpConfiguration()`
* `clearArpEntries(models.EnableDisableOnResources)` was removed
* `updateBfdForStaticRouteAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed

#### `models.NetworkFabricPatchableProperties` was modified

* `l3IsolationDomains()` was removed
* `l2IsolationDomains()` was removed
* `racks()` was removed

#### `models.IpExtendedCommunity$Definition` was modified

* `withAction(models.CommunityActionTypes)` was removed
* `withRouteTargets(java.util.List)` was removed

#### `models.NetworkRack` was modified

* `networkRackSku()` was removed

#### `models.ExternalNetwork$Definition` was modified

* `withOptionBProperties(models.OptionBProperties)` was removed

#### `models.NetworkDevice$Definition` was modified

* `withNetworkDeviceRole(models.NetworkDeviceRoleTypes)` was removed

#### `models.Layer2Configuration` was modified

* `withPortCount(java.lang.Integer)` was removed
* `withMtu(int)` was removed
* `portCount()` was removed
* `int mtu()` -> `java.lang.Integer mtu()`

#### `models.L2IsolationDomainPatch` was modified

* `tags()` was removed

#### `models.NetworkFabric$Definition` was modified

* `withRackCount(int)` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfiguration)` was removed
* `withFabricAsn(int)` was removed

#### `models.NetworkFabrics` was modified

* `void deprovision(java.lang.String,java.lang.String)` -> `models.CommonPostActionResponseForDeviceUpdate deprovision(java.lang.String,java.lang.String)`
* `void provision(java.lang.String,java.lang.String)` -> `models.CommonPostActionResponseForDeviceUpdate provision(java.lang.String,java.lang.String)`
* `void provision(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate provision(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deprovision(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForDeviceUpdate deprovision(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.NetworkDevice` was modified

* `reboot()` was removed
* `getStaticInterfaceMaps()` was removed
* `getStatus(com.azure.core.util.Context)` was removed
* `models.NetworkDeviceRoleTypes networkDeviceRole()` -> `models.NetworkDeviceRole networkDeviceRole()`
* `reboot(com.azure.core.util.Context)` was removed
* `updatePowerCycle(models.UpdatePowerCycleProperties,com.azure.core.util.Context)` was removed
* `getStatus()` was removed
* `updateVersion(models.UpdateVersionProperties,com.azure.core.util.Context)` was removed
* `generateSupportPackage(com.azure.core.util.Context)` was removed
* `getStaticInterfaceMaps(com.azure.core.util.Context)` was removed
* `getDynamicInterfaceMaps(com.azure.core.util.Context)` was removed
* `restoreConfig(com.azure.core.util.Context)` was removed
* `getDynamicInterfaceMaps()` was removed
* `updateVersion(models.UpdateVersionProperties)` was removed
* `restoreConfig()` was removed
* `generateSupportPackage()` was removed
* `updatePowerCycle(models.UpdatePowerCycleProperties)` was removed

#### `models.NetworkFabricController` was modified

* `operationalState()` was removed
* `disableWorkloadManagementNetwork()` was removed
* `enableWorkloadManagementNetwork(com.azure.core.util.Context)` was removed
* `models.InfrastructureServices infrastructureServices()` -> `models.ControllerServices infrastructureServices()`
* `models.WorkloadServices workloadServices()` -> `models.ControllerServices workloadServices()`
* `enableWorkloadManagementNetwork()` was removed
* `disableWorkloadManagementNetwork(com.azure.core.util.Context)` was removed

#### `models.BfdConfiguration` was modified

* `models.EnabledDisabledState administrativeState()` -> `models.BfdAdministrativeState administrativeState()`
* `interval()` was removed

#### `models.IpCommunityPatch` was modified

* `tags()` was removed

#### `models.ExternalNetworkPatchableProperties` was modified

* `withPeeringOption(models.PeeringOption)` was removed
* `peeringOption()` was removed
* `optionAProperties()` was removed
* `withOptionAProperties(models.Layer3OptionAProperties)` was removed
* `optionBProperties()` was removed
* `withOptionBProperties(models.OptionBProperties)` was removed

#### `models.NetworkInterfaces` was modified

* `getStatus(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)`
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getStatus(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String)` was removed
* `void updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` -> `models.CommonPostActionResponseForStateUpdate updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)`

#### `models.UpdateAdministrativeState` was modified

* `models.AdministrativeState state()` -> `models.EnableDisableState state()`
* `withState(models.AdministrativeState)` was removed

#### `models.VpnConfigurationProperties` was modified

* `models.OptionBPropertiesAutoGenerated optionBProperties()` -> `models.OptionBProperties optionBProperties()`
* `models.OptionAProperties optionAProperties()` -> `models.VpnConfigurationPropertiesOptionAProperties optionAProperties()`
* `withOptionAProperties(models.OptionAProperties)` was removed
* `models.EnabledDisabledState administrativeState()` -> `models.AdministrativeState administrativeState()`
* `withOptionBProperties(models.OptionBPropertiesAutoGenerated)` was removed

### Features Added

* `models.UpdateDeviceAdministrativeState` was added

* `models.NetworkRackType` was added

* `models.NetworkPacketBrokersListResult` was added

* `models.InternetGatewayRule$Update` was added

* `models.ImportRoutePolicyInformation` was added

* `models.RebootType` was added

* `models.AccessControlListPatchableProperties` was added

* `models.NetworkTap$Update` was added

* `models.ExportRoutePolicyInformation` was added

* `models.NetworkTapRules` was added

* `models.NetworkTapRule` was added

* `models.NetworkToNetworkInterconnectPatch` was added

* `models.NetworkTapRuleAction` was added

* `models.NeighborGroups` was added

* `models.IpGroupProperties` was added

* `models.NpbStaticRouteConfiguration` was added

* `models.IsMonitoringEnabled` was added

* `models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration` was added

* `models.NetworkPacketBroker$Definition` was added

* `models.DestinationProperties` was added

* `models.GatewayType` was added

* `models.ConnectedSubnetRoutePolicy` was added

* `models.NeighborGroupPatchableProperties` was added

* `models.NetworkTapRuleMatchCondition` was added

* `models.RebootProperties` was added

* `models.NetworkToNetworkInterconnect$UpdateStages` was added

* `models.L3OptionAProperties` was added

* `models.VlanGroupProperties` was added

* `models.TagsUpdate` was added

* `models.VpnConfigurationPatchableProperties` was added

* `models.NetworkTap$Definition` was added

* `models.VpnConfigurationPatchablePropertiesOptionAProperties` was added

* `models.NetworkFabricPatch` was added

* `models.AccessControlListPortCondition` was added

* `models.CommonPostActionResponseForStateUpdate` was added

* `models.PollingType` was added

* `models.IpPrefixRule` was added

* `models.EncapsulationType` was added

* `models.ExtensionEnumProperty` was added

* `models.NetworkTapPropertiesDestinationsItem` was added

* `models.InternetGateway$DefinitionStages` was added

* `models.NeighborGroup$Update` was added

* `models.VpnConfigurationPropertiesOptionAProperties` was added

* `models.Encapsulation` was added

* `models.InternetGatewayRulePatch` was added

* `models.Action` was added

* `models.IpAddressType` was added

* `models.FabricSkuType` was added

* `models.NetworkFabricPatchablePropertiesTerminalServerConfiguration` was added

* `models.NetworkTaps` was added

* `models.PortGroupProperties` was added

* `models.BfdAdministrativeState` was added

* `models.IsolationDomainProperties` was added

* `models.NetworkPacketBroker$DefinitionStages` was added

* `models.Layer4Protocol` was added

* `models.VlanMatchCondition` was added

* `models.NeighborGroup$Definition` was added

* `models.PortCondition` was added

* `models.ControllerServices` was added

* `models.ValidateConfigurationResponse` was added

* `models.RoutePolicyConditionType` was added

* `models.NetworkTapRuleMatchConfiguration` was added

* `models.InternetGateway$UpdateStages` was added

* `models.NeighborGroupsListResult` was added

* `models.InternetGatewayRule$UpdateStages` was added

* `models.AccessControlListMatchCondition` was added

* `models.NetworkTapRulePatchableProperties` was added

* `models.IsWorkloadManagementNetworkEnabled` was added

* `models.PrefixType` was added

* `models.PollingIntervalInSeconds` was added

* `models.InternetGatewayRulesListResult` was added

* `models.ManagementNetworkConfigurationPatchableProperties` was added

* `models.NetworkTapRule$Definition` was added

* `models.TapRuleActionType` was added

* `models.SourceDestinationType` was added

* `models.IpCommunityRule` was added

* `models.L3OptionBProperties` was added

* `models.InternetGatewayRule$Definition` was added

* `models.CommonMatchConditions` was added

* `models.NfcSku` was added

* `models.NetworkTapRule$Update` was added

* `models.ValidateAction` was added

* `models.UpdateVersion` was added

* `models.InternetGateways` was added

* `models.NetworkTapRulesListResult` was added

* `models.IpPrefixPatchableProperties` was added

* `models.L3IsolationDomainPatchableProperties` was added

* `models.InternetGatewaysListResult` was added

* `models.NetworkToNetworkInterconnect$Update` was added

* `models.ConfigurationState` was added

* `models.L3ExportRoutePolicy` was added

* `models.NetworkDeviceRole` was added

* `models.InternetGateway$Definition` was added

* `models.RuleProperties` was added

* `models.ManagementNetworkConfigurationProperties` was added

* `models.NetworkPacketBroker$UpdateStages` was added

* `models.NeighborGroup$UpdateStages` was added

* `models.DeviceAdministrativeState` was added

* `models.NetworkTapRule$DefinitionStages` was added

* `models.NetworkTap` was added

* `models.ExportRoutePolicy` was added

* `models.NeighborGroup` was added

* `models.InternalNetworkPropertiesBgpConfiguration` was added

* `models.InternetGateway` was added

* `models.AccessControlListMatchConfiguration` was added

* `models.Extension` was added

* `models.NetworkTap$DefinitionStages` was added

* `models.AclActionType` was added

* `models.NetworkTap$UpdateStages` was added

* `models.InternalNetworkPropertiesStaticRouteConfiguration` was added

* `models.CommonDynamicMatchConfiguration` was added

* `models.EnableDisableState` was added

* `models.RoutePolicyActionType` was added

* `models.NeighborGroupPatch` was added

* `models.InternetGatewayRules` was added

* `models.NeighborGroupDestination` was added

* `models.InternetGateway$Update` was added

* `models.ValidateConfigurationProperties` was added

* `models.NetworkTapPatchableParametersDestinationsItem` was added

* `models.NetworkPacketBrokers` was added

* `models.RouteTargetInformation` was added

* `models.DestinationType` was added

* `models.NeighborGroup$DefinitionStages` was added

* `models.InternetGatewayRule` was added

* `models.NetworkPacketBrokerPatch` was added

* `models.ImportRoutePolicy` was added

* `models.InternetGatewayRule$DefinitionStages` was added

* `models.NetworkPacketBroker` was added

* `models.IpExtendedCommunityRule` was added

* `models.ExternalNetworkPatchPropertiesOptionAProperties` was added

* `models.NetworkPacketBroker$Update` was added

* `models.ConfigurationType` was added

* `models.AccessControlListAction` was added

* `models.IpExtendedCommunityPatchableProperties` was added

* `models.NetworkTapRulePatch` was added

* `models.CommonPostActionResponseForDeviceUpdate` was added

* `models.NetworkTapPatch` was added

* `models.AddressFamilyType` was added

* `models.IsManagementType` was added

* `models.PortType` was added

* `models.NetworkTapsListResult` was added

* `models.InternetGatewayPatch` was added

* `models.NetworkTapRule$UpdateStages` was added

* `models.OptionBLayer3Configuration` was added

* `models.IpMatchCondition` was added

#### `models.AccessControlList` was modified

* `aclsUrl()` was added
* `dynamicMatchConfigurations()` was added
* `validateConfiguration(com.azure.core.util.Context)` was added
* `resync()` was added
* `updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `configurationState()` was added
* `administrativeState()` was added
* `lastSyncedTime()` was added
* `matchConfigurations()` was added
* `updateAdministrativeState(models.UpdateAdministrativeState)` was added
* `validateConfiguration()` was added
* `configurationType()` was added
* `resync(com.azure.core.util.Context)` was added

#### `models.InternalNetwork$Definition` was modified

* `withBgpConfiguration(models.InternalNetworkPropertiesBgpConfiguration)` was added
* `withExtension(models.Extension)` was added
* `withEgressAclId(java.lang.String)` was added
* `withIngressAclId(java.lang.String)` was added
* `withStaticRouteConfiguration(models.InternalNetworkPropertiesStaticRouteConfiguration)` was added
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.InternalNetworkPatchableProperties` was modified

* `withIngressAclId(java.lang.String)` was added
* `importRoutePolicy()` was added
* `withEgressAclId(java.lang.String)` was added
* `isMonitoringEnabled()` was added
* `ingressAclId()` was added
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `egressAclId()` was added
* `exportRoutePolicy()` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.IpPrefix$Update` was modified

* `withIpPrefixRules(java.util.List)` was added
* `withAnnotation(java.lang.String)` was added

#### `models.L2IsolationDomains` was modified

* `validateConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateConfiguration(java.lang.String,java.lang.String)` was added
* `commitConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `commitConfiguration(java.lang.String,java.lang.String)` was added

#### `models.L3IsolationDomainPatch` was modified

* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was added
* `withAnnotation(java.lang.String)` was added
* `withTags(java.util.Map)` was added
* `annotation()` was added

#### `models.NetworkFabricControllerPatch` was modified

* `withTags(java.util.Map)` was added

#### `models.NetworkToNetworkInterconnect` was modified

* `npbStaticRouteConfiguration()` was added
* `resourceGroupName()` was added
* `updateAdministrativeState(models.UpdateAdministrativeState)` was added
* `updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `ingressAclId()` was added
* `importRoutePolicy()` was added
* `optionBLayer3Configuration()` was added
* `updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was added
* `exportRoutePolicy()` was added
* `configurationState()` was added
* `egressAclId()` was added
* `updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `update()` was added

#### `models.NetworkFabricSku` was modified

* `details()` was added
* `maximumServerCount()` was added
* `supportedVersions()` was added

#### `models.IpCommunity$Definition` was modified

* `withIpCommunityRules(java.util.List)` was added

#### `models.L3IsolationDomain$Update` was modified

* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was added
* `withAnnotation(java.lang.String)` was added

#### `models.OptionAProperties` was modified

* `withPeerAsn(java.lang.Long)` was added
* `withBfdConfiguration(models.BfdConfiguration)` was added

#### `models.TerminalServerConfiguration` was modified

* `primaryIpv6Prefix()` was added
* `withPassword(java.lang.String)` was added
* `withSerialNumber(java.lang.String)` was added
* `withUsername(java.lang.String)` was added
* `secondaryIpv6Prefix()` was added
* `secondaryIpv4Prefix()` was added
* `primaryIpv4Prefix()` was added

#### `models.NetworkRack$Definition` was modified

* `withNetworkRackType(models.NetworkRackType)` was added

#### `models.IpCommunity` was modified

* `ipCommunityRules()` was added
* `configurationState()` was added
* `administrativeState()` was added

#### `models.IpExtendedCommunityPatch` was modified

* `withAnnotation(java.lang.String)` was added
* `ipExtendedCommunityRules()` was added
* `withTags(java.util.Map)` was added
* `withIpExtendedCommunityRules(java.util.List)` was added
* `annotation()` was added

#### `models.RoutePolicy` was modified

* `networkFabricId()` was added
* `commitConfiguration()` was added
* `updateAdministrativeState(models.UpdateAdministrativeState)` was added
* `updateAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `administrativeState()` was added
* `addressFamilyType()` was added
* `commitConfiguration(com.azure.core.util.Context)` was added
* `configurationState()` was added
* `validateConfiguration()` was added
* `validateConfiguration(com.azure.core.util.Context)` was added

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was added
* `withOptionBLayer3Configuration(models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration)` was added
* `withEgressAclId(java.lang.String)` was added
* `withIsManagementType(models.IsManagementType)` was added
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was added
* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was added
* `withIngressAclId(java.lang.String)` was added

#### `models.ExternalNetworkPatch` was modified

* `withOptionAProperties(models.ExternalNetworkPatchPropertiesOptionAProperties)` was added
* `importRoutePolicy()` was added
* `withOptionBProperties(models.L3OptionBProperties)` was added
* `exportRoutePolicy()` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.SupportedVersionProperties` was modified

* `withIsDefault(models.BooleanEnumProperty)` was added
* `isDefault()` was added

#### `models.BgpConfiguration` was modified

* `withPeerAsn(java.lang.Long)` was added

#### `models.IpExtendedCommunity` was modified

* `ipExtendedCommunityRules()` was added
* `administrativeState()` was added
* `configurationState()` was added

#### `models.RoutePolicies` was modified

* `commitConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `validateConfiguration(java.lang.String,java.lang.String)` was added
* `validateConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `commitConfiguration(java.lang.String,java.lang.String)` was added

#### `models.IpPrefixPatch` was modified

* `withAnnotation(java.lang.String)` was added
* `annotation()` was added
* `withTags(java.util.Map)` was added
* `ipPrefixRules()` was added
* `withIpPrefixRules(java.util.List)` was added

#### `models.AccessControlList$Definition` was modified

* `withAclsUrl(java.lang.String)` was added
* `withConfigurationType(models.ConfigurationType)` was added
* `withMatchConfigurations(java.util.List)` was added
* `withDynamicMatchConfigurations(java.util.List)` was added

#### `ManagedNetworkFabricManager` was modified

* `networkPacketBrokers()` was added
* `internetGatewayRules()` was added
* `neighborGroups()` was added
* `networkTapRules()` was added
* `internetGateways()` was added
* `networkTaps()` was added

#### `models.ExternalNetworks` was modified

* `listByL3IsolationDomain(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `listByL3IsolationDomain(java.lang.String,java.lang.String)` was added

#### `models.ExternalNetwork$Update` was modified

* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `withOptionAProperties(models.ExternalNetworkPatchPropertiesOptionAProperties)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added
* `withOptionBProperties(models.L3OptionBProperties)` was added

#### `models.L3IsolationDomain$Definition` was modified

* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was added

#### `models.L3IsolationDomain` was modified

* `validateConfiguration()` was added
* `commitConfiguration(com.azure.core.util.Context)` was added
* `validateConfiguration(com.azure.core.util.Context)` was added
* `configurationState()` was added
* `commitConfiguration()` was added

#### `models.StatementActionProperties` was modified

* `withActionType(models.RoutePolicyActionType)` was added

#### `models.NeighborAddress` was modified

* `configurationState()` was added

#### `models.AccessControlList$Update` was modified

* `withDynamicMatchConfigurations(java.util.List)` was added
* `withConfigurationType(models.ConfigurationType)` was added
* `withAclsUrl(java.lang.String)` was added
* `withMatchConfigurations(java.util.List)` was added

#### `models.InternalNetworks` was modified

* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `listByL3IsolationDomain(java.lang.String,java.lang.String)` was added
* `listByL3IsolationDomain(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `peerAsn()` was added
* `withEgressAclId(java.lang.String)` was added
* `fabricAsn()` was added
* `ingressAclId()` was added
* `egressAclId()` was added
* `withIngressAclId(java.lang.String)` was added
* `mtu()` was added
* `bfdConfiguration()` was added
* `withPeerAsn(java.lang.Long)` was added
* `vlanId()` was added

#### `models.L2IsolationDomain` was modified

* `configurationState()` was added
* `commitConfiguration()` was added
* `validateConfiguration()` was added
* `validateConfiguration(com.azure.core.util.Context)` was added
* `commitConfiguration(com.azure.core.util.Context)` was added

#### `models.AccessControlLists` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `validateConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `resync(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateConfiguration(java.lang.String,java.lang.String)` was added
* `resync(java.lang.String,java.lang.String)` was added

#### `models.InternalNetwork$Update` was modified

* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was added
* `withIngressAclId(java.lang.String)` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `withEgressAclId(java.lang.String)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.NetworkDevices` was modified

* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState)` was added
* `refreshConfiguration(java.lang.String,java.lang.String)` was added
* `reboot(java.lang.String,java.lang.String,models.RebootProperties)` was added
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion,com.azure.core.util.Context)` was added
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)` was added
* `reboot(java.lang.String,java.lang.String,models.RebootProperties,com.azure.core.util.Context)` was added
* `refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NetworkFabric` was modified

* `upgrade(models.UpdateVersion,com.azure.core.util.Context)` was added
* `getTopology()` was added
* `administrativeState()` was added
* `updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `fabricVersion()` was added
* `routerIds()` was added
* `refreshConfiguration()` was added
* `getTopology(com.azure.core.util.Context)` was added
* `commitConfiguration(com.azure.core.util.Context)` was added
* `configurationState()` was added
* `commitConfiguration()` was added
* `updateWorkloadManagementBfdConfiguration(models.UpdateAdministrativeState)` was added
* `updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `updateInfraManagementBfdConfiguration(models.UpdateAdministrativeState)` was added
* `validateConfiguration(models.ValidateConfigurationProperties)` was added
* `validateConfiguration(models.ValidateConfigurationProperties,com.azure.core.util.Context)` was added
* `refreshConfiguration(com.azure.core.util.Context)` was added
* `upgrade(models.UpdateVersion)` was added

#### `models.ExternalNetwork` was modified

* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was added
* `importRoutePolicy()` was added
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `configurationState()` was added
* `exportRoutePolicy()` was added

#### `models.NetworkFabric$Update` was modified

* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was added
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was added
* `withIpv4Prefix(java.lang.String)` was added
* `withRackCount(java.lang.Integer)` was added
* `withIpv6Prefix(java.lang.String)` was added
* `withFabricAsn(java.lang.Long)` was added
* `withServerCountPerRack(java.lang.Integer)` was added

#### `models.IpCommunity$Update` was modified

* `withIpCommunityRules(java.util.List)` was added

#### `models.NetworkDevicePatchParameters` was modified

* `withTags(java.util.Map)` was added

#### `models.L3IsolationDomains` was modified

* `validateConfiguration(java.lang.String,java.lang.String)` was added
* `commitConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `commitConfiguration(java.lang.String,java.lang.String)` was added

#### `models.NetworkToNetworkInterconnects` was modified

* `updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `listByNetworkFabric(java.lang.String,java.lang.String)` was added
* `updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `listByNetworkFabric(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added

#### `models.InternalNetworkPatch` was modified

* `isMonitoringEnabled()` was added
* `withIngressAclId(java.lang.String)` was added
* `withEgressAclId(java.lang.String)` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `exportRoutePolicy()` was added
* `importRoutePolicy()` was added
* `egressAclId()` was added
* `ingressAclId()` was added
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.RoutePolicyPatch` was modified

* `statements()` was added
* `withTags(java.util.Map)` was added
* `withStatements(java.util.List)` was added

#### `models.AccessControlListPatch` was modified

* `aclsUrl()` was added
* `withMatchConfigurations(java.util.List)` was added
* `configurationType()` was added
* `withDynamicMatchConfigurations(java.util.List)` was added
* `withAclsUrl(java.lang.String)` was added
* `dynamicMatchConfigurations()` was added
* `matchConfigurations()` was added
* `withTags(java.util.Map)` was added
* `withConfigurationType(models.ConfigurationType)` was added

#### `models.IpPrefix` was modified

* `configurationState()` was added
* `administrativeState()` was added

#### `models.OptionBProperties` was modified

* `routeTargets()` was added
* `withRouteTargets(models.RouteTargetInformation)` was added

#### `models.NetworkFabricController$Definition` was modified

* `withNfcSku(models.NfcSku)` was added
* `withIsWorkloadManagementNetworkEnabled(models.IsWorkloadManagementNetworkEnabled)` was added

#### `models.InternalNetwork` was modified

* `extension()` was added
* `egressAclId()` was added
* `exportRoutePolicy()` was added
* `ingressAclId()` was added
* `importRoutePolicy()` was added
* `isMonitoringEnabled()` was added
* `configurationState()` was added
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was added
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was added

#### `models.NetworkFabricPatchableProperties` was modified

* `withIpv4Prefix(java.lang.String)` was added
* `managementNetworkConfiguration()` was added
* `terminalServerConfiguration()` was added
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was added
* `fabricAsn()` was added
* `withIpv6Prefix(java.lang.String)` was added
* `withRackCount(java.lang.Integer)` was added
* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was added
* `withFabricAsn(java.lang.Long)` was added
* `ipv4Prefix()` was added
* `rackCount()` was added
* `withServerCountPerRack(java.lang.Integer)` was added
* `ipv6Prefix()` was added
* `serverCountPerRack()` was added

#### `models.RoutePolicy$Update` was modified

* `withStatements(java.util.List)` was added

#### `models.IpExtendedCommunity$Definition` was modified

* `withIpExtendedCommunityRules(java.util.List)` was added

#### `models.NetworkRack` was modified

* `networkRackType()` was added

#### `models.ExternalNetwork$Definition` was modified

* `withOptionBProperties(models.L3OptionBProperties)` was added
* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.Layer2Configuration` was modified

* `withMtu(java.lang.Integer)` was added
* `withInterfaces(java.util.List)` was added

#### `models.L2IsolationDomainPatch` was modified

* `withTags(java.util.Map)` was added

#### `models.NetworkFabric$Definition` was modified

* `withFabricAsn(long)` was added
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationProperties)` was added
* `withRackCount(java.lang.Integer)` was added

#### `models.NetworkFabrics` was modified

* `updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `refreshConfiguration(java.lang.String,java.lang.String)` was added
* `commitConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `commitConfiguration(java.lang.String,java.lang.String)` was added
* `updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `updateInfraManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was added
* `refreshConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion,com.azure.core.util.Context)` was added
* `getTopology(java.lang.String,java.lang.String)` was added
* `upgrade(java.lang.String,java.lang.String,models.UpdateVersion)` was added
* `validateConfiguration(java.lang.String,java.lang.String,models.ValidateConfigurationProperties)` was added
* `validateConfiguration(java.lang.String,java.lang.String,models.ValidateConfigurationProperties,com.azure.core.util.Context)` was added
* `updateWorkloadManagementBfdConfiguration(java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was added
* `getTopology(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NetworkDevice` was modified

* `administrativeState()` was added
* `upgrade(models.UpdateVersion)` was added
* `refreshConfiguration(com.azure.core.util.Context)` was added
* `reboot(models.RebootProperties)` was added
* `reboot(models.RebootProperties,com.azure.core.util.Context)` was added
* `updateAdministrativeState(models.UpdateDeviceAdministrativeState)` was added
* `refreshConfiguration()` was added
* `managementIpv4Address()` was added
* `upgrade(models.UpdateVersion,com.azure.core.util.Context)` was added
* `managementIpv6Address()` was added
* `updateAdministrativeState(models.UpdateDeviceAdministrativeState,com.azure.core.util.Context)` was added
* `configurationState()` was added

#### `models.NetworkFabricController` was modified

* `isWorkloadManagementNetworkEnabled()` was added
* `tenantInternetGatewayIds()` was added
* `nfcSku()` was added

#### `models.BfdConfiguration` was modified

* `intervalInMilliSeconds()` was added
* `withIntervalInMilliSeconds(java.lang.Integer)` was added
* `withMultiplier(java.lang.Integer)` was added

#### `models.IpCommunityPatch` was modified

* `withIpCommunityRules(java.util.List)` was added
* `withTags(java.util.Map)` was added
* `ipCommunityRules()` was added

#### `models.ExternalNetworkPatchableProperties` was modified

* `withExportRoutePolicy(models.ExportRoutePolicy)` was added
* `importRoutePolicy()` was added
* `exportRoutePolicy()` was added
* `withImportRoutePolicy(models.ImportRoutePolicy)` was added

#### `models.NetworkInterfaces` was modified

* `listByNetworkDevice(java.lang.String,java.lang.String)` was added
* `listByNetworkDevice(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.IpExtendedCommunity$Update` was modified

* `withAnnotation(java.lang.String)` was added
* `withIpExtendedCommunityRules(java.util.List)` was added

#### `models.UpdateAdministrativeState` was modified

* `withState(models.EnableDisableState)` was added

#### `models.VpnConfigurationProperties` was modified

* `withOptionAProperties(models.VpnConfigurationPropertiesOptionAProperties)` was added
* `withOptionBProperties(models.OptionBProperties)` was added
* `withNetworkToNetworkInterconnectId(java.lang.String)` was added

#### `models.StatementConditionProperties` was modified

* `type()` was added
* `withType(models.RoutePolicyConditionType)` was added

#### `models.RoutePolicy$Definition` was modified

* `withAddressFamilyType(models.AddressFamilyType)` was added
* `withNetworkFabricId(java.lang.String)` was added

## 1.0.0-beta.1 (2023-06-27)

- Azure Resource Manager Managed Network Fabric client library for Java. This package contains Microsoft Azure SDK for Managed Network Fabric Management SDK. Self service experience for Azure Network Fabric API. Package tag package-2023-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
