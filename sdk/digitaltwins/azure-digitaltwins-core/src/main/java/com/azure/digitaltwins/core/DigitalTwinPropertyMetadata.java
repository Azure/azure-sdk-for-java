// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contains metadata about changes on properties on a digital twin or component.
 */
@Fluent
public final class DigitalTwinPropertyMetadata implements JsonSerializable<DigitalTwinPropertyMetadata> {
    private OffsetDateTime lastUpdatedOn;
    private OffsetDateTime sourceTime;

    /**
     * Creates a new instance of {@link DigitalTwinPropertyMetadata}.
     */
    public DigitalTwinPropertyMetadata() {
    }

    /**
     * Gets the date and time the property was last updated.
     *
     * @return The date and time the property was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * Sets the date and time the property was last updated.
     *
     * @param lastUpdatedOn The date and time the property was last updated.
     * @return The DigitalTwinPropertyMetadata object itself.
     */
    public DigitalTwinPropertyMetadata setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
        return this;
    }

    /**
     * Gets the date and time the value of the property was sourced.
     *
     * @return The date and time the value of the property was last sourced.
     */
    public OffsetDateTime getSourceTime() {
        return sourceTime;
    }

    /**
     * Sets the date and time the value of the property was sourced.
     *
     * @param sourceTime The date and time the value of the property was last sourced.
     * @return The DigitalTwinPropertyMetadata object itself.
     */
    public DigitalTwinPropertyMetadata setSourceTime(OffsetDateTime sourceTime) {
        this.sourceTime = sourceTime;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.METADATA_PROPERTY_LAST_UPDATE_TIME,
                this.lastUpdatedOn == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.lastUpdatedOn))
            .writeStringField(DigitalTwinsJsonPropertyNames.METADATA_PROPERTY_SOURCE_TIME,
                this.sourceTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.sourceTime))
            .writeEndObject();
    }

    /**
     * Reads an instance of DigitalTwinPropertyMetadata from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DigitalTwinPropertyMetadata if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DigitalTwinPropertyMetadata.
     */
    public static DigitalTwinPropertyMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DigitalTwinPropertyMetadata metadata = new DigitalTwinPropertyMetadata();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.METADATA_PROPERTY_LAST_UPDATE_TIME.equals(fieldName)) {
                    metadata.lastUpdatedOn = reader.getNullable(
                        nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else if (DigitalTwinsJsonPropertyNames.METADATA_PROPERTY_SOURCE_TIME.equals(fieldName)) {
                    metadata.sourceTime = reader.getNullable(
                        nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return metadata;
        });
    }
}
