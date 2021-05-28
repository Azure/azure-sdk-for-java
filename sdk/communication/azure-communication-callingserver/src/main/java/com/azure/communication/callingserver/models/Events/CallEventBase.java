// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

/**
 * Base type for all calling events.
 */
public abstract class CallEventBase {
    /**
     * Deserialize calling event.
     * 
     * @param eventType The event type.
     * @param eventData The event data.
     * @throws IllegalArgumentException if any properties are null or empty.
     * @return deserialized calling event
     */
    public static CallEventBase deserialize(String eventType, String eventData) {
        if (eventType == null || eventType.isEmpty()) {
            throw new IllegalArgumentException("object eventType cannot be null or empty");
        }
        if (eventData == null || eventData.isEmpty()) {
            throw new IllegalArgumentException("object eventData cannot be null or empty");
        }

        // switch (eventType)
        // {
        // case CallLegStateChangedEvent.EventType:
        // {
        // return CallLegStateChangedEvent.Deserialize(eventData);
        // }
        // case ToneReceivedEvent.EventType:
        // {
        // return ToneReceivedEvent.Deserialize(eventData);
        // }
        // case PlayAudioResultEvent.EventType:
        // {
        // return PlayAudioResultEvent.Deserialize(eventData);
        // }
        // case CallRecordingStateChangeEvent.EventType:
        // {
        // return CallRecordingStateChangeEvent.Deserialize(eventData);
        // }
        // case InviteParticipantsResultEvent.EventType:
        // {
        // return InviteParticipantsResultEvent.Deserialize(eventData);
        // }
        // case ParticipantsUpdatedEvent.EventType:
        // {
        // return ParticipantsUpdatedEvent.Deserialize(eventData);
        // }
        // default:
        // throw new UnsupportedOperationException("Provided event type is not
        // supported");
        // }
        return null;
    }
}
