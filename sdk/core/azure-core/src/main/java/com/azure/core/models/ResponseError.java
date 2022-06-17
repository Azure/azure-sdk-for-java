// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

/**
 * This class represents the error details of an HTTP response.
 */
public final class ResponseError implements JsonSerializable<ResponseError> {
    private final String code;
    private final String message;

    private String target;
    private ResponseInnerError innerError;
    private List<ResponseError> errorDetails;

    /**
     * Creates an instance of {@link ResponseError}.
     *
     * @param code the error code of this error.
     * @param message the error message of this error.
     */
    public ResponseError(String code, String message) {
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
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeStringField("message", message)
            .writeStringField("target", target, false)
            .writeJsonField("innererror", innerError, false);

        JsonUtils.writeArray(jsonWriter, "details", errorDetails, JsonWriter::writeJson);

        return jsonWriter.writeEndObject().flush();
    }

    /**
     * Creates an instance of {@link ResponseInnerError} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link ResponseInnerError} if the {@link JsonReader} is pointing to
     * {@link ResponseInnerError} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static ResponseError fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            // required
            String code = null;
            String message = null;

            boolean hasCode = false;
            boolean hasMessage = false;

            // optional
            String target = null;
            ResponseInnerError innerError = null;
            List<ResponseError> errorDetails = null;

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                jsonReader.nextToken();

                if ("code".equals(fieldName)) {
                    hasCode = true;
                    code = jsonReader.getStringValue();
                } else if ("message".equals(fieldName)) {
                    hasMessage = true;
                    message = jsonReader.getStringValue();
                } else if ("target".equals(fieldName)) {
                    target = jsonReader.getStringValue();
                } else if ("innererror".equals(fieldName)) {
                    innerError = ResponseInnerError.fromJson(jsonReader);
                } else if ("details".equals(fieldName)) {
                    errorDetails = JsonUtils.readArray(reader, ResponseError::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            if (!hasCode && !hasMessage) {
                throw new IllegalStateException("Missing required properties 'code' and 'message'.");
            } else if (!hasCode) {
                throw new IllegalStateException("Missing required property 'code'.");
            } else if (!hasMessage) {
                throw new IllegalStateException("Missing required property 'message'.");
            } else {
                return new ResponseError(code, message)
                    .setTarget(target)
                    .setInnerError(innerError)
                    .setErrorDetails(errorDetails);
            }
        });
    }
}
