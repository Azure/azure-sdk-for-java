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

    public OperationCancelledException(final String message) {
        super(false, message);
    }

    public OperationCancelledException(final String message, final Throwable cause) {
        super(false, message, cause);
    }
}
