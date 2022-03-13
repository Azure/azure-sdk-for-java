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
    /**
     * Hotel ID
     */
    @JsonProperty("HotelId")
    @SimpleField(isKey = true)
    public String hotelId;

    /**
     * Hotel name
     */
    @JsonProperty("HotelName")
    @SearchableField(isSortable = true)
    public String hotelName;

    /**
     * Description
     */
    @JsonProperty("Description")
    @SearchableField(analyzerName = "en.microsoft")
    public String description;

    /**
     * French description
     */
    @JsonProperty("DescriptionFr")
    @SearchableField(analyzerName = "fr.lucene")
    public String descriptionFr;

    /**
     * Category
     */
    @JsonProperty("Category")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String category;

    /**
     * Tags
     */
    @JsonProperty("Tags")
    @SearchableField(isFilterable = true, isFacetable = true)
    public String[] tags;

    /**
     * Whether parking is included
     */
    @JsonProperty("ParkingIncluded")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Boolean parkingIncluded;

    /**
     * Last renovation time
     */
    @JsonProperty("LastRenovationDate")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public OffsetDateTime lastRenovationDate;

    /**
     * Rating
     */
    @JsonProperty("Rating")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Double rating;

    /**
     * Address
     */
    @JsonProperty("Address")
    public Address address;
}
