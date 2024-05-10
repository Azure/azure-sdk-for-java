// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.implementation.util.ServerSentEventHelper;

import java.time.Duration;
import java.util.List;

/**
 * Represents the SSE response from the server on event stream interpretation
 * <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">here</a>.
 */
public final class ServerSentEvent {
    private String id;
    private String event;
    private List<String> data;
    private String comment;
    private Duration retryAfter;

    /**
     * Get event identifier.
     * <p>
     * Contains value of SSE {@code "id"} field. This field is optional and may return {@code null}, if the event
     * identifier is not specified.
     *
     * @return event id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get event field.
     * <p>
     * Contains value of SSE {@code "event"} field. This field is optional and may return {@code null}, if the event
     * is not specified.
     *
     * @return event, or {@code null} if not set.
     */
    public String getEvent() {
        return this.event;
    }

    /**
     * Get event data.
     * <p>
     * Contains value of SSE {@code "data"} field. This field is optional and may return {@code null}, if the event
     * data is not specified.
     *
     * @return event data.
     */
    public List<String> getData() {
        return this.data;
    }

    /**
     * Get the comment associated with the event.
     * <p>
     * This field is optional. Method may return {@code null}, if the event comment is not specified.
     *
     * @return comment associated with the event.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Get new connection retry time duration the event receiver should wait before attempting to reconnect after a
     * connection to the SSE event source is lost.
     * <p>
     * Contains value of SSE {@code "retry"} field. This field is optional and method may return 0 if no
     * value has been set.
     *
     * @return reconnection delay in milliseconds or 0 if no value has been set.
     */
    private Duration getRetryAfter() {
        return this.retryAfter;
    }

    /**
     * Set event ID
     * @param id the event id
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Set the value of "event" field from stream
     * @param event the value of "event" field
     */
    private void setEvent(String event) {
        this.event = event;
    }

    /**
     * Set the value of "data" field from stream
     * @param data the value of "data" field
     */
    private void setData(List<String> data) {
        this.data = data;
    }

    /**
     * Set the comment associated with the event
     * @param comment the comment associated with the event
     */
    private void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Set the new connection retry time duration the event receiver should wait before attempting to reconnect
     * after a connection error.
     * @param retryAfter the new connection retry time in milliseconds
     */
    private void setRetryAfter(Duration retryAfter) {
        this.retryAfter = retryAfter;
    }

    static {
        ServerSentEventHelper.setAccessor(new ServerSentEventHelper.ServerSentEventAccessor() {
            @Override
            public void setId(ServerSentEvent serverSentEvent, String id) {
                serverSentEvent.setId(id);
            }

            @Override
            public void setEvent(ServerSentEvent serverSentEvent, String event) {
                serverSentEvent.setEvent(event);
            }

            @Override
            public void setData(ServerSentEvent serverSentEvent, List<String> data) {
                serverSentEvent.setData(data);
            }

            @Override
            public void setComment(ServerSentEvent serverSentEvent, String comment) {
                serverSentEvent.setComment(comment);
            }

            @Override
            public void setRetryAfter(ServerSentEvent serverSentEvent, Duration retryAfter) {
                serverSentEvent.setRetryAfter(retryAfter);
            }

            @Override
            public Duration getRetryAfter(ServerSentEvent serverSentEvent) {
                return serverSentEvent.getRetryAfter();
            }
        });
    }
}
