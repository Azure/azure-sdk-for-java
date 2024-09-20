// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchableField;

import java.io.IOException;

/**
 * Model class representing an address.
 */
public class Address implements JsonSerializable<Address> {
    /**
     * Street address
     */
    @SearchableField
    public String streetAddress;

    /**
     * City
     */
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String city;

    /**
     * State or province
     */
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String stateProvince;

    /**
     * Postal code
     */
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String postalCode;

    /**
     * Country
     */
    @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
    public String country;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("StreetAddress", streetAddress)
            .writeStringField("City", city)
            .writeStringField("StateProvince", stateProvince)
            .writeStringField("PostalCode", postalCode)
            .writeStringField("Country", country)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of {@link Address} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link Address}, or null if the {@link JsonReader} was pointing to {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static Address fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Address address = new Address();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("StreetAddress".equals(fieldName)) {
                    address.streetAddress = reader.getString();
                } else if ("City".equals(fieldName)) {
                    address.city = reader.getString();
                } else if ("StateProvince".equals(fieldName)) {
                    address.stateProvince = reader.getString();
                } else if ("PostalCode".equals(fieldName)) {
                    address.postalCode = reader.getString();
                } else if ("Country".equals(fieldName)) {
                    address.country = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return address;
        });
    }
}
