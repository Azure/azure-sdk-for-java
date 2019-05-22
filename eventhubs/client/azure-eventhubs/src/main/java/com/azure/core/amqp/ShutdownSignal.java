// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.Locale;

/**
 * Represents a signal that caused the connection to shutdown.
 */
public class ShutdownSignal {
    private final boolean isTransient;
    private final boolean initiatedByClient;
    private final String message;
    private final AmqpConnection connection;

    /**
     * Creates a new instance of ShutdownSignal.
     *
     * @param isTransient Whether the shutdown signal can be retried or not.
     * @param initiatedByClient {@code true} if the shutdown was initiated by the client; {@code false} otherwise.
     * @param message Message associated with the shutdown.
     */
    public ShutdownSignal(boolean isTransient, boolean initiatedByClient, String message) {
        this(isTransient, initiatedByClient, message, null);
    }

    /**
     * Creates a new instance of ShutdownSignal.
     *
     * @param isTransient Whether the shutdown signal can be retried or not.
     * @param initiatedByClient {@code true} if the shutdown was initiated by the client; {@code false} otherwise.
     * @param message Message associated with the shutdown.
     * @param connection Reference to the AMQP connection that was shutdown.
     */
    public ShutdownSignal(boolean isTransient, boolean initiatedByClient, String message, AmqpConnection connection) {
        this.isTransient = isTransient;
        this.initiatedByClient = initiatedByClient;
        this.message = message;
        this.connection = connection;
    }

    /**
     * Gets the AMQP connection that was shutdown; or {@code null} if none is associated with the shutdown..
     * @return the AMQP connection that was shutdown; or {@code null} if none caused the signal.
     */
    public AmqpConnection getConnection() {
        return this.connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "%s, isTransient[%s], initiatedByClient[%s]", message, isTransient, initiatedByClient);
    }
}
