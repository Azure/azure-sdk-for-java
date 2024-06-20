// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.RecordingState;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** The TeamsRecordingStateChanged model. */
@Immutable
public final class TeamsRecordingStateChanged extends CallAutomationEventBase {

    /**
     * Recording Id.
     */
    @JsonProperty(value = "recordingId")
    private String recordingId;

    /**
     * Recording state.
     */
    @JsonProperty(value = "state")
    private RecordingState recordingState;

    /**
     * Time of when it started recording.
     */
    @JsonIgnore
    private OffsetDateTime startDateTime;

    @JsonCreator
    private TeamsRecordingStateChanged(@JsonProperty("startDateTime") String startDateTime) {
        this.startDateTime = OffsetDateTime.parse(startDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.recordingId = null;
        this.recordingState = null;
    }

    private TeamsRecordingStateChanged() {

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

    static TeamsRecordingStateChanged fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TeamsRecordingStateChanged event = new TeamsRecordingStateChanged();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("recordingId".equals(fieldName)) {
                    event.recordingId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    event.recordingState = RecordingState.fromString(reader.getString());
                } else if ("startDateTime".equals(fieldName)) {
                    event.startDateTime = OffsetDateTime.parse(reader.getString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
