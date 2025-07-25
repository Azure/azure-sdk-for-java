// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.storageactions.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Represents the storage task conditions to be tested for a match with container and blob properties.
 */
@Fluent
public final class StorageTaskPreviewActionCondition implements JsonSerializable<StorageTaskPreviewActionCondition> {
    /*
     * The condition to be tested for a match with container and blob properties.
     */
    private StorageTaskPreviewActionIfCondition ifProperty;

    /*
     * Specify whether the else block is present in the condition.
     */
    private boolean elseBlockExists;

    /**
     * Creates an instance of StorageTaskPreviewActionCondition class.
     */
    public StorageTaskPreviewActionCondition() {
    }

    /**
     * Get the ifProperty property: The condition to be tested for a match with container and blob properties.
     * 
     * @return the ifProperty value.
     */
    public StorageTaskPreviewActionIfCondition ifProperty() {
        return this.ifProperty;
    }

    /**
     * Set the ifProperty property: The condition to be tested for a match with container and blob properties.
     * 
     * @param ifProperty the ifProperty value to set.
     * @return the StorageTaskPreviewActionCondition object itself.
     */
    public StorageTaskPreviewActionCondition withIfProperty(StorageTaskPreviewActionIfCondition ifProperty) {
        this.ifProperty = ifProperty;
        return this;
    }

    /**
     * Get the elseBlockExists property: Specify whether the else block is present in the condition.
     * 
     * @return the elseBlockExists value.
     */
    public boolean elseBlockExists() {
        return this.elseBlockExists;
    }

    /**
     * Set the elseBlockExists property: Specify whether the else block is present in the condition.
     * 
     * @param elseBlockExists the elseBlockExists value to set.
     * @return the StorageTaskPreviewActionCondition object itself.
     */
    public StorageTaskPreviewActionCondition withElseBlockExists(boolean elseBlockExists) {
        this.elseBlockExists = elseBlockExists;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (ifProperty() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property ifProperty in model StorageTaskPreviewActionCondition"));
        } else {
            ifProperty().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(StorageTaskPreviewActionCondition.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("if", this.ifProperty);
        jsonWriter.writeBooleanField("elseBlockExists", this.elseBlockExists);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of StorageTaskPreviewActionCondition from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of StorageTaskPreviewActionCondition if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the StorageTaskPreviewActionCondition.
     */
    public static StorageTaskPreviewActionCondition fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            StorageTaskPreviewActionCondition deserializedStorageTaskPreviewActionCondition
                = new StorageTaskPreviewActionCondition();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("if".equals(fieldName)) {
                    deserializedStorageTaskPreviewActionCondition.ifProperty
                        = StorageTaskPreviewActionIfCondition.fromJson(reader);
                } else if ("elseBlockExists".equals(fieldName)) {
                    deserializedStorageTaskPreviewActionCondition.elseBlockExists = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedStorageTaskPreviewActionCondition;
        });
    }
}
