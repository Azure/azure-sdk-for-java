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

/** The Recognize configurations specific for Recognize Choice. **/
@Fluent
public final class CallMediaRecognizeChoiceOptions extends CallMediaRecognizeOptions {
    /*
     * List of recognition choices.
     */
    private final List<RecognitionChoice> choices;

    /*
     * Speech language to be recognized, If not set default is en-US
     */
    private String speechLanguage;

    /*
     * Endpoint where the custom model was deployed.
     */
    private String speechRecognitionModelEndpointId;

    /**
     * Get the list of recognition choices.
     *
     * @return the list of recognition choices.
     */
    public List<RecognitionChoice> getChoices() {
        return this.choices;
    }

    /**
     * Set the speech language property.
     * @param speechLanguage the interToneTimeout value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    public CallMediaRecognizeChoiceOptions setSpeechLanguage(String speechLanguage) {
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
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    public CallMediaRecognizeChoiceOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     *
     * @param targetParticipant Target participant of DTFM tone recognition.
     * @param choices Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeChoiceOptions(CommunicationIdentifier targetParticipant,  List<RecognitionChoice> choices) {
        super(RecognizeInputType.CHOICES, targetParticipant);
        this.choices = choices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        // write properties of base class.
        jsonWriter.writeStringField("recognizeInputType", "choices");
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
        jsonWriter.writeStringField("speechLanguage", this.speechLanguage);
        jsonWriter.writeStringField("speechRecognitionModelEndpointId", this.speechRecognitionModelEndpointId);
        jsonWriter.writeArrayField("choices", this.choices, (writer, choice) -> choice.toJson(writer));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallMediaRecognizeChoiceOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallMediaRecognizeChoiceOptions if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallMediaRecognizeChoiceOptions.
     */
    public static CallMediaRecognizeChoiceOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // variables to hold values of properties specific to this class.
            String speechLanguage = null;
            String speechRecognitionModelEndpointId = null;
            List<RecognitionChoice> choices = null;
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
                if ("speechLanguage".equals(fieldName)) {
                    speechLanguage = reader.getString();
                } else if ("speechRecognitionModelEndpointId".equals(fieldName)) {
                    speechRecognitionModelEndpointId = reader.getString();
                } else if ("choices".equals(fieldName)) {
                    choices = reader.readArray(RecognitionChoice::fromJson);
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
            final CallMediaRecognizeChoiceOptions options = new CallMediaRecognizeChoiceOptions(targetParticipant, choices);
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
