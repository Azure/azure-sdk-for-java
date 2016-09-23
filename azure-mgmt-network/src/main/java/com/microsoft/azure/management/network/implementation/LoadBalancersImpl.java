/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

/**
 *  Implementation for {@link LoadBalancers}.
 */
@LangDefinition
class LoadBalancersImpl
        extends GroupableResourcesImpl<
            LoadBalancer,
            LoadBalancerImpl,
            LoadBalancerInner,
            LoadBalancersInner,
            NetworkManager>
        implements LoadBalancers {

    LoadBalancersImpl(
            final NetworkManagementClientImpl networkClient,
            final NetworkManager networkManager) {
        super(networkClient.loadBalancers(), networkManager);
    }

    @Override
    public PagedList<LoadBalancer> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<LoadBalancer> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public LoadBalancerImpl getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    public LoadBalancerImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected LoadBalancerImpl wrapModel(String name) {
        LoadBalancerInner inner = new LoadBalancerInner();
        return new LoadBalancerImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected LoadBalancerImpl wrapModel(LoadBalancerInner inner) {
        return new LoadBalancerImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
