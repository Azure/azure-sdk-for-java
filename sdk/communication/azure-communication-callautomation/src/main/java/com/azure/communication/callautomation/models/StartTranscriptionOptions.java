// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

/**
 * Options for the Start Transcription operation.
 */
public class StartTranscriptionOptions {

    /**
     * Defines Locale for the transcription e,g en-US.
     */
    private String locale;

    /**
     * The value to identify context of the operation.
     */
    private String operationContext;

    /**
     * Creates an instance of {@link StartTranscriptionOptions}.
     */
    public StartTranscriptionOptions() {
    }

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
     * Sets the operation context.
     *
     * @param operationContext Operation Context
     * @return The StartTranscriptionOptions object.
     */
    public StartTranscriptionOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
