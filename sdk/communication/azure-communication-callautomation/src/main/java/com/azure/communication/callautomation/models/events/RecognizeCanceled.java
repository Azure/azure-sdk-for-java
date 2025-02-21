// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The RecognizeCanceled model. */
@Fluent
public final class RecognizeCanceled extends CallAutomationEventBase {
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RecognizeCanceled from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecognizeCanceled if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecognizeCanceled.
     */
    public static RecognizeCanceled fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognizeCanceled event = new RecognizeCanceled();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if (!event.readField(fieldName, reader)) {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
