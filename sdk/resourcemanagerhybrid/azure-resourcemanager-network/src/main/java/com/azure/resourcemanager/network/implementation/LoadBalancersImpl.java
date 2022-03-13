// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancers;
import com.azure.resourcemanager.network.fluent.models.LoadBalancerInner;
import com.azure.resourcemanager.network.fluent.LoadBalancersClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link LoadBalancers}. */
public class LoadBalancersImpl
    extends TopLevelModifiableResourcesImpl<
        LoadBalancer, LoadBalancerImpl, LoadBalancerInner, LoadBalancersClient, NetworkManager>
    implements LoadBalancers {

    public LoadBalancersImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getLoadBalancers(), networkManager);
    }

    @Override
    public LoadBalancerImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected LoadBalancerImpl wrapModel(String name) {
        LoadBalancerInner inner = new LoadBalancerInner();
        return new LoadBalancerImpl(name, inner, this.manager());
    }

    @Override
    protected LoadBalancerImpl wrapModel(LoadBalancerInner inner) {
        if (inner == null) {
            return null;
        }
        return new LoadBalancerImpl(inner.name(), inner, this.manager());
    }
}
