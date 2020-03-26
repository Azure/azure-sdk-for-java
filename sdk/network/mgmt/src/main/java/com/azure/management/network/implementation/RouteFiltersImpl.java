/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.RouteFilter;
import com.azure.management.network.RouteFilters;
import com.azure.management.network.models.RouteFilterInner;
import com.azure.management.network.models.RouteFiltersInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for RouteFilters.
 */
class RouteFiltersImpl
        extends TopLevelModifiableResourcesImpl<
        RouteFilter,
        RouteFilterImpl,
        RouteFilterInner,
        RouteFiltersInner,
        NetworkManager>
        implements RouteFilters {

    RouteFiltersImpl(final NetworkManager networkManager) {
        super(networkManager.inner().routeFilters(), networkManager);
    }

    @Override
    public RouteFilterImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected RouteFilterImpl wrapModel(String name) {
        RouteFilterInner inner = new RouteFilterInner();
        return new RouteFilterImpl(name, inner, super.manager());
    }

    @Override
    protected RouteFilterImpl wrapModel(RouteFilterInner inner) {
        if (inner == null) {
            return null;
        }
        return new RouteFilterImpl(inner.getName(), inner, this.manager());
    }
}

