// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AddParticipantsResponseInternal;
import com.azure.communication.callingserver.models.AddParticipantsResponse;

/**
 * A converter for {@link AddParticipantsResponse}
 */
public final class AddParticipantResponseConverter {

    /**
     * Converts for {@link AddParticipantsResponse}
     */
    public static AddParticipantsResponse convert(AddParticipantsResponseInternal addParticipantsResponseInternal) {
        return new AddParticipantsResponse(addParticipantsResponseInternal.getOperationId(),
            CallingOperationStatusConverter.convert(addParticipantsResponseInternal.getStatus()),
            addParticipantsResponseInternal.getOperationContext(),
            CallingOperationResultDetailsConverter.convert(addParticipantsResponseInternal.getResultDetails()));
    }

    private AddParticipantResponseConverter() {

    }
}
