// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluent.models.LocationInner;

/**
 * An immutable client-side representation of an Azure location.
 */
@Fluent
public interface Location extends Indexable, HasInnerModel<LocationInner>, HasName {
    /**
     * Gets the subscription UUID.
     *
     * @return the subscription UUID
     */
    String subscriptionId();

    /**
     * Gets the display name of the location readable by humans.
     *
     * @return the display name of the location readable by humans
     */
    String displayName();

    /**
     * Gets the region of the data center location.
     *
     * @return the region of the data center location
     */
    Region region();

    /**
     * Gets the latitude of the location.
     *
     * @return the latitude of the location
     */
    String latitude();

    /**
     * Gets the longitude of the location.
     *
     * @return the longitude of the location
     */
    String longitude();

    /**
     * Gets the type of the region.
     *
     * @return the type of the region.
     */
    RegionType regionType();

    /**
     * Gets the category of the region.
     *
     * @return the category of the region.
     */
    RegionCategory regionCategory();

    /**
     * Gets the geography group.
     *
     * @return the geography group.
     */
    String geographyGroup();

    /**
     * Gets the physical location.
     *
     * @return the physical location.
     */
    String physicalLocation();
}
