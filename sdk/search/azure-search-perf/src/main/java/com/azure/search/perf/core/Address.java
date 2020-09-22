// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.SearchableField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class representing an address.
 */
public class Address {
    @JsonProperty("StreetAddress")
    @SearchableField
    public String streetAddress;

    @JsonProperty("City")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String city;

    @JsonProperty("StateProvince")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String stateProvince;

    @JsonProperty("PostalCode")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String postalCode;

    @JsonProperty("Country")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String country;
}
