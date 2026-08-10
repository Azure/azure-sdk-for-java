// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.annotation.Immutable;
import com.azure.core.implementation.util.ServerSentEventHelper;

import java.time.Duration;

/**
 * Represents a server-sent event with a typed data payload.
 *
 * <p>An emitted server-sent event contains data and may expose an identifier, event name, comment, and retry interval.
 * The identifier and retry interval represent the effective stream state when the event was dispatched, including
 * values inherited from earlier metadata-only blocks.</p>
 *
 * <p>Generated clients may use the effective identifier and retry interval to reconnect after a response body ends.
 * These values remain available to callers for diagnostics and service-specific stream handling. Metadata-only
 * updates received after the latest emitted event aren't exposed as an additional event.</p>
 *
 * @param <T> The type of the event data.
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
 * Parsing an event stream</a>
 */
@Immutable
public final class ServerSentEvent<T> {
    private final String id;
    private final String event;
    private final T data;
    private final String comment;
    private final Duration retryAfter;

    static {
        ServerSentEventHelper.setAccessor(new ServerSentEventHelper.ServerSentEventAccessor() {
            @Override
            public <U> ServerSentEvent<U> create(String id, String event, U data, String comment, Duration retryAfter) {
                return new ServerSentEvent<>(id, event, data, comment, retryAfter);
            }
        });
    }

    private ServerSentEvent(String id, String event, T data, String comment, Duration retryAfter) {
        this.id = id;
        this.event = event;
        this.data = data;
        this.comment = comment;
        this.retryAfter = retryAfter;
    }

    /**
     * Gets the effective last-event identifier when this event was dispatched.
     *
     * @return The effective last-event identifier, {@code null} if no valid {@code id} field was received before this
     * event, or an empty string if an empty {@code id} field reset the identifier. An empty identifier should not be
     * sent as a {@code Last-Event-Id} request header.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the event name.
     *
     * @return The event name, or {@code message} if no non-empty {@code event} field was specified.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Gets the event data.
     *
     * @return The event data, or {@code null} if event data wasn't specified.
     */
    public T getData() {
        return data;
    }

    /**
     * Gets the event comment.
     *
     * @return The event comment, or {@code null} if it wasn't specified.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the effective reconnection delay when this event was dispatched.
     *
     * @return The latest valid reconnection delay received before this event, or {@code null} if no valid
     * {@code retry} field was received.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }

}
