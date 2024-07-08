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

/** The SingleClassificationDocument model. */
@Fluent
public final class SingleClassificationDocument implements JsonSerializable<SingleClassificationDocument> {
    /*
     * Unique, non-empty document identifier.
     */
    private String id;

    /*
     * The classification property.
     */
    private ClassificationResult classification;

    /*
     * Warnings encountered while processing document.
     */
    private List<DocumentWarning> warnings;

    /*
     * if showStats=true was specified in the request this field will contain
     * information about the document payload.
     */
    private DocumentStatistics statistics;

    /**
     * Get the id property: Unique, non-empty document identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Unique, non-empty document identifier.
     *
     * @param id the id value to set.
     * @return the SingleClassificationDocument object itself.
     */
    public SingleClassificationDocument setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the classification property: The classification property.
     *
     * @return the classification value.
     */
    public ClassificationResult getClassification() {
        return this.classification;
    }

    /**
     * Set the classification property: The classification property.
     *
     * @param classification the classification value to set.
     * @return the SingleClassificationDocument object itself.
     */
    public SingleClassificationDocument setClassification(ClassificationResult classification) {
        this.classification = classification;
        return this;
    }

    /**
     * Get the warnings property: Warnings encountered while processing document.
     *
     * @return the warnings value.
     */
    public List<DocumentWarning> getWarnings() {
        return this.warnings;
    }

    /**
     * Set the warnings property: Warnings encountered while processing document.
     *
     * @param warnings the warnings value to set.
     * @return the SingleClassificationDocument object itself.
     */
    public SingleClassificationDocument setWarnings(List<DocumentWarning> warnings) {
        this.warnings = warnings;
        return this;
    }

    /**
     * Get the statistics property: if showStats=true was specified in the request this field will contain information
     * about the document payload.
     *
     * @return the statistics value.
     */
    public DocumentStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Set the statistics property: if showStats=true was specified in the request this field will contain information
     * about the document payload.
     *
     * @param statistics the statistics value to set.
     * @return the SingleClassificationDocument object itself.
     */
    public SingleClassificationDocument setStatistics(DocumentStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeJsonField("classification", this.classification);
        jsonWriter.writeArrayField("warnings", this.warnings, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("statistics", this.statistics);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SingleClassificationDocument from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SingleClassificationDocument if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SingleClassificationDocument.
     */
    public static SingleClassificationDocument fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SingleClassificationDocument deserializedSingleClassificationDocument = new SingleClassificationDocument();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedSingleClassificationDocument.id = reader.getString();
                } else if ("classification".equals(fieldName)) {
                    deserializedSingleClassificationDocument.classification = ClassificationResult.fromJson(reader);
                } else if ("warnings".equals(fieldName)) {
                    deserializedSingleClassificationDocument.warnings = reader.readArray(reader1 -> DocumentWarning.fromJson(reader1));
                } else if ("statistics".equals(fieldName)) {
                    deserializedSingleClassificationDocument.statistics = DocumentStatistics.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedSingleClassificationDocument;
        });
    }
}
