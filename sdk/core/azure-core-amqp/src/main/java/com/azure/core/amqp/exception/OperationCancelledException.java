// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

/**
 * This exception is thrown when the underlying AMQP layer encounters an abnormal link abort or the connection is
 * disconnected in an unexpected fashion.
 */
public class OperationCancelledException extends AmqpException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance of this exception with provided {@code message}.
     *
     * @param message Message associated with this exception.
     * @param context The context that caused this OperationCancelledException.
     */
    public OperationCancelledException(String message, AmqpErrorContext context) {
        super(false, message, context);
    }

    /**
     * Creates an instance of this exception with provided {@code message} and underlying {@code cause}.
     *
     * @param message Message associated with this exception.
     * @param cause The throwable that caused this exception to be thrown.
     * @param context The context that caused this OperationCancelledException.
     */
    public OperationCancelledException(final String message, final Throwable cause, AmqpErrorContext context) {
        super(false, message, cause, context);
    }
}
