// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;

/**
 * Metadata for Transcription Streaming.
 */
public final class TranscriptionMetadata extends StreamingData {

    /*
     * Transcription Subscription Id.
     */
    private final String transcriptionSubscriptionId;

    /*
     * The target locale in which the translated text needs to be
     */
    private final String locale;

    /*
     * Call connection id
     */
    private final String callConnectionId;

    /*
     * Correlation id
     */
    private final String correlationId;

    /*
     * Gets or sets a value indicating if sentiment analysis should be used.
     */
    private final Boolean enableSentimentAnalysis;

    /*
     * PII redaction configuration options.
     */
    private final PiiRedactionOptions piiRedactionOptions;

    static {
        TranscriptionMetadataContructorProxy
            .setAccessor(new TranscriptionMetadataContructorProxy.TranscriptionMetadataContructorProxyAccessor() {
                @Override
                public TranscriptionMetadata create(TranscriptionMetadataConverter internalData) {
                    return new TranscriptionMetadata(internalData);
                }
            });
    }

    /**
     * Creates an instance of TranscriptionMetadata class.
     * @param internalData Transcription meta data internal.
     */
    TranscriptionMetadata(TranscriptionMetadataConverter internalData) {
        this.transcriptionSubscriptionId = internalData.getTranscriptionSubscriptionId();
        this.locale = internalData.getLocale();
        this.callConnectionId = internalData.getCallConnectionId();
        this.correlationId = internalData.getCorrelationId();
        this.enableSentimentAnalysis = internalData.getEnableSentimentAnalysis();
        this.piiRedactionOptions = internalData.getPiiRedactionOptions();
    }

    /**
     * Creates an instance of TranscriptionMetadata class.
     */
    public TranscriptionMetadata() {
        this.transcriptionSubscriptionId = null;
        this.locale = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.enableSentimentAnalysis = null;
        this.piiRedactionOptions = null;
    }

    /**
     * Get the transcriptionSubscriptionId property.
     *
     * @return the transcriptionSubscriptionId value.
     */
    public String getTranscriptionSubscriptionId() {
        return transcriptionSubscriptionId;
    }

    /**
     * Get the locale property.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Get the callConnectionId property.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Get the correlationId property.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Get the enableSentimentAnalysis property: Gets or sets a value indicating if
     * sentiment analysis should be used.
     * 
     * @return the enableSentimentAnalysis value.
     */
    public Boolean getEnableSentimentAnalysis() {
        return this.enableSentimentAnalysis;
    }

    /**
     * Get the piiRedactionOptions property: PII redaction configuration options.
     * 
     * @return the piiRedactionOptions value.
     */
    public PiiRedactionOptions getPiiRedactionOptions() {
        return this.piiRedactionOptions;
    }
}
