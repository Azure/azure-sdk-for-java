// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Specification of which command to run where.
 */
@Fluent
public final class CommandPostBody implements JsonSerializable<CommandPostBody> {
    /*
     * The command which should be run
     */
    private String command;

    /*
     * The arguments for the command to be run
     */
    private Map<String, String> arguments;

    /*
     * IP address of the cassandra host to run the command on
     */
    private String host;

    /*
     * If true, stops cassandra before executing the command and then start it again
     */
    private Boolean cassandraStopStart;

    /*
     * If true, allows the command to *write* to the cassandra directory, otherwise read-only.
     */
    private Boolean readwrite;

    /**
     * Creates an instance of CommandPostBody class.
     */
    public CommandPostBody() {
    }

    /**
     * Get the command property: The command which should be run.
     * 
     * @return the command value.
     */
    public String command() {
        return this.command;
    }

    /**
     * Set the command property: The command which should be run.
     * 
     * @param command the command value to set.
     * @return the CommandPostBody object itself.
     */
    public CommandPostBody withCommand(String command) {
        this.command = command;
        return this;
    }

    /**
     * Get the arguments property: The arguments for the command to be run.
     * 
     * @return the arguments value.
     */
    public Map<String, String> arguments() {
        return this.arguments;
    }

    /**
     * Set the arguments property: The arguments for the command to be run.
     * 
     * @param arguments the arguments value to set.
     * @return the CommandPostBody object itself.
     */
    public CommandPostBody withArguments(Map<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    /**
     * Get the host property: IP address of the cassandra host to run the command on.
     * 
     * @return the host value.
     */
    public String host() {
        return this.host;
    }

    /**
     * Set the host property: IP address of the cassandra host to run the command on.
     * 
     * @param host the host value to set.
     * @return the CommandPostBody object itself.
     */
    public CommandPostBody withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Get the cassandraStopStart property: If true, stops cassandra before executing the command and then start it
     * again.
     * 
     * @return the cassandraStopStart value.
     */
    public Boolean cassandraStopStart() {
        return this.cassandraStopStart;
    }

    /**
     * Set the cassandraStopStart property: If true, stops cassandra before executing the command and then start it
     * again.
     * 
     * @param cassandraStopStart the cassandraStopStart value to set.
     * @return the CommandPostBody object itself.
     */
    public CommandPostBody withCassandraStopStart(Boolean cassandraStopStart) {
        this.cassandraStopStart = cassandraStopStart;
        return this;
    }

    /**
     * Get the readwrite property: If true, allows the command to *write* to the cassandra directory, otherwise
     * read-only.
     * 
     * @return the readwrite value.
     */
    public Boolean readwrite() {
        return this.readwrite;
    }

    /**
     * Set the readwrite property: If true, allows the command to *write* to the cassandra directory, otherwise
     * read-only.
     * 
     * @param readwrite the readwrite value to set.
     * @return the CommandPostBody object itself.
     */
    public CommandPostBody withReadwrite(Boolean readwrite) {
        this.readwrite = readwrite;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (command() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property command in model CommandPostBody"));
        }
        if (host() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property host in model CommandPostBody"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(CommandPostBody.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("command", this.command);
        jsonWriter.writeStringField("host", this.host);
        jsonWriter.writeMapField("arguments", this.arguments, (writer, element) -> writer.writeString(element));
        jsonWriter.writeBooleanField("cassandra-stop-start", this.cassandraStopStart);
        jsonWriter.writeBooleanField("readwrite", this.readwrite);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CommandPostBody from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CommandPostBody if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the CommandPostBody.
     */
    public static CommandPostBody fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CommandPostBody deserializedCommandPostBody = new CommandPostBody();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("command".equals(fieldName)) {
                    deserializedCommandPostBody.command = reader.getString();
                } else if ("host".equals(fieldName)) {
                    deserializedCommandPostBody.host = reader.getString();
                } else if ("arguments".equals(fieldName)) {
                    Map<String, String> arguments = reader.readMap(reader1 -> reader1.getString());
                    deserializedCommandPostBody.arguments = arguments;
                } else if ("cassandra-stop-start".equals(fieldName)) {
                    deserializedCommandPostBody.cassandraStopStart = reader.getNullable(JsonReader::getBoolean);
                } else if ("readwrite".equals(fieldName)) {
                    deserializedCommandPostBody.readwrite = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCommandPostBody;
        });
    }
}
