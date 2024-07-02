// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The HealthcareJobState model. */
@Fluent
public final class HealthcareJobState extends JobMetadata {
    /*
     * The results property.
     */
    private HealthcareResult results;

    /*
     * The errors property.
     */
    private List<Error> errors;

    /*
     * The @nextLink property.
     */
    private String nextLink;

    /**
     * Get the results property: The results property.
     *
     * @return the results value.
     */
    public HealthcareResult getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     *
     * @param results the results value to set.
     * @return the HealthcareJobState object itself.
     */
    public HealthcareJobState setResults(HealthcareResult results) {
        this.results = results;
        return this;
    }

    /**
     * Get the errors property: The errors property.
     *
     * @return the errors value.
     */
    public List<Error> getErrors() {
        return this.errors;
    }

    /**
     * Set the errors property: The errors property.
     *
     * @param errors the errors value to set.
     * @return the HealthcareJobState object itself.
     */
    public HealthcareJobState setErrors(List<Error> errors) {
        this.errors = errors;
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
     * @return the HealthcareJobState object itself.
     */
    public HealthcareJobState setNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("results", this.results);
        jsonWriter.writeArrayField("errors", this.errors, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("@nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HealthcareJobState from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of HealthcareJobState if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the HealthcareJobState.
     */
    public static HealthcareJobState fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HealthcareJobState deserializedHealthcareJobState = new HealthcareJobState();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("results".equals(fieldName)) {
                    deserializedHealthcareJobState.results = HealthcareResult.fromJson(reader);
                } else if ("errors".equals(fieldName)) {
                    deserializedHealthcareJobState.errors = reader.readArray(reader1 -> Error.fromJson(reader1));
                } else if ("@nextLink".equals(fieldName)) {
                    deserializedHealthcareJobState.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedHealthcareJobState;
        });
    }
}
