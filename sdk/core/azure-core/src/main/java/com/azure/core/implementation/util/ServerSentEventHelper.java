// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class that accesses non-public members of {@link ServerSentEvent}.
 */
public final class ServerSentEventHelper {
    private static final AtomicReference<ServerSentEventAccessor> ACCESSOR = new AtomicReference<>();

    private ServerSentEventHelper() {
    }

    /**
     * Defines access to non-public members of {@link ServerSentEvent}.
     */
    public interface ServerSentEventAccessor {
        /**
         * Creates a server-sent event.
         *
         * @param id The event identifier.
         * @param event The event name.
         * @param data The event data.
         * @param comment The event comment.
         * @param retryAfter The retry interval.
         * @param <T> The type of the event data.
         * @return The server-sent event.
         */
        <T> ServerSentEvent<T> create(String id, String event, T data, String comment, Duration retryAfter);
    }

    /**
     * Sets the accessor.
     *
     * @param serverSentEventAccessor The accessor.
     */
    public static void setAccessor(final ServerSentEventAccessor serverSentEventAccessor) {
        ACCESSOR.set(Objects.requireNonNull(serverSentEventAccessor, "'serverSentEventAccessor' cannot be null."));
    }

    /**
     * Creates a server-sent event.
     *
     * @param id The event identifier.
     * @param event The event name.
     * @param data The event data.
     * @param comment The event comment.
     * @param retryAfter The retry interval.
     * @param <T> The type of the event data.
     * @return The server-sent event.
     */
    public static <T> ServerSentEvent<T> create(String id, String event, T data, String comment, Duration retryAfter) {
        return getAccessor().create(id, event, data, comment, retryAfter);
    }

    private static ServerSentEventAccessor getAccessor() {
        ServerSentEventAccessor accessor = ACCESSOR.get();
        if (accessor == null) {
            try {
                Class.forName(ServerSentEvent.class.getName(), true, ServerSentEvent.class.getClassLoader());
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException("Unable to initialize ServerSentEvent.", exception);
            }
            accessor = ACCESSOR.get();
        }
        return accessor;
    }
}
