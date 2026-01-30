// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class representing an address.
 */
public class Address {
    /**
     * Street address
     */
    @JsonProperty("StreetAddress")
    public String streetAddress;

    /**
     * City
     */
    @JsonProperty("City")
    public String city;

    /**
     * State or province
     */
    @JsonProperty("StateProvince")
    public String stateProvince;

    /**
     * Postal code
     */
    @JsonProperty("PostalCode")
    public String postalCode;

    /**
     * Country
     */
    @JsonProperty("Country")
    public String country;
}
