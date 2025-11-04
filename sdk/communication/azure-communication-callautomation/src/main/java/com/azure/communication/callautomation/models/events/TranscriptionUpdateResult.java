// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * The TranscriptionUpdateResult model.
 */
@Fluent
public final class TranscriptionUpdateResult implements JsonSerializable<TranscriptionUpdateResult> {
    /*
     * The transcriptionStatus property.
     */
    private TranscriptionStatus transcriptionStatus;

    /*
     * The transcriptionStatusDetails property.
     */
    private TranscriptionStatusDetails transcriptionStatusDetails;

    /**
     * Creates an instance of TranscriptionUpdateResult class.
     */
    public TranscriptionUpdateResult() {
    }

    /**
     * Get the transcriptionStatus property: The transcriptionStatus property.
     *
     * @return the transcriptionStatus value.
     */
    public TranscriptionStatus getTranscriptionStatus() {
        return this.transcriptionStatus;
    }

    /**
     * Get the transcriptionStatusDetails property: The transcriptionStatusDetails property.
     *
     * @return the transcriptionStatusDetails value.
     */
    public TranscriptionStatusDetails getTranscriptionStatusDetails() {
        return this.transcriptionStatusDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("transcriptionStatus",
            transcriptionStatus != null ? transcriptionStatus.toString() : null);
        jsonWriter.writeStringField("transcriptionStatusDetails",
            transcriptionStatusDetails != null ? transcriptionStatusDetails.toString() : null);
        return jsonWriter.writeEndObject();
    }

    static TranscriptionUpdateResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionUpdateResult event = new TranscriptionUpdateResult();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transcriptionStatus".equals(fieldName)) {
                    event.transcriptionStatus = TranscriptionStatus.fromString(reader.getString());
                } else if ("transcriptionStatusDetails".equals(fieldName)) {
                    event.transcriptionStatusDetails = TranscriptionStatusDetails.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
