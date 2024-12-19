// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Base64;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;

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
     * @throws IOException when failed to serilize the data
     * 
     */
    public static String getStreamingDataForOutbound(byte[] audioData) throws IOException {
        OutStreamingData data = new OutStreamingData(MediaKind.AUDIO_DATA);
        data.setAudioData(audioData);
        return serializeOutStreamingData(data);
    }

    /**
     * Get the stop audiofor outbound
     * @return the string of outstreaming data
     * @throws IOException throws exception when failed to serialize
     * 
     */
    public static String getStopAudioForOutbound() throws IOException {
        OutStreamingData data = new OutStreamingData(MediaKind.STOP_AUDIO);
        data.setStopAudio();
        return serializeOutStreamingData(data);
    }

    /**
     * serilize the outstreaming data
     * @param data Outstreaming data
     * @return string
     * @throws IOException throws exception when fails to serialize
     */
    private static String serializeOutStreamingData(OutStreamingData data) throws IOException {
        Writer json = new StringWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
            // JsonWriter automatically flushes on close.
            data.toJson(jsonWriter);
        }

        return json.toString();
    }

    /**
     * convert to json
     * @param jsonWriter json writer 
     * @return return json writeer
     * @throws IOException hrows exception when fails to serialize
     */
    private JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("kind", this.kind.toString());

        if (this.audioData != null) {
            jsonWriter.writeFieldName("audioData");
            jsonWriter.writeStartObject();
            jsonWriter.writeRawField("data", Base64.getEncoder().encodeToString(this.audioData.getData()));
            jsonWriter.writeNullField("timestamp");
            jsonWriter.writeNullField("participant");
            jsonWriter.writeBooleanField("isSilent", this.audioData.isSilent());
            jsonWriter.writeEndObject();
        }

        if (this.stopAudio != null) {
            jsonWriter.writeRawField("stopAudio", "{}");
        } else {
            jsonWriter.writeNullField("stopAudio");
        }

        jsonWriter.writeEndObject();
        return jsonWriter;
    }
}
