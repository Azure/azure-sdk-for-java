// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

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

    /**
     * Creates an instance of TranscriptionMetadata class.
     * @param transcriptionSubscriptionId Transcription Subscription Id.
     * @param locale The target locale in which the translated text needs to be
     * @param callConnectionId Call connection id
     * @param correlationId Correlation id
     */
    public TranscriptionMetadata(String transcriptionSubscriptionId, String locale, String callConnectionId, String correlationId) {
        this.transcriptionSubscriptionId = transcriptionSubscriptionId;
        this.locale = locale;
        this.callConnectionId = callConnectionId;
        this.correlationId = correlationId;
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
}
