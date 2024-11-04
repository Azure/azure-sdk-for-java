// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for Search.
 */
@Fluent
public final class ReverseSearchCrossStreetAddressOptions
    extends BaseReverseSearchOptions<ReverseSearchCrossStreetAddressOptions> {
    private Integer top;

    /**
     * Builds reverse cross street search options with coordinates.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public ReverseSearchCrossStreetAddressOptions(GeoPosition coordinates) {
        this.setCoordinates(coordinates);
    }

    /**
     * Returns the top value.
     * @return the top value.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the top value.
     * @param top the top value.
     * @return a reference to this {@code ReverseSearchCrossStreetAddressOptions}
     */
    public ReverseSearchCrossStreetAddressOptions setTop(Integer top) {
        this.top = top;
        return this;
    }
}
