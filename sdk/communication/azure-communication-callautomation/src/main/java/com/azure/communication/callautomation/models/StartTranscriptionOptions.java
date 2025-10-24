// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.List;

import com.azure.core.annotation.Fluent;

/**
 * Options for the Start Transcription operation.
 */
@Fluent
public final class StartTranscriptionOptions {

    /**
     * Defines Locale for the transcription e,g en-US.
     */
    private String locale;

    /**
     * The value to identify context of the operation.
     */
    private String operationContext;

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
     * Creates an instance of {@link StartTranscriptionOptions}.
     */
    public StartTranscriptionOptions() {
    }

    /**
     * Endpoint where the custom model was deployed.
     */
    private String speechRecognitionModelEndpointId;

    /**
     * Get the locale.
     *
     * @return locale.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale the incoming locale
     * @return The StartTranscriptionOptions object.
     */
    public StartTranscriptionOptions setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Get the operation context.
     *
     * @return operation context.
     */
    public String getOperationContext() {
        return operationContext;
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
     * Get the enableSentimentAnalysis property: Indicating if sentiment analysis
     * should be used.
     * 
     * @return the enableSentimentAnalysis value.
     */
    public Boolean isEnableSentimentAnalysis() {
        return this.enableSentimentAnalysis;
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
     * Sets the locales property: List of languages for Language Identification.
     * 
     * @param locales list of locales for Language Identification.
     * @return the locales value.
     */
    public StartTranscriptionOptions setLocales(List<String> locales) {
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
     * Get the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     * 
     * @return the speechRecognitionModelEndpointId value.
     */
    public String getSpeechRecognitionModelEndpointId() {
        return this.speechRecognitionModelEndpointId;
    }

    /**
     * Sets the operation context.
     *
     * @param operationContext Operation Context
     * @return The StartTranscriptionOptions object.
     */
    public StartTranscriptionOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     * 
     * @param speechRecognitionModelEndpointId the speechRecognitionModelEndpointId value to set.
     * @return the StartTranscriptionOptions object itself.
     */
    public StartTranscriptionOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Set the piiRedactionOptions property: PII redaction configuration options.
     * 
     * @param piiRedactionOptions the piiRedactionOptions value to set.
     * @return the StartTranscriptionOptions object itself.
     */
    public StartTranscriptionOptions setPiiRedactionOptions(PiiRedactionOptions piiRedactionOptions) {
        this.piiRedactionOptions = piiRedactionOptions;
        return this;
    }

    /**
     * Set the enableSentimentAnalysis property: Indicating if sentiment analysis
     * should be used.
     * 
     * @param enableSentimentAnalysis the enableSentimentAnalysis value to set.
     * @return the StartTranscriptionOptions object itself.
     */
    public StartTranscriptionOptions setEnableSentimentAnalysis(Boolean enableSentimentAnalysis) {
        this.enableSentimentAnalysis = enableSentimentAnalysis;
        return this;
    }

    /**
     * Set the summarizationOptions property: Summarization configuration options.
     * 
     * @param summarizationOptions the summarizationOptions value to set.
     * @return the StartTranscriptionOptions object itself.
     */
    public StartTranscriptionOptions setSummarizationOptions(SummarizationOptions summarizationOptions) {
        this.summarizationOptions = summarizationOptions;
        return this;
    }
}
