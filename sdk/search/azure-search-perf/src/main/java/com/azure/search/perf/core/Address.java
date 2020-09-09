// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class representing an address.
 */
public class Address {
    @JsonProperty("StreetAddress")
    @SearchableFieldProperty
    public String streetAddress;

    @JsonProperty("City")
    @SearchableFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public String city;

    @JsonProperty("StateProvince")
    @SearchableFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public String stateProvince;

    @JsonProperty("PostalCode")
    @SearchableFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public String postalCode;

    @JsonProperty("Country")
    @SearchableFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public String country;
}
