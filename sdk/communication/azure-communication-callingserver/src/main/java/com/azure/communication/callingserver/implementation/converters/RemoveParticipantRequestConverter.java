// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.RemoveParticipantRequest;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link RemoveParticipantRequest}
 */
public final class RemoveParticipantRequestConverter {

    /**
     * Converts to {@link RemoveParticipantByIdRequest}.
     */
    public static RemoveParticipantRequest convert(
        CommunicationIdentifier participant) {
        if (participant == null) {
            return null;
        }

        return new RemoveParticipantRequest()
            .setIdentifier(CommunicationIdentifierConverter.convert(participant));
    }

    private RemoveParticipantRequestConverter() {
    }
}
