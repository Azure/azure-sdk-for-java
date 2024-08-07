// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The ConnectFailed model. */
@Immutable
public final class ConnectFailed extends CallAutomationEventBase {
    private ConnectFailed() {

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
     * Reads an instance of ConnectFailed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConnectFailed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ConnectFailed.
     */
    public static ConnectFailed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ConnectFailed event = new ConnectFailed();
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
