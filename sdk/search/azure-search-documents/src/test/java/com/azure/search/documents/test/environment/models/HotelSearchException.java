package com.azure.search.documents.test.environment.models;

import com.azure.search.annotation.SearchableFieldProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The data object model is to test exception case.
 */
public class HotelSearchException {

    @SearchableFieldProperty
    private int hotelId;

    /**
     * Gets hotel id.
     *
     * @return Get hotel id
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * Sets hotel id.
     *
     * @param hotelId The hotel id.
     * @return the {@link HotelSearchException} object itself.
     */
    public HotelSearchException setHotelId(int hotelId) {
        this.hotelId = hotelId;
        return this;
    }
}
