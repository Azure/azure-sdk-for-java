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
import java.util.List;

/**
 * <p>Represents the error details of an HTTP response.</p>
 *
 * <p>This class encapsulates the details of an HTTP error response, including the error code, message, target,
 * inner error, and additional error details. It provides methods to access these properties.</p>
 *
 * <p>This class also provides a {@link #toJson(JsonWriter)} method to serialize the error details to JSON, and
 * a {@link #fromJson(JsonReader)} method to deserialize the error details from JSON.</p>
 *
 * @see JsonSerializable
 * @see JsonReader
 * @see JsonWriter
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class AzureResponseError implements JsonSerializable<AzureResponseError> {
    private final String code;
    private final String message;
    private String target;
    private AzureResponseInnerError innerError;
    private List<AzureResponseError> errorDetails;

    /**
     * Creates an instance of {@link AzureResponseError}.
     *
     * @param code the error code of this error.
     * @param message the error message of this error.
     */
    public AzureResponseError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the error code of this error.
     *
     * @return the error code of this error.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error message of this error.
     *
     * @return the error message of this error.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the target of this error.
     *
     * @return the target of this error.
     */
    String getTarget() {
        return target;
    }

    /**
     * Sets the target of this error.
     *
     * @param target the target of this error.
     * @return the updated {@link AzureResponseError} instance.
     */
    AzureResponseError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Returns the inner error information for this error.
     *
     * @return the inner error for this error.
     */
    AzureResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the inner error information for this error.
     *
     * @param innerError the inner error for this error.
     * @return the updated {@link AzureResponseError} instance.
     */
    AzureResponseError setInnerError(AzureResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    /**
     * Returns a list of details about specific errors that led to this reported error.
     *
     * @return the error details.
     */
    List<AzureResponseError> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets a list of details about specific errors that led to this reported error.
     *
     * @param errorDetails the error details.
     * @return the updated {@link AzureResponseError} instance.
     */
    AzureResponseError setErrorDetails(List<AzureResponseError> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeStringField("message", message)
            .writeStringField("target", target)
            .writeJsonField("innererror", innerError)
            .writeArrayField("details", errorDetails, JsonWriter::writeJson)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link AzureResponseError}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link AzureResponseError} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If a {@link AzureResponseError} fails to be read from the {@code jsonReader}.
     */
    public static AzureResponseError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // Buffer the next JSON object as ResponseError can take two forms:
            //
            // - A ResponseError object
            // - A ResponseError object wrapped in an "error" node.
            JsonReader bufferedReader = reader.bufferObject();
            bufferedReader.nextToken(); // Get to the START_OBJECT token.
            while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = bufferedReader.getFieldName();
                bufferedReader.nextToken();

                if ("error".equals(fieldName)) {
                    // If the ResponseError was wrapped in the "error" node begin reading it now.
                    return readResponseError(bufferedReader);
                } else {
                    bufferedReader.skipChildren();
                }
            }

            // Otherwise reset the JsonReader and read the whole JSON object.
            return readResponseError(bufferedReader.reset());
        });
    }

    private static AzureResponseError readResponseError(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String code = null;
            boolean codeFound = false;
            String message = null;
            boolean messageFound = false;
            String target = null;
            AzureResponseInnerError innerError = null;
            List<AzureResponseError> errorDetails = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equalsIgnoreCase(fieldName)) {
                    code = reader.getString();
                    codeFound = true;
                } else if ("message".equalsIgnoreCase(fieldName)) {
                    message = reader.getString();
                    messageFound = true;
                } else if ("target".equalsIgnoreCase(fieldName)) {
                    target = reader.getString();
                } else if ("innererror".equalsIgnoreCase(fieldName)) {
                    innerError = AzureResponseInnerError.fromJson(reader);
                } else if ("details".equalsIgnoreCase(fieldName)) {
                    errorDetails = reader.readArray(AzureResponseError::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            if (!codeFound && !messageFound) {
                throw new IllegalStateException("Missing required properties: code, message");
            } else if (!codeFound) {
                throw new IllegalStateException("Missing required property: code");
            } else if (!messageFound) {
                throw new IllegalStateException("Missing required property: message");
            }

            return new AzureResponseError(code, message).setTarget(target)
                .setInnerError(innerError)
                .setErrorDetails(errorDetails);
        });
    }
}
