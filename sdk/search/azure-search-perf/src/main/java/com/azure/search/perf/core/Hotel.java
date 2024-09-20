// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Model class representing a hotel.
 */
public class Hotel implements JsonSerializable<Hotel> {
    /**
     * Hotel ID
     */
    @SimpleField(isKey = true)
    public String hotelId;

    /**
     * Hotel name
     */
    @SearchableField(isSortable = true)
    public String hotelName;

    /**
     * Description
     */
    @SearchableField(analyzerName = "en.microsoft")
    public String description;

    /**
     * French description
     */
    @SearchableField(analyzerName = "fr.lucene")
    public String descriptionFr;

    /**
     * Category
     */
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String category;

    /**
     * Tags
     */
    @SearchableField(isFilterable = true, isFacetable = true)
    public String[] tags;

    /**
     * Whether parking is included
     */
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Boolean parkingIncluded;

    /**
     * Last renovation time
     */
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public OffsetDateTime lastRenovationDate;

    /**
     * Rating
     */
    @SimpleField(isFilterable = true, isSortable = true, isFacetable = true)
    public Double rating;

    /**
     * Address
     */
    public Address address;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("HotelId", hotelId)
            .writeStringField("HotelName", hotelName)
            .writeStringField("Description", description)
            .writeStringField("DescriptionFr", descriptionFr)
            .writeStringField("Category", category)
            .writeArrayField("Tags", tags, JsonWriter::writeString)
            .writeBooleanField("ParkingIncluded", parkingIncluded)
            .writeStringField("LastRenovationDate", Objects.toString(lastRenovationDate, null))
            .writeNumberField("Rating", rating)
            .writeJsonField("Address", address)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of {@link Hotel} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link Hotel}, or null if the {@link JsonReader} was pointing to {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static Hotel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Hotel hotel = new Hotel();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("HotelId".equals(fieldName)) {
                    hotel.hotelId = reader.getString();
                } else if ("HotelName".equals(fieldName)) {
                    hotel.hotelName = reader.getString();
                } else if ("Description".equals(fieldName)) {
                    hotel.description = reader.getString();
                } else if ("DescriptionFr".equals(fieldName)) {
                    hotel.descriptionFr = reader.getString();
                } else if ("Category".equals(fieldName)) {
                    hotel.category = reader.getString();
                } else if ("Tags".equals(fieldName)) {
                    hotel.tags = reader.getNullable(nonNull ->
                        nonNull.readArray(JsonReader::getString).toArray(new String[0]));
                } else if ("ParkingIncluded".equals(fieldName)) {
                    hotel.parkingIncluded = reader.getNullable(JsonReader::getBoolean);
                } else if ("LastRenovationDate".equals(fieldName)) {
                    hotel.lastRenovationDate = reader.getNullable(nonNull ->
                        CoreUtils.parseBestOffsetDateTime(nonNull.getString()));
                } else if ("Rating".equals(fieldName)) {
                    hotel.rating = reader.getNullable(JsonReader::getDouble);
                } else if ("Address".equals(fieldName)) {
                    hotel.address = Address.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return hotel;
        });
    }
}
