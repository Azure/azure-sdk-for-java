// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.communication.callingserver.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The request payload for playing audio. */
@Fluent
public final class PlayAudioRequest {
    /*
     * The media resource uri of the play audio request.
     * Currently only Wave file (.wav) format audio prompts are supported.
     * More specifically, the audio content in the wave file must be mono
     * (single-channel),
     * 16-bit samples with a 16,000 (16KHz) sampling rate.
     */
    @JsonProperty(value = "audioFileUri")
    private String audioFileUri;

    /*
     * The flag indicating whether audio file needs to be played in loop or
     * not.
     */
    @JsonProperty(value = "loop")
    private Boolean loop;

    /*
     * The value to identify context of the operation.
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * An id for the media in the AudioFileUri, using which we cache the media
     * resource.
     */
    @JsonProperty(value = "audioFileId")
    private String audioFileId;

    /*
     * The callback Uri to receive PlayAudio status notifications.
     */
    @JsonProperty(value = "callbackUri")
    private String callbackUri;

    /**
     * Get the audioFileUri property: The media resource uri of the play audio request. Currently only Wave file (.wav)
     * format audio prompts are supported. More specifically, the audio content in the wave file must be mono
     * (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     *
     * @return the audioFileUri value.
     */
    public String getAudioFileUri() {
        return this.audioFileUri;
    }

    /**
     * Set the audioFileUri property: The media resource uri of the play audio request. Currently only Wave file (.wav)
     * format audio prompts are supported. More specifically, the audio content in the wave file must be mono
     * (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     *
     * @param audioFileUri the audioFileUri value to set.
     * @return the PlayAudioRequest object itself.
     */
    public PlayAudioRequest setAudioFileUri(String audioFileUri) {
        this.audioFileUri = audioFileUri;
        return this;
    }

    /**
     * Get the loop property: The flag indicating whether audio file needs to be played in loop or not.
     *
     * @return the loop value.
     */
    public Boolean isLoop() {
        return this.loop;
    }

    /**
     * Set the loop property: The flag indicating whether audio file needs to be played in loop or not.
     *
     * @param loop the loop value to set.
     * @return the PlayAudioRequest object itself.
     */
    public PlayAudioRequest setLoop(Boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the PlayAudioRequest object itself.
     */
    public PlayAudioRequest setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the audioFileId property: An id for the media in the AudioFileUri, using which we cache the media resource.
     *
     * @return the audioFileId value.
     */
    public String getAudioFileId() {
        return this.audioFileId;
    }

    /**
     * Set the audioFileId property: An id for the media in the AudioFileUri, using which we cache the media resource.
     *
     * @param audioFileId the audioFileId value to set.
     * @return the PlayAudioRequest object itself.
     */
    public PlayAudioRequest setAudioFileId(String audioFileId) {
        this.audioFileId = audioFileId;
        return this;
    }

    /**
     * Get the callbackUri property: The callback Uri to receive PlayAudio status notifications.
     *
     * @return the callbackUri value.
     */
    public String getCallbackUri() {
        return this.callbackUri;
    }

    /**
     * Set the callbackUri property: The callback Uri to receive PlayAudio status notifications.
     *
     * @param callbackUri the callbackUri value to set.
     * @return the PlayAudioRequest object itself.
     */
    public PlayAudioRequest setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }
}
