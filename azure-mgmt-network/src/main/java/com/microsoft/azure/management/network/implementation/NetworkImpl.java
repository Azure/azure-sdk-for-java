/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
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
    private TreeMap<String, Subnet> subnets;

    NetworkImpl(String name,
            final VirtualNetworkInner innerModel,
            final VirtualNetworksInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
        initializeSubnetsFromInner();
    }

    private void initializeSubnetsFromInner() {
        this.subnets = new TreeMap<>();
        for (SubnetInner subnetInner : this.inner().subnets()) {
            SubnetImpl subnet = new SubnetImpl(subnetInner.name(), subnetInner, this);
            this.subnets.put(subnetInner.name(), subnet);
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
    public NetworkImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<Network> callback) {
        return createAsync(callback);
    }

    // Helpers

    NetworkImpl withSubnet(SubnetImpl subnet) {
        this.inner().subnets().add(subnet.inner());
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
        this.subnets.remove(name);

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
    public Map<String, Subnet> subnets() {
        return Collections.unmodifiableMap(this.subnets);
    }

    private void ensureCreationPrerequisites() {
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
    }

    @Override
    protected void createResource() throws Exception {
        ensureCreationPrerequisites();

        ServiceResponse<VirtualNetworkInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        initializeSubnetsFromInner();
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback) {
        ensureCreationPrerequisites();

        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
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

    @Override
    public SubnetImpl updateSubnet(String name) {
        return (SubnetImpl) this.subnets.get(name);
    }
}