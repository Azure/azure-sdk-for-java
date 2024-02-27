// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

/** The Recognize configurations specific for Continuous Speech or DTMF Recognition. **/
@Fluent
public class CallMediaRecognizeSpeechOrDtmfOptions extends CallMediaRecognizeOptions {
    /*
     * The length of end silence when user stops speaking and cogservice send
     * response.
     */
    @JsonProperty(value = "endSilenceTimeout")
    private Duration endSilenceTimeout;

    /*
     * Speech language to be recognized, If not set default is en-US
     */
    @JsonProperty(value = "speechLanguage")
    private String speechLanguage;

    /*
     * Endpoint where the custom model was deployed.
     */
    @JsonProperty(value = "speechRecognitionModelEndpointId")
    private String speechRecognitionModelEndpointId;

    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    @JsonProperty(value = "interToneTimeout")
    private Duration interToneTimeout;

    /*
     * Maximum number of DTMFs to be collected.
     */
    @JsonProperty(value = "maxTonesToCollect")
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    @JsonProperty(value = "stopTones")
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
}
