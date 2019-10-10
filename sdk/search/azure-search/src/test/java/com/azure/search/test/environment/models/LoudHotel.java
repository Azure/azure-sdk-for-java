// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LoudHotel {
    @JsonProperty(value = "HotelId")
    private String HOTELID;

    @JsonProperty(value = "HotelName")
    private String HOTELNAME;

    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String DESCRIPTION;

    @JsonProperty(value = "Description_fr")
    private String DESCRIPTIONFRENCH;

    @JsonProperty(value = "Category")
    private String CATEGORY;

    @JsonProperty(value = "Tags")
    private List<String> TAGS;

    @JsonProperty(value = "ParkingIncluded")
    private Boolean PARKINGINCLUDED;

    @JsonProperty(value = "SmokingAllowed")
    private Boolean SMOKINGALLOWED;

    @JsonProperty(value = "LastRenovationDate")
    private Date LASTRENOVATIONDATE;

    @JsonProperty(value = "Rating")
    private Integer RATING;

    @JsonProperty(value = "Location")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private GeoPoint LOCATION;

    @JsonProperty(value = "Address")
    private HotelAddress ADDRESS;

    @JsonProperty(value = "Rooms")
    private List<HotelRoom> ROOMS;

    public LoudHotel() {
        this.TAGS = new ArrayList<>();
        this.ROOMS = new ArrayList<>();
    }

    public String HOTELID() {
        return this.HOTELID;
    }

    public LoudHotel HOTELID(String hotelId) {
        this.HOTELID = hotelId;
        return this;
    }

    public String HOTELNAME() {
        return this.HOTELNAME;
    }

    public LoudHotel HOTELNAME(String hotelName) {
        this.HOTELNAME = hotelName;
        return this;
    }

    public String DESCRIPTION() {
        return this.DESCRIPTION;
    }

    public LoudHotel DESCRIPTION(String description) {
        this.DESCRIPTION = description;
        return this;
    }

    public String DESCRIPTIONFRENCH() {
        return this.DESCRIPTIONFRENCH;
    }

    public LoudHotel DESCRIPTIONFRENCH(String descriptionFr) {
        this.DESCRIPTIONFRENCH = descriptionFr;
        return this;
    }

    public String CATEGORY() {
        return this.CATEGORY;
    }

    public LoudHotel CATEGORY(String category) {
        this.CATEGORY = category;
        return this;
    }

    public List<String> TAGS() {
        return this.TAGS;
    }

    public LoudHotel TAGS(List<String> tags) {
        this.TAGS = tags;
        return this;
    }


    public Boolean PARKINGINCLUDED() {
        return this.PARKINGINCLUDED;
    }

    public LoudHotel PARKINGINCLUDED(Boolean parkingIncluded) {
        this.PARKINGINCLUDED = parkingIncluded;
        return this;
    }

    public Boolean SMOKINGALLOWED() {
        return this.SMOKINGALLOWED;
    }

    public LoudHotel SMOKINGALLOWED(Boolean smokingAllowed) {
        this.SMOKINGALLOWED = smokingAllowed;
        return this;
    }

    public Date LASTRENOVATIONDATE() {
        return this.LASTRENOVATIONDATE;
    }

    public LoudHotel LASTRENOVATIONDATE(Date lastRenovationDate) {
        this.LASTRENOVATIONDATE = lastRenovationDate;
        return this;
    }

    public Integer RATING() {
        return this.RATING;
    }

    public LoudHotel RATING(Integer rating) {
        this.RATING = rating;
        return this;
    }

    public GeoPoint LOCATION() {
        return this.LOCATION;
    }

    public LoudHotel LOCATION(GeoPoint location) {
        this.LOCATION = location;
        return this;
    }

    public HotelAddress ADDRESS() {
        return this.ADDRESS;
    }

    public LoudHotel ADDRESS(HotelAddress address) {
        this.ADDRESS = address;
        return this;
    }

    public List<HotelRoom> ROOMS() {
        return this.ROOMS;
    }

    public LoudHotel ROOMS(List<HotelRoom> rooms) {
        this.ROOMS = rooms;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoudHotel)) {
            return false;
        }
        LoudHotel hotel = (LoudHotel) o;
        return Objects.equals(HOTELID, hotel.HOTELID)
            && Objects.equals(HOTELNAME, hotel.HOTELNAME)
            && Objects.equals(DESCRIPTION, hotel.DESCRIPTION)
            && Objects.equals(DESCRIPTIONFRENCH, hotel.DESCRIPTIONFRENCH)
            && Objects.equals(CATEGORY, hotel.CATEGORY)
            && ModelComparer.collectionEquals(TAGS, hotel.TAGS)
            && Objects.equals(PARKINGINCLUDED, hotel.PARKINGINCLUDED)
            && Objects.equals(SMOKINGALLOWED, hotel.SMOKINGALLOWED)
            && Objects.equals(LASTRENOVATIONDATE, hotel.LASTRENOVATIONDATE)
            && Objects.equals(RATING, hotel.RATING)
            && Objects.equals(LOCATION, hotel.LOCATION)
            && Objects.equals(ADDRESS, hotel.ADDRESS)
            && ModelComparer.collectionEquals(ROOMS, hotel.ROOMS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(HOTELID, HOTELNAME, DESCRIPTION, DESCRIPTIONFRENCH, CATEGORY, TAGS, PARKINGINCLUDED,
            SMOKINGALLOWED, LASTRENOVATIONDATE, RATING, LOCATION, ADDRESS, ROOMS);
    }
}
