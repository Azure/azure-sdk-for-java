// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.TransferCallRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link TransferCallRequest}
 */
public final class TransferCallRequestConverter {

    /**
     * Converts to {@link TransferCallRequest}.
     */
    public static TransferCallRequest convert(
        CommunicationIdentifier targetParticipant,
        String userToUserInformation) {
            
        if (targetParticipant == null) {
            return null;
        }

        return new TransferCallRequest()
            .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
            .setUserToUserInformation(userToUserInformation);
    }

    private TransferCallRequestConverter() {
    }
}