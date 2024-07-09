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

/** The CustomMultiClassificationResult model. */
@Fluent
public final class CustomMultiClassificationResult implements JsonSerializable<CustomMultiClassificationResult> {
    /*
     * Response by document
     */
    private List<MultiClassificationDocument> documents;

    /*
     * Errors by document id.
     */
    private List<DocumentError> errors;

    /*
     * if showStats=true was specified in the request this field will contain
     * information about the request payload.
     */
    private RequestStatistics statistics;

    /*
     * This field indicates the project name for the model.
     */
    private String projectName;

    /*
     * This field indicates the deployment name for the model.
     */
    private String deploymentName;

    /**
     * Get the documents property: Response by document.
     *
     * @return the documents value.
     */
    public List<MultiClassificationDocument> getDocuments() {
        return this.documents;
    }

    /**
     * Set the documents property: Response by document.
     *
     * @param documents the documents value to set.
     * @return the CustomMultiClassificationResult object itself.
     */
    public CustomMultiClassificationResult setDocuments(List<MultiClassificationDocument> documents) {
        this.documents = documents;
        return this;
    }

    /**
     * Get the errors property: Errors by document id.
     *
     * @return the errors value.
     */
    public List<DocumentError> getErrors() {
        return this.errors;
    }

    /**
     * Set the errors property: Errors by document id.
     *
     * @param errors the errors value to set.
     * @return the CustomMultiClassificationResult object itself.
     */
    public CustomMultiClassificationResult setErrors(List<DocumentError> errors) {
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
     * @return the CustomMultiClassificationResult object itself.
     */
    public CustomMultiClassificationResult setStatistics(RequestStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * Get the projectName property: This field indicates the project name for the model.
     *
     * @return the projectName value.
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Set the projectName property: This field indicates the project name for the model.
     *
     * @param projectName the projectName value to set.
     * @return the CustomMultiClassificationResult object itself.
     */
    public CustomMultiClassificationResult setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Get the deploymentName property: This field indicates the deployment name for the model.
     *
     * @return the deploymentName value.
     */
    public String getDeploymentName() {
        return this.deploymentName;
    }

    /**
     * Set the deploymentName property: This field indicates the deployment name for the model.
     *
     * @param deploymentName the deploymentName value to set.
     * @return the CustomMultiClassificationResult object itself.
     */
    public CustomMultiClassificationResult setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("documents", this.documents, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("errors", this.errors, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("statistics", this.statistics);
        jsonWriter.writeStringField("projectName", this.projectName);
        jsonWriter.writeStringField("deploymentName", this.deploymentName);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CustomMultiClassificationResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CustomMultiClassificationResult if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the CustomMultiClassificationResult.
     */
    public static CustomMultiClassificationResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CustomMultiClassificationResult deserializedCustomMultiClassificationResult = new CustomMultiClassificationResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("documents".equals(fieldName)) {
                    deserializedCustomMultiClassificationResult.documents
                            = reader.readArray(reader1 -> MultiClassificationDocument.fromJson(reader1));
                } else if ("errors".equals(fieldName)) {
                    deserializedCustomMultiClassificationResult.errors
                            = reader.readArray(reader1 -> DocumentError.fromJson(reader1));
                } else if ("statistics".equals(fieldName)) {
                    deserializedCustomMultiClassificationResult.statistics = RequestStatistics.fromJson(reader);
                } else if ("projectName".equals(fieldName)) {
                    deserializedCustomMultiClassificationResult.projectName = reader.getString();
                } else if ("deploymentName".equals(fieldName)) {
                    deserializedCustomMultiClassificationResult.deploymentName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCustomMultiClassificationResult;
        });
    }
}
