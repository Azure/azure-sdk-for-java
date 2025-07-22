// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.List;

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

    /*
     * PII redaction configuration options.
     */
    private PiiRedactionOptions piiRedactionOptions;

    /*
     * Indicating if sentiment analysis should be used.
     */
    private Boolean enableSentimentAnalysis;

    /*
     * List of languages for Language Identification.
     */
    private List<String> locales;

    /*
     * Summarization configuration options.
     */
    private SummarizationOptions summarizationOptions;

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

    /**
    * Get the piiRedactionOptions property: PII redaction configuration options.
    * 
    * @return the piiRedactionOptions value.
    */
    public PiiRedactionOptions getPiiRedactionOptions() {
        return this.piiRedactionOptions;
    }

    /**
     * Set the piiRedactionOptions property: PII redaction configuration options.
     * 
     * @param piiRedactionOptions the piiRedactionOptions value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setPiiRedactionOptions(PiiRedactionOptions piiRedactionOptions) {
        this.piiRedactionOptions = piiRedactionOptions;
        return this;
    }

    /**
     * Get the enableSentimentAnalysis property: Indicating if sentiment analysis
     * should be used.
     * 
     * @return the enableSentimentAnalysis value.
     */
    public Boolean isEnableSentimentAnalysis() {
        return this.enableSentimentAnalysis;
    }

    /**
     * Set the enableSentimentAnalysis property: Indicating if sentiment analysis
     * should be used.
     * 
     * @param enableSentimentAnalysis the enableSentimentAnalysis value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setEnableSentimentAnalysis(Boolean enableSentimentAnalysis) {
        this.enableSentimentAnalysis = enableSentimentAnalysis;
        return this;
    }

    /**
     * Get the locales property: List of languages for Language Identification.
     * 
     * @return the locales value.
     */
    public List<String> getLocales() {
        return this.locales;
    }

    /**
     * Set the locales property: List of languages for Language Identification.
     * 
     * @param locales the locales value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setLocales(List<String> locales) {
        this.locales = locales;
        return this;
    }

    /**
     * Get the summarizationOptions property: Summarization configuration options.
     * 
     * @return the summarizationOptions value.
     */
    public SummarizationOptions getSummarizationOptions() {
        return this.summarizationOptions;
    }

    /**
     * Set the summarizationOptions property: Summarization configuration options.
     * 
     * @param summarizationOptions the summarizationOptions value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setSummarizationOptions(SummarizationOptions summarizationOptions) {
        this.summarizationOptions = summarizationOptions;
        return this;
    }
}
