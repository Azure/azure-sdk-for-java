// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Use custom models to ease the process of information extraction from unstructured documents like contracts or
 * financial documents.
 */
@Fluent
public final class CustomEntitiesLROTask extends AnalyzeTextLROTask {
    /*
     * Enumeration of supported long-running Text Analysis tasks.
     */
    @Generated
    private AnalyzeTextLROTaskKind kind = AnalyzeTextLROTaskKind.CUSTOM_ENTITY_RECOGNITION;

    /*
     * Supported parameters for a Custom Entities task.
     */
    @Generated
    private CustomEntitiesTaskParameters parameters;

    /**
     * Creates an instance of CustomEntitiesLROTask class.
     */
    @Generated
    public CustomEntitiesLROTask() {
    }

    /**
     * Get the kind property: Enumeration of supported long-running Text Analysis tasks.
     * 
     * @return the kind value.
     */
    @Generated
    @Override
    public AnalyzeTextLROTaskKind getKind() {
        return this.kind;
    }

    /**
     * Get the parameters property: Supported parameters for a Custom Entities task.
     * 
     * @return the parameters value.
     */
    @Generated
    public CustomEntitiesTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: Supported parameters for a Custom Entities task.
     * 
     * @param parameters the parameters value to set.
     * @return the CustomEntitiesLROTask object itself.
     */
    @Generated
    public CustomEntitiesLROTask setParameters(CustomEntitiesTaskParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public CustomEntitiesLROTask setTaskName(String taskName) {
        super.setTaskName(taskName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("taskName", getTaskName());
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        jsonWriter.writeJsonField("parameters", this.parameters);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CustomEntitiesLROTask from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CustomEntitiesLROTask if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CustomEntitiesLROTask.
     */
    @Generated
    public static CustomEntitiesLROTask fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CustomEntitiesLROTask deserializedCustomEntitiesLROTask = new CustomEntitiesLROTask();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("taskName".equals(fieldName)) {
                    deserializedCustomEntitiesLROTask.setTaskName(reader.getString());
                } else if ("kind".equals(fieldName)) {
                    deserializedCustomEntitiesLROTask.kind = AnalyzeTextLROTaskKind.fromString(reader.getString());
                } else if ("parameters".equals(fieldName)) {
                    deserializedCustomEntitiesLROTask.parameters = CustomEntitiesTaskParameters.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCustomEntitiesLROTask;
        });
    }
}
