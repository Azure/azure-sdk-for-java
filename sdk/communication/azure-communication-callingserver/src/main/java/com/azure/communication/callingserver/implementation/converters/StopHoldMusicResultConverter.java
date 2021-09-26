// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.StopHoldMusicResultInternal;
import com.azure.communication.callingserver.models.StopHoldMusicResult;

/**
 * A converter between {@link StopHoldMusicResultInternal} and {@link StopHoldMusicResult}.
 */
public final class StopHoldMusicResultConverter {

    /**
     * Maps from {@link StopHoldMusicResultInternal} to {@link StopHoldMusicResult}.
     */
    public static StopHoldMusicResult convert(StopHoldMusicResultInternal stopHoldMusicResultInternal) {
        if (stopHoldMusicResultInternal == null) {
            return null;
        }

        return new StopHoldMusicResult(
            stopHoldMusicResultInternal.getOperationId(),
            stopHoldMusicResultInternal.getStatus(),
            stopHoldMusicResultInternal.getOperationContext(),
            ResultInfoConverter.convert(stopHoldMusicResultInternal.getResultInfo()));
    }
}
