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
import com.microsoft.azure.management.network.implementation.api.AddressSpace;
import com.microsoft.azure.management.network.implementation.api.DhcpOptions;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.network.implementation.api.VirtualNetworkInner;
import com.microsoft.azure.management.network.implementation.api.VirtualNetworksInner;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Implementation of the Networks interface.
 * (Internal use only)
 */
class NetworksImpl implements Networks {
    private final VirtualNetworksInner client;
    private final ResourceManager resourceManager;
    private final PagedListConverter<VirtualNetworkInner, Network> converter;

    NetworksImpl(final VirtualNetworksInner client, final ResourceManager resourceManager) {
        this.client = client;
        this.resourceManager = resourceManager;
        this.converter = new PagedListConverter<VirtualNetworkInner, Network>() {
            @Override
            public Network typeConvert(VirtualNetworkInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<Network> list() throws CloudException, IOException {
        ServiceResponse<PagedList<VirtualNetworkInner>> response = client.listAll();
        return converter.convert(response.getBody());
    }

    @Override
    public PagedList<Network> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<PagedList<VirtualNetworkInner>> response = client.list(groupName);
        return converter.convert(response.getBody());
    }

    @Override
    public NetworkImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<VirtualNetworkInner> serviceResponse = this.client.get(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    @Override
    public NetworkImpl define(String name) {
        return createFluentModel(name);
    }

    // Fluent model create helpers

    private NetworkImpl createFluentModel(String name) {
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

        return new NetworkImpl(name, inner, this.client, this.resourceManager);
    }

    private NetworkImpl createFluentModel(VirtualNetworkInner inner) {
        return new NetworkImpl(inner.name(), inner, this.client, this.resourceManager);
    }
}
