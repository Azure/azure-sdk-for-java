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

/** The Recognize configurations specific for Continuous Speech Recognition. **/
@Fluent
public class CallMediaRecognizeSpeechOptions extends CallMediaRecognizeOptions {
    /*
     * The length of end silence when user stops speaking and cogservice send
     * response.
     */
    private Duration endSilenceTimeout;

    /*
     * Speech language to be recognized, If not set default is en-US
     */
    private String speechLanguage;

    /*
     * Endpoint where the custom model was deployed.
     */
    private String speechRecognitionModelEndpointId;

    /**
     * Get the endSilenceTimeout property: The length of end silence when user stops speaking and cogservice send
     * response.
     *
     * @return the endSilenceTimeout value.
     */
    public Duration getEndSilenceTimeout() {
        return this.endSilenceTimeout;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
    }

    /**
     * Set the speech language property.
     * @param speechLanguage the speechLanguage value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    public CallMediaRecognizeSpeechOptions setSpeechLanguage(String speechLanguage) {
        this.speechLanguage = speechLanguage;
        return this;
    }

    /**
     * Get the speech language property.
     *
     * @return the speech language.
     */
    public String getSpeechLanguage() {
        return this.speechLanguage;
    }

    /**
     * Get the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @return the speechRecognitionModelEndpointId value.
     */
    public String getSpeechRecognitionModelEndpointId() {
        return this.speechRecognitionModelEndpointId;
    }

    /**
     * Set the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @param speechRecognitionModelEndpointId the speechRecognitionModelEndpointId value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    public CallMediaRecognizeSpeechOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeSpeechOptions object.
     *
     * @param targetParticipant Target participant of continuous speech recognition.
     * @param endSilenceTimeout the endSilenceTimeout value to set.
     */
    public CallMediaRecognizeSpeechOptions(CommunicationIdentifier targetParticipant, Duration endSilenceTimeout) {
        super(RecognizeInputType.SPEECH, targetParticipant);
        this.endSilenceTimeout = endSilenceTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        // write properties of base class.
        jsonWriter.writeStringField("recognizeInputType", "speech");
        jsonWriter.writeJsonField("playPrompt", getPlayPrompt());
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
        jsonWriter.writeStringField("endSilenceTimeout", CoreUtils.durationToStringWithDays(this.endSilenceTimeout));
        jsonWriter.writeStringField("speechLanguage", speechLanguage);
        jsonWriter.writeStringField("speechRecognitionModelEndpointId", speechRecognitionModelEndpointId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallMediaRecognizeSpeechOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallMediaRecognizeSpeechOptions if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallMediaRecognizeSpeechOptions.
     */
    public static CallMediaRecognizeSpeechOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // variables to hold values of properties specific to this class.
            Duration endSilenceTimeout = null;
            String speechLanguage = null;
            String speechRecognitionModelEndpointId = null;
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
                if ("endSilenceTimeout".equals(fieldName)) {
                    final String value = reader.getString();
                    endSilenceTimeout = value != null ? Duration.parse(value) : null;
                } else if ("speechLanguage".equals(fieldName)) {
                    speechLanguage = reader.getString();
                } else if ("speechRecognitionModelEndpointId".equals(fieldName)) {
                    speechRecognitionModelEndpointId = reader.getString();
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
            final CallMediaRecognizeSpeechOptions options = new CallMediaRecognizeSpeechOptions(targetParticipant, endSilenceTimeout);
            options.speechLanguage = speechLanguage;
            options.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
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
