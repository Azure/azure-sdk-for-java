/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 *  Implementation for {@link LoadBalancers}.
 */
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
    public PagedList<LoadBalancer> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<LoadBalancer> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public LoadBalancerImpl getByGroup(String groupName, String name) throws CloudException, IOException {
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
