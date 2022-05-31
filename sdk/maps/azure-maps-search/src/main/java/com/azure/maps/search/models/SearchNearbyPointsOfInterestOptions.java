package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Options for searching nearby points of interest.
 */
public final class SearchNearbyPointsOfInterestOptions extends BaseSearchPointOfInterestOptions<SearchNearbyPointsOfInterestOptions> {
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
     * @return
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor
     * @return
     */
    public SearchNearbyPointsOfInterestOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }
}
