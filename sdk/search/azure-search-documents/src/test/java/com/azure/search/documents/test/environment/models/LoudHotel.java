// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"UseOfObsoleteDateTimeApi", "unused"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoudHotel {
    @JsonProperty(value = "HotelId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String HOTELID;

    @JsonProperty(value = "HotelName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String HOTELNAME;

    @JsonProperty(value = "Description")
    private String DESCRIPTION;

    @JsonProperty(value = "Description_fr")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String DESCRIPTIONFRENCH;

    @JsonProperty(value = "Category")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String CATEGORY;

    @JsonProperty(value = "Tags")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> TAGS;

    @JsonProperty(value = "ParkingIncluded")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean PARKINGINCLUDED;

    @JsonProperty(value = "SmokingAllowed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean SMOKINGALLOWED;

    @JsonProperty(value = "LastRenovationDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date LASTRENOVATIONDATE;

    @JsonProperty(value = "Rating")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer RATING;

    @JsonProperty(value = "Location")
    private GeoPoint LOCATION;

    @JsonProperty(value = "Address")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HotelAddress ADDRESS;

    @JsonProperty(value = "Rooms")
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
        return (this.TAGS) == null ? null : new ArrayList<>(this.TAGS);
    }

    public LoudHotel TAGS(List<String> tags) {
        this.TAGS = (tags == null) ? null : new ArrayList<>(tags);
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
        return (this.LASTRENOVATIONDATE == null) ? null : (Date) this.LASTRENOVATIONDATE.clone();
    }

    public LoudHotel LASTRENOVATIONDATE(Date lastRenovationDate) {
        this.LASTRENOVATIONDATE = (lastRenovationDate == null) ? null : (Date) lastRenovationDate.clone();
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
        return (this.ROOMS == null) ? null : new ArrayList<>(this.ROOMS);
    }

    public LoudHotel ROOMS(List<HotelRoom> rooms) {
        this.ROOMS = (rooms == null) ? null : new ArrayList<>(rooms);
        return this;
    }
}
