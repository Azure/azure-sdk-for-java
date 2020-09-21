// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.core.util.CoreUtils;
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
    private String hotelId;

    @JsonProperty("HotelName")
    @SearchableField(isSortable = true)
    private String hotelName;

    @JsonProperty("Description")
    @SearchableField(analyzerName = "en.microsoft")
    private String description;

    @JsonProperty("DescriptionFr")
    @SearchableField(analyzerName = "fr.lucene")
    private String descriptionFr;

    @JsonProperty("Category")
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    private String category;

    @JsonProperty("Tags")
    @SearchableField(isFilterable = true, isFacetable = true)
    private String[] tags;

    @JsonProperty("ParkingIncluded")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    private Boolean parkingIncluded;

    @JsonProperty("LastRenovationDate")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    private OffsetDateTime lastRenovationDate;

    @JsonProperty("Rating")
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    private Double rating;

    @JsonProperty("Address")
    private Address address;

    /**
     * Creates a Hotel object.
     * @param hotelId The hotel unique ID.
     * @param hotelName The hotel name.
     * @param description The description.
     * @param descriptionFr The French description.
     * @param category THe category.
     * @param tags The tags.
     * @param parkingIncluded Whether parking is included.
     * @param lastRenovationDate The last renovation date.
     * @param rating The rating.
     * @param address The address.
     */
    public Hotel(@JsonProperty("HotelId") String hotelId, @JsonProperty("HotelName") String hotelName,
        @JsonProperty("Description") String description, @JsonProperty("DescriptionFr") String descriptionFr,
        @JsonProperty("Category") String category, @JsonProperty("Tags") String[] tags,
        @JsonProperty("ParkingIncluded") Boolean parkingIncluded,
        @JsonProperty("LastRenovationDate") OffsetDateTime lastRenovationDate, @JsonProperty("Rating") Double rating,
        @JsonProperty("Address") Address address) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.description = description;
        this.descriptionFr = descriptionFr;
        this.category = category;
        this.tags = CoreUtils.clone(tags);
        this.parkingIncluded = parkingIncluded;
        this.lastRenovationDate = lastRenovationDate;
        this.rating = rating;
        this.address = address;
    }

    /**
     * Get the hotel unique ID.
     *
     * @return The hotel unique ID.
     */
    public String getHotelId() {
        return hotelId;
    }

    /**
     * Sets the hotel unique ID.
     * @param hotelId The hotel unique ID.
     * @return The updated Hotel object.
     */
    public Hotel setHotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    /**
     * Get the hotel name.
     *
     * @return The hotel name.
     */
    public String getHotelName() {
        return hotelName;
    }

    /**
     * Get the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the French description.
     *
     * @return The French description.
     */
    public String getDescriptionFr() {
        return descriptionFr;
    }

    /**
     * Get the category.
     *
     * @return The category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the tags.
     *
     * @return The tags.
     */
    public String[] getTags() {
        return CoreUtils.clone(tags);
    }

    /**
     * Get whether parking is included.
     *
     * @return Whether parking is included.
     */
    public Boolean getParkingIncluded() {
        return parkingIncluded;
    }

    /**
     * Get the last renovation date.
     *
     * @return The last renovation date.
     */
    public OffsetDateTime getLastRenovationDate() {
        return lastRenovationDate;
    }

    /**
     * Get the rating.
     *
     * @return The rating.
     */
    public Double getRating() {
        return rating;
    }

    /**
     * Get the address.
     *
     * @return The address.
     */
    public Address getAddress() {
        return address;
    }
}
