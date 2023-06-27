// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.AddressValueHelper;
import com.azure.core.annotation.Immutable;

/**
 * Address field value.
 */
@Immutable
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

    /*
     * Apartment or office number
     */
    private String unit;

    /*
     * Districts or boroughs within a city, such as Brooklyn in New York City or City of Westminster in London.
     */
    private String cityDistrict;

    /*
     * Second-level administrative division used in certain locales.
     */
    private String stateDistrict;

    /*
     * Unofficial neighborhood name, like Chinatown.
     */
    private String suburb;

    /*
     * Building name, such as World Trade Center.
     */
    private String house;

    /*
     * Floor number, such as 3F.
     */
    private String level;

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
     */
    private void setHouseNumber(String houseNumber) {
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
     */
    private void setPoBox(String poBox) {
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
     */
    private void setRoad(String road) {
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
     */
    private void setCity(String city) {
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
     */
    private void setState(String state) {
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
     */
    private void setPostalCode(String postalCode) {
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
     */
    private void setCountryRegion(String countryRegion) {
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
     * Get the unit property: Apartment or office number.
     *
     * @return the unit value.
     */
    public String getUnit() {
        return this.unit;
    }
 /**
     * Get the cityDistrict property: Districts or boroughs within a city, such as Brooklyn in New York City or City of
     * Westminster in London.
     *
     * @return the cityDistrict value.
     */
    public String getCityDistrict() {
        return this.cityDistrict;
    }

    /**
     * Get the stateDistrict property: Second-level administrative division used in certain locales.
     *
     * @return the stateDistrict value.
     */
    public String getStateDistrict() {
        return this.stateDistrict;
    }

    /**
     * Get the suburb property: Unofficial neighborhood name, like Chinatown.
     *
     * @return the suburb value.
     */
    public String getSuburb() {
        return this.suburb;
    }

    /**
     * Get the house property: Build name, such as World Trade Center.
     *
     * @return the house value.
     */
    public String getHouse() {
        return this.house;
    }

    /**
     * Get the level property: Floor number, such as 3F.
     *
     * @return the level value.
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * Set the streetAddress property: Street-level address, excluding city, state, countryRegion, and postalCode.
     *
     * @param streetAddress the streetAddress value to set.
     */
    private void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    private void setCityDistrict(String city) {
        this.cityDistrict = city;
    }

    private void setStateDistrict(String state) {
        this.stateDistrict = state;
    }

    private void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    private void setHouse(String house) {
        this.house = house;
    }

    private void setLevel(String level) {
        this.level = level;
    }

    private void setUnit(String unit) {
        this.unit = unit;
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

            @Override
            public void setUnit(AddressValue addressValue, String unit) {
                addressValue.setUnit(unit);
            }

            @Override
            public void setCityDistrict(AddressValue addressValue, String cityDistrict) {
                addressValue.setCityDistrict(cityDistrict);
            }

            @Override
            public void setStateDistrict(AddressValue addressValue, String stateDistrict) {
                addressValue.setStateDistrict(stateDistrict);
            }

            @Override
            public void setSuburb(AddressValue addressValue, String suburb) {
                addressValue.setSuburb(suburb);
            }

            @Override
            public void setHouse(AddressValue addressValue, String house) {
                addressValue.setHouse(house);
            }

            @Override
            public void setLevel(AddressValue addressValue, String level) {
                addressValue.setLevel(level);
            }
        });
    }
}
