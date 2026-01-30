// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

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
    public String hotelId;

    /**
     * Hotel name
     */
    @JsonProperty("HotelName")
    public String hotelName;

    /**
     * Description
     */
    @JsonProperty("Description")
    public String description;

    /**
     * French description
     */
    @JsonProperty("DescriptionFr")
    public String descriptionFr;

    /**
     * Category
     */
    @JsonProperty("Category")
    public String category;

    /**
     * Tags
     */
    @JsonProperty("Tags")
    public String[] tags;

    /**
     * Whether parking is included
     */
    @JsonProperty("ParkingIncluded")
    public Boolean parkingIncluded;

    /**
     * Last renovation time
     */
    @JsonProperty("LastRenovationDate")
    public OffsetDateTime lastRenovationDate;

    /**
     * Rating
     */
    @JsonProperty("Rating")
    public Double rating;

    /**
     * Address
     */
    @JsonProperty("Address")
    public Address address;
}
