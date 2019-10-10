// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Hotel {
    @JsonProperty(value = "HotelId")
    private String hotelId;

    @JsonProperty(value = "HotelName")
    private String hotelName;

    @JsonProperty(value = "Description")
    private String description;

    @JsonProperty(value = "Description_fr")
    private String descriptionFr;

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
    private GeoPoint location;

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

    public GeoPoint location() {
        return this.location;
    }

    public Hotel location(GeoPoint location) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hotel)) {
            return false;
        }
        Hotel hotel = (Hotel) o;
        return Objects.equals(hotelId, hotel.hotelId)
            && Objects.equals(hotelName, hotel.hotelName)
            && Objects.equals(description, hotel.description)
            && Objects.equals(descriptionFr, hotel.descriptionFr)
            && Objects.equals(category, hotel.category)
            && ModelComparer.collectionEquals(tags, hotel.tags)
            && Objects.equals(parkingIncluded, hotel.parkingIncluded)
            && Objects.equals(smokingAllowed, hotel.smokingAllowed)
            && Objects.equals(lastRenovationDate, hotel.lastRenovationDate)
            && Objects.equals(rating, hotel.rating)
            && Objects.equals(location, hotel.location)
            && Objects.equals(address, hotel.address)
            && ModelComparer.collectionEquals(rooms, hotel.rooms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, hotelName, description, descriptionFr, category, tags, parkingIncluded,
            smokingAllowed, lastRenovationDate, rating, location, address, rooms);
    }
}
