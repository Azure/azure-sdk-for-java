// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

/**
 * A converter for {@link AddParticipantRequest}
 */
public final class AddParticipantRequestConverter {

    /**
     * Converts to {@link AddParticipantRequest}.
     */
    public static AddParticipantRequest convert(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        String callBackUri) {
        if (participant == null) {
            return null;
        }

        PhoneNumberIdentifierModel phoneNumberIdentifierModel =
            (alternateCallerId == null || alternateCallerId.isEmpty()) ? null
                : CommunicationIdentifierConverter
                    .convert(new PhoneNumberIdentifier(alternateCallerId)).getPhoneNumber();

        return new AddParticipantRequest()
            .setParticipant(CommunicationIdentifierConverter.convert(participant))
            .setAlternateCallerId(phoneNumberIdentifierModel)
            .setOperationContext(operationContext)
            .setCallbackUri(callBackUri);
    }

    private AddParticipantRequestConverter() {
    }
}
