// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.StartHoldMusicResultInternal;
import com.azure.communication.callingserver.models.StartHoldMusicResult;

/**
 * A converter between {@link StartHoldMusicResultInternal} and {@link StartHoldMusicResult}.
 */
public final class StartHoldMusicResultConverter {

    /**
     * Maps from {@link StartHoldMusicResultInternal} to {@link StartHoldMusicResult}.
     */
    public static StartHoldMusicResult convert(StartHoldMusicResultInternal startHoldMusicResultInternal) {
        if (startHoldMusicResultInternal == null) {
            return null;
        }

        return new StartHoldMusicResult(
            startHoldMusicResultInternal.getOperationId(),
            startHoldMusicResultInternal.getStatus(),
            startHoldMusicResultInternal.getOperationContext(),
            ResultInfoConverter.convert(startHoldMusicResultInternal.getResultInfo()));
    }
}
