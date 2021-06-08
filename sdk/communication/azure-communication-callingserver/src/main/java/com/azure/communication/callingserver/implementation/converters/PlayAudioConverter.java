// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.PlayAudioOptions;

public class PlayAudioConverter {
    /**
     * Convert PlayAudioOptions into PlayAudioRequest
     * @param audioFileUri Audio file uri to be added during conversion
     * @param playAudioOptions PlayAudioOptions to be converted
     * @return PlayAudioRequest
     */
    public static PlayAudioRequest convert(String audioFileUri, PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(playAudioOptions.isLoop());
        playAudioRequest.setAudioFileId(playAudioOptions.getAudioFileId());
        playAudioRequest.setOperationContext(playAudioOptions.getOperationContext());
        playAudioRequest.setCallbackUri(playAudioOptions.getCallbackUri());
        return playAudioRequest;
    }
}
