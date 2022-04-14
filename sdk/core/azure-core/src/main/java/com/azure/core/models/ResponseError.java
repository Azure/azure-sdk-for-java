// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the error details of an HTTP response.
 */
public final class ResponseError implements JsonCapable<ResponseError> {
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
    public StringBuilder toJson(StringBuilder builder) {
        builder.append("{\"code\":\"").append(code)
            .append("\",\"message\":\"").append(message).append("\"");

        if (target != null) {
            builder.append(",\"target\":\"").append(target).append("\"");
        }

        if (innerError != null) {
            builder.append(",\"innererror\":");
            innerError.toJson(builder);
        }

        if (errorDetails != null) {
            builder.append(",\"details\":[");

            for (int i = 0; i < errorDetails.size(); i++) {
                if (i > 0) {
                    builder.append(",");
                }

                errorDetails.get(i).toJson(builder);
            }

            builder.append("]");
        }

        return builder.append("}");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeStringField("message", message);

        if (target != null) {
            jsonWriter.writeStringField("target", target);
        }

        if (innerError != null) {
            jsonWriter.writeFieldName("innererror");
            innerError.toJson(jsonWriter);
        }

        if (errorDetails != null) {
            jsonWriter.writeFieldName("details").writeStartArray();

            errorDetails.forEach(error -> error.toJson(jsonWriter));

            jsonWriter.writeEndArray();
        }

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
        return JsonUtils.deserializeObject(jsonReader, (reader, token) -> {
            // required
            String code = null;
            String message = null;

            // optional
            String target = null;
            ResponseInnerError innerError = null;
            List<ResponseError> errorDetails = null;

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                token = jsonReader.nextToken();

                switch (jsonReader.getFieldName()) {
                    case "code":
                        code = jsonReader.getStringValue();
                        break;

                    case "message":
                        message = jsonReader.getStringValue();
                        break;

                    case "target":
                        target = jsonReader.getStringValue();
                        break;

                    case "innererror":
                        innerError = ResponseInnerError.fromJson(jsonReader);
                        break;

                    case "details":
                        if (token == JsonToken.START_ARRAY) {
                            token = jsonReader.nextToken();
                            errorDetails = new ArrayList<>();
                        }

                        while (token != JsonToken.END_ARRAY) {
                            errorDetails.add(ResponseError.fromJson(jsonReader));
                            token = jsonReader.nextToken();
                        }

                        break;

                    default:
                        break;
                }
            }

            if (code == null && message == null) {
                throw new IllegalStateException("Missing required properties 'code' and 'message'.");
            } else if (code == null) {
                throw new IllegalStateException("Missing required property 'code'.");
            } else if (message == null) {
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
