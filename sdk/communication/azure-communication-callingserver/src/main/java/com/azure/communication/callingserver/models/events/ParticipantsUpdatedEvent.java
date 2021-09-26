// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CallParticipantInternal;
import com.azure.communication.callingserver.implementation.models.ParticipantsUpdatedEventInternal;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.util.LinkedList;
import java.util.List;

/**
 * The participants updated event.
 */
@Immutable
public final class ParticipantsUpdatedEvent extends CallingServerEventBase {
    /**
     * The call connection id.
     */
    private final String callConnectionId;

    /**
     * The participants.
     */
    private final List<CallParticipant> participants;

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
     * @return the list of participants value.
     */
    public List<CallParticipant> getParticipants() {
        return participants;
    }

    /**
     * Initializes a new instance of ParticipantsUpdatedEvent.
     *
     * @param callConnectionId The call connection id.
     * @param participants The participants
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    ParticipantsUpdatedEvent(String callConnectionId, List<CallParticipant> participants) {
        if (callConnectionId == null || callConnectionId.isEmpty()) {
            throw new IllegalArgumentException("object callConnectionId cannot be null or empty");
        }
        if (participants == null) {
            throw new IllegalArgumentException("object participants cannot be null");
        }
        this.callConnectionId = callConnectionId;
        this.participants = participants;
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
        for (CallParticipantInternal callParticipantInternal : internalEvent.getParticipants()) {
            participants.add(CallParticipantConverter.convert(callParticipantInternal));
        }

        return new ParticipantsUpdatedEvent(internalEvent.getCallConnectionId(), participants);
    }
}
