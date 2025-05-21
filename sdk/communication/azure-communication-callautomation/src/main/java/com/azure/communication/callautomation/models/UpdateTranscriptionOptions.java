// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * Options for the Update Transcription operation.
 */
@Fluent
public final class UpdateTranscriptionOptions {

    /**
     * Defines Locale for the transcription e,g en-US.
     */
    private String locale;

    /**
     * The value to identify context of the operation.
     */
    private String operationContext;

    /**
     * Creates an instance of {@link UpdateTranscriptionOptions}.
     */
    public UpdateTranscriptionOptions() {
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
     * @return The UpdateTranscriptionOptions object.
     */
    public UpdateTranscriptionOptions setLocale(String locale) {
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
     * @return The UpdateTranscriptionOptions object.
     */
    public UpdateTranscriptionOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     * 
     * @param speechRecognitionModelEndpointId the speechRecognitionModelEndpointId value to set.
     * @return the UpdateTranscriptionOptions object itself.
     */
    public UpdateTranscriptionOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }
}
