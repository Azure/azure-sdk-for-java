// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.PlayAudioOptions;

public class PlayAudioConverter {
    public static PlayAudioRequest convert(String audioFileUri, PlayAudioOptions playAudioOptions) {
        return new PlayAudioRequest()
            .setAudioFileUri(audioFileUri)
            .setLoop(playAudioOptions.isLoop())
            .setAudioFileId(playAudioOptions.getAudioFileId())
            .setOperationContext(playAudioOptions.getOperationContext())
            .setCallbackUri(playAudioOptions.getCallbackUri());
    }
}
