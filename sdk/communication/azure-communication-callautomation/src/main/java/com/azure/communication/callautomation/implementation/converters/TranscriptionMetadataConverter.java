// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TranscriptionMetadataInternal model.
 */
public final class TranscriptionMetadataConverter {

    /*
     * Transcription Subscription Id.
     */
    @JsonProperty(value = "subscriptionId")
    private String transcriptionSubscriptionId;

    /*
     * The target locale in which the translated text needs to be
     */
    @JsonProperty(value = "locale")
    private String locale;

    /*
     * call connection Id.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

    /*
     * correlation Id
     */
    @JsonProperty(value = "correlationId")
    private String correlationId;

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
