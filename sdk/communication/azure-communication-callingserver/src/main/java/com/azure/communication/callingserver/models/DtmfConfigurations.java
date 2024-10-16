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
import java.time.Duration;
import java.util.List;

/** Options for DTMF recognition. */
@Fluent
public final class DtmfConfigurations {
    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    @JsonProperty(value = "interToneTimeoutInSeconds")
    private Duration interToneTimeoutInSeconds;

    /*
     * Maximum number of DTMFs to be collected.
     */
    @JsonProperty(value = "maxTonesToCollect")
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    @JsonProperty(value = "stopTones")
    private List<StopTones> stopTones;

    /**
     * Get the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @return the interToneTimeoutInSeconds value.
     */
    public Duration getInterToneTimeoutInSeconds() {
        return this.interToneTimeoutInSeconds;
    }

    /**
     * Set the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @param interToneTimeoutInSeconds the interToneTimeoutInSeconds value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setInterToneTimeoutInSeconds(Duration interToneTimeoutInSeconds) {
        this.interToneTimeoutInSeconds = interToneTimeoutInSeconds;
        return this;
    }

    /**
     * Get the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @return the maxTonesToCollect value.
     */
    public Integer getMaxTonesToCollect() {
        return this.maxTonesToCollect;
    }

    /**
     * Set the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @param maxTonesToCollect the maxTonesToCollect value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setMaxTonesToCollect(Integer maxTonesToCollect) {
        this.maxTonesToCollect = maxTonesToCollect;
        return this;
    }

    /**
     * Get the stopTones property: List of tones that will stop recognizing.
     *
     * @return the stopTones value.
     */
    public List<StopTones> getStopTones() {
        return this.stopTones;
    }

    /**
     * Set the stopTones property: List of tones that will stop recognizing.
     *
     * @param stopTones the stopTones value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setStopTones(List<StopTones> stopTones) {
        this.stopTones = stopTones;
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
