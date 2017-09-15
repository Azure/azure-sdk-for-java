/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.BgpSettings;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnection;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnections;
import com.microsoft.azure.management.network.VirtualNetworkGatewayIPConfiguration;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySku;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuName;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuTier;
import com.microsoft.azure.management.network.VirtualNetworkGatewayType;
import com.microsoft.azure.management.network.VpnClientConfiguration;
import com.microsoft.azure.management.network.VpnType;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for VirtualNetworkGateway and its create and update interfaces.
 */
@LangDefinition
class VirtualNetworkGatewayImpl
        extends GroupableParentResourceImpl<
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
    private Creatable<PublicIPAddress> creatablePublicIPAddress;

    private final PagedListConverter<VirtualNetworkGatewayConnectionListEntityInner, VirtualNetworkGatewayConnection> connectionsConverter =
            new PagedListConverter<VirtualNetworkGatewayConnectionListEntityInner, VirtualNetworkGatewayConnection>() {
                @Override
                public VirtualNetworkGatewayConnection typeConvert(VirtualNetworkGatewayConnectionListEntityInner inner) {
                    return connections().getById(inner.id());
                }
            };


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
                .withTier(new VirtualNetworkGatewaySkuTier(skuName.toString()));
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
        withNewNetwork(SdkContext.randomResourceName("vnet", 8), addressSpaceCidr, subnetAddressSpaceCidr);
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
        final String pipName = SdkContext.randomResourceName("pip", 9);
        this.creatablePip = this.manager().publicIPAddresses().define(pipName)
                .withRegion(this.regionName())
                .withExistingResourceGroup(this.resourceGroupName());
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl disableBgp() {
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

    @Override
    public VirtualNetworkGatewayImpl withActiveActive(boolean activeActive) {
        this.inner().withActiveActive(activeActive);
        return this;
    }

    @Override
    public void reset() {
        resetAsync().await();
    }

    @Override
    public Completable resetAsync() {
        return this.manager().inner().virtualNetworkGateways().resetAsync(resourceGroupName(), name()).map(new Func1<VirtualNetworkGatewayInner, Void>() {
            @Override
            public Void call(VirtualNetworkGatewayInner inner) {
                VirtualNetworkGatewayImpl.this.setInner(inner);
                return null;
            }
        }).toCompletable();
    }

    @Override
    public PagedList<VirtualNetworkGatewayConnection> listConnections() {
        return wrapConnectionsList(this.manager().inner().virtualNetworkGateways().listConnections(this.resourceGroupName(), this.name()));
    }

    private PagedList<VirtualNetworkGatewayConnection> wrapConnectionsList(PagedList<VirtualNetworkGatewayConnectionListEntityInner> connectionListEntityInners) {
        return connectionsConverter.convert(connectionListEntityInners);
    }

    @Override
    public Observable<VirtualNetworkGatewayConnection> listConnectionsAsync() {
        return ReadableWrappersImpl.convertPageToInnerAsync(this.manager().inner().virtualNetworkGateways().listConnectionsAsync(this.resourceGroupName(), this.name()))
                .map(new Func1<VirtualNetworkGatewayConnectionListEntityInner, VirtualNetworkGatewayConnection>() {
                    @Override
                    public VirtualNetworkGatewayConnection call(VirtualNetworkGatewayConnectionListEntityInner connectionInner) {
                        // will re-query to get full information for the connection
                        return connections().getById(connectionInner.id());
                    }
                });
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
        return Utils.toPrimitiveBoolean(inner().activeActive());
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
    public List<VirtualNetworkGatewayIPConfigurationInner> ipConfigurations() {
        return null;
    }

    Creatable<ResourceGroup> newGroup() {
        return this.creatableGroup;
    }

    @Override
    protected void initializeChildrenFromInner() {
        initializeIPConfigsFromInner();
    }

    @Override
    public Observable<VirtualNetworkGateway> refreshAsync() {
        return super.refreshAsync().map(new Func1<VirtualNetworkGateway, VirtualNetworkGateway>() {
            @Override
            public VirtualNetworkGateway call(VirtualNetworkGateway virtualNetworkGateway) {
                VirtualNetworkGatewayImpl impl = (VirtualNetworkGatewayImpl) virtualNetworkGateway;
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<VirtualNetworkGatewayInner> getInnerAsync() {
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

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
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
            String name = SdkContext.randomResourceName("ipcfg", 11);
            ipConfig = this.defineIPConfiguration(name);
            ipConfig.attach();
        }
        return ipConfig;
    }

    private Creatable<PublicIPAddress> creatablePip = null;
    private Creatable<PublicIPAddress> ensureDefaultPipDefinition() {
        if (this.creatablePip == null) {
            final String pipName = SdkContext.randomResourceName("pip", 9);
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
    protected Observable<VirtualNetworkGatewayInner> createInner() {
        // Determine if a default public frontend PIP should be created
        final VirtualNetworkGatewayIPConfigurationImpl defaultIPConfig = ensureDefaultIPConfig();
        final Observable<Resource> pipObservable;
        if (defaultIPConfig != null && defaultIPConfig.publicIPAddressId() == null) {
            // If public ip not specified, then create a default PIP
            pipObservable = Utils.<PublicIPAddress>rootResource(ensureDefaultPipDefinition()
                    .createAsync()).map(new Func1<PublicIPAddress, Resource>() {
                @Override
                public Resource call(PublicIPAddress publicIPAddress) {
                    defaultIPConfig.withExistingPublicIPAddress(publicIPAddress);
                    return publicIPAddress;
                }
            });
        } else {
            // If existing public ip address specified, skip creating the PIP
            pipObservable = Observable.empty();
        }

        final Observable<Resource> networkObservable;
        // Determine if default VNet should be created
         if (defaultIPConfig.subnetName() != null) {
            // ...and no need to create VNet
            networkObservable = Observable.empty(); // ...and don't create another VNet
        } else {
            // But if default IP config does not have a subnet specified, then create a VNet
            networkObservable = Utils.<Network>rootResource(creatableNetwork
                    .createAsync()).map(new Func1<Network, Resource>() {
                @Override
                public Resource call(Network network) {
                    //... and assign the created VNet to the default IP config
                    defaultIPConfig.withExistingSubnet(network, GATEWAY_SUBNET);
                    return network;
                }
            });
        }

        return Observable.merge(networkObservable, pipObservable)
                .defaultIfEmpty(null)
                .last().flatMap(new Func1<Resource, Observable<VirtualNetworkGatewayInner>>() {
                    @Override
                    public Observable<VirtualNetworkGatewayInner> call(Resource resource) {
                        return VirtualNetworkGatewayImpl.this.manager().inner().virtualNetworkGateways().createOrUpdateAsync(resourceGroupName(), name(), inner());
                    }
                });
    }
}
