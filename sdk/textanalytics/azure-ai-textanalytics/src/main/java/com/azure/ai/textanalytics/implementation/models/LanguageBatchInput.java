// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The LanguageBatchInput model. */
@Fluent
public final class LanguageBatchInput implements JsonSerializable<LanguageBatchInput> {
    /*
     * The documents property.
     */
    private List<LanguageInput> documents;

    /**
     * Get the documents property: The documents property.
     *
     * @return the documents value.
     */
    public List<LanguageInput> getDocuments() {
        return this.documents;
    }

    /**
     * Set the documents property: The documents property.
     *
     * @param documents the documents value to set.
     * @return the LanguageBatchInput object itself.
     */
    public LanguageBatchInput setDocuments(List<LanguageInput> documents) {
        this.documents = documents;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("documents", this.documents, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of LanguageBatchInput from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of LanguageBatchInput if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the LanguageBatchInput.
     */
    public static LanguageBatchInput fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LanguageBatchInput deserializedLanguageBatchInput = new LanguageBatchInput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("documents".equals(fieldName)) {
                    deserializedLanguageBatchInput.documents =
                            reader.readArray(reader1 -> LanguageInput.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedLanguageBatchInput;
        });
    }
}
