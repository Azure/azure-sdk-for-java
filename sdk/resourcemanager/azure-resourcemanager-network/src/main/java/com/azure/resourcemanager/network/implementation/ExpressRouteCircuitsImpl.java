// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCircuitsClient;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuit;
import com.azure.resourcemanager.network.models.ExpressRouteCircuits;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public class ExpressRouteCircuitsImpl
    extends TopLevelModifiableResourcesImpl<
        ExpressRouteCircuit,
        ExpressRouteCircuitImpl,
        ExpressRouteCircuitInner,
        ExpressRouteCircuitsClient,
        NetworkManager>
    implements ExpressRouteCircuits {

    public ExpressRouteCircuitsImpl(NetworkManager manager) {
        super(manager.serviceClient().getExpressRouteCircuits(), manager);
    }

    @Override
    protected ExpressRouteCircuitImpl wrapModel(String name) {
        ExpressRouteCircuitInner inner = new ExpressRouteCircuitInner();

        return new ExpressRouteCircuitImpl(name, inner, super.manager());
    }

    @Override
    protected ExpressRouteCircuitImpl wrapModel(ExpressRouteCircuitInner inner) {
        if (inner == null) {
            return null;
        }
        return new ExpressRouteCircuitImpl(inner.name(), inner, this.manager());
    }

    @Override
    public ExpressRouteCircuitImpl define(String name) {
        return wrapModel(name);
    }
}
