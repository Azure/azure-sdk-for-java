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
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

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
    private NicIPConfigurationImpl nicPrimaryIPConfiguration;
    /**
     * references to all ip configuration.
     */
    private Map<String, NicIPConfiguration> nicIPConfigurations;
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
                         final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.nicName = name;
        this.namer = SdkContext.getResourceNamerFactory().createResourceNamer(this.nicName);
        initializeChildrenFromInner();
    }

    // Verbs

    @Override
    public Observable<NetworkInterface> refreshAsync() {
        return super.refreshAsync().map(new Func1<NetworkInterface, NetworkInterface>() {
            @Override
            public NetworkInterface call(NetworkInterface networkInterface) {
                NetworkInterfaceImpl impl = (NetworkInterfaceImpl) networkInterface;
                impl.clearCachedRelatedResources();
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<NetworkInterfaceInner> getInnerAsync() {
        return this.manager().inner().networkInterfaces().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Setters (fluent)

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
    public NetworkInterfaceImpl withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName) {
        this.primaryIPConfiguration().withExistingLoadBalancerInboundNatRule(loadBalancer, inboundNatRuleName);
        return this;
    }

    @Override
    public Update withoutLoadBalancerBackends() {
        for (NicIPConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name())
                .withoutLoadBalancerBackends();
        }
        return this;
    }

    @Override
    public Update withoutLoadBalancerInboundNatRules() {
        for (NicIPConfiguration ipConfig : this.ipConfigurations().values()) {
            this.updateIPConfiguration(ipConfig.name())
                .withoutLoadBalancerInboundNatRules();
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

    //TODO: Networking doesn't support this yet, even though it exposes the API; so we have the impl but not exposed via the interface yet.
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
            this.networkSecurityGroup = super.myManager
                    .networkSecurityGroups()
                    .getByResourceGroup(ResourceUtils.groupFromResourceId(id),
                    ResourceUtils.nameFromResourceId(id));
        }
        return this.networkSecurityGroup;
    }

    /**
     * @return the primary IP configuration of the network interface
     */
    public NicIPConfigurationImpl primaryIPConfiguration() {
        if (this.nicPrimaryIPConfiguration != null) {
            return this.nicPrimaryIPConfiguration;
        }

        if (isInCreateMode()) {
            this.nicPrimaryIPConfiguration = prepareNewNicIPConfiguration("primary");
            withIPConfiguration(this.nicPrimaryIPConfiguration);
        } else {
            // TODO: Currently Azure supports only one IP configuration and that is the primary
            // hence we pick the first one here.
            // when Azure support multiple IP configurations then there will be a flag in
            // the IPConfiguration or a property in the network interface to identify the
            // primary so below logic will be changed.
            this.nicPrimaryIPConfiguration = (NicIPConfigurationImpl) new ArrayList<NicIPConfiguration>(
                    this.nicIPConfigurations.values()).get(0);
        }
        return this.nicPrimaryIPConfiguration;
    }

    /**
     * @return the list of DNS server IPs from the DNS settings
     */
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
                NicIPConfigurationImpl  nicIPConfiguration = new NicIPConfigurationImpl(inner, this, super.myManager, false);
                this.nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
            }
        }
    }

    /**
     * Gets a new IP configuration child resource {@link NicIPConfiguration} wrapping {@link NetworkInterfaceIPConfigurationInner}.
     *
     * @param name the name for the new ip configuration
     * @return {@link NicIPConfiguration}
     */
    private NicIPConfigurationImpl prepareNewNicIPConfiguration(String name) {
        NicIPConfigurationImpl nicIPConfiguration = NicIPConfigurationImpl.prepareNicIPConfiguration(
                name,
                this,
                super.myManager
        );
        return nicIPConfiguration;
    }

    private void clearCachedRelatedResources() {
        this.networkSecurityGroup = null;
        this.nicPrimaryIPConfiguration = null;
    }

    NetworkInterfaceImpl withIPConfiguration(NicIPConfigurationImpl nicIPConfiguration) {
        this.nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
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
        return this.manager().inner().networkInterfaces().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
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

        NicIPConfigurationImpl.ensureConfigurations(this.nicIPConfigurations.values());

        // Reset and update IP configs
        this.inner().withIpConfigurations(innersFromWrappers(this.nicIPConfigurations.values()));
    }
}