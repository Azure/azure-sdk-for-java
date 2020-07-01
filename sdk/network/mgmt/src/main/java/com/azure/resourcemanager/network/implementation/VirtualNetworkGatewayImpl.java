// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.BgpSettings;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnections;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayIpConfiguration;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySku;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuTier;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayType;
import com.azure.resourcemanager.network.models.VpnClientConfiguration;
import com.azure.resourcemanager.network.models.VpnClientParameters;
import com.azure.resourcemanager.network.models.VpnType;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayConnectionListEntityInner;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayInner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for VirtualNetworkGateway and its create and update interfaces. */
class VirtualNetworkGatewayImpl
    extends GroupableParentResourceWithTagsImpl<
        VirtualNetworkGateway, VirtualNetworkGatewayInner, VirtualNetworkGatewayImpl, NetworkManager>
    implements VirtualNetworkGateway, VirtualNetworkGateway.Definition, VirtualNetworkGateway.Update {
    private static final String GATEWAY_SUBNET = "GatewaySubnet";
    private final ClientLogger logger = new ClientLogger(getClass());

    private Map<String, VirtualNetworkGatewayIpConfiguration> ipConfigs;
    private VirtualNetworkGatewayConnections connections;
    private Creatable<Network> creatableNetwork;
    private Creatable<PublicIpAddress> creatablePip;

    VirtualNetworkGatewayImpl(
        String name, final VirtualNetworkGatewayInner innerModel, final NetworkManager networkManager) {
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
        VirtualNetworkGatewaySku sku =
            new VirtualNetworkGatewaySku()
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
        Network.DefinitionStages.WithGroup definitionWithGroup =
            this.manager().networks().define(name).withRegion(this.regionName());

        Network.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        Creatable<Network> network =
            definitionAfterGroup.withAddressSpace(addressSpace).withSubnet(GATEWAY_SUBNET, subnetAddressSpaceCidr);
        return withNewNetwork(network);
    }

    @Override
    public VirtualNetworkGatewayImpl withNewNetwork(String addressSpaceCidr, String subnetAddressSpaceCidr) {
        withNewNetwork(
            this.manager().sdkContext().randomResourceName("vnet", 8), addressSpaceCidr, subnetAddressSpaceCidr);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingNetwork(Network network) {
        ensureDefaultIPConfig().withExistingSubnet(network, GATEWAY_SUBNET);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingPublicIpAddress(PublicIpAddress publicIPAddress) {
        ensureDefaultIPConfig().withExistingPublicIpAddress(publicIPAddress);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withExistingPublicIpAddress(String resourceId) {
        ensureDefaultIPConfig().withExistingPublicIpAddress(resourceId);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        this.creatablePip = creatable;
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewPublicIpAddress() {
        final String pipName = this.manager().sdkContext().randomResourceName("pip", 9);
        this.creatablePip =
            this
                .manager()
                .publicIpAddresses()
                .define(pipName)
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
        return this
            .manager()
            .inner()
            .getVirtualNetworkGateways()
            .resetAsync(resourceGroupName(), name())
            .map(
                inner -> {
                    VirtualNetworkGatewayImpl.this.setInner(inner);
                    return Mono.empty();
                })
            .then();
    }

    @Override
    public PagedIterable<VirtualNetworkGatewayConnection> listConnections() {
        return new PagedIterable<>(this.listConnectionsAsync());
    }

    @Override
    public PagedFlux<VirtualNetworkGatewayConnection> listConnectionsAsync() {
        PagedFlux<VirtualNetworkGatewayConnectionListEntityInner> connectionInners =
            this.manager().inner().getVirtualNetworkGateways()
            .listConnectionsAsync(this.resourceGroupName(), this.name());
        return PagedConverter
            .flatMapPage(connectionInners, connectionInner -> connections().getByIdAsync(connectionInner.id()));
    }

    @Override
    public String generateVpnProfile() {
        return this
            .manager()
            .inner()
            .getVirtualNetworkGateways()
            .generateVpnProfile(resourceGroupName(), name(), new VpnClientParameters());
    }

    @Override
    public Mono<String> generateVpnProfileAsync() {
        return this
            .manager()
            .inner()
            .getVirtualNetworkGateways()
            .generateVpnProfileAsync(resourceGroupName(), name(), new VpnClientParameters());
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .inner()
            .getVirtualNetworkGateways()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags());
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
        return inner().gatewayDefaultSite() == null ? null : inner().gatewayDefaultSite().id();
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
    public Collection<VirtualNetworkGatewayIpConfiguration> ipConfigurations() {
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
        return super
            .refreshAsync()
            .map(
                virtualNetworkGateway -> {
                    VirtualNetworkGatewayImpl impl = (VirtualNetworkGatewayImpl) virtualNetworkGateway;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getVirtualNetworkGateways()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    VirtualNetworkGatewayImpl withConfig(VirtualNetworkGatewayIpConfigurationImpl config) {
        if (config != null) {
            this.ipConfigs.put(config.name(), config);
        }
        return this;
    }

    private VirtualNetworkGatewayIpConfigurationImpl defineIPConfiguration(String name) {
        VirtualNetworkGatewayIpConfiguration ipConfig = this.ipConfigs.get(name);
        if (ipConfig == null) {
            VirtualNetworkGatewayIpConfigurationInner inner =
                new VirtualNetworkGatewayIpConfigurationInner().withName(name);
            return new VirtualNetworkGatewayIpConfigurationImpl(inner, this);
        } else {
            return (VirtualNetworkGatewayIpConfigurationImpl) ipConfig;
        }
    }

    private void initializeIPConfigsFromInner() {
        this.ipConfigs = new TreeMap<>();
        List<VirtualNetworkGatewayIpConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners != null) {
            for (VirtualNetworkGatewayIpConfigurationInner inner : inners) {
                VirtualNetworkGatewayIpConfigurationImpl config =
                    new VirtualNetworkGatewayIpConfigurationImpl(inner, this);
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

    private VirtualNetworkGatewayIpConfigurationImpl ensureDefaultIPConfig() {
        VirtualNetworkGatewayIpConfigurationImpl ipConfig =
            (VirtualNetworkGatewayIpConfigurationImpl) defaultIPConfiguration();
        if (ipConfig == null) {
            String name = this.manager().sdkContext().randomResourceName("ipcfg", 11);
            ipConfig = this.defineIPConfiguration(name);
            ipConfig.attach();
        }
        return ipConfig;
    }

    private Creatable<PublicIpAddress> ensureDefaultPipDefinition() {
        if (this.creatablePip == null) {
            final String pipName = this.manager().sdkContext().randomResourceName("pip", 9);
            this.creatablePip =
                this
                    .manager()
                    .publicIpAddresses()
                    .define(pipName)
                    .withRegion(this.regionName())
                    .withExistingResourceGroup(this.resourceGroupName());
        }
        return this.creatablePip;
    }

    VirtualNetworkGatewayIpConfiguration defaultIPConfiguration() {
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
        final VirtualNetworkGatewayIpConfigurationImpl defaultIPConfig = ensureDefaultIPConfig();
        final Mono<Resource> pipObservable;
        if (defaultIPConfig.publicIpAddressId() == null) {
            // If public ip not specified, then create a default PIP
            pipObservable =
                Utils
                    .<PublicIpAddress>rootResource(ensureDefaultPipDefinition().createAsync().last())
                    .map(
                        publicIPAddress -> {
                            defaultIPConfig.withExistingPublicIpAddress(publicIPAddress);
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
        } else if (creatableNetwork != null) {
            // But if default IP config does not have a subnet specified, then create a VNet
            networkObservable =
                Utils
                    .<Network>rootResource(creatableNetwork.createAsync().last())
                    .map(
                        network -> {
                            // ... and assign the created VNet to the default IP config
                            defaultIPConfig.withExistingSubnet(network, GATEWAY_SUBNET);
                            return network;
                        });
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("Creatable Network should not be null"));
        }

        return Flux
            .merge(networkObservable, pipObservable)
            .last(Resource.DUMMY)
            .flatMap(
                resource ->
                    VirtualNetworkGatewayImpl
                        .this
                        .manager()
                        .inner()
                        .getVirtualNetworkGateways()
                        .createOrUpdateAsync(resourceGroupName(), name(), inner()));
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
