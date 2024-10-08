// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The SparkJobScalaEntry model.
 */
@Fluent
public final class SparkJobScalaEntry extends SparkJobEntry {
    /*
     * [Required] Type of the job's entry point.
     */
    private SparkJobEntryType sparkJobEntryType = SparkJobEntryType.SPARK_JOB_SCALA_ENTRY;

    /*
     * [Required] Scala class name used as entry point.
     */
    private String className;

    /**
     * Creates an instance of SparkJobScalaEntry class.
     */
    public SparkJobScalaEntry() {
    }

    /**
     * Get the sparkJobEntryType property: [Required] Type of the job's entry point.
     * 
     * @return the sparkJobEntryType value.
     */
    @Override
    public SparkJobEntryType sparkJobEntryType() {
        return this.sparkJobEntryType;
    }

    /**
     * Get the className property: [Required] Scala class name used as entry point.
     * 
     * @return the className value.
     */
    public String className() {
        return this.className;
    }

    /**
     * Set the className property: [Required] Scala class name used as entry point.
     * 
     * @param className the className value to set.
     * @return the SparkJobScalaEntry object itself.
     */
    public SparkJobScalaEntry withClassName(String className) {
        this.className = className;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (className() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property className in model SparkJobScalaEntry"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SparkJobScalaEntry.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("className", this.className);
        jsonWriter.writeStringField("sparkJobEntryType",
            this.sparkJobEntryType == null ? null : this.sparkJobEntryType.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SparkJobScalaEntry from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SparkJobScalaEntry if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SparkJobScalaEntry.
     */
    public static SparkJobScalaEntry fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SparkJobScalaEntry deserializedSparkJobScalaEntry = new SparkJobScalaEntry();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("className".equals(fieldName)) {
                    deserializedSparkJobScalaEntry.className = reader.getString();
                } else if ("sparkJobEntryType".equals(fieldName)) {
                    deserializedSparkJobScalaEntry.sparkJobEntryType = SparkJobEntryType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSparkJobScalaEntry;
        });
    }
}
