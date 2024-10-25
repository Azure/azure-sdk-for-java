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

/** Contains a set of input documents to be analyzed by the service. */
@Fluent
public final class MultiLanguageBatchInput implements JsonSerializable<MultiLanguageBatchInput> {
    /*
     * The set of documents to process as part of this batch.
     */
    private List<MultiLanguageInput> documents;

    /**
     * Get the documents property: The set of documents to process as part of this batch.
     *
     * @return the documents value.
     */
    public List<MultiLanguageInput> getDocuments() {
        return this.documents;
    }

    /**
     * Set the documents property: The set of documents to process as part of this batch.
     *
     * @param documents the documents value to set.
     * @return the MultiLanguageBatchInput object itself.
     */
    public MultiLanguageBatchInput setDocuments(List<MultiLanguageInput> documents) {
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
     * Reads an instance of MultiLanguageBatchInput from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MultiLanguageBatchInput if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the MultiLanguageBatchInput.
     */
    public static MultiLanguageBatchInput fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MultiLanguageBatchInput deserializedMultiLanguageBatchInput = new MultiLanguageBatchInput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("documents".equals(fieldName)) {
                    deserializedMultiLanguageBatchInput.documents
                            = reader.readArray(reader1 -> MultiLanguageInput.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedMultiLanguageBatchInput;
        });
    }
}
