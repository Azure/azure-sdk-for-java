// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NameTestingHotel {
    @JsonProperty("hotelId")
    private String id;

    @JsonProperty
    private String hotelName;

    @JsonProperty("")
    private String description;

    private String reviews;

    private String address;

    private String tags;

    @JsonIgnore
    private float price;

    public String getId() {
        return id;
    }

    public NameTestingHotel setId(String id) {
        this.id = id;
        return this;
    }

    public String getHotelName() {
        return hotelName;
    }

    public NameTestingHotel setHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NameTestingHotel setDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("hotelReviews")
    public String getReviews() {
        return reviews;
    }

    public NameTestingHotel setReviews(String reviews) {
        this.reviews = reviews;
        return this;
    }

    public String getAddress() {
        return address;
    }

    @JsonProperty("hotelAddress")
    public NameTestingHotel setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public NameTestingHotel setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public float getPrice() {
        return price;
    }

    public NameTestingHotel setPrice(float price) {
        this.price = price;
        return this;
    }

}
