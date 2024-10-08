// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dataprotection.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The RestorableTimeRange model.
 */
@Fluent
public final class RestorableTimeRange implements JsonSerializable<RestorableTimeRange> {
    /*
     * Start time for the available restore range
     */
    private String startTime;

    /*
     * End time for the available restore range
     */
    private String endTime;

    /*
     * The objectType property.
     */
    private String objectType;

    /**
     * Creates an instance of RestorableTimeRange class.
     */
    public RestorableTimeRange() {
    }

    /**
     * Get the startTime property: Start time for the available restore range.
     * 
     * @return the startTime value.
     */
    public String startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime property: Start time for the available restore range.
     * 
     * @param startTime the startTime value to set.
     * @return the RestorableTimeRange object itself.
     */
    public RestorableTimeRange withStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime property: End time for the available restore range.
     * 
     * @return the endTime value.
     */
    public String endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime property: End time for the available restore range.
     * 
     * @param endTime the endTime value to set.
     * @return the RestorableTimeRange object itself.
     */
    public RestorableTimeRange withEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the objectType property: The objectType property.
     * 
     * @return the objectType value.
     */
    public String objectType() {
        return this.objectType;
    }

    /**
     * Set the objectType property: The objectType property.
     * 
     * @param objectType the objectType value to set.
     * @return the RestorableTimeRange object itself.
     */
    public RestorableTimeRange withObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (startTime() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property startTime in model RestorableTimeRange"));
        }
        if (endTime() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property endTime in model RestorableTimeRange"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(RestorableTimeRange.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("startTime", this.startTime);
        jsonWriter.writeStringField("endTime", this.endTime);
        jsonWriter.writeStringField("objectType", this.objectType);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RestorableTimeRange from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RestorableTimeRange if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the RestorableTimeRange.
     */
    public static RestorableTimeRange fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RestorableTimeRange deserializedRestorableTimeRange = new RestorableTimeRange();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("startTime".equals(fieldName)) {
                    deserializedRestorableTimeRange.startTime = reader.getString();
                } else if ("endTime".equals(fieldName)) {
                    deserializedRestorableTimeRange.endTime = reader.getString();
                } else if ("objectType".equals(fieldName)) {
                    deserializedRestorableTimeRange.objectType = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRestorableTimeRange;
        });
    }
}
