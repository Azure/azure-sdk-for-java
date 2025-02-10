// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.Serializable;

/**
 * The error from AckMessage.
 */
@Immutable
public final class AckResponseError implements Serializable, JsonSerializable<AckResponseError> {
    private static final long serialVersionUID = 1L;

    /**
     * the name of the error.
     */
    private final String name;

    /**
     * the error message.
     */
    private final String message;

    /**
     * Creates a new instance of AckMessageError.
     *
     * @param name the name of the error.
     * @param message the error message.
     */
    public AckResponseError(String name, String message) {
        this.name = name;
        this.message = message;
    }

    /**
     * Gets the name of the error.
     *
     * @return the name of the error.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("name", name)
            .writeStringField("message", message)
            .writeEndObject();
    }

    /**
     * Reads an instance of AckResponseError from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AckResponseError if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the AckResponseError.
     */
    public static AckResponseError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            String message = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new AckResponseError(name, message);
        });
    }
}
