// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datamigration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.exception.ManagementError;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Properties for task that migrates SQL Server databases to Azure SQL Database Managed Instance.
 */
@Fluent
public final class MigrateSqlServerSqlMITaskProperties extends ProjectTaskProperties {
    /*
     * Task type.
     */
    private String taskType = "Migrate.SqlServer.AzureSqlDbMI";

    /*
     * Task input
     */
    private MigrateSqlServerSqlMITaskInput input;

    /*
     * Task output. This is ignored if submitted.
     */
    private List<MigrateSqlServerSqlMITaskOutput> output;

    /**
     * Creates an instance of MigrateSqlServerSqlMITaskProperties class.
     */
    public MigrateSqlServerSqlMITaskProperties() {
    }

    /**
     * Get the taskType property: Task type.
     * 
     * @return the taskType value.
     */
    @Override
    public String taskType() {
        return this.taskType;
    }

    /**
     * Get the input property: Task input.
     * 
     * @return the input value.
     */
    public MigrateSqlServerSqlMITaskInput input() {
        return this.input;
    }

    /**
     * Set the input property: Task input.
     * 
     * @param input the input value to set.
     * @return the MigrateSqlServerSqlMITaskProperties object itself.
     */
    public MigrateSqlServerSqlMITaskProperties withInput(MigrateSqlServerSqlMITaskInput input) {
        this.input = input;
        return this;
    }

    /**
     * Get the output property: Task output. This is ignored if submitted.
     * 
     * @return the output value.
     */
    public List<MigrateSqlServerSqlMITaskOutput> output() {
        return this.output;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (input() != null) {
            input().validate();
        }
        if (output() != null) {
            output().forEach(e -> e.validate());
        }
        if (commands() != null) {
            commands().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("taskType", this.taskType);
        jsonWriter.writeJsonField("input", this.input);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MigrateSqlServerSqlMITaskProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MigrateSqlServerSqlMITaskProperties if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MigrateSqlServerSqlMITaskProperties.
     */
    public static MigrateSqlServerSqlMITaskProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MigrateSqlServerSqlMITaskProperties deserializedMigrateSqlServerSqlMITaskProperties
                = new MigrateSqlServerSqlMITaskProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("errors".equals(fieldName)) {
                    List<ManagementError> errors = reader.readArray(reader1 -> ManagementError.fromJson(reader1));
                    deserializedMigrateSqlServerSqlMITaskProperties.withErrors(errors);
                } else if ("state".equals(fieldName)) {
                    deserializedMigrateSqlServerSqlMITaskProperties.withState(TaskState.fromString(reader.getString()));
                } else if ("commands".equals(fieldName)) {
                    List<CommandProperties> commands = reader.readArray(reader1 -> CommandProperties.fromJson(reader1));
                    deserializedMigrateSqlServerSqlMITaskProperties.withCommands(commands);
                } else if ("taskType".equals(fieldName)) {
                    deserializedMigrateSqlServerSqlMITaskProperties.taskType = reader.getString();
                } else if ("input".equals(fieldName)) {
                    deserializedMigrateSqlServerSqlMITaskProperties.input
                        = MigrateSqlServerSqlMITaskInput.fromJson(reader);
                } else if ("output".equals(fieldName)) {
                    List<MigrateSqlServerSqlMITaskOutput> output
                        = reader.readArray(reader1 -> MigrateSqlServerSqlMITaskOutput.fromJson(reader1));
                    deserializedMigrateSqlServerSqlMITaskProperties.output = output;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedMigrateSqlServerSqlMITaskProperties;
        });
    }
}
