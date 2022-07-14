// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AcsCallParticipantInternal;
import com.azure.communication.callingserver.models.AcsCallParticipant;

/**
 * A converter for {@link AcsCallParticipant}
 */
public final class AcsCallParticipantConverter {

    /**
     * Converts to {@link AcsCallParticipant}.
     */
    public static AcsCallParticipant convert(
        AcsCallParticipantInternal acsCallParticipantDto) {

        if (acsCallParticipantDto == null) {
            return null;
        }

        return new AcsCallParticipant(CommunicationIdentifierConverter.convert(acsCallParticipantDto.getIdentifier()),
            acsCallParticipantDto.isMuted());
    }

    private AcsCallParticipantConverter() {
    }
}
