// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;


import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * The inner error of a {@link ResponseError}.
 */
final class ResponseInnerError implements JsonSerializable<ResponseInnerError> {
    private String code;
    private ResponseInnerError innerError;

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
     * @return the updated {@link ResponseInnerError} instance.
     */
    public ResponseInnerError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Returns the nested inner error for this error.
     *
     * @return the nested inner error for this error.
     */
    public ResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the nested inner error for this error.
     *
     * @param innerError the nested inner error for this error.
     * @return the updated {@link ResponseInnerError} instance.
     */
    public ResponseInnerError setInnerError(ResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeJsonField("innererror", innerError, false)
            .writeEndObject()
            .flush();
    }

    /**
     * Creates an instance of {@link ResponseInnerError} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link ResponseInnerError} if the {@link JsonReader} is pointing to {@link
     * ResponseInnerError} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static ResponseInnerError fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            ResponseInnerError innerError = new ResponseInnerError();

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.getFieldName();
                reader.nextToken();

                // Ignore unknown properties.
                if ("code".equals(fieldName)) {
                    innerError.setCode(jsonReader.getStringValue());
                } else if ("innererror".equals(fieldName)) {
                    innerError.setInnerError(ResponseInnerError.fromJson(jsonReader));
                } else {
                    reader.skipChildren();
                }
            }

            return innerError;
        });
    }
}
