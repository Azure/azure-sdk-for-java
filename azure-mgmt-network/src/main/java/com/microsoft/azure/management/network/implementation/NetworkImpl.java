/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Implementation for {@link Network} and its create and update interfaces.
 */
class NetworkImpl
    extends GroupableResourceImpl<
        Network,
        VirtualNetworkInner,
        NetworkImpl,
        NetworkManager>
    implements
        Network,
        Network.Definition,
        Network.Update {

    private final VirtualNetworksInner innerCollection;
    private final Map<String, Subnet> subnets = new TreeMap<>();

    NetworkImpl(String name,
            final VirtualNetworkInner innerModel,
            final VirtualNetworksInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
        initializeSubnetsFromInner();
    }

    private void initializeSubnetsFromInner() {
        this.subnets.clear();
        List<SubnetInner> inners = this.inner().subnets();
        if (inners != null) {
            for (SubnetInner inner : inners) {
                SubnetImpl subnet = new SubnetImpl(inner, this);
                this.subnets.put(inner.name(), subnet);
            }
        }
    }

    // Verbs

    @Override
    public NetworkImpl refresh() throws Exception {
        ServiceResponse<VirtualNetworkInner> response =
            this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeSubnetsFromInner();
        return this;
    }

    @Override
    public Observable<Network> applyAsync() {
        return createAsync();
    }

    // Helpers

    NetworkImpl withSubnet(SubnetImpl subnet) {
        this.subnets.put(subnet.name(), subnet);
        return this;
    }

    NetworkManager myManager() {
        return super.myManager;
    }

    // Setters (fluent)

    @Override
    public NetworkImpl withDnsServer(String ipAddress) {
        this.inner().dhcpOptions().dnsServers().add(ipAddress);
        return this;
    }

    @Override
    public NetworkImpl withSubnet(String name, String cidr) {
        return this.defineSubnet(name)
            .withAddressPrefix(cidr)
            .attach();
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
        this.inner().addressSpace().addressPrefixes().add(cidr);
        return this;
    }

    @Override
    public SubnetImpl defineSubnet(String name) {
        SubnetInner inner = new SubnetInner()
                .withName(name);
        return new SubnetImpl(inner, this);
    }

    // Getters

    @Override
    public List<String> addressSpaces() {
        return Collections.unmodifiableList(this.inner().addressSpace().addressPrefixes());
    }

    @Override
    public List<String> dnsServerIPs() {
        return Collections.unmodifiableList(this.inner().dhcpOptions().dnsServers());
    }

    @Override
    public Map<String, Subnet> subnets() {
        return Collections.unmodifiableMap(this.subnets);
    }

    private void beforeCreating() {
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
    public Observable<Network> createResourceAsync() {
        final NetworkImpl self = this;
        beforeCreating();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(new Func1<ServiceResponse<VirtualNetworkInner>, Network>() {
                    @Override
                    public Network call(ServiceResponse<VirtualNetworkInner> virtualNetworkInner) {
                        setInner(virtualNetworkInner.getBody());
                        initializeSubnetsFromInner();
                        return self;
                    }
                });
    }
}