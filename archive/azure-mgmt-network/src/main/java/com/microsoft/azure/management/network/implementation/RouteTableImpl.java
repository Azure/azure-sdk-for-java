/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Route;
import com.microsoft.azure.management.network.RouteNextHopType;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for RouteTable.
 */
@LangDefinition
class RouteTableImpl
    extends GroupableParentResourceImpl<
        RouteTable,
        RouteTableInner,
        RouteTableImpl,
        NetworkManager>
    implements
        RouteTable,
        RouteTable.Definition,
        RouteTable.Update {

    private Map<String, Route> routes;

    RouteTableImpl(String name,
            final RouteTableInner innerModel,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.routes = new TreeMap<>();
        List<RouteInner> inners = this.inner().routes();
        if (inners != null) {
            for (RouteInner inner : inners) {
                RouteImpl route = new RouteImpl(inner, this);
                this.routes.put(inner.name(), route);
            }
        }
    }

    // Getters

    // Verbs

    @Override
    public Observable<RouteTable> refreshAsync() {
        return super.refreshAsync().map(new Func1<RouteTable, RouteTable>() {
            @Override
            public RouteTable call(RouteTable routeTable) {
                RouteTableImpl impl = (RouteTableImpl) routeTable;
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<RouteTableInner> getInnerAsync() {
        return this.manager().inner().routeTables().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public List<Subnet> listAssociatedSubnets() {
        return this.myManager.listAssociatedSubnets(this.inner().subnets());
    }

    // Setters (fluent)

    @Override
    public RouteImpl defineRoute(String name) {
        RouteInner inner = new RouteInner()
                .withName(name);
        return new RouteImpl(inner, this);
    }

    @Override
    public RouteImpl updateRoute(String name) {
        return (RouteImpl) this.routes.get(name);
    }

    @Override
    public Update withoutRoute(String name) {
        this.routes.remove(name);
        return this;
    }

    @Override
    public RouteTableImpl withRoute(String destinationAddressPrefix, RouteNextHopType nextHop) {
        return this.defineRoute("route_" + this.name() + System.currentTimeMillis())
                .withDestinationAddressPrefix(destinationAddressPrefix)
                .withNextHop(nextHop)
                .attach();
    }

    @Override
    public RouteTableImpl withRouteViaVirtualAppliance(String destinationAddressPrefix, String ipAddress) {
        return this.defineRoute("route_" + this.name() + System.currentTimeMillis())
                .withDestinationAddressPrefix(destinationAddressPrefix)
                .withNextHopToVirtualAppliance(ipAddress)
                .attach();
    }

    RouteTableImpl withRoute(RouteImpl route) {
        this.routes.put(route.name(), route);
        return this;
    }

    // Helpers

    @Override
    protected void beforeCreating() {
        // Reset and update routes
        this.inner().withRoutes(innersFromWrappers(this.routes.values()));
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected Observable<RouteTableInner> createInner() {
        return this.manager().inner().routeTables().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    public Map<String, Route> routes() {
        return Collections.unmodifiableMap(this.routes);
    }
}
