// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.RouteInner;
import com.azure.resourcemanager.network.fluent.inner.RouteTableInner;
import com.azure.resourcemanager.network.models.Route;
import com.azure.resourcemanager.network.models.RouteNextHopType;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for RouteTable. */
class RouteTableImpl
    extends GroupableParentResourceWithTagsImpl<RouteTable, RouteTableInner, RouteTableImpl, NetworkManager>
    implements RouteTable, RouteTable.Definition, RouteTable.Update {

    private Map<String, Route> routes;

    RouteTableImpl(String name, final RouteTableInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected Mono<RouteTableInner> applyTagsToInnerAsync() {
        return this.manager().inner().getRouteTables().updateTagsAsync(resourceGroupName(), name(), inner().tags());
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
        return super
            .refreshAsync()
            .map(
                routeTable -> {
                    RouteTableImpl impl = (RouteTableImpl) routeTable;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<RouteTableInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getRouteTables()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public List<Subnet> listAssociatedSubnets() {
        return com.azure.resourcemanager.network.implementation.Utils.listAssociatedSubnets(
            this.myManager, this.inner().subnets()
        );
    }

    // Setters (fluent)

    @Override
    public RouteImpl defineRoute(String name) {
        RouteInner inner = new RouteInner().withName(name);
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
        return this
            .defineRoute("route_" + this.name() + System.currentTimeMillis())
            .withDestinationAddressPrefix(destinationAddressPrefix)
            .withNextHop(nextHop)
            .attach();
    }

    @Override
    public RouteTableImpl withRouteViaVirtualAppliance(String destinationAddressPrefix, String ipAddress) {
        return this
            .defineRoute("route_" + this.name() + System.currentTimeMillis())
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
        return this
            .manager()
            .inner()
            .getRouteTables()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
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
