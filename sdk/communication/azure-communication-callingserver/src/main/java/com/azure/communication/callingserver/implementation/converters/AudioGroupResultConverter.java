// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AudioGroupResultInternal;
import com.azure.communication.callingserver.models.AudioGroupResult;
import com.azure.communication.callingserver.models.PlayAudioResult;

import java.util.stream.Collectors;

/**
 * A converter between {@link AudioGroupResultInternal} and {@link AudioGroupResult}.
 */
public final class AudioGroupResultConverter {

    /**
     * Maps from {@link AudioGroupResultInternal} to {@link PlayAudioResult}.
     */
    public static AudioGroupResult convert(AudioGroupResultInternal audioGroupResultInternal) {
        if (audioGroupResultInternal == null) {
            return null;
        }

        return new AudioGroupResult(
            audioGroupResultInternal.getAudioRoutingMode(),
            audioGroupResultInternal.getTargets().stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList())
        );
    }
}
