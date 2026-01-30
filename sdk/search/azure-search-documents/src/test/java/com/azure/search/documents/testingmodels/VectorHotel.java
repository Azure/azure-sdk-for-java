// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.core.models.GeoPoint;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.VectorSearchEmbeddings;
import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UseOfObsoleteDateTimeApi")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VectorHotel implements JsonSerializable<VectorHotel> {
    @BasicField(name = "HotelId", isKey = BasicField.BooleanHelper.TRUE, isSortable = BasicField.BooleanHelper.TRUE)
    @JsonProperty(value = "HotelId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hotelId;

    @BasicField(
        name = "HotelName",
        isSearchable = BasicField.BooleanHelper.TRUE,
        isSortable = BasicField.BooleanHelper.TRUE,
        analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hotelName;

    @BasicField(name = "Description")
    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonProperty(value = "Description_fr")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String descriptionFr;

    @JsonProperty(value = "DescriptionVector")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Float> descriptionVector = VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION;
    // Default DescriptionVector: "Hotel"

    @BasicField(name = "Category")
    @JsonProperty(value = "Category")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String category;

    @BasicField(name = "Tags", isSearchable = BasicField.BooleanHelper.TRUE)
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
    private OffsetDateTime lastRenovationDate;

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

    public VectorHotel() {
        this.tags = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public String hotelId() {
        return this.hotelId;
    }

    public VectorHotel hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public String hotelName() {
        return this.hotelName;
    }

    public VectorHotel hotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String description() {
        return this.description;
    }

    public VectorHotel description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFr() {
        return this.descriptionFr;
    }

    public VectorHotel descriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
        return this;
    }

    public List<Float> descriptionVector() {
        return (this.descriptionVector == null) ? null : new ArrayList<>(this.descriptionVector);
    }

    public VectorHotel descriptionVector(List<Float> descriptionVector) {
        this.descriptionVector = (descriptionVector == null) ? null : new ArrayList<>(descriptionVector);
        return this;
    }

    public String category() {
        return this.category;
    }

    public VectorHotel category(String category) {
        this.category = category;
        return this;
    }

    public List<String> tags() {
        return (this.tags == null) ? null : new ArrayList<>(this.tags);
    }

    public VectorHotel tags(List<String> tags) {
        this.tags = (tags == null) ? null : new ArrayList<>(tags);
        return this;
    }

    public Boolean parkingIncluded() {
        return this.parkingIncluded;
    }

    public VectorHotel parkingIncluded(Boolean parkingIncluded) {
        this.parkingIncluded = parkingIncluded;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public VectorHotel smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public OffsetDateTime lastRenovationDate() {
        return this.lastRenovationDate;
    }

    public VectorHotel lastRenovationDate(OffsetDateTime lastRenovationDate) {
        this.lastRenovationDate = lastRenovationDate;
        return this;
    }

    public GeoPoint location() {
        return location;
    }

    public VectorHotel location(GeoPoint location) {
        this.location = location;
        return this;
    }

    public Integer rating() {
        return this.rating;
    }

    public VectorHotel rating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public HotelAddress address() {
        return this.address;
    }

    public VectorHotel address(HotelAddress address) {
        this.address = address;
        return this;
    }

    public List<HotelRoom> rooms() {
        return this.rooms;
    }

    public VectorHotel rooms(List<HotelRoom> rooms) {
        this.rooms = rooms;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("HotelId", hotelId)
            .writeStringField("HotelName", hotelName)
            .writeStringField("Description", description)
            .writeStringField("Description_fr", descriptionFr)
            .writeArrayField("DescriptionVector", descriptionVector, JsonWriter::writeFloat)
            .writeStringField("Category", category)
            .writeArrayField("Tags", tags, JsonWriter::writeString)
            .writeBooleanField("ParkingIncluded", parkingIncluded)
            .writeBooleanField("SmokingAllowed", smokingAllowed)
            .writeStringField("LastRenovationDate", Objects.toString(lastRenovationDate, null))
            .writeNumberField("Rating", rating)
            .writeJsonField("Location", location)
            .writeJsonField("Address", address)
            .writeArrayField("Rooms", rooms, JsonWriter::writeJson)
            .writeEndObject();
    }

    @SuppressWarnings("deprecation")
    public static VectorHotel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VectorHotel hotel = new VectorHotel();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("HotelId".equals(fieldName)) {
                    hotel.hotelId = reader.getString();
                } else if ("HotelName".equals(fieldName)) {
                    hotel.hotelName = reader.getString();
                } else if ("Description".equals(fieldName)) {
                    hotel.description = reader.getString();
                } else if ("Description_fr".equals(fieldName)) {
                    hotel.descriptionFr = reader.getString();
                } else if ("DescriptionVector".equals(fieldName)) {
                    hotel.descriptionVector = reader.readArray(JsonReader::getFloat);
                } else if ("Category".equals(fieldName)) {
                    hotel.category = reader.getString();
                } else if ("Tags".equals(fieldName)) {
                    hotel.tags = reader.readArray(JsonReader::getString);
                } else if ("ParkingIncluded".equals(fieldName)) {
                    hotel.parkingIncluded = reader.getNullable(JsonReader::getBoolean);
                } else if ("SmokingAllowed".equals(fieldName)) {
                    hotel.smokingAllowed = reader.getNullable(JsonReader::getBoolean);
                } else if ("LastRenovationDate".equals(fieldName)) {
                    hotel.lastRenovationDate = reader.getNullable(nonNull -> OffsetDateTime.parse(nonNull.getString()));
                } else if ("Rating".equals(fieldName)) {
                    hotel.rating = reader.getNullable(JsonReader::getInt);
                } else if ("Location".equals(fieldName)) {
                    hotel.location = GeoPoint.fromJson(reader);
                } else if ("Address".equals(fieldName)) {
                    hotel.address = HotelAddress.fromJson(reader);
                } else if ("Rooms".equals(fieldName)) {
                    hotel.rooms = reader.readArray(HotelRoom::fromJson);
                } else {
                    reader.skipChildren();
                }
            }
            return hotel;
        });
    }
}
