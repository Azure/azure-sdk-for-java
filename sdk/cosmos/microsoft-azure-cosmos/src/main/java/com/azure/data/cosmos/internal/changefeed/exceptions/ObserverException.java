// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.exceptions;

/**
 * Exception occurred when an operation in a ChangeFeedObserver is running and throws by user code.
 */
public class ObserverException extends RuntimeException {
    private static final String DefaultMessage = "Exception has been thrown by the Observer.";

    /**
     * Initializes a new instance of the {@link ObserverException} class using the specified internal exception.
     *
     * @param originalException {@link Exception} thrown by the user code.
     */
    public ObserverException(Exception originalException) {
        super(DefaultMessage, originalException.getCause());
    }
}
