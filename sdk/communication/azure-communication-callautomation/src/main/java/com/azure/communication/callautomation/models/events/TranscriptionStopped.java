// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The TranscriptionStopped model.
 */
@Fluent
public final class TranscriptionStopped extends CallAutomationEventBase {

    /*
     * Defines the result for TranscriptionUpdate with the current status and the details about the status
     */
    private TranscriptionUpdate transcriptionUpdateResult;

    /**
     * Creates an instance of TranscriptionStopped class.
     */
    public TranscriptionStopped() {
        transcriptionUpdateResult = null;
    }

    /**
     * Get the transcriptionUpdateResult property: Defines the result for TranscriptionUpdate with the current status
     * and the details about the status.
     *
     * @return the transcriptionUpdateResult value.
     */
    public TranscriptionUpdate getTranscriptionUpdateResult() {
        return this.transcriptionUpdateResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("transcriptionUpdate", transcriptionUpdateResult);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TranscriptionStopped from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TranscriptionStopped if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the TranscriptionStopped.
     */
    public static TranscriptionStopped fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionStopped event = new TranscriptionStopped();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transcriptionUpdate".equals(fieldName)) {
                    event.transcriptionUpdateResult = TranscriptionUpdate.fromJson(reader);
                } else {
                    if (!event.readField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
