// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Type holding error details of long-running-operation (LRO) or polling-operation of an LRO.
 */
class Error implements JsonSerializable<Error> {
    @JsonProperty(value = "error")
    private String message;
    @JsonProperty(value = "responseStatusCode")
    private int responseStatusCode;
    @JsonProperty(value = "responseBody")
    private String responseBody;
    @JsonProperty(value = "responseHeaders")
    private Map<String, String> responseHeaders;

    Error() {
    }

    /**
     * Creates Error.
     *
     * @param message the error message
     * @param responseStatusCode the http status code associated with the error
     * @param responseHeaders the http response headers associated with the error
     * @param responseBody the http response body associated with the error
     */
    Error(String message, int responseStatusCode, Map<String, String> responseHeaders, String responseBody) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders;
    }

    String getMessage() {
        return this.message;
    }

    int getResponseStatusCode() {
        return this.responseStatusCode;
    }

    String getResponseBody() {
        return this.responseBody;
    }

    Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("error", message)
            .writeIntField("responseStatusCode", responseStatusCode)
            .writeStringField("responseBody", responseBody)
            .writeMapField("responseHeaders", responseHeaders, JsonWriter::writeString)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into an {@link Error}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link Error} that the JSON stream represented, may return null.
     * @throws IOException If an {@link Error} fails to be read from the {@code jsonReader}.
     */
    public static Error fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Error error = new Error();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("error".equals(fieldName)) {
                    error.message = reader.getString();
                } else if ("responseStatusCode".equals(fieldName)) {
                    error.responseStatusCode = reader.getInt();
                } else if ("responseBody".equals(fieldName)) {
                    error.responseBody = reader.getString();
                } else if ("responseHeaders".equals(fieldName)) {
                    error.responseHeaders = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }

            return error;
        });
    }
}
