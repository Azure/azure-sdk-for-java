// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The TranscriptionUpdate model.
 */
@Fluent
public final class TranscriptionUpdate implements JsonSerializable<TranscriptionUpdate> {
    /*
     * The transcriptionStatus property.
     */
    private TranscriptionStatus transcriptionStatus;

    /*
     * The transcriptionStatusDetails property.
     */
    private TranscriptionStatusDetails transcriptionStatusDetails;

    /**
     * Creates an instance of TranscriptionUpdate class.
     */
    public TranscriptionUpdate() {
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
     * Set the transcriptionStatus property: The transcriptionStatus property.
     *
     * @param transcriptionStatus the transcriptionStatus value to set.
     * @return the TranscriptionUpdate object itself.
     */
    public TranscriptionUpdate setTranscriptionStatus(TranscriptionStatus transcriptionStatus) {
        this.transcriptionStatus = transcriptionStatus;
        return this;
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
     * Set the transcriptionStatusDetails property: The transcriptionStatusDetails property.
     *
     * @param transcriptionStatusDetails the transcriptionStatusDetails value to set.
     * @return the TranscriptionUpdate object itself.
     */
    public TranscriptionUpdate setTranscriptionStatusDetails(TranscriptionStatusDetails transcriptionStatusDetails) {
        this.transcriptionStatusDetails = transcriptionStatusDetails;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("transcriptionStatus", transcriptionStatus != null ? transcriptionStatus.toString() : null);
        jsonWriter.writeStringField("transcriptionStatusDetails", transcriptionStatusDetails != null ? transcriptionStatusDetails.toString() : null);
        return jsonWriter.writeEndObject();
    }

    static TranscriptionUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionUpdate event = new TranscriptionUpdate();
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
