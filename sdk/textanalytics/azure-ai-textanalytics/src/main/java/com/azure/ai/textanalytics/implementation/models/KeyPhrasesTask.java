// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The KeyPhrasesTask model. */
@Fluent
public final class KeyPhrasesTask implements JsonSerializable<KeyPhrasesTask> {
    /*
     * The parameters property.
     */
    private KeyPhraseTaskParameters parameters;

    /*
     * The taskName property.
     */
    private String taskName;

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    public KeyPhraseTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     *
     * @param parameters the parameters value to set.
     * @return the KeyPhrasesTask object itself.
     */
    public KeyPhrasesTask setParameters(KeyPhraseTaskParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Get the taskName property: The taskName property.
     *
     * @return the taskName value.
     */
    public String getTaskName() {
        return this.taskName;
    }

    /**
     * Set the taskName property: The taskName property.
     *
     * @param taskName the taskName value to set.
     * @return the KeyPhrasesTask object itself.
     */
    public KeyPhrasesTask setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("parameters", parameters);
        jsonWriter.writeStringField("taskName", this.taskName);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of KeyPhrasesTask from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of KeyPhrasesTask if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the KeyPhrasesTask.
     */
    public static KeyPhrasesTask fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            KeyPhrasesTask deserializedKeyPhrasesTask = new KeyPhrasesTask();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("parameters".equals(fieldName)) {
                    deserializedKeyPhrasesTask.parameters = KeyPhraseTaskParameters.fromJson(reader);
                } else if ("taskName".equals(fieldName)) {
                    deserializedKeyPhrasesTask.taskName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedKeyPhrasesTask;
        });
    }
}
