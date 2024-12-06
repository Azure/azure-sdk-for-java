// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.databind.ObjectMapper;

/** The PlaySource model. */
@Fluent
public class OutStreamingData {

    /**
     * Out streaming data kind ex. StopAudio, AudioData
     */
    private final MediaKind kind;

    /**
     * The audio data
     */
    private AudioData audioData;

    /**
     * The stop audio
     */
    private StopAudio stopAudio;

    /**
     * Constructor
     * 
     * @param kind media kind type on the out streaming data
     */
    public OutStreamingData(MediaKind kind) {
        this.kind = kind;
    }

    /**
    * Get the out streaming media kind.
    *
    * @return the MediaKind
    */
    MediaKind getMediaKind() {
        return kind;
    }

    /**
     * Get the out streaming Audio Data.
     *
     * @return the audioData
     */
    AudioData getAudioData() {
        return audioData;
    }

    /**
    * Set the out streaming Audio Data.
    *
    * @param audioData the audioData to set
    * @return the OutStreamingData object itself.
    */
    OutStreamingData setAudioData(byte[] audioData) {
        this.audioData = new AudioData(audioData);
        return this;
    }

    /**
     * Set the out streaming stop Audio.
     *
     * @return the OutStreamingData object itself.
     */
    OutStreamingData setStopAudio() {
        this.stopAudio = new StopAudio();
        return this;
    }

    /**
     * Get the streaming data for outbound
     * @param audioData the audioData to set
     * @return the string of outstreaming data
     * 
     */
    public static String getStreamingDataForOutbound(byte[] audioData) {
        OutStreamingData data = new OutStreamingData(MediaKind.AUDIO_DATA);
        data.setAudioData(audioData);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String serializedData = objectMapper.writeValueAsString(data);
            return serializedData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the stop audiofor outbound
     * @return the string of outstreaming data
     * 
     */
    public static String getStopAudioForOutbound() {
        OutStreamingData data = new OutStreamingData(MediaKind.STOP_AUDIO);
        data.setStopAudio();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String serializedData = objectMapper.writeValueAsString(data);
            return serializedData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
