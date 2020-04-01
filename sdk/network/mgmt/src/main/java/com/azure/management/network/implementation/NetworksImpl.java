/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.AddressSpace;
import com.azure.management.network.DhcpOptions;
import com.azure.management.network.Network;
import com.azure.management.network.Networks;
import com.azure.management.network.models.SubnetInner;
import com.azure.management.network.models.VirtualNetworkInner;
import com.azure.management.network.models.VirtualNetworksInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

import java.util.ArrayList;

/**
 * Implementation for Networks.
 */
class NetworksImpl
        extends TopLevelModifiableResourcesImpl<
        Network,
        NetworkImpl,
        VirtualNetworkInner,
        VirtualNetworksInner,
        NetworkManager>
        implements Networks {

    NetworksImpl(final NetworkManager networkManager) {
        super(networkManager.inner().virtualNetworks(), networkManager);
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

        return new NetworkImpl(name, inner, super.manager());
    }

    @Override
    protected NetworkImpl wrapModel(VirtualNetworkInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkImpl(inner.getName(), inner, this.manager());
    }
}
