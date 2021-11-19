// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AudioRoutingGroupResultInternal;
import com.azure.communication.callingserver.models.AudioRoutingGroupResult;
import com.azure.communication.callingserver.models.PlayAudioResult;

import java.util.stream.Collectors;

/**
 * A converter between {@link AudioRoutingGroupResultInternal} and {@link AudioRoutingGroupResult}.
 */
public final class AudioRoutingGroupResultConverter {

    /**
     * Maps from {@link AudioRoutingGroupResultInternal} to {@link PlayAudioResult}.
     */
    public static AudioRoutingGroupResult convert(AudioRoutingGroupResultInternal audioRoutingGroupResultInternal) {
        if (audioRoutingGroupResultInternal == null) {
            return null;
        }

        return new AudioRoutingGroupResult(
            audioRoutingGroupResultInternal.getAudioRoutingMode(),
            audioRoutingGroupResultInternal.getTargets().stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList())
        );
    }
}
