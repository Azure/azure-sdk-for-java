/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *  Implementation for {@link NetworkInterface} and its create and update interfaces.
 */
@LangDefinition
class NetworkInterfaceImpl
        extends GroupableParentResourceImpl<
            NetworkInterface,
            NetworkInterfaceInner,
            NetworkInterfaceImpl,
            NetworkManager>
        implements
        NetworkInterface,
        NetworkInterface.Definition,
        NetworkInterface.Update {
    /**
     * the inner collection.
     */
    private final NetworkInterfacesInner innerCollection;
    /**
     * the name of the network interface.
     */
    private final String nicName;
    /**
     * used to generate unique name for any dependency resources.
     */
    protected final ResourceNamer namer;
    /**
     * reference to the primary ip configuration.
     */
    private NicIpConfigurationImpl nicPrimaryIpConfiguration;
    /**
     * references to all ip configuration.
     */
    private Map<String, NicIpConfiguration> nicIpConfigurations;
    /**
     * unique key of a creatable network security group to be associated with the network interface.
     */
    private String creatableNetworkSecurityGroupKey;
    /**
     * reference to an network security group to be associated with the network interface.
     */
    private NetworkSecurityGroup existingNetworkSecurityGroupToAssociate;
    /**
     * cached related resources.
     */
    private NetworkSecurityGroup networkSecurityGroup;

    NetworkInterfaceImpl(String name,
                         NetworkInterfaceInner innerModel,
                         final NetworkInterfacesInner client,
                         final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = client;
        this.nicName = name;
        this.namer = SdkContext.getResourceNamerFactory().createResourceNamer(this.nicName);
        initializeChildrenFromInner();
    }

    // Verbs

    @Override
    public NetworkInterface refresh() {
        NetworkInterfaceInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        clearCachedRelatedResources();
        initializeChildrenFromInner();
        return this;
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
    public NetworkInterfaceImpl withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName) {
        this.primaryIpConfiguration().withExistingLoadBalancerBackend(loadBalancer, backendName);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName) {
        this.primaryIpConfiguration().withExistingLoadBalancerInboundNatRule(loadBalancer, inboundNatRuleName);
        return this;
    }

    @Override
    public Update withoutLoadBalancerBackends() {
        for (NicIpConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIpConfiguration(ipConfig.name())
                .withoutLoadBalancerBackends();
        }
        return this;
    }

    @Override
    public Update withoutLoadBalancerInboundNatRules() {
        for (NicIpConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIpConfiguration(ipConfig.name())
                .withoutLoadBalancerInboundNatRules();
        }
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
        return (NicIpConfigurationImpl) this.nicIpConfigurations.get(name);
    }

    @Override
    public NetworkInterfaceImpl withIpForwarding() {
        this.inner().withEnableIPForwarding(true);
        return this;
    }

    //TODO: Networking doesn't support this yet, even though it exposes the API; so we have the impl but not exposed via the interface yet.
    public NetworkInterfaceImpl withoutIpConfiguration(String name) {
        this.nicIpConfigurations.remove(name);
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
    public String virtualMachineId() {
        if (this.inner().virtualMachine() != null) {
            return this.inner().virtualMachine().id();
        } else {
            return null;
        }
    }

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
        return (this.inner().dnsSettings() != null) ? this.inner().dnsSettings().internalDnsNameLabel() : null;
    }

    @Override
    public String internalDomainNameSuffix() {
        return (this.inner().dnsSettings() != null) ? this.inner().dnsSettings().internalDomainNameSuffix() : null;
    }

    @Override
    public List<String> appliedDnsServers() {
        List<String> dnsServers = new ArrayList<String>();
        if (this.inner().dnsSettings() == null) {
            return Collections.unmodifiableList(dnsServers);
        } else if (this.inner().dnsSettings().appliedDnsServers() == null) {
            return Collections.unmodifiableList(dnsServers);
        } else {
            return Collections.unmodifiableList(this.inner().dnsSettings().appliedDnsServers());
        }
    }

    @Override
    public String internalFqdn() {
        return (this.inner().dnsSettings() != null) ? this.inner().dnsSettings().internalFqdn() : null;
    }

    @Override
    public List<String> dnsServers() {
        return this.dnsServerIps();
    }

    @Override
    public String primaryPrivateIp() {
        return this.primaryIpConfiguration().privateIpAddress();
    }

    @Override
    public IPAllocationMethod primaryPrivateIpAllocationMethod() {
        return this.primaryIpConfiguration().privateIpAllocationMethod();
    }

    @Override
    public Map<String, NicIpConfiguration> ipConfigurations() {
        return Collections.unmodifiableMap(this.nicIpConfigurations);
    }

    @Override
    public String networkSecurityGroupId() {
        return (this.inner().networkSecurityGroup() != null) ? this.inner().networkSecurityGroup().id() : null;
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
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
            this.nicPrimaryIpConfiguration = prepareNewNicIpConfiguration("primary");
            withIpConfiguration(this.nicPrimaryIpConfiguration);
        } else {
            // TODO: Currently Azure supports only one IP configuration and that is the primary
            // hence we pick the first one here.
            // when Azure support multiple IP configurations then there will be a flag in
            // the IPConfiguration or a property in the network interface to identify the
            // primary so below logic will be changed.
            this.nicPrimaryIpConfiguration = (NicIpConfigurationImpl) new ArrayList<NicIpConfiguration>(
                    this.nicIpConfigurations.values()).get(0);
        }
        return this.nicPrimaryIpConfiguration;
    }

    /**
     * @return the list of DNS server IPs from the DNS settings
     */
    private List<String> dnsServerIps() {
        List<String> dnsServers = new ArrayList<String>();
        if (this.inner().dnsSettings() == null) {
            return dnsServers;
        } else if (this.inner().dnsSettings().dnsServers() == null) {
            return dnsServers;
        } else {
            return this.inner().dnsSettings().dnsServers();
        }
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.nicIpConfigurations = new TreeMap<>();
        List<NetworkInterfaceIPConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners != null) {
            for (NetworkInterfaceIPConfigurationInner inner : inners) {
                NicIpConfigurationImpl  nicIpConfiguration = new NicIpConfigurationImpl(inner, this, super.myManager, false);
                this.nicIpConfigurations.put(nicIpConfiguration.name(), nicIpConfiguration);
            }
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
        this.networkSecurityGroup = null;
        this.nicPrimaryIpConfiguration = null;
    }

    NetworkInterfaceImpl withIpConfiguration(NicIpConfigurationImpl nicIpConfiguration) {
        this.nicIpConfigurations.put(nicIpConfiguration.name(), nicIpConfiguration);
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

    @Override
    protected Observable<NetworkInterfaceInner> createInner() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void afterCreating() {
        clearCachedRelatedResources();
    }

    @Override
    protected void beforeCreating() {
        NetworkSecurityGroup networkSecurityGroup = null;
        if (creatableNetworkSecurityGroupKey != null) {
            networkSecurityGroup = (NetworkSecurityGroup) this.createdResource(creatableNetworkSecurityGroupKey);
        } else if (existingNetworkSecurityGroupToAssociate != null) {
            networkSecurityGroup = existingNetworkSecurityGroupToAssociate;
        }

        // Associate an NSG if needed
        if (networkSecurityGroup != null) {
            this.inner().withNetworkSecurityGroup(new SubResource().withId(networkSecurityGroup.id()));
        }

        NicIpConfigurationImpl.ensureConfigurations(this.nicIpConfigurations.values());

        // Reset and update IP configs
        this.inner().withIpConfigurations(innersFromWrappers(this.nicIpConfigurations.values()));
    }
}