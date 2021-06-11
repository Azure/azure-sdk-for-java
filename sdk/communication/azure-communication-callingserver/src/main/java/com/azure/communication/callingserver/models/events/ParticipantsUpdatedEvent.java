// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationParticipantInternal;
import com.azure.communication.callingserver.implementation.models.ParticipantsUpdatedEventInternal;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.core.util.BinaryData;

import java.util.LinkedList;
import java.util.List;

/**
 * The participants updated event.
 */
public final class ParticipantsUpdatedEvent extends CallingServerEventBase {
    /**
     * The call connection id.
     */
    private final String callConnectionId;

    /**
     * The participants.
     */
    private final CallParticipant[] participants;

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }


    /**
     * Get the participants.
     *
     * @return the result info value.
     */
    public CallParticipant[] getParticipants() {
        return participants == null ? new CallParticipant[0] : participants.clone();
    }

    /**
     * Initializes a new instance of ParticipantsUpdatedEvent.
     *
     * @param callConnectionId The call connection id.
     * @param participants The participants
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public ParticipantsUpdatedEvent(String callConnectionId, CallParticipant[] participants) {
        if (callConnectionId == null || callConnectionId.isEmpty()) {
            throw new IllegalArgumentException("object callConnectionId cannot be null or empty");
        }
        if (participants == null) {
            throw new IllegalArgumentException("object participants cannot be null");
        }
        this.callConnectionId = callConnectionId;
        this.participants = participants.clone();
    }

    /**
     * Deserialize {@link ParticipantsUpdatedEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link ParticipantsUpdatedEvent} event.
     */
    public static ParticipantsUpdatedEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        ParticipantsUpdatedEventInternal internalEvent = eventData.toObject(ParticipantsUpdatedEventInternal.class);
        List<CallParticipant> participants = new LinkedList<>();
        for (CommunicationParticipantInternal communicationParticipantInternal : internalEvent.getParticipants()) {
            participants.add(
                new CallParticipant(
                    CommunicationIdentifierConverter.convert(communicationParticipantInternal.getIdentifier()),
                    communicationParticipantInternal.getParticipantId(),
                    communicationParticipantInternal.isMuted()));
        }
        return new ParticipantsUpdatedEvent(internalEvent.getCallConnectionId(),
            participants.toArray(new CallParticipant[0]));
    }
}
