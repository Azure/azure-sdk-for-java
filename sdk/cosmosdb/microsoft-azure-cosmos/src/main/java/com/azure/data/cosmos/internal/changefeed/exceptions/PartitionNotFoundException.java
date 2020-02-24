// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.exceptions;

/**
 * Exception occurred when partition wasn't found.
 */
public class PartitionNotFoundException extends PartitionException {

    /**
     * Initializes a new instance of the {@link PartitionNotFoundException} class using error message and last continuation token.
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     */
    public PartitionNotFoundException(String message, String lastContinuation) {
        super(message, lastContinuation);
    }

    /**
     * Initializes a new instance of the {@link PartitionNotFoundException} class using error message, the last continuation
     *   token and the inner exception.
     *
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     * @param innerException the inner exception.
     */
    public PartitionNotFoundException(String message, String lastContinuation, Exception innerException) {
        super(message, lastContinuation, innerException);
    }
}
