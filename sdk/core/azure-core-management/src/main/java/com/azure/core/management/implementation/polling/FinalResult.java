// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Type holding long-running-operation final result or uri to fetch the final result.
 */
final class FinalResult implements JsonSerializable<FinalResult> {
    @JsonIgnore
    private static final ClientLogger LOGGER = new ClientLogger(FinalResult.class);

    @JsonProperty(value = "resultUri")
    private URL resultUri;
    @JsonProperty(value = "result")
    private String result;

    FinalResult() {
    }

    /**
     * Creates FinalResult.
     *
     * @param resultFetchUri the uri path to fetch the final result
     * @param result the result of long-running-operation
     */
    FinalResult(URL resultFetchUri, String result) {
        if (resultFetchUri == null && result == null) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Either resultFetchUri or result is required"));
        }
        this.resultUri = resultFetchUri;
        this.result = result;
    }

    /**
     * @return the uri path to fetch the final result of long-running-operation
     */
    URL getResultUri() {
        return this.resultUri;
    }

    /**
     * @return the result of long-running-operation
     */
    String getResult() {
        return this.result;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("resultUri", Objects.toString(resultUri, null))
            .writeStringField("result", result)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link FinalResult}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link FinalResult} that the JSON stream represented, may return null.
     * @throws IOException If a {@link FinalResult} fails to be read from the {@code jsonReader}.
     */
    @SuppressWarnings("deprecation")
    public static FinalResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FinalResult finalResult = new FinalResult();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("resultUri".equals(fieldName)) {
                    finalResult.resultUri = reader.getNullable(nonNullReader -> new URL(nonNullReader.getString()));
                } else if ("result".equals(fieldName)) {
                    finalResult.result = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return finalResult;
        });
    }
}
