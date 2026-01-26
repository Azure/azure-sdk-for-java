// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotel implements JsonSerializable<Hotel> {
    private String hotelId;
    private List<String> tags;

    public Hotel() {
        this.tags = new ArrayList<>();
    }

    @JsonProperty(value = "HotelId")
    @SimpleField(name = "HotelId", isKey = true)
    public String getHotelId() {
        return this.hotelId;
    }

    public Hotel setHotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    @JsonProperty(value = "Tags")
    @SearchableField(name = "Tags", isFilterable = true, analyzerName = "en.lucene")
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("HotelId", hotelId)
            .writeArrayField("Tags", tags, JsonWriter::writeString)
            .writeEndObject();
    }
}
