/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.RouteTables;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;

/**
 *  Implementation for RouteTables.
 */
@LangDefinition
class RouteTablesImpl
        extends GroupableResourcesImpl<
            RouteTable,
            RouteTableImpl,
            RouteTableInner,
            RouteTablesInner,
            NetworkManager>
        implements RouteTables {

    RouteTablesImpl(
            final NetworkManagementClientImpl networkClient,
            final NetworkManager networkManager) {
        super(networkClient.routeTables(), networkManager);
    }

    @Override
    public PagedList<RouteTable> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<RouteTable> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public RouteTableImpl getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public RouteTableImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected RouteTableImpl wrapModel(String name) {
        RouteTableInner inner = new RouteTableInner();

        return new RouteTableImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected RouteTableImpl wrapModel(RouteTableInner inner) {
        return new RouteTableImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
