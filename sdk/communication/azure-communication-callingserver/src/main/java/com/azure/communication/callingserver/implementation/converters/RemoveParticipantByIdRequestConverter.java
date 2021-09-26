// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.RemoveParticipantByIdRequest;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link RemoveParticipantByIdRequest}
 */
public final class RemoveParticipantByIdRequestConverter {

    /**
     * Converts to {@link RemoveParticipantByIdRequest}.
     */
    public static RemoveParticipantByIdRequest convert(
        CommunicationIdentifier participant) {
        if (participant == null) {
            return null;
        }

        return new RemoveParticipantByIdRequest()
            .setIdentifier(CommunicationIdentifierConverter.convert(participant));
    }

    private RemoveParticipantByIdRequestConverter() {
    }
}