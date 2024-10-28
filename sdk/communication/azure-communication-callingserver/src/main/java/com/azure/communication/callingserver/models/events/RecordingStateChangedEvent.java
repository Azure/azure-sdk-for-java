// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.RecordingState;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

/** The RecordingStateChangedEvent model. */
@Immutable
public final class RecordingStateChangedEvent extends CallAutomationEventBase {

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

    private RecordingStateChangedEvent() {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeStringField("recordingId", recordingId)
            .writeStringField("state", Objects.toString(recordingState, null))
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link RecordingStateChangedEvent} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read from.
     * @return An instance of {@link RecordingStateChangedEvent}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static RecordingStateChangedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RecordingStateChangedEvent event = new RecordingStateChangedEvent();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(event, fieldName, reader)) {
                    continue;
                }

                if ("recordingId".equals(fieldName)) {
                    event.recordingId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    event.recordingState = RecordingState.fromString(reader.getString());
                } else if ("startDateTime".equals(fieldName)) {
                    event.startDateTime = CoreUtils.parseBestOffsetDateTime(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return event;
        });
    }
}
