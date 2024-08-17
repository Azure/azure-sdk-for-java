// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The AnalyzeJobMetadata model. */
@Fluent
public class AnalyzeJobMetadata extends JobMetadata {
    /*
     * The displayName property.
     */
    private String displayName;

    /**
     * Get the displayName property: The displayName property.
     *
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: The displayName property.
     *
     * @param displayName the displayName value to set.
     * @return the AnalyzeJobMetadata object itself.
     */
    public AnalyzeJobMetadata setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("displayName", this.displayName);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AnalyzeJobMetadata from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AnalyzeJobMetadata if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AnalyzeJobMetadata.
     */
    public static AnalyzeJobMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AnalyzeJobMetadata deserializedAnalyzeJobMetadata = new AnalyzeJobMetadata();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("displayName".equals(fieldName)) {
                    deserializedAnalyzeJobMetadata.displayName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAnalyzeJobMetadata;
        });
    }
}
