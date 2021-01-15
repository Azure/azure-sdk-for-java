// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.azure.resourcemanager.trafficmanager.fluent.GeographicHierarchiesClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.TrafficManagerGeographicHierarchyInner;
import com.azure.resourcemanager.trafficmanager.models.GeographicHierarchies;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;

/** Implementation for GeographicHierarchies. */
class GeographicHierarchiesImpl extends WrapperImpl<GeographicHierarchiesClient> implements GeographicHierarchies {
    private final TrafficManager manager;

    protected GeographicHierarchiesImpl(TrafficManager trafficManager, GeographicHierarchiesClient inner) {
        super(inner);
        this.manager = trafficManager;
    }

    @Override
    public TrafficManager manager() {
        return this.manager;
    }

    @Override
    public GeographicLocation getRoot() {
        TrafficManagerGeographicHierarchyInner defaultHierarchy = this.innerModel().getDefault();
        if (defaultHierarchy == null) {
            return null;
        }
        return new GeographicLocationImpl(defaultHierarchy.geographicHierarchy());
    }
}
