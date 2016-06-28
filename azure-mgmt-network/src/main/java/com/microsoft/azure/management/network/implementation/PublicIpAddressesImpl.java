/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 *  Implementation for {@link PublicIpAddresses}.
 */
class PublicIpAddressesImpl
        extends GroupableResourcesImpl<
            PublicIpAddress,
            PublicIpAddressImpl,
            PublicIPAddressInner,
            PublicIPAddressesInner,
            NetworkManager>
        implements PublicIpAddresses {

    PublicIpAddressesImpl(
            final PublicIPAddressesInner client,
            final NetworkManager networkManager) {
        super(client, networkManager);
    }

    @Override
    public PagedList<PublicIpAddress> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.listAll().getBody());
    }

    @Override
    public PagedList<PublicIpAddress> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName).getBody());
    }

    @Override
    public PublicIpAddressImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name).getBody());
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
    public PublicIpAddressImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected PublicIpAddressImpl wrapModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIPAddressDnsSettings());
        }

        return new PublicIpAddressImpl(
                name,
                inner,
                this.innerCollection,
                this.myManager);
    }

    @Override
    protected PublicIpAddressImpl wrapModel(PublicIPAddressInner inner) {
        return new PublicIpAddressImpl(
                inner.id(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
