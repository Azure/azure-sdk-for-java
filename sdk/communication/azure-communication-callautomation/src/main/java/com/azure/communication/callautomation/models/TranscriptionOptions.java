// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/** The TranscriptionOptions model. */
@Fluent
public final class TranscriptionOptions {
    /*
     * The type of transport to be used for live transcription, eg. Websocket
     */
    private final StreamingTransport transportType;

    /*
     * Defines the locale for the data e.g en-CA, en-AU
     */
    private final String locale;

    /*
     * Determines if the transcription should be started immediately after call is answered or not.
     */
    private boolean startTranscription;

    /*
     * Endpoint where the custom model was deployed.
     */
    private String speechRecognitionModelEndpointId;

    /*
     * Enables intermediate results for the transcribed speech.
     */
    private Boolean enableIntermediateResults;

    /*
     * Transport URL for live transcription
     */
    private String transportUrl;

    /**
     * Creates a new instance of TranscriptionOptions
     * @param locale - Locale
     * @param transportType - The type of transport to be used for live transcription
     */
    public TranscriptionOptions(String locale, StreamingTransport transportType) {
        this.transportType = transportType;
        this.locale = locale;
        this.startTranscription = false;
    }

    /**
     * Creates a new instance of TranscriptionOptions with default transportType as WEBSOCKET.
     * @param locale - Locale
     */
    public TranscriptionOptions(String locale) {
        this(locale, StreamingTransport.WEBSOCKET);
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
     * Set the transportUrl property: Transport URL for live transcription.
     *
     * @param transportUrl the transportUrl value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setTransportUrl(String transportUrl) {
        this.transportUrl = transportUrl;
        return this;
    }

    /**
     * Get the transportType property: The type of transport to be used for live transcription, eg. Websocket.
     *
     * @return the transportType value.
     */
    public StreamingTransport getTransportType() {
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
     * Get the startTranscription property: Indicates whether the transcription should start immediately after the call
     * is answered.
     *
     * @return the startTranscription value.
     */
    public Boolean isStartTranscription() {
        return this.startTranscription;
    }

    /**
     * Set the startTranscription property: Indicates whether the transcription should start immediately after the call
     * is answered.
     *
     * @param startTranscription the startTranscription value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setStartTranscription(Boolean startTranscription) {
        this.startTranscription = startTranscription;
        return this;
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
    public Boolean isIntermediateResultsEnabled() {
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
