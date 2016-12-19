package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Implementation for {@link NicIpConfiguration} and its create and update interfaces.
 */
@LangDefinition()
class NicIpConfigurationImpl
        extends
            ChildResourceImpl<NetworkInterfaceIPConfigurationInner, NetworkInterfaceImpl, NetworkInterface>
        implements
            NicIpConfiguration,
            NicIpConfiguration.Definition<NetworkInterface.DefinitionStages.WithCreate>,
            NicIpConfiguration.UpdateDefinition<NetworkInterface.Update>,
            NicIpConfiguration.Update {
    // Clients
    private final NetworkManager networkManager;

    // Flag indicating whether IP configuration is in create or update mode
    private final boolean isInCreateMode;

    // Unique key of a creatable virtual network to be associated with the ip configuration
    private String creatableVirtualNetworkKey;

    // Unique key of a creatable public IP to be associated with the ip configuration
    private String creatablePublicIpKey;

    // Reference to an existing virtual network to be associated with the ip configuration
    private Network existingVirtualNetworkToAssociate;

    // Reference to an existing public IP to be associated with the ip configuration
    private String existingPublicIpAddressIdToAssociate;

    // Name of an existing subnet to be associated with a new or existing ip configuration
    private String subnetToAssociate;

    // Flag indicating to remove public IP association from the ip configuration during update
    private boolean removePrimaryPublicIPAssociation;

    protected NicIpConfigurationImpl(NetworkInterfaceIPConfigurationInner inner,
                                     NetworkInterfaceImpl parent,
                                     NetworkManager networkManager,
                                     final boolean isInCreateModel) {
        super(inner, parent);
        this.isInCreateMode = isInCreateModel;
        this.networkManager = networkManager;
    }

    protected static NicIpConfigurationImpl prepareNicIpConfiguration(
            String name,
            NetworkInterfaceImpl parent,
            final NetworkManager networkManager) {
        NetworkInterfaceIPConfigurationInner ipConfigurationInner = new NetworkInterfaceIPConfigurationInner();
        ipConfigurationInner.withName(name);
        return new NicIpConfigurationImpl(ipConfigurationInner,
                parent,
                networkManager,
                true);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public IPVersion privateIpAddressVersion() {
        return this.inner().privateIPAddressVersion();
    }

    @Override
    public String publicIpAddressId() {
        if (this.inner().publicIPAddress() == null) {
            return null;
        }
        return this.inner().publicIPAddress().id();
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
    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        return (subnetRef != null) ? ResourceUtils.parentResourceIdFromResourceId(subnetRef.id()) : null;
    }

    @Override
    public Network getNetwork() {
        String id = this.networkId();
        return (id != null) ? this.networkManager.networks().getById(id) : null;
    }

    @Override
    public String privateIpAddress() {
        return this.inner().privateIPAddress();
    }

    @Override
    public IPAllocationMethod privateIpAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
    }

    @Override
    public NetworkInterfaceImpl attach() {
        return parent().withIpConfiguration(this);
    }

    @Override
    public NicIpConfigurationImpl withNewNetwork(Creatable<Network> creatable) {
        this.creatableVirtualNetworkKey = creatable.key();
        this.parent().addToCreatableDependencies(creatable);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewNetwork(String name, String addressSpaceCidr) {
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
        this.inner().withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        this.inner().withPrivateIPAddress(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressStatic(String staticPrivateIpAddress) {
        this.inner().withPrivateIPAllocationMethod(IPAllocationMethod.STATIC);
        this.inner().withPrivateIPAddress(staticPrivateIpAddress);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        if (this.creatablePublicIpKey == null) {
            this.creatablePublicIpKey = creatable.key();
            this.parent().addToCreatableDependencies(creatable);
        }
        return this;
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress() {
        String name = this.parent().namer.randomName("pip", 15);
        return withNewPublicIpAddress(prepareCreatablePublicIp(name, name));
    }

    @Override
    public NicIpConfigurationImpl withNewPublicIpAddress(String leafDnsLabel) {
        return withNewPublicIpAddress(prepareCreatablePublicIp(this.parent().namer.randomName("pip", 15), leafDnsLabel));
    }

    @Override
    public NicIpConfigurationImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    @Override
    public NicIpConfigurationImpl withExistingPublicIpAddress(String resourceId) {
        this.existingPublicIpAddressIdToAssociate = resourceId;
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
        for (BackendAddressPoolInner pool : loadBalancer.inner().backendAddressPools()) {
            if (pool.name().equalsIgnoreCase(backendName)) {
                ensureBackendAddressPools().add(pool);
                return this;
            }
        }

        return null;
    }

    @Override
    public NicIpConfigurationImpl withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName) {
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

    protected static void ensureConfigurations(Collection<NicIpConfiguration> nicIpConfigurations) {
        for (NicIpConfiguration nicIpConfiguration : nicIpConfigurations) {
            NicIpConfigurationImpl config = (NicIpConfigurationImpl) nicIpConfiguration;
            config.inner().withSubnet(config.subnetToAssociate());
            config.inner().withPublicIPAddress(config.publicIpToAssociate());
        }
    }

    // Creates a creatable public IP address definition with the given name and DNS label.
    private Creatable<PublicIpAddress> prepareCreatablePublicIp(String name, String leafDnsLabel) {
        PublicIpAddress.DefinitionStages.WithGroup definitionWithGroup = this.networkManager.publicIpAddresses()
                    .define(name)
                    .withRegion(this.parent().regionName());

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
    private SubResource publicIpToAssociate() {
        String pipId = null;
        if (this.removePrimaryPublicIPAssociation) {
            return null;
        } else if (this.creatablePublicIpKey != null) {
            pipId = ((PublicIpAddress) this.parent()
                    .createdDependencyResource(this.creatablePublicIpKey)).id();
        } else if (this.existingPublicIpAddressIdToAssociate != null) {
            pipId = this.existingPublicIpAddressIdToAssociate;
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
    public NicIpConfigurationImpl withPrivateIpVersion(IPVersion ipVersion) {
        this.inner().withPrivateIPAddressVersion(ipVersion);
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

    @Override
    public List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules() {
        final List<InboundNatRuleInner> refs = this.inner().loadBalancerInboundNatRules();
        final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        final List<LoadBalancerInboundNatRule> rules = new ArrayList<>();

        if (refs != null) {
            for (InboundNatRuleInner ref : refs) {
                String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(ref.id());
                LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId);
                if (loadBalancer == null) {
                    loadBalancer = this.parent().manager().loadBalancers().getById(loadBalancerId);
                    loadBalancers.put(loadBalancerId, loadBalancer);
                }

                String ruleName = ResourceUtils.nameFromResourceId(ref.id());
                rules.add(loadBalancer.inboundNatRules().get(ruleName));
            }
        }

        return Collections.unmodifiableList(rules);
    }

    @Override
    public List<LoadBalancerBackend> listAssociatedLoadBalancerBackends() {
        final List<BackendAddressPoolInner> backendRefs = this.inner().loadBalancerBackendAddressPools();
        final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        final List<LoadBalancerBackend> backends = new ArrayList<>();

        if (backendRefs != null) {
            for (BackendAddressPoolInner backendRef : backendRefs) {
                String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(backendRef.id());
                LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId);
                if (loadBalancer == null) {
                    loadBalancer = this.parent().manager().loadBalancers().getById(loadBalancerId);
                    loadBalancers.put(loadBalancerId, loadBalancer);
                }

                String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
                backends.add(loadBalancer.backends().get(backendName));
            }
        }

        return Collections.unmodifiableList(backends);
    }
}
