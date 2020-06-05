// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.RouteTable;
import com.azure.resourcemanager.network.RouteTables;
import com.azure.resourcemanager.network.models.RouteTableInner;
import com.azure.resourcemanager.network.models.RouteTablesInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for RouteTables. */
class RouteTablesImpl
    extends TopLevelModifiableResourcesImpl<
        RouteTable, RouteTableImpl, RouteTableInner, RouteTablesInner, NetworkManager>
    implements RouteTables {

    RouteTablesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().routeTables(), networkManager);
    }

    @Override
    public RouteTableImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected RouteTableImpl wrapModel(String name) {
        RouteTableInner inner = new RouteTableInner();

        return new RouteTableImpl(name, inner, this.manager());
    }

    @Override
    protected RouteTableImpl wrapModel(RouteTableInner inner) {
        return new RouteTableImpl(inner.name(), inner, this.manager());
    }
}
