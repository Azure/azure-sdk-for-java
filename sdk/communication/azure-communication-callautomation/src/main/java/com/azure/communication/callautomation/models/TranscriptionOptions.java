// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TranscriptionConfigurationInternal model. */
@Fluent
public final class TranscriptionOptions {
    /*
     * Transport URL for live transcription
     */
    @JsonProperty(value = "transportUrl", required = true)
    private final String transportUrl;

    /*
     * The type of transport to be used for live transcription, eg. Websocket
     */
    @JsonProperty(value = "transportType", required = true)
    private final TranscriptionTransportType transportType;

    /*
     * Defines the locale for the data e.g en-CA, en-AU
     */
    @JsonProperty(value = "locale", required = true)
    private final String locale;

    /*
     * Endpoint where the custom model was deployed.
     */
    @JsonProperty(value = "speechRecognitionModelEndpointId")
    private String speechRecognitionModelEndpointId;

    /*
     * Determines if the transcription should be started immediately after call is answered or not.
     */
    @JsonProperty(value = "startTranscription", required = true)
    private final boolean startTranscription;

    /*
     * Enables intermediate results for the transcribed speech.
     */
    @JsonProperty(value = "enableIntermediateResults")
    private Boolean enableIntermediateResults;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param locale - Locale
     * @param startTranscription - Start Transcription
     */
    public TranscriptionOptions(String transportUrl, TranscriptionTransportType transportType, String locale, boolean startTranscription) {
        this.transportUrl = transportUrl;
        this.transportType = transportType;
        this.locale = locale;
        this.startTranscription = startTranscription;
    }

    /**
     * Get the transportUrl property: Transport URL for live transcription.
     *
     * @return the transportUrl value.
     */
    public String getTransportUrl() {
        return this.transportUrl;
    }

    /**
     * Get the transportType property: The type of transport to be used for live transcription, eg. Websocket.
     *
     * @return the transportType value.
     */
    public TranscriptionTransportType getTransportType() {
        return this.transportType;
    }

    /**
     * Get the locale property: locale for the data e.g en-CA, en-AU.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Get the startTranscription property: Which determines if the transcription should be started immediately after call is answered or not.
     *
     * @return the startTranscription value.
     */
    public boolean getStartTranscription() {
        return this.startTranscription;
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
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Get the enableIntermediateResults property: Enables intermediate results for the transcribed speech.
     * 
     * @return the enableIntermediateResults value.
     */
    public Boolean isEnableIntermediateResults() {
        return this.enableIntermediateResults;
    }

    /**
     * Set the enableIntermediateResults property: Enables intermediate results for the transcribed speech.
     * 
     * @param enableIntermediateResults the enableIntermediateResults value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setEnableIntermediateResults(Boolean enableIntermediateResults) {
        this.enableIntermediateResults = enableIntermediateResults;
        return this;
    }
}
