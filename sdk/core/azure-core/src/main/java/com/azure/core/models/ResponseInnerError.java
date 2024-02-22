// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * The inner error of a {@link ResponseError}.
 */
final class ResponseInnerError implements JsonSerializable<ResponseInnerError> {

    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "innererror")
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
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeJsonField("innererror", innerError)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link ResponseInnerError}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link ResponseInnerError} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If a {@link ResponseInnerError} fails to be read from the {@code jsonReader}.
     */
    public static ResponseInnerError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponseInnerError innerError = new ResponseInnerError();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    innerError.code = reader.getString();
                } else if ("innererror".equals(fieldName)) {
                    innerError.innerError = ResponseInnerError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return innerError;
        });
    }
}
