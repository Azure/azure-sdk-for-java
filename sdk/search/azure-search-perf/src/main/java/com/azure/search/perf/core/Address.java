// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.core.annotation.Immutable;
import com.azure.search.documents.indexes.SearchableField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class representing an address.
 */
@Immutable
public class Address {
    @JsonProperty("StreetAddress")
    @SearchableField
    private String streetAddress;

    @JsonProperty("City")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    private String city;

    @JsonProperty("StateProvince")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    private String stateProvince;

    @JsonProperty("PostalCode")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    private String postalCode;

    @JsonProperty("Country")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    private String country;

    /**
     * Creates an Address object.
     *
     * @param streetAddress The street address.
     * @param city The city.
     * @param stateProvince The state or province.
     * @param postalCode The postal code.
     * @param country The country.
     */
    public Address(@JsonProperty("StreetAddress") String streetAddress, @JsonProperty("City") String city,
        @JsonProperty("StateProvince") String stateProvince, @JsonProperty("PostalCode") String postalCode,
        @JsonProperty("Country") String country) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    /**
     * Gets the street address.
     *
     * @return The street address.
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Gets the city.
     *
     * @return The city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets the state or province.
     *
     * @return The state or province.
     */
    public String getStateProvince() {
        return stateProvince;
    }

    /**
     * Gets the postal code.
     *
     * @return The postal code.
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Gets the country.
     *
     * @return The country.
     */
    public String getCountry() {
        return country;
    }
}
