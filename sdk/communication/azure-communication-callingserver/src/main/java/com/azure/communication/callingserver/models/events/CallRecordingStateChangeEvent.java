// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/** The call recording state change event. */
@Fluent
public final class CallRecordingStateChangeEvent extends CallingServerEventBase {
    /*
     * The call recording id
     */
    @JsonProperty(value = "recordingId")
    private String recordingId;

    /*
     * The recording state of the recording
     */
    @JsonProperty(value = "state")
    private CallRecordingState state;

    /*
     * The time of the recording started
     */
    @JsonProperty(value = "startDateTime")
    private OffsetDateTime startDateTime;

    /*
     * The conversation id from a out call start recording request
     */
    @JsonProperty(value = "conversationId")
    private String conversationId;

    /**
     * Get the recordingId property: The call recording id.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Set the recordingId property: The call recording id.
     *
     * @param recordingId the recordingId value to set.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setRecordingId(String recordingId) {
        this.recordingId = recordingId;
        return this;
    }

    /**
     * Get the state property: The recording state of the recording.
     *
     * @return the state value.
     */
    public CallRecordingState getState() {
        return this.state;
    }

    /**
     * Set the state property: The recording state of the recording.
     *
     * @param state the state value to set.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setState(CallRecordingState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the startDateTime property: The time of the recording started.
     *
     * @return the startDateTime value.
     */
    public OffsetDateTime getStartDateTime() {
        return this.startDateTime;
    }

    /**
     * Set the startDateTime property: The time of the recording started.
     *
     * @param startDateTime the startDateTime value to set.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    /**
     * Get the conversationId property: The conversation id from a out call start recording request.
     *
     * @return the conversationId value.
     */
    public String getConversationId() {
        return this.conversationId;
    }

    /**
     * Set the conversationId property: The conversation id from a out call start recording request.
     *
     * @param conversationId the conversationId value to set.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * Deserialize {@link CallRecordingStateChangeEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link CallRecordingStateChangeEvent} event.
     */
    public static CallRecordingStateChangeEvent deserialize(BinaryData eventData) {
        return eventData == null ? null : eventData.toObject(CallRecordingStateChangeEvent.class);
    }
}
