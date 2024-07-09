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

/** The LanguageResult model. */
@Fluent
public final class LanguageResult implements JsonSerializable<LanguageResult> {
    /*
     * Response by document
     */
    private List<DocumentLanguage> documents;

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
     * This field indicates which model is used for scoring.
     */
    private String modelVersion;

    /**
     * Get the documents property: Response by document.
     *
     * @return the documents value.
     */
    public List<DocumentLanguage> getDocuments() {
        return this.documents;
    }

    /**
     * Set the documents property: Response by document.
     *
     * @param documents the documents value to set.
     * @return the LanguageResult object itself.
     */
    public LanguageResult setDocuments(List<DocumentLanguage> documents) {
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
     * @return the LanguageResult object itself.
     */
    public LanguageResult setErrors(List<DocumentError> errors) {
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
     * @return the LanguageResult object itself.
     */
    public LanguageResult setStatistics(RequestStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * Get the modelVersion property: This field indicates which model is used for scoring.
     *
     * @return the modelVersion value.
     */
    public String getModelVersion() {
        return this.modelVersion;
    }

    /**
     * Set the modelVersion property: This field indicates which model is used for scoring.
     *
     * @param modelVersion the modelVersion value to set.
     * @return the LanguageResult object itself.
     */
    public LanguageResult setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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
        jsonWriter.writeStringField("modelVersion", this.modelVersion);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of LanguageResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of LanguageResult if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the LanguageResult.
     */
    public static LanguageResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LanguageResult deserializedLanguageResult = new LanguageResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("documents".equals(fieldName)) {
                    deserializedLanguageResult.documents = reader.readArray(reader1 -> DocumentLanguage.fromJson(reader1));
                } else if ("errors".equals(fieldName)) {
                    deserializedLanguageResult.errors = reader.readArray(reader1 -> DocumentError.fromJson(reader1));
                } else if ("statistics".equals(fieldName)) {
                    deserializedLanguageResult.statistics = RequestStatistics.fromJson(reader);
                } else if ("modelVersion".equals(fieldName)) {
                    deserializedLanguageResult.modelVersion = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedLanguageResult;
        });
    }
}
