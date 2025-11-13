# Release History

## 1.2.0-beta.1 (2025-11-13)

- Azure Resource Manager fileshares client library for Java. This package contains Microsoft Azure SDK for fileshares Management SDK. Self service experience for Azure Network Fabric API. Package api-version 2024-06-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

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

#### `models.IpAddressType` was removed

#### `ManagedNetworkFabricManager` was removed

#### `models.NeighborGroupsListResult` was removed

#### `models.NetworkTapRulePatchableProperties` was removed

#### `models.NetworkDeviceSkusListResult` was removed

#### `models.NetworkTapRulesListResult` was removed

#### `models.L3IsolationDomainPatchableProperties` was removed

#### `models.AnnotationResource` was removed

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

#### `ManagedNetworkFabricManager$Configurable` was removed

#### `models.EnableDisableOnResources` was removed

#### `models.AccessControlListPatchableProperties` was removed

#### `models.NetworkRacksListResult` was removed

#### `models.NetworkFabricsListResult` was removed

#### `models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration` was removed

#### `models.NeighborGroupPatchableProperties` was removed

#### `models.TagsUpdate` was removed

#### `models.VpnConfigurationPropertiesOptionAProperties` was removed

#### `models.NetworkFabricPatchablePropertiesTerminalServerConfiguration` was removed

#### `models.InternetGatewayRulesListResult` was removed

#### `models.ManagementNetworkConfigurationPatchableProperties` was removed

#### `models.NetworkFabricSkusListResult` was removed

#### `models.NetworkToNetworkInterconnectsList` was removed

#### `models.CommonMatchConditions` was removed

#### `models.RoutePoliciesListResult` was removed

#### `models.L2IsolationDomainsListResult` was removed

#### `models.IpPrefixPatchableProperties` was removed

#### `models.InternetGatewaysListResult` was removed

#### `models.IpCommunitySetOperationProperties` was removed

#### `models.NetworkInterfacesList` was removed

#### `models.AccessControlListsListResult` was removed

#### `models.IpExtendedCommunityDeleteOperationProperties` was removed

#### `models.Layer3IpPrefixProperties` was removed

#### `models.InternalNetworksList` was removed

#### `models.NetworkFabricControllersListResult` was removed

#### `models.NetworkDevicePatchableProperties` was removed

#### `models.NetworkTapsListResult` was removed

#### `models.IpExtendedCommunityAddOperationProperties` was removed

#### `models.NetworkRack$DefinitionStages` was modified

* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.NetworkDevice$DefinitionStages` was modified

* Required stage 3 was added

#### `models.InternalNetwork$DefinitionStages` was modified

* `withVlanId(int)` was removed in stage 2

#### `models.IpCommunity$DefinitionStages` was modified

* Required stage 3 was added

#### `models.IpExtendedCommunity$DefinitionStages` was modified

* `withIpExtendedCommunityRules(java.util.List)` was removed in stage 3

#### `models.NetworkFabricController$DefinitionStages` was modified

* Required stage 3 was added

#### `models.NetworkFabric$DefinitionStages` was modified

* `withNetworkFabricSku(java.lang.String)` was removed in stage 3

#### `models.NetworkTapRule$DefinitionStages` was modified

* Required stage 3 was added

#### `models.InternetGatewayRule$DefinitionStages` was modified

* `withRuleProperties(models.RuleProperties)` was removed in stage 3

#### `models.NetworkToNetworkInterconnect$DefinitionStages` was modified

* `withUseOptionB(models.BooleanEnumProperty)` was removed in stage 2

#### `models.NeighborGroup$DefinitionStages` was modified

* Required stage 3 was added

#### `models.RoutePolicy$DefinitionStages` was modified

* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.NetworkInterface$DefinitionStages` was modified

* Required stage 2 was added

#### `models.L3IsolationDomain$DefinitionStages` was modified

* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.ExternalNetwork$DefinitionStages` was modified

* `withPeeringOption(models.PeeringOption)` was removed in stage 2

#### `models.IpPrefix$DefinitionStages` was modified

* Required stage 3 was added

#### `models.L2IsolationDomain$DefinitionStages` was modified

* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.NetworkPacketBroker$DefinitionStages` was modified

* `withNetworkFabricId(java.lang.String)` was removed in stage 3

#### `models.NetworkTap$DefinitionStages` was modified

* `withNetworkPacketBrokerId(java.lang.String)` was removed in stage 3

#### `models.AccessControlList$DefinitionStages` was modified

* Required stage 3 was added

#### `models.InternetGateway$DefinitionStages` was modified

* `withTypePropertiesType(models.GatewayType)` was removed in stage 3

#### `models.InternalNetwork$Definition` was modified

* `withBgpConfiguration(models.InternalNetworkPropertiesBgpConfiguration)` was removed
* `withEgressAclId(java.lang.String)` was removed
* `withConnectedIPv4Subnets(java.util.List)` was removed
* `withExtension(models.Extension)` was removed
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was removed
* `withVlanId(int)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withIngressAclId(java.lang.String)` was removed
* `withMtu(java.lang.Integer)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withStaticRouteConfiguration(models.InternalNetworkPropertiesStaticRouteConfiguration)` was removed
* `withConnectedIPv6Subnets(java.util.List)` was removed

#### `models.ImportRoutePolicyInformation` was modified

* `validate()` was removed

#### `models.IpPrefix$Update` was modified

* `withIpPrefixRules(java.util.List)` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.L3IsolationDomainPatch` was modified

* `aggregateRouteConfiguration()` was removed
* `connectedSubnetRoutePolicy()` was removed
* `withTags(java.util.Map)` was removed
* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was removed
* `withRedistributeConnectedSubnets(models.RedistributeConnectedSubnets)` was removed
* `annotation()` was removed
* `redistributeStaticRoutes()` was removed
* `withRedistributeStaticRoutes(models.RedistributeStaticRoutes)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withAggregateRouteConfiguration(models.AggregateRouteConfiguration)` was removed
* `redistributeConnectedSubnets()` was removed
* `validate()` was removed

#### `models.ExportRoutePolicyInformation` was modified

* `validate()` was removed

#### `models.NetworkTapRuleAction` was modified

* `validate()` was removed

#### `models.IpGroupProperties` was modified

* `models.IpAddressType ipAddressType()` -> `models.IPAddressType ipAddressType()`
* `validate()` was removed
* `withIpAddressType(models.IpAddressType)` was removed

#### `models.NetworkInterfacePatch` was modified

* `annotation()` was removed
* `validate()` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.NetworkPacketBroker$Definition` was modified

* `withNetworkFabricId(java.lang.String)` was removed

#### `models.DestinationProperties` was modified

* `validate()` was removed

#### `models.TerminalServerConfiguration` was modified

* `withUsername(java.lang.String)` was removed
* `validate()` was removed
* `withPassword(java.lang.String)` was removed
* `withSerialNumber(java.lang.String)` was removed

#### `models.NetworkTapRuleMatchCondition` was modified

* `withIpCondition(models.IpMatchCondition)` was removed
* `withProtocolTypes(java.util.List)` was removed
* `validate()` was removed
* `withVlanMatchCondition(models.VlanMatchCondition)` was removed

#### `models.RebootProperties` was modified

* `validate()` was removed

#### `models.NetworkDevice$Update` was modified

* `withHostname(java.lang.String)` was removed
* `withSerialNumber(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.VpnConfigurationPatchableProperties` was modified

* `models.OptionBProperties optionBProperties()` -> `models.VpnOptionBPatchProperties optionBProperties()`
* `validate()` was removed
* `models.VpnConfigurationPatchablePropertiesOptionAProperties optionAProperties()` -> `models.VpnOptionAPatchProperties optionAProperties()`
* `withOptionBProperties(models.OptionBProperties)` was removed
* `withOptionAProperties(models.VpnConfigurationPatchablePropertiesOptionAProperties)` was removed

#### `models.ExternalNetworkPatch` was modified

* `withOptionAProperties(models.ExternalNetworkPatchPropertiesOptionAProperties)` was removed
* `withNetworkToNetworkInterconnectId(java.lang.String)` was removed
* `withPeeringOption(models.PeeringOption)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `exportRoutePolicyId()` was removed
* `importRoutePolicyId()` was removed
* `optionAProperties()` was removed
* `withOptionBProperties(models.L3OptionBProperties)` was removed
* `importRoutePolicy()` was removed
* `peeringOption()` was removed
* `exportRoutePolicy()` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `optionBProperties()` was removed
* `annotation()` was removed
* `validate()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `networkToNetworkInterconnectId()` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed

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

#### `models.IpExtendedCommunity` was modified

* `ipExtendedCommunityRules()` was removed
* `annotation()` was removed
* `administrativeState()` was removed
* `configurationState()` was removed
* `provisioningState()` was removed

#### `models.IpPrefixPatch` was modified

* `annotation()` was removed
* `validate()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `ipPrefixRules()` was removed
* `withIpPrefixRules(java.util.List)` was removed

#### `models.NetworkInterface` was modified

* `administrativeState()` was removed
* `interfaceType()` was removed
* `provisioningState()` was removed
* `physicalIdentifier()` was removed
* `ipv6Address()` was removed
* `connectedTo()` was removed
* `annotation()` was removed
* `ipv4Address()` was removed

#### `models.NetworkInterface$Update` was modified

* `withAnnotation(java.lang.String)` was removed

#### `models.InternetGatewayRulePatch` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed

#### `models.ExternalNetworks` was modified

* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed

#### `models.ExternalNetwork$Update` was modified

* `withOptionBProperties(models.L3OptionBProperties)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withNetworkToNetworkInterconnectId(java.lang.String)` was removed
* `withOptionAProperties(models.ExternalNetworkPatchPropertiesOptionAProperties)` was removed
* `withPeeringOption(models.PeeringOption)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed

#### `models.PortGroupProperties` was modified

* `validate()` was removed

#### `models.L3IsolationDomain` was modified

* `annotation()` was removed
* `networkFabricId()` was removed
* `redistributeStaticRoutes()` was removed
* `redistributeConnectedSubnets()` was removed
* `provisioningState()` was removed
* `connectedSubnetRoutePolicy()` was removed
* `aggregateRouteConfiguration()` was removed
* `configurationState()` was removed
* `administrativeState()` was removed

#### `models.NetworkFabricController$Update` was modified

* `withInfrastructureExpressRouteConnections(java.util.List)` was removed
* `withWorkloadExpressRouteConnections(java.util.List)` was removed

#### `models.VlanMatchCondition` was modified

* `validate()` was removed

#### `models.NeighborGroup$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withDestination(models.NeighborGroupDestination)` was removed

#### `models.NeighborAddress` was modified

* `validate()` was removed

#### `models.AccessControlList$Update` was modified

* `withAnnotation(java.lang.String)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed
* `withMatchConfigurations(java.util.List)` was removed
* `withConfigurationType(models.ConfigurationType)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed
* `withAclsUrl(java.lang.String)` was removed

#### `models.NetworkDeviceSku` was modified

* `supportedVersions()` was removed
* `model()` was removed
* `manufacturer()` was removed
* `provisioningState()` was removed
* `interfaces()` was removed
* `supportedRoleTypes()` was removed

#### `models.InternalNetworks` was modified

* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `updateStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `peerAsn()` was removed
* `withPeerAsn(java.lang.Long)` was removed
* `withVlanId(java.lang.Integer)` was removed
* `java.lang.Integer vlanId()` -> `int vlanId()`
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `fabricAsn()` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `validate()` was removed

#### `models.AccessControlListMatchCondition` was modified

* `withIpCondition(models.IpMatchCondition)` was removed
* `withProtocolTypes(java.util.List)` was removed
* `validate()` was removed
* `withVlanMatchCondition(models.VlanMatchCondition)` was removed

#### `models.ManagedResourceGroupConfiguration` was modified

* `validate()` was removed

#### `models.InternalNetwork$Update` was modified

* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withBgpConfiguration(models.BgpConfiguration)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withConnectedIPv4Subnets(java.util.List)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withIngressAclId(java.lang.String)` was removed
* `withConnectedIPv6Subnets(java.util.List)` was removed
* `withEgressAclId(java.lang.String)` was removed
* `withMtu(java.lang.Integer)` was removed

#### `models.NetworkTapRule$Definition` was modified

* `withMatchConfigurations(java.util.List)` was removed
* `withConfigurationType(models.ConfigurationType)` was removed
* `withPollingIntervalInSeconds(models.PollingIntervalInSeconds)` was removed
* `withTapRulesUrl(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed

#### `models.L3OptionBProperties` was modified

* `validate()` was removed

#### `models.InternetGatewayRule$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withRuleProperties(models.RuleProperties)` was removed

#### `models.NetworkTapRule$Update` was modified

* `withMatchConfigurations(java.util.List)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withTapRulesUrl(java.lang.String)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed
* `withConfigurationType(models.ConfigurationType)` was removed

#### `models.UpgradeNetworkFabricProperties` was modified

* `withVersion(java.lang.String)` was removed
* `validate()` was removed

#### `models.L2IsolationDomain$Definition` was modified

* `withMtu(java.lang.Integer)` was removed
* `withNetworkFabricId(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withVlanId(int)` was removed

#### `models.L3ExportRoutePolicy` was modified

* `validate()` was removed

#### `models.IpCommunity$Update` was modified

* `withIpCommunityRules(java.util.List)` was removed

#### `models.InternetGateway$Definition` was modified

* `withNetworkFabricControllerId(java.lang.String)` was removed
* `withInternetGatewayRuleId(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withTypePropertiesType(models.GatewayType)` was removed

#### `models.RuleProperties` was modified

* `validate()` was removed

#### `models.InternalNetworkPatch` was modified

* `exportRoutePolicy()` was removed
* `withConnectedIPv4Subnets(java.util.List)` was removed
* `mtu()` was removed
* `connectedIPv6Subnets()` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withMtu(java.lang.Integer)` was removed
* `withStaticRouteConfiguration(models.StaticRouteConfiguration)` was removed
* `bgpConfiguration()` was removed
* `connectedIPv4Subnets()` was removed
* `staticRouteConfiguration()` was removed
* `exportRoutePolicyId()` was removed
* `importRoutePolicy()` was removed
* `withBgpConfiguration(models.BgpConfiguration)` was removed
* `withIsMonitoringEnabled(models.IsMonitoringEnabled)` was removed
* `importRoutePolicyId()` was removed
* `withIngressAclId(java.lang.String)` was removed
* `withEgressAclId(java.lang.String)` was removed
* `isMonitoringEnabled()` was removed
* `annotation()` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withAnnotation(java.lang.String)` was removed
* `egressAclId()` was removed
* `validate()` was removed
* `ingressAclId()` was removed
* `withConnectedIPv6Subnets(java.util.List)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed

#### `models.RoutePolicyPatch` was modified

* `defaultAction()` was removed
* `withStatements(java.util.List)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed
* `statements()` was removed
* `validate()` was removed
* `withTags(java.util.Map)` was removed

#### `models.AccessControlListPatch` was modified

* `validate()` was removed
* `aclsUrl()` was removed
* `withAclsUrl(java.lang.String)` was removed
* `defaultAction()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed
* `dynamicMatchConfigurations()` was removed
* `withConfigurationType(models.ConfigurationType)` was removed
* `withTags(java.util.Map)` was removed
* `annotation()` was removed
* `configurationType()` was removed
* `withMatchConfigurations(java.util.List)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed
* `matchConfigurations()` was removed

#### `models.IpPrefix` was modified

* `ipPrefixRules()` was removed
* `provisioningState()` was removed
* `annotation()` was removed
* `administrativeState()` was removed
* `configurationState()` was removed

#### `models.InternetGateway` was modified

* `internetGatewayRuleId()` was removed
* `annotation()` was removed
* `provisioningState()` was removed
* `port()` was removed
* `networkFabricControllerId()` was removed
* `typePropertiesType()` was removed
* `ipv4Address()` was removed

#### `models.NetworkFabricController$Definition` was modified

* `withInfrastructureExpressRouteConnections(java.util.List)` was removed
* `withIpv6AddressSpace(java.lang.String)` was removed
* `withNfcSku(models.NfcSku)` was removed
* `withManagedResourceGroupConfiguration(models.ManagedResourceGroupConfiguration)` was removed
* `withIsWorkloadManagementNetworkEnabled(models.IsWorkloadManagementNetworkEnabled)` was removed
* `withIpv4AddressSpace(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withWorkloadExpressRouteConnections(java.util.List)` was removed

#### `models.AccessControlListMatchConfiguration` was modified

* `models.IpAddressType ipAddressType()` -> `models.IPAddressType ipAddressType()`
* `validate()` was removed
* `withIpAddressType(models.IpAddressType)` was removed

#### `models.InternalNetwork` was modified

* `connectedIPv4Subnets()` was removed
* `isMonitoringEnabled()` was removed
* `bgpConfiguration()` was removed
* `connectedIPv6Subnets()` was removed
* `egressAclId()` was removed
* `provisioningState()` was removed
* `configurationState()` was removed
* `ingressAclId()` was removed
* `extension()` was removed
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was removed
* `exportRoutePolicy()` was removed
* `staticRouteConfiguration()` was removed
* `mtu()` was removed
* `updateBgpAdministrativeState(models.UpdateAdministrativeState)` was removed
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `updateBgpAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `exportRoutePolicyId()` was removed
* `importRoutePolicy()` was removed
* `annotation()` was removed
* `importRoutePolicyId()` was removed
* `vlanId()` was removed
* `administrativeState()` was removed

#### `models.IpCommunityIdList` was modified

* `validate()` was removed

#### `models.IpExtendedCommunity$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withIpExtendedCommunityRules(java.util.List)` was removed

#### `models.NetworkRack` was modified

* `provisioningState()` was removed
* `networkFabricId()` was removed
* `annotation()` was removed
* `networkRackType()` was removed
* `networkDevices()` was removed

#### `models.CommonDynamicMatchConfiguration` was modified

* `validate()` was removed

#### `models.NetworkDevice$Definition` was modified

* `withSerialNumber(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withHostname(java.lang.String)` was removed
* `withNetworkDeviceSku(java.lang.String)` was removed

#### `models.NeighborGroupDestination` was modified

* `validate()` was removed

#### `models.InternetGateway$Update` was modified

* `withInternetGatewayRuleId(java.lang.String)` was removed

#### `models.ValidateConfigurationProperties` was modified

* `validate()` was removed

#### `models.NetworkFabric$Definition` was modified

* `withFabricVersion(java.lang.String)` was removed
* `withTerminalServerConfiguration(models.TerminalServerConfiguration)` was removed
* `withServerCountPerRack(int)` was removed
* `withIpv6Prefix(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationProperties)` was removed
* `withIpv4Prefix(java.lang.String)` was removed
* `withNetworkFabricControllerId(java.lang.String)` was removed
* `withRackCount(java.lang.Integer)` was removed
* `withFabricAsn(long)` was removed
* `withNetworkFabricSku(java.lang.String)` was removed

#### `models.NetworkDevice` was modified

* `version()` was removed
* `annotation()` was removed
* `serialNumber()` was removed
* `administrativeState()` was removed
* `hostname()` was removed
* `networkRackId()` was removed
* `managementIpv6Address()` was removed
* `networkDeviceRole()` was removed
* `provisioningState()` was removed
* `managementIpv4Address()` was removed
* `configurationState()` was removed
* `networkDeviceSku()` was removed

#### `models.NetworkFabricController` was modified

* `annotation()` was removed
* `infrastructureExpressRouteConnections()` was removed
* `provisioningState()` was removed
* `infrastructureServices()` was removed
* `workloadExpressRouteConnections()` was removed
* `workloadServices()` was removed
* `ipv6AddressSpace()` was removed
* `managedResourceGroupConfiguration()` was removed
* `nfcSku()` was removed
* `tenantInternetGatewayIds()` was removed
* `isWorkloadManagementNetworkEnabled()` was removed
* `networkFabricIds()` was removed
* `workloadManagementNetwork()` was removed
* `ipv4AddressSpace()` was removed

#### `models.ActionIpExtendedCommunityProperties` was modified

* `validate()` was removed
* `withAdd(models.IpExtendedCommunityIdList)` was removed

#### `models.NetworkPacketBrokerPatch` was modified

* `withTags(java.util.Map)` was removed
* `validate()` was removed

#### `models.IpCommunityPatch` was modified

* `withIpCommunityRules(java.util.List)` was removed
* `ipCommunityRules()` was removed
* `validate()` was removed
* `withTags(java.util.Map)` was removed

#### `models.NetworkInterface$Definition` was modified

* `withAnnotation(java.lang.String)` was removed

#### `models.IpExtendedCommunity$Update` was modified

* `withIpExtendedCommunityRules(java.util.List)` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.UpdateAdministrativeState` was modified

* `validate()` was removed
* `withResourceIds(java.util.List)` was removed

#### `models.VpnConfigurationProperties` was modified

* `models.VpnConfigurationPropertiesOptionAProperties optionAProperties()` -> `models.VpnOptionAProperties optionAProperties()`
* `withOptionBProperties(models.OptionBProperties)` was removed
* `withOptionAProperties(models.VpnConfigurationPropertiesOptionAProperties)` was removed
* `validate()` was removed
* `models.OptionBProperties optionBProperties()` -> `models.VpnOptionBProperties optionBProperties()`

#### `models.RoutePolicy$Definition` was modified

* `withNetworkFabricId(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed
* `withAddressFamilyType(models.AddressFamilyType)` was removed
* `withStatements(java.util.List)` was removed

#### `models.IpPrefix$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withIpPrefixRules(java.util.List)` was removed

#### `models.UpdateDeviceAdministrativeState` was modified

* `validate()` was removed
* `withResourceIds(java.util.List)` was removed

#### `models.AccessControlList` was modified

* `provisioningState()` was removed
* `dynamicMatchConfigurations()` was removed
* `lastSyncedTime()` was removed
* `matchConfigurations()` was removed
* `aclsUrl()` was removed
* `annotation()` was removed
* `configurationState()` was removed
* `administrativeState()` was removed
* `configurationType()` was removed
* `defaultAction()` was removed

#### `models.DeviceInterfaceProperties` was modified

* `DeviceInterfaceProperties()` was changed to private access
* `withInterfaceType(java.lang.String)` was removed
* `withSupportedConnectorTypes(java.util.List)` was removed
* `validate()` was removed
* `withIdentifier(java.lang.String)` was removed

#### `models.ConnectedSubnet` was modified

* `withAnnotation(java.lang.String)` was removed
* `validate()` was removed

#### `models.NetworkFabricControllerPatch` was modified

* `withTags(java.util.Map)` was removed
* `infrastructureExpressRouteConnections()` was removed
* `withInfrastructureExpressRouteConnections(java.util.List)` was removed
* `validate()` was removed
* `workloadExpressRouteConnections()` was removed
* `withWorkloadExpressRouteConnections(java.util.List)` was removed

#### `models.StaticRouteConfiguration` was modified

* `validate()` was removed

#### `models.RoutePolicyStatementProperties` was modified

* `withAnnotation(java.lang.String)` was removed
* `validate()` was removed

#### `models.NetworkTap$Update` was modified

* `withAnnotation(java.lang.String)` was removed
* `withDestinationsForUpdate(java.util.List)` was removed
* `withPollingType(models.PollingType)` was removed

#### `models.NetworkToNetworkInterconnect` was modified

* `importRoutePolicy()` was removed
* `layer2Configuration()` was removed
* `administrativeState()` was removed
* `useOptionB()` was removed
* `nniType()` was removed
* `egressAclId()` was removed
* `updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `isManagementType()` was removed
* `provisioningState()` was removed
* `exportRoutePolicy()` was removed
* `ingressAclId()` was removed
* `updateNpbStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was removed
* `npbStaticRouteConfiguration()` was removed
* `optionBLayer3Configuration()` was removed
* `configurationState()` was removed

#### `models.NetworkTapRule` was modified

* `provisioningState()` was removed
* `configurationType()` was removed
* `dynamicMatchConfigurations()` was removed
* `matchConfigurations()` was removed
* `lastSyncedTime()` was removed
* `configurationState()` was removed
* `tapRulesUrl()` was removed
* `networkTapId()` was removed
* `administrativeState()` was removed
* `annotation()` was removed
* `pollingIntervalInSeconds()` was removed

#### `models.NetworkToNetworkInterconnectPatch` was modified

* `layer2Configuration()` was removed
* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was removed
* `exportRoutePolicy()` was removed
* `optionBLayer3Configuration()` was removed
* `withEgressAclId(java.lang.String)` was removed
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was removed
* `ingressAclId()` was removed
* `name()` was removed
* `withIngressAclId(java.lang.String)` was removed
* `withLayer2Configuration(models.Layer2Configuration)` was removed
* `egressAclId()` was removed
* `withOptionBLayer3Configuration(models.OptionBLayer3Configuration)` was removed
* `npbStaticRouteConfiguration()` was removed
* `validate()` was removed
* `importRoutePolicy()` was removed
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was removed

#### `models.NetworkFabricSku` was modified

* `details()` was removed
* `typePropertiesType()` was removed
* `provisioningState()` was removed
* `maxComputeRacks()` was removed
* `maximumServerCount()` was removed
* `supportedVersions()` was removed

#### `models.IpCommunity$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withIpCommunityRules(java.util.List)` was removed

#### `models.NpbStaticRouteConfiguration` was modified

* `validate()` was removed

#### `models.L3IsolationDomain$Update` was modified

* `withRedistributeStaticRoutes(models.RedistributeStaticRoutes)` was removed
* `withRedistributeConnectedSubnets(models.RedistributeConnectedSubnets)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withAggregateRouteConfiguration(models.AggregateRouteConfiguration)` was removed
* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was removed

#### `models.ConnectedSubnetRoutePolicy` was modified

* `exportRoutePolicyId()` was removed
* `validate()` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed

#### `models.NetworkRack$Definition` was modified

* `withNetworkRackType(models.NetworkRackType)` was removed
* `withNetworkFabricId(java.lang.String)` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.IpCommunity` was modified

* `ipCommunityRules()` was removed
* `provisioningState()` was removed
* `configurationState()` was removed
* `administrativeState()` was removed
* `annotation()` was removed

#### `models.StaticRouteProperties` was modified

* `validate()` was removed

#### `models.IpExtendedCommunityPatch` was modified

* `withAnnotation(java.lang.String)` was removed
* `annotation()` was removed
* `withIpExtendedCommunityRules(java.util.List)` was removed
* `withTags(java.util.Map)` was removed
* `ipExtendedCommunityRules()` was removed
* `validate()` was removed

#### `models.RoutePolicy` was modified

* `configurationState()` was removed
* `defaultAction()` was removed
* `annotation()` was removed
* `addressFamilyType()` was removed
* `administrativeState()` was removed
* `networkFabricId()` was removed
* `provisioningState()` was removed
* `statements()` was removed

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withLayer2Configuration(models.Layer2Configuration)` was removed
* `withIngressAclId(java.lang.String)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was removed
* `withIsManagementType(models.IsManagementType)` was removed
* `withOptionBLayer3Configuration(models.NetworkToNetworkInterconnectPropertiesOptionBLayer3Configuration)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was removed
* `withEgressAclId(java.lang.String)` was removed
* `withUseOptionB(models.BooleanEnumProperty)` was removed
* `withNniType(models.NniType)` was removed
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was removed

#### `models.VlanGroupProperties` was modified

* `validate()` was removed

#### `models.NetworkTap$Definition` was modified

* `withAnnotation(java.lang.String)` was removed
* `withPollingType(models.PollingType)` was removed
* `withNetworkPacketBrokerId(java.lang.String)` was removed
* `withDestinations(java.util.List)` was removed

#### `models.BgpConfiguration` was modified

* `peerAsn()` was removed
* `withPeerAsn(java.lang.Long)` was removed
* `validate()` was removed
* `fabricAsn()` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.NetworkFabricPatch` was modified

* `ipv4Prefix()` was removed
* `ipv6Prefix()` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was removed
* `annotation()` was removed
* `withTags(java.util.Map)` was removed
* `withIpv6Prefix(java.lang.String)` was removed
* `withRackCount(java.lang.Integer)` was removed
* `managementNetworkConfiguration()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was removed
* `serverCountPerRack()` was removed
* `validate()` was removed
* `withServerCountPerRack(java.lang.Integer)` was removed
* `rackCount()` was removed
* `withIpv4Prefix(java.lang.String)` was removed
* `fabricAsn()` was removed
* `withFabricAsn(java.lang.Long)` was removed
* `terminalServerConfiguration()` was removed

#### `models.AccessControlListPortCondition` was modified

* `withPortGroupNames(java.util.List)` was removed
* `withPorts(java.util.List)` was removed
* `validate()` was removed
* `withPortType(models.PortType)` was removed
* `withLayer4Protocol(models.Layer4Protocol)` was removed

#### `models.IpPrefixRule` was modified

* `validate()` was removed

#### `models.NeighborGroup$Update` was modified

* `withAnnotation(java.lang.String)` was removed
* `withDestination(models.NeighborGroupDestination)` was removed

#### `models.AccessControlList$Definition` was modified

* `withDynamicMatchConfigurations(java.util.List)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withConfigurationType(models.ConfigurationType)` was removed
* `withAclsUrl(java.lang.String)` was removed
* `withMatchConfigurations(java.util.List)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed

#### `models.AggregateRouteConfiguration` was modified

* `validate()` was removed

#### `models.L3IsolationDomain$Definition` was modified

* `withRedistributeStaticRoutes(models.RedistributeStaticRoutes)` was removed
* `withConnectedSubnetRoutePolicy(models.ConnectedSubnetRoutePolicy)` was removed
* `withAggregateRouteConfiguration(models.AggregateRouteConfiguration)` was removed
* `withNetworkFabricId(java.lang.String)` was removed
* `withRedistributeConnectedSubnets(models.RedistributeConnectedSubnets)` was removed
* `withAnnotation(java.lang.String)` was removed

#### `models.IsolationDomainProperties` was modified

* `validate()` was removed

#### `models.StatementActionProperties` was modified

* `validate()` was removed

#### `models.PortCondition` was modified

* `validate()` was removed

#### `models.ControllerServices` was modified

* `ControllerServices()` was changed to private access
* `withIpv4AddressSpaces(java.util.List)` was removed
* `validate()` was removed
* `withIpv6AddressSpaces(java.util.List)` was removed

#### `models.NetworkTapRuleMatchConfiguration` was modified

* `validate()` was removed
* `withIpAddressType(models.IpAddressType)` was removed
* `models.IpAddressType ipAddressType()` -> `models.IPAddressType ipAddressType()`

#### `models.L2IsolationDomain$Update` was modified

* `withAnnotation(java.lang.String)` was removed
* `withMtu(java.lang.Integer)` was removed

#### `models.L2IsolationDomain` was modified

* `networkFabricId()` was removed
* `provisioningState()` was removed
* `configurationState()` was removed
* `annotation()` was removed
* `mtu()` was removed
* `vlanId()` was removed
* `administrativeState()` was removed

#### `models.NetworkFabric` was modified

* `l3IsolationDomains()` was removed
* `l2IsolationDomains()` was removed
* `routerIds()` was removed
* `racks()` was removed
* `serverCountPerRack()` was removed
* `ipv6Prefix()` was removed
* `managementNetworkConfiguration()` was removed
* `rackCount()` was removed
* `annotation()` was removed
* `terminalServerConfiguration()` was removed
* `networkFabricControllerId()` was removed
* `configurationState()` was removed
* `provisioningState()` was removed
* `networkFabricSku()` was removed
* `fabricAsn()` was removed
* `administrativeState()` was removed
* `ipv4Prefix()` was removed
* `fabricVersion()` was removed

#### `models.IpCommunityRule` was modified

* `validate()` was removed

#### `models.ExternalNetwork` was modified

* `exportRoutePolicy()` was removed
* `provisioningState()` was removed
* `configurationState()` was removed
* `optionAProperties()` was removed
* `importRoutePolicyId()` was removed
* `annotation()` was removed
* `optionBProperties()` was removed
* `administrativeState()` was removed
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed
* `updateStaticRouteBfdAdministrativeState(models.UpdateAdministrativeState)` was removed
* `importRoutePolicy()` was removed
* `peeringOption()` was removed
* `networkToNetworkInterconnectId()` was removed
* `exportRoutePolicyId()` was removed

#### `models.UpdateVersion` was modified

* `validate()` was removed

#### `models.NetworkFabric$Update` was modified

* `withTerminalServerConfiguration(models.NetworkFabricPatchablePropertiesTerminalServerConfiguration)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withIpv6Prefix(java.lang.String)` was removed
* `withManagementNetworkConfiguration(models.ManagementNetworkConfigurationPatchableProperties)` was removed
* `withServerCountPerRack(java.lang.Integer)` was removed
* `withFabricAsn(java.lang.Long)` was removed
* `withRackCount(java.lang.Integer)` was removed
* `withIpv4Prefix(java.lang.String)` was removed

#### `models.NetworkToNetworkInterconnect$Update` was modified

* `withIngressAclId(java.lang.String)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicyInformation)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicyInformation)` was removed
* `withOptionBLayer3Configuration(models.OptionBLayer3Configuration)` was removed
* `withNpbStaticRouteConfiguration(models.NpbStaticRouteConfiguration)` was removed
* `withLayer2Configuration(models.Layer2Configuration)` was removed
* `withEgressAclId(java.lang.String)` was removed

#### `models.NetworkDevicePatchParameters` was modified

* `validate()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withHostname(java.lang.String)` was removed
* `hostname()` was removed
* `annotation()` was removed
* `withTags(java.util.Map)` was removed
* `serialNumber()` was removed
* `withSerialNumber(java.lang.String)` was removed

#### `models.ManagementNetworkConfigurationProperties` was modified

* `validate()` was removed

#### `models.NetworkTap` was modified

* `networkPacketBrokerId()` was removed
* `destinations()` was removed
* `annotation()` was removed
* `provisioningState()` was removed
* `pollingType()` was removed
* `configurationState()` was removed
* `administrativeState()` was removed
* `sourceTapRuleId()` was removed

#### `models.ExportRoutePolicy` was modified

* `validate()` was removed

#### `models.NetworkToNetworkInterconnects` was modified

* `updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState)` was removed
* `updateNpbStaticRouteBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.UpdateAdministrativeState,com.azure.core.util.Context)` was removed

#### `models.NeighborGroup` was modified

* `provisioningState()` was removed
* `networkTapRuleIds()` was removed
* `destination()` was removed
* `networkTapIds()` was removed
* `annotation()` was removed

#### `models.RoutePolicy$Update` was modified

* `withStatements(java.util.List)` was removed
* `withDefaultAction(models.CommunityActionTypes)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.IpExtendedCommunityIdList` was modified

* `validate()` was removed

#### `models.ExternalNetwork$Definition` was modified

* `withNetworkToNetworkInterconnectId(java.lang.String)` was removed
* `withExportRoutePolicyId(java.lang.String)` was removed
* `withOptionAProperties(models.ExternalNetworkPropertiesOptionAProperties)` was removed
* `withPeeringOption(models.PeeringOption)` was removed
* `withAnnotation(java.lang.String)` was removed
* `withImportRoutePolicyId(java.lang.String)` was removed
* `withImportRoutePolicy(models.ImportRoutePolicy)` was removed
* `withOptionBProperties(models.L3OptionBProperties)` was removed
* `withExportRoutePolicy(models.ExportRoutePolicy)` was removed

#### `models.NeighborGroupPatch` was modified

* `withDestination(models.NeighborGroupDestination)` was removed
* `validate()` was removed
* `withTags(java.util.Map)` was removed
* `annotation()` was removed
* `withAnnotation(java.lang.String)` was removed
* `destination()` was removed

#### `models.Layer2Configuration` was modified

* `validate()` was removed

#### `models.L2IsolationDomainPatch` was modified

* `withAnnotation(java.lang.String)` was removed
* `annotation()` was removed
* `mtu()` was removed
* `withTags(java.util.Map)` was removed
* `validate()` was removed
* `withMtu(java.lang.Integer)` was removed

#### `models.RouteTargetInformation` was modified

* `validate()` was removed

#### `models.ExpressRouteConnectionInformation` was modified

* `validate()` was removed

#### `models.InternetGatewayRule` was modified

* `internetGatewayIds()` was removed
* `provisioningState()` was removed
* `ruleProperties()` was removed
* `annotation()` was removed

#### `models.BfdConfiguration` was modified

* `validate()` was removed

#### `models.ImportRoutePolicy` was modified

* `validate()` was removed

#### `models.NetworkPacketBroker` was modified

* `networkDeviceIds()` was removed
* `provisioningState()` was removed
* `sourceInterfaceIds()` was removed
* `neighborGroupIds()` was removed
* `networkTapIds()` was removed
* `networkFabricId()` was removed

#### `models.IpExtendedCommunityRule` was modified

* `validate()` was removed

#### `models.ExternalNetworkPatchPropertiesOptionAProperties` was modified

* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withPeerAsn(java.lang.Long)` was removed
* `peerAsn()` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `fabricAsn()` was removed
* `models.BfdConfiguration bfdConfiguration()` -> `models.BfdPatchConfiguration bfdConfiguration()`
* `validate()` was removed
* `withBfdConfiguration(models.BfdConfiguration)` was removed

#### `models.AccessControlListAction` was modified

* `validate()` was removed

#### `models.SupportedConnectorProperties` was modified

* `SupportedConnectorProperties()` was changed to private access
* `withConnectorType(java.lang.String)` was removed
* `withMaxSpeedInMbps(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.NetworkTapRulePatch` was modified

* `withConfigurationType(models.ConfigurationType)` was removed
* `matchConfigurations()` was removed
* `withMatchConfigurations(java.util.List)` was removed
* `withTags(java.util.Map)` was removed
* `dynamicMatchConfigurations()` was removed
* `withAnnotation(java.lang.String)` was removed
* `validate()` was removed
* `withTapRulesUrl(java.lang.String)` was removed
* `configurationType()` was removed
* `tapRulesUrl()` was removed
* `annotation()` was removed
* `withDynamicMatchConfigurations(java.util.List)` was removed

#### `models.NetworkTapPatch` was modified

* `validate()` was removed
* `withAnnotation(java.lang.String)` was removed
* `withPollingType(models.PollingType)` was removed
* `annotation()` was removed
* `pollingType()` was removed
* `withDestinations(java.util.List)` was removed
* `withTags(java.util.Map)` was removed
* `destinations()` was removed

#### `models.AggregateRoute` was modified

* `validate()` was removed

#### `models.StatementConditionProperties` was modified

* `withIpCommunityIds(java.util.List)` was removed
* `validate()` was removed

#### `models.InternetGatewayPatch` was modified

* `internetGatewayRuleId()` was removed
* `validate()` was removed
* `withInternetGatewayRuleId(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.OptionBLayer3Configuration` was modified

* `fabricAsn()` was removed
* `withSecondaryIpv6Prefix(java.lang.String)` was removed
* `peerAsn()` was removed
* `java.lang.Integer vlanId()` -> `int vlanId()`
* `withPeerAsn(java.lang.Long)` was removed
* `withPrimaryIpv4Prefix(java.lang.String)` was removed
* `withSecondaryIpv4Prefix(java.lang.String)` was removed
* `validate()` was removed
* `withPrimaryIpv6Prefix(java.lang.String)` was removed
* `withVlanId(java.lang.Integer)` was removed

#### `models.IpMatchCondition` was modified

* `validate()` was removed

### Features Added

* `models.IpCommunityPatchableProperties` was added

* `models.NetworkFabricProperties` was added

* `models.HeaderAddressProperties` was added

* `models.ExternalNetworkPatchProperties` was added

* `models.RouteType` was added

* `models.VpnOptionBProperties` was added

* `models.ExternalNetworkProperties` was added

* `models.InternalNetworkBmpProperties` was added

* `models.NetworkMonitor$Definition` was added

* `models.NeighborGroupPatchProperties` was added

* `models.ExternalNetworkBmpProperties` was added

* `models.L3OptionBPatchProperties` was added

* `models.NeighborGroupDestinationPatch` was added

* `models.VlanGroupPatchProperties` was added

* `models.IpExtendedCommunityProperties` was added

* `models.BgpAdministrativeState` was added

* `models.StatementActionPatchProperties` was added

* `models.ManagedServiceIdentity` was added

* `models.NetworkDeviceSkuProperties` was added

* `models.IcmpConfigurationPatchProperties` was added

* `models.VlanMatchConditionPatch` was added

* `models.BfdPatchConfiguration` was added

* `models.StaticRouteRoutePolicy` was added

* `models.NetworkDevicePatchParametersProperties` was added

* `models.IcmpConfigurationProperties` was added

* `models.InternalNetworkBgpAdministrativeStateRequest` was added

* `models.NniStaticRouteConfiguration` was added

* `models.VpnOptionBPatchProperties` was added

* `models.NetworkToNetworkInterconnectProperties` was added

* `models.ExternalNetworkStaticRouteConfiguration` was added

* `models.ImportRoutePolicyInformationPatch` was added

* `models.NetworkFabricLockAction` was added

* `models.GlobalNetworkTapRuleActionPatchProperties` was added

* `models.L3IsolationDomainProperties` was added

* `models.UniqueRouteDistinguisherPatchProperties` was added

* `models.ExternalNetworkBmpPatchProperties` was added

* `models.ConditionalDefaultRouteProperties` was added

* `models.ArmConfigurationDiffResponse` was added

* `models.BmpConfigurationPatchProperties` was added

* `models.NeighborGroupProperties` was added

* `models.ManagedServiceIdentityPatch` was added

* `models.Layer2ConfigurationPatch` was added

* `models.NetworkInterfacePatchProperties` was added

* `models.InternalNetworkBgpAdministrativeStateResponse` was added

* `models.NetworkFabricLockRequest` was added

* `models.BitRateUnit` was added

* `models.NetworkFabricPatchProperties` was added

* `models.BurstSizeUnit` was added

* `models.ExternalNetworkBfdAdministrativeStateResponse` was added

* `models.NetworkTapRuleProperties` was added

* `models.L3IsolationDomainPatchProperties` was added

* `models.NetworkMonitorPatchProperties` was added

* `models.DestinationPatchProperties` was added

* `models.ExternalNetworkStaticRoutePatchConfiguration` was added

* `models.NetworkTapRuleActionPatch` was added

* `models.StorageAccountConfiguration` was added

* `models.NetworkFabricLockType` was added

* `models.L2IsolationDomainProperties` was added

* `models.NetworkRackPatch` was added

* `models.DiscardCommitBatchResponse` was added

* `models.StaticRoutePatchConfiguration` was added

* `models.BmpMonitoredAddressFamily` was added

* `models.RoutePolicyProperties` was added

* `models.NetworkDeviceProperties` was added

* `models.NativeIpv6PrefixLimitPatchProperties` was added

* `models.OptionBLayer3PrefixLimitProperties` was added

* `models.StorageAccountPatchConfiguration` was added

* `models.NetworkMonitor` was added

* `models.AccessControlListProperties` was added

* `models.InternalNetworkRouteType` was added

* `models.StationConnectionMode` was added

* `models.StationConfigurationState` was added

* `models.CommonPostActionResponseForDeviceROCommands` was added

* `models.L3UniqueRouteDistinguisherProperties` was added

* `models.DeviceRole` was added

* `models.V4OverV6BgpSessionState` was added

* `models.RoutePrefixLimitPatchProperties` was added

* `models.BgpPatchConfiguration` was added

* `models.PrefixLimitProperties` was added

* `models.GlobalAccessControlListActionPatchProperties` was added

* `models.ExternalNetworkRouteType` was added

* `models.AccessControlListPatchProperties` was added

* `models.BurstSize` was added

* `models.GlobalAccessControlListActionProperties` was added

* `models.InternalNetworkPatchProperties` was added

* `models.BmpConfigurationState` was added

* `models.NativeIpv4PrefixLimitProperties` was added

* `models.NetworkMonitors` was added

* `models.InternetGatewayRuleProperties` was added

* `models.InternetGatewayPatchProperties` was added

* `models.StationConnectionProperties` was added

* `FilesharesManager` was added

* `models.IdentitySelectorPatch` was added

* `models.CommitBatchState` was added

* `FilesharesManager$Configurable` was added

* `models.ExternalNetworkBfdAdministrativeStateRequest` was added

* `models.AccessControlListMatchConfigurationPatch` was added

* `models.NetworkMonitorProperties` was added

* `models.ManagementNetworkPatchConfiguration` was added

* `models.IsolationDomainPatchProperties` was added

* `models.NetworkTapPatchProperties` was added

* `models.InternalNetworkBmpPatchProperties` was added

* `models.PrefixLimitPatchProperties` was added

* `models.RoutePolicyPatchableProperties` was added

* `models.AccessControlListPortConditionPatch` was added

* `models.ExtendedVlan` was added

* `models.ExportRoutePolicyPatch` was added

* `models.RuleCondition` was added

* `models.BmpConfigurationProperties` was added

* `models.CommonDynamicMatchConfigurationPatch` was added

* `models.NniStaticRoutePatchConfiguration` was added

* `models.NetworkPacketBrokerProperties` was added

* `models.IpPrefixPatchProperties` was added

* `models.ImportRoutePolicyPatch` was added

* `models.NetworkToNetworkInterconnectPatchProperties` was added

* `models.OptionBLayer3PrefixLimitPatchProperties` was added

* `models.NetworkTapRuleMatchConditionPatch` was added

* `models.StaticRoutePatchProperties` was added

* `models.NniBfdAdministrativeStateRequest` was added

* `models.OptionBLayer3ConfigurationPatchProperties` was added

* `models.NniBmpPatchProperties` was added

* `models.PortConditionPatch` was added

* `models.RouteTargetPatchInformation` was added

* `models.StationConnectionPatchProperties` was added

* `models.IpGroupPatchProperties` was added

* `models.IpCommunityProperties` was added

* `models.IpPrefixProperties` was added

* `models.NetworkRackProperties` was added

* `models.InternetGatewayProperties` was added

* `models.IpMatchConditionPatch` was added

* `models.FabricLockProperties` was added

* `models.DiscardCommitBatchRequest` was added

* `models.NetworkMonitorPatch` was added

* `models.VpnOptionAProperties` was added

* `models.InternalNetworkBfdAdministrativeStateResponse` was added

* `models.NativeIpv6PrefixLimitProperties` was added

* `models.RoutePolicyStatementPatchProperties` was added

* `models.ConnectedSubnetRoutePolicyPatch` was added

* `models.NeighborAddressBgpAdministrativeStatus` was added

* `models.BitRate` was added

* `models.StaticRouteRoutePolicyPatch` was added

* `models.StatementConditionPatchProperties` was added

* `models.NpbStaticRouteConfigurationPatch` was added

* `models.BmpExportPolicy` was added

* `models.CommonPostActionResponseForDeviceROCommandsOperationStatusResult` was added

* `models.AccessControlListMatchConditionPatch` was added

* `models.AccessControlListActionPatch` was added

* `models.IpExtendedCommunityPatchProperties` was added

* `models.UniqueRouteDistinguisherProperties` was added

* `models.UniqueRouteDistinguisherConfigurationState` was added

* `models.L2IsolationDomainPatchProperties` was added

* `models.InternalNetworkProperties` was added

* `models.NNIDerivedUniqueRouteDistinguisherConfigurationState` was added

* `models.NetworkTapProperties` was added

* `models.CommitBatchStatusResponse` was added

* `models.NetworkMonitor$DefinitionStages` was added

* `models.NniBfdAdministrativeStateResponse` was added

* `models.ViewDeviceConfigurationResponse` was added

* `models.VpnOptionAPatchProperties` was added

* `models.PortGroupPatchProperties` was added

* `models.LastOperationProperties` was added

* `models.CommonPostActionResponseForDeviceRWCommands` was added

* `models.CommitBatchStatusRequest` was added

* `models.ManagedServiceIdentityType` was added

* `models.LockConfigurationState` was added

* `models.DeviceRwCommand` was added

* `models.DeviceRoCommand` was added

* `models.V6OverV4BgpSessionState` was added

* `models.ActionIpExtendedCommunityPatchProperties` was added

* `models.MicroBfdState` was added

* `models.UserAssignedIdentity` was added

* `models.AclType` was added

* `models.IPAddressType` was added

* `models.RoutePrefixLimitProperties` was added

* `models.NativeIpv4PrefixLimitPatchProperties` was added

* `models.ActionIpCommunityPatchProperties` was added

* `models.L3ExportRoutePolicyPatch` was added

* `models.AggregateRoutePatchConfiguration` was added

* `models.PoliceRateConfigurationProperties` was added

* `models.NetworkFabricControllerPatchProperties` was added

* `models.NetworkMonitor$Update` was added

* `models.NeighborAddressBfdAdministrativeStatus` was added

* `models.NetworkFabricControllerProperties` was added

* `models.TerminalServerPatchConfiguration` was added

* `models.FeatureFlagProperties` was added

* `models.NniBmpProperties` was added

* `models.NetworkInterfaceProperties` was added

* `models.ExportRoutePolicyInformationPatch` was added

* `models.NetworkTapRulePatchProperties` was added

* `models.NetworkFabricSkuProperties` was added

* `models.ManagedServiceIdentitySelectorType` was added

* `models.GlobalNetworkTapRuleActionProperties` was added

* `models.NeighborAddressPatch` was added

* `models.IdentitySelector` was added

* `models.NetworkTapRuleMatchConfigurationPatch` was added

* `models.NetworkMonitor$UpdateStages` was added

* `models.InternalNetworkBfdAdministrativeStateRequest` was added

* `models.CommitBatchDetails` was added

* `models.ConnectedSubnetPatch` was added

#### `models.InternalNetwork$Definition` was modified

* `withProperties(models.InternalNetworkProperties)` was added

#### `models.IpPrefix$Update` was modified

* `withProperties(models.IpPrefixPatchProperties)` was added

#### `models.L3IsolationDomainPatch` was modified

* `properties()` was added
* `tags()` was added
* `withProperties(models.L3IsolationDomainPatchProperties)` was added

#### `models.IpGroupProperties` was modified

* `withIpAddressType(models.IPAddressType)` was added

#### `models.NetworkInterfacePatch` was modified

* `properties()` was added
* `withProperties(models.NetworkInterfacePatchProperties)` was added

#### `models.NetworkPacketBroker$Definition` was modified

* `withProperties(models.NetworkPacketBrokerProperties)` was added

#### `models.TerminalServerConfiguration` was modified

* `password()` was added
* `username()` was added
* `serialNumber()` was added

#### `models.NetworkTapRuleMatchCondition` was modified

* `vlanMatchCondition()` was added
* `ipCondition()` was added
* `protocolTypes()` was added

#### `models.NetworkDevice$Update` was modified

* `withProperties(models.NetworkDevicePatchParametersProperties)` was added

#### `models.VpnConfigurationPatchableProperties` was modified

* `withOptionAProperties(models.VpnOptionAPatchProperties)` was added
* `withOptionBProperties(models.VpnOptionBPatchProperties)` was added

#### `models.ExternalNetworkPatch` was modified

* `withProperties(models.ExternalNetworkPatchProperties)` was added
* `properties()` was added

#### `models.ActionIpCommunityProperties` was modified

* `add()` was added

#### `models.IpExtendedCommunity` was modified

* `properties()` was added

#### `models.IpPrefixPatch` was modified

* `tags()` was added
* `properties()` was added
* `withProperties(models.IpPrefixPatchProperties)` was added

#### `models.NetworkInterface` was modified

* `properties()` was added

#### `models.NetworkInterface$Update` was modified

* `withProperties(models.NetworkInterfacePatchProperties)` was added

#### `models.InternetGatewayRulePatch` was modified

* `tags()` was added

#### `models.ExternalNetworks` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.ExternalNetworkBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.ExternalNetworkBfdAdministrativeStateRequest)` was added

#### `models.ExternalNetwork$Update` was modified

* `withProperties(models.ExternalNetworkPatchProperties)` was added

#### `models.L3IsolationDomain` was modified

* `properties()` was added

#### `models.NetworkFabricController$Update` was modified

* `withProperties(models.NetworkFabricControllerPatchProperties)` was added

#### `models.NeighborGroup$Definition` was modified

* `withProperties(models.NeighborGroupProperties)` was added

#### `models.NeighborAddress` was modified

* `bgpAdministrativeState()` was added
* `bfdAdministrativeState()` was added

#### `models.AccessControlList$Update` was modified

* `withProperties(models.AccessControlListPatchProperties)` was added

#### `models.NetworkDeviceSku` was modified

* `properties()` was added

#### `models.InternalNetworks` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkBgpAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkBfdAdministrativeStateRequest)` was added
* `updateBgpAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.InternalNetworkBgpAdministrativeStateRequest)` was added

#### `models.ExternalNetworkPropertiesOptionAProperties` was modified

* `nativeIpv6PrefixLimit()` was added
* `secondaryIpv6Prefix()` was added
* `peerASN()` was added
* `withPeerASN(long)` was added
* `withVlanId(int)` was added
* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `v6OverV4BgpSession()` was added
* `secondaryIpv4Prefix()` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `v4OverV6BgpSession()` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitProperties)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitProperties)` was added
* `nativeIpv4PrefixLimit()` was added
* `primaryIpv6Prefix()` was added
* `bmpConfiguration()` was added
* `fabricASN()` was added
* `primaryIpv4Prefix()` was added
* `withBmpConfiguration(models.ExternalNetworkBmpProperties)` was added

#### `models.AccessControlListMatchCondition` was modified

* `vlanMatchCondition()` was added
* `ipCondition()` was added
* `withIcmpConfiguration(models.IcmpConfigurationProperties)` was added
* `protocolNeighbors()` was added
* `icmpConfiguration()` was added
* `withProtocolNeighbors(java.util.List)` was added
* `protocolTypes()` was added

#### `models.InternalNetwork$Update` was modified

* `withProperties(models.InternalNetworkPatchProperties)` was added

#### `models.NetworkDevices` was modified

* `runRwCommand(java.lang.String,java.lang.String,models.DeviceRwCommand)` was added
* `runRoCommand(java.lang.String,java.lang.String,models.DeviceRoCommand,com.azure.core.util.Context)` was added
* `runRwCommand(java.lang.String,java.lang.String,models.DeviceRwCommand,com.azure.core.util.Context)` was added
* `runRoCommand(java.lang.String,java.lang.String,models.DeviceRoCommand)` was added

#### `models.NetworkTapRule$Definition` was modified

* `withProperties(models.NetworkTapRuleProperties)` was added

#### `models.InternetGatewayRule$Definition` was modified

* `withProperties(models.InternetGatewayRuleProperties)` was added

#### `models.NetworkTapRule$Update` was modified

* `withProperties(models.NetworkTapRulePatchProperties)` was added

#### `models.UpgradeNetworkFabricProperties` was modified

* `version()` was added

#### `models.L2IsolationDomain$Definition` was modified

* `withProperties(models.L2IsolationDomainProperties)` was added

#### `models.IpCommunity$Update` was modified

* `withProperties(models.IpCommunityPatchableProperties)` was added

#### `models.InternetGateway$Definition` was modified

* `withProperties(models.InternetGatewayProperties)` was added

#### `models.RuleProperties` was modified

* `withDestinationAddressList(java.util.List)` was added
* `sourceAddressList()` was added
* `withSourceAddressList(java.util.List)` was added
* `condition()` was added
* `destinationAddressList()` was added
* `withCondition(models.RuleCondition)` was added
* `headerAddressList()` was added
* `withHeaderAddressList(java.util.List)` was added

#### `models.InternalNetworkPatch` was modified

* `properties()` was added
* `withProperties(models.InternalNetworkPatchProperties)` was added

#### `models.RoutePolicyPatch` was modified

* `properties()` was added
* `tags()` was added
* `withProperties(models.RoutePolicyPatchableProperties)` was added

#### `models.AccessControlListPatch` was modified

* `tags()` was added
* `properties()` was added
* `withProperties(models.AccessControlListPatchProperties)` was added

#### `models.IpPrefix` was modified

* `properties()` was added

#### `models.InternetGateway` was modified

* `properties()` was added

#### `models.NetworkFabricController$Definition` was modified

* `withProperties(models.NetworkFabricControllerProperties)` was added

#### `models.AccessControlListMatchConfiguration` was modified

* `withIpAddressType(models.IPAddressType)` was added

#### `models.InternalNetwork` was modified

* `updateBfdAdministrativeState(models.InternalNetworkBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `updateBgpAdministrativeState(models.InternalNetworkBgpAdministrativeStateRequest)` was added
* `updateBfdAdministrativeState(models.InternalNetworkBfdAdministrativeStateRequest)` was added
* `updateBgpAdministrativeState(models.InternalNetworkBgpAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `properties()` was added

#### `models.IpExtendedCommunity$Definition` was modified

* `withProperties(models.IpExtendedCommunityProperties)` was added

#### `models.NetworkRack` was modified

* `properties()` was added

#### `models.NetworkDevice$Definition` was modified

* `withProperties(models.NetworkDeviceProperties)` was added

#### `models.InternetGateway$Update` was modified

* `withProperties(models.InternetGatewayPatchProperties)` was added

#### `models.NetworkFabric$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withProperties(models.NetworkFabricProperties)` was added

#### `models.NetworkFabrics` was modified

* `commitBatchStatus(java.lang.String,java.lang.String,models.CommitBatchStatusRequest)` was added
* `armConfigurationDiff(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `lockFabric(java.lang.String,java.lang.String,models.NetworkFabricLockRequest)` was added
* `discardCommitBatch(java.lang.String,java.lang.String,models.DiscardCommitBatchRequest)` was added
* `armConfigurationDiff(java.lang.String,java.lang.String)` was added
* `lockFabric(java.lang.String,java.lang.String,models.NetworkFabricLockRequest,com.azure.core.util.Context)` was added
* `commitBatchStatus(java.lang.String,java.lang.String,models.CommitBatchStatusRequest,com.azure.core.util.Context)` was added
* `viewDeviceConfiguration(java.lang.String,java.lang.String)` was added
* `discardCommitBatch(java.lang.String,java.lang.String,models.DiscardCommitBatchRequest,com.azure.core.util.Context)` was added
* `viewDeviceConfiguration(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NetworkDevice` was modified

* `runRwCommand(models.DeviceRwCommand,com.azure.core.util.Context)` was added
* `runRwCommand(models.DeviceRwCommand)` was added
* `runRoCommand(models.DeviceRoCommand,com.azure.core.util.Context)` was added
* `runRoCommand(models.DeviceRoCommand)` was added
* `properties()` was added

#### `models.NetworkFabricController` was modified

* `properties()` was added

#### `models.ActionIpExtendedCommunityProperties` was modified

* `add()` was added

#### `models.NetworkPacketBrokerPatch` was modified

* `tags()` was added

#### `models.IpCommunityPatch` was modified

* `properties()` was added
* `withProperties(models.IpCommunityPatchableProperties)` was added
* `tags()` was added

#### `models.NetworkInterface$Definition` was modified

* `withProperties(models.NetworkInterfaceProperties)` was added

#### `models.IpExtendedCommunity$Update` was modified

* `withProperties(models.IpExtendedCommunityPatchProperties)` was added

#### `models.UpdateAdministrativeState` was modified

* `resourceIds()` was added

#### `models.VpnConfigurationProperties` was modified

* `withOptionAProperties(models.VpnOptionAProperties)` was added
* `withOptionBProperties(models.VpnOptionBProperties)` was added

#### `models.RoutePolicy$Definition` was modified

* `withProperties(models.RoutePolicyProperties)` was added

#### `models.IpPrefix$Definition` was modified

* `withProperties(models.IpPrefixProperties)` was added

#### `models.UpdateDeviceAdministrativeState` was modified

* `resourceIds()` was added

#### `models.AccessControlList` was modified

* `properties()` was added

#### `models.ConnectedSubnet` was modified

* `annotation()` was added

#### `models.NetworkFabricControllerPatch` was modified

* `tags()` was added
* `withProperties(models.NetworkFabricControllerPatchProperties)` was added
* `properties()` was added

#### `models.StaticRouteConfiguration` was modified

* `extension()` was added
* `withExtension(models.Extension)` was added

#### `models.RoutePolicyStatementProperties` was modified

* `annotation()` was added

#### `models.NetworkTap$Update` was modified

* `withProperties(models.NetworkTapPatchProperties)` was added

#### `models.NetworkToNetworkInterconnect` was modified

* `updateBfdAdministrativeState(models.NniBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `properties()` was added
* `updateBfdAdministrativeState(models.NniBfdAdministrativeStateRequest)` was added

#### `models.NetworkTapRule` was modified

* `properties()` was added

#### `models.NetworkToNetworkInterconnectPatch` was modified

* `properties()` was added
* `withProperties(models.NetworkToNetworkInterconnectPatchProperties)` was added

#### `models.NetworkFabricSku` was modified

* `properties()` was added

#### `models.IpCommunity$Definition` was modified

* `withProperties(models.IpCommunityProperties)` was added

#### `models.L3IsolationDomain$Update` was modified

* `withProperties(models.L3IsolationDomainPatchProperties)` was added

#### `models.NetworkRack$Definition` was modified

* `withProperties(models.NetworkRackProperties)` was added

#### `models.IpCommunity` was modified

* `properties()` was added

#### `models.IpExtendedCommunityPatch` was modified

* `withProperties(models.IpExtendedCommunityPatchProperties)` was added
* `tags()` was added
* `properties()` was added

#### `models.RoutePolicy` was modified

* `properties()` was added

#### `models.NetworkToNetworkInterconnect$Definition` was modified

* `withProperties(models.NetworkToNetworkInterconnectProperties)` was added

#### `models.NetworkTap$Definition` was modified

* `withProperties(models.NetworkTapProperties)` was added

#### `models.BgpConfiguration` was modified

* `withBmpConfiguration(models.InternalNetworkBmpProperties)` was added
* `v4OverV6BgpSession()` was added
* `bmpConfiguration()` was added
* `v6OverV4BgpSession()` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `withPeerASN(long)` was added
* `annotation()` was added
* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `fabricASN()` was added
* `peerASN()` was added

#### `models.NetworkFabricPatch` was modified

* `withIdentity(models.ManagedServiceIdentityPatch)` was added
* `withProperties(models.NetworkFabricPatchProperties)` was added
* `identity()` was added
* `tags()` was added
* `properties()` was added

#### `models.AccessControlListPortCondition` was modified

* `portType()` was added
* `ports()` was added
* `portGroupNames()` was added
* `layer4Protocol()` was added

#### `models.NeighborGroup$Update` was modified

* `withProperties(models.NeighborGroupPatchProperties)` was added

#### `models.AccessControlList$Definition` was modified

* `withProperties(models.AccessControlListProperties)` was added

#### `models.L3IsolationDomain$Definition` was modified

* `withProperties(models.L3IsolationDomainProperties)` was added

#### `models.NetworkTapRuleMatchConfiguration` was modified

* `withIpAddressType(models.IPAddressType)` was added

#### `models.L2IsolationDomain$Update` was modified

* `withProperties(models.L2IsolationDomainPatchProperties)` was added

#### `models.PollingIntervalInSeconds` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L2IsolationDomain` was modified

* `properties()` was added

#### `models.NetworkFabric` was modified

* `viewDeviceConfiguration()` was added
* `properties()` was added
* `armConfigurationDiff(com.azure.core.util.Context)` was added
* `viewDeviceConfiguration(com.azure.core.util.Context)` was added
* `discardCommitBatch(models.DiscardCommitBatchRequest,com.azure.core.util.Context)` was added
* `commitBatchStatus(models.CommitBatchStatusRequest,com.azure.core.util.Context)` was added
* `identity()` was added
* `discardCommitBatch(models.DiscardCommitBatchRequest)` was added
* `commitBatchStatus(models.CommitBatchStatusRequest)` was added
* `lockFabric(models.NetworkFabricLockRequest,com.azure.core.util.Context)` was added
* `lockFabric(models.NetworkFabricLockRequest)` was added
* `armConfigurationDiff()` was added

#### `models.ExternalNetwork` was modified

* `updateBfdAdministrativeState(models.ExternalNetworkBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added
* `properties()` was added
* `updateBfdAdministrativeState(models.ExternalNetworkBfdAdministrativeStateRequest)` was added

#### `models.NetworkFabric$Update` was modified

* `withProperties(models.NetworkFabricPatchProperties)` was added
* `withIdentity(models.ManagedServiceIdentityPatch)` was added

#### `models.NetworkToNetworkInterconnect$Update` was modified

* `withProperties(models.NetworkToNetworkInterconnectPatchProperties)` was added

#### `models.NetworkDevicePatchParameters` was modified

* `withProperties(models.NetworkDevicePatchParametersProperties)` was added
* `properties()` was added
* `tags()` was added

#### `models.NetworkTap` was modified

* `properties()` was added

#### `models.NetworkToNetworkInterconnects` was modified

* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.NniBfdAdministrativeStateRequest)` was added
* `updateBfdAdministrativeState(java.lang.String,java.lang.String,java.lang.String,models.NniBfdAdministrativeStateRequest,com.azure.core.util.Context)` was added

#### `models.NeighborGroup` was modified

* `properties()` was added

#### `models.RoutePolicy$Update` was modified

* `withProperties(models.RoutePolicyPatchableProperties)` was added

#### `models.ExternalNetwork$Definition` was modified

* `withProperties(models.ExternalNetworkProperties)` was added

#### `models.NeighborGroupPatch` was modified

* `properties()` was added
* `tags()` was added
* `withProperties(models.NeighborGroupPatchProperties)` was added

#### `models.L2IsolationDomainPatch` was modified

* `tags()` was added
* `properties()` was added
* `withProperties(models.L2IsolationDomainPatchProperties)` was added

#### `models.InternetGatewayRule` was modified

* `properties()` was added

#### `models.NetworkPacketBroker` was modified

* `properties()` was added

#### `models.ExternalNetworkPatchPropertiesOptionAProperties` was modified

* `withBfdConfiguration(models.BfdPatchConfiguration)` was added
* `v4OverV6BgpSession()` was added
* `peerASN()` was added
* `bmpConfiguration()` was added
* `primaryIpv4Prefix()` was added
* `nativeIpv6PrefixLimit()` was added
* `primaryIpv6Prefix()` was added
* `fabricASN()` was added
* `v6OverV4BgpSession()` was added
* `withNativeIpv4PrefixLimit(models.NativeIpv4PrefixLimitPatchProperties)` was added
* `withBmpConfiguration(models.ExternalNetworkBmpPatchProperties)` was added
* `withNativeIpv6PrefixLimit(models.NativeIpv6PrefixLimitPatchProperties)` was added
* `secondaryIpv6Prefix()` was added
* `withPeerASN(java.lang.Long)` was added
* `withV4OverV6BgpSession(models.V4OverV6BgpSessionState)` was added
* `withV6OverV4BgpSession(models.V6OverV4BgpSessionState)` was added
* `secondaryIpv4Prefix()` was added
* `nativeIpv4PrefixLimit()` was added

#### `models.AccessControlListAction` was modified

* `policeRateConfiguration()` was added
* `withRemarkComment(java.lang.String)` was added
* `withPoliceRateConfiguration(models.PoliceRateConfigurationProperties)` was added
* `remarkComment()` was added

#### `models.NetworkTapRulePatch` was modified

* `properties()` was added
* `withProperties(models.NetworkTapRulePatchProperties)` was added
* `tags()` was added

#### `models.NetworkTapPatch` was modified

* `tags()` was added
* `withProperties(models.NetworkTapPatchProperties)` was added
* `properties()` was added

#### `models.StatementConditionProperties` was modified

* `ipCommunityIds()` was added

#### `models.InternetGatewayPatch` was modified

* `properties()` was added
* `withProperties(models.InternetGatewayPatchProperties)` was added
* `tags()` was added

#### `models.OptionBLayer3Configuration` was modified

* `withPeerASN(long)` was added
* `withVlanId(int)` was added
* `withBmpConfiguration(models.NniBmpProperties)` was added
* `prefixLimits()` was added
* `primaryIpv6Prefix()` was added
* `peLoopbackIpAddress()` was added
* `primaryIpv4Prefix()` was added
* `withPrefixLimits(java.util.List)` was added
* `bmpConfiguration()` was added
* `peerASN()` was added
* `withPeLoopbackIpAddress(java.util.List)` was added
* `secondaryIpv4Prefix()` was added
* `secondaryIpv6Prefix()` was added
* `fabricASN()` was added

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
