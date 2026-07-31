// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class HotelAddress implements JsonSerializable<HotelAddress> {
    @BasicField(name = "StreetAddress", isFacetable = BasicField.BooleanHelper.TRUE)
    @JsonProperty(value = "StreetAddress")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String streetAddress;

    @BasicField(
        name = "City",
        isSearchable = BasicField.BooleanHelper.TRUE,
        isFilterable = BasicField.BooleanHelper.TRUE)
    @JsonProperty(value = "City")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String city;

    @BasicField(name = "StateProvince", isSearchable = BasicField.BooleanHelper.TRUE)
    @JsonProperty(value = "StateProvince")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String stateProvince;

    @BasicField(name = "Country", isSearchable = BasicField.BooleanHelper.TRUE, synonymMapNames = { "fieldbuilder" })
    @JsonProperty(value = "Country")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String country;

    @BasicField(name = "PostalCode")
    @JsonProperty(value = "PostalCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String postalCode;

    public String streetAddress() {
        return this.streetAddress;
    }

    public HotelAddress streetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        return this;
    }

    public String city() {
        return this.city;
    }

    public HotelAddress city(String city) {
        this.city = city;
        return this;
    }

    public String stateProvince() {
        return this.stateProvince;
    }

    public HotelAddress stateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
        return this;
    }

    public String country() {
        return this.country;
    }

    public HotelAddress country(String country) {
        this.country = country;
        return this;
    }

    public String postalCode() {
        return this.postalCode;
    }

    public HotelAddress postalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("StreetAddress", streetAddress)
            .writeStringField("City", city)
            .writeStringField("StateProvince", stateProvince)
            .writeStringField("Country", country)
            .writeStringField("PostalCode", postalCode)
            .writeEndObject();
    }

    public static HotelAddress fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HotelAddress hotelAddress = new HotelAddress();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("StreetAddress".equals(fieldName)) {
                    hotelAddress.streetAddress = reader.getString();
                } else if ("City".equals(fieldName)) {
                    hotelAddress.city = reader.getString();
                } else if ("StateProvince".equals(fieldName)) {
                    hotelAddress.stateProvince = reader.getString();
                } else if ("Country".equals(fieldName)) {
                    hotelAddress.country = reader.getString();
                } else if ("PostalCode".equals(fieldName)) {
                    hotelAddress.postalCode = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return hotelAddress;
        });
    }
}
