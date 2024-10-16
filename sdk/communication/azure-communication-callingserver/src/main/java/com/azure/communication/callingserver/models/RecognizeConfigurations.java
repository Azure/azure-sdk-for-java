// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.Duration;

/** The RecognizeConfigurations model. */
@Fluent
public final class RecognizeConfigurations {
    /*
     * Determines if we interrupt the prompt and start recognizing.
     */
    @JsonProperty(value = "interruptPromptAndStartRecognition")
    private Boolean interruptPromptAndStartRecognition;

    /*
     * Time to wait for first input after prompt (if any).
     */
    @JsonProperty(value = "initialSilenceTimeoutInSeconds")
    private Duration initialSilenceTimeoutInSeconds;

    /*
     * Target participant of DTFM tone recognition.
     */
    @JsonProperty(value = "targetParticipant")
    private CommunicationIdentifier targetParticipant;

    /*
     * Defines configurations for DTMF.
     */
    @JsonProperty(value = "dtmfConfigurations")
    private DtmfConfigurations dtmfConfigurations;

    /**
     * Get the interruptPromptAndStartRecognition property: Determines if we interrupt the prompt and start recognizing.
     *
     * @return the interruptPromptAndStartRecognition value.
     */
    public Boolean isInterruptPromptAndStartRecognition() {
        return this.interruptPromptAndStartRecognition;
    }

    /**
     * Set the interruptPromptAndStartRecognition property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPromptAndStartRecognition the interruptPromptAndStartRecognition value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public RecognizeConfigurations setInterruptPromptAndStartRecognition(
        Boolean interruptPromptAndStartRecognition) {
        this.interruptPromptAndStartRecognition = interruptPromptAndStartRecognition;
        return this;
    }

    /**
     * Get the initialSilenceTimeoutInSeconds property: Time to wait for first input after prompt (if any).
     *
     * @return the initialSilenceTimeoutInSeconds value.
     */
    public Duration getInitialSilenceTimeoutInSeconds() {
        return this.initialSilenceTimeoutInSeconds;
    }

    /**
     * Set the initialSilenceTimeoutInSeconds property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeoutInSeconds the initialSilenceTimeoutInSeconds value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public RecognizeConfigurations setInitialSilenceTimeoutInSeconds(Duration initialSilenceTimeoutInSeconds) {
        this.initialSilenceTimeoutInSeconds = initialSilenceTimeoutInSeconds;
        return this;
    }

    /**
     * Get the targetParticipant property: Target participant of DTFM tone recognition.
     *
     * @return the targetParticipant value.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return this.targetParticipant;
    }

    /**
     * Set the targetParticipant property: Target participant of DTFM tone recognition.
     *
     * @param targetParticipant the targetParticipant value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public RecognizeConfigurations setTargetParticipant(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        return this;
    }

    /**
     * Get the dtmfConfigurations property: Defines configurations for DTMF.
     *
     * @return the dtmfConfigurations value.
     */
    public DtmfConfigurations getDtmfConfigurations() {
        return this.dtmfConfigurations;
    }

    /**
     * Set the dtmfConfigurations property: Defines configurations for DTMF.
     *
     * @param dtmfConfigurations the dtmfConfigurations value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public RecognizeConfigurations setDtmfConfigurations(DtmfConfigurations dtmfConfigurations) {
        this.dtmfConfigurations = dtmfConfigurations;
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
