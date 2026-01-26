// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.models.GeoPoint;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({ "UseOfObsoleteDateTimeApi", "unused" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoudHotel implements JsonSerializable<LoudHotel> {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("HotelId", HOTELID)
            .writeStringField("HotelName", HOTELNAME)
            .writeStringField("Description", DESCRIPTION)
            .writeStringField("Description_fr", DESCRIPTIONFRENCH)
            .writeStringField("Category", CATEGORY)
            .writeArrayField("Tags", TAGS, JsonWriter::writeString)
            .writeBooleanField("ParkingIncluded", PARKINGINCLUDED)
            .writeBooleanField("SmokingAllowed", SMOKINGALLOWED)
            .writeStringField("LastRenovationDate", Objects.toString(LASTRENOVATIONDATE, null))
            .writeNumberField("Rating", RATING)
            .writeJsonField("Location", LOCATION)
            .writeJsonField("Address", ADDRESS)
            .writeArrayField("Rooms", ROOMS, JsonWriter::writeJson)
            .writeEndObject();
    }

    @SuppressWarnings("deprecation")
    public static LoudHotel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LoudHotel hotel = new LoudHotel();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();


                if ("HotelId".equals(fieldName)) {
                    hotel.HOTELID = reader.getString();
                } else if ("HotelName".equals(fieldName)) {
                    hotel.HOTELNAME = reader.getString();
                } else if ("Description".equals(fieldName)) {
                    hotel.DESCRIPTION = reader.getString();
                } else if ("Description_fr".equals(fieldName)) {
                    hotel.DESCRIPTIONFRENCH = reader.getString();
                } else if ("Category".equals(fieldName)) {
                    hotel.CATEGORY = reader.getString();
                } else if ("Tags".equals(fieldName)) {
                    hotel.TAGS = reader.readArray(JsonReader::getString);
                } else if ("ParkingIncluded".equals(fieldName)) {
                    hotel.PARKINGINCLUDED = reader.getNullable(JsonReader::getBoolean);
                } else if ("SmokingAllowed".equals(fieldName)) {
                    hotel.SMOKINGALLOWED = reader.getNullable(JsonReader::getBoolean);
                } else if ("LastRenovationDate".equals(fieldName)) {
                    hotel.LASTRENOVATIONDATE = reader.getNullable(nonNull -> new Date(nonNull.getString()));
                } else if ("Rating".equals(fieldName)) {
                    hotel.RATING = reader.getNullable(JsonReader::getInt);
                } else if ("Location".equals(fieldName)) {
                    hotel.LOCATION = GeoPoint.fromJson(reader);
                } else if ("Address".equals(fieldName)) {
                    hotel.ADDRESS = HotelAddress.fromJson(reader);
                } else if ("Rooms".equals(fieldName)) {
                    hotel.ROOMS = reader.readArray(HotelRoom::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            return hotel;
        });
    }
}
