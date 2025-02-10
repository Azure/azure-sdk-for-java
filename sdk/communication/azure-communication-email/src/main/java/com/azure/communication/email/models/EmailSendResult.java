// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.ResponseError;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** Status of the long running operation. */
@Fluent
public final class EmailSendResult implements JsonSerializable<EmailSendResult> {
    /*
     * The unique id of the operation. Use a UUID.
     */
    private String id;

    /*
     * Status of operation.
     */
    private EmailSendStatus status;

    /*
     * Response error when status is a non-success terminal state.
     */
    private ResponseError error;

    /**
     * Creates an instance of EmailSendResult class.
     *
     * @param id the id value to set.
     * @param status the status value to set.
     * @param error the error value to set.
     */
    public EmailSendResult(String id, EmailSendStatus status, ResponseError error) {
        this.id = id;
        this.status = status;
        this.error = error;
    }

    /**
     * Get the id property: The unique id of the operation. Use a UUID.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the status property: Status of operation.
     *
     * @return the status value.
     */
    public EmailSendStatus getStatus() {
        return this.status;
    }

    /**
     * Get the error property: Response error when status is a non-success terminal state.
     *
     * @return the error value.
     */
    public ResponseError getError() {
        return this.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id);
        jsonWriter.writeStringField("status", status != null ? status.toString() : null);
        jsonWriter.writeJsonField("error", error);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EmailSendResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of EmailSendResult if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the EmailSendResult.
     */
    public static EmailSendResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            EmailSendStatus status = null;
            ResponseError error = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("status".equals(fieldName)) {
                    status = EmailSendStatus.fromString(reader.getString());
                } else if ("error".equals(fieldName)) {
                    error = ResponseError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return new EmailSendResult(id, status, error);
        });
    }
}
