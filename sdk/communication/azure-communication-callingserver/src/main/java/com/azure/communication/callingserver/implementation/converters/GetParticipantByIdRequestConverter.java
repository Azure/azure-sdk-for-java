// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.GetParticipantByIdRequest;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link GetParticipantByIdRequest}
 */
public final class GetParticipantByIdRequestConverter {

    /**
     * Converts to {@link RemoveParticipantByIdRequest}.
     */
    public static GetParticipantByIdRequest convert(
        CommunicationIdentifier participant) {
        if (participant == null) {
            return null;
        }

        return new GetParticipantByIdRequest()
            .setIdentifier(CommunicationIdentifierConverter.convert(participant));
    }

    private GetParticipantByIdRequestConverter() {
    }
}