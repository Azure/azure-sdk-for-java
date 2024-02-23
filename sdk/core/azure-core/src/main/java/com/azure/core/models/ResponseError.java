// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;

/**
 * This class represents the error details of an HTTP response.
 */
public final class ResponseError implements JsonSerializable<ResponseError> {

    @JsonProperty(value = "code")
    private final String code;

    @JsonProperty(value = "message")
    private final String message;

    @JsonProperty(value = "target")
    private String target;

    @JsonProperty(value = "innererror")
    private ResponseInnerError innerError;

    @JsonProperty(value = "details")
    private List<ResponseError> errorDetails;

    /**
     * Creates an instance of {@link ResponseError}.
     *
     * @param code the error code of this error.
     * @param message the error message of this error.
     */
    @JsonCreator
    public ResponseError(@JsonProperty(value = "code", required = true) String code,
        @JsonProperty(value = "message", required = true) String message) {
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
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Returns the inner error information for this error.
     *
     * @return the inner error for this error.
     */
    ResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the inner error information for this error.
     *
     * @param innerError the inner error for this error.
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setInnerError(ResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    /**
     * Returns a list of details about specific errors that led to this reported error.
     *
     * @return the error details.
     */
    List<ResponseError> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets a list of details about specific errors that led to this reported error.
     *
     * @param errorDetails the error details.
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setErrorDetails(List<ResponseError> errorDetails) {
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
     * Reads a JSON stream into a {@link ResponseError}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link ResponseError} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If a {@link ResponseError} fails to be read from the {@code jsonReader}.
     */
    public static ResponseError fromJson(JsonReader jsonReader) throws IOException {
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

    private static ResponseError readResponseError(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String code = null;
            boolean codeFound = false;
            String message = null;
            boolean messageFound = false;
            String target = null;
            ResponseInnerError innerError = null;
            List<ResponseError> errorDetails = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    code = reader.getString();
                    codeFound = true;
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                    messageFound = true;
                } else if ("target".equals(fieldName)) {
                    target = reader.getString();
                } else if ("innererror".equals(fieldName)) {
                    innerError = ResponseInnerError.fromJson(reader);
                } else if ("details".equals(fieldName)) {
                    errorDetails = reader.readArray(ResponseError::fromJson);
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

            return new ResponseError(code, message).setTarget(target)
                .setInnerError(innerError)
                .setErrorDetails(errorDetails);
        });
    }
}
