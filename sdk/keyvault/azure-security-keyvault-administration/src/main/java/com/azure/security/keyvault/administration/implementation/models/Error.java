// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The key vault server error.
 */
@Immutable
public final class Error implements JsonSerializable<Error> {

    /*
     * The error code.
     */
    @Generated
    private String code;

    /*
     * The error message.
     */
    @Generated
    private String message;

    /*
     * The key vault server error.
     */
    @Generated
    private Error innerError;

    /**
     * Creates an instance of Error class.
     */
    @Generated
    private Error() {
    }

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    @Generated
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    @Generated
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the innerError property: The key vault server error.
     *
     * @return the innerError value.
     */
    @Generated
    public Error getInnerError() {
        return this.innerError;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Error from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of Error if the JsonReader was pointing to an instance of it, or null if it was pointing to
     * JSON null.
     * @throws IOException If an error occurs while reading the Error.
     */
    @Generated
    public static Error fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Error deserializedError = new Error();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("code".equals(fieldName)) {
                    deserializedError.code = reader.getString();
                } else if ("message".equals(fieldName)) {
                    deserializedError.message = reader.getString();
                } else if ("innererror".equals(fieldName)) {
                    deserializedError.innerError = Error.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedError;
        });
    }
}
