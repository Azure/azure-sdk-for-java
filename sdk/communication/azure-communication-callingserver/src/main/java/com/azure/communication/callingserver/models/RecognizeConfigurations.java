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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.Duration;

/** The RecognizeConfigurations model. */
@Fluent
public final class RecognizeConfigurations implements JsonSerializable<RecognizeConfigurations> {
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
     * Creates RecognizeConfigurations.
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
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("interruptPromptAndStartRecognition", this.interruptPromptAndStartRecognition);
        jsonWriter.writeStringField("initialSilenceTimeoutInSeconds", CoreUtils.durationToStringWithDays(initialSilenceTimeoutInSeconds));
        final CommunicationIdentifierModel participant = CommunicationIdentifierConverter.convert(this.targetParticipant);
        // TODO: (anu): Enable this after refreshing the protocol layer.
        // jsonWriter.writeJsonField("targetParticipant", participant);
        jsonWriter.writeJsonField("dtmfConfigurations", dtmfConfigurations);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RecognizeConfigurations from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecognizeConfigurations if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecognizeConfigurations.
     */
    public static RecognizeConfigurations fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognizeConfigurations source = new RecognizeConfigurations();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("interruptPromptAndStartRecognition".equals(fieldName)) {
                    source.interruptPromptAndStartRecognition = reader.getNullable(JsonReader::getBoolean);
                } else if ("initialSilenceTimeoutInSeconds".equals(fieldName)) {
                    final String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        source.initialSilenceTimeoutInSeconds = Duration.parse(value);
                    }
                } else if ("targetParticipant".equals(fieldName)) {
                    source.targetParticipant = null;
                    // TODO: (anu): Enable this after refreshing the protocol layer.
                    // final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    // source.targetParticipant = CommunicationIdentifierConverter.convert(inner);
                } else if ("dtmfConfigurations".equals(fieldName)) {
                    source.dtmfConfigurations = DtmfConfigurations.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return source;
        });
    }
}
