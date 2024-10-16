// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The MediaStreamingConfigurationInternal model. */
@Fluent
public final class MediaStreamingConfiguration {
    /*
     * Transport URL for media streaming
     */
    @JsonProperty(value = "transportUrl")
    private final String transportUrl;

    /*
     * The type of tranport to be used for media streaming, eg. Websocket
     */
    @JsonProperty(value = "transportType")
    private final MediaStreamingTransportType transportType;

    /*
     * Content type to stream, eg. audio, audio/video
     */
    @JsonProperty(value = "contentType")
    private final MediaStreamingContentType contentType;

    /*
     * Audio channel type to stream, eg. unmixed audio, mixed audio
     */
    @JsonProperty(value = "audioChannelType")
    private final MediaStreamingAudioChannelType audioChannelType;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param contentType - Content Type
     * @param audioChannelType - Audio Channel Type
     */
    public MediaStreamingConfiguration(String transportUrl, MediaStreamingTransportType transportType, MediaStreamingContentType contentType, MediaStreamingAudioChannelType audioChannelType) {
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

    /**
     * Reads an instance of {@link AddParticipantsRequestInternal} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link AddParticipantsRequestInternal}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static AddParticipantsRequestInternal fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("sourceCallerId".equals(fieldName)) {
                    request.sourceCallerId = PhoneNumberIdentifierModel.fromJson(reader);
                } else if ("participantsToAdd".equals(fieldName)) {
                    request.participantsToAdd = reader.readArray(CommunicationIdentifierModel::fromJson);
                } else if ("invitationTimeoutInSeconds".equals(fieldName)) {
                    request.invitationTimeoutInSeconds = reader.getNullable(JsonReader::getInt);
                } else if ("operationContext".equals(fieldName)) {
                    request.operationContext = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return request;
        });
    }
}
