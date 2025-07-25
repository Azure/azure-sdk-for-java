// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.monitor.query.logs.implementation.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Response to a batch query.
 */
@Immutable
public final class BatchResponse implements JsonSerializable<BatchResponse> {

    /*
     * An array of responses corresponding to each individual request in a batch.
     */
    @Generated
    private List<BatchQueryResponse> responses;

    /**
     * Creates an instance of BatchResponse class.
     */
    @Generated
    private BatchResponse() {
    }

    /**
     * Get the responses property: An array of responses corresponding to each individual request in a batch.
     *
     * @return the responses value.
     */
    @Generated
    public List<BatchQueryResponse> getResponses() {
        return this.responses;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("responses", this.responses, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BatchResponse from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BatchResponse if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the BatchResponse.
     */
    @Generated
    public static BatchResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BatchResponse deserializedBatchResponse = new BatchResponse();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("responses".equals(fieldName)) {
                    List<BatchQueryResponse> responses
                        = reader.readArray(reader1 -> BatchQueryResponse.fromJson(reader1));
                    deserializedBatchResponse.responses = responses;
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedBatchResponse;
        });
    }
}
