// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.util.ClientLogger;

import java.io.IOException;

/**
 * Interface to implement event stream listeners for handling {@link ServerSentEvent}.
 */
public interface ServerSentEventListener {
    ClientLogger LOGGER = new ClientLogger(ServerSentEventListener.class);

    /**
     * Gets called every time an event or data is received.
     *
     * @param sse the instance of {@link ServerSentEvent}
     */
    void onEvent(ServerSentEvent sse) throws IOException;

    /**
     * Gets called if an error has occurred
     *
     * @param throwable Error that occurred
     */
    default void onError(Throwable throwable) {
        LOGGER.atWarning().log("Unexpected failure in handling server sent event: {}", throwable.getMessage(),
            throwable);
    }

    /**
     * The stream can define the retry time sending a message with "retry: milliseconds"
     * If the received event contains the "retry" field, this method will be called after the retry time in
     * milliseconds and in case of an error.
     * <p>
     * The Server Sent Event protocol defines that should be able to reestablish a connection using retry mechanism.
     * In some cases depending on the error the connection should not be retried.
     * Implement this method to define this behavior for retry.
     * </p>
     *
     * @param throwable the instance of the error that caused the failure
     * @param retryAfter new retry time in milliseconds
     * @param lastEventId ID of last event that was received
     */
    default boolean shouldRetry(Throwable throwable, long retryAfter, long lastEventId) {
        // do not auto-retry.
        return false;
    }

    /**
     * Notify that the connection was closed.
     */
    default void onClose() {
        LOGGER.atInfo().log("Connection closed");
    }
}
