// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The X12 schema reference.
 */
@Fluent
public final class X12SchemaReference implements JsonSerializable<X12SchemaReference> {
    /*
     * The message id.
     */
    private String messageId;

    /*
     * The sender application id.
     */
    private String senderApplicationId;

    /*
     * The schema version.
     */
    private String schemaVersion;

    /*
     * The schema name.
     */
    private String schemaName;

    /**
     * Creates an instance of X12SchemaReference class.
     */
    public X12SchemaReference() {
    }

    /**
     * Get the messageId property: The message id.
     * 
     * @return the messageId value.
     */
    public String messageId() {
        return this.messageId;
    }

    /**
     * Set the messageId property: The message id.
     * 
     * @param messageId the messageId value to set.
     * @return the X12SchemaReference object itself.
     */
    public X12SchemaReference withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Get the senderApplicationId property: The sender application id.
     * 
     * @return the senderApplicationId value.
     */
    public String senderApplicationId() {
        return this.senderApplicationId;
    }

    /**
     * Set the senderApplicationId property: The sender application id.
     * 
     * @param senderApplicationId the senderApplicationId value to set.
     * @return the X12SchemaReference object itself.
     */
    public X12SchemaReference withSenderApplicationId(String senderApplicationId) {
        this.senderApplicationId = senderApplicationId;
        return this;
    }

    /**
     * Get the schemaVersion property: The schema version.
     * 
     * @return the schemaVersion value.
     */
    public String schemaVersion() {
        return this.schemaVersion;
    }

    /**
     * Set the schemaVersion property: The schema version.
     * 
     * @param schemaVersion the schemaVersion value to set.
     * @return the X12SchemaReference object itself.
     */
    public X12SchemaReference withSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    /**
     * Get the schemaName property: The schema name.
     * 
     * @return the schemaName value.
     */
    public String schemaName() {
        return this.schemaName;
    }

    /**
     * Set the schemaName property: The schema name.
     * 
     * @param schemaName the schemaName value to set.
     * @return the X12SchemaReference object itself.
     */
    public X12SchemaReference withSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (messageId() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property messageId in model X12SchemaReference"));
        }
        if (schemaVersion() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property schemaVersion in model X12SchemaReference"));
        }
        if (schemaName() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property schemaName in model X12SchemaReference"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(X12SchemaReference.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("messageId", this.messageId);
        jsonWriter.writeStringField("schemaVersion", this.schemaVersion);
        jsonWriter.writeStringField("schemaName", this.schemaName);
        jsonWriter.writeStringField("senderApplicationId", this.senderApplicationId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of X12SchemaReference from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of X12SchemaReference if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the X12SchemaReference.
     */
    public static X12SchemaReference fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            X12SchemaReference deserializedX12SchemaReference = new X12SchemaReference();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("messageId".equals(fieldName)) {
                    deserializedX12SchemaReference.messageId = reader.getString();
                } else if ("schemaVersion".equals(fieldName)) {
                    deserializedX12SchemaReference.schemaVersion = reader.getString();
                } else if ("schemaName".equals(fieldName)) {
                    deserializedX12SchemaReference.schemaName = reader.getString();
                } else if ("senderApplicationId".equals(fieldName)) {
                    deserializedX12SchemaReference.senderApplicationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedX12SchemaReference;
        });
    }
}
