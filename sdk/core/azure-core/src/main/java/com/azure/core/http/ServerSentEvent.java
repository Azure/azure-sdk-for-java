// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.annotation.Immutable;

import java.time.Duration;

/**
 * Represents a server-sent event with a typed data payload.
 *
 * <p>A server-sent event may contain an identifier, event name, data, comment, and retry interval. This type stores
 * these values without parsing or normalizing them.</p>
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

    /**
     * Creates a server-sent event.
     *
     * @param id The event identifier.
     * @param event The event name.
     * @param data The event data.
     * @param comment The event comment.
     * @param retryAfter The retry interval.
     */
    public ServerSentEvent(String id, String event, T data, String comment, Duration retryAfter) {
        this.id = id;
        this.event = event;
        this.data = data;
        this.comment = comment;
        this.retryAfter = retryAfter;
    }

    /**
     * Gets the event identifier.
     *
     * @return The event identifier, or {@code null} if none was provided.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the event name.
     *
     * @return The event name, or {@code null} if none was provided.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Gets the event data.
     *
     * @return The event data, or {@code null} if none was provided.
     */
    public T getData() {
        return data;
    }

    /**
     * Gets the event comment.
     *
     * @return The event comment, or {@code null} if none was provided.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the retry interval.
     *
     * @return The retry interval, or {@code null} if none was provided.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
