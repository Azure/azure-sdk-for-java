// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;
import com.azure.core.annotation.Fluent;

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
    public MediaKind getKind() {
        return kind;
    }

    /**
     * Get the out streaming Audio Data.
     *
     * @return the audioData
     */
    public AudioData getAudioData() {
        return audioData;
    }

     /**
     * Set the out streaming Audio Data.
     *
     * @param audioData the audioData to set
     * @return the OutStreamingData object itself.
     */
    public OutStreamingData setAudioData(byte[] audioData) {
        this.audioData = new AudioData(audioData);
        return this;
    }

    /**
     * Set the out streaming stop Audio.
     *
     * @return the OutStreamingData object itself.
     */
    public OutStreamingData setStopAudio() {
        this.stopAudio = new StopAudio();
        return this;
    }
}
