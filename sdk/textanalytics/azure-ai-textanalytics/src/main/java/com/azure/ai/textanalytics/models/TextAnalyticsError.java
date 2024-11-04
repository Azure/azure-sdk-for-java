// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The {@code TextAnalyticsError} model.
 */
@Immutable
public final class TextAnalyticsError implements JsonSerializable<TextAnalyticsError> {
    /*
     * Error code. Possible values include: 'invalidRequest',
     * 'invalidArgument', 'internalServerError', 'serviceUnavailable'
     */
    private final TextAnalyticsErrorCode errorCode;

    /*
     * Error message.
     */
    private final String message;

    /*
     * Error target.
     */
    private final String target;

    /**
     * Creates a {@code TextAnalyticsError} model that describes text analytics error.
     * @param errorCode The error code.
     * @param message The error message.
     * @param target The error target.
     */
    public TextAnalyticsError(TextAnalyticsErrorCode errorCode, String message, String target) {
        this.errorCode = errorCode;
        this.message = message;
        this.target = target;
    }

    /**
     * Get the code property: Error code. Possible values include:
     * 'invalidRequest', 'invalidArgument', 'internalServerError',
     * 'serviceUnavailable'.
     *
     * @return The code value.
     */
    public TextAnalyticsErrorCode getErrorCode() {
        return this.errorCode;
    }

    /**
     * Get the message property: Error message.
     *
     * @return The message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: Error target.
     *
     * @return The target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("errorCode", this.errorCode == null ? null : this.errorCode.toString());
        jsonWriter.writeStringField("message", this.message);
        jsonWriter.writeStringField("target", this.target);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TextAnalyticsError from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TextAnalyticsError if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TextAnalyticsError.
     */
    public static TextAnalyticsError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TextAnalyticsErrorCode errorCode = null;
            String message = null;
            String target = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("errorCode".equals(fieldName)) {
                    errorCode = TextAnalyticsErrorCode.fromString(reader.getString());
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else if ("target".equals(fieldName)) {
                    target = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new TextAnalyticsError(errorCode, message, target);
        });
    }
}
