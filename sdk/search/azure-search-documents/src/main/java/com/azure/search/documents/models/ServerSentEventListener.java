// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

/**
 * A listener for receiving server-sent events.
 *
 * <p>Errors terminate processing, invoke {@link #onError(Throwable)} and {@link #onClose()}, and are rethrown to the
 * synchronous service caller as unchecked exceptions.</p>
 *
 * @param <T> The type of the event data.
 */
@FunctionalInterface
public interface ServerSentEventListener<T> {
    /**
     * Handles a server-sent event.
     *
     * @param event The server-sent event.
     * @throws RuntimeException If an error occurs while handling the event.
     */
    void onEvent(ServerSentEvent<T> event);

    /**
     * Handles an error that terminates event processing.
     *
     * @param error The error that terminated event processing.
     */
    default void onError(Throwable error) {
        // No-op by default.
    }

    /**
     * Handles closure of the event stream.
     */
    default void onClose() {
        // No-op by default.
    }
}
