// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequest;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link TransferToParticipantRequest}
 */
public final class TransferToParticipantRequestConverter {

    /**
     * Converts to {@link TransferToParticipantRequest}.
     */
    public static TransferToParticipantRequest convert(
        CommunicationIdentifier targetParticipant,
        String alternateCallerId,
        String userToUserInformation,
        String operationContext) {

        if (targetParticipant == null) {
            return null;
        }

        PhoneNumberIdentifierModel alternateIdentity = null;
        if (alternateCallerId != null && !alternateCallerId.isEmpty()) {
            alternateIdentity = new PhoneNumberIdentifierModel()
                .setValue(alternateCallerId);
        }

        return new TransferToParticipantRequest()
            .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
            .setAlternateCallerId(alternateIdentity)
            .setUserToUserInformation(userToUserInformation)
            .setOperationContext(operationContext);
    }

    private TransferToParticipantRequestConverter() {
    }
}
