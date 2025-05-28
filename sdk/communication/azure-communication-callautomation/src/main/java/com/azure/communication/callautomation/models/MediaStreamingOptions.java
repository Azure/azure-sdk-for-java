// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/** The MediaStreamingOptions model. */
@Fluent
public final class MediaStreamingOptions {
    /*
     * Transport URL for media streaming
     */
    private String transportUrl;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    private final StreamingTransport transportType;

    /*
     * Content type to stream, eg. audio, audio/video
     */
    private MediaStreamingContentType contentType;

    /*
     * Audio channel type to stream, eg. unmixed audio, mixed audio
     */
    private final MediaStreamingAudioChannel audioChannelType;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    private Boolean startMediaStreaming;

    /*
     * A value indicating whether bidirectional streaming is enabled.
     */
    private Boolean enableBidirectional;

    /*
     * Specifies the audio format used for encoding, including sample rate and channel type.
     */
    private AudioFormat audioFormat;

    /*
     * A value that indicates whether to stream the DTMF tones.
     */
    private Boolean enableDtmfTones;

    /**
     * Creates a new instance of MediaStreamingOptions
     * @param audioChannelType - Audio Channel Type
     * @param transportType - The type of transport to be used for media streaming, eg. Websocket
     */
    public MediaStreamingOptions(MediaStreamingAudioChannel audioChannelType, StreamingTransport transportType) {
        this.transportType = StreamingTransport.WEBSOCKET;
        this.contentType = MediaStreamingContentType.AUDIO;
        this.audioChannelType = audioChannelType;
        this.startMediaStreaming = false;
    }

    /**
     * Creates a new instance of TranscriptionOptions with default transportType as WEBSOCKET.
     * @param audioChannelType - Audio Channel Type
     */
    public MediaStreamingOptions(MediaStreamingAudioChannel audioChannelType) {
        this(audioChannelType, StreamingTransport.WEBSOCKET);
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
     * Set the transportUrl property: Transport URL for media streaming.
     *
     * @param transportUrl the transportUrl value to set.
     * @return the MediaStreamingOptions object itself.
     */
    public MediaStreamingOptions setTransportUrl(String transportUrl) {
        this.transportUrl = transportUrl;
        return this;
    }

    /**
     * Get the transportType property: The type of tranport to be used for media streaming, eg. Websocket.
     *
     * @return the transportType value.
     */
    public StreamingTransport getTransportType() {
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
    * Get the startMediaStreaming property: Enables intermediate results for the transcribed speech.
    *
    * @return the startMediaStreaming value.
    */
    public Boolean isStartMediaStreamingEnabled() {
        return this.startMediaStreaming;
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
    * Set the contentType property: The contentType property.
    *
    * @param contentType the contentType value to set.
    * @return the MediaStreamingOptions object itself.
    */
    public MediaStreamingOptions setContentType(MediaStreamingContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the startMediaStreaming property: A value indicating whether the media streaming should start immediately
     * after the call is answered.
     *
     * @return the startMediaStreaming value.
     */
    public Boolean isStartMediaStreaming() {
        return this.startMediaStreaming;
    }

    /**
     * Set the startMediaStreaming property: A value indicating whether the media streaming should start immediately
     * after the call is answered.
     *
     * @param startMediaStreaming the startMediaStreaming value to set.
     * @return the MediaStreamingOptions object itself.
     */
    public MediaStreamingOptions setStartMediaStreaming(Boolean startMediaStreaming) {
        this.startMediaStreaming = startMediaStreaming;
        return this;
    }

    /**
     * Get the enableDtmfTones property: A value that indicates whether to stream the DTMF tones.
     *
     * @return the enableDtmfTones value.
     */
    public Boolean isEnableDtmfTones() {
        return this.enableDtmfTones;
    }

    /**
     * Set the enableDtmfTones property: A value that indicates whether to stream the DTMF tones.
     *
     * @param enableDtmfTones the enableDtmfTones value to set.
     * @return the MediaStreamingOptions object itself.
     */
    public MediaStreamingOptions setEnableDtmfTones(Boolean enableDtmfTones) {
        this.enableDtmfTones = enableDtmfTones;
        return this;
    }

    /**
    * Get the enableBidirectional property: A value indicating whether bidirectional streaming is enabled.
    *
    * @return the enableBidirectional value.
    */
    public Boolean isEnableBidirectional() {
        return this.enableBidirectional;
    }

    /**
     * Set the enableBidirectional property: A value indicating whether bidirectional streaming is enabled.
     *
     * @param enableBidirectional the enableBidirectional value to set.
     * @return the MediaStreamingOptions object itself.
     */
    public MediaStreamingOptions setEnableBidirectional(Boolean enableBidirectional) {
        this.enableBidirectional = enableBidirectional;
        return this;
    }

    /**
     * Get the audioFormat property: Specifies the audio format used for encoding, including sample rate and channel
     * type.
     *
     * @return the audioFormat value.
     */
    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    /**
     * Set the audioFormat property: Specifies the audio format used for encoding, including sample rate and channel
     * type.
     *
     * @param audioFormat the audioFormat value to set.
     * @return the MediaStreamingOptions object itself.
     */
    public MediaStreamingOptions setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }
}
