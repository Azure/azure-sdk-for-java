/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.network.implementation.api.VirtualNetworkInner;
import com.microsoft.azure.management.network.implementation.api.VirtualNetworksInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Internal virtual network implementation of the fluent interface.
 * (Internal use only)
 */
class NetworkImpl
    extends GroupableResourceImpl<Network, VirtualNetworkInner, NetworkImpl>
    implements
        Network,
        Network.Definitions,
        Network.Update {

    private final VirtualNetworksInner client;
    private List<Subnet> subnets;

    NetworkImpl(String name,
            VirtualNetworkInner innerModel,
            final VirtualNetworksInner client,
            final ResourceManager resourceManager) {
        super(name, innerModel, resourceManager);
        this.client = client;
        initializeSubnetsFromInner();
    }

    private void initializeSubnetsFromInner() {
        this.subnets = new ArrayList<>();
        for (SubnetInner subnetInner : this.inner().subnets()) {
            SubnetImpl subnet = new SubnetImpl(subnetInner.name(), subnetInner, this);
            this.subnets.add(subnet);
        }
    }

    // Verbs

    @Override
    public NetworkImpl refresh() throws Exception {
        ServiceResponse<VirtualNetworkInner> response =
            this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeSubnetsFromInner();
        return this;
    }

    @Override
    public NetworkImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<Network> callback) {
        return createAsync(callback);
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
        List<SubnetInner> azureSubnets = new ArrayList<>();
        this.inner().withSubnets(azureSubnets);
        initializeSubnetsFromInner();
        for (Entry<String, String> pair : nameCidrPairs.entrySet()) {
            this.withSubnet(pair.getKey(), pair.getValue());
        }
        return this;
    }

    @Override
    public NetworkImpl withoutSubnet(String name) {
        // Remove from cache
        List<Subnet> s = this.subnets;
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i).name().equalsIgnoreCase(name)) {
                s.remove(i);
                break;
            }
        }

        // Remove from inner
        List<SubnetInner> innerSubnets = this.inner().subnets();
        for (int i = 0; i < innerSubnets.size(); i++) {
            if (innerSubnets.get(i).name().equalsIgnoreCase(name)) {
                innerSubnets.remove(i);
                break;
            }
        }

        return this;
    }

    @Override
    public NetworkImpl withAddressSpace(String cidr) {
        this.inner().addressSpace().addressPrefixes().add(cidr);
        return this;
    }

    @Override
    public SubnetImpl defineSubnet(String name) {
        SubnetInner inner = new SubnetInner();
        inner.withName(name);
        return new SubnetImpl(name, inner, this);
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
    public List<Subnet> subnets() {
        return Collections.unmodifiableList(this.subnets);
    }

    @Override
    protected void createResource() throws Exception {
        // Ensure address spaces
        if (this.addressSpaces().size() == 0) {
            this.withAddressSpace("10.0.0.0/16");
        }

        if (isInCreateMode()) {
            // Create a subnet as needed, covering the entire first address space
            if (this.inner().subnets().size() == 0) {
                this.withSubnet("subnet1", this.addressSpaces().get(0));
            }
        }

        ServiceResponse<VirtualNetworkInner> response =
                this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        initializeSubnetsFromInner();
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback) {
        // Ensure address spaces
        if (this.addressSpaces().size() == 0) {
            this.withAddressSpace("10.0.0.0/16");
        }

        if (isInCreateMode()) {
            // Create a subnet as needed, covering the entire first address space
            if (this.inner().subnets().size() == 0) {
                this.withSubnet("subnet1", this.addressSpaces().get(0));
            }
        }

        return this.client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                Utils.fromVoidCallback(this, new ServiceCallback<Void>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<Void> result) {
                        initializeSubnetsFromInner();
                        callback.success(result);
                    }
                }));
    }
}