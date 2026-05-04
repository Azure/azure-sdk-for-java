// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

/**
 * The address model class to test circular dependencies.
 */
public class AddressCircularDependencies {

    private HotelCircularDependencies hotel;

    /**
     * Get hotel address.
     *
     * @return the hotel address.
     */
    public HotelCircularDependencies getHotel() {
        return hotel;
    }

    /**
     * Sets hotel address.
     *
     * @param hotel the hotel address.
     * @return The {@link AddressCircularDependencies} object itself.
     */
    public AddressCircularDependencies setHotel(HotelCircularDependencies hotel) {
        this.hotel = hotel;
        return this;
    }
}
