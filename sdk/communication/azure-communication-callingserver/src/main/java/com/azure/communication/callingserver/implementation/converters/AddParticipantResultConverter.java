// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AddParticipantResultInternal;
import com.azure.communication.callingserver.models.AddParticipantResult;

/**
 * A converter between {@link AddParticipantResultInternal} and {@link AddParticipantResult}.
 */
public final class AddParticipantResultConverter {

    /**
     * Maps from {@link AddParticipantResultInternal} to {@link AddParticipantResult}.
     */
    public static AddParticipantResult convert(AddParticipantResultInternal addParticipantResultInternal) {
        if (addParticipantResultInternal == null) {
            return null;
        }

        return new AddParticipantResult(
            addParticipantResultInternal.getOperationId(),
            addParticipantResultInternal.getStatus(),
            addParticipantResultInternal.getOperationContext(),
            ResultDetailsConverter.convert(addParticipantResultInternal.getResultDetails()));
    }
}
