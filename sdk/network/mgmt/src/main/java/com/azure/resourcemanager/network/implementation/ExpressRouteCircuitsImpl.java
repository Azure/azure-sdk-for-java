// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.ExpressRouteCircuit;
import com.azure.resourcemanager.network.ExpressRouteCircuits;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

class ExpressRouteCircuitsImpl
    extends TopLevelModifiableResourcesImpl<
        ExpressRouteCircuit,
        ExpressRouteCircuitImpl,
        ExpressRouteCircuitInner,
        ExpressRouteCircuitsInner,
        NetworkManager>
    implements ExpressRouteCircuits {

    ExpressRouteCircuitsImpl(NetworkManager manager) {
        super(manager.inner().expressRouteCircuits(), manager);
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
