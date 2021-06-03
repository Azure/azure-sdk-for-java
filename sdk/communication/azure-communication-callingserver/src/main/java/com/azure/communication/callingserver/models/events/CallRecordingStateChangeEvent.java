// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import java.time.OffsetDateTime;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.core.util.BinaryData;

/**
 * The call recording state change event.
 */
public final class CallRecordingStateChangeEvent extends CallingServerEventBase {
    /**
     * The event type.
     */
    public static final String EVENT_TYPE = "Microsoft.Communication.CallRecordingStateChange";

    /**
     * The call recording id
     */
    private String recordingId;

    /**
     * Get the call recording id.
     * 
     * @return the call recording id value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Set the call recording id.
     * 
     * @param recordingId the call recording id.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setRecordingId(String recordingId) {
        this.recordingId = recordingId;
        return this;
    }

    /**
     * The call recording state.
     */
    private CallRecordingState state;

    /**
     * Get the call recording state.
     *
     * @return the call recording state value.
     */
    public CallRecordingState getState() {
        return this.state;
    }

    /**
     * Set the call recording state.
     *
     * @param state the call recording state.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setState(CallRecordingState state) {
        this.state = state;
        return this;
    }

    /**
     * The time of the recording started.
     */
    private OffsetDateTime startDateTime;

    /**
     * Get the time of the recording started.
     *
     * @return the time of the recording started.
     */
    public OffsetDateTime getStartDateTime() {
        return this.startDateTime;
    }

    /**
     * Set the subject.
     *
     * @param startDateTime the call leg id.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    /**
     * The conversation id from a out call start recording request.
     */
    private String conversationId;

    /**
     * Get the conversation id from a out call start recording request.
     * 
     * @return the time of the recording started.
     */
    public String getConversationId() {
        return this.conversationId;
    }

    /**
     * Set the conversation id from a out call start recording request.
     *
     * @param conversationId the call leg id.
     * @return the CallRecordingStateChangeEvent object itself.
     */
    public CallRecordingStateChangeEvent setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * Initializes a new instance of CallRecordingStateChangeEvent.
     * 
     * @param recordingId The recording id.
     * @param state The state.
     * @param startDateTime The startDateTime.
     * @param conversationId The conversation id.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public CallRecordingStateChangeEvent(String recordingId, CallRecordingState state, OffsetDateTime startDateTime,
            String conversationId) {
        if (recordingId == null || recordingId.isEmpty()) {
            throw new IllegalArgumentException("object recordingId cannot be null or empty");
        }
        if (state == null) {
            throw new IllegalArgumentException("object state cannot be null");
        }
        if (startDateTime == null) {
            throw new IllegalArgumentException("object startDateTime cannot be null");
        }
        if (conversationId == null || conversationId.isEmpty()) {
            throw new IllegalArgumentException("object conversationId cannot be null or empty");
        }
        this.recordingId = recordingId;
        this.state = state;
        this.startDateTime = startDateTime;
        this.conversationId = conversationId;
    }

    /**
     * Deserialize {@see CallRecordingStateChangeEvent} event.
     * 
     * @param eventData binary data for event
     * @return {@see CallRecordingStateChangeEvent} event.
     */
    public static CallRecordingStateChangeEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        return eventData.toObject(CallRecordingStateChangeEvent.class);
    }
}
