// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.search.documents.indexes.BasicField;
import com.azure.search.documents.indexes.ComplexField;
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
    @BasicField(name = "HotelId")
    public String hotelId;

    /**
     * Hotel name
     */
    @JsonProperty("HotelName")
    @BasicField(name = "HotelName")
    public String hotelName;

    /**
     * Description
     */
    @JsonProperty("Description")
    @BasicField(name = "Description")
    public String description;

    /**
     * French description
     */
    @JsonProperty("DescriptionFr")
    @BasicField(name = "DescriptionFr")
    public String descriptionFr;

    /**
     * Category
     */
    @JsonProperty("Category")
    @BasicField(name = "Category")
    public String category;

    /**
     * Tags
     */
    @JsonProperty("Tags")
    @BasicField(name = "Tags")
    public String[] tags;

    /**
     * Whether parking is included
     */
    @JsonProperty("ParkingIncluded")
    @BasicField(name = "ParkingIncluded")
    public Boolean parkingIncluded;

    /**
     * Last renovation time
     */
    @JsonProperty("LastRenovationDate")
    @BasicField(name = "LastRenovationDate")
    public OffsetDateTime lastRenovationDate;

    /**
     * Rating
     */
    @JsonProperty("Rating")
    @BasicField(name = "Rating")
    public Double rating;

    /**
     * Address
     */
    @JsonProperty("Address")
    @ComplexField(name = "Address")
    public Address address;
}
