// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * The type to store the data associated a long-running-operation that successfully completed synchronously.
 */
final class SynchronouslySucceededLroData implements JsonSerializable<SynchronouslySucceededLroData> {
    @JsonProperty(value = "lroResponseBody")
    private String lroResponseBody;
    @JsonProperty(value = "finalResult")
    private FinalResult finalResult;

    SynchronouslySucceededLroData() {
    }

    /**
     * Creates SynchronouslySucceededLroData.
     *
     * @param lroResponseBody the lro response body
     */
    SynchronouslySucceededLroData(String lroResponseBody) {
        this.lroResponseBody = lroResponseBody;
        if (this.lroResponseBody != null) {
            this.finalResult = new FinalResult(null, lroResponseBody);
        }
    }

    /**
     * @return FinalResult object to access final result of long-running-operation.
     */
    FinalResult getFinalResult() {
        return this.finalResult;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("lroResponseBody", lroResponseBody)
            .writeJsonField("finalResult", finalResult)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SynchronouslySucceededLroData}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link SynchronouslySucceededLroData} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SynchronouslySucceededLroData} fails to be read from the {@code jsonReader}.
     */
    public static SynchronouslySucceededLroData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SynchronouslySucceededLroData lroData = new SynchronouslySucceededLroData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("lroResponseBody".equals(fieldName)) {
                    lroData.lroResponseBody = reader.getString();
                } else if ("finalResult".equals(fieldName)) {
                    lroData.finalResult = FinalResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return lroData;
        });
    }
}
