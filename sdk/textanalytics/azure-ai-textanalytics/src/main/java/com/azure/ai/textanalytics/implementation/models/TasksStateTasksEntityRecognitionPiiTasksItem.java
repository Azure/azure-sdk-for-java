// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The TasksStateTasksEntityRecognitionPiiTasksItem model. */
@Fluent
public final class TasksStateTasksEntityRecognitionPiiTasksItem extends TaskState {
    /*
     * The results property.
     */
    private PiiResult results;

    /**
     * Get the results property: The results property.
     *
     * @return the results value.
     */
    public PiiResult getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     *
     * @param results the results value to set.
     * @return the TasksStateTasksEntityRecognitionPiiTasksItem object itself.
     */
    public TasksStateTasksEntityRecognitionPiiTasksItem setResults(PiiResult results) {
        this.results = results;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("results", results);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TasksStateTasksEntityRecognitionPiiTasksItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TasksStateTasksEntityRecognitionPiiTasksItem if the JsonReader was pointing to an instance
     * of it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TasksStateTasksEntityRecognitionPiiTasksItem.
     */
    public static TasksStateTasksEntityRecognitionPiiTasksItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TasksStateTasksEntityRecognitionPiiTasksItem deserializedTasksStateTasksEntityRecognitionPiiTasksItem =
                    new TasksStateTasksEntityRecognitionPiiTasksItem();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("results".equals(fieldName)) {
                    deserializedTasksStateTasksEntityRecognitionPiiTasksItem.results = PiiResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedTasksStateTasksEntityRecognitionPiiTasksItem;
        });
    }
}
