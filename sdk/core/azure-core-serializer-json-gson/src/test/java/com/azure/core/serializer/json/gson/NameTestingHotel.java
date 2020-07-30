// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NameTestingHotel {
    @SerializedName("hotelId")
    @Expose
    private String id;

    @SerializedName("")
    @Expose
    private String hotelName;

    private transient String description;

    private String reviews;

    @Expose
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

    public float getPrice() {
        return price;
    }

    public NameTestingHotel setPrice(float price) {
        this.price = price;
        return this;
    }

}
