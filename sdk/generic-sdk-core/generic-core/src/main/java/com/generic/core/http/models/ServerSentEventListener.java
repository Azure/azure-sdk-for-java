// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import java.io.IOException;
import java.time.Duration;

/**
 * Interface to implement event stream listeners for handling {@link ServerSentEvent}.
 */
@FunctionalInterface
public interface ServerSentEventListener {

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
        // Do nothing
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
     * @param retryAfter new retry time duration
     * @param lastEventId ID of last event that was received
     */
    default boolean shouldRetry(Throwable throwable, Duration retryAfter, long lastEventId) {
        // do not auto-retry.
        return false;
    }

    /**
     * Notify that the connection was closed.
     */
    default void onClose() {
        // Do nothing
    }
}
