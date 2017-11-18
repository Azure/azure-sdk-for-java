/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.trafficmanager.GeographicLocation;
import com.microsoft.azure.management.trafficmanager.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for GeographicLocation.
 */
@LangDefinition
class GeographicLocationImpl extends WrapperImpl<com.microsoft.azure.management.trafficmanager.Region> implements GeographicLocation {
    protected GeographicLocationImpl(com.microsoft.azure.management.trafficmanager.Region innerRegion) {
        super(innerRegion);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String code() {
        return this.inner().code();
    }

    @Override
    public List<GeographicLocation> childLocations() {
        if (this.inner().regions() == null || this.inner().regions().isEmpty()) {
            return Collections.unmodifiableList(new ArrayList<GeographicLocation>());
        }
        ArrayList<GeographicLocation> subLocations = new ArrayList<>();
        for (Region innerRegion : this.inner().regions()) {
            subLocations.add(new GeographicLocationImpl(innerRegion));
        }
        return Collections.unmodifiableList(subLocations);
    }

    @Override
    public List<GeographicLocation> descendantLocations() {
        ArrayList<GeographicLocation> descendantsLocations = new ArrayList<>();
        List<GeographicLocation> childLocations = childLocations();
        descendantsLocations.addAll(childLocations);
        for (GeographicLocation childLocation : childLocations) {
            descendantsLocations.addAll(childLocation.descendantLocations());
        }
        return descendantsLocations;
    }
}
