// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;
import com.azure.resourcemanager.trafficmanager.models.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation for GeographicLocation. */
class GeographicLocationImpl extends WrapperImpl<Region> implements GeographicLocation {
    protected GeographicLocationImpl(Region innerRegion) {
        super(innerRegion);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String code() {
        return this.innerModel().code();
    }

    @Override
    public List<GeographicLocation> childLocations() {
        if (this.innerModel().regions() == null || this.innerModel().regions().isEmpty()) {
            return Collections.unmodifiableList(new ArrayList<GeographicLocation>());
        }
        ArrayList<GeographicLocation> subLocations = new ArrayList<>();
        for (Region innerRegion : this.innerModel().regions()) {
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
