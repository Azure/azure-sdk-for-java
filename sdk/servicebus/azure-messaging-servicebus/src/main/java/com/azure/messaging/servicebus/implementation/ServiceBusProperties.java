// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Holds information about an Service Bus which can come handy while performing operations .
 *
 * @see com.azure.messaging.servicebus.QueueReceiverAsyncClient
 */
@Immutable
public final class ServiceBusProperties {
    private final String name;
    private final Instant createdAt;
    private final IterableStream<String> partitionIds;

    /**
     * Creates an instance of {@link ServiceBusProperties}.
     *
     * @param name Name of the Service Bus.
     * @param createdAt Datetime the Service Bus was created, in UTC.
     * @param partitionIds The partitions id in the Service Bus.
     *
     * @throws NullPointerException if {@code name}, {@code createdAt}, or {@code partitionIds} is {@code null}.
     */
    public ServiceBusProperties(final String name, final Instant createdAt, final String[] partitionIds) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null.");
        this.createdAt = Objects.requireNonNull(createdAt, "'createdAt' cannot be null.");
        this.partitionIds = new IterableStream<>(Arrays.asList(
            Objects.requireNonNull(partitionIds, "'partitionIds' cannot be null.")));
    }

    /**
     * Gets the name of the Service Bus.
     *
     * @return Name of the Service Bus.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the instant, in UTC, at which Event Hub was created.
     *
     * @return The instant, in UTC, at which the Event Hub was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the list of partition identifier of the Service Bus.
     *
     * @return The list of partition identifier of the Service Bus.
     */
    public IterableStream<String> getPartitionIds() {
        return partitionIds;
    }
}
