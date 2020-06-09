// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddressPool;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.fluent.inner.BackendAddressPoolInner;
import com.azure.resourcemanager.network.fluent.inner.InboundNatRuleInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.inner.PublicIpAddressInner;
import com.azure.resourcemanager.network.fluent.inner.SubnetInner;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Implementation for NicIPConfiguration and its create and update interfaces. */
class NicIpConfigurationImpl extends NicIpConfigurationBaseImpl<NetworkInterfaceImpl, NetworkInterface>
    implements NicIpConfiguration,
        NicIpConfiguration.Definition<NetworkInterface.DefinitionStages.WithCreate>,
        NicIpConfiguration.UpdateDefinition<NetworkInterface.Update>,
        NicIpConfiguration.Update {
    /** the network client. */
    private final NetworkManager networkManager;
    /** flag indicating whether IP configuration is in create or update mode. */
    private final boolean isInCreateMode;
    /** unique key of a creatable virtual network to be associated with the ip configuration. */
    private String creatableVirtualNetworkKey;
    /** unique key of a creatable public IP to be associated with the ip configuration. */
    private String creatablePublicIPKey;
    /** reference to an existing virtual network to be associated with the ip configuration. */
    private Network existingVirtualNetworkToAssociate;
    /** reference to an existing public IP to be associated with the ip configuration. */
    private String existingPublicIPAddressIdToAssociate;
    /** name of an existing subnet to be associated with a new or existing IP configuration. */
    private String subnetToAssociate;
    /** flag indicating to remove public IP association from the ip configuration during update. */
    private boolean removePrimaryPublicIPAssociation;

    private final ClientLogger logger = new ClientLogger(getClass());

    protected NicIpConfigurationImpl(
        NetworkInterfaceIpConfigurationInner inner,
        NetworkInterfaceImpl parent,
        NetworkManager networkManager,
        final boolean isInCreateModel) {
        super(inner, parent, networkManager);
        this.isInCreateMode = isInCreateModel;
        this.networkManager = networkManager;
    }

    protected static NicIpConfigurationImpl prepareNicIPConfiguration(
        String name, NetworkInterfaceImpl parent, final NetworkManager networkManager) {
        NetworkInterfaceIpConfigurationInner ipConfigurationInner = new NetworkInterfaceIpConfigurationInner();
        ipConfigurationInner.withName(name);
        return new NicIpConfigurationImpl(ipConfigurationInner, parent, networkManager, true);
    }

    @Override
    public String publicIpAddressId() {
        if (this.inner().publicIpAddress() == null) {
            return null;
        }
        return this.inner().publicIpAddress().id();
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        String id = publicIpAddressId();
        if (id == null) {
            return null;
        }

        return this.networkManager.publicIpAddresses().getById(id);
    }

    @Override
    public NetworkInterfaceImpl attach() {
        return parent().withIPConfiguration(this);
    }

    @Override
    public NicIpConfigurationImpl withNewNetwork(Creatable<Network> creatable) {
        this.creatableVirtualNetworkKey = creatable.key();
        this.parent().addToCreatableDependencies(creatable);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewNetwork(String name, String addressSpaceCidr) {
        Network.DefinitionStages.WithGroup definitionWithGroup =
            this.networkManager.networks().define(name).withRegion(this.parent().regionName());

        Network.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.parent().newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.parent().newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.parent().resourceGroupName());
        }
        return withNewNetwork(definitionAfterGroup.withAddressSpace(addressSpaceCidr));
    }

    @Override
    public NicIpConfigurationImpl withNewNetwork(String addressSpaceCidr) {
        return withNewNetwork(this.parent().namer.randomName("vnet", 20), addressSpaceCidr);
    }

    @Override
    public NicIpConfigurationImpl withExistingNetwork(Network network) {
        this.existingVirtualNetworkToAssociate = network;
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressDynamic() {
        this.inner().withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        this.inner().withPrivateIpAddress(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressStatic(String staticPrivateIPAddress) {
        this.inner().withPrivateIpAllocationMethod(IpAllocationMethod.STATIC);
        this.inner().withPrivateIpAddress(staticPrivateIPAddress);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        if (this.creatablePublicIPKey == null) {
            this.creatablePublicIPKey = creatable.key();
            this.parent().addToCreatableDependencies(creatable);
        }
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress() {
        String name = this.parent().namer.randomName("pip", 15);
        return withNewPublicIpAddress(prepareCreatablePublicIP(name, name));
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress(String leafDnsLabel) {
        return withNewPublicIpAddress(
            prepareCreatablePublicIP(this.parent().namer.randomName("pip", 15), leafDnsLabel));
    }

    @Override
    public NicIpConfigurationImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    @Override
    public NicIpConfigurationImpl withExistingPublicIpAddress(String resourceId) {
        this.existingPublicIPAddressIdToAssociate = resourceId;
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutPublicIpAddress() {
        this.removePrimaryPublicIPAssociation = true;
        return this;
    }

    @Override
    public NicIpConfigurationImpl withSubnet(String name) {
        this.subnetToAssociate = name;
        return this;
    }

    @Override
    public NicIpConfigurationImpl withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName) {
        if (loadBalancer != null) {
            for (BackendAddressPoolInner pool : loadBalancer.inner().backendAddressPools()) {
                if (pool.name().equalsIgnoreCase(backendName)) {
                    ensureLoadBalancerBackendAddressPools().add(pool);
                    return this;
                }
            }
        }

        return null;
    }

    @Override
    public NicIpConfigurationImpl withExistingApplicationGatewayBackend(
        ApplicationGateway appGateway, String backendName) {
        if (appGateway != null) {
            for (ApplicationGatewayBackendAddressPool pool : appGateway.inner().backendAddressPools()) {
                if (pool.name().equalsIgnoreCase(backendName)) {
                    ensureAppGatewayBackendAddressPools().add(pool);
                    return this;
                }
            }
        }

        return null;
    }

    @Override
    public NicIpConfigurationImpl withExistingLoadBalancerInboundNatRule(
        LoadBalancer loadBalancer, String inboundNatRuleName) {
        if (loadBalancer != null) {
            for (InboundNatRuleInner rule : loadBalancer.inner().inboundNatRules()) {
                if (rule.name().equalsIgnoreCase(inboundNatRuleName)) {
                    ensureInboundNatRules().add(rule);
                    return this;
                }
            }
        }

        return null;
    }

    private List<ApplicationGatewayBackendAddressPool> ensureAppGatewayBackendAddressPools() {
        List<ApplicationGatewayBackendAddressPool> poolRefs = this.inner().applicationGatewayBackendAddressPools();
        if (poolRefs == null) {
            poolRefs = new ArrayList<>();
            this.inner().withApplicationGatewayBackendAddressPools(poolRefs);
        }
        return poolRefs;
    }

    private List<BackendAddressPoolInner> ensureLoadBalancerBackendAddressPools() {
        List<BackendAddressPoolInner> poolRefs = this.inner().loadBalancerBackendAddressPools();
        if (poolRefs == null) {
            poolRefs = new ArrayList<>();
            this.inner().withLoadBalancerBackendAddressPools(poolRefs);
        }
        return poolRefs;
    }

    private List<InboundNatRuleInner> ensureInboundNatRules() {
        List<InboundNatRuleInner> natRefs = this.inner().loadBalancerInboundNatRules();
        if (natRefs == null) {
            natRefs = new ArrayList<>();
            this.inner().withLoadBalancerInboundNatRules(natRefs);
        }
        return natRefs;
    }

    protected static void ensureConfigurations(Collection<NicIpConfiguration> nicIPConfigurations) {
        for (NicIpConfiguration nicIPConfiguration : nicIPConfigurations) {
            NicIpConfigurationImpl config = (NicIpConfigurationImpl) nicIPConfiguration;
            config.inner().withSubnet(config.subnetToAssociate());
            config.inner().withPublicIpAddress(config.publicIPToAssociate());
        }
    }

    // Creates a creatable public IP address definition with the given name and DNS label.
    private Creatable<PublicIpAddress> prepareCreatablePublicIP(String name, String leafDnsLabel) {
        PublicIpAddress.DefinitionStages.WithGroup definitionWithGroup =
            this.networkManager.publicIpAddresses().define(name).withRegion(this.parent().regionName());

        PublicIpAddress.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.parent().newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.parent().newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.parent().resourceGroupName());
        }
        return definitionAfterGroup.withLeafDomainLabel(leafDnsLabel);
    }

    /**
     * Gets the subnet to associate with the IP configuration.
     *
     * <p>This method will never return null as subnet is required for a IP configuration, in case of update mode if
     * user didn't choose to change the subnet then existing subnet will be returned. Updating the nic subnet has a
     * restriction, the new subnet must reside in the same virtual network as the current one.
     *
     * @return the subnet resource
     */
    private SubnetInner subnetToAssociate() {
        SubnetInner subnetInner = new SubnetInner();
        if (this.isInCreateMode) {
            if (this.creatableVirtualNetworkKey != null) {
                Network network = (Network) parent().createdDependencyResource(this.creatableVirtualNetworkKey);
                subnetInner.withId(network.inner().subnets().get(0).id());
                return subnetInner;
            }

            for (SubnetInner subnet : this.existingVirtualNetworkToAssociate.inner().subnets()) {
                if (subnet.name().equalsIgnoreCase(this.subnetToAssociate)) {
                    subnetInner.withId(subnet.id());
                    return subnetInner;
                }
            }

            throw logger.logExceptionAsError(new RuntimeException(
                "A subnet with name '"
                    + subnetToAssociate
                    + "' not found under the network '"
                    + this.existingVirtualNetworkToAssociate.name()
                    + "'"));

        } else {
            if (subnetToAssociate != null) {
                int idx = this.inner().subnet().id().lastIndexOf('/');
                subnetInner.withId(this.inner().subnet().id().substring(0, idx + 1) + subnetToAssociate);
            } else {
                subnetInner.withId(this.inner().subnet().id());
            }
            return subnetInner;
        }
    }

    /**
     * Get the SubResource instance representing a public IP that needs to be associated with the IP configuration.
     *
     * <p>null will be returned if withoutPublicIP() is specified in the update fluent chain or user did't opt for
     * public IP in create fluent chain. In case of update chain, if withoutPublicIP(..) is not specified then existing
     * associated (if any) public IP will be returned.
     *
     * @return public IP SubResource
     */
    private PublicIpAddressInner publicIPToAssociate() {
        String pipId = null;
        if (this.removePrimaryPublicIPAssociation) {
            return null;
        } else if (this.creatablePublicIPKey != null) {
            pipId = ((PublicIpAddress) this.parent().createdDependencyResource(this.creatablePublicIPKey)).id();
        } else if (this.existingPublicIPAddressIdToAssociate != null) {
            pipId = this.existingPublicIPAddressIdToAssociate;
        }

        if (pipId != null) {
            return new PublicIpAddressInner().withId(pipId);
        } else if (!this.isInCreateMode) {
            return this.inner().publicIpAddress();
        } else {
            return null;
        }
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpVersion(IpVersion ipVersion) {
        this.inner().withPrivateIpAddressVersion(ipVersion);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutApplicationGatewayBackends() {
        this.inner().withApplicationGatewayBackendAddressPools(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutLoadBalancerBackends() {
        this.inner().withLoadBalancerBackendAddressPools(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutLoadBalancerInboundNatRules() {
        this.inner().withLoadBalancerInboundNatRules(null);
        return this;
    }
}
