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
    public OutStreamingData setAudioData(AudioData audioData) {
        this.audioData = audioData;
        return this;
    }

     /**
     * Get the out streaming Stop Audio.
     *
     * @return the stopAudio
     */
    public StopAudio getStopAudio() {
        return stopAudio;
    }

    /**
     * Set the out streaming stop Audio.
     *
     * @param stopAudio the stopAudio to set
     * @return the OutStreamingData object itself.
     */
    public OutStreamingData setStopAudio(StopAudio stopAudio) {
        this.stopAudio = stopAudio;
        return this;
    }
}
