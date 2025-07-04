// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The HealthcareLROResult model.
 */
@Fluent
public final class HealthcareLROResult extends AnalyzeTextLROResult {
    /*
     * Enumeration of supported Text Analysis long-running operation task results.
     */
    @Generated
    private AnalyzeTextLROResultsKind kind = AnalyzeTextLROResultsKind.HEALTHCARE_LRORESULTS;

    /*
     * The results property.
     */
    @Generated
    private HealthcareResult results;

    /**
     * Creates an instance of HealthcareLROResult class.
     */
    @Generated
    public HealthcareLROResult() {
    }

    /**
     * Get the kind property: Enumeration of supported Text Analysis long-running operation task results.
     * 
     * @return the kind value.
     */
    @Generated
    @Override
    public AnalyzeTextLROResultsKind getKind() {
        return this.kind;
    }

    /**
     * Get the results property: The results property.
     * 
     * @return the results value.
     */
    @Generated
    public HealthcareResult getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     * 
     * @param results the results value to set.
     * @return the HealthcareLROResult object itself.
     */
    @Generated
    public HealthcareLROResult setResults(HealthcareResult results) {
        this.results = results;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public HealthcareLROResult setTaskName(String taskName) {
        super.setTaskName(taskName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public HealthcareLROResult setLastUpdateDateTime(OffsetDateTime lastUpdateDateTime) {
        super.setLastUpdateDateTime(lastUpdateDateTime);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public HealthcareLROResult setStatus(State status) {
        super.setStatus(status);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("lastUpdateDateTime",
            getLastUpdateDateTime() == null
                ? null
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(getLastUpdateDateTime()));
        jsonWriter.writeStringField("status", getStatus() == null ? null : getStatus().toString());
        jsonWriter.writeStringField("taskName", getTaskName());
        jsonWriter.writeJsonField("results", this.results);
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HealthcareLROResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of HealthcareLROResult if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the HealthcareLROResult.
     */
    @Generated
    public static HealthcareLROResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HealthcareLROResult deserializedHealthcareLROResult = new HealthcareLROResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("lastUpdateDateTime".equals(fieldName)) {
                    deserializedHealthcareLROResult.setLastUpdateDateTime(reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString())));
                } else if ("status".equals(fieldName)) {
                    deserializedHealthcareLROResult.setStatus(State.fromString(reader.getString()));
                } else if ("taskName".equals(fieldName)) {
                    deserializedHealthcareLROResult.setTaskName(reader.getString());
                } else if ("results".equals(fieldName)) {
                    deserializedHealthcareLROResult.results = HealthcareResult.fromJson(reader);
                } else if ("kind".equals(fieldName)) {
                    deserializedHealthcareLROResult.kind = AnalyzeTextLROResultsKind.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedHealthcareLROResult;
        });
    }
}
