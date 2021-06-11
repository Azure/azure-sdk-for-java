// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

import java.util.Collections;
import java.util.LinkedList;

public final class AddParticipantConverter {
    public static InviteParticipantsRequest convert(CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        if (participant == null) {
            return null;
        }
        PhoneNumberIdentifierModel phoneNumberIdentifierModel =
            (alternateCallerId == null || alternateCallerId.isEmpty()) ? null
                : CommunicationIdentifierConverter.convert(new PhoneNumberIdentifier(alternateCallerId)).getPhoneNumber();

        InviteParticipantsRequest inviteParticipantsRequest = new InviteParticipantsRequest();
        inviteParticipantsRequest.setParticipants(new LinkedList<>(Collections.singletonList(CommunicationIdentifierConverter.convert(participant))));
        inviteParticipantsRequest.setAlternateCallerId(phoneNumberIdentifierModel);
        inviteParticipantsRequest.setOperationContext(operationContext);
        inviteParticipantsRequest.setCallbackUri(callBackUri);

        return inviteParticipantsRequest;
    }

    private AddParticipantConverter() {
    }
}
