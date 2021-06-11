// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PlayAudioResultInternal;
import com.azure.communication.callingserver.models.PlayAudioResult;

public final class PlayAudioResultConverter {
    public static PlayAudioResult convert(PlayAudioResultInternal playAudioResultInternal) {
        return new PlayAudioResult(
            playAudioResultInternal.getId(),
            playAudioResultInternal.getStatus(),
            playAudioResultInternal.getOperationContext(),
            ResultInfoConverter.convert(playAudioResultInternal.getResultInfo()));
    }
}
