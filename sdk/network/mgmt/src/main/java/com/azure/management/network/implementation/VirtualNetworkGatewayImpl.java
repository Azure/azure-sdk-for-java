/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.BgpSettings;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewayConnection;
import com.azure.management.network.VirtualNetworkGatewayConnections;
import com.azure.management.network.VirtualNetworkGatewayIPConfiguration;
import com.azure.management.network.VirtualNetworkGatewaySku;
import com.azure.management.network.VirtualNetworkGatewaySkuName;
import com.azure.management.network.VirtualNetworkGatewaySkuTier;
import com.azure.management.network.VirtualNetworkGatewayType;
import com.azure.management.network.VpnClientConfiguration;
import com.azure.management.network.VpnClientParameters;
import com.azure.management.network.VpnType;
import com.azure.management.network.models.GroupableParentResourceWithTagsImpl;
import com.azure.management.network.models.VirtualNetworkGatewayConnectionListEntityInner;
import com.azure.management.network.models.VirtualNetworkGatewayIPConfigurationInner;
import com.azure.management.network.models.VirtualNetworkGatewayInner;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for VirtualNetworkGateway and its create and update interfaces.
 */
class VirtualNetworkGatewayImpl
        extends GroupableParentResourceWithTagsImpl<
        VirtualNetworkGateway,
        VirtualNetworkGatewayInner,
        VirtualNetworkGatewayImpl,
        NetworkManager>
        implements
        VirtualNetworkGateway,
        VirtualNetworkGateway.Definition,
        VirtualNetworkGateway.Update {
    private static final String GATEWAY_SUBNET = "GatewaySubnet";

    private Map<String, VirtualNetworkGatewayIPConfiguration> ipConfigs;
    private VirtualNetworkGatewayConnections connections;
    private Creatable<Network> creatableNetwork;
    private Creatable<PublicIPAddress> creatablePip;

    VirtualNetworkGatewayImpl(String name,
                              final VirtualNetworkGatewayInner innerModel,
                              final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    public VirtualNetworkGatewayImpl withExpressRoute() {
        inner().withGatewayType(VirtualNetworkGatewayType.EXPRESS_ROUTE);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withRouteBasedVpn() {
        inner().withGatewayType(VirtualNetworkGatewayType.VPN);
        inner().withVpnType(VpnType.ROUTE_BASED);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withPolicyBasedVpn() {
        inner().withGatewayType(VirtualNetworkGatewayType.VPN);
        inner().withVpnType(VpnType.POLICY_BASED);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withSku(VirtualNetworkGatewaySkuName skuName) {
        VirtualNetworkGatewaySku sku = new VirtualNetworkGatewaySku()
                .withName(skuName)
                // same sku tier as sku name
                .withTier(VirtualNetworkGatewaySkuTier.fromString(skuName.toString()));
        this.inner().withSku(sku);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewNetwork(Creatable<Network> creatable) {
        this.creatableNetwork = creatable;
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewNetwork(String name, String addressSpace, String subnetAddressSpaceCidr) {
        Network.DefinitionStages.WithGroup definitionWithGroup = this.manager().networks()
                .define(name)
                .withRegion(this.regionName());

        Network.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        Creatable<Network> network = definitionAfterGroup.withAddressSpace(addressSpace).withSubnet(GATEWAY_SUBNET, subnetAddressSpaceCidr);
        return withNewNetwork(network);
    }

    @Override
    public VirtualNetworkGatewayImpl withNewNetwork(String addressSpaceCidr, String subnetAddressSpaceCidr) {
        withNewNetwork(this.manager().getSdkContext().randomResourceName("vnet", 8), addressSpaceCidr, subnetAddressSpaceCidr);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingNetwork(Network network) {
        ensureDefaultIPConfig().withExistingSubnet(network, GATEWAY_SUBNET);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingPublicIPAddress(PublicIPAddress publicIPAddress) {
        ensureDefaultIPConfig().withExistingPublicIPAddress(publicIPAddress);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingPublicIPAddress(String resourceId) {
        ensureDefaultIPConfig().withExistingPublicIPAddress(resourceId);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewPublicIPAddress(Creatable<PublicIPAddress> creatable) {
        this.creatablePip = creatable;
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewPublicIPAddress() {
        final String pipName = this.manager().getSdkContext().randomResourceName("pip", 9);
        this.creatablePip = this.manager().publicIPAddresses().define(pipName)
                .withRegion(this.regionName())
                .withExistingResourceGroup(this.resourceGroupName());
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withoutBgp() {
        inner().withBgpSettings(null);
        inner().withEnableBgp(false);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withBgp(long asn, String bgpPeeringAddress) {
        inner().withEnableBgp(true);
        ensureBgpSettings().withAsn(asn).withBgpPeeringAddress(bgpPeeringAddress);
        return this;
    }

    void attachPointToSiteConfiguration(PointToSiteConfigurationImpl pointToSiteConfiguration) {
        inner().withVpnClientConfiguration(pointToSiteConfiguration.inner());
    }

    @Override
    public void reset() {
        resetAsync().block();
    }

    @Override
    public Mono<Void> resetAsync() {
        // FIXME: gatewayVip - parameter
        return this.manager().inner().virtualNetworkGateways().resetAsync(resourceGroupName(), name(), null).map(inner -> {
            VirtualNetworkGatewayImpl.this.setInner(inner);
            return Mono.empty();
        }).then();
    }

    @Override
    public PagedIterable<VirtualNetworkGatewayConnection> listConnections() {
        return new PagedIterable<>(this.listConnectionsAsync());
    }

    @Override
    public PagedFlux<VirtualNetworkGatewayConnection> listConnectionsAsync() {
        PagedFlux<VirtualNetworkGatewayConnectionListEntityInner> connectionInners = this.manager().inner().virtualNetworkGateways().listConnectionsAsync(this.resourceGroupName(), this.name());
        return PagedConverter.flatMapPage(connectionInners, connectionInner -> connections().getByIdAsync(connectionInner.getId()));
    }

    @Override
    public String generateVpnProfile() {
        return this.manager().inner().virtualNetworkGateways().generateVpnProfile(resourceGroupName(), name(), new VpnClientParameters());
    }

    @Override
    public Mono<String> generateVpnProfileAsync() {
        return this.manager().inner().virtualNetworkGateways().generateVpnProfileAsync(resourceGroupName(), name(), new VpnClientParameters());
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> applyTagsToInnerAsync() {
        return this.manager().inner().virtualNetworkGateways().updateTagsAsync(resourceGroupName(), name(), inner().getTags());
    }

    @Override
    public VirtualNetworkGatewayConnections connections() {
        if (connections == null) {
            connections = new VirtualNetworkGatewayConnectionsImpl(this);
        }
        return connections;
    }

    @Override
    public VirtualNetworkGatewayType gatewayType() {
        return inner().gatewayType();
    }

    @Override
    public VpnType vpnType() {
        return inner().vpnType();
    }

    @Override
    public boolean isBgpEnabled() {
        return Utils.toPrimitiveBoolean(inner().enableBgp());
    }

    @Override
    public boolean activeActive() {
        return Utils.toPrimitiveBoolean(inner().active());
    }

    @Override
    public String gatewayDefaultSiteResourceId() {
        return inner().gatewayDefaultSite() == null ? null : inner().gatewayDefaultSite().getId();
    }

    @Override
    public VirtualNetworkGatewaySku sku() {
        return this.inner().sku();
    }

    @Override
    public VpnClientConfiguration vpnClientConfiguration() {
        return inner().vpnClientConfiguration();
    }

    @Override
    public BgpSettings bgpSettings() {
        return inner().bgpSettings();
    }

    @Override
    public Collection<VirtualNetworkGatewayIPConfiguration> ipConfigurations() {
        return Collections.unmodifiableCollection(ipConfigs.values());
    }

    Creatable<ResourceGroup> newGroup() {
        return this.creatableGroup;
    }

    @Override
    protected void initializeChildrenFromInner() {
        initializeIPConfigsFromInner();
    }

    @Override
    public Mono<VirtualNetworkGateway> refreshAsync() {
        return super.refreshAsync().map(virtualNetworkGateway -> {
            VirtualNetworkGatewayImpl impl = (VirtualNetworkGatewayImpl) virtualNetworkGateway;
            impl.initializeChildrenFromInner();
            return impl;
        });
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> getInnerAsync() {
        return this.manager().inner().virtualNetworkGateways().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    VirtualNetworkGatewayImpl withConfig(VirtualNetworkGatewayIPConfigurationImpl config) {
        if (config != null) {
            this.ipConfigs.put(config.name(), config);
        }
        return this;
    }

    private VirtualNetworkGatewayIPConfigurationImpl defineIPConfiguration(String name) {
        VirtualNetworkGatewayIPConfiguration ipConfig = this.ipConfigs.get(name);
        if (ipConfig == null) {
            VirtualNetworkGatewayIPConfigurationInner inner = new VirtualNetworkGatewayIPConfigurationInner()
                    .withName(name);
            return new VirtualNetworkGatewayIPConfigurationImpl(inner, this);
        } else {
            return (VirtualNetworkGatewayIPConfigurationImpl) ipConfig;
        }
    }


    private void initializeIPConfigsFromInner() {
        this.ipConfigs = new TreeMap<>();
        List<VirtualNetworkGatewayIPConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners != null) {
            for (VirtualNetworkGatewayIPConfigurationInner inner : inners) {
                VirtualNetworkGatewayIPConfigurationImpl config = new VirtualNetworkGatewayIPConfigurationImpl(inner, this);
                this.ipConfigs.put(inner.name(), config);
            }
        }
    }

    @Override
    protected void beforeCreating() {
        // Reset and update IP configs
        ensureDefaultIPConfig();
        this.inner().withIpConfigurations(innersFromWrappers(this.ipConfigs.values()));
    }

    private BgpSettings ensureBgpSettings() {
        if (inner().bgpSettings() == null) {
            inner().withBgpSettings(new BgpSettings());
        }
        return inner().bgpSettings();
    }

    private VirtualNetworkGatewayIPConfigurationImpl ensureDefaultIPConfig() {
        VirtualNetworkGatewayIPConfigurationImpl ipConfig = (VirtualNetworkGatewayIPConfigurationImpl) defaultIPConfiguration();
        if (ipConfig == null) {
            String name = this.manager().getSdkContext().randomResourceName("ipcfg", 11);
            ipConfig = this.defineIPConfiguration(name);
            ipConfig.attach();
        }
        return ipConfig;
    }

    private Creatable<PublicIPAddress> ensureDefaultPipDefinition() {
        if (this.creatablePip == null) {
            final String pipName = this.manager().getSdkContext().randomResourceName("pip", 9);
            this.creatablePip = this.manager().publicIPAddresses().define(pipName)
                    .withRegion(this.regionName())
                    .withExistingResourceGroup(this.resourceGroupName());
        }
        return this.creatablePip;
    }

    VirtualNetworkGatewayIPConfiguration defaultIPConfiguration() {
        // Default means the only one
        if (this.ipConfigs.size() == 1) {
            return this.ipConfigs.values().iterator().next();
        } else {
            return null;
        }
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> createInner() {
        // Determine if a default public frontend PIP should be created
        final VirtualNetworkGatewayIPConfigurationImpl defaultIPConfig = ensureDefaultIPConfig();
        final Mono<Resource> pipObservable;
        if (defaultIPConfig != null && defaultIPConfig.publicIPAddressId() == null) {
            // If public ip not specified, then create a default PIP
            pipObservable = Utils.<PublicIPAddress>rootResource(ensureDefaultPipDefinition()
                    .createAsync().last()).map(publicIPAddress -> {
                defaultIPConfig.withExistingPublicIPAddress(publicIPAddress);
                return publicIPAddress;
            });
        } else {
            // If existing public ip address specified, skip creating the PIP
            pipObservable = Mono.empty();
        }

        final Mono<Resource> networkObservable;
        // Determine if default VNet should be created
        if (defaultIPConfig.subnetName() != null) {
            // ...and no need to create VNet
            networkObservable = Mono.empty(); // ...and don't create another VNet
        } else {
            // But if default IP config does not have a subnet specified, then create a VNet
            networkObservable = Utils.<Network>rootResource(creatableNetwork
                    .createAsync().last()).map(network -> {
                //... and assign the created VNet to the default IP config
                defaultIPConfig.withExistingSubnet(network, GATEWAY_SUBNET);
                return network;
            });
        }

        return Flux.merge(networkObservable, pipObservable)
                .last(Resource.DUMMY)
                .flatMap(resource -> VirtualNetworkGatewayImpl.this.manager().inner().virtualNetworkGateways().createOrUpdateAsync(resourceGroupName(), name(), inner()));
    }

    @Override
    public PointToSiteConfigurationImpl definePointToSiteConfiguration() {
        return new PointToSiteConfigurationImpl(new VpnClientConfiguration(), this);
    }

    @Override
    public PointToSiteConfigurationImpl updatePointToSiteConfiguration() {
        return new PointToSiteConfigurationImpl(inner().vpnClientConfiguration(), this);
    }
}
