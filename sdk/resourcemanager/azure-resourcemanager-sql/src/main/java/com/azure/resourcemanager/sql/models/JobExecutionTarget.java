// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The target that a job execution is executed on.
 */
@Immutable
public final class JobExecutionTarget implements JsonSerializable<JobExecutionTarget> {
    /*
     * The type of the target.
     */
    private JobTargetType type;

    /*
     * The server name.
     */
    private String serverName;

    /*
     * The database name.
     */
    private String databaseName;

    /**
     * Creates an instance of JobExecutionTarget class.
     */
    public JobExecutionTarget() {
    }

    /**
     * Get the type property: The type of the target.
     * 
     * @return the type value.
     */
    public JobTargetType type() {
        return this.type;
    }

    /**
     * Get the serverName property: The server name.
     * 
     * @return the serverName value.
     */
    public String serverName() {
        return this.serverName;
    }

    /**
     * Get the databaseName property: The database name.
     * 
     * @return the databaseName value.
     */
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of JobExecutionTarget from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of JobExecutionTarget if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the JobExecutionTarget.
     */
    public static JobExecutionTarget fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JobExecutionTarget deserializedJobExecutionTarget = new JobExecutionTarget();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    deserializedJobExecutionTarget.type = JobTargetType.fromString(reader.getString());
                } else if ("serverName".equals(fieldName)) {
                    deserializedJobExecutionTarget.serverName = reader.getString();
                } else if ("databaseName".equals(fieldName)) {
                    deserializedJobExecutionTarget.databaseName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedJobExecutionTarget;
        });
    }
}
