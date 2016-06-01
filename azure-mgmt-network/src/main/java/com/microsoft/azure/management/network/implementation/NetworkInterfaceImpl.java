/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.network.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
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

    NetworkInterfaceImpl(String name,
                         NetworkInterfaceInner innerModel,
                         final NetworkInterfacesInner client,
                         final Networks networks,
                         final PublicIpAddresses publicIpAddresses,
                         final ResourceManager resourceManager) {
        super(name, innerModel, resourceManager.resourceGroups());
        this.client = client;
        this.networks = networks;
        this.publicIpAddresses = publicIpAddresses;
        this.nicName = name;
        this.uniqueId = this.nicName + String.valueOf(System.currentTimeMillis());
        initialize();
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
    public boolean isIPForwardingEnabled() {
        return this.inner().enableIPForwarding();
    }

    @Override
    public boolean isPrimary() {
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
        return this.networks.get(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
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
        SubnetInner subnetInner = subnetToAssociate();
        if (subnetInner != null) {
            this.primaryIpConfiguration().setSubnet(subnetInner);
        }

        PublicIPAddressInner publicIpInner = publicIpToAssociate();
        if (publicIpInner != null) {
            this.primaryIpConfiguration().setPublicIPAddress(publicIpInner);
        }

        ServiceResponse<NetworkInterfaceInner> response = this.client.createOrUpdate(this.resourceGroupName(), this.nicName, this.inner());
        this.setInner(response.getBody());
    }

    /**************************************************.
     * Helper methods
     **************************************************/

    private void initialize() {
        if (this.inner().ipConfigurations() == null) {
            this.inner().setIpConfigurations(new ArrayList<NetworkInterfaceIPConfiguration>());
        }

        if (this.inner().dnsSettings() == null) {
            this.inner().setDnsSettings(new NetworkInterfaceDnsSettings());
        }
    }

    private String nameWithPrefix(String prefix) {
        return prefix + "-" + this.uniqueId + "-" + this.resourceGroupName();
    }

    private NetworkInterfaceIPConfiguration primaryIpConfiguration() {
        if (this.inner().ipConfigurations().size() == 0) {
            this.inner().ipConfigurations().add(new NetworkInterfaceIPConfiguration());
        }
        return this.inner().ipConfigurations().get(0);
    }

    private List<String> dnsServerIps() {
        if (this.inner().dnsSettings().dnsServers() == null) {
            this.inner().dnsSettings().setDnsServers(new ArrayList<String>());
        }
        return this.inner().dnsSettings().dnsServers();
    }

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

    private SubnetInner subnetToAssociate() {
        SubnetInner subnetInner = new SubnetInner();
        if (isInCreateMode()) {
            // define..create mode
            if (this.creatableVirtualNetworkKey != null) {
                Network network = (Network) createdResource(this.creatableVirtualNetworkKey);
                subnetInner.setId(network.subnets().get(0).inner().id());
                return subnetInner;
            }

            for (Subnet subnet : this.existingVirtualNetworkToAssociate.subnets()) {
                if (subnet.name().compareToIgnoreCase(this.subnetToAssociate) == 0) {
                    subnetInner.setId(subnet.inner().id());
                    return subnetInner;
                }
            }

            throw new RuntimeException("A subnet with name '" + subnetToAssociate + "' not found under the network '" + this.existingVirtualNetworkToAssociate.name() + "'");

        } else {
            // update..apply mode
            if (subnetToAssociate != null) {
                int idx = this.primaryIpConfiguration().subnet().id().lastIndexOf('/');
                subnetInner.setId(this.primaryIpConfiguration().subnet().id().substring(0, idx) + subnetToAssociate);
                return subnetInner;
            }

            return null;
        }
    }

    private PublicIPAddressInner publicIpToAssociate() {
        if (isInCreateMode()) {
            // define..create mode
            if (this.creatablePublicIpKey != null) {
                PublicIpAddress publicIpAddress = (PublicIpAddress) createdResource(this.creatablePublicIpKey);
                return publicIpAddress.inner();
            }

            if (this.existingPublicIpAddressToAssociate != null) {
                return this.existingPublicIpAddressToAssociate.inner();
            }
            // withoutPublicIPAddress
        }
        return null;
    }

    /**
     * @return <tt>true</tt> if currently in define..create mode
     */
    private boolean isInCreateMode() {
        return this.inner() == null;
    }
}
