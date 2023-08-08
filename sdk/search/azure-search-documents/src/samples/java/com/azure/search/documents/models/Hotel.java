// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotel {
    private String hotelId;
    private List<String> tags;

    public Hotel() {
        this.tags = new ArrayList<>();
    }

    @JsonProperty(value = "HotelId")
    @SimpleField(isKey = true)
    public String getHotelId() {
        return this.hotelId;
    }

    public Hotel setHotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    @JsonProperty(value = "Tags")
    @SearchableField(isFilterable = true, analyzerName = "en.lucene")
    public List<String> getTags() {
        return this.tags;
    }

    public Hotel setTags(List<String> tags) {
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
        return Objects.equals(hotelId, hotel.hotelId) && Objects.equals(tags, hotel.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, tags);
    }
}
