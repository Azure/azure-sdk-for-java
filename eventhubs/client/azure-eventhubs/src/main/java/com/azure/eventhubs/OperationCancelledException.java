// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;

/**
 * This exception is thrown when the underlying AMQP layer encounter an abnormal link abort or disconnect of connection
 * in an unexpected fashion.
 */
public class OperationCancelledException extends AmqpException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance of this exception with provided {@code message}.
     *
     * @param message Message associated with this exception.
     */
    public OperationCancelledException(final String message) {
        super(false, message);
    }

    /**
     * Creates an instance of this exception with provided {@code message} and underlying {@code cause}.
     *
     * @param message Message associated with this exception.
     * @param cause The throwable that caused this exception to be thrown.
     */
    public OperationCancelledException(final String message, final Throwable cause) {
        super(false, message, cause);
    }
}
