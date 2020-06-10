// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.SubResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.DdosProtectionPlan;
import com.azure.resourcemanager.network.models.DhcpOptions;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkPeerings;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.fluent.inner.IpAddressAvailabilityResultInner;
import com.azure.resourcemanager.network.fluent.inner.SubnetInner;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkInner;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for Network and its create and update interfaces. */
class NetworkImpl extends GroupableParentResourceWithTagsImpl<Network, VirtualNetworkInner, NetworkImpl, NetworkManager>
    implements Network, Network.Definition, Network.Update {

    private final ClientLogger logger = new ClientLogger(getClass());
    private Map<String, Subnet> subnets;
    private NetworkPeeringsImpl peerings;
    private Creatable<DdosProtectionPlan> ddosProtectionPlanCreatable;

    NetworkImpl(String name, final VirtualNetworkInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected void initializeChildrenFromInner() {
        // Initialize subnets
        this.subnets = new TreeMap<>();
        List<SubnetInner> inners = this.inner().subnets();
        if (inners != null) {
            for (SubnetInner inner : inners) {
                SubnetImpl subnet = new SubnetImpl(inner, this);
                this.subnets.put(inner.name(), subnet);
            }
        }

        this.peerings = new NetworkPeeringsImpl(this);
    }

    // Verbs

    @Override
    public Mono<Network> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                network -> {
                    NetworkImpl impl = (NetworkImpl) network;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<VirtualNetworkInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getVirtualNetworks()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<VirtualNetworkInner> applyTagsToInnerAsync() {
        return this.manager().inner().getVirtualNetworks().updateTagsAsync(resourceGroupName(), name(), inner().tags());
    }

    @Override
    public boolean isPrivateIPAddressAvailable(String ipAddress) {
        IpAddressAvailabilityResultInner result = checkIPAvailability(ipAddress);
        return (result != null) ? result.available() : false;
    }

    @Override
    public boolean isPrivateIPAddressInNetwork(String ipAddress) {
        IpAddressAvailabilityResultInner result = checkIPAvailability(ipAddress);
        return (result != null) ? true : false;
    }

    // Helpers

    private IpAddressAvailabilityResultInner checkIPAvailability(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        IpAddressAvailabilityResultInner result = null;
        try {
            result =
                this
                    .manager()
                    .networks()
                    .inner()
                    .checkIpAddressAvailability(this.resourceGroupName(), this.name(), ipAddress);
        } catch (ManagementException e) {
            if (!e.getValue().getCode().equalsIgnoreCase("PrivateIPAddressNotInAnySubnet")) {
                throw logger.logExceptionAsError(e);
                // Rethrow if the exception reason is anything other than IP address not found
            }
        }
        return result;
    }

    NetworkImpl withSubnet(SubnetImpl subnet) {
        this.subnets.put(subnet.name(), subnet);
        return this;
    }

    // Setters (fluent)

    @Override
    public NetworkImpl withDnsServer(String ipAddress) {
        if (this.inner().dhcpOptions() == null) {
            this.inner().withDhcpOptions(new DhcpOptions());
        }

        if (this.inner().dhcpOptions().dnsServers() == null) {
            this.inner().dhcpOptions().withDnsServers(new ArrayList<String>());
        }

        this.inner().dhcpOptions().dnsServers().add(ipAddress);
        return this;
    }

    @Override
    public NetworkImpl withSubnet(String name, String cidr) {
        return this.defineSubnet(name).withAddressPrefix(cidr).attach();
    }

    @Override
    public NetworkImpl withSubnets(Map<String, String> nameCidrPairs) {
        this.subnets.clear();
        for (Entry<String, String> pair : nameCidrPairs.entrySet()) {
            this.withSubnet(pair.getKey(), pair.getValue());
        }
        return this;
    }

    @Override
    public NetworkImpl withoutSubnet(String name) {
        this.subnets.remove(name);
        return this;
    }

    @Override
    public NetworkImpl withAddressSpace(String cidr) {
        if (this.inner().addressSpace() == null) {
            this.inner().withAddressSpace(new AddressSpace());
        }

        if (this.inner().addressSpace().addressPrefixes() == null) {
            this.inner().addressSpace().withAddressPrefixes(new ArrayList<String>());
        }

        this.inner().addressSpace().addressPrefixes().add(cidr);
        return this;
    }

    @Override
    public SubnetImpl defineSubnet(String name) {
        SubnetInner inner = new SubnetInner().withName(name);
        return new SubnetImpl(inner, this);
    }

    @Override
    public NetworkImpl withoutAddressSpace(String cidr) {
        if (cidr != null && this.inner().addressSpace() != null
            && this.inner().addressSpace().addressPrefixes() != null) {
            this.inner().addressSpace().addressPrefixes().remove(cidr);
        }
        return this;
    }

    // Getters

    @Override
    public List<String> addressSpaces() {
        List<String> addressSpaces = new ArrayList<String>();
        if (this.inner().addressSpace() == null) {
            return Collections.unmodifiableList(addressSpaces);
        } else if (this.inner().addressSpace().addressPrefixes() == null) {
            return Collections.unmodifiableList(addressSpaces);
        } else {
            return Collections.unmodifiableList(this.inner().addressSpace().addressPrefixes());
        }
    }

    @Override
    public List<String> dnsServerIPs() {
        List<String> ips = new ArrayList<String>();
        if (this.inner().dhcpOptions() == null) {
            return Collections.unmodifiableList(ips);
        } else if (this.inner().dhcpOptions().dnsServers() == null) {
            return Collections.unmodifiableList(ips);
        } else {
            return this.inner().dhcpOptions().dnsServers();
        }
    }

    @Override
    public Map<String, Subnet> subnets() {
        return Collections.unmodifiableMap(this.subnets);
    }

    @Override
    protected void beforeCreating() {
        // Ensure address spaces
        if (this.addressSpaces().size() == 0) {
            this.withAddressSpace("10.0.0.0/16");
        }

        if (isInCreateMode()) {
            // Create a subnet as needed, covering the entire first address space
            if (this.subnets.size() == 0) {
                this.withSubnet("subnet1", this.addressSpaces().get(0));
            }
        }

        // Reset and update subnets
        this.inner().withSubnets(innersFromWrappers(this.subnets.values()));
    }

    @Override
    public SubnetImpl updateSubnet(String name) {
        return (SubnetImpl) this.subnets.get(name);
    }

    @Override
    protected Mono<VirtualNetworkInner> createInner() {
        if (ddosProtectionPlanCreatable != null && this.taskResult(ddosProtectionPlanCreatable.key()) != null) {
            DdosProtectionPlan ddosProtectionPlan =
                this.<DdosProtectionPlan>taskResult(ddosProtectionPlanCreatable.key());
            withExistingDdosProtectionPlan(ddosProtectionPlan.id());
        }
        return this
            .manager()
            .inner()
            .getVirtualNetworks()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(
                virtualNetworkInner -> {
                    NetworkImpl.this.ddosProtectionPlanCreatable = null;
                    return virtualNetworkInner;
                });
    }

    @Override
    public NetworkPeerings peerings() {
        return this.peerings;
    }

    @Override
    public boolean isDdosProtectionEnabled() {
        return Utils.toPrimitiveBoolean(inner().enableDdosProtection());
    }

    @Override
    public boolean isVmProtectionEnabled() {
        return Utils.toPrimitiveBoolean(inner().enableVmProtection());
    }

    @Override
    public String ddosProtectionPlanId() {
        return inner().ddosProtectionPlan() == null ? null : inner().ddosProtectionPlan().id();
    }

    @Override
    public NetworkImpl withNewDdosProtectionPlan() {
        inner().withEnableDdosProtection(true);
        DdosProtectionPlan.DefinitionStages.WithGroup ddosProtectionPlanWithGroup =
            manager()
                .ddosProtectionPlans()
                .define(this.manager().sdkContext().randomResourceName(name(), 20))
                .withRegion(region());
        if (super.creatableGroup != null && isInCreateMode()) {
            ddosProtectionPlanCreatable = ddosProtectionPlanWithGroup.withNewResourceGroup(super.creatableGroup);
        } else {
            ddosProtectionPlanCreatable = ddosProtectionPlanWithGroup.withExistingResourceGroup(resourceGroupName());
        }
        this.addDependency(ddosProtectionPlanCreatable);
        return this;
    }

    @Override
    public NetworkImpl withExistingDdosProtectionPlan(String planId) {
        inner().withEnableDdosProtection(true).withDdosProtectionPlan(new SubResource().withId(planId));
        return this;
    }

    @Override
    public NetworkImpl withoutDdosProtectionPlan() {
        inner().withEnableDdosProtection(false).withDdosProtectionPlan(null);
        return this;
    }

    @Override
    public NetworkImpl withVmProtection() {
        inner().withEnableVmProtection(true);
        return this;
    }

    @Override
    public NetworkImpl withoutVmProtection() {
        inner().withEnableVmProtection(false);
        return this;
    }
}
