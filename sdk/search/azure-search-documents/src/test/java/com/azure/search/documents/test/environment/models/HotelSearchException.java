// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableFieldProperty;

/**
 * The data object model is to test exception case.
 */
public class HotelSearchException {
    private int hotelId;

    /**
     * Gets hotel id.
     *
     * @return Get hotel id
     */
    @SearchableFieldProperty
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
