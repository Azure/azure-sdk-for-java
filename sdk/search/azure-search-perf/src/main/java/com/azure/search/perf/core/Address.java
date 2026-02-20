// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class representing an address.
 */
public class Address {
    /**
     * Street address
     */
    @JsonProperty("StreetAddress")
    @BasicField(name = "StreetAddress")
    public String streetAddress;

    /**
     * City
     */
    @JsonProperty("City")
    @BasicField(name = "City")
    public String city;

    /**
     * State or province
     */
    @JsonProperty("StateProvince")
    @BasicField(name = "StateProvince")
    public String stateProvince;

    /**
     * Postal code
     */
    @JsonProperty("PostalCode")
    @BasicField(name = "PostalCode")
    public String postalCode;

    /**
     * Country
     */
    @JsonProperty("Country")
    @BasicField(name = "Country")
    public String country;
}
