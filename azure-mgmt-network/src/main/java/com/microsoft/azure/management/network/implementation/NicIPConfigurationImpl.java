/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  Implementation for NicIPConfiguration and its create and update interfaces.
 */
@LangDefinition
class NicIPConfigurationImpl
        extends
        NicIPConfigurationBaseImpl<NetworkInterfaceImpl, NetworkInterface>
        implements
            NicIPConfiguration,
            NicIPConfiguration.Definition<NetworkInterface.DefinitionStages.WithCreate>,
            NicIPConfiguration.UpdateDefinition<NetworkInterface.Update>,
            NicIPConfiguration.Update {
    /**
     * the network client.
     */
    private final NetworkManager networkManager;
    /**
     * flag indicating whether IP configuration is in create or update mode.
     */
    private final boolean isInCreateMode;
    /**
     * unique key of a creatable virtual network to be associated with the ip configuration.
     */
    private String creatableVirtualNetworkKey;
    /**
     * unique key of a creatable public IP to be associated with the ip configuration.
     */
    private String creatablePublicIPKey;
    /**
     * reference to an existing virtual network to be associated with the ip configuration.
     */
    private Network existingVirtualNetworkToAssociate;
    /**
     * reference to an existing public IP to be associated with the ip configuration.
     */
    private String existingPublicIPAddressIdToAssociate;
    /**
     * name of an existing subnet to be associated with a new or existing ip configuration.
     */
    private String subnetToAssociate;
    /**
     * flag indicating to remove public IP association from the ip configuration during update.
     */
    private boolean removePrimaryPublicIPAssociation;

    protected NicIPConfigurationImpl(NetworkInterfaceIPConfigurationInner inner,
                                     NetworkInterfaceImpl parent,
                                     NetworkManager networkManager,
                                     final boolean isInCreateModel) {
        super(inner, parent, networkManager);
        this.isInCreateMode = isInCreateModel;
        this.networkManager = networkManager;
    }

    protected static NicIPConfigurationImpl prepareNicIPConfiguration(
            String name,
            NetworkInterfaceImpl parent,
            final NetworkManager networkManager) {
        NetworkInterfaceIPConfigurationInner ipConfigurationInner = new NetworkInterfaceIPConfigurationInner();
        ipConfigurationInner.withName(name);
        return new NicIPConfigurationImpl(ipConfigurationInner,
                parent,
                networkManager,
                true);
    }

    @Override
    public String publicIPAddressId() {
        if (this.inner().publicIPAddress() == null) {
            return null;
        }
        return this.inner().publicIPAddress().id();
    }

    @Override
    public PublicIPAddress getPublicIPAddress() {
        String id = publicIPAddressId();
        if (id == null) {
            return null;
        }

        return this.networkManager.publicIPAddresses().getById(id);
    }

    @Override
    public NetworkInterfaceImpl attach() {
        return parent().withIPConfiguration(this);
    }

    @Override
    public NicIPConfigurationImpl withNewNetwork(Creatable<Network> creatable) {
        this.creatableVirtualNetworkKey = creatable.key();
        this.parent().addToCreatableDependencies(creatable);
        return this;
    }

    @Override
    public NicIPConfigurationImpl withNewNetwork(String name, String addressSpaceCidr) {
        Network.DefinitionStages.WithGroup definitionWithGroup = this.networkManager.networks()
                .define(name)
                .withRegion(this.parent().regionName());

        Network.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.parent().newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.parent().newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.parent().resourceGroupName());
        }
        return withNewNetwork(definitionAfterGroup.withAddressSpace(addressSpaceCidr));
    }

    @Override
    public NicIPConfigurationImpl withNewNetwork(String addressSpaceCidr) {
        return withNewNetwork(this.parent().namer.randomName("vnet", 20), addressSpaceCidr);
    }

    @Override
    public NicIPConfigurationImpl withExistingNetwork(Network network) {
        this.existingVirtualNetworkToAssociate = network;
        return this;
    }

    @Override
    public NicIPConfigurationImpl withPrivateIPAddressDynamic() {
        this.inner().withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        this.inner().withPrivateIPAddress(null);
        return this;
    }

    @Override
    public NicIPConfigurationImpl withPrivateIPAddressStatic(String staticPrivateIPAddress) {
        this.inner().withPrivateIPAllocationMethod(IPAllocationMethod.STATIC);
        this.inner().withPrivateIPAddress(staticPrivateIPAddress);
        return this;
    }

    @Override
    public NicIPConfigurationImpl withNewPublicIPAddress(Creatable<PublicIPAddress> creatable) {
        if (this.creatablePublicIPKey == null) {
            this.creatablePublicIPKey = creatable.key();
            this.parent().addToCreatableDependencies(creatable);
        }
        return this;
    }

    @Override
    public NicIPConfigurationImpl withNewPublicIPAddress() {
        String name = this.parent().namer.randomName("pip", 15);
        return withNewPublicIPAddress(prepareCreatablePublicIP(name, name));
    }

    @Override
    public NicIPConfigurationImpl withNewPublicIPAddress(String leafDnsLabel) {
        return withNewPublicIPAddress(prepareCreatablePublicIP(this.parent().namer.randomName("pip", 15), leafDnsLabel));
    }

    @Override
    public NicIPConfigurationImpl withExistingPublicIPAddress(PublicIPAddress publicIPAddress) {
        return this.withExistingPublicIPAddress(publicIPAddress.id());
    }

    @Override
    public NicIPConfigurationImpl withExistingPublicIPAddress(String resourceId) {
        this.existingPublicIPAddressIdToAssociate = resourceId;
        return this;
    }

    @Override
    public NicIPConfigurationImpl withoutPublicIPAddress() {
        this.removePrimaryPublicIPAssociation = true;
        return this;
    }

    @Override
    public NicIPConfigurationImpl withSubnet(String name) {
        this.subnetToAssociate = name;
        return this;
    }

    @Override
    public NicIPConfigurationImpl withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName) {
        for (BackendAddressPoolInner pool : loadBalancer.inner().backendAddressPools()) {
            if (pool.name().equalsIgnoreCase(backendName)) {
                ensureBackendAddressPools().add(pool);
                return this;
            }
        }

        return null;
    }

    @Override
    public NicIPConfigurationImpl withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName) {
        for (InboundNatRuleInner rule : loadBalancer.inner().inboundNatRules()) {
            if (rule.name().equalsIgnoreCase(inboundNatRuleName)) {
                ensureInboundNatRules().add(rule);
                return this;
            }
        }

        return null;
    }

    private List<BackendAddressPoolInner> ensureBackendAddressPools() {
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

    protected static void ensureConfigurations(Collection<NicIPConfiguration> nicIPConfigurations) {
        for (NicIPConfiguration nicIPConfiguration : nicIPConfigurations) {
            NicIPConfigurationImpl config = (NicIPConfigurationImpl) nicIPConfiguration;
            config.inner().withSubnet(config.subnetToAssociate());
            config.inner().withPublicIPAddress(config.publicIPToAssociate());
        }
    }

    // Creates a creatable public IP address definition with the given name and DNS label.
    private Creatable<PublicIPAddress> prepareCreatablePublicIP(String name, String leafDnsLabel) {
        PublicIPAddress.DefinitionStages.WithGroup definitionWithGroup = this.networkManager.publicIPAddresses()
                    .define(name)
                    .withRegion(this.parent().regionName());

        PublicIPAddress.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.parent().newGroup() != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.parent().newGroup());
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.parent().resourceGroupName());
        }
        return definitionAfterGroup.withLeafDomainLabel(leafDnsLabel);
    }

    /**
     * Gets the subnet to associate with the IP configuration.
     * <p>
     * this method will never return null as subnet is required for a IP configuration, in case of
     * update mode if user didn't choose to change the subnet then existing subnet will be returned.
     * Updating the nic subnet has a restriction, the new subnet must reside in the same virtual network
     * as the current one.
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
                if (subnet.name().compareToIgnoreCase(this.subnetToAssociate) == 0) {
                    subnetInner.withId(subnet.id());
                    return subnetInner;
                }
            }

            throw new RuntimeException("A subnet with name '" + subnetToAssociate + "' not found under the network '" + this.existingVirtualNetworkToAssociate.name() + "'");

        } else {
            if (subnetToAssociate != null) {
                int idx = this.inner().subnet().id().lastIndexOf('/');
                subnetInner.withId(this.inner().subnet().id().substring(0, idx) + subnetToAssociate);
            } else {
                subnetInner.withId(this.inner().subnet().id());
            }
            return subnetInner;
        }
    }

    /**
     * Get the SubResource instance representing a public IP that needs to be associated with the
     * IP configuration.
     * <p>
     * null will be returned if withoutPublicIP() is specified in the update fluent chain or user did't
     * opt for public IP in create fluent chain. In case of update chain, if withoutPublicIP(..) is
     * not specified then existing associated (if any) public IP will be returned.
     * @return public ip SubResource
     */
    private SubResource publicIPToAssociate() {
        String pipId = null;
        if (this.removePrimaryPublicIPAssociation) {
            return null;
        } else if (this.creatablePublicIPKey != null) {
            pipId = ((PublicIPAddress) this.parent()
                    .createdDependencyResource(this.creatablePublicIPKey)).id();
        } else if (this.existingPublicIPAddressIdToAssociate != null) {
            pipId = this.existingPublicIPAddressIdToAssociate;
        }

        if (pipId != null) {
            return new SubResource().withId(pipId);
        } else if (!this.isInCreateMode) {
            return this.inner().publicIPAddress();
        } else {
            return null;
        }
    }

    @Override
    public NicIPConfigurationImpl withPrivateIPVersion(IPVersion ipVersion) {
        this.inner().withPrivateIPAddressVersion(ipVersion);
        return this;
    }

    @Override
    public NicIPConfigurationImpl withoutLoadBalancerBackends() {
        this.inner().withLoadBalancerBackendAddressPools(null);
        return this;
    }

    @Override
    public NicIPConfigurationImpl withoutLoadBalancerInboundNatRules() {
        this.inner().withLoadBalancerInboundNatRules(null);
        return this;
    }
}
