// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.Locale;

/**
 * Represents a signal that caused the AMQP connection to shutdown.
 */
public class AmqpShutdownSignal {
    private final boolean isTransient;
    private final boolean initiatedByClient;
    private final String message;

    /**
     * Creates a new instance of ShutdownSignal.
     *
     * @param isTransient Whether the shutdown signal can be retried or not.
     * @param initiatedByClient {@code true} if the shutdown was initiated by the client; {@code false} otherwise.
     * @param message Message associated with the shutdown.
     */
    public AmqpShutdownSignal(boolean isTransient, boolean initiatedByClient, String message) {
        this.isTransient = isTransient;
        this.initiatedByClient = initiatedByClient;
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "%s, isTransient[%s], initiatedByClient[%s]", message, isTransient, initiatedByClient);
    }
}
