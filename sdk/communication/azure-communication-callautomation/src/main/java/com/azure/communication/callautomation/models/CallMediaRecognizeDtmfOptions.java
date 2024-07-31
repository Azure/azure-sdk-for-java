// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/** The Recognize configurations specific for Dtmf. **/
@Fluent
public final class CallMediaRecognizeDtmfOptions extends CallMediaRecognizeOptions {

    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    private Duration interToneTimeout;

    /*
     * Maximum number of DTMFs to be collected.
     */
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    private List<DtmfTone> stopDtmfTones;

    /**
     * Get the interToneTimeout property: Time to wait between DTMF inputs to stop recognizing.
     * If left unspecified, the default value is set to 2 seconds.
     *
     * @return the interToneTimeout value.
     */
    public Duration getInterToneTimeout() {
        return this.interToneTimeout;
    }

    /**
     * Set the interToneTimeout property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @param interToneTimeout the interToneTimeout value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeDtmfOptions setInterToneTimeout(Duration interToneTimeout) {
        this.interToneTimeout = interToneTimeout;
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
     * Get the stopTones property: List of tones that will stop recognizing.
     *
     * @return the stopTones value.
     */
    public List<DtmfTone> getStopTones() {
        return this.stopDtmfTones;
    }

    /**
     * Set the stopTones property: List of tones that will stop recognizing.
     *
     * @param stopDtmfTones the stopTones value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    public CallMediaRecognizeDtmfOptions setStopTones(List<DtmfTone> stopDtmfTones) {
        this.stopDtmfTones = stopDtmfTones;
        return this;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     *
     * @param targetParticipant Target participant of DTFM tone recognition.
     * @param maxTonesToCollect Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeDtmfOptions(CommunicationIdentifier targetParticipant, int maxTonesToCollect) {
        super(RecognizeInputType.DTMF, targetParticipant);
        this.interToneTimeout = Duration.ofSeconds(2);
        this.maxTonesToCollect = maxTonesToCollect;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        // write properties of base class.
        jsonWriter.writeStringField("recognizeInputType", "dtmf");
        jsonWriter.writeJsonField("playPrompt", getPlayPrompt());
        jsonWriter.writeArrayField("playPrompts", this.getPlayPrompts(), (writer, playPrompt) -> playPrompt.toJson(writer));
        jsonWriter.writeBooleanField("interruptCallMediaOperation", isInterruptCallMediaOperation());
        jsonWriter.writeBooleanField("stopCurrentOperations", isStopCurrentOperations());
        jsonWriter.writeStringField("operationContext", getOperationContext());
        jsonWriter.writeBooleanField("interruptPrompt", isInterruptPrompt());
        jsonWriter.writeStringField("initialSilenceTimeout", CoreUtils.durationToStringWithDays(getInitialSilenceTimeout()));
        jsonWriter.writeStringField("speechModelEndpointId", getSpeechModelEndpointId());
        final CommunicationIdentifierModel participant = CommunicationIdentifierConverter.convert(getTargetParticipant());
        jsonWriter.writeJsonField("targetParticipant", participant);
        jsonWriter.writeStringField("operationCallbackUrl", getOperationCallbackUrl());
        // write properties specific to this class.
        jsonWriter.writeStringField("interToneTimeout", CoreUtils.durationToStringWithDays(this.interToneTimeout));
        if (this.maxTonesToCollect != null) {
            jsonWriter.writeNumberField("maxTonesToCollect", this.maxTonesToCollect);
        }
        jsonWriter.writeArrayField("stopTones", this.stopDtmfTones, (writer, element) -> writer.writeString(element.toString()));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallMediaRecognizeDtmfOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallMediaRecognizeDtmfOptions if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallMediaRecognizeDtmfOptions.
     */
    public static CallMediaRecognizeDtmfOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // variables to hold values of properties specific to this class.
            Duration interToneTimeout = null;
            int maxTonesToCollect = 0;
            List<DtmfTone> stopDtmfTones = null;
            // variables to hold values of properties of base class.
            String recognizeInputType = null;
            Boolean interruptCallMediaOperation = null;
            Boolean stopCurrentOperations = null;
            String operationContext = null;
            Boolean interruptPrompt = null;
            Duration initialSilenceTimeout = null;
            String speechModelEndpointId = null;
            String operationCallbackUrl = null;
            CommunicationIdentifier targetParticipant = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("interToneTimeout".equals(fieldName)) {
                    final String value = reader.getString();
                    interToneTimeout = value != null ? Duration.parse(value) : null;
                } else if ("maxTonesToCollect".equals(fieldName)) {
                    final Integer value = reader.getNullable(JsonReader::getInt);
                    maxTonesToCollect = value != null ? value : 0;
                } else if ("stopTones".equals(fieldName)) {
                    stopDtmfTones = reader.readArray(r -> DtmfTone.fromString(r.getString()));
                } else if ("recognizeInputType".equals(fieldName)) {
                    recognizeInputType = reader.getString();
                } else if ("interruptCallMediaOperation".equals(fieldName)) {
                    interruptCallMediaOperation = reader.getNullable(JsonReader::getBoolean);
                } else if ("stopCurrentOperations".equals(fieldName)) {
                    stopCurrentOperations = reader.getNullable(JsonReader::getBoolean);
                } else if ("operationContext".equals(fieldName)) {
                    operationContext = reader.getString();
                } else if ("interruptPrompt".equals(fieldName)) {
                    interruptPrompt = reader.getNullable(JsonReader::getBoolean);
                } else if ("initialSilenceTimeout".equals(fieldName)) {
                    final String value = reader.getString();
                    initialSilenceTimeout = value != null ? Duration.parse(value) : null;
                } else if ("speechModelEndpointId".equals(fieldName)) {
                    speechModelEndpointId = reader.getString();
                } else if ("operationCallbackUrl".equals(fieldName)) {
                    operationCallbackUrl = reader.getString();
                } else if ("targetParticipant".equals(fieldName)) {
                    final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    targetParticipant = CommunicationIdentifierConverter.convert(inner);
                } else {
                    reader.skipChildren();
                }
            }
            final CallMediaRecognizeDtmfOptions options = new CallMediaRecognizeDtmfOptions(targetParticipant, maxTonesToCollect);
            options.interToneTimeout = interToneTimeout;
            options.stopDtmfTones = stopDtmfTones;
            // set properties of base class.
            options.setRecognizeInputType(RecognizeInputType.fromString(recognizeInputType));
            options.setInterruptCallMediaOperation(interruptCallMediaOperation);
            options.setStopCurrentOperations(stopCurrentOperations);
            options.setOperationContext(operationContext);
            options.setInterruptPrompt(interruptPrompt);
            options.setInitialSilenceTimeout(initialSilenceTimeout);
            options.setSpeechModelEndpointId(speechModelEndpointId);
            options.setOperationCallbackUrl(operationCallbackUrl);

            return options;
        });
    }
}
