// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.PlayAudioOptions;

public class PlayAudioOptionsConverter {
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

    /**
     * Convert Play audio options into PlayAudioRequest
     * @param audioFileUri The media resource uri of the play audio request.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param operationContext The value to identify context of the operation.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @return PlayAudioRequest
     */
    public static PlayAudioRequest convert(String audioFileUri,
                                           boolean loop,
                                           String audioFileId,
                                           String operationContext,
                                           String callbackUri) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(loop);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
        return playAudioRequest;
    }
}
