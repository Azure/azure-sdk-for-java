/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ListableGroupableResourcesPageImpl;
import rx.Completable;
import rx.Observable;

/**
 *  Implementation for {@link PublicIPAddresses}.
 */
@LangDefinition
class PublicIPAddressesImpl
        extends ListableGroupableResourcesPageImpl<
            PublicIPAddress,
            PublicIPAddressImpl,
            PublicIPAddressInner,
            PublicIPAddressesInner,
            NetworkManager>
        implements PublicIPAddresses {

    PublicIPAddressesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().publicIPAddresses(), networkManager);
    }

    @Override
    public PagedList<PublicIPAddress> list() {
        return wrapList(this.inner().listAll());
    }

    @Override
    public PagedList<PublicIPAddress> listByGroup(String groupName) {
        return wrapList(this.inner().list(groupName));
    }

    @Override
    public PublicIPAddressImpl getByGroup(String groupName, String name) {
        return wrapModel(this.inner().get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public PublicIPAddressImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected PublicIPAddressImpl wrapModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIPAddressDnsSettings());
        }

        return new PublicIPAddressImpl(name, inner, this.manager());
    }

    @Override
    protected PublicIPAddressImpl wrapModel(PublicIPAddressInner inner) {
        if (inner == null) {
            return null;
        }
        return new PublicIPAddressImpl(inner.id(), inner, this.manager());
    }

    @Override
    protected Observable<Page<PublicIPAddressInner>> listAsyncPage() {
        return inner().listAllAsync();
    }

    @Override
    protected Observable<Page<PublicIPAddressInner>> listByGroupAsyncPage(String resourceGroupName) {
        return inner().listAsync(resourceGroupName);
    }
}
