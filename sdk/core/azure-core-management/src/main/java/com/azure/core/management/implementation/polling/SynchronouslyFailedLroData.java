// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * The type to store the data associated a long-running-operation that is Failed to synchronously.
 */
final class SynchronouslyFailedLroData extends Error {

    SynchronouslyFailedLroData() {
    }

    /**
     * Creates SynchronouslyFailedLroData.
     *
     * @param message the error message
     * @param lroResponseStatusCode the http response status code of long-running init operation
     * @param responseHeaders the http response headers of long-running init operation
     * @param lroResponseBody the http response body of long-running init operation
     */
    SynchronouslyFailedLroData(String message, int lroResponseStatusCode, Map<String, String> responseHeaders,
        String lroResponseBody) {
        super(message, lroResponseStatusCode, responseHeaders, lroResponseBody);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("error", getMessage())
            .writeIntField("responseStatusCode", getResponseStatusCode())
            .writeStringField("responseBody", getResponseBody())
            .writeMapField("responseHeaders", getResponseHeaders(), JsonWriter::writeString)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SynchronouslyFailedLroData}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link SynchronouslyFailedLroData} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SynchronouslyFailedLroData} fails to be read from the {@code jsonReader}.
     */
    public static SynchronouslyFailedLroData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String message = null;
            int responseStatusCode = 0;
            String responseBody = null;
            Map<String, String> responseHeaders = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("error".equals(fieldName)) {
                    message = reader.getString();
                } else if ("responseStatusCode".equals(fieldName)) {
                    responseStatusCode = reader.getInt();
                } else if ("responseBody".equals(fieldName)) {
                    responseBody = reader.getString();
                } else if ("responseHeaders".equals(fieldName)) {
                    responseHeaders = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }

            return new SynchronouslyFailedLroData(message, responseStatusCode, responseHeaders, responseBody);
        });
    }
}
