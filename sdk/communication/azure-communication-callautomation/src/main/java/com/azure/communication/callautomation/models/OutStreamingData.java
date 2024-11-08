package com.azure.communication.callautomation.models;

public class OutStreamingData {

    /**
     * Out streaming data kind ex. StopAudio, AudioData
     */
    private final MediaKind kind;

    /**
     * The operational context
     */
    private AudioData audioData;

    /**
     * The operational context
     */
    private StopAudio stopAudio;

    public OutStreamingData(MediaKind kind)
    {
        this.kind = kind;

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
     * @param audioData the stopAudio to set
     * @return the OutStreamingData object itself.
     */
    public OutStreamingData setStopAudio(StopAudio stopAudio) {
        this.stopAudio = stopAudio;
        return this;
    }
}
