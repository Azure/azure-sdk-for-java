/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for {@link LoadBalancers}.
 */
@LangDefinition
class LoadBalancersImpl
    extends TopLevelModifiableResourcesImpl<
        LoadBalancer,
        LoadBalancerImpl,
        LoadBalancerInner,
        LoadBalancersInner,
        NetworkManager>
    implements LoadBalancers {

    LoadBalancersImpl(final NetworkManager networkManager) {
        super(networkManager.inner().loadBalancers(), networkManager);
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
                this.manager());
    }

    @Override
    protected LoadBalancerImpl wrapModel(LoadBalancerInner inner) {
        if (inner == null) {
            return null;
        }
        return new LoadBalancerImpl(
                inner.name(),
                inner,
                this.manager());
    }
}
