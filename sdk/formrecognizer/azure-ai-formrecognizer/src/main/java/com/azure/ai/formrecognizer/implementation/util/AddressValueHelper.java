// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.AddressValue;

/**
 * The helper class to set the non-public properties of an {@link AddressValue} instance.
 */
public final class AddressValueHelper {
    private static AddressValueAccessor accessor;

    private AddressValueHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AddressValue} instance.
     */
    public interface AddressValueAccessor {
        public void setPoBox(AddressValue addressValue, String poBox);
        public void setHouseNumber(AddressValue addressValue, String houseNumber);
        public void setRoad(AddressValue addressValue, String road);
        public void setCity(AddressValue addressValue, String city) ;
        public void setState(AddressValue addressValue, String state);
        public void setPostalCode(AddressValue addressValue, String postalCode);
        public void setCountryRegion(AddressValue addressValue, String countryRegion);
        public void setStreetAddress(AddressValue addressValue, String streetAddress);
    }

    /**
     * The method called from {@link AddressValue} to set it's accessor.
     *
     * @param addressValueAccessor The accessor.
     */
    public static void setAccessor(final AddressValueAccessor addressValueAccessor) {
        accessor = addressValueAccessor;
    }

    static void setPoBox(AddressValue addressValue, String poBox) {
        addressValue.setPoBox(poBox);
    }

    static void setHouseNumber(AddressValue addressValue, String houseNumber) {
        addressValue.setHouseNumber(houseNumber);
    }

    static void setRoad(AddressValue addressValue, String road) {
        addressValue.setRoad(road);
    }

    static void setCity(AddressValue addressValue, String city) {
        addressValue.setCity(city);
    }

    static void setState(AddressValue addressValue, String state) {
        addressValue.setState(state);
    }

    static void setPostalCode(AddressValue addressValue, String postalCode) {
        addressValue.setPostalCode(postalCode);
    }

    static void setCountryRegion(AddressValue addressValue, String countryRegion) {
        addressValue.setCountryRegion(countryRegion);
    }

    static void setStreetAddress(AddressValue addressValue, String streetAddress) {
        addressValue.setStreetAddress(streetAddress);
    }
}
