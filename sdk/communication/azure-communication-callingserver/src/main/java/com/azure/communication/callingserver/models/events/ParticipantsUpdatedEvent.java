// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationParticipantInternal;
import com.azure.communication.callingserver.implementation.models.ParticipantsUpdatedEventInternal;
import com.azure.communication.callingserver.models.CommunicationParticipant;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.util.BinaryData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The invited participants result event.
 */
public final class ParticipantsUpdatedEvent extends CallingServerEventBase {
    /**
     * The event type.
     */
    public static final String EVENT_TYPE = "Microsoft.Communication.ParticipantsUpdated";

    /**
     * The call leg Id.
     */
    private String callLegId;

    /**
     * The list of participants.
     */
    private List<CommunicationParticipant> participants;

    /**
     * Get the call leg Id.
     *
     * @return the time of the recording started.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the call leg Id.
     *
     * @param callLegId the call leg id.
     * @return the ParticipantsUpdatedEvent object itself.
     */
    public ParticipantsUpdatedEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * Get the list of participants.
     * 
     * @return the result info value.
     */
    public List<CommunicationParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Set the list of participants.
     * 
     * @param participants the list of participants.
     * @return the ParticipantsUpdatedEvent object itself.
     */
    public ParticipantsUpdatedEvent setParticipants(List<CommunicationParticipant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Initializes a new instance of ParticipantsUpdatedEvent.
     */
    public ParticipantsUpdatedEvent() {

    }

    /**
     * Initializes a new instance of ParticipantsUpdatedEvent.
     * 
     * @param callLegId The call leg id.
     * @param participants The conversation id.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public ParticipantsUpdatedEvent(String callLegId, Iterable<CommunicationParticipant> participants) {
        if (callLegId == null || callLegId.isEmpty()) {
            throw new IllegalArgumentException("object callLegId cannot be null or empty");
        }
        if (participants == null) {
            throw new IllegalArgumentException("object participants cannot be null");
        }

        this.callLegId = callLegId;
        this.participants = new ArrayList<>();

        participants.forEach(this.participants::add);
        if (this.participants.size() == 0) {
            throw new IllegalArgumentException(
                    String.format("object '%s' cannot be empty", participants.getClass().getName()));
        }
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
        List<CommunicationParticipant> participants = new LinkedList<>();
        for (CommunicationParticipantInternal communicationParticipantInternal : internalEvent.getParticipants()) {
            CommunicationIdentifier communicationIdentifier = CommunicationIdentifierConverter.convert(communicationParticipantInternal.getIdentifier());
            CommunicationParticipant communicationParticipant = new CommunicationParticipant(communicationIdentifier, communicationParticipantInternal.getParticipantId(), communicationParticipantInternal.isMuted());
            participants.add(communicationParticipant);
        }
        ParticipantsUpdatedEvent event = new ParticipantsUpdatedEvent(internalEvent.getCallLegId(), participants);
        return event;
    }
}
