/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfacesInner;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type representing Azure network interface.
 */
class NetworkInterfaceImpl
        extends GroupableResourceImpl<NetworkInterface, NetworkInterfaceInner, NetworkInterfaceImpl>
        implements
        NetworkInterface,
        NetworkInterface.Definitions,
        NetworkInterface.Update {
    // Clients
    private final NetworkInterfacesInner client;
    private final Networks networks;
    private final PublicIpAddresses publicIpAddresses;
    private final String nicName;
    // used to generate unique name for any dependency resources
    private final String uniqueId;
    // unique key of a creatable virtual network to be associated with a new network interface
    private String creatableVirtualNetworkKey;
    // unique key of a creatable public IP to be associated with the network interface
    private String creatablePublicIpKey;
    // reference to an existing virtual network to be associated with a new network interface
    private Network existingVirtualNetworkToAssociate;
    // reference to an existing public IP to be associated with a new network interface
    private PublicIpAddress existingPublicIpAddressToAssociate;
    // name of an existing subnet to be associated with a new or existing network interface
    private String subnetToAssociate;
    // flag indicating to remove public IP association during update
    private Boolean removePublicIPAssociation;

    NetworkInterfaceImpl(String name,
                         NetworkInterfaceInner innerModel,
                         final NetworkInterfacesInner client,
                         final Networks networks,
                         final PublicIpAddresses publicIpAddresses,
                         final ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
        this.networks = networks;
        this.publicIpAddresses = publicIpAddresses;
        this.nicName = name;
        this.uniqueId = this.nicName + String.valueOf(System.currentTimeMillis() / 1000L);
    }

    /**************************************************.
     * Verbs
     **************************************************/

    @Override
    public NetworkInterface refresh() throws Exception {
        ServiceResponse<NetworkInterfaceInner> response =
                this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public NetworkInterfaceImpl create() throws Exception {
        this.creatablesCreate();
        return this;
    }

    @Override
    public NetworkInterfaceImpl update() throws Exception {
        return this;
    }

    @Override
    public NetworkInterfaceImpl apply() throws Exception {
        this.create();
        return this;
    }

    /**************************************************.
     * Setters
     **************************************************/

    @Override
    public NetworkInterfaceImpl withNewNetwork(Network.DefinitionCreatable creatable) {
        creatableVirtualNetworkKey = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewNetwork(String name, String addressSpaceCidr) {
        Network.DefinitionWithGroup definitionWithGroup = this.networks
            .define(name)
            .withRegion(this.region());

        Network.DefinitionCreatable definitionAfterGroup;
        if (this.newGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewGroup(this.newGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingGroup(this.resourceGroupName());
        }
        return withNewNetwork(definitionAfterGroup.withAddressSpace(addressSpaceCidr));
    }

    @Override
    public NetworkInterfaceImpl withNewNetwork(String addressSpaceCidr) {
        return withNewNetwork(nameWithPrefix("vnet"), addressSpaceCidr);
    }

    @Override
    public NetworkInterfaceImpl withExistingNetwork(Network network) {
        this.existingVirtualNetworkToAssociate = network;
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable) {
        this.creatablePublicIpKey = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPublicIpAddress() {
        String name = nameWithPrefix("pip");
        return withNewPublicIpAddress(prepareCreatablePublicIp(name, name));
    }

    @Override
    public NetworkInterfaceImpl withNewPublicIpAddress(String leafDnsLabel) {
        return withNewPublicIpAddress(prepareCreatablePublicIp(nameWithPrefix("pip"), leafDnsLabel));
    }

    @Override
    public NetworkInterfaceImpl withoutPublicIpAddress() {
        this.removePublicIPAssociation = true;
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        this.existingPublicIpAddressToAssociate = publicIpAddress;
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrivateIpAddressDynamic() {
        this.primaryIpConfiguration().setPrivateIPAllocationMethod("Dynamic");
        this.primaryIpConfiguration().setPrivateIPAddress(null);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrivateIpAddressStatic(String staticPrivateIpAddress) {
        this.primaryIpConfiguration().setPrivateIPAllocationMethod("Static");
        this.primaryIpConfiguration().setPrivateIPAddress(staticPrivateIpAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withIPForwardingEnabled() {
        this.inner().setEnableIPForwarding(true);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withIPForwardingDisabled() {
        this.inner().setEnableIPForwarding(false);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withDnsServer(String ipAddress) {
        this.dnsServerIps().add(ipAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutDnsServer(String ipAddress) {
        this.dnsServerIps().remove(ipAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withAzureDnsServer() {
        this.dnsServerIps().clear();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withSubnet(String name) {
        this.subnetToAssociate = name;
        return this;
    }

    @Override
    public NetworkInterfaceImpl withInternalDnsNameLabel(String dnsNameLabel) {
        this.inner().dnsSettings().setInternalDnsNameLabel(dnsNameLabel);
        return this;
    }

    /**************************************************.
     * Getters
     **************************************************/

    @Override
    public Boolean isIPForwardingEnabled() {
        return this.inner().enableIPForwarding();
    }

    @Override
    public Boolean isPrimary() {
        return this.inner().primary();
    }

    @Override
    public String macAddress() {
        return this.inner().macAddress();
    }

    @Override
    public String internalDNSNameLabel() {
        return this.inner().dnsSettings().internalDnsNameLabel();
    }

    @Override
    public String internalFQDN() {
        return this.inner().dnsSettings().internalFqdn();
    }

    @Override
    public List<String> dnsServers() {
        return this.dnsServerIps();
    }

    @Override
    public String publicIpAddressId() {
        if (this.primaryIpConfiguration().publicIPAddress() == null) {
            return null;
        }
        return this.primaryIpConfiguration().publicIPAddress().id();
    }

    @Override
    public PublicIpAddress publicIpAddress() throws CloudException, IOException {
        String id = publicIpAddressId();
        if (id == null) {
            return null;
        }

        return this.publicIpAddresses.get(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public String subnetId() {
        return this.primaryIpConfiguration().subnet().id();
    }

    @Override
    public Network network() throws CloudException, IOException {
        String id = subnetId();
        return this.networks.get(ResourceUtils.groupFromResourceId(id), ResourceUtils.extractFromResourceId(id, "virtualNetworks"));
    }

    @Override
    public String privateIp() {
        return this.primaryIpConfiguration().privateIPAddress();
    }

    @Override
    public String privateIpAllocationMethod() {
        return this.primaryIpConfiguration().privateIPAllocationMethod();
    }


    /**************************************************.
     * CreatableImpl::createResource
     **************************************************/

    @Override
    protected void createResource() throws Exception {
        this.primaryIpConfiguration().setSubnet(subnetToAssociate());
        this.primaryIpConfiguration().setPublicIPAddress(publicIpToAssociate());
        ServiceResponse<NetworkInterfaceInner> response = this.client.createOrUpdate(this.resourceGroupName(), this.nicName, this.inner());
        this.setInner(response.getBody());
    }

    /**************************************************.
     * Helper methods
     **************************************************/

    private String nameWithPrefix(String prefix) {
        return prefix + "-" + this.uniqueId + "-" + this.resourceGroupName();
    }

    /**
     * @return the primary IP configuration of the network interface
     */
    private NetworkInterfaceIPConfiguration primaryIpConfiguration() {
        if (this.inner().ipConfigurations().size() == 0) {
            this.inner().ipConfigurations().add(new NetworkInterfaceIPConfiguration());
            this.inner().ipConfigurations().get(0).setName("primary-nic-config");
        }
        return this.inner().ipConfigurations().get(0);
    }

    private List<String> dnsServerIps() {
        if (this.inner().dnsSettings().dnsServers() == null) {
            this.inner().dnsSettings().setDnsServers(new ArrayList<String>());
        }
        return this.inner().dnsSettings().dnsServers();
    }

    /**
     * Creates a {@link PublicIpAddress.DefinitionCreatable} with the give name and DNS label.
     *
     * @param name the public IP name
     * @param leafDnsLabel the domain name label
     * @return {@link PublicIpAddress.DefinitionCreatable}
     */
    private PublicIpAddress.DefinitionCreatable prepareCreatablePublicIp(String name, String leafDnsLabel) {
        PublicIpAddress.DefinitionWithGroup definitionWithGroup = this.publicIpAddresses
                .define(name)
                .withRegion(this.region());

        PublicIpAddress.DefinitionCreatable definitionAfterGroup;
        if (this.newGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewGroup(this.newGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingGroup(this.resourceGroupName());
        }
        return definitionAfterGroup.withLeafDomainLabel(leafDnsLabel);
    }

    /**
     * Gets the subnet to associate with the network interface.
     * <p>
     * this method will never return null as subnet is required for a network interface, in case of
     * update mode if user didn't choose to change the subnet then existing subnet will be returned.
     *
     * @return the subnet resource
     */
    private SubnetInner subnetToAssociate() {
        SubnetInner subnetInner = new SubnetInner();
        if (isInCreateMode()) {
            // define..create mode
            if (this.creatableVirtualNetworkKey != null) {
                Network network = (Network) createdResource(this.creatableVirtualNetworkKey);
                subnetInner.setId(network.inner().subnets().get(0).id());
                return subnetInner;
            }

            for (SubnetInner subnet : this.existingVirtualNetworkToAssociate.inner().subnets()) {
                if (subnet.name().compareToIgnoreCase(this.subnetToAssociate) == 0) {
                    subnetInner.setId(subnet.id());
                    return subnetInner;
                }
            }

            throw new RuntimeException("A subnet with name '" + subnetToAssociate + "' not found under the network '" + this.existingVirtualNetworkToAssociate.name() + "'");

        } else {
            // update..apply mode
            if (subnetToAssociate != null) {
                int idx = this.primaryIpConfiguration().subnet().id().lastIndexOf('/');
                subnetInner.setId(this.primaryIpConfiguration().subnet().id().substring(0, idx) + subnetToAssociate);
            } else {
                subnetInner.setId(this.primaryIpConfiguration().subnet().id());
            }
            return subnetInner;
        }
    }

    /**
     * Get the SubResource instance representing a public IP that needs to be associated with the
     * network interface.
     * <p>
     * null will be returned if withoutPublicIP() is specified in the update fluent chain or user did't
     * opt for public IP in create fluent chain. In case of update chain, if withoutPublicIP(..) is
     * not specified then existing associated (if any) public IP will be returned.
     * @return public ip SubResource
     */
    private SubResource publicIpToAssociate() {
        if (this.removePublicIPAssociation) {
            return null;
        }

        PublicIPAddressInner publicIPAddressInner = null;
        if (this.creatablePublicIpKey != null) {
            PublicIpAddress publicIpAddress = (PublicIpAddress) createdResource(this.creatablePublicIpKey);
            publicIPAddressInner = publicIpAddress.inner();
        }

        if (this.existingPublicIpAddressToAssociate != null) {
            publicIPAddressInner = this.existingPublicIpAddressToAssociate.inner();
        }

        if (publicIPAddressInner != null) {
            SubResource subResource = new SubResource();
            subResource.setId(publicIPAddressInner.id());
            return subResource;
        }

        if (!isInCreateMode()) {
            // update..apply mode
            return this.primaryIpConfiguration().publicIPAddress();
        }
        return null;
    }
}
