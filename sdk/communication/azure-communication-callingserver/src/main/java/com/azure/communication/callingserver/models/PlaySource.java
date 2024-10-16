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

/** The PlaySource model. */
@Fluent
public abstract class PlaySource {
    /*
     * Defines the identifier to be used for caching related media
     */
    @JsonProperty(value = "playSourceId")
    private String playSourceId;

    /**
     * Get the playSourceId property: Defines the identifier to be used for caching related media.
     *
     * @return the playSourceId value.
     */
    public String getPlaySourceId() {
        return this.playSourceId;
    }

    /**
     * Set the playSourceId property: Defines the identifier to be used for caching related media.
     *
     * @param playSourceId the playSourceId value to set.
     * @return the PlaySourceInternal object itself.
     */
    public PlaySource setPlaySourceId(String playSourceId) {
        this.playSourceId = playSourceId;
        return this;
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
