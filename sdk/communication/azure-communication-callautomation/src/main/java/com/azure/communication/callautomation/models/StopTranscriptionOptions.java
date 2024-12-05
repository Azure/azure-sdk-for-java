// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

/**
 * Options for the Stop Transcription operation.
 */
public class StopTranscriptionOptions {

    /**
     * The value to identify context of the operation.
     */
    private String operationContext;

    /**
     * Creates an instance of {@link StopTranscriptionOptions}.
     */
    public StopTranscriptionOptions() {
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
     * @return The StopTranscriptionOptions object.
     */
    public StopTranscriptionOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
