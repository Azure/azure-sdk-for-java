// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.RouteTablesClient;
import com.azure.resourcemanager.network.fluent.inner.RouteTableInner;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.RouteTables;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for RouteTables. */
public class RouteTablesImpl
    extends TopLevelModifiableResourcesImpl<
        RouteTable, RouteTableImpl, RouteTableInner, RouteTablesClient, NetworkManager>
    implements RouteTables {

    public RouteTablesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getRouteTables(), networkManager);
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
