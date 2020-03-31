/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.ExpressRouteCircuit;
import com.azure.management.network.ExpressRouteCircuits;
import com.azure.management.network.models.ExpressRouteCircuitInner;
import com.azure.management.network.models.ExpressRouteCircuitsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

class ExpressRouteCircuitsImpl extends TopLevelModifiableResourcesImpl<
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
        return new ExpressRouteCircuitImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public ExpressRouteCircuitImpl define(String name) {
        return wrapModel(name);
    }
}
