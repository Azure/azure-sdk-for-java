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
 * The error exception.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class ServiceError implements JsonSerializable<ServiceError> {
    /*
     * The server error.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    private OperationError error;

    /**
     * Creates an instance of Error class.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    private ServiceError() {
    }

    /**
     * Get the error property: The server error.
     *
     * @return the error value.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public OperationError getError() {
        return this.error;
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
     * Reads an instance of Error from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of Error if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the Error.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static ServiceError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ServiceError deserializedServiceError = new ServiceError();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("error".equals(fieldName)) {
                    deserializedServiceError.error = OperationError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedServiceError;
        });
    }
}
