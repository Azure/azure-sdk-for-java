// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

/**
 * The model class to test the behaviour of circular dependencies
 */
public class HotelCircularDependencies {
    private AddressCircularDependencies homeAddress;
    private AddressCircularDependencies billingAddress;

    /**
     * Gets home address.
     *
     * @return home address.
     */
    public AddressCircularDependencies getHomeAddress() {
        return homeAddress;
    }

    /**
     * Sets home address.
     *
     * @param homeAddress Home address to set.
     * @return The {@link HotelCircularDependencies} object itself.
     */
    public HotelCircularDependencies setHomeAddress(AddressCircularDependencies homeAddress) {
        this.homeAddress = homeAddress;
        return this;
    }

    /**
     * Gets billing address.
     *
     * @return billing address.
     */
    public AddressCircularDependencies getBillingAddress() {
        return billingAddress;
    }

    /**
     * Sets billing address.
     *
     * @param billingAddress Billing address to set.
     * @return The {@link HotelCircularDependencies} object itself.
     */
    public HotelCircularDependencies setBillingAddress(AddressCircularDependencies billingAddress) {
        this.billingAddress = billingAddress;
        return this;
    }
}
