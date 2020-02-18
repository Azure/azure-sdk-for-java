// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoudHotel {
    @JsonProperty(value = "HotelId")
    private String hotelId;

    @JsonProperty(value = "HotelName")
    private String hotelName;

    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String description;

    @JsonProperty(value = "Description_fr")
    private String descriptionFrench;

    @JsonProperty(value = "Category")
    private String category;

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

    @JsonProperty(value = "Location")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private GeoPoint location;

    @JsonProperty(value = "Address")
    private HotelAddress address;

    @JsonProperty(value = "Rooms")
    private List<HotelRoom> rooms;

    public LoudHotel() {
        this.tags = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public String hotelId() {
        return this.hotelId;
    }

    public LoudHotel hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public String hotelName() {
        return this.hotelName;
    }

    public LoudHotel hotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String description() {
        return this.description;
    }

    public LoudHotel description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFrench() {
        return this.descriptionFrench;
    }

    public LoudHotel descriptionFrench(String descriptionFr) {
        this.descriptionFrench = descriptionFr;
        return this;
    }

    public String category() {
        return this.category;
    }

    public LoudHotel category(String category) {
        this.category = category;
        return this;
    }

    public List<String> tags() {
        return this.tags;
    }

    public LoudHotel tags(List<String> tags) {
        this.tags = tags;
        return this;
    }


    public Boolean parkingIncluded() {
        return this.parkingIncluded;
    }

    public LoudHotel parkingIncluded(Boolean parkingIncluded) {
        this.parkingIncluded = parkingIncluded;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public LoudHotel smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public Date lastRenovationDate() {
        return this.lastRenovationDate;
    }

    public LoudHotel lastRenovationDate(Date lastRenovationDate) {
        this.lastRenovationDate = lastRenovationDate;
        return this;
    }

    public Integer rating() {
        return this.rating;
    }

    public LoudHotel rating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public GeoPoint location() {
        return this.location;
    }

    public LoudHotel location(GeoPoint location) {
        this.location = location;
        return this;
    }

    public HotelAddress address() {
        return this.address;
    }

    public LoudHotel address(HotelAddress address) {
        this.address = address;
        return this;
    }

    public List<HotelRoom> rooms() {
        return this.rooms;
    }

    public LoudHotel rooms(List<HotelRoom> rooms) {
        this.rooms = rooms;
        return this;
    }
}
