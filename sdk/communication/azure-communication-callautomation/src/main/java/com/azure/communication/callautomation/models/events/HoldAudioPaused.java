// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The HoldAudioPaused model. */
@Immutable
public final class HoldAudioPaused extends CallAutomationEventBase {

    private HoldAudioPaused() {
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
     * Reads an instance of HoldAudioPaused from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of HoldAudioPaused if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the HoldAudioPaused.
     */
    public static HoldAudioPaused fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final HoldAudioPaused event = new HoldAudioPaused();
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
