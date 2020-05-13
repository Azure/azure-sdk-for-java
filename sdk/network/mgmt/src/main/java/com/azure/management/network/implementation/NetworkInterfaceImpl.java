// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.network.implementation;

import com.azure.management.network.IPAllocationMethod;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.Network;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NetworkSecurityGroup;
import com.azure.management.network.NicIPConfiguration;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.models.GroupableParentResourceWithTagsImpl;
import com.azure.management.network.models.NetworkInterfaceIPConfigurationInner;
import com.azure.management.network.models.NetworkInterfaceInner;
import com.azure.management.network.models.NetworkSecurityGroupInner;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.azure.management.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for NetworkInterface and its create and update interfaces. */
class NetworkInterfaceImpl
    extends GroupableParentResourceWithTagsImpl<
        NetworkInterface, NetworkInterfaceInner, NetworkInterfaceImpl, NetworkManager>
    implements NetworkInterface, NetworkInterface.Definition, NetworkInterface.Update {
    /** the name of the network interface. */
    private final String nicName;
    /** used to generate unique name for any dependency resources. */
    protected final ResourceNamer namer;
    /** references to all ip configuration. */
    private Map<String, NicIPConfiguration> nicIPConfigurations;
    /** unique key of a creatable network security group to be associated with the network interface. */
    private String creatableNetworkSecurityGroupKey;
    /** reference to an network security group to be associated with the network interface. */
    private NetworkSecurityGroup existingNetworkSecurityGroupToAssociate;
    /** cached related resources. */
    private NetworkSecurityGroup networkSecurityGroup;

    NetworkInterfaceImpl(String name, NetworkInterfaceInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.nicName = name;
        this.namer = this.manager().getSdkContext().getResourceNamerFactory().createResourceNamer(this.nicName);
        initializeChildrenFromInner();
    }

    // Verbs

    @Override
    public Mono<NetworkInterface> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                networkInterface -> {
                    NetworkInterfaceImpl impl = (NetworkInterfaceImpl) networkInterface;
                    impl.clearCachedRelatedResources();
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<NetworkInterfaceInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .networkInterfaces()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<NetworkInterfaceInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .inner()
            .networkInterfaces()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags());
    }

    // Setters (fluent)

    @Override
    public NetworkInterfaceImpl withAcceleratedNetworking() {
        this.inner().withEnableAcceleratedNetworking(true);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutAcceleratedNetworking() {
        this.inner().withEnableAcceleratedNetworking(false);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(Creatable<Network> creatable) {
        this.primaryIPConfiguration().withNewNetwork(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(String name, String addressSpaceCidr) {
        this.primaryIPConfiguration().withNewNetwork(name, addressSpaceCidr);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryNetwork(String addressSpaceCidr) {
        this.primaryIPConfiguration().withNewNetwork(addressSpaceCidr);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPrimaryNetwork(Network network) {
        this.primaryIPConfiguration().withExistingNetwork(network);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress(Creatable<PublicIPAddress> creatable) {
        this.primaryIPConfiguration().withNewPublicIPAddress(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress() {
        this.primaryIPConfiguration().withNewPublicIPAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress(String leafDnsLabel) {
        this.primaryIPConfiguration().withNewPublicIPAddress(leafDnsLabel);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName) {
        this.primaryIPConfiguration().withExistingLoadBalancerBackend(loadBalancer, backendName);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingLoadBalancerInboundNatRule(
        LoadBalancer loadBalancer, String inboundNatRuleName) {
        this.primaryIPConfiguration().withExistingLoadBalancerInboundNatRule(loadBalancer, inboundNatRuleName);
        return this;
    }

    @Override
    public Update withoutLoadBalancerBackends() {
        for (NicIPConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name()).withoutLoadBalancerBackends();
        }
        return this;
    }

    @Override
    public Update withoutLoadBalancerInboundNatRules() {
        for (NicIPConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name()).withoutLoadBalancerInboundNatRules();
        }
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutPrimaryPublicIPAddress() {
        this.primaryIPConfiguration().withoutPublicIPAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPrimaryPublicIPAddress(PublicIPAddress publicIPAddress) {
        this.primaryIPConfiguration().withExistingPublicIPAddress(publicIPAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIPAddressDynamic() {
        this.primaryIPConfiguration().withPrivateIPAddressDynamic();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress) {
        this.primaryIPConfiguration().withPrivateIPAddressStatic(staticPrivateIPAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewNetworkSecurityGroup(Creatable<NetworkSecurityGroup> creatable) {
        if (this.creatableNetworkSecurityGroupKey == null) {
            this.creatableNetworkSecurityGroupKey = this.addDependency(creatable);
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
    public NicIPConfigurationImpl defineSecondaryIPConfiguration(String name) {
        return prepareNewNicIPConfiguration(name);
    }

    @Override
    public NicIPConfigurationImpl updateIPConfiguration(String name) {
        return (NicIPConfigurationImpl) this.nicIPConfigurations.get(name);
    }

    @Override
    public NetworkInterfaceImpl withIPForwarding() {
        this.inner().withEnableIPForwarding(true);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutIPConfiguration(String name) {
        this.nicIPConfigurations.remove(name);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutIPForwarding() {
        this.inner().withEnableIPForwarding(false);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withDnsServer(String ipAddress) {
        this.dnsServerIPs().add(ipAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutDnsServer(String ipAddress) {
        this.dnsServerIPs().remove(ipAddress);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withAzureDnsServer() {
        this.dnsServerIPs().clear();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withSubnet(String name) {
        this.primaryIPConfiguration().withSubnet(name);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withInternalDnsNameLabel(String dnsNameLabel) {
        this.inner().dnsSettings().withInternalDnsNameLabel(dnsNameLabel);
        return this;
    }

    // Getters

    @Override
    public boolean isAcceleratedNetworkingEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableAcceleratedNetworking());
    }

    @Override
    public String virtualMachineId() {
        if (this.inner().virtualMachine() != null) {
            return this.inner().virtualMachine().id();
        } else {
            return null;
        }
    }

    @Override
    public boolean isIPForwardingEnabled() {
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
        return this.dnsServerIPs();
    }

    @Override
    public String primaryPrivateIP() {
        return this.primaryIPConfiguration().privateIPAddress();
    }

    @Override
    public IPAllocationMethod primaryPrivateIPAllocationMethod() {
        return this.primaryIPConfiguration().privateIPAllocationMethod();
    }

    @Override
    public Map<String, NicIPConfiguration> ipConfigurations() {
        return Collections.unmodifiableMap(this.nicIPConfigurations);
    }

    @Override
    public String networkSecurityGroupId() {
        return (this.inner().networkSecurityGroup() != null) ? this.inner().networkSecurityGroup().id() : null;
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
        if (this.networkSecurityGroup == null && this.networkSecurityGroupId() != null) {
            String id = this.networkSecurityGroupId();
            this.networkSecurityGroup =
                super
                    .myManager
                    .networkSecurityGroups()
                    .getByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
        }
        return this.networkSecurityGroup;
    }

    /** @return the primary IP configuration of the network interface */
    @Override
    public NicIPConfigurationImpl primaryIPConfiguration() {
        NicIPConfigurationImpl primaryIPConfig = null;
        if (this.nicIPConfigurations.size() == 0) {
            // If no primary IP config found yet, then create one automatically, otherwise the NIC is in a bad state
            primaryIPConfig = prepareNewNicIPConfiguration("primary");
            primaryIPConfig.inner().withPrimary(true);
            withIPConfiguration(primaryIPConfig);
        } else if (this.nicIPConfigurations.size() == 1) {
            // If there is only one IP config, assume it is primary, regardless of the Primary flag
            primaryIPConfig = (NicIPConfigurationImpl) this.nicIPConfigurations.values().iterator().next();
        } else {
            // If multiple IP configs, then find the one marked as primary
            for (NicIPConfiguration ipConfig : this.nicIPConfigurations.values()) {
                if (ipConfig.isPrimary()) {
                    primaryIPConfig = (NicIPConfigurationImpl) ipConfig;
                    break;
                }
            }
        }

        // Return the found primary IP config, including null, if no primary IP config can be identified
        // in which case the NIC is in a bad state anyway
        return primaryIPConfig;
    }

    /** @return the list of DNS server IPs from the DNS settings */
    private List<String> dnsServerIPs() {
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
        this.nicIPConfigurations = new TreeMap<>();
        List<NetworkInterfaceIPConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners != null) {
            for (NetworkInterfaceIPConfigurationInner inner : inners) {
                NicIPConfigurationImpl nicIPConfiguration =
                    new NicIPConfigurationImpl(inner, this, super.myManager, false);
                this.nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
            }
        }
    }

    /**
     * Gets a new IP configuration child resource {@link NicIPConfiguration} wrapping {@link
     * NetworkInterfaceIPConfigurationInner}.
     *
     * @param name the name for the new ip configuration
     * @return {@link NicIPConfiguration}
     */
    private NicIPConfigurationImpl prepareNewNicIPConfiguration(String name) {
        NicIPConfigurationImpl nicIPConfiguration =
            NicIPConfigurationImpl.prepareNicIPConfiguration(name, this, super.myManager);
        return nicIPConfiguration;
    }

    private void clearCachedRelatedResources() {
        this.networkSecurityGroup = null;
    }

    NetworkInterfaceImpl withIPConfiguration(NicIPConfigurationImpl nicIPConfiguration) {
        this.nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
        return this;
    }

    void addToCreatableDependencies(Creatable<? extends Resource> creatableResource) {
        this.addDependency(creatableResource);
    }

    Resource createdDependencyResource(String key) {
        return this.<Resource>taskResult(key);
    }

    Creatable<ResourceGroup> newGroup() {
        return this.creatableGroup;
    }

    @Override
    protected Mono<NetworkInterfaceInner> createInner() {
        return this
            .manager()
            .inner()
            .networkInterfaces()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void afterCreating() {
        clearCachedRelatedResources();
    }

    @Override
    protected void beforeCreating() {
        NetworkSecurityGroup networkSecurityGroup = null;
        if (creatableNetworkSecurityGroupKey != null) {
            networkSecurityGroup = this.<NetworkSecurityGroup>taskResult(creatableNetworkSecurityGroupKey);
        } else if (existingNetworkSecurityGroupToAssociate != null) {
            networkSecurityGroup = existingNetworkSecurityGroupToAssociate;
        }

        // Associate an NSG if needed
        if (networkSecurityGroup != null) {
            this.inner().withNetworkSecurityGroup(new NetworkSecurityGroupInner().withId(networkSecurityGroup.id()));
        }

        NicIPConfigurationImpl.ensureConfigurations(this.nicIPConfigurations.values());

        // Reset and update IP configs
        this.inner().withIpConfigurations(innersFromWrappers(this.nicIPConfigurations.values()));
    }
}
