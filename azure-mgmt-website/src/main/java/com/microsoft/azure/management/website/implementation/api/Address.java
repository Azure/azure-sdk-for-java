/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Address information for domain registration.
 */
public class Address {
    /**
     * Address 1.
     */
    private String address1;

    /**
     * Address 2.
     */
    private String address2;

    /**
     * City.
     */
    private String city;

    /**
     * Country.
     */
    private String country;

    /**
     * Postal code.
     */
    private String postalCode;

    /**
     * State.
     */
    private String state;

    /**
     * Get the address1 value.
     *
     * @return the address1 value
     */
    public String address1() {
        return this.address1;
    }

    /**
     * Set the address1 value.
     *
     * @param address1 the address1 value to set
     * @return the Address object itself.
     */
    public Address withAddress1(String address1) {
        this.address1 = address1;
        return this;
    }

    /**
     * Get the address2 value.
     *
     * @return the address2 value
     */
    public String address2() {
        return this.address2;
    }

    /**
     * Set the address2 value.
     *
     * @param address2 the address2 value to set
     * @return the Address object itself.
     */
    public Address withAddress2(String address2) {
        this.address2 = address2;
        return this;
    }

    /**
     * Get the city value.
     *
     * @return the city value
     */
    public String city() {
        return this.city;
    }

    /**
     * Set the city value.
     *
     * @param city the city value to set
     * @return the Address object itself.
     */
    public Address withCity(String city) {
        this.city = city;
        return this;
    }

    /**
     * Get the country value.
     *
     * @return the country value
     */
    public String country() {
        return this.country;
    }

    /**
     * Set the country value.
     *
     * @param country the country value to set
     * @return the Address object itself.
     */
    public Address withCountry(String country) {
        this.country = country;
        return this;
    }

    /**
     * Get the postalCode value.
     *
     * @return the postalCode value
     */
    public String postalCode() {
        return this.postalCode;
    }

    /**
     * Set the postalCode value.
     *
     * @param postalCode the postalCode value to set
     * @return the Address object itself.
     */
    public Address withPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public String state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the Address object itself.
     */
    public Address withState(String state) {
        this.state = state;
        return this;
    }

}
