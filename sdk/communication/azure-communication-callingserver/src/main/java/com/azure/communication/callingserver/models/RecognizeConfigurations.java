// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/** The RecognizeConfigurations model. */
@Fluent
public final class RecognizeConfigurations implements JsonSerializable<RecognizeConfigurations> {
    /*
     * Determines if we interrupt the prompt and start recognizing.
     */
    private Boolean interruptPromptAndStartRecognition;

    /*
     * Time to wait for first input after prompt (if any).
     */
    private Duration initialSilenceTimeoutInSeconds;

    /*
     * Target participant of DTFM tone recognition.
     */
    private CommunicationIdentifier targetParticipant;

    /*
     * Defines configurations for DTMF.
     */
    private DtmfConfigurations dtmfConfigurations;

    /**
     * Creates a new instance of {@link RecognizeConfigurations}.
     */
    public RecognizeConfigurations() {
    }

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
    public RecognizeConfigurations setInterruptPromptAndStartRecognition(Boolean interruptPromptAndStartRecognition) {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("interruptPromptAndStartRecognition", interruptPromptAndStartRecognition)
            .writeStringField("initialSilenceTimeoutInSeconds",
                CoreUtils.durationToStringWithDays(initialSilenceTimeoutInSeconds))
            .writeJsonField("targetParticipant", CommunicationIdentifierConverter.convert(targetParticipant))
            .writeJsonField("dtmfConfigurations", dtmfConfigurations)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link RecognizeConfigurations} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link RecognizeConfigurations}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static RecognizeConfigurations fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RecognizeConfigurations configurations = new RecognizeConfigurations();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("interruptPromptAndStartRecognition".equals(fieldName)) {
                    configurations.interruptPromptAndStartRecognition = reader.getNullable(JsonReader::getBoolean);
                } else if ("initialSilenceTimeoutInSeconds".equals(fieldName)) {
                    configurations.initialSilenceTimeoutInSeconds
                        = reader.getNullable(nonNull -> Duration.parse(nonNull.getString()));
                } else if ("targetParticipant".equals(fieldName)) {
                    configurations.targetParticipant
                        = CommunicationIdentifierConverter.convert(CommunicationIdentifierModel.fromJson(reader));
                } else if ("dtmfConfigurations".equals(fieldName)) {
                    configurations.dtmfConfigurations = DtmfConfigurations.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return configurations;
        });
    }
}
