// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.models.GeoPoint;
import com.azure.search.documents.indexes.FieldBuilderIgnore;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("UseOfObsoleteDateTimeApi")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hotel {
    @SimpleField(isKey = true, isSortable = true)
    @JsonProperty(value = "HotelId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hotelId;

    @SearchableField(isSortable = true, analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hotelName;

    @SimpleField
    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @FieldBuilderIgnore
    @JsonProperty(value = "Description_fr")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String descriptionFr;

    @SimpleField
    @JsonProperty(value = "Category")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String category;

    @SearchableField
    @JsonProperty(value = "Tags")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> tags;

    @JsonProperty(value = "ParkingIncluded")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean parkingIncluded;

    @JsonProperty(value = "SmokingAllowed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean smokingAllowed;

    @JsonProperty(value = "LastRenovationDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date lastRenovationDate;

    @JsonProperty(value = "Rating")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rating;

    @JsonProperty(value = "Location")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GeoPoint location;

    @JsonProperty(value = "Address")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HotelAddress address;

    @JsonProperty(value = "Rooms")
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
        return (this.tags == null) ? null : new ArrayList<>(this.tags);
    }

    public Hotel tags(List<String> tags) {
        this.tags = (tags == null) ? null : new ArrayList<>(tags);
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
        return (this.lastRenovationDate == null) ? null : (Date) this.lastRenovationDate.clone();
    }

    public Hotel lastRenovationDate(Date lastRenovationDate) {
        this.lastRenovationDate = (lastRenovationDate == null) ? null : (Date) lastRenovationDate.clone();
        return this;
    }

    public GeoPoint location() {
        return location;
    }

    public Hotel location(GeoPoint location) {
        this.location = location;
        return this;
    }

    public Integer rating() {
        return this.rating;
    }

    public Hotel rating(Integer rating) {
        this.rating = rating;
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
