// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;

import java.util.List;
import java.util.ArrayList;

/**
 * The metadata of transcription which contains the information such as subscriptionId, locale ...
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
    * The list of target locale in which the translated text needs to be
    */
    private final List<String> locales;

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

    /*
     * The custom speech recognition model endpoint id
     */
    private final String speechRecognitionModelEndpointId;

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
        super(StreamingDataKind.TRANSCRIPTION_METADATA);
        this.transcriptionSubscriptionId = internalData.getTranscriptionSubscriptionId();
        this.locale = internalData.getLocale();
        this.locales
            = internalData.getLocales() != null ? new ArrayList<>(internalData.getLocales()) : new ArrayList<>();
        this.callConnectionId = internalData.getCallConnectionId();
        this.correlationId = internalData.getCorrelationId();
        this.enableSentimentAnalysis = internalData.getEnableSentimentAnalysis();
        this.piiRedactionOptions = internalData.getPiiRedactionOptions();
        this.speechRecognitionModelEndpointId = internalData.getSpeechRecognitionModelEndpointId();
    }

    /**
     * The custom speech recognition model endpoint id
     * Get the transcriptionSubscriptionId property.
     *
     * @return the transcriptionSubscriptionId value.
     */
    public String getTranscriptionSubscriptionId() {
        return transcriptionSubscriptionId;
    }

    /**
     * The target locale in which the translated text needs to be
     * Get the locale property.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * The target locales in which the translated text needs to be
     * Get the locale property.
     *
     * @return the locale value.
     */
    public List<String> getLocales() {
        return locales;
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
     * The custom speech recognition model endpoint id
     * Get the speechRecognitionModelEndpointId property.
     *
     * @return the speechRecognitionModelEndpointId value.
     */
    public String getSpeechRecognitionModelEndpointId() {
        return speechRecognitionModelEndpointId;
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
