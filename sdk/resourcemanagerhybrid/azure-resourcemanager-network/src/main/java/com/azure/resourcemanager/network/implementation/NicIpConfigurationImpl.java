// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddressPool;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.fluent.models.BackendAddressPoolInner;
import com.azure.resourcemanager.network.fluent.models.InboundNatRuleInner;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.models.PublicIpAddressInner;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        if (this.innerModel().publicIpAddress() == null) {
            return null;
        }
        return this.innerModel().publicIpAddress().id();
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        return this.getPublicIpAddressAsync().block();
    }

    @Override
    public Mono<PublicIpAddress> getPublicIpAddressAsync() {
        String pipId = this.publicIpAddressId();
        return pipId == null ? Mono.empty() : this.networkManager.publicIpAddresses().getByIdAsync(pipId);
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
        return withNewNetwork(this.parent().namer.getRandomName("vnet", 20), addressSpaceCidr);
    }

    @Override
    public NicIpConfigurationImpl withExistingNetwork(Network network) {
        this.existingVirtualNetworkToAssociate = network;
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressDynamic() {
        this.innerModel().withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        this.innerModel().withPrivateIpAddress(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressStatic(String staticPrivateIPAddress) {
        this.innerModel().withPrivateIpAllocationMethod(IpAllocationMethod.STATIC);
        this.innerModel().withPrivateIpAddress(staticPrivateIPAddress);
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
        String name = this.parent().namer.getRandomName("pip", 15);
        return withNewPublicIpAddress(prepareCreatablePublicIP(name, name));
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress(String leafDnsLabel) {
        return withNewPublicIpAddress(
            prepareCreatablePublicIP(this.parent().namer.getRandomName("pip", 15), leafDnsLabel));
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
            for (BackendAddressPoolInner pool : loadBalancer.innerModel().backendAddressPools()) {
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
            for (ApplicationGatewayBackendAddressPool pool : appGateway.innerModel().backendAddressPools()) {
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
            for (InboundNatRuleInner rule : loadBalancer.innerModel().inboundNatRules()) {
                if (rule.name().equalsIgnoreCase(inboundNatRuleName)) {
                    ensureInboundNatRules().add(rule);
                    return this;
                }
            }
        }

        return null;
    }

    private List<ApplicationGatewayBackendAddressPool> ensureAppGatewayBackendAddressPools() {
        List<ApplicationGatewayBackendAddressPool> poolRefs = this.innerModel().applicationGatewayBackendAddressPools();
        if (poolRefs == null) {
            poolRefs = new ArrayList<>();
            this.innerModel().withApplicationGatewayBackendAddressPools(poolRefs);
        }
        return poolRefs;
    }

    private List<BackendAddressPoolInner> ensureLoadBalancerBackendAddressPools() {
        List<BackendAddressPoolInner> poolRefs = this.innerModel().loadBalancerBackendAddressPools();
        if (poolRefs == null) {
            poolRefs = new ArrayList<>();
            this.innerModel().withLoadBalancerBackendAddressPools(poolRefs);
        }
        return poolRefs;
    }

    private List<InboundNatRuleInner> ensureInboundNatRules() {
        List<InboundNatRuleInner> natRefs = this.innerModel().loadBalancerInboundNatRules();
        if (natRefs == null) {
            natRefs = new ArrayList<>();
            this.innerModel().withLoadBalancerInboundNatRules(natRefs);
        }
        return natRefs;
    }

    protected static void ensureConfigurations(Collection<NicIpConfiguration> nicIPConfigurations) {
        for (NicIpConfiguration nicIPConfiguration : nicIPConfigurations) {
            NicIpConfigurationImpl config = (NicIpConfigurationImpl) nicIPConfiguration;
            config.innerModel().withSubnet(config.subnetToAssociate());
            config.innerModel().withPublicIpAddress(config.publicIPToAssociate());
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
                subnetInner.withId(network.innerModel().subnets().get(0).id());
                return subnetInner;
            }

            for (SubnetInner subnet : this.existingVirtualNetworkToAssociate.innerModel().subnets()) {
                if (subnet.name().equalsIgnoreCase(this.subnetToAssociate)) {
                    subnetInner.withId(subnet.id());
                    return subnetInner;
                }
            }

            throw logger
                .logExceptionAsError(
                    new RuntimeException(
                        "A subnet with name '"
                            + subnetToAssociate
                            + "' not found under the network '"
                            + this.existingVirtualNetworkToAssociate.name()
                            + "'"));

        } else {
            if (subnetToAssociate != null) {
                int idx = this.innerModel().subnet().id().lastIndexOf('/');
                subnetInner.withId(this.innerModel().subnet().id().substring(0, idx + 1) + subnetToAssociate);
            } else {
                subnetInner.withId(this.innerModel().subnet().id());
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
            return this.innerModel().publicIpAddress();
        } else {
            return null;
        }
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpVersion(IpVersion ipVersion) {
        this.innerModel().withPrivateIpAddressVersion(ipVersion);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutApplicationGatewayBackends() {
        this.innerModel().withApplicationGatewayBackendAddressPools(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutLoadBalancerBackends() {
        this.innerModel().withLoadBalancerBackendAddressPools(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withoutLoadBalancerInboundNatRules() {
        this.innerModel().withLoadBalancerInboundNatRules(null);
        return this;
    }

    NicIpConfigurationImpl withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup) {
        this.withExistingApplicationSecurityGroup(applicationSecurityGroup.innerModel());
        return this;
    }

    NicIpConfigurationImpl withExistingApplicationSecurityGroup(ApplicationSecurityGroupInner inner) {
        if (this.innerModel().applicationSecurityGroups() == null) {
            this.innerModel().withApplicationSecurityGroups(new ArrayList<>());
        }
        this.innerModel().applicationSecurityGroups().add(inner);
        return this;
    }

    NicIpConfigurationImpl withoutApplicationSecurityGroup(String name) {
        if (this.innerModel().applicationSecurityGroups() != null) {
            this.innerModel().applicationSecurityGroups().removeIf(asg -> {
                String asgName = asg.name() == null
                    ? ResourceUtils.nameFromResourceId(asg.id())
                    : asg.name();
                return Objects.equals(name, asgName);
            });
        }
        return this;
    }
}
