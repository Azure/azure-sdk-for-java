// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The ExtractiveSummarizationTask model. */
@Fluent
public final class ExtractiveSummarizationTask implements JsonSerializable<ExtractiveSummarizationTask> {
    /*
     * The parameters property.
     */
    private ExtractiveSummarizationTaskParameters parameters;

    /*
     * The taskName property.
     */
    private String taskName;

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    public ExtractiveSummarizationTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     *
     * @param parameters the parameters value to set.
     * @return the ExtractiveSummarizationTask object itself.
     */
    public ExtractiveSummarizationTask setParameters(ExtractiveSummarizationTaskParameters parameters) {
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
     * @return the ExtractiveSummarizationTask object itself.
     */
    public ExtractiveSummarizationTask setTaskName(String taskName) {
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
     * Reads an instance of ExtractiveSummarizationTask from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ExtractiveSummarizationTask if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ExtractiveSummarizationTask.
     */
    public static ExtractiveSummarizationTask fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ExtractiveSummarizationTask deserializedExtractiveSummarizationTask = new ExtractiveSummarizationTask();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("parameters".equals(fieldName)) {
                    deserializedExtractiveSummarizationTask.parameters = ExtractiveSummarizationTaskParameters.fromJson(reader);
                } else if ("taskName".equals(fieldName)) {
                    deserializedExtractiveSummarizationTask.taskName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedExtractiveSummarizationTask;
        });
    }
}
