// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

/**
 * Represents the SSE response from the server on event stream interpretation
 * <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">here</a>.
 */
public class ServerSentEvent {
    private Long id;
    private String event;
    private String data;
    private String comment;
    private Long retryAfter;

    /**
     * Get event identifier.
     * <p>
     * Contains value of SSE {@code "id"} field. This field is optional and may return {@code null}, if the event
     * identifier is not specified.
     *
     * @return event id.
     */
    public Long getId() {
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
     */
    public String getData() {
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
     * Get new connection retry time in milliseconds the event receiver should wait before attempting to reconnect after a
     * connection to the SSE event source is lost.
     * <p>
     * Contains value of SSE {@code "retry"} field. This field is optional and method may return 0 if no
     * value has been set.
     *
     * @return reconnection delay in milliseconds or 0 if no value has been set.
     */
    public Long getRetryAfter() {
        return this.retryAfter;
    }

    /**
     * Set event ID
     * @param id the event id
     * @return the {@link ServerSentEvent} object
     */
    public ServerSentEvent setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Set the value of "event" field from stream
     * @param event the value of "event" field
     * @return the {@link ServerSentEvent} object
     */
    public ServerSentEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    /**
     * Set the value of "data" field from stream
     * @param data the value of "data" field
     * @return the {@link ServerSentEvent} object
     */
    public ServerSentEvent setData(String data) {
        this.data = data;
        return this;
    }

    /**
     * Set the comment associated with the event
     * @param comment the comment associated with the event
     * @return the {@link ServerSentEvent} object
     */
    public ServerSentEvent setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Set the new connection retry time in milliseconds the event receiver should wait before attempting to reconnect
     * after a connection error.
     * @param retryAfter the new connection retry time in milliseconds
     * @return the {@link ServerSentEvent} object
     */
    public ServerSentEvent setRetryAfter(Long retryAfter) {
        this.retryAfter = retryAfter;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("{id=").append(id).
            append("; event=\"").append(event).append("\"; data=\"").append(data).
            append("\"}").toString();
    }
}
