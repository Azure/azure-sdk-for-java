package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.io.IOException;
import java.util.List;

/**
 *  Implementation for {@link NicIpConfiguration} and its create and update interfaces.
 */
class NicIpConfigurationImpl
        extends
        ChildResourceImpl<NetworkInterfaceIPConfiguration, NetworkInterfaceImpl>
        implements
        NicIpConfiguration,
        NicIpConfiguration.Definition<NetworkInterface.DefinitionStages.WithCreate>,
        NicIpConfiguration.UpdateDefinition<NetworkInterface.Update>,
        NicIpConfiguration.Update {
    // Clients
    private final NetworkManager networkManager;
    // flag indicating whether IP configuration is in create or update mode
    private final boolean isInCreateMode;
    // unique key of a creatable virtual network to be associated with the ip configuration
    private String creatableVirtualNetworkKey;
    // unique key of a creatable public IP to be associated with the ip configuration
    private String creatablePublicIpKey;
    // reference to an existing virtual network to be associated with the ip configuration
    private Network existingVirtualNetworkToAssociate;
    // reference to an existing public IP to be associated with the ip configuration
    private PublicIpAddress existingPublicIpAddressToAssociate;
    // name of an existing subnet to be associated with a new or existing ip configuration
    private String subnetToAssociate;
    // flag indicating to remove public IP association from the ip configuration during update
    private boolean removePrimaryPublicIPAssociation;

    protected NicIpConfigurationImpl(String name,
                                     NetworkInterfaceIPConfiguration inner,
                                     NetworkInterfaceImpl parent,
                                     NetworkManager networkManager,
                                     final boolean isInCreateModel) {
        super(name, inner, parent);
        this.isInCreateMode = isInCreateModel;
        this.networkManager = networkManager;
    }

    protected static NicIpConfigurationImpl prepareNicIpConfiguration(String name,
                                                                      NetworkInterfaceImpl parent,
                                                                      final NetworkManager networkManager) {
        NetworkInterfaceIPConfiguration ipConfigurationInner = new NetworkInterfaceIPConfiguration();
        ipConfigurationInner.withName(name);
        return new NicIpConfigurationImpl(name,
                ipConfigurationInner,
                parent,
                networkManager,
                true);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String publicIpAddressId() {
        if (this.inner().publicIPAddress() == null) {
            return null;
        }
        return this.inner().publicIPAddress().id();
    }

    @Override
    public PublicIpAddress publicIpAddress() throws CloudException, IOException {
        String id = publicIpAddressId();
        if (id == null) {
            return null;
        }

        return this.networkManager.publicIpAddresses().getByGroup(
                ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public String subnetId() {
        return this.inner().subnet().id();
    }

    @Override
    public Network network() throws CloudException, IOException {
        String id = subnetId();
        return this.networkManager.networks().getByGroup(ResourceUtils.groupFromResourceId(id),
                ResourceUtils.extractFromResourceId(id, "virtualNetworks"));
    }

    @Override
    public String privateIp() {
        return this.inner().privateIPAddress();
    }

    @Override
    public String privateIpAllocationMethod() {
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
        this.inner().withPrivateIPAllocationMethod("Dynamic");
        this.inner().withPrivateIPAddress(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl withPrivateIpAddressStatic(String staticPrivateIpAddress) {
        this.inner().withPrivateIPAllocationMethod("Static");
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
        this.existingPublicIpAddressToAssociate = publicIpAddress;
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

    protected static void ensureConfigurations(List<NicIpConfiguration> nicIpConfigurations) {
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
        if (this.removePrimaryPublicIPAssociation) {
            return null;
        }

        PublicIPAddressInner publicIPAddressInner = null;
        if (this.creatablePublicIpKey != null) {
            PublicIpAddress publicIpAddress = (PublicIpAddress) this.parent()
                    .createdDependencyResource(this.creatablePublicIpKey);
            publicIPAddressInner = publicIpAddress.inner();
        }

        if (this.existingPublicIpAddressToAssociate != null) {
            publicIPAddressInner = this.existingPublicIpAddressToAssociate.inner();
        }

        if (publicIPAddressInner != null) {
            SubResource subResource = new SubResource();
            subResource.withId(publicIPAddressInner.id());
            return subResource;
        }

        if (!this.isInCreateMode) {
            return this.inner().publicIPAddress();
        }
        return null;
    }
}
