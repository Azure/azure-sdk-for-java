// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.AddressValue;

/**
 * The helper class to set the non-public properties of an {@link AddressValue} instance.
 */
public final class AddressValueHelper {
    private static AddressValueAccessor accessor;

    private AddressValueHelper() {
    }

    /**
     * The method called from {@link AddressValue} to set it's accessor.
     *
     * @param addressValueAccessor The accessor.
     */
    public static void setAccessor(final AddressValueAccessor addressValueAccessor) {
        accessor = addressValueAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AddressValue} instance.
     */
    public interface AddressValueAccessor {
        void setPoBox(AddressValue addressValue, String poBox);
        void setHouseNumber(AddressValue addressValue, String houseNumber);
        void setRoad(AddressValue addressValue, String road);
        void setCity(AddressValue addressValue, String city);
        void setState(AddressValue addressValue, String state);
        void setPostalCode(AddressValue addressValue, String postalCode);
        void setCountryRegion(AddressValue addressValue, String countryRegion);
        void setStreetAddress(AddressValue addressValue, String streetAddress);

        void setUnit(AddressValue addressValue, String unit);

        void setCityDistrict(AddressValue addressValue, String cityDistrict);

        void setStateDistrict(AddressValue addressValue, String stateDistrict);

        void setSuburb(AddressValue addressValue, String suburb);

        void setHouse(AddressValue addressValue, String house);

        void setLevel(AddressValue addressValue, String level);
    }
    static void setPoBox(AddressValue addressValue, String poBox) {
        accessor.setPoBox(addressValue, poBox);
    }

    static void setHouseNumber(AddressValue addressValue, String houseNumber) {
        accessor.setHouseNumber(addressValue, houseNumber);
    }

    static void setRoad(AddressValue addressValue, String road) {
        accessor.setRoad(addressValue, road);
    }

    static void setCity(AddressValue addressValue, String city) {
        accessor.setCity(addressValue, city);
    }

    static void setState(AddressValue addressValue, String state) {
        accessor.setState(addressValue, state);
    }

    static void setPostalCode(AddressValue addressValue, String postalCode) {
        accessor.setPostalCode(addressValue, postalCode);
    }

    static void setCountryRegion(AddressValue addressValue, String countryRegion) {
        accessor.setCountryRegion(addressValue, countryRegion);
    }

    static void setStreetAddress(AddressValue addressValue, String streetAddress) {
        accessor.setStreetAddress(addressValue, streetAddress);
    }
}
