// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.implementation.models.CallRecordingStateChangeEventInternal;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.time.OffsetDateTime;

/** The call recording state change event. */
@Immutable
public final class CallRecordingStateChangeEvent extends CallingServerEventBase {
    /*
     * The call recording id
     */
    private final String recordingId;

    /*
     * The state of the recording
     */
    private final CallRecordingState state;

    /*
     * The time the recording started
     */
    private final OffsetDateTime startDateTime;

    /*
     * The call locator.
     */
    private final CallLocator callLocator;

    /**
     * Get the recordingId property: The call recording id.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return recordingId;
    }

    /**
     * Get the state property: The state of the recording.
     *
     * @return the state value.
     */
    public CallRecordingState getState() {
        return state;
    }

    /**
     * Get the startDateTime property: The time the recording started.
     *
     * @return the startDateTime value.
     */
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Get the callLocator property: The call locator.
     *
     * @return the callLocator value.
     */
    public CallLocator getCallLocator() {
        return callLocator;
    }

    /**
     * Initializes a new instance of CallRecordingStateChangeEvent.
     *
     * @param recordingId the recordingId value.
     * @param state the state value.
     * @param startDateTime the startDateTime value.
     * @param callLocator the callLocator value.
     */
    CallRecordingStateChangeEvent(
        String recordingId,
        CallRecordingState state,
        OffsetDateTime startDateTime,
        CallLocator callLocator) {
        this.recordingId = recordingId;
        this.state = state;
        this.startDateTime = startDateTime;
        this.callLocator = callLocator;
    }

    /**
     * Deserialize {@link CallRecordingStateChangeEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link CallRecordingStateChangeEvent} event.
     */
    public static CallRecordingStateChangeEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        CallRecordingStateChangeEventInternal callRecordingStateChangeEventInternal =
            eventData.toObject(CallRecordingStateChangeEventInternal.class);

        return new CallRecordingStateChangeEvent(
            callRecordingStateChangeEventInternal.getRecordingId(),
            callRecordingStateChangeEventInternal.getState(),
            callRecordingStateChangeEventInternal.getStartDateTime(),
            CallLocatorConverter.convert(callRecordingStateChangeEventInternal.getCallLocator()));
    }
}

