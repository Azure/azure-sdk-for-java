// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The CallDisconnected model. */
@Immutable
public final class CallDisconnected extends CallAutomationEventBase {
    private CallDisconnected() {
    }

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
     * Reads an instance of CallDisconnected from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallDisconnected if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallDisconnected.
     */
    public static CallDisconnected fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallDisconnected event = new CallDisconnected();
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
