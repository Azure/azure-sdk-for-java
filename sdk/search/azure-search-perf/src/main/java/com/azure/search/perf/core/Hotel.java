// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Model class representing a hotel.
 */
public class Hotel {
    @JsonProperty("HotelId")
    @SimpleField(isKey = true)
    public String hotelId;

    @JsonProperty("HotelName")
    @SearchableField(isSortable = true)
    public String hotelName;

    @JsonProperty("Description")
    @SearchableField(analyzerName = "en.microsoft")
    public String description;

    @JsonProperty("DescriptionFr")
    @SearchableField(analyzerName = "fr.lucene")
    public String descriptionFr;

    @JsonProperty("Category")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String category;

    @JsonProperty("Tags")
    @SearchableField(isFilterable = true, isFacetable = true)
    public String[] tags;

    @JsonProperty("ParkingIncluded")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Boolean parkingIncluded;

    @JsonProperty("LastRenovationDate")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public OffsetDateTime lastRenovationDate;

    @JsonProperty("Rating")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Double rating;

    @JsonProperty("Address")
    public Address address;
}
