// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.RecordingKind;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** The RecordingStateChanged model. */
@Immutable
public final class RecordingStateChanged extends CallAutomationEventBase {

    /**
     * Recording Id.
     */
    @JsonProperty(value = "recordingId")
    private final String recordingId;

    /**
     * Recording state.
     */
    @JsonProperty(value = "state")
    private final RecordingState recordingState;

    /**
     * Recording kind.
     */
    @JsonProperty(value = "recordingKind")
    private final RecordingKind recordingKind;

    /**
     * Time of when it started recording.
     */
    @JsonIgnore
    private final OffsetDateTime startDateTime;

    @JsonCreator
    private RecordingStateChanged(@JsonProperty("startDateTime") String startDateTime) {
        this.startDateTime = OffsetDateTime.parse(startDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.recordingId = null;
        this.recordingState = null;
        this.recordingKind = null;
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
     * Get the Recording kind property: Recording Kind.
     * 
     * @return the recordingKind.
     */
    public RecordingKind getRecordingKind() {
        return recordingKind;
    }
}
