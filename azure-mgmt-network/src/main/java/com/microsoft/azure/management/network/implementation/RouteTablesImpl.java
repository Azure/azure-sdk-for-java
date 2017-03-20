/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.RouteTables;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for RouteTables.
 */
@LangDefinition
class RouteTablesImpl
    extends TopLevelModifiableResourcesImpl<
        RouteTable,
        RouteTableImpl,
        RouteTableInner,
        RouteTablesInner,
        NetworkManager>
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
