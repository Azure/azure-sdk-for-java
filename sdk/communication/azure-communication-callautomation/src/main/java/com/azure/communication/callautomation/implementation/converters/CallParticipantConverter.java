// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.models.CallParticipant;

/**
 * A converter for {@link CallParticipant}
 */
public final class CallParticipantConverter {

    /**
     * Converts to {@link CallParticipant}.
     */
    public static CallParticipant convert(CallParticipantInternal acsCallParticipantDto) {

        if (acsCallParticipantDto == null) {
            return null;
        }

        return new CallParticipant(CommunicationIdentifierConverter.convert(acsCallParticipantDto.getIdentifier()),
            acsCallParticipantDto.isMuted());
    }

    private CallParticipantConverter() {
    }
}
