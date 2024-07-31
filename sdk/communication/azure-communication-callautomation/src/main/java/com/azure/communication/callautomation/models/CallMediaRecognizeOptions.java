// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/** Options to configure the Recognize operation **/
@Fluent
public abstract class CallMediaRecognizeOptions implements JsonSerializable<CallMediaRecognizeOptions> {
    /*
     * Determines the type of the recognition.
     */
    private RecognizeInputType recognizeInputType;

    /*
     * The source of the audio to be played for recognition.
     */
    private PlaySource playPrompt;

    /*
     * The playPrompts property.
     */
    private List<PlaySource> playPrompts;

    /*
     * If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     */
    private Boolean interruptCallMediaOperation;

    /*
     * If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     */
    private Boolean stopCurrentOperations;

    /*
     * The value to identify context of the operation.
     */
    private String operationContext;

    /*
     * Determines if we interrupt the prompt and start recognizing.
     */
    private Boolean interruptPrompt;

    /*
     * Time to wait for first input after prompt (if any).
     */
    private Duration initialSilenceTimeout;

    /*
     * Endpoint where the custom model was deployed.
     */
    private String speechModelEndpointId;

    /*
     * Target participant of DTMF tone recognition.
     */
    @SuppressWarnings("FieldMayBeFinal")
    private CommunicationIdentifier targetParticipant;

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     */
    private String operationCallbackUrl;

    /**
     * Initializes a CallMediaRecognizeOptions object.
     * @param recognizeInputType What input the operation should recognize.
     * @param targetParticipant Target participant of DTFM tone recognition.
     */
    public CallMediaRecognizeOptions(RecognizeInputType recognizeInputType, CommunicationIdentifier targetParticipant) {
        this.recognizeInputType = recognizeInputType;
        this.targetParticipant = targetParticipant;
        this.initialSilenceTimeout = Duration.ofSeconds(5);
    }

    /**
     * Get the recognizeInputType property: Determines the type of the recognition.
     *
     * @return the recognizeInputType value.
     */
    public RecognizeInputType getRecognizeInputType() {
        return this.recognizeInputType;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        this.recognizeInputType = recognizeInputType;
        return this;
    }

    /**
     * Get the playPrompt property: The source of the audio to be played for recognition.
     *
     * @return the playPrompt value.
     */
    public PlaySource getPlayPrompt() {
        return this.playPrompt;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setPlayPrompt(PlaySource playPrompt) {
        this.playPrompt = playPrompt;
        return this;
    }

    /**
     * Get the playPrompts property: The list source of the audio to be played for recognition.
     *
     * @return the playPrompts value.
     */
    public List<PlaySource> getPlayPrompts() {
        return this.playPrompts;
    }

    /**
     * Set the playPrompts property: The list source of the audio to be played for recognition.
     *
     * @param playPrompts the playPrompts value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setPlayPrompts(List<PlaySource> playPrompts) {
        this.playPrompts = playPrompts;
        return this;
    }

    /**
     * Get the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @return the interruptCallMediaOperation value.
     */
    public Boolean isInterruptCallMediaOperation() {
        return this.interruptCallMediaOperation;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        this.interruptCallMediaOperation = interruptCallMediaOperation;
        return this;
    }

    /**
     * Get the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @return the stopCurrentOperations value.
     */
    public Boolean isStopCurrentOperations() {
        return this.stopCurrentOperations;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        this.stopCurrentOperations = stopCurrentOperations;
        return this;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @return the interruptPrompt value.
     */
    public Boolean isInterruptPrompt() {
        return this.interruptPrompt;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        this.interruptPrompt = interruptPrompt;
        return this;
    }

    /**
     * Get the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @return the initialSilenceTimeout value.
     */
    public Duration getInitialSilenceTimeout() {
        return this.initialSilenceTimeout;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        this.initialSilenceTimeout = initialSilenceTimeout;
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
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the CallMediaRecognizeOptions object itself.
     */
    public CallMediaRecognizeOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }

     /**
     * Get the speech model endpoint id.
     *
     * @return the speech model endpoint id.
     */
    public String getSpeechModelEndpointId() {
        return speechModelEndpointId;
    }
    /**
     * Set the speechModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @param speechModelEndpointId the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeSpeechOrDtmfOptions object itself.
     */
    public CallMediaRecognizeOptions setSpeechModelEndpointId(String speechModelEndpointId) {
        this.speechModelEndpointId = speechModelEndpointId;
        return this;
    }

    /**
     * Reads an instance of CallMediaRecognizeOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallMediaRecognizeOptions if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ExternalStorage.
     */
    public static CallMediaRecognizeOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // The discriminator value to identity the actual type.
            String recognizeInputType = null;
            final JsonReader reader1 = reader.bufferObject();
            reader1.nextToken();
            while (reader1.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader1.getFieldName();
                reader1.nextToken();
                if ("recognizeInputType".equals(fieldName)) {
                    recognizeInputType = reader1.getString();
                } else {
                    reader1.skipChildren();
                }
            }
            CallMediaRecognizeOptions options = null;
            if ("dtmf".equals(recognizeInputType)) {
                options = CallMediaRecognizeDtmfOptions.fromJson(reader1.reset());
            } else if ("choices".equals(recognizeInputType)) {
                options = CallMediaRecognizeChoiceOptions.fromJson(reader1.reset());
            } else if ("speech".equals(recognizeInputType)) {
                options = CallMediaRecognizeSpeechOptions.fromJson(reader1.reset());
            } else if ("speechordtmf".equals(recognizeInputType)) {
                options = CallMediaRecognizeSpeechOrDtmfOptions.fromJson(reader1.reset());
            }
            return options;
        });
    }
}
