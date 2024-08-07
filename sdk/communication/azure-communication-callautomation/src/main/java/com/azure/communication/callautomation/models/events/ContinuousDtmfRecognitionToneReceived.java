// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The ContinuousDtmfRecognitionToneReceived model. */
@Immutable
public final class ContinuousDtmfRecognitionToneReceived extends CallAutomationEventBase {

    /*
     * The sequence id which can be used to determine if the same tone was played multiple times or if any tones were missed.
     */
    private Integer sequenceId;

    /*
     * The tone property.
     */
    private DtmfTone tone;

    /**
     * Constructor for ContinuousDtmfRecognitionToneReceived
     */
    public ContinuousDtmfRecognitionToneReceived() {
        sequenceId = 0;
        tone = null;
    }

    /**
     * Get sequenceId: The sequence id which can be used to determine if the same tone was played multiple
     * times or if any tones were missed.
     *
     * @return the sequenceId value.
     */
    public int getSequenceId() {
        return this.sequenceId;
    }

    /**
     * Get the tone property:
     *
     * @return the tone value.
     */
    public DtmfTone getTone() {
        return this.tone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeNumberField("sequenceId", sequenceId);
        jsonWriter.writeStringField("tone", tone.toString());
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ContinuousDtmfRecognitionToneReceived from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ContinuousDtmfRecognitionToneReceived if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ContinuousDtmfRecognitionToneReceived.
     */
    public static ContinuousDtmfRecognitionToneReceived fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ContinuousDtmfRecognitionToneReceived event = new ContinuousDtmfRecognitionToneReceived();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("sequenceId".equals(fieldName)) {
                    event.sequenceId = reader.getInt();
                } else if ("tone".equals(fieldName)) {
                    event.tone = DtmfTone.fromString(reader.getString());
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
