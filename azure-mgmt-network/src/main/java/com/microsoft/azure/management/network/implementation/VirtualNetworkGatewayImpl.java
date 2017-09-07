/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.BgpSettings;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnections;
import com.microsoft.azure.management.network.VirtualNetworkGatewayIPConfiguration;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySku;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuName;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuTier;
import com.microsoft.azure.management.network.VirtualNetworkGatewayType;
import com.microsoft.azure.management.network.VpnClientConfiguration;
import com.microsoft.azure.management.network.VpnType;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.util.HashMap;
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
    private static final String DEFAULT = "GatewaySubnet";
    private Map<String, VirtualNetworkGatewayIPConfiguration> ipConfigs;
    private Map<String, String> creatablePipsByIPConfig;
    private VirtualNetworkGatewayConnections connections;

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
    public VirtualNetworkGatewayImpl withVPN() {
        inner().withGatewayType(VirtualNetworkGatewayType.VPN);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withRouteBased() {
        inner().withVpnType(VpnType.ROUTE_BASED);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withPolicyBased() {
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
        final String name = ensureDefaultIPConfig().name();
        this.creatablePipsByIPConfig.put(name, creatable.key());
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public VirtualNetworkGatewayImpl withNewPublicIPAddress() {
        ensureDefaultIPConfig();
        return this;
    }


    @Override
    public VirtualNetworkGatewayImpl withActiveActive(boolean activeActive) {
        this.inner().withActiveActive(activeActive);
        return this;
    }

    @Override
    public void reset() {

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
    public Boolean enableBgp() {
        return inner().enableBgp();
    }

    @Override
    public Boolean activeActive() {
        return inner().activeActive();
    }

    @Override
    public SubResource gatewayDefaultSite() {
        return inner().gatewayDefaultSite();
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

    @Override
    protected void initializeChildrenFromInner() {
        initializeIPConfigsFromInner();
        this.creatablePipsByIPConfig = new HashMap<>();
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
        // Process created PIPs
        for (Map.Entry<String, String> frontendPipPair : this.creatablePipsByIPConfig.entrySet()) {
            Resource createdPip = this.createdResource(frontendPipPair.getValue());
            ensureDefaultIPConfig().withExistingPublicIPAddress(createdPip.id());
        }
        this.creatablePipsByIPConfig.clear();
        // Reset and update IP configs
        ensureDefaultIPConfig();
        this.inner().withIpConfigurations(innersFromWrappers(this.ipConfigs.values()));


    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
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

    private Creatable<Network> ensureDefaultNetworkDefinition() {
        final String vnetName = SdkContext.randomResourceName("vnet", 10);
        Creatable<Network> creatableNetwork = this.manager().networks().define(vnetName)
                .withRegion(this.region())
                .withExistingResourceGroup(this.resourceGroupName())
                .withAddressSpace("10.0.0.0/24")
                .withSubnet(DEFAULT, "10.0.0.0/25")
                .withSubnet("apps", "10.0.0.128/25");
        return creatableNetwork;
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

    public VirtualNetworkGatewayIPConfiguration defaultIPConfiguration() {
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
        final VirtualNetworkGatewayIPConfigurationImpl defaultIPConfiguration = (VirtualNetworkGatewayIPConfigurationImpl) defaultIPConfiguration();
        final Observable<Resource> pipObservable;
        if (defaultIPConfiguration != null && defaultIPConfiguration.publicIPAddressId() == null) {
            // If public frontend requested but no PIP specified, then create a default PIP
            pipObservable = Utils.<PublicIPAddress>rootResource(ensureDefaultPipDefinition()
                    .createAsync()).map(new Func1<PublicIPAddress, Resource>() {
                @Override
                public Resource call(PublicIPAddress publicIPAddress) {
                    defaultIPConfiguration.withExistingPublicIPAddress(publicIPAddress);
                    return publicIPAddress;
                }
            });
        } else {
            // If no public frontend requested, skip creating the PIP
            pipObservable = Observable.empty();
        }

        // Determine if default VNet should be created
        final VirtualNetworkGatewayIPConfigurationImpl defaultIPConfig = ensureDefaultIPConfig();
        final Observable<Resource> networkObservable;
        if (defaultIPConfig.subnetName() != null) {
            // ...and no need to create a default VNet
            networkObservable = Observable.empty(); // ...and don't create another VNet
        } else {
            // But if default IP config does not have a subnet specified, then create a default VNet
            networkObservable = Utils.<Network>rootResource(ensureDefaultNetworkDefinition()
                    .createAsync()).map(new Func1<Network, Resource>() {
                @Override
                public Resource call(Network network) {
                    //... and assign the created VNet to the default IP config
                    defaultIPConfig.withExistingSubnet(network, DEFAULT);
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
