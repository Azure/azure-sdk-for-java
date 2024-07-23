// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The AnalyzeJobState model. */
@Fluent
public final class AnalyzeJobState extends AnalyzeJobMetadata {
    /*
     * The tasks property.
     */
    private TasksStateTasksOld tasks;

    /*
     * The errors property.
     */
    private List<TextAnalyticsError> errors;

    /*
     * if showStats=true was specified in the request this field will contain
     * information about the request payload.
     */
    private RequestStatistics statistics;

    /*
     * The @nextLink property.
     */
    private String nextLink;

    /**
     * Get the tasks property: The tasks property.
     *
     * @return the tasks value.
     */
    public TasksStateTasksOld getTasks() {
        return this.tasks;
    }

    /**
     * Set the tasks property: The tasks property.
     *
     * @param tasks the tasks value to set.
     * @return the AnalyzeJobState object itself.
     */
    public AnalyzeJobState setTasks(TasksStateTasksOld tasks) {
        this.tasks = tasks;
        return this;
    }

    /**
     * Get the errors property: The errors property.
     *
     * @return the errors value.
     */
    public List<TextAnalyticsError> getErrors() {
        return this.errors;
    }

    /**
     * Set the errors property: The errors property.
     *
     * @param errors the errors value to set.
     * @return the AnalyzeJobState object itself.
     */
    public AnalyzeJobState setErrors(List<TextAnalyticsError> errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Get the statistics property: if showStats=true was specified in the request this field will contain information
     * about the request payload.
     *
     * @return the statistics value.
     */
    public RequestStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Set the statistics property: if showStats=true was specified in the request this field will contain information
     * about the request payload.
     *
     * @param statistics the statistics value to set.
     * @return the AnalyzeJobState object itself.
     */
    public AnalyzeJobState setStatistics(RequestStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * Get the nextLink property: The @nextLink property.
     *
     * @return the nextLink value.
     */
    public String getNextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The @nextLink property.
     *
     * @param nextLink the nextLink value to set.
     * @return the AnalyzeJobState object itself.
     */
    public AnalyzeJobState setNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("tasks", this.tasks);
        jsonWriter.writeArrayField("errors", this.errors, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("statistics", this.statistics);
        jsonWriter.writeStringField("@nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AnalyzeJobState from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AnalyzeJobState if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AnalyzeJobState.
     */
    public static AnalyzeJobState fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AnalyzeJobState deserializedAnalyzeJobState = new AnalyzeJobState();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("tasks".equals(fieldName)) {
                    deserializedAnalyzeJobState.tasks = TasksStateTasksOld.fromJson(reader);
                } else if ("errors".equals(fieldName)) {
                    deserializedAnalyzeJobState.errors = reader.readArray(reader1 -> TextAnalyticsError.fromJson(reader1));
                } else if ("statistics".equals(fieldName)) {
                    deserializedAnalyzeJobState.statistics = RequestStatistics.fromJson(reader);
                } else if ("@nextLink".equals(fieldName)) {
                    deserializedAnalyzeJobState.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedAnalyzeJobState;
        });
    }
}
