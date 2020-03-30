/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.Route;
import com.azure.management.network.RouteNextHopType;
import com.azure.management.network.RouteTable;
import com.azure.management.network.Subnet;
import com.azure.management.network.models.GroupableParentResourceWithTagsImpl;
import com.azure.management.network.models.RouteInner;
import com.azure.management.network.models.RouteTableInner;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for RouteTable.
 */
class RouteTableImpl
        extends GroupableParentResourceWithTagsImpl<
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
    protected Mono<RouteTableInner> applyTagsToInnerAsync() {
        return this.manager().inner().routeTables().updateTagsAsync(resourceGroupName(), name(), inner().getTags());
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
    public Mono<RouteTable> refreshAsync() {
        return super.refreshAsync().map(routeTable -> {
            RouteTableImpl impl = (RouteTableImpl) routeTable;
            impl.initializeChildrenFromInner();
            return impl;
        });
    }

    @Override
    protected Mono<RouteTableInner> getInnerAsync() {
        // FIXME: parameter - expand
        return this.manager().inner().routeTables().getByResourceGroupAsync(this.resourceGroupName(), this.name(), null);
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
    protected Mono<RouteTableInner> createInner() {
        return this.manager().inner().routeTables().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    public Map<String, Route> routes() {
        return Collections.unmodifiableMap(this.routes);
    }

    @Override
    public boolean isBgpRoutePropagationDisabled() {
        return Utils.toPrimitiveBoolean(inner().disableBgpRoutePropagation());
    }

    @Override
    public RouteTableImpl withDisableBgpRoutePropagation() {
        inner().withDisableBgpRoutePropagation(true);
        return this;
    }

    @Override
    public RouteTableImpl withEnableBgpRoutePropagation() {
        inner().withDisableBgpRoutePropagation(false);
        return this;
    }
}
