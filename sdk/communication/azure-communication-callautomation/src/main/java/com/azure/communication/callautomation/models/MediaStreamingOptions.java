// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The MediaStreamingConfigurationInternal model. */
@Fluent
public final class MediaStreamingOptions implements JsonSerializable<MediaStreamingOptions> {
    /*
     * Transport URL for media streaming
     */
    private final String transportUrl;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    private final MediaStreamingTransport transportType;

    /*
     * Content type to stream, eg. audio, audio/video
     */
    private final MediaStreamingContent contentType;

    /*
     * Audio channel type to stream, eg. unmixed audio, mixed audio
     */
    private final MediaStreamingAudioChannel audioChannelType;

    /*
     * The type of transport to be used for media streaming, eg. Websocket
     */
    private final Boolean startMediaStreaming;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param contentType - Content Type
     * @param audioChannelType - Audio Channel Type
     * @param startMediaStreaming - Start media streaming flag
     */
    public MediaStreamingOptions(String transportUrl, MediaStreamingTransport transportType, MediaStreamingContent contentType, MediaStreamingAudioChannel audioChannelType, Boolean startMediaStreaming) {
        this.transportUrl = transportUrl;
        this.transportType = transportType;
        this.contentType = contentType;
        this.audioChannelType = audioChannelType;
        this.startMediaStreaming = startMediaStreaming;
    }

    /**
     * Get the transportUrl property: Transport URL for media streaming., 
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
    public MediaStreamingContent getContentType() {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("transportUrl", transportUrl);
        jsonWriter.writeStringField("transportType", transportType != null ? transportType.toString() : null);
        jsonWriter.writeStringField("contentType", contentType != null ? contentType.toString() : null);
        jsonWriter.writeStringField("audioChannelType", audioChannelType != null ? audioChannelType.toString() : null);
        jsonWriter.writeBooleanField("startMediaStreaming", startMediaStreaming);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MediaStreamingOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MediaStreamingOptions if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MediaStreamingOptions.
     */
    public static MediaStreamingOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String transportUrl = null;
            MediaStreamingTransport transportType = null;
            MediaStreamingContent contentType = null;
            MediaStreamingAudioChannel audioChannelType = null;
            boolean startMediaStreaming = false;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transportUrl".equals(fieldName)) {
                    transportUrl = reader.getString();
                } else if ("transportType".equals(fieldName)) {
                    transportType = MediaStreamingTransport.fromString(reader.getString());
                } else if ("contentType".equals(fieldName)) {
                    contentType = MediaStreamingContent.fromString(reader.getString());
                } else if ("audioChannelType".equals(fieldName)) {
                    audioChannelType = MediaStreamingAudioChannel.fromString(reader.getString());
                } else if ("startMediaStreaming".equals(fieldName)) {
                    startMediaStreaming = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }
            return new MediaStreamingOptions(transportUrl, transportType, contentType, audioChannelType, startMediaStreaming);
        });
    }
}
