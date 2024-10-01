// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

/**
 * The address model.
 */
public class Address {
    private String state = "";
    private String county = "";
    private String city = "";

    /**
     * Creates a new instance of {@link Address}.
     */
    public Address() {
    }

    /**
     * Returns the state.
     * @return the state.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     * @param state the state.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the county.
     * @return the county.
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the county.
     * @param county the county.
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * Returns the city.
     * @return the city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city the city.
     */
    public void setCity(String city) {
        this.city = city;
    }
}
