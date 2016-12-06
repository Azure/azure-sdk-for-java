/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

/**
 *  Implementation for {@link PublicIpAddresses}.
 */
@LangDefinition
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
    public PagedList<PublicIpAddress> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<PublicIpAddress> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public PublicIpAddressImpl getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
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
        if (inner == null) {
            return null;
        }
        return new PublicIpAddressImpl(
                inner.id(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
