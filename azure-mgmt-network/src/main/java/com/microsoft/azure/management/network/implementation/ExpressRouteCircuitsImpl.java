/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuits;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

@LangDefinition
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
        return new ExpressRouteCircuitImpl(inner.name(), inner, this.manager());
    }

    @Override
    public ExpressRouteCircuitImpl define(String name) {
        return wrapModel(name);
    }
}
