// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.http.policy.RetryPolicy;

/**
 * The set of options that can be specified when creating an {@link EventSender} to configure its behavior.
 */
public class EventSenderOptions {
    private String partitionId;
    private RetryPolicy retry;

    /**
     * Sets The identifier of the Event Hub partition that the {@link EventSender} will be bound to, limiting it to
     * sending events to only that partition.
     *
     * @param partitionId The identifier of the Event Hub partition that the {@link EventSender} will be bound to.
     * @return The updated SenderOptions object.
     */
    public EventSenderOptions partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    /**
     * Sets the retry policy used to govern retry attempts when an issue is encountered while sending.
     *
     * @param retry The retry policy used to govern retry attempts when an issue is encountered while sending.
     * @return The updated SenderOptions object.
     */
    public EventSenderOptions retry(RetryPolicy retry) {
        this.retry = retry;
        return this;
    }

    /**
     * Gets the retry policy used to govern retry attempts when an issue is encountered while sending.
     *
     * @return the retry policy used to govern retry attempts when an issue is encountered while sending. If
     * {@code null}, then the retry policy configured on the associated {@link EventHubClient} is used.
     */
    public RetryPolicy retry() {
        return retry;
    }

    /**
     * Gets the identifier of the Event Hub partition that the {@link EventSender} will be bound to, limiting it to
     * sending events to only that partition.
     *
     * If the identifier is not specified, the Event Hubs service will be responsible for routing events that sent to an
     * available partition.
     *
     * @return the identifier of the Event Hub partition that the {@link EventSender} will be bound to.
     */
    public String partitionId() {
        return partitionId;
    }

}
