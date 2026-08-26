// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.search.documents.models.implementation.sse.ServerSentEventHelper;
import java.time.Duration;

/**
 * Represents a server-sent event with a typed data payload.
 *
 * <p>An emitted server-sent event contains data and may expose an identifier, event name, comment, and retry interval.
 * The identifier and retry interval represent the effective stream state when the event was dispatched, including
 * values inherited from earlier metadata-only blocks.</p>
 *
 * <p>The identifier and retry interval are protocol metadata only. The client does not reconnect or replay the
 * request. Metadata-only updates received after the latest emitted event aren't exposed as an additional event.</p>
 *
 * @param <T> The type of the event data.
 */
@Immutable
public final class ServerSentEvent<T> {
    @Generated
    private final String id;
    @Generated
    private final String event;
    @Generated
    private final T data;
    @Generated
    private final String comment;
    @Generated
    private final Duration retryAfter;

    static {
        ServerSentEventHelper.setAccessor(new ServerSentEventHelper.ServerSentEventAccessor() {
            @Generated
            @Override
            public <U> ServerSentEvent<U> create(String id, String event, U data, String comment, Duration retryAfter) {
                return new ServerSentEvent<>(id, event, data, comment, retryAfter);
            }
        });
    }

    @Generated
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
     * event, or an empty string if an empty {@code id} field reset the identifier.
     */
    @Generated
    public String getId() {
        return id;
    }

    /**
     * Gets the event name.
     *
     * @return The event name, or {@code message} if no non-empty {@code event} field was specified.
     */
    @Generated
    public String getEvent() {
        return event;
    }

    /**
     * Gets the event data.
     *
     * @return The event data, or {@code null} if event data wasn't specified.
     */
    @Generated
    public T getData() {
        return data;
    }

    /**
     * Gets the event comment.
     *
     * @return The event comment, or {@code null} if it wasn't specified.
     */
    @Generated
    public String getComment() {
        return comment;
    }

    /**
     * Gets the effective retry interval when this event was dispatched.
     *
     * @return The latest valid retry interval received before this event, or {@code null} if no valid
     * {@code retry} field was received.
     */
    @Generated
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
