// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The TasksStateTasksEntityRecognitionTasksItem model. */
@Fluent
public class TasksStateTasksEntityRecognitionTasksItem extends TaskState {
    /*
     * The results property.
     */
    private EntitiesResult results;

    /**
     * Get the results property: The results property.
     *
     * @return the results value.
     */
    public EntitiesResult getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     *
     * @param results the results value to set.
     * @return the TasksStateTasksEntityRecognitionTasksItem object itself.
     */
    public TasksStateTasksEntityRecognitionTasksItem setResults(EntitiesResult results) {
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
     * Reads an instance of TasksStateTasksEntityRecognitionTasksItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TasksStateTasksEntityRecognitionTasksItem if the JsonReader was pointing to an instance
     * of it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TasksStateTasksEntityRecognitionTasksItem.
     */
    public static TasksStateTasksEntityRecognitionTasksItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TasksStateTasksEntityRecognitionTasksItem deserializedTasksStateTasksEntityRecognitionTasksItem =
                    new TasksStateTasksEntityRecognitionTasksItem();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("results".equals(fieldName)) {
                    deserializedTasksStateTasksEntityRecognitionTasksItem.results = EntitiesResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedTasksStateTasksEntityRecognitionTasksItem;
        });
    }
}
