// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequestInternal;
import com.azure.communication.common.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.communication.callingserver.models.InviteParticipantsRequest} and
 * {@link InviteParticipantsRequestInternal}.
 */
public final class InviteParticipantsRequestConverter {
    /**
     * Maps from {com.azure.communication.callingserver.models.InviteParticipantsRequest} to {@link InviteParticipantsRequestInternal}.
     */
    public static InviteParticipantsRequestInternal convert(com.azure.communication.callingserver.models.InviteParticipantsRequest obj) {
        if (obj == null) {
            return null;
        }
        List<CommunicationIdentifierModel> participants = new ArrayList<>();
        for (CommunicationIdentifier participant : obj.getParticipants()) {
            participants.add(CommunicationIdentifierConverter.convert(participant));
        }

        InviteParticipantsRequestInternal inviteParticipantsRequest = new InviteParticipantsRequestInternal()
            .setParticipants(participants)
            .setAlternateCallerId(CommunicationIdentifierConverter.convert(obj.getAlternateCallerId()).getPhoneNumber())
            .setOperationContext(obj.getOperationContext())
            .setCallbackUri(obj.getCallbackUri());
        return inviteParticipantsRequest;
    }

    private InviteParticipantsRequestConverter() {
    }
}
