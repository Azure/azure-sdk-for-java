// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RouteInner;
import com.azure.resourcemanager.cdn.models.AfdEndpoint;
import com.azure.resourcemanager.cdn.models.Route;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of routes associated with an AFD endpoint.
 */
class RoutesImpl
    extends ExternalChildResourcesNonCachedImpl<RouteImpl, Route, RouteInner, AfdEndpointImpl, AfdEndpoint> {

    RoutesImpl(AfdEndpointImpl parent) {
        super(parent, parent.taskGroup(), "Route");
    }

    Map<String, Route> routesAsMap() {
        Map<String, Route> result = new HashMap<>();
        for (RouteInner inner : this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .listByEndpoint(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name())) {
            RouteImpl route = new RouteImpl(inner.name(), this.getParent(), inner);
            result.put(route.name(), route);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new RouteImpl(name, getParent(), new RouteInner()));
    }

    void addRoute(RouteImpl route) {
        this.childCollection.put(route.name(), route);
    }

    RouteImpl defineNewRoute(String name) {
        return this.prepareInlineDefine(new RouteImpl(name, this.getParent(), new RouteInner()));
    }

    RouteImpl updateRoute(String name) {
        RouteInner inner = this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .get(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name(), name);
        return this.prepareInlineUpdate(new RouteImpl(name, this.getParent(), inner));
    }
}
