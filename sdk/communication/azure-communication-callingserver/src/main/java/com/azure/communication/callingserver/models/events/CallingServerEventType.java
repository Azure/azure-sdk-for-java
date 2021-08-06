// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for CallingServerEventType. */
@Immutable
public final class CallingServerEventType extends ExpandableStringEnum<CallingServerEventType> {

    /** The call connection state change event type. */
    public static final CallingServerEventType CALL_CONNECTION_STATE_CHANGED_EVENT = fromString("Microsoft.Communication.CallConnectionStateChanged");

    /** The add participant result event type. */
    public static final CallingServerEventType ADD_PARTICIPANT_RESULT_EVENT = fromString("Microsoft.Communication.AddParticipantResult");

    /** The call recording state change event type. */
    public static final CallingServerEventType CALL_RECORDING_STATE_CHANGED_EVENT = fromString("Microsoft.Communication.CallRecordingStateChanged");

    /** The play audio result event type. */
    public static final CallingServerEventType PLAY_AUDIO_RESULT_EVENT = fromString("Microsoft.Communication.PlayAudioResult");

    /** The participants updated event type. */
    public static final CallingServerEventType PARTICIPANTS_UPDATED_EVENT = fromString("Microsoft.Communication.ParticipantsUpdated");

    /** The subscribe to tone event type. */
    public static final CallingServerEventType TONE_RECEIVED_EVENT = fromString("Microsoft.Communication.DtmfReceived");

    /**
     * Creates or finds a CallingServerEventType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CallingServerEventType.
     */
    @JsonCreator
    public static CallingServerEventType fromString(String name) {
        return fromString(name, CallingServerEventType.class);
    }

    /** @return known CallingServerEventType values. */
    public static Collection<CallingServerEventType> values() {
        return values(CallingServerEventType.class);
    }
}
