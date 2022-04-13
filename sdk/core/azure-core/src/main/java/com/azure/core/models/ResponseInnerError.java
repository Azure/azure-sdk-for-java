// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;


import com.azure.core.util.serializer.JsonCapable;
import com.azure.core.util.serializer.JsonReader;
import com.azure.core.util.serializer.JsonToken;
import com.azure.core.util.serializer.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The inner error of a {@link ResponseError}.
 */
final class ResponseInnerError implements JsonCapable<ResponseInnerError> {

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
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        jsonWriter.writeStringField("code", code)
            .writeFieldName("innererror");

        if (innerError != null) {
            innerError.toJson(jsonWriter);
        } else {
            jsonWriter.writeNull();
        }

        // Always flush at the end of writing an object.
        return jsonWriter.writeEndObject()
            .flush();
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
    public static ResponseInnerError fromJson(JsonReader jsonReader) {
        // The JsonReader will begin in one of three states:
        //
        // 1. The current token is null, indicating that the JsonReader was created just to read an instance of this
        // class.
        //
        // An example is DefaultJsonReader.fromString("{\"code\":\"the code\",\"innererror\":null}")
        //
        // 2. The current token is JsonToken#START_OBJECT, indicating that the JsonReader is currently pointing to an
        // instance of this class.
        //
        // An example is DefaultJsonReader.fromString("{\"error\":{\"code\":\"the code\",\"innererror\":null}}")
        //
        // 3. The current token is JsonToken#NULL, indicating that the JsonReader is currently pointing to a null
        // instance of this class.
        //
        // An example is DefaultJsonReader.fromString("{\"error\":null}") where the Java property corresponding to
        // "error" is type ResponseInnerError.
        //
        // States 1 and 3 could be combined with DefaultJsonReader.fromString("null").

        JsonToken token = jsonReader.currentToken();

        // The JsonReader was just initialized and isn't pointing to a current token.
        if (token == null) {
            token = jsonReader.nextToken();
        }

        // The JSON value for this type is null, return null.
        if (token == JsonToken.NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + token);
        }

        ResponseInnerError innerError = new ResponseInnerError();
        // Keep looping until the object has been read.
        //
        // Always terminate on JsonToken#END_OBJECT because if this was called from another deserialization method
        // their next token will point to either the next field name or the termination that object as well.
        //
        // At this point it is assumed that the next token will always be a field name based on JsonReader reading
        // conventions.
        while ((token = jsonReader.nextToken()) != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();

            // Ignore unknown properties.
            if ("code".equals(fieldName)) {
                jsonReader.nextToken();
                innerError.setCode(jsonReader.getStringValue());
            } else if ("innererror".equals(fieldName)) {
                token = jsonReader.nextToken();

                // If the next token isn't JsonToken#NULL that means there is an inner error.
                if (token != JsonToken.NULL) {
                    innerError.setInnerError(ResponseInnerError.fromJson(jsonReader));
                }
            }
        }

        return innerError;
    }
}
