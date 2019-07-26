// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.exceptions;

/**
 * General exception occurred during partition processing.
 */
public class PartitionException extends RuntimeException {
    private String lastContinuation;

    /**
     * Initializes a new instance of the {@link PartitionException} class using error message and last continuation token.
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     */
    public PartitionException(String message, String lastContinuation) {
        super(message);
        this.lastContinuation = lastContinuation;
    }

    /**
     * Initializes a new instance of the {@link PartitionException} class using error message, the last continuation
     *   token and the inner exception.
     *
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     * @param innerException the inner exception.
     */
    public PartitionException(String message, String lastContinuation, Exception innerException) {
        super(message, innerException.getCause());
        this.lastContinuation = lastContinuation;
    }

    /**
     * Gets the value of request continuation token.
     *
     * @return the value of request continuation token.
     */
    public String getLastContinuation() {
        return this.lastContinuation;
    }
}
