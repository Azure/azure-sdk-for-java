// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

/**
 * This exception is thrown when the underlying AMQP layer encounter an abnormal link abort or disconnect of connection in an unexpected fashion.
 */
public class OperationCancelledException extends EventHubException {
    private static final long serialVersionUID = 1L;

    OperationCancelledException() {
        super(false);
    }

    public OperationCancelledException(final String message) {
        super(false, message);
    }

    OperationCancelledException(final Throwable cause) {
        super(false, cause);
    }

    public OperationCancelledException(final String message, final Throwable cause) {
        super(false, message, cause);
    }
}
