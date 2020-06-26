// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluent.inner.LocationInner;

/**
 * An immutable client-side representation of an Azure location.
 */
@Fluent
public interface Location extends
        Indexable,
        HasInner<LocationInner>,
        HasName {
    /**
     * @return the subscription UUID
     */
    String subscriptionId();

    /**
     * @return the display name of the location readable by humans
     */
    String displayName();

    /**
     * @return the region of the data center location
     */
    Region region();

    /**
     * @return the latitude of the location
     */
    String latitude();

    /**
     * @return the longitude of the location
     */
    String longitude();

    /**
     * @return the type of the region.
     */
    RegionType regionType();

    /**
     * @return the category of the region.
     */
    RegionCategory regionCategory();

    /**
     * @return the geography group.
     */
    String geographyGroup();

    /**
     * @return the physical location.
     */
    String physicalLocation();
}
