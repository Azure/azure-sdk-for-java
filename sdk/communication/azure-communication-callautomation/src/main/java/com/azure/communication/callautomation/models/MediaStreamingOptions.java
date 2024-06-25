// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingConfigurationInternal model. */
@Fluent
public final class MediaStreamingOptions {
    /*
     * Transport URL for media streaming
     */
    @JsonProperty(value = "transportUrl")
    private final String transportUrl;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    @JsonProperty(value = "transportType")
    private final MediaStreamingTransport transportType;

    /*
     * Content type to stream, eg. audio, audio/video
     */
    @JsonProperty(value = "contentType")
    private final MediaStreamingContentType contentType;

    /*
     * Audio channel type to stream, eg. unmixed audio, mixed audio
     */
    @JsonProperty(value = "audioChannelType")
    private final MediaStreamingAudioChannel audioChannelType;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    @JsonProperty(value = "startMediaStreaming")
    private Boolean startMediaStreaming;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param contentType - Content Type
     * @param audioChannelType - Audio Channel Type
     * @param startMediaStreaming - Start media streaming
     */
    public MediaStreamingOptions(String transportUrl, MediaStreamingTransport transportType, MediaStreamingContentType contentType, MediaStreamingAudioChannel audioChannelType, Boolean startMediaStreaming) {
        this.transportUrl = transportUrl;
        this.transportType = transportType;
        this.contentType = contentType;
        this.audioChannelType = audioChannelType;
        this.startMediaStreaming = startMediaStreaming;
    }

    /**
     * Get the transportUrl property: Transport URL for media streaming.
     *
     * @return the transportUrl value.
     */
    public String getTransportUrl() {
        return this.transportUrl;
    }

    /**
     * Get the transportType property: The type of tranport to be used for media streaming, eg. Websocket.
     *
     * @return the transportType value.
     */
    public MediaStreamingTransport getTransportType() {
        return this.transportType;
    }

    /**
     * Get the contentType property: Content type to stream, eg. audio, audio/video.
     *
     * @return the contentType value.
     */
    public MediaStreamingContentType getContentType() {
        return this.contentType;
    }

    /**
     * Get the audioChannelType property: Audio channel type to stream, eg. unmixed audio, mixed audio.
     *
     * @return the audioChannelType value.
     */
    public MediaStreamingAudioChannel getAudioChannelType() {
        return this.audioChannelType;
    }

     /**
     * Get the startMediaStreaming property: Enables intermediate results for the transcribed speech.
     * 
     * @return the startMediaStreaming value.
     */
    public Boolean isStartMediaStreamingEnabled() {
        return this.startMediaStreaming;
    }

    /**
     * Set the startMediaStreaming property: Determines if the media streaming should be started immediately after call
     * is answered or not.
     * 
     * @param startMediaStreaming the startMediaStreaming value to set.
     * @return the MediaStreamingOptionsInternal object itself.
     */
    public MediaStreamingOptions setStartMediaStreaming(Boolean startMediaStreaming) {
        this.startMediaStreaming = startMediaStreaming;
        return this;
    }
}
