// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The JobDescriptor model. */
@Fluent
public class JobDescriptor implements JsonSerializable<LanguageResult> {
    /*
     * Optional display name for the analysis job.
     */
    private String displayName;

    /**
     * Get the displayName property: Optional display name for the analysis job.
     *
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: Optional display name for the analysis job.
     *
     * @param displayName the displayName value to set.
     * @return the JobDescriptor object itself.
     */
    public JobDescriptor setDisplayName(String displayName) {
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
     * Reads an instance of JobDescriptor from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of JobDescriptor if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the JobDescriptor.
     */
    public static JobDescriptor fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JobDescriptor deserializedJobDescriptor = new JobDescriptor();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("displayName".equals(fieldName)) {
                    deserializedJobDescriptor.displayName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedJobDescriptor;
        });
    }
}
