package com.azure.maps.search.models;

import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for Search.
 */
public final class ReverseSearchCrossStreetAddressOptions extends BaseReverseSearchOptions<ReverseSearchCrossStreetAddressOptions> {
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
     * @return
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the top value.
     * @param top
     * @return
     */
    public ReverseSearchCrossStreetAddressOptions setTop(Integer top) {
        this.top = top;
        return this;
    }
}
