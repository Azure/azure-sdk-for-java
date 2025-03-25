// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

/**
 * <p>Represents the inner error details of a {@link AzureResponseError}.</p>
 *
 * <p>This class encapsulates the details of an inner error in a {@link AzureResponseError}, including the error code
 * and a nested inner error. It provides methods to access and modify these properties.</p>
 *
 * <p>This class also provides a {@link #toJson(JsonWriter)} method to serialize the inner error details to JSON,
 * and a {@link #fromJson(JsonReader)} method to deserialize the inner error details from JSON.</p>
 *
 * @see AzureResponseError
 * @see JsonSerializable
 * @see JsonReader
 * @see JsonWriter
 */
@Metadata(properties = MetadataProperties.FLUENT)
final class AzureResponseInnerError implements JsonSerializable<AzureResponseInnerError> {
    private String code;
    private AzureResponseInnerError innerError;

    /**
     * Returns the error code of the inner error.
     *
     * @return the error code of this inner error.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the error code of the inner error.
     *
     * @param code the error code of this inner error.
     * @return the updated {@link AzureResponseInnerError} instance.
     */
    public AzureResponseInnerError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Returns the nested inner error for this error.
     *
     * @return the nested inner error for this error.
     */
    public AzureResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the nested inner error for this error.
     *
     * @param innerError the nested inner error for this error.
     * @return the updated {@link AzureResponseInnerError} instance.
     */
    public AzureResponseInnerError setInnerError(AzureResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeJsonField("innererror", innerError)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link AzureResponseInnerError}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link AzureResponseInnerError} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If a {@link AzureResponseInnerError} fails to be read from the {@code jsonReader}.
     */
    public static AzureResponseInnerError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AzureResponseInnerError innerError = new AzureResponseInnerError();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    innerError.code = reader.getString();
                } else if ("innererror".equals(fieldName)) {
                    innerError.innerError = AzureResponseInnerError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return innerError;
        });
    }
}
