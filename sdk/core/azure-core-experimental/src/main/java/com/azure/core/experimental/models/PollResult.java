// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** Provides status details for long-running operations. */
@Immutable
public final class PollResult implements JsonSerializable<PollResult> {
    private final String operationId;
    private ResponseError error;

    /**
     * Creates an instance of ResourceOperationStatusUserError class.
     *
     * @param operationId the unique ID of the operation.
     */
    private PollResult(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get the id property: The unique ID of the operation.
     *
     * @return the unique ID of the operation.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Get the error property: Error object that describes the error when status is "Failed".
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
     * Deserializes an instance of {@link PollResult} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link PollResult}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static PollResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String operationId = null;
            ResponseError error = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    operationId = reader.getString();
                } else if ("error".equals(fieldName)) {
                    error = ResponseError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            PollResult pollResult = new PollResult(operationId);
            pollResult.error = error;

            return pollResult;
        });
    }
}
