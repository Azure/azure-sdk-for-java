// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Model class representing a hotel.
 */
public class Hotel {
    @JsonProperty("HotelId")
    @SimpleFieldProperty(isKey = true)
    public String hotelId;

    @JsonProperty("HotelName")
    @SearchableFieldProperty(isSortable = true)
    public String hotelName;

    @JsonProperty("Description")
    @SearchableFieldProperty(analyzerName = "en.microsoft")
    public String description;

    @JsonProperty("DescriptionFr")
    @SearchableFieldProperty(analyzerName = "fr.lucene")
    public String descriptionFr;

    @JsonProperty("Category")
    @SearchableFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public String category;

    @JsonProperty("Tags")
    @SearchableFieldProperty(isFilterable = true, isFacetable = true)
    public String[] tags;

    @JsonProperty("ParkingIncluded")
    @SimpleFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public Boolean parkingIncluded;

    @JsonProperty("LastRenovationDate")
    @SimpleFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public OffsetDateTime lastRenovationDate;

    @JsonProperty("Rating")
    @SimpleFieldProperty(isFilterable = true, isSortable = true, isFacetable = true)
    public Double rating;

    @JsonProperty("Address")
    public Address address;
}
