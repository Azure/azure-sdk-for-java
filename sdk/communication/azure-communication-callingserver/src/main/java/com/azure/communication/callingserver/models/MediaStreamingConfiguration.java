// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/** The MediaStreamingConfigurationInternal model. */
@Fluent
public final class MediaStreamingConfiguration implements JsonSerializable<MediaStreamingConfiguration> {
    /*
     * Transport URL for media streaming
     */
    private final String transportUrl;

    /*
     * The type of tranport to be used for media streaming, eg. Websocket
     */
    private final MediaStreamingTransportType transportType;

    /*
     * Content type to stream, eg. audio, audio/video
     */
    private final MediaStreamingContentType contentType;

    /*
     * Audio channel type to stream, eg. unmixed audio, mixed audio
     */
    private final MediaStreamingAudioChannelType audioChannelType;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param contentType - Content Type
     * @param audioChannelType - Audio Channel Type
     */
    public MediaStreamingConfiguration(String transportUrl, MediaStreamingTransportType transportType,
        MediaStreamingContentType contentType, MediaStreamingAudioChannelType audioChannelType) {
        this.transportUrl = transportUrl;
        this.transportType = transportType;
        this.contentType = contentType;
        this.audioChannelType = audioChannelType;
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
    public MediaStreamingTransportType getTransportType() {
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
    public MediaStreamingAudioChannelType getAudioChannelType() {
        return this.audioChannelType;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("transportUrl", transportUrl)
            .writeStringField("transportType", Objects.toString(transportType, null))
            .writeStringField("contentType", Objects.toString(contentType, null))
            .writeStringField("audioChannelType", Objects.toString(audioChannelType, null))
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link MediaStreamingConfiguration} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link MediaStreamingConfiguration}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static MediaStreamingConfiguration fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String transportUrl = null;
            MediaStreamingTransportType transportType = null;
            MediaStreamingContentType contentType = null;
            MediaStreamingAudioChannelType audioChannelType = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("transportUrl".equals(fieldName)) {
                    transportUrl = reader.getString();
                } else if ("transportType".equals(fieldName)) {
                    transportType = MediaStreamingTransportType.fromString(reader.getString());
                } else if ("contentType".equals(fieldName)) {
                    contentType = MediaStreamingContentType.fromString(reader.getString());
                } else if ("audioChannelType".equals(fieldName)) {
                    audioChannelType = MediaStreamingAudioChannelType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return new MediaStreamingConfiguration(transportUrl, transportType, contentType, audioChannelType);
        });
    }
}
