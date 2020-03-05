// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.RequestTimeline;
import reactor.netty.http.client.HttpClientState;

import java.time.OffsetDateTime;

/**
 * Represents the timeline of various events in the lifetime of a reactor netty request response.
 * <p>
 * A {@link ReactorNettyRequestRecord} create a snapshot of {@link RequestTimeline} based on various {@link HttpClientState}.
 * Below are the states which are used to capture {@link RequestTimeline} snapshot.
 * <p><ul>
 * <li>{@link HttpClientState#CONNECTED},
 * <li>{@link HttpClientState#ACQUIRED},
 * <li>{@link HttpClientState#CONFIGURED},
 * <li>{@link HttpClientState#REQUEST_SENT},
 * <li>{@link HttpClientState#RESPONSE_RECEIVED},
 * </ul></p>
 */
public final class ReactorNettyRequestRecord {

    private volatile OffsetDateTime timeCreated;
    private volatile OffsetDateTime timeConnected;
    private volatile OffsetDateTime timeConfigured;
    private volatile OffsetDateTime timeSent;
    private volatile OffsetDateTime timeReceived;
    private volatile OffsetDateTime timeCompleted;

    /**
     * Gets request created offsetDateTime.
     * @return
     */
    public OffsetDateTime timeCreated() {
        return this.timeCreated;
    }

    /**
     * Get connection established offsetDateTime.
     * @return timeConnected
     */
    public OffsetDateTime timeConnected() {
        return this.timeConnected;
    }

    /**
     * Get connection configured offsetDateTime.
     * @return timeConfigured
     */
    public OffsetDateTime timeConfigured() {
        return this.timeConfigured;
    }

    /**
     * Gets request sent offsetDateTime.
     * @return timeSent
     */
    public OffsetDateTime timeSent() {
        return this.timeSent;
    }

    /**
     * Gets response received offsetDateTime.
     * @return timeReceived
     */
    public OffsetDateTime timeReceived() {
        return this.timeReceived;
    }

    /**
     * Gets request completed  offsetDateTime.
     * @return timeCompleted
     */
    public OffsetDateTime timeCompleted() {
        return this.timeCompleted;
    }

    /**
     * Sets request created offsetDateTime.
     * @param timeCreated
     */
    public void setTimeCreated(OffsetDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * Sets connection established offsetDateTime.
     * @param timeConnected
     */
    public void setTimeConnected(OffsetDateTime timeConnected) {
        this.timeConnected = timeConnected;
    }

    /**
     * Sets connection configured offsetDateTime.
     * @param timeConfigured
     */
    public void setTimeConfigured(OffsetDateTime timeConfigured) {
        this.timeConfigured = timeConfigured;
    }

    /**
     * Sets request sent offsetDateTime.
     * @param timeSent
     */
    public void setTimeSent(OffsetDateTime timeSent) {
        this.timeSent = timeSent;
    }

    /**
     * Sets response received offsetDateTime.
     * @param timeReceived
     */
    public void setTimeReceived(OffsetDateTime timeReceived) {
        this.timeReceived = timeReceived;
    }

    /**
     * Sets request completed offsetDateTime.
     * @param timeCompleted
     */
    public void setTimeCompleted(OffsetDateTime timeCompleted) {
        this.timeCompleted = timeCompleted;
    }

    /**
     * Creates the RequestTimeline snapshot.
     * @return requestTimeline
     */
    public RequestTimeline takeTimelineSnapshot() {

        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime timeCreated = this.timeCreated();
        OffsetDateTime timeConnected = this.timeConnected();
        OffsetDateTime timeConfigured = this.timeConfigured();
        OffsetDateTime timeSent = this.timeSent();
        OffsetDateTime timeReceived = this.timeReceived();
        OffsetDateTime timeCompleted = this.timeCompleted();
        OffsetDateTime timeCompletedOrNow = timeCompleted == null ? now : timeCompleted;

        return RequestTimeline.of(
            new RequestTimeline.Event("connectionCreated",
                timeCreated, timeConnected() == null ? timeCompletedOrNow : timeConnected),
            new RequestTimeline.Event("connectionConfigured",
                timeConnected, timeConfigured == null ? timeCompletedOrNow : timeConfigured),
            new RequestTimeline.Event("requestSent",
                timeConfigured, timeSent == null ? timeCompletedOrNow : timeSent),
            new RequestTimeline.Event("transitTime",
                timeSent, timeReceived == null ? timeCompletedOrNow : timeReceived),
            new RequestTimeline.Event("received",
                timeReceived, timeCompletedOrNow));
    }
}
