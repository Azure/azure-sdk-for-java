// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.AddressValueHelper;

/**
 * Address field value.
 */
public final class AddressValue {


    /*
     * Building number.
     */
    private String houseNumber;

    /*
     * Post office box number.
     */
    private String poBox;

    /*
     * Street name.
     */
    private String road;

    /*
     * Name of city, town, village, etc.
     */
    private String city;

    /*
     * First-level administrative division.
     */
    private String state;

    /*
     * Postal code used for mail sorting.
     */
    private String postalCode;

    /*
     * Country/region.
     */
    private String countryRegion;

    /*
     * Street-level address, excluding city, state, countryRegion, and
     * postalCode.
     */
    private String streetAddress;

    /**
     * Get the houseNumber property: Building number.
     *
     * @return the houseNumber value.
     */
    public String getHouseNumber() {
        return this.houseNumber;
    }

    /**
     * Set the houseNumber property: Building number.
     *
     * @param houseNumber the houseNumber value to set.
     * @return the AddressValue object itself.
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Get the poBox property: Post office box number.
     *
     * @return the poBox value.
     */
    public String getPoBox() {
        return this.poBox;
    }

    /**
     * Set the poBox property: Post office box number.
     *
     * @param poBox the poBox value to set.
     * @return the AddressValue object itself.
     */
    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    /**
     * Get the road property: Street name.
     *
     * @return the road value.
     */
    public String getRoad() {
        return this.road;
    }

    /**
     * Set the road property: Street name.
     *
     * @param road the road value to set.
     * @return the AddressValue object itself.
     */
    public void setRoad(String road) {
        this.road = road;
    }

    /**
     * Get the city property: Name of city, town, village, etc.
     *
     * @return the city value.
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Set the city property: Name of city, town, village, etc.
     *
     * @param city the city value to set.
     * @return the AddressValue object itself.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the state property: First-level administrative division.
     *
     * @return the state value.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Set the state property: First-level administrative division.
     *
     * @param state the state value to set.
     * @return the AddressValue object itself.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Get the postalCode property: Postal code used for mail sorting.
     *
     * @return the postalCode value.
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    /**
     * Set the postalCode property: Postal code used for mail sorting.
     *
     * @param postalCode the postalCode value to set.
     * @return the AddressValue object itself.
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Get the countryRegion property: Country/region.
     *
     * @return the countryRegion value.
     */
    public String getCountryRegion() {
        return this.countryRegion;
    }

    /**
     * Set the countryRegion property: Country/region.
     *
     * @param countryRegion the countryRegion value to set.
     * @return the AddressValue object itself.
     */
    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    /**
     * Get the streetAddress property: Street-level address, excluding city, state, countryRegion, and postalCode.
     *
     * @return the streetAddress value.
     */
    public String getStreetAddress() {
        return this.streetAddress;
    }

    /**
     * Set the streetAddress property: Street-level address, excluding city, state, countryRegion, and postalCode.
     *
     * @param streetAddress the streetAddress value to set.
     * @return the AddressValue object itself.
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    static {
        AddressValueHelper.setAccessor(new AddressValueHelper.AddressValueAccessor() {

            @Override
            public void setPoBox(AddressValue addressValue, String poBox) {
                addressValue.setPoBox(poBox);
            }

            @Override
            public void setHouseNumber(AddressValue addressValue, String houseNumber) {
                addressValue.setHouseNumber(houseNumber);
            }

            @Override
            public void setRoad(AddressValue addressValue, String road) {
                addressValue.setRoad(road);
            }

            @Override
            public void setCity(AddressValue addressValue, String city) {
                addressValue.setCity(city);
            }

            @Override
            public void setState(AddressValue addressValue, String state) {
                addressValue.setState(state);
            }

            @Override
            public void setPostalCode(AddressValue addressValue, String postalCode) {
                addressValue.setPostalCode(postalCode);
            }

            @Override
            public void setCountryRegion(AddressValue addressValue, String countryRegion) {
                addressValue.setCountryRegion(countryRegion);
            }

            @Override
            public void setStreetAddress(AddressValue addressValue, String streetAddress) {
                addressValue.setStreetAddress(streetAddress);
            }
        });
    }
}
