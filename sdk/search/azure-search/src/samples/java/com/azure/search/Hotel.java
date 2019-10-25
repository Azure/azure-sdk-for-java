// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.test.environment.models.ModelComparer;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotel {
    @JsonProperty(value = "HotelId")
    private String hotelId;

    @JsonProperty(value = "Tags")
    private List<String> tags;

    public Hotel() {
        this.tags = new ArrayList<>();
    }

    public String hotelId() {
        return this.hotelId;
    }

    public Hotel hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public List<String> tags() {
        return this.tags;
    }

    public Hotel tags(List<String> tags) {
        this.tags = tags;
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
            && ModelComparer.collectionEquals(tags, hotel.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, tags);
    }
}
