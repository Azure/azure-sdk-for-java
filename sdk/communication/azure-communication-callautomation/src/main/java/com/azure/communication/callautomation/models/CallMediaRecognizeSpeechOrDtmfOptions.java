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

/** The Recognize configurations specific for Continuous Speech or DTMF Recognition. **/
@Fluent
public class CallMediaRecognizeSpeechOrDtmfOptions extends CallMediaRecognizeOptions {
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
    public CallMediaRecognizeSpeechOrDtmfOptions setInterToneTimeout(Duration interToneTimeout) {
        this.interToneTimeout = interToneTimeout;
        return this;
    }

    /**
     * Get the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @return the maxTonesToCollect value.
     */
    public int getMaxTonesToCollect() {
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
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions setStopTones(List<DtmfTone> stopDtmfTones) {
        this.stopDtmfTones = stopDtmfTones;
        return this;
    }

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
     * Set the speech language property.
     * @param speechLanguage the speechLanguage value to set.
     * @return the CallMediaRecognizeSpeechOrDtmfOptions object itself.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions setSpeechLanguage(String speechLanguage) {
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
     * @return the CallMediaRecognizeSpeechOrDtmfOptions object itself.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeSpeechOrDtmfOptions object.
     *
     * @param targetParticipant Target participant of continuous speech recognition.
     * @param maxTonesToCollect Maximum number of DTMF tones to be collected.
     * @param endSilenceTimeout the endSilenceTimeout value to set.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions(CommunicationIdentifier targetParticipant, int maxTonesToCollect, Duration endSilenceTimeout) {
        super(RecognizeInputType.SPEECH_OR_DTMF, targetParticipant);
        this.endSilenceTimeout = endSilenceTimeout;
        this.interToneTimeout = Duration.ofSeconds(2);
        this.maxTonesToCollect = maxTonesToCollect;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        // write properties of base class.
        jsonWriter.writeStringField("recognizeInputType", "speechordtmf");
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
        jsonWriter.writeStringField("endSilenceTimeout", CoreUtils.durationToStringWithDays(endSilenceTimeout));
        jsonWriter.writeStringField("speechLanguage", speechLanguage);
        jsonWriter.writeStringField("speechRecognitionModelEndpointId", speechRecognitionModelEndpointId);
        jsonWriter.writeStringField("interToneTimeout", CoreUtils.durationToStringWithDays(interToneTimeout));
        if (maxTonesToCollect != null) {
            jsonWriter.writeNumberField("maxTonesToCollect", maxTonesToCollect);
        }
        jsonWriter.writeArrayField("stopTones", this.stopDtmfTones, (writer, element) -> writer.writeString(element.toString()));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallMediaRecognizeSpeechOrDtmfOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallMediaRecognizeSpeechOrDtmfOptions if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallMediaRecognizeSpeechOrDtmfOptions.
     */
    public static CallMediaRecognizeSpeechOrDtmfOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // variables to hold values of properties specific to this class.
            Duration endSilenceTimeout = null;
            String speechLanguage = null;
            String speechRecognitionModelEndpointId = null;
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
                if ("endSilenceTimeout".equals(fieldName)) {
                    endSilenceTimeout = Duration.parse(reader.getString());
                } else if ("speechLanguage".equals(fieldName)) {
                    speechLanguage = reader.getString();
                } else if ("speechRecognitionModelEndpointId".equals(fieldName)) {
                    speechRecognitionModelEndpointId = reader.getString();
                } else if ("interToneTimeout".equals(fieldName)) {
                    final String value = reader.getString();
                    interToneTimeout = value != null ? Duration.parse(value) : null;
                } else if ("maxTonesToCollect".equals(fieldName)) {
                    final Integer value = reader.getNullable(JsonReader::getInt);
                    maxTonesToCollect = value != null ? value : 0;
                } else if ("stopTones".equals(fieldName)) {
                    stopDtmfTones = reader.readArray(r -> DtmfTone.fromString(r.getString()));
                } else {
                    reader.skipChildren();
                }
            }
            final CallMediaRecognizeSpeechOrDtmfOptions options = new CallMediaRecognizeSpeechOrDtmfOptions(targetParticipant, maxTonesToCollect, endSilenceTimeout);
            //
            options.speechLanguage = speechLanguage;
            options.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
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
