// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * A converter for {@link com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest}
 */
public final class InviteParticipantsRequestConverter {
    /**
     * Maps to {@link InviteParticipantsRequest}.
     */
    public static InviteParticipantsRequest convert(CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        if (participant == null) {
            return null;
        }
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = (alternateCallerId == null || alternateCallerId.isEmpty()) ? null
            : CommunicationIdentifierConverter.convert(new PhoneNumberIdentifier(alternateCallerId)).getPhoneNumber();

        InviteParticipantsRequest inviteParticipantsRequest = new InviteParticipantsRequest()
            .setParticipants(new LinkedList<CommunicationIdentifierModel>(Arrays.asList(CommunicationIdentifierConverter.convert(participant))))
            .setAlternateCallerId(phoneNumberIdentifierModel)
            .setOperationContext(operationContext)
            .setCallbackUri(callBackUri);

        return inviteParticipantsRequest;
    }

    private InviteParticipantsRequestConverter() {
    }
}
