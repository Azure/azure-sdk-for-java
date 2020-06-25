// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.models.spatial.PointGeometry;
import com.azure.search.documents.indexes.FieldIgnore;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Hotel {
    @SimpleFieldProperty(isKey = true, isSortable = true)
    @JsonProperty(value = "HotelId")
    private String hotelId;

    @SearchableFieldProperty(isSortable = true, analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    private String hotelName;

    @SimpleFieldProperty
    @JsonProperty(value = "Description")
    private String description;

    @FieldIgnore
    @JsonProperty(value = "Description_fr")
    private String descriptionFr;

    @SimpleFieldProperty
    @JsonProperty(value = "Category")
    private String category;

    @SearchableFieldProperty
    @JsonProperty(value = "Tags")
    private List<String> tags;

    @JsonProperty(value = "ParkingIncluded")
    private Boolean parkingIncluded;

    @JsonProperty(value = "SmokingAllowed")
    private Boolean smokingAllowed;

    @JsonProperty(value = "LastRenovationDate")
    private Date lastRenovationDate;

    @JsonProperty(value = "Rating")
    private Integer rating;

    @SimpleFieldProperty
    @JsonProperty(value = "Location")
    private PointGeometry location;

    @JsonProperty(value = "Address")
    private HotelAddress address;

    @JsonProperty(value = "Rooms")
    private List<HotelRoom> rooms;

    public Hotel() {
        this.tags = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public String hotelId() {
        return this.hotelId;
    }

    public Hotel hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public String hotelName() {
        return this.hotelName;
    }

    public Hotel hotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String description() {
        return this.description;
    }

    public Hotel description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFr() {
        return this.descriptionFr;
    }

    public Hotel descriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
        return this;
    }

    public String category() {
        return this.category;
    }

    public Hotel category(String category) {
        this.category = category;
        return this;
    }

    public List<String> tags() {
        return this.tags;
    }

    public Hotel tags(List<String> tags) {
        this.tags = tags;
        return this;
    }


    public Boolean parkingIncluded() {
        return this.parkingIncluded;
    }

    public Hotel parkingIncluded(Boolean parkingIncluded) {
        this.parkingIncluded = parkingIncluded;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public Hotel smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public Date lastRenovationDate() {
        return this.lastRenovationDate;
    }

    public Hotel lastRenovationDate(Date lastRenovationDate) {
        this.lastRenovationDate = lastRenovationDate;
        return this;
    }

    public Integer rating() {
        return this.rating;
    }

    public Hotel rating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public PointGeometry location() {
        return this.location;
    }

    public Hotel location(PointGeometry location) {
        this.location = location;
        return this;
    }

    public HotelAddress address() {
        return this.address;
    }

    public Hotel address(HotelAddress address) {
        this.address = address;
        return this;
    }

    public List<HotelRoom> rooms() {
        return this.rooms;
    }

    public Hotel rooms(List<HotelRoom> rooms) {
        this.rooms = rooms;
        return this;
    }
}
