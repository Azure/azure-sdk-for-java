// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;

/**
 * A converter for {@link RemoveParticipantsResponse}
 */
public final class RemoveParticipantsResponseConverter {

    /**
     * Converts to {@link RemoveParticipantsResponse}.
     */
    public static RemoveParticipantsResponse convert(
        RemoveParticipantsResponseInternal removeParticipantsResponseInternal) {

        if (removeParticipantsResponseInternal == null) {
            return null;
        }

        return new RemoveParticipantsResponse(removeParticipantsResponseInternal.getOperationId(),
            CallingOperationStatusConverter.convert(removeParticipantsResponseInternal.getStatus()),
            removeParticipantsResponseInternal.getOperationContext(),
            CallingOperationResultDetailsConverter.convert(removeParticipantsResponseInternal.getResultDetails()));
    }

    private RemoveParticipantsResponseConverter() {
    }
}
