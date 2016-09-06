/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Implementation for {@link NetworkInterface} and its create and update interfaces.
 */
class NetworkInterfaceImpl
        extends GroupableResourceImpl<
            NetworkInterface,
            NetworkInterfaceInner,
            NetworkInterfaceImpl,
            NetworkManager>
        implements
        NetworkInterface,
        NetworkInterface.Definition,
        NetworkInterface.Update {
    // Clients
    private final NetworkInterfacesInner client;
    // the name of the network interface
    private final String nicName;
    // used to generate unique name for any dependency resources
    protected final ResourceNamer namer;
    // reference to the primary ip configuration
    private NicIpConfigurationImpl nicPrimaryIpConfiguration;
    // list of references to all ip configuration
    private List<NicIpConfiguration> nicIpConfigurations;
    // unique key of a creatable network security group to be associated with the network interface
    private String creatableNetworkSecurityGroupKey;
    // reference to an network security group to be associated with the network interface
    private NetworkSecurityGroup existingNetworkSecurityGroupToAssociate;
    // Cached related resources.
    private PublicIpAddress primaryPublicIp;
    private Network primaryNetwork;
    private NetworkSecurityGroup networkSecurityGroup;

    NetworkInterfaceImpl(String name,
                         NetworkInterfaceInner innerModel,
                         final NetworkInterfacesInner client,
                         final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.client = client;
        this.nicName = name;
        this.namer = new ResourceNamer(this.nicName);
        initializeNicIpConfigurations();
    }

    // Verbs

    @Override
    public NetworkInterface refresh() throws Exception {
        ServiceResponse<NetworkInterfaceInner> response =
                this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        clearCachedRelatedResources();
        initializeNicIpConfigurations();
        return this;
    }

    @Override
    public Observable<NetworkInterface> applyAsync() {
        return createAsync();
    }

    // Setters (fluent)

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(Creatable<Network> creatable) {
        this.primaryIpConfiguration().withNewNetwork(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(String name, String addressSpaceCidr) {
        this.primaryIpConfiguration().withNewNetwork(name, addressSpaceCidr);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(String addressSpaceCidr) {
        this.primaryIpConfiguration().withNewNetwork(addressSpaceCidr);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPrimaryNetwork(Network network) {
        this.primaryIpConfiguration().withExistingNetwork(network);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        this.primaryIpConfiguration().withNewPublicIpAddress(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIpAddress() {
        this.primaryIpConfiguration().withNewPublicIpAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIpAddress(String leafDnsLabel) {
        this.primaryIpConfiguration().withNewPublicIpAddress(leafDnsLabel);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutPrimaryPublicIpAddress() {
        this.primaryIpConfiguration().withoutPublicIpAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress) {
        this.primaryIpConfiguration().withExistingPublicIpAddress(publicIpAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIpAddressDynamic() {
        this.primaryIpConfiguration().withPrivateIpAddressDynamic();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress) {
        this.primaryIpConfiguration().withPrivateIpAddressStatic(staticPrivateIpAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewNetworkSecurityGroup(Creatable<NetworkSecurityGroup> creatable) {
        if (this.creatableNetworkSecurityGroupKey == null) {
            this.creatableNetworkSecurityGroupKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup) {
        this.existingNetworkSecurityGroupToAssociate = networkSecurityGroup;
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutNetworkSecurityGroup() {
        this.inner().withNetworkSecurityGroup(null);
        return this;
    }

    @Override
    public NicIpConfigurationImpl defineSecondaryIpConfiguration(String name) {
        return prepareNewNicIpConfiguration(name);
    }

    @Override
    public NicIpConfigurationImpl updateIpConfiguration(String name) {
        for (NicIpConfiguration nicIpConfiguration : this.nicIpConfigurations) {
            if (name.equalsIgnoreCase(nicIpConfiguration.name())) {
                return (NicIpConfigurationImpl) nicIpConfiguration;
            }
        }
        throw new RuntimeException("IP configuration '" + name + "' not found");
    }

    @Override
    public NetworkInterfaceImpl withIpForwarding() {
        this.inner().withEnableIPForwarding(true);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutIpForwarding() {
        this.inner().withEnableIPForwarding(false);
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
        this.primaryIpConfiguration().withSubnet(name);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withInternalDnsNameLabel(String dnsNameLabel) {
        this.inner().dnsSettings().withInternalDnsNameLabel(dnsNameLabel);
        return this;
    }

    // Getters

    @Override
    public boolean isIpForwardingEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableIPForwarding());
    }

    @Override
    public String macAddress() {
        return this.inner().macAddress();
    }

    @Override
    public String internalDnsNameLabel() {
        return this.inner().dnsSettings().internalDnsNameLabel();
    }

    @Override
    public String internalDomainNameSuffix() {
        return this.inner().dnsSettings().internalDomainNameSuffix();
    }

    @Override
    public List<String> appliedDnsServers() {
        return Collections.unmodifiableList(this.inner().dnsSettings().appliedDnsServers());
    }

    @Override
    public String internalFqdn() {
        return this.inner().dnsSettings().internalFqdn();
    }

    @Override
    public List<String> dnsServers() {
        return this.dnsServerIps();
    }

    @Override
    public PublicIpAddress primaryPublicIpAddress() throws CloudException, IOException {
        if (this.primaryPublicIp == null) {
            this.primaryPublicIp = this.primaryIpConfiguration().publicIpAddress();
        }
        return primaryPublicIp;
    }

    @Override
    public String primarySubnetId() {
        return this.primaryIpConfiguration().subnetId();
    }

    @Override
    public Network primaryNetwork() throws CloudException, IOException {
        if (this.primaryNetwork == null) {
            this.primaryNetwork = this.primaryIpConfiguration().network();
        }
        return this.primaryNetwork;
    }

    @Override
    public String primaryPrivateIp() {
        return this.primaryIpConfiguration().privateIp();
    }

    @Override
    public IPAllocationMethod primaryPrivateIpAllocationMethod() {
        return this.primaryIpConfiguration().privateIpAllocationMethod();
    }

    @Override
    public List<NicIpConfiguration> ipConfigurations() {
        return Collections.unmodifiableList(this.nicIpConfigurations);
    }

    @Override
    public String networkSecurityGroupId() {
        if (this.inner().networkSecurityGroup() != null) {
            return this.inner().networkSecurityGroup().id();
        }
        return null;
    }

    @Override
    public NetworkSecurityGroup networkSecurityGroup() throws CloudException, IOException {
        if (this.networkSecurityGroup == null && this.networkSecurityGroupId() != null) {
            String id = this.networkSecurityGroupId();
            this.networkSecurityGroup = super.myManager
                    .networkSecurityGroups()
                    .getByGroup(ResourceUtils.groupFromResourceId(id),
                    ResourceUtils.nameFromResourceId(id));
        }
        return this.networkSecurityGroup;
    }

    /**
     * @return the primary IP configuration of the network interface
     */
    public NicIpConfigurationImpl primaryIpConfiguration() {
        if (this.nicPrimaryIpConfiguration != null) {
            return this.nicPrimaryIpConfiguration;
        }

        if (isInCreateMode()) {
            this.nicPrimaryIpConfiguration = prepareNewNicIpConfiguration("primary-nic-config");
            withIpConfiguration(this.nicPrimaryIpConfiguration);
        } else {
            // Currently Azure supports only one IP configuration and that is the primary
            // hence we pick the first one here.
            // when Azure support multiple IP configurations then there will be a flag in
            // the IPConfiguration or a property in the network interface to identify the
            // primary so below logic will be changed.
            this.nicPrimaryIpConfiguration = (NicIpConfigurationImpl) this.nicIpConfigurations.get(0);
        }
        return this.nicPrimaryIpConfiguration;
    }


    // CreatorTaskGroup.ResourceCreator implementation
    @Override
    public Observable<NetworkInterface> createResourceAsync() {
        final NetworkInterfaceImpl self = this;
        NetworkSecurityGroup networkSecurityGroup = null;
        if (creatableNetworkSecurityGroupKey != null) {
            networkSecurityGroup = (NetworkSecurityGroup) this.createdResource(creatableNetworkSecurityGroupKey);
        } else if (existingNetworkSecurityGroupToAssociate != null) {
            networkSecurityGroup = existingNetworkSecurityGroupToAssociate;
        }

        if (networkSecurityGroup != null) {
            this.inner().withNetworkSecurityGroup(new SubResource().withId(networkSecurityGroup.id()));
        }
        NicIpConfigurationImpl.ensureConfigurations(this.nicIpConfigurations);

        return this.client.createOrUpdateAsync(this.resourceGroupName(),
                this.nicName,
                this.inner())
                .map(new Func1<ServiceResponse<NetworkInterfaceInner>, NetworkInterface>() {
                    @Override
                    public NetworkInterface call(ServiceResponse<NetworkInterfaceInner> networkInterfaceInner) {
                        self.setInner(networkInterfaceInner.getBody());
                        clearCachedRelatedResources();
                        initializeNicIpConfigurations();
                        return self;
                    }
                });
    }

    /**************************************************.
     * Helper methods
     **************************************************/

    /**
     * @return the list of DNS server IPs from the DNS settings
     */
    private List<String> dnsServerIps() {
        if (this.inner().dnsSettings().dnsServers() == null) {
            this.inner().dnsSettings().withDnsServers(new ArrayList<String>());
        }
        return this.inner().dnsSettings().dnsServers();
    }

    /**
     * Initializes the list of {@link NicIpConfiguration} that wraps {@link NetworkInterfaceInner#ipConfigurations()}.
     */
    private void initializeNicIpConfigurations() {
        if (this.inner().ipConfigurations() == null) {
            this.inner().withIpConfigurations(new ArrayList<NetworkInterfaceIPConfigurationInner>());
        }

        this.nicIpConfigurations = new ArrayList<>();
        for (NetworkInterfaceIPConfigurationInner ipConfig : this.inner().ipConfigurations()) {
            NicIpConfigurationImpl  nicIpConfiguration = new NicIpConfigurationImpl(ipConfig,
                    this,
                    super.myManager,
                    false);
            this.nicIpConfigurations.add(nicIpConfiguration);
        }
    }

    /**
     * Gets a new IP configuration child resource {@link NicIpConfiguration} wrapping {@link NetworkInterfaceIPConfigurationInner}.
     *
     * @param name the name for the new ip configuration
     * @return {@link NicIpConfiguration}
     */
    private NicIpConfigurationImpl prepareNewNicIpConfiguration(String name) {
        NicIpConfigurationImpl nicIpConfiguration = NicIpConfigurationImpl.prepareNicIpConfiguration(
                name,
                this,
                super.myManager
        );
        return nicIpConfiguration;
    }

    private void clearCachedRelatedResources() {
        this.primaryPublicIp = null;
        this.primaryNetwork = null;
        this.networkSecurityGroup = null;
        this.nicPrimaryIpConfiguration = null;
    }

    NetworkInterfaceImpl withIpConfiguration(NicIpConfigurationImpl nicIpConfiguration) {
        this.nicIpConfigurations.add(nicIpConfiguration);
        this.inner().ipConfigurations().add(nicIpConfiguration.inner());
        return this;
    }

    void addToCreatableDependencies(Creatable<? extends Resource> creatableResource) {
        super.addCreatableDependency(creatableResource);
    }

    Resource createdDependencyResource(String key) {
        return super.createdResource(key);
    }

    Creatable<ResourceGroup> newGroup() {
        return this.creatableGroup;
    }
}