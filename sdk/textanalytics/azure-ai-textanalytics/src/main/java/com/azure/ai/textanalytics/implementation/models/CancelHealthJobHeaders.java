// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The CancelHealthJobHeaders model. */
@Fluent
public final class CancelHealthJobHeaders implements JsonSerializable<CancelHealthJobHeaders> {
    /*
     * The Operation-Location property.
     */
    private String operationLocation;

    /**
     * Get the operationLocation property: The Operation-Location property.
     *
     * @return the operationLocation value.
     */
    public String getOperationLocation() {
        return this.operationLocation;
    }

    /**
     * Set the operationLocation property: The Operation-Location property.
     *
     * @param operationLocation the operationLocation value to set.
     * @return the CancelHealthJobHeaders object itself.
     */
    public CancelHealthJobHeaders setOperationLocation(String operationLocation) {
        this.operationLocation = operationLocation;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("Operation-Location", this.operationLocation);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CancelHealthJobHeaders from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CancelHealthJobHeaders if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the CancelHealthJobHeaders.
     */
    public static CancelHealthJobHeaders fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CancelHealthJobHeaders deserializedCancelHealthJobHeaders = new CancelHealthJobHeaders();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("Operation-Location".equals(fieldName)) {
                    deserializedCancelHealthJobHeaders.operationLocation = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedCancelHealthJobHeaders;
        });
    }
}
