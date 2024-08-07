// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Supported auto pause delay time range.
 */
@Immutable
public final class AutoPauseDelayTimeRange implements JsonSerializable<AutoPauseDelayTimeRange> {
    /*
     * Minimum value
     */
    private Integer minValue;

    /*
     * Maximum value
     */
    private Integer maxValue;

    /*
     * Step value for discrete values between the minimum value and the maximum value.
     */
    private Integer stepSize;

    /*
     * Default value is no value is provided
     */
    private Integer defaultProperty;

    /*
     * Unit of time that delay is expressed in
     */
    private PauseDelayTimeUnit unit;

    /*
     * Value that is used to not pause (infinite delay before pause)
     */
    private Integer doNotPauseValue;

    /**
     * Creates an instance of AutoPauseDelayTimeRange class.
     */
    public AutoPauseDelayTimeRange() {
    }

    /**
     * Get the minValue property: Minimum value.
     * 
     * @return the minValue value.
     */
    public Integer minValue() {
        return this.minValue;
    }

    /**
     * Get the maxValue property: Maximum value.
     * 
     * @return the maxValue value.
     */
    public Integer maxValue() {
        return this.maxValue;
    }

    /**
     * Get the stepSize property: Step value for discrete values between the minimum value and the maximum value.
     * 
     * @return the stepSize value.
     */
    public Integer stepSize() {
        return this.stepSize;
    }

    /**
     * Get the defaultProperty property: Default value is no value is provided.
     * 
     * @return the defaultProperty value.
     */
    public Integer defaultProperty() {
        return this.defaultProperty;
    }

    /**
     * Get the unit property: Unit of time that delay is expressed in.
     * 
     * @return the unit value.
     */
    public PauseDelayTimeUnit unit() {
        return this.unit;
    }

    /**
     * Get the doNotPauseValue property: Value that is used to not pause (infinite delay before pause).
     * 
     * @return the doNotPauseValue value.
     */
    public Integer doNotPauseValue() {
        return this.doNotPauseValue;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AutoPauseDelayTimeRange from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of AutoPauseDelayTimeRange if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AutoPauseDelayTimeRange.
     */
    public static AutoPauseDelayTimeRange fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AutoPauseDelayTimeRange deserializedAutoPauseDelayTimeRange = new AutoPauseDelayTimeRange();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("minValue".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.minValue = reader.getNullable(JsonReader::getInt);
                } else if ("maxValue".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.maxValue = reader.getNullable(JsonReader::getInt);
                } else if ("stepSize".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.stepSize = reader.getNullable(JsonReader::getInt);
                } else if ("default".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.defaultProperty = reader.getNullable(JsonReader::getInt);
                } else if ("unit".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.unit = PauseDelayTimeUnit.fromString(reader.getString());
                } else if ("doNotPauseValue".equals(fieldName)) {
                    deserializedAutoPauseDelayTimeRange.doNotPauseValue = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAutoPauseDelayTimeRange;
        });
    }
}
