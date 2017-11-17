/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.trafficmanager.GeographicHierarchies;
import com.microsoft.azure.management.trafficmanager.GeographicLocation;

/**
 * Implementation for GeographicHierarchies.
 */
@LangDefinition
class GeographicHierarchiesImpl extends WrapperImpl<GeographicHierarchiesInner> implements GeographicHierarchies {
    private final TrafficManager manager;

    protected GeographicHierarchiesImpl(TrafficManager trafficManager, GeographicHierarchiesInner inner) {
        super(inner);
        this.manager = trafficManager;
    }

    @Override
    public TrafficManager manager() {
        return this.manager;
    }

    @Override
    public GeographicLocation getRoot() {
        TrafficManagerGeographicHierarchyInner defaultHierarchy = this.inner().getDefault();
        if (defaultHierarchy == null) {
            return null;
        }
        return new GeographicLocationImpl(defaultHierarchy.geographicHierarchy());
    }
}
