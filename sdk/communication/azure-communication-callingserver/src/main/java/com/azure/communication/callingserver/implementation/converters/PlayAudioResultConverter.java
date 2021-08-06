// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PlayAudioResultInternal;
import com.azure.communication.callingserver.models.PlayAudioResult;

/**
 * A converter between {@link PlayAudioResultInternal} and {@link PlayAudioResult}.
 */
public final class PlayAudioResultConverter {

    /**
     * Maps from {@link PlayAudioResultInternal} to {@link PlayAudioResult}.
     */
    public static PlayAudioResult convert(PlayAudioResultInternal playAudioResultInternal) {
        if (playAudioResultInternal == null) {
            return null;
        }

        return new PlayAudioResult(
            playAudioResultInternal.getOperationId(),
            playAudioResultInternal.getStatus(),
            playAudioResultInternal.getOperationContext(),
            ResultInfoConverter.convert(playAudioResultInternal.getResultInfo()));
    }
}
