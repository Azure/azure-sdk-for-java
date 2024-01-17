// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

/**
 * Interface to implement event stream listeners for handling {@link ServerSentEvent}.
 */
public interface EventStreamListener {

    /**
     * Gets called every time an event or data is received.
     *
     * @param sse the instance of {@link ServerSentEvent}
     */
    void onEvent(ServerSentEvent sse);

    /**
     * Gets called if an error has occurred
     *
     * @param throwable Error that occurred
     */
    void onError(Throwable throwable);

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
     * @param milliseconds new retry time in milliseconds
     * @param lastEventID ID of last event that was received
     */
    boolean shouldRetry(Throwable throwable, long milliseconds, long lastEventID);

    /**
     * Notify that the connection was closed.
     *
     * @param sse the instance of {@link ServerSentEvent}
     */
    void onClose(ServerSentEvent sse);
}
