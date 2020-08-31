// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkSecurityGroupInner;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceNamer;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for NetworkInterface and its create and update interfaces. */
class NetworkInterfaceImpl
    extends GroupableParentResourceWithTagsImpl<
        NetworkInterface, NetworkInterfaceInner, NetworkInterfaceImpl, NetworkManager>
    implements NetworkInterface, NetworkInterface.Definition, NetworkInterface.Update {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /** the name of the network interface. */
    private final String nicName;
    /** used to generate unique name for any dependency resources. */
    protected final ResourceNamer namer;
    /** references to all ip configuration. */
    private Map<String, NicIpConfiguration> nicIPConfigurations;
    /** unique key of a creatable network security group to be associated with the network interface. */
    private String creatableNetworkSecurityGroupKey;
    /** reference to an network security group to be associated with the network interface. */
    private NetworkSecurityGroup existingNetworkSecurityGroupToAssociate;
    /** cached related resources. */
    private NetworkSecurityGroup networkSecurityGroup;

    NetworkInterfaceImpl(String name, NetworkInterfaceInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.nicName = name;
        this.namer = this.manager().sdkContext().getResourceNamerFactory().createResourceNamer(this.nicName);
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
            .getNetworkInterfaces()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<NetworkInterfaceInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .inner()
            .getNetworkInterfaces()
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
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress(Creatable<PublicIpAddress> creatable) {
        this.primaryIPConfiguration().withNewPublicIpAddress(creatable);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress() {
        this.primaryIPConfiguration().withNewPublicIpAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withNewPrimaryPublicIPAddress(String leafDnsLabel) {
        this.primaryIPConfiguration().withNewPublicIpAddress(leafDnsLabel);
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
        for (NicIpConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name()).withoutLoadBalancerBackends();
        }
        return this;
    }

    @Override
    public Update withoutLoadBalancerInboundNatRules() {
        for (NicIpConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name()).withoutLoadBalancerInboundNatRules();
        }
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutPrimaryPublicIPAddress() {
        this.primaryIPConfiguration().withoutPublicIpAddress();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withExistingPrimaryPublicIPAddress(PublicIpAddress publicIPAddress) {
        this.primaryIPConfiguration().withExistingPublicIpAddress(publicIPAddress);
        this.primaryIPConfiguration().withPrivateIpVersion(publicIPAddress.version());
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIPAddressDynamic() {
        this.primaryIPConfiguration().withPrivateIpAddressDynamic();
        return this;
    }

    @Override
    public NetworkInterfaceImpl withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress) {
        this.primaryIPConfiguration().withPrivateIpAddressStatic(staticPrivateIPAddress);
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
    public NicIpConfigurationImpl defineSecondaryIPConfiguration(String name) {
        return prepareNewNicIPConfiguration(name);
    }

    @Override
    public NicIpConfigurationImpl updateIPConfiguration(String name) {
        return (NicIpConfigurationImpl) this.nicIPConfigurations.get(name);
    }

    @Override
    public NetworkInterfaceImpl withIPForwarding() {
        this.inner().withEnableIpForwarding(true);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutIPConfiguration(String name) {
        this.nicIPConfigurations.remove(name);
        return this;
    }

    @Override
    public NetworkInterfaceImpl withoutIPForwarding() {
        this.inner().withEnableIpForwarding(false);
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
        return Utils.toPrimitiveBoolean(this.inner().enableIpForwarding());
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
        return this.primaryIPConfiguration().privateIpAddress();
    }

    @Override
    public IpAllocationMethod primaryPrivateIpAllocationMethod() {
        return this.primaryIPConfiguration().privateIpAllocationMethod();
    }

    @Override
    public Map<String, NicIpConfiguration> ipConfigurations() {
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
    public NicIpConfigurationImpl primaryIPConfiguration() {
        NicIpConfigurationImpl primaryIPConfig = null;
        if (this.nicIPConfigurations.size() == 0) {
            // If no primary IP config found yet, then create one automatically, otherwise the NIC is in a bad state
            primaryIPConfig = prepareNewNicIPConfiguration("primary");
            primaryIPConfig.inner().withPrimary(true);
            withIPConfiguration(primaryIPConfig);
        } else if (this.nicIPConfigurations.size() == 1) {
            // If there is only one IP config, assume it is primary, regardless of the Primary flag
            primaryIPConfig = (NicIpConfigurationImpl) this.nicIPConfigurations.values().iterator().next();
        } else {
            // If multiple IP configs, then find the one marked as primary
            for (NicIpConfiguration ipConfig : this.nicIPConfigurations.values()) {
                if (ipConfig.isPrimary()) {
                    primaryIPConfig = (NicIpConfigurationImpl) ipConfig;
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
        List<NetworkInterfaceIpConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners != null) {
            for (NetworkInterfaceIpConfigurationInner inner : inners) {
                NicIpConfigurationImpl nicIPConfiguration =
                    new NicIpConfigurationImpl(inner, this, super.myManager, false);
                this.nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
            }
        }
    }

    /**
     * Gets a new IP configuration child resource {@link NicIpConfiguration} wrapping {@link
     * NetworkInterfaceIpConfigurationInner}.
     *
     * @param name the name for the new ip configuration
     * @return {@link NicIpConfiguration}
     */
    private NicIpConfigurationImpl prepareNewNicIPConfiguration(String name) {
        NicIpConfigurationImpl nicIPConfiguration =
            NicIpConfigurationImpl.prepareNicIPConfiguration(name, this, super.myManager);
        return nicIPConfiguration;
    }

    private void clearCachedRelatedResources() {
        this.networkSecurityGroup = null;
    }

    NetworkInterfaceImpl withIPConfiguration(NicIpConfigurationImpl nicIPConfiguration) {
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
    public Accepted<NetworkInterface> beginCreate() {
        return AcceptedImpl.newAccepted(logger,
            this.manager().inner(),
            () -> this.manager().inner().getNetworkInterfaces()
                .createOrUpdateWithResponseAsync(resourceGroupName(), name(), this.inner()).block(),
            inner -> new NetworkInterfaceImpl(inner.name(), inner, this.manager()),
            NetworkInterfaceInner.class,
            () -> {
                Flux<Indexable> dependencyTasksAsync =
                    taskGroup().invokeDependencyAsync(taskGroup().newInvocationContext());
                dependencyTasksAsync.blockLast();

                beforeCreating();
            },
            inner -> {
                innerToFluentMap(this);
                initializeChildrenFromInner();
                afterCreating();
            });
    }

    @Override
    protected Mono<NetworkInterfaceInner> createInner() {
        return this
            .manager()
            .inner()
            .getNetworkInterfaces()
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

        NicIpConfigurationImpl.ensureConfigurations(this.nicIPConfigurations.values());

        // Reset and update IP configs
        this.inner().withIpConfigurations(innersFromWrappers(this.nicIPConfigurations.values()));
    }
}
