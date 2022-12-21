// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AcsCallParticipantInternal;
import com.azure.communication.callingserver.models.CallParticipant;

/**
 * A converter for {@link CallParticipant}
 */
public final class CallParticipantConverter {

    /**
     * Converts to {@link CallParticipant}.
     */
    public static CallParticipant convert(
        AcsCallParticipantInternal acsCallParticipantDto) {

        if (acsCallParticipantDto == null) {
            return null;
        }

        return new CallParticipant(CommunicationIdentifierConverter.convert(acsCallParticipantDto.getIdentifier()),
            acsCallParticipantDto.isMuted());
    }

    private CallParticipantConverter() {
    }
}
