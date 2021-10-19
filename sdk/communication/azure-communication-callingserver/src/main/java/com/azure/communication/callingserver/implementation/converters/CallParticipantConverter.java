// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallParticipantInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter for {@link CallParticipant}
 */
public final class CallParticipantConverter {

    /**
     * Converts to {@link CallParticipant}.
     */
    public static CallParticipant convert(
        CallParticipantInternal callParticipantInternal) {
            
        if (callParticipantInternal == null) {
            return null;
        }

        return new CallParticipant(
            CommunicationIdentifierConverter.convert(callParticipantInternal.getIdentifier()),
            callParticipantInternal.getParticipantId(),
            callParticipantInternal.isMuted());
    }

    private CallParticipantConverter() {
    }
}