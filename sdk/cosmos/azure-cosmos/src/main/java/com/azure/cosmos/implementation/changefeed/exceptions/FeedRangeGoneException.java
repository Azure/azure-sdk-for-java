// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.exceptions;

/**
 * Exception occurred during partition split.
 */
public class FeedRangeGoneException extends PartitionException {
    /**
     * Initializes a new instance of the {@link FeedRangeGoneException} class using error message and last continuation token.
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     */
    public FeedRangeGoneException(String message, String lastContinuation) {
        super(message, lastContinuation);
    }

    /**
     * Initializes a new instance of the {@link FeedRangeGoneException} class using error message, the last continuation
     *   token and the inner exception.
     *
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     * @param innerException the inner exception.
     */
    public FeedRangeGoneException(String message, String lastContinuation, Exception innerException) {
        super(message, lastContinuation, innerException);
    }
}
