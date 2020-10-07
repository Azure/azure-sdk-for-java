// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** Type representing Geographic Hierarchy region (location). */
public interface GeographicLocation extends HasName, HasInnerModel<Region> {
    /** @return the location code. */
    String code();
    /** @return list of immediate child locations grouped under this location in the Geographic Hierarchy. */
    List<GeographicLocation> childLocations();
    /** @return list of all descendant locations grouped under this location in the Geographic Hierarchy. */
    List<GeographicLocation> descendantLocations();
}
