// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.IOException;
import java.util.List;

@JsonPropertyOrder({ "Description", "Description_fr", "Type", "BaseRate", "BedOptions", "BedOptions", "SleepsCount", })
public class HotelRoom implements JsonSerializable<HotelRoom> {
    @SimpleField(name = "Description")
    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @SimpleField(name = "Description_fr")
    @JsonProperty(value = "Description_fr")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String descriptionFr;

    @SimpleField(name = "Type")
    @JsonProperty(value = "Type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;

    @SimpleField(name = "BaseRate")
    @JsonProperty(value = "BaseRate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double baseRate;

    @SimpleField(name = "BedOptions")
    @JsonProperty(value = "BedOptions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bedOptions;

    @SimpleField(name = "SleepsCount")
    @JsonProperty(value = "SleepsCount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sleepsCount;

    @SimpleField(name = "SmokingAllowed")
    @JsonProperty(value = "SmokingAllowed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean smokingAllowed;

    @SimpleField(name = "Tags")
    @JsonProperty(value = "Tags")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] tags;

    public String description() {
        return this.description;
    }

    public HotelRoom description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFr() {
        return this.descriptionFr;
    }

    public HotelRoom descriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
        return this;
    }

    public String type() {
        return this.type;
    }

    public HotelRoom type(String type) {
        this.type = type;
        return this;
    }

    public Double baseRate() {
        return this.baseRate;
    }

    public HotelRoom baseRate(Double baseRate) {
        this.baseRate = baseRate;
        return this;
    }

    public String bedOptions() {
        return this.bedOptions;
    }

    public HotelRoom bedOptions(String bedOptions) {
        this.bedOptions = bedOptions;
        return this;
    }

    public Integer sleepsCount() {
        return this.sleepsCount;
    }

    public HotelRoom sleepsCount(Integer sleepsCount) {
        this.sleepsCount = sleepsCount;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public HotelRoom smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public String[] tags() {
        return CoreUtils.clone(this.tags);
    }

    public HotelRoom tags(String[] tags) {
        this.tags = CoreUtils.clone(tags);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Description", description)
            .writeStringField("Description_fr", descriptionFr)
            .writeStringField("Type", type)
            .writeNumberField("BaseRate", baseRate)
            .writeStringField("BedOptions", bedOptions)
            .writeNumberField("SleepCount", sleepsCount)
            .writeBooleanField("SmokingAllowed", smokingAllowed)
            .writeArrayField("Tags", tags, JsonWriter::writeString)
            .writeEndObject();
    }

    public static HotelRoom fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HotelRoom hotelRoom = new HotelRoom();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Description".equals(fieldName)) {
                    hotelRoom.description = reader.getString();
                } else if ("Description_fr".equals(fieldName)) {
                    hotelRoom.descriptionFr = reader.getString();
                } else if ("Type".equals(fieldName)) {
                    hotelRoom.type = reader.getString();
                } else if ("BaseRate".equals(fieldName)) {
                    hotelRoom.baseRate = reader.getNullable(JsonReader::getDouble);
                } else if ("BedOptions".equals(fieldName)) {
                    hotelRoom.bedOptions = reader.getString();
                } else if ("SleepCount".equals(fieldName)) {
                    hotelRoom.sleepsCount = reader.getNullable(JsonReader::getInt);
                } else if ("SmokingAllowed".equals(fieldName)) {
                    hotelRoom.smokingAllowed = reader.getNullable(JsonReader::getBoolean);
                } else if ("Tags".equals(fieldName)) {
                    List<String> tags = reader.readArray(JsonReader::getString);
                    if (tags != null) {
                        hotelRoom.tags = tags.toArray(new String[0]);
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return hotelRoom;
        });
    }
}
