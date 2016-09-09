/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.AddressSpace;
import com.microsoft.azure.management.network.DhcpOptions;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;
import java.util.ArrayList;

/**
 *  Implementation for {@link Networks}.
 */
class NetworksImpl
        extends GroupableResourcesImpl<
            Network,
            NetworkImpl,
            VirtualNetworkInner,
            VirtualNetworksInner,
            NetworkManager>
        implements Networks {

    NetworksImpl(
            final NetworkManagementClientImpl networkClient,
            final NetworkManager networkManager) {
        super(networkClient.virtualNetworks(), networkManager);
    }

    @Override
    public PagedList<Network> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<Network> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public NetworkImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public NetworkImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkImpl wrapModel(String name) {
        VirtualNetworkInner inner = new VirtualNetworkInner();

        // Initialize address space
        AddressSpace addressSpace = inner.addressSpace();
        if (addressSpace == null) {
            addressSpace = new AddressSpace();
            inner.withAddressSpace(addressSpace);
        }

        if (addressSpace.addressPrefixes() == null) {
            addressSpace.withAddressPrefixes(new ArrayList<String>());
        }

        // Initialize subnets
        if (inner.subnets() == null) {
            inner.withSubnets(new ArrayList<SubnetInner>());
        }

        // Initialize DHCP options (DNS servers)
        DhcpOptions dhcp = inner.dhcpOptions();
        if (dhcp == null) {
            dhcp = new DhcpOptions();
            inner.withDhcpOptions(dhcp);
        }

        if (dhcp.dnsServers() == null) {
            dhcp.withDnsServers(new ArrayList<String>());
        }

        return new NetworkImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected NetworkImpl wrapModel(VirtualNetworkInner inner) {
        return new NetworkImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
