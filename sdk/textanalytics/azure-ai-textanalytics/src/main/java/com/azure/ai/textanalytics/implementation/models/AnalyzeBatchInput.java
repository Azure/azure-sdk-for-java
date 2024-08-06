// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The AnalyzeBatchInput model. */
@Fluent
public final class AnalyzeBatchInput extends JobDescriptor {
    /*
     * Contains a set of input documents to be analyzed by the service.
     */
    private MultiLanguageBatchInput analysisInput;

    /*
     * The set of tasks to execute on the input documents. Cannot specify the
     * same task more than once.
     */
    private JobManifestTasks tasks;

    /**
     * Get the analysisInput property: Contains a set of input documents to be analyzed by the service.
     *
     * @return the analysisInput value.
     */
    public MultiLanguageBatchInput getAnalysisInput() {
        return this.analysisInput;
    }

    /**
     * Set the analysisInput property: Contains a set of input documents to be analyzed by the service.
     *
     * @param analysisInput the analysisInput value to set.
     * @return the AnalyzeBatchInput object itself.
     */
    public AnalyzeBatchInput setAnalysisInput(MultiLanguageBatchInput analysisInput) {
        this.analysisInput = analysisInput;
        return this;
    }

    /**
     * Get the tasks property: The set of tasks to execute on the input documents. Cannot specify the same task more
     * than once.
     *
     * @return the tasks value.
     */
    public JobManifestTasks getTasks() {
        return this.tasks;
    }

    /**
     * Set the tasks property: The set of tasks to execute on the input documents. Cannot specify the same task more
     * than once.
     *
     * @param tasks the tasks value to set.
     * @return the AnalyzeBatchInput object itself.
     */
    public AnalyzeBatchInput setTasks(JobManifestTasks tasks) {
        this.tasks = tasks;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("analysisInput", this.analysisInput);
        jsonWriter.writeJsonField("tasks", this.tasks);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AnalyzeBatchInput from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AnalyzeBatchInput if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AnalyzeBatchInput.
     */
    public static AnalyzeBatchInput fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AnalyzeBatchInput deserializedAnalyzeBatchInput = new AnalyzeBatchInput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("analysisInput".equals(fieldName)) {
                    deserializedAnalyzeBatchInput.analysisInput = MultiLanguageBatchInput.fromJson(reader);
                } else if ("tasks".equals(fieldName)) {
                    deserializedAnalyzeBatchInput.tasks = JobManifestTasks.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAnalyzeBatchInput;
        });
    }
}
