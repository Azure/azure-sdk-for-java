// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;
import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoPosition;

/**
 * Options for searching nearby points of interest.
 */
@Fluent
public final class SearchNearbyPointsOfInterestOptions
    extends BaseSearchPointOfInterestOptions<SearchNearbyPointsOfInterestOptions> {
    private List<SearchIndexes> extendedPostalCodesFor;

    /**
     * Builds search nearby options with coordinates.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public SearchNearbyPointsOfInterestOptions(GeoPosition coordinates) {
        this.setCoordinates(coordinates);
    }

    /**
     * Returns the extended postal codes for.
     * @return the extended postal codes  used for the search
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor the extended postal codes used for the search
     * @return a reference to this {@code SearchNearbyPointsOfInterestOptions}
     */
    public SearchNearbyPointsOfInterestOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }
}
