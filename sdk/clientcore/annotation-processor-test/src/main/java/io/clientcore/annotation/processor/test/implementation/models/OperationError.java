// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import java.io.IOException;

/**
 * The OperationError model.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class OperationError implements JsonSerializable<OperationError> {

    /*
     * The error code.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    private String code;

    /*
     * The error message.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    private String message;

    /*
     * The key vault server error.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    private OperationError innerError;

    /**
     * Creates an instance of OperationError class.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public OperationError() {
    }

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the innerError property: The key vault server error.
     *
     * @return the innerError value.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public OperationError getInnerError() {
        return this.innerError;
    }

    /**
     * {@inheritDoc}
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of OperationError from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of OperationError if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the OperationError.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static OperationError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            OperationError deserializedOperationError = new OperationError();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("code".equals(fieldName)) {
                    deserializedOperationError.code = reader.getString();
                } else if ("message".equals(fieldName)) {
                    deserializedOperationError.message = reader.getString();
                } else if ("innererror".equals(fieldName)) {
                    deserializedOperationError.innerError = OperationError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedOperationError;
        });
    }
}
