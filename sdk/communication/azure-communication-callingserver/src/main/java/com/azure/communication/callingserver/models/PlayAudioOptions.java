// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import java.net.URI;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The request payload for playing audio. */
@Fluent
public final class PlayAudioOptions {

    /*
     * The flag indicating whether audio file needs to be played in loop or not.
     */
    @JsonProperty(value = "loop")
    private Boolean loop;

    /*
     * The value to identify context of the operation.
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * An id for the media in the AudioFileUri, using which we cache the media resource.
     */
    @JsonProperty(value = "audioFileId")
    private String audioFileId;

    /*
     * The callback Uri to receive playAudio status notifications.
     */
    @JsonProperty(value = "callbackUri")
    private URI callbackUri;

    /**
     * Get the loop property: The flag indicating whether audio file needs to be played in loop or not.
     *
     * @return the loop value.
     */
    public Boolean isLoop() {
        return loop;
    }

    /**
     * Set the loop property: The flag indicating whether audio file needs to be played in loop or not.
     *
     * @param loop the loop value to set.
     * @return the PlayAudioOptions object itself.
     */
    public PlayAudioOptions setLoop(Boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the PlayAudioOptions object itself.
     */
    public PlayAudioOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the audioFileId property: An id for the media in the AudioFileUri, using which we cache the media resource.
     *
     * @return the audioFileId value.
     */
    public String getAudioFileId() {
        return audioFileId;
    }

    /**
     * Set the audioFileId property: An id for the media in the AudioFileUri, using which we cache the media resource.
     *
     * @param audioFileId the audioFileId value to set.
     * @return the PlayAudioOptions object itself.
     */
    public PlayAudioOptions setAudioFileId(String audioFileId) {
        this.audioFileId = audioFileId;
        return this;
    }

    /**
     * Get the callbackUri property: The callback Uri to receive PlayAudio status notifications.
     *
     * @return the callbackUri value.
     */
    public URI getCallbackUri() {
        return callbackUri;
    }

    /**
     * Set the callbackUri property: The callback Uri to receive PlayAudio status notifications.
     *
     * @param callbackUri the callbackUri value to set.
     * @return the PlayAudioOptions object itself.
     */
    public PlayAudioOptions setCallbackUri(URI callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }
}
