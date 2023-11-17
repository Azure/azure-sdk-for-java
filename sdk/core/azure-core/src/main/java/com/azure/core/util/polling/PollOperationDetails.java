// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** PollOperationDetails provides details for long running operations. */
@Immutable
public final class PollOperationDetails implements JsonSerializable<PollOperationDetails> {
    @JsonProperty(value = "id", required = true)
    private final String operationId;

    @JsonProperty(value = "error")
    private ResponseError error;

    /**
     * Creates an instance of PollOperationDetails class.
     *
     * @param operationId the unique ID of the operation.
     */
    @JsonCreator
    private PollOperationDetails(@JsonProperty(value = "id", required = true) String operationId) {
        this.operationId = operationId;
    }

    /**
     * Gets the unique ID of the operation.
     *
     * @return the unique ID of the operation.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Gets the error object that describes the error when status is "Failed".
     *
     * @return the error object that describes the error when status is "Failed".
     */
    public ResponseError getError() {
        return this.error;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("id", operationId)
            .writeJsonField("error", error)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link PollOperationDetails}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link PollOperationDetails} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If a {@link PollOperationDetails} fails to be read from the {@code jsonReader}.
     */
    public static PollOperationDetails fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            boolean foundId = false;
            String id = null;
            ResponseError error = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    id = reader.getString();
                    foundId = true;
                } else if ("error".equals(fieldName)) {
                    error = ResponseError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            if (foundId) {
                PollOperationDetails pollOperationDetails = new PollOperationDetails(id);
                pollOperationDetails.error = error;
                return pollOperationDetails;
            } else {
                throw new IllegalStateException("Missing required property: id");
            }
        });
    }
}
