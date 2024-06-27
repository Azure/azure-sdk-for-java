// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.RecordingState;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** The RecordingStateChanged model. */
@Immutable
public final class RecordingStateChanged extends CallAutomationEventBase {

    /**
     * Recording Id.
     */
    private String recordingId;

    /**
     * Recording state.
     */
    private RecordingState recordingState;

    /**
     * Time of when it started recording.
     */
    private OffsetDateTime startDateTime;

    private RecordingStateChanged() {

    }

    /**
     * Get the recordingId property: Recording Id.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return recordingId;
    }

    /**
     * Get the recordingState property: Recording State.
     *
     * @return the recordingState value.
     */
    public RecordingState getRecordingState() {
        return recordingState;
    }

    /**
     * Get the startDateTime property: Start Date time.
     *
     * @return the startDateTime value.
     */
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Reads an instance of RecordingStateChanged from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecordingStateChanged if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecordingStateChanged.
     */
    public static RecordingStateChanged fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecordingStateChanged event = new RecordingStateChanged();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("startDateTime".equals(fieldName)) {
                    event.startDateTime = OffsetDateTime.parse(reader.getString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } else if ("recordingId".equals(fieldName)) {
                    event.recordingId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    event.recordingState = RecordingState.fromString(reader.getString());
                } else {
                    if (!event.handleField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
