// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.models;

import com.azure.core.annotation.Generated;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Status and result of the analyze operation.
 */
public final class AnalyzeResultOperation implements JsonSerializable<AnalyzeResultOperation> {
    /*
     * Operation status. notStarted, running, succeeded, or failed
     */
    private final OperationStatus status;

    /*
     * Date and time (UTC) when the analyze operation was submitted.
     */
    private final OffsetDateTime createdDateTime;

    /*
     * Date and time (UTC) when the status was last updated.
     */
    private final OffsetDateTime lastUpdatedDateTime;

    /*
     * Encountered error during document analysis.
     */
    private Error error;

    /*
     * Document analysis result.
     */
    private AnalyzeResult analyzeResult;

    private String operationId;
    /**
     * Creates an instance of AnalyzeResultOperation class.
     *
     * @param status the status value to set.
     * @param createdDateTime the createdDateTime value to set.
     * @param lastUpdatedDateTime the lastUpdatedDateTime value to set.
     */
    private AnalyzeResultOperation(OperationStatus status, OffsetDateTime createdDateTime,
        OffsetDateTime lastUpdatedDateTime) {
        this.status = status;
        this.createdDateTime = createdDateTime;
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    /**
     * Get the status property: Operation status. notStarted, running, succeeded, or failed.
     *
     * @return the status value.
     */
    public OperationStatus getStatus() {
        return this.status;
    }

    /**
     * Get the createdDateTime property: Date and time (UTC) when the analyze operation was submitted.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedDateTime() {
        return this.createdDateTime;
    }

    /**
     * Get the lastUpdatedDateTime property: Date and time (UTC) when the status was last updated.
     *
     * @return the lastUpdatedDateTime value.
     */
    public OffsetDateTime getLastUpdatedDateTime() {
        return this.lastUpdatedDateTime;
    }

    /**
     * Get the error property: Encountered error during document analysis.
     *
     * @return the error value.
     */
    public Error getError() {
        return this.error;
    }

    /**
     * Get the analyzeResult property: Document analysis result.
     *
     * @return the analyzeResult value.
     */
    public AnalyzeResult getAnalyzeResult() {
        return this.analyzeResult;
    }

    /**
     * Gets the operationId property: Operation ID.
     * @return the operationId value.
     */
    public String getOperationId() { // Add getter method
        return this.operationId;
    }

    /**
     * Sets the operationId property: Operation ID.
     * @param operationId the operationId value to set.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("status", this.status == null ? null : this.status.toString());
        jsonWriter.writeStringField("createdDateTime",
            this.createdDateTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.createdDateTime));
        jsonWriter.writeStringField("lastUpdatedDateTime",
            this.lastUpdatedDateTime == null
                ? null
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.lastUpdatedDateTime));
        jsonWriter.writeJsonField("error", this.error);
        jsonWriter.writeJsonField("analyzeResult", this.analyzeResult);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AnalyzeResultOperation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AnalyzeResultOperation if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AnalyzeResultOperation.
     */
    public static AnalyzeResultOperation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            OperationStatus status = null;
            OffsetDateTime createdDateTime = null;
            OffsetDateTime lastUpdatedDateTime = null;
            Error error = null;
            AnalyzeResult analyzeResult = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("status".equals(fieldName)) {
                    status = OperationStatus.fromString(reader.getString());
                } else if ("createdDateTime".equals(fieldName)) {
                    createdDateTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("lastUpdatedDateTime".equals(fieldName)) {
                    lastUpdatedDateTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("error".equals(fieldName)) {
                    error = Error.fromJson(reader);
                } else if ("analyzeResult".equals(fieldName)) {
                    analyzeResult = AnalyzeResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            AnalyzeResultOperation deserializedAnalyzeResultOperation
                = new AnalyzeResultOperation(status, createdDateTime, lastUpdatedDateTime);
            deserializedAnalyzeResultOperation.error = error;
            deserializedAnalyzeResultOperation.analyzeResult = analyzeResult;

            return deserializedAnalyzeResultOperation;
        });
    }
}
