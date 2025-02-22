// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingKind;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

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
     * Recording kind.
     */
    private RecordingKind recordingKind;

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
     * Get the recordingKind property: Recording Kind.
     *
     * @return the recordingKind value.
     */
    public RecordingKind getRecordingKind() {
        return recordingKind;
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
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("startDateTime", startDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        jsonWriter.writeStringField("recordingId", recordingId);
        jsonWriter.writeStringField("state", recordingState != null ? recordingState.toString() : null);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
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
                    event.startDateTime
                        = OffsetDateTime.parse(reader.getString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } else if ("recordingId".equals(fieldName)) {
                    event.recordingId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    event.recordingState = RecordingState.fromString(reader.getString());
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
